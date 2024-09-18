package cn.zhoutaolinmusic.service.user.impl;

import cn.zhoutaolinmusic.constant.RedisConstant;
import cn.zhoutaolinmusic.entity.user.Follow;
import cn.zhoutaolinmusic.entity.vo.BasePage;
import cn.zhoutaolinmusic.exception.BaseException;
import cn.zhoutaolinmusic.mapper.FollowMapper;
import cn.zhoutaolinmusic.service.FeedService;
import cn.zhoutaolinmusic.service.user.FollowService;
import cn.zhoutaolinmusic.service.video.VideoService;
import cn.zhoutaolinmusic.utils.RedisCacheUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService
{
    private final RedisCacheUtil redisCacheUtil;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    @Lazy
    private VideoService videoService;

    @Autowired
    private FeedService feedService;

    public FollowServiceImpl(RedisCacheUtil redisCacheUtil) {
        this.redisCacheUtil = redisCacheUtil;
    }

    @Override
    public int getFollowCount(Long userId) {
        return count(new LambdaQueryWrapper<Follow>().eq(Follow::getUserId, userId));
    }

    @Override
    public int getFansCount(Long userId) {
        return count(new LambdaQueryWrapper<Follow>().eq(Follow::getFollowId, userId));
    }

    @Override
    public Collection<Long> getFollow(Long userId, BasePage basePage) {
        // 获取关注的人并且按照关注时间排序
        if (basePage == null) {
            Set<Object> set = redisCacheUtil.getSortList(RedisConstant.USER_FOLLOW + userId);
            if (ObjectUtils.isEmpty(set)) return Collections.emptyList();

            // 转换为Long列表
            return set.stream().map(o ->
                Long.valueOf(o.toString())
            ).collect(Collectors.toList());
        }

        Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisCacheUtil.getSortListByPage(RedisConstant.USER_FOLLOW + userId, basePage.getPage(), basePage.getLimit());
        if (ObjectUtils.isEmpty(typedTuples)) return Collections.emptyList();

        return typedTuples.stream().map(o -> Long.valueOf(o.getValue().toString())).collect(Collectors.toList());
    }

    @Override
    public Collection<Long> getFans(Long userId, BasePage basePage) {
        if (basePage == null) {
            final Set<Object> set = redisCacheUtil.getSortList(RedisConstant.USER_FANS + userId);
            if(ObjectUtils.isEmpty(set)){
                return Collections.EMPTY_SET;
            }
            return set.stream().map(o->Long.valueOf(o.toString())).collect(Collectors.toList());
        }
        final Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisCacheUtil.getSortListByPage(RedisConstant.USER_FANS + userId, basePage.getPage(), basePage.getLimit());
        if (ObjectUtils.isEmpty(typedTuples)) {
            final List<Follow> follows = page(basePage.page(),new LambdaQueryWrapper<Follow>().eq(Follow::getFollowId, userId)).getRecords();
            if (ObjectUtils.isEmpty(follows)){
                return Collections.EMPTY_LIST;
            }
            return follows.stream().map(Follow::getUserId).collect(Collectors.toList());
        }
        return typedTuples.stream().map(t -> Long.parseLong(t.getValue().toString())).collect(Collectors.toList());
    }

    @Override
    public Boolean follows(Long followsId, Long userId) {

        if (followsId.equals(userId)) {
            throw new BaseException("你不能关注自己");
        }

        // 直接保存(唯一索引),保存失败则删除
        final Follow follow = new Follow();
        follow.setFollowId(followsId);
        follow.setUserId(userId);
        try {
            save(follow);
            final Date date = new Date();
            // 自己关注列表添加
            redisTemplate.opsForZSet().add(RedisConstant.USER_FOLLOW + userId, followsId, date.getTime());
            // 对方粉丝列表添加
            redisTemplate.opsForZSet().add(RedisConstant.USER_FANS + followsId, userId, date.getTime());
        } catch (Exception e) {
            // 删除
            remove(new LambdaQueryWrapper<Follow>().eq(Follow::getFollowId, followsId).eq(Follow::getUserId, userId));
            // 删除收件箱的视频
            // 获取关注人的视频
            final List<Long> videoIds = (List<Long>) videoService.listVideoIdByUserId(followsId);
            feedService.deleteInBoxFeed(userId, videoIds);
            // 自己关注列表删除
            redisTemplate.opsForZSet().remove(RedisConstant.USER_FOLLOW + userId, followsId);
            // 对方粉丝列表删除
            redisTemplate.opsForZSet().remove(RedisConstant.USER_FANS + followsId, userId);
            return false;
        }

        return true;
    }

    @Override
    public Boolean isFollows(Long followId, Long userId) {
        return null;
    }
}
