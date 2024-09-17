package cn.zhoutaolinmusic.schedule;

import cn.zhoutaolinmusic.constant.AuditStatus;
import cn.zhoutaolinmusic.constant.RedisConstant;
import cn.zhoutaolinmusic.entity.Setting;
import cn.zhoutaolinmusic.entity.video.Video;
import cn.zhoutaolinmusic.entity.vo.HotVideo;
import cn.zhoutaolinmusic.service.SettingService;
import cn.zhoutaolinmusic.service.video.VideoService;
import cn.zhoutaolinmusic.utils.RedisCacheUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 热度排行榜
 */
@Component
public class HotRank {

    @Autowired
    private VideoService videoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SettingService settingService;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    ObjectMapper om = new ObjectMapper();
    Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
    {
        jackson2JsonRedisSerializer.setObjectMapper(om);
    }


    @Scheduled(cron = "0 * * * * ?")
    public void hotRank() {
        System.out.println("开始计算视频热度排行榜");
        // 添加队列，从小到大
        final TopK topK = new TopK(10, new PriorityQueue<>(10, Comparator.comparing(HotVideo::getHot)));
        // 每次取1000个
        long limit = 1000;

        // 获取视频的条件 id > 0 且 已审核完成 且 视频开放 获取最后的1000个
        long id = 0;
        List<Video> videos = videoService.list(new LambdaQueryWrapper<Video>()
                .select(Video::getId, Video::getShareCount, Video::getHistoryCount, Video::getStartCount, Video::getFavoritesCount,
                        Video::getGmtCreated, Video::getTitle).gt(Video::getId, id)
                .eq(Video::getAuditStatus, AuditStatus.SUCCESS).eq(Video::getOpen, 0).last("limit " + limit));

        while (!ObjectUtils.isEmpty(videos)) {
            for (Video video : videos) {
                // 自动乘以系数，自定义推荐算法
                Long shareCount = video.getShareCount();
                Double historyCount = video.getHistoryCount() * 0.8;
                Long startCount = video.getStartCount();
                Double favoritesCount = video.getFavoritesCount() * 1.5;
                final Date date = new Date();
                long t = date.getTime() - video.getGmtCreated().getTime();
                // 随机获取6位数,用于去重
                final double v = weightRandom();
                final double hot = hot(shareCount + historyCount + startCount + favoritesCount + v, TimeUnit.MILLISECONDS.toDays(t));
                final HotVideo hotVideo = new HotVideo(hot, video.getId(), video.getTitle());

                // 添加到队列中，队列自行维护顺序
                topK.add(hotVideo);
            }

            // 获取下一页数据
            id = videos.get(videos.size() - 1).getId();
            videos = videoService.list(new LambdaQueryWrapper<Video>().gt(Video::getId, id).last("limit " + limit));
        }

        final byte[] key = RedisConstant.HOT_RANK.getBytes();
        // 向列表添加数据
        final List<HotVideo> hotVideos = topK.get();
        // 最小热度数据 小 》 大
        final Double minHot = hotVideos.get(0).getHot();
        // 插入数据
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (HotVideo hotVideo : hotVideos) {
                final Double hot = hotVideo.getHot();
                try {
                    hotVideo.setHot(null);
                    // 不这样写铁报错！序列化问题
                    connection.zAdd(key, hot, jackson2JsonRedisSerializer.serialize(om.writeValueAsString(hotVideo)));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });

        // 删除 最小分数到0 的所有数据
        redisTemplate.opsForZSet().removeRangeByScore(RedisConstant.HOT_RANK, minHot,0);
    }

    // 热门视频,没有热度排行榜实时且重要
    @Scheduled(cron = "0 * * * * ?")
    public void hotVideo() {
        System.out.println("开始计算视频实时热门");
        // 分片查询3天内的视频
        int limit = 1000;
        long id = 1;
        // 获取前n天的视频
        List<Video> videos = videoService.selectNDaysAgeVideo(id, 3000, limit);
        final Double hotLimit = settingService.list(new LambdaQueryWrapper<>()).get(0).getHotLimit();
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DATE);

        while (!ObjectUtils.isEmpty(videos)) {
            final ArrayList<Long> hotVideos = new ArrayList<>();
            for (Video video : videos) {
                Long shareCount = video.getShareCount();
                Double historyCount = video.getHistoryCount() * 0.8;
                Long startCount = video.getStartCount();
                Double favoritesCount = video.getFavoritesCount() * 1.5;
                final Date date = new Date();
                long t = date.getTime() - video.getGmtCreated().getTime();
                final double hot = hot(shareCount + historyCount + startCount + favoritesCount, TimeUnit.MILLISECONDS.toDays(t));

                // 大于X热度说明是热门视频
                if (hot > hotLimit) {
                    hotVideos.add(video.getId());
                }

            }
            id = videos.get(videos.size() - 1).getId();
            videos = videoService.selectNDaysAgeVideo(id, 3, limit);
            // RedisConstant.HOT_VIDEO + 今日日期 作为key  达到元素过期效果
            if (!ObjectUtils.isEmpty(hotVideos)){
                String key = RedisConstant.HOT_VIDEO + today;
                redisTemplate.opsForSet().add(key, hotVideos.toArray(new Object[hotVideos.size()]));
                redisTemplate.expire(key, 3, TimeUnit.DAYS);
            }

        }
    }

    static double a = 0.011;

    public static double hot(double weight, double t) {
        return weight * Math.exp(-a * t);
    }


    public double weightRandom() {
        int i = (int) ((Math.random() * 9 + 1) * 100000);
        return i / 1000000.0;
    }

}
