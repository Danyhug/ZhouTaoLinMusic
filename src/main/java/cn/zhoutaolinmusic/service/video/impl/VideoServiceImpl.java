package cn.zhoutaolinmusic.service.video.impl;

import cn.zhoutaolinmusic.config.LocalCache;
import cn.zhoutaolinmusic.config.QiNiuConfig;
import cn.zhoutaolinmusic.constant.AuditStatus;
import cn.zhoutaolinmusic.constant.RedisConstant;
import cn.zhoutaolinmusic.entity.File;
import cn.zhoutaolinmusic.entity.VideoTask;
import cn.zhoutaolinmusic.entity.user.User;
import cn.zhoutaolinmusic.entity.video.Video;
import cn.zhoutaolinmusic.entity.video.VideoShare;
import cn.zhoutaolinmusic.entity.video.VideoStar;
import cn.zhoutaolinmusic.entity.video.VideoType;
import cn.zhoutaolinmusic.entity.vo.BasePage;
import cn.zhoutaolinmusic.entity.vo.HotVideo;
import cn.zhoutaolinmusic.entity.vo.UserModel;
import cn.zhoutaolinmusic.entity.vo.UserVO;
import cn.zhoutaolinmusic.exception.BaseException;
import cn.zhoutaolinmusic.mapper.video.VideoMapper;
import cn.zhoutaolinmusic.service.FeedService;
import cn.zhoutaolinmusic.service.FileService;
import cn.zhoutaolinmusic.service.InterestPushService;
import cn.zhoutaolinmusic.service.user.FavoritesService;
import cn.zhoutaolinmusic.service.user.FollowService;
import cn.zhoutaolinmusic.service.user.UserService;
import cn.zhoutaolinmusic.service.video.TypeService;
import cn.zhoutaolinmusic.service.video.VideoService;
import cn.zhoutaolinmusic.service.video.VideoShareService;
import cn.zhoutaolinmusic.service.video.VideoStarService;
import cn.zhoutaolinmusic.utils.FileUtil;
import cn.zhoutaolinmusic.utils.RedisCacheUtil;
import cn.zhoutaolinmusic.utils.UserHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private QiNiuConfig qiNiuConfig;

    @Autowired
    private InterestPushService interestPushService;

    @Autowired
    private FeedService feedService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // @Autowired
    // private VideoAuditService videoAuditService;

    @Autowired
    private TypeService typeService;
    @Autowired
    private VideoStarService videoStarService;
    @Autowired
    private VideoShareService videoShareService;
    @Autowired
    private FavoritesService favoritesService;
    @Autowired
    private FollowService followService;
    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Override
    public Video getVideoById(Long videoId, Long userId) {
        Video video = this.getOne(new LambdaQueryWrapper<Video>().eq(Video::getId, videoId));
        if (video == null) throw new BaseException("指定视频信息不存在");

        // 如果是私密视频返回空
        if (video.getOpen()) return new Video();

        // 收集用户自己对该视频的 点赞 / 收藏 信息
        video.setStart(videoStarService.starState(videoId, userId));
        video.setFavorites(favoritesService.favoritesState(videoId, userId));
        video.setFollow(followService.isFollows(video.getUserId(), userId));
        return video;
    }

    @Override
    public void publishVideo(Video video) {
        Long userId = UserHolder.get();
        Video oldVideo = null;

        Long videoId = video.getId();
        if (videoId != null) {
            // 获取视频源信息
            oldVideo = this.getOne(new LambdaQueryWrapper<Video>().eq(Video::getId, videoId).eq(Video::getUserId, userId));

            // 不允许修改视频封片和视频源
            if (!oldVideo.buildVideoUrl().equals(video.buildVideoUrl())
                    || !video.buildCoverUrl().equals(oldVideo.buildCoverUrl())) {
                throw new BaseException("视频源和封面不能修改");
            }
        }

        // 校验标签最多不能超过5个
        if (video.buildLabel().size() > 5) {
            throw new BaseException("标签最多只能选择5个");
        }

        // 修改状态
        video.setAuditStatus(AuditStatus.PROCESS);
        video.setUserId(userId);

        // 判断是添加还是修改
        boolean isAdd = videoId == null;

        // 设置一个空 Yv 号
        video.setYv(null);

        if (!isAdd) {
            // 修改时，下面字段不被修改，设置为空
            video.setVideoType(null);
            video.setLabelNames(null);
            video.setUrl(null);
            video.setCover(null);
        } else {
            // 未设置封面生成默认封面
            if (ObjectUtils.isEmpty(video.getCover())) {
                video.setCover(fileService.generatePhoto(video.getUrl(), userId));
            }

            // 生成视频号
            video.setYv("YV" + UUID.randomUUID().toString().replace("-", "").substring(8));
        }

        // 填充视频时长
        if (isAdd || !StringUtils.hasLength(oldVideo.getDuration())) {
            String uuid = UUID.randomUUID().toString();
            LocalCache.put(uuid, true);
            try {
                Long url = video.getUrl();
                if (url == null || url == 0) url = oldVideo.getUrl();
                String fileKey = fileService.getById(url).getFileKey();
                String duration = FileUtil.getVideoDuration(qiNiuConfig.getCname() + "/" + fileKey + "?uuid=" + uuid);
                video.setDuration(duration);
            } finally {
                LocalCache.rm(uuid);
            }
        }
        this.saveOrUpdate(video);

        // 提交审核
        VideoTask videoTask = new VideoTask();
        videoTask.setOldVideo(video);
        videoTask.setVideo(video);
        videoTask.setIsAdd(isAdd);
        videoTask.setOldState(isAdd || video.getOpen());
        videoTask.setNewState(true);
        // TODO
        // videoPublishAuditService.audit(videoTask, false);
    }

    @Override
    public void deleteVideo(Long id) {
        Long userId = UserHolder.get();
        Video video = this.getOne(
                new LambdaQueryWrapper<Video>()
                        .eq(Video::getId, id).eq(Video::getUserId, userId)
        );

        if (video == null) {
            throw new BaseException("要删除的视频不存在");
        }

        if (this.removeById(id)) {
            new Thread(() -> {
                // 删除 分享量 & 点赞量
                videoShareService.remove(
                        new LambdaQueryWrapper<VideoShare>()
                                .eq(VideoShare::getVideoId, id)
                                .eq(VideoShare::getUserId, userId)
                );
                videoStarService.remove(
                        new LambdaQueryWrapper<VideoStar>()
                                .eq(VideoStar::getVideoId, id)
                                .eq(VideoStar::getUserId, userId)
                );

                // 兴趣推送删除
                // 删除对应标签的视频数据
                // interestPushService.deleteSystemStockIn(video);
                // 删除对应分类的视频数据
                // interestPushService.deleteSystemTypeStockIn(video);
            }).start();
        }

    }

    @Override
    public Collection<Video> pushVideos(Long userId) {
        User user = null;
        // 获取用户信息
        if (userId != null) user = userService.getById(userId);

        // 获取用户喜欢的视频id
        Collection<Long> videoIds = interestPushService.listVideoIdByUserModel(user);

        if (ObjectUtils.isEmpty(videoIds)) {
            // 获取所有的视频id，创建时间 倒序
            videoIds = list(new LambdaQueryWrapper<Video>().orderByDesc(Video::getGmtCreated))
                    .stream().map(Video::getId).collect(Collectors.toList());
        }

        if (ObjectUtils.isEmpty(videoIds)) return new ArrayList<>();

        Collection<Video> videos = this.listByIds(videoIds);
        addVideosDetailInfo(videos);
        return videos;
    }

    // 添加推送视频的用户信息（用户信息和视频对应的文件信息）
    public void addVideosDetailInfo(Collection<Video> videos) {
        if (ObjectUtils.isEmpty(videos)) return;

        Set<Long> userIds = new HashSet<>();
        ArrayList<Long> fileIds = new ArrayList<>();
        // 遍历所有视频信息
        for (Video video: videos) {
            // 获取用户id
            userIds.add(video.getUserId());
            // 获取视频id
            fileIds.add(video.getUrl());
            // 获取视频封面对应的fileId
            fileIds.add(video.getCover());
        }

        // 获取所有的文件信息 key为id，value为本身（identity）
        Map<Long, File> fileMap = fileService.listByIds(fileIds).stream()
                .collect(Collectors.toMap(File::getId, Function.identity()));
        // 获取所有的用户信息 key为id，value为本身（identity）
        Map<Long, User> userMap = userService.list(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        for (Video video: videos) {
            UserVO userVO = new UserVO();
            // 获取视频对应的用户信息
            User user = userMap.get(video.getUserId());
            userVO.setId(video.getUserId());
            userVO.setNickName(user.getNickName());
            userVO.setDescription(user.getDescription());
            userVO.setSex(user.getSex());
            // 视频关联用户信息
            video.setUser(userVO);
            // 视频关联文件信息
            File file = fileMap.get(video.getUrl());
            video.setVideoType(file.getFormat());
        }
    }

    @Override
    public Collection<Video> getVideoByTypeId(Long typeId) {
        if (typeId == null) return Collections.emptyList();

        // 获取视频分类
        VideoType videoType = typeService.getById(typeId);
        // 分类不存在
        if (videoType == null) throw new BaseException("分类不存在");

        // 获取该分类下的视频
        Collection<Long> videoIds = interestPushService.listVideoIdByTypeId(typeId);
        if (ObjectUtils.isEmpty(videoIds)) return Collections.emptyList();
        // 获取所有视频信息
        Collection<Video> videos = this.listByIds(videoIds);

        addVideosDetailInfo(videos);
        return videos;
    }

    @Override
    public IPage<Video> searchVideo(String search, BasePage basePage, Long userId) {
        if (ObjectUtils.isEmpty(search)) throw new BaseException("搜索内容不能为空");

        IPage p = basePage.page();
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        // 携带YV则精准搜索
        if (search.contains("YV")) {
            wrapper.eq(Video::getYv, search);
        } else {
            // 模糊搜索标题
            wrapper.like(Video::getTitle, search);
        }

        IPage<Video> page = this.page(p, wrapper);
        List<Video> videos = page.getRecords();

        // 添加用户信息
        addVideosDetailInfo(videos);
        // 添加用户搜索记录
        userService.addSearchHistory(userId, search);

        return page;
    }

    @Override
    public void auditProcess(Video video) {
        // 审核通过
        this.updateById(video);
        // 添加到兴趣推送
        interestPushService.pushSystemStockIn(video);
        interestPushService.pushSystemTypeStockIn(video);

        // 审核状态添加到邮箱 TODO
        // feedService.pushInBoxFeed(video.getUserId(), video.getId(), video.getGmtCreated().getTime());
    }

    @Override
    public boolean startVideo(Long videoId) {
        // 获取视频信息
        Video video = this.getById(videoId);
        if (video == null) throw new BaseException("点赞的视频不存在");

        VideoStar videoStar = new VideoStar();
        // 添加点赞信息
        videoStar.setVideoId(videoId);
        videoStar.setUserId(video.getUserId());

        // 点赞是否成功
        boolean result = videoStarService.starVideo(videoStar);
        updateStar(video, result ? 1L : -1L);

        // 获取标签
        List<String> labels = video.buildLabel();
        // 更新用户模型
        UserModel userModel = UserModel.buildUserModel(labels, videoId, 1.0);
        interestPushService.updateUserModel(userModel);

        return result;
    }

    /**
     * 更新点赞
     * @param video
     * @param value
     */
    public void updateStar(Video video, Long value) {
        UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("start_count = start_count + #{value}");
        updateWrapper.lambda().eq(Video::getId, video.getId()).eq(Video::getStartCount, video.getStartCount());
        this.update(video, updateWrapper);
    }

    @Override
    public boolean shareVideo(VideoShare videoShare) {
        // TODO
        return false;
    }

    /**
     * 浏览量
     *
     * @param video
     */
    public void updateHistory(Video video, Long value) {
        final UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("history_count = history_count + " + value);
        updateWrapper.lambda().eq(Video::getId, video.getId()).eq(Video::getHistoryCount, video.getHistoryCount());
        this.update(video, updateWrapper);
    }

    @Override
    public void historyVideo(Long videoId, Long userId) throws Exception {
        String key = RedisConstant.HISTORY_VIDEO + userId;
        Object o = redisCacheUtil.get(key);

        // 因为是有序集合，key值不可重复，所以说只需要添加一次就可以了
        if (o == null) {
            redisCacheUtil.set(key, videoId, RedisConstant.HISTORY_TIME);
            Video video = this.getById(videoId);
            video.setUser(userService.getInfo(video.getUserId()));
            // 设置分类名称
            video.setTypeName(typeService.getById(video.getTypeId()).getName());
            redisCacheUtil.addSortList(RedisConstant.USER_HISTORY_VIDEO + userId, new Date().getTime(), video, RedisConstant.HISTORY_TIME);
            updateHistory(video, 1L);
        }
    }

    public void updateFavorites(Video video, Long value) {
        final UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("favorites_count = favorites_count + " + value);
        updateWrapper.lambda().eq(Video::getId, video.getId()).eq(Video::getFavoritesCount, video.getFavoritesCount());
        this.update(video, updateWrapper);
    }

    @Override
    public boolean favoritesVideo(Long fId, Long vId) {
        Video video = this.getById(vId);
        if (video == null) throw new BaseException("收藏的视频不存在");

        boolean favorites = favoritesService.favorites(fId, vId);
        updateFavorites(video, favorites ? 1L : -1L);

        List<String> labels = video.buildLabel();

        // 更新用户模型
        UserModel userModel = UserModel.buildUserModel(labels, vId, 2.0);
        interestPushService.updateUserModel(userModel);

        return favorites;
    }

    @Override
    public LinkedHashMap<String, List<Video>> getHistory(BasePage basePage) {
        // 获取浏览历史
        Long userId = UserHolder.get();
        String key = RedisConstant.USER_HISTORY_VIDEO + userId;
        Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisCacheUtil.getSortListByPage(key, basePage.getPage(), basePage.getLimit());
        // 缓存没有，返回空
        if (ObjectUtils.isEmpty(typedTuples)) return new LinkedHashMap<>();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<Video> temp = new ArrayList<>();

        LinkedHashMap<String, List<Video>> result = new LinkedHashMap<>();
        for (ZSetOperations.TypedTuple<Object> typedTuple : typedTuples) {
            Date date = new Date(typedTuple.getScore().longValue());
            String format = simpleDateFormat.format(date);
            if (!result.containsKey(format)) {
                result.put(format, new ArrayList<>());
            }
            Video video = (Video) typedTuple.getValue();
            // 按时间分类，添加视频数据
            result.get(format).add(video);
            temp.add(video);
        }
        addVideosDetailInfo(temp);

        return result;
    }

    @Override
    public Collection<Video> listVideoByFavorites(Long favoritesId) {
        // 获取收藏夹数据
        List<Long> videoIds = favoritesService.listVideoIds(favoritesId, UserHolder.get());
        if (ObjectUtils.isEmpty(videoIds)) return Collections.emptyList();

        // 根据收藏夹的视频id获取所有的视频数据
        Collection<Video> videos = this.listByIds(videoIds);
        // 添加视频详情信息
        addVideosDetailInfo(videos);

        return videos;
    }

    @Override
    public Collection<HotVideo> hotRank() {
        // 获取热门视频
        Set<ZSetOperations.TypedTuple<Object>> zSet = redisTemplate.opsForZSet().reverseRangeWithScores(RedisConstant.HOT_RANK, 0, -1);
        ArrayList<HotVideo> hotVideos = new ArrayList<>();

        // 遍历所有热门视频
        for (ZSetOperations.TypedTuple<Object> objectTypedTuple : zSet) {
            HotVideo hotVideo;
            try {
                // 获取视频信息 id 、title
                hotVideo = objectMapper.readValue(objectTypedTuple.getValue().toString(), HotVideo.class);
                // 获取热点值
                hotVideo.setHot((double) objectTypedTuple.getScore().intValue());
                // 将热点值格式化为字符串
                hotVideo.hotFormat();
                hotVideos.add(hotVideo);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return hotVideos;
    }

    @Override
    public Collection<Video> listSimilarVideo(Video video) {
        if (ObjectUtils.isEmpty(video.getId()) || ObjectUtils.isEmpty(video)) return Collections.emptyList();

        // 获取视频标签
        List<String> labels = video.buildLabel();
        ArrayList<String> labelNames = new ArrayList<>();
        // 将所有的标签添加到集合中
        labelNames.addAll(labels);

        // 根据标签获取视频id
        Set<Long> videoIds = (Set<Long>) interestPushService.listVideoIdByLabels(labelNames);

        // 推荐的视频不要有当前的视频
        videoIds.remove(video.getId());

        Collection<Video> videos = new ArrayList<>();
        if (!ObjectUtils.isEmpty(videoIds)) {
            videos = this.listByIds(videoIds);
            addVideosDetailInfo(videos);
        }
        return videos;
    }

    @Override
    public IPage<Video> listByUserIdOpenVideo(Long userId, BasePage basePage) {
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<Video>().eq(Video::getUserId, userId).eq(Video::getOpen, 0);
        IPage<Video> page = this.page(basePage.page(), wrapper);
        List<Video> videos = page.getRecords();
        addVideosDetailInfo(videos);
        return page;
    }

    @Override
    public String getAuditQueueState() {
        return "";
    }

    @Override
    public List<Video> selectNDaysAgeVideo(long id, int days, int limit) {
        return null;
    }

    @Override
    public Collection<Video> listHotVideo() {
        // 获取热门视频
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DATE);

        HashMap<String, Integer> map = new HashMap<>();

        // 优先推荐今天的视频，后面是权重
        map.put(RedisConstant.HOT_VIDEO + today, 10);
        map.put(RedisConstant.HOT_VIDEO + (today - 1), 3);
        map.put(RedisConstant.HOT_VIDEO + (today - 2), 2);

        List<Long> hotVideoIds = redisTemplate.executePipelined((RedisCallback<?>) connection -> {
            // 传入key为当天日期，value为获取当天的几个视频，最后获取的是当天的视频列表
            map.forEach((key, value) -> {
                connection.sRandMember(key.getBytes(), value);
            });
            return null;
        });

        // 没有热门视频
        if (ObjectUtils.isEmpty(hotVideoIds)) return Collections.emptyList();

        ArrayList<Long> videoIds = new ArrayList<>();
        for (Object hotVideoId : hotVideoIds) {
            if (!ObjectUtils.isEmpty(hotVideoId)) {
                videoIds.addAll(Arrays.asList((Long[]) hotVideoId));
            }
        }

        if (ObjectUtils.isEmpty(videoIds)) return Collections.emptyList();

        Collection<Video> videos = this.listByIds(videoIds);
        addVideosDetailInfo(videos);

        return videos;
    }

    @Override
    public Collection<Video> followFeed(Long userId, Long lastTime) {
        // 查看redis中是否存在
        Set<Long> set = redisTemplate.opsForZSet().reverseRangeByScore(
                RedisConstant.IN_FOLLOW + userId,
                    0, lastTime == null ? new Date().getTime() : lastTime, lastTime == null ? 0 : 1, 5);
        // 缓存中不存在
        if (ObjectUtils.isEmpty(set)) return Collections.emptyList();

        // 查到的数据按时间排序
        Collection<Video> videos = this.list(new LambdaQueryWrapper<Video>()
                .in(Video::getId, set).orderByDesc(Video::getGmtCreated));
        addVideosDetailInfo(videos);

        return videos;
    }

    @Override
    public void initFollowFeed(Long userId) {
        // 获取所有关注的人
        Collection<Long> followIds = followService.getFollow(userId, null);
        log.info("获取所有关注的人 {}", followIds);
        feedService.initFollowFeed(userId, followIds);
    }

    @Override
    public IPage<Video> listByUserIdVideo(BasePage basePage, Long userId) {
        // 获取用户的所有视频数据
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<Video>()
                .eq(Video::getUserId, userId).orderByDesc(Video::getGmtCreated);
        IPage page = this.page(basePage.page(), wrapper);
        addVideosDetailInfo(page.getRecords());

        return page;
    }

    @Override
    public Collection<Long> listVideoIdByUserId(Long userId) {
        return null;
    }

    @Override
    public void violations(Long id) {

    }
}
