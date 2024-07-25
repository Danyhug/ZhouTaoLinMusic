package cn.zhoutaolinmusic.service.impl;

import cn.zhoutaolinmusic.constant.RedisConstant;
import cn.zhoutaolinmusic.service.FeedService;
import cn.zhoutaolinmusic.utils.DateUtil;
import cn.zhoutaolinmusic.utils.RedisCacheUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Service
public class FeedServiceImpl implements FeedService {

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void pusOutBoxFeed(Long userId, Long videoId, Long time) {
        // 推入发件箱
        redisCacheUtil.addSortList(RedisConstant.OUT_FOLLOW + userId, time, videoId, -1);
    }

    @Override
    public void pushInBoxFeed(Long userId, Long videoId, Long time) {
        // 推入收件箱
        redisCacheUtil.addSortList(RedisConstant.IN_FOLLOW + userId, time, videoId, -1);
    }

    @Override
    @Async
    public void deleteOutBoxFeed(Long userId, Collection<Long> fans, Long videoId) {
        // 删除收件箱
        String inFollow = RedisConstant.IN_FOLLOW;
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Long fan: fans) {
                connection.zRem((inFollow + fan).getBytes(), String.valueOf(videoId).getBytes());
            }
            // 删除发件箱
            connection.zRem((RedisConstant.OUT_FOLLOW + userId).getBytes(), String.valueOf(videoId).getBytes());
            return null;
        });
    }

    @Override
    public void deleteInBoxFeed(Long userId, List<Long> videoIds) {
        redisTemplate.opsForZSet().remove(RedisConstant.IN_FOLLOW + userId, videoIds.toArray());
    }

    @Override
    public void initFollowFeed(Long userId, Collection<Long> followIds) {
        final Date curDate = new Date();
        final Date limitDate = DateUtil.addDateDays(curDate, -7);

        final Set<ZSetOperations.TypedTuple<Long>> set = redisTemplate.opsForZSet().rangeWithScores(RedisConstant.IN_FOLLOW, -1, -1);
        if (!ObjectUtils.isEmpty(set)) {
            Double oldTime = set.iterator().next().getScore();
            init(userId,oldTime.longValue(),new Date().getTime(),followIds);
        } else {
            init(userId,limitDate.getTime(),curDate.getTime(),followIds);
        }

    }

    public void init(Long userId, Long min, Long max, Collection<Long> followIds) {
        String t1 = RedisConstant.OUT_FOLLOW;
        String t2 = RedisConstant.IN_FOLLOW;
        // 查看关注人的发件箱
        final List<Set<DefaultTypedTuple>> result = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Long followId : followIds) {
                connection.zRevRangeByScoreWithScores((t1 + followId).getBytes(), min, max, 0, 50);
            }
            return null;
        });
        final ObjectMapper objectMapper = new ObjectMapper();
        final HashSet<Long> ids = new HashSet<>();
        // 放入收件箱
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Set<DefaultTypedTuple> tuples : result) {
                if (!ObjectUtils.isEmpty(tuples)) {

                    for (DefaultTypedTuple tuple : tuples) {

                        final Object value = tuple.getValue();
                        ids.add(Long.parseLong(value.toString()));
                        final byte[] key = (t2 + userId).getBytes();
                        try {
                            connection.zAdd(key, tuple.getScore(), objectMapper.writeValueAsBytes(value));
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        connection.expire(key, RedisConstant.HISTORY_TIME);
                    }
                }
            }
            return null;
        });
    }
}
