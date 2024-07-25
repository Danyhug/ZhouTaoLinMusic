package cn.zhoutaolinmusic.service.user.impl;

import cn.zhoutaolinmusic.constant.RedisConstant;
import cn.zhoutaolinmusic.entity.user.Follow;
import cn.zhoutaolinmusic.entity.vo.BasePage;
import cn.zhoutaolinmusic.mapper.FollowMapper;
import cn.zhoutaolinmusic.service.user.FollowService;
import cn.zhoutaolinmusic.utils.RedisCacheUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService
{
    private final RedisCacheUtil redisCacheUtil;

    public FollowServiceImpl(RedisCacheUtil redisCacheUtil) {
        this.redisCacheUtil = redisCacheUtil;
    }

    @Override
    public int getFollowCount(Long userId) {
        return 0;
    }

    @Override
    public int getFansCount(Long userId) {
        return 0;
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
        return null;
    }

    @Override
    public Boolean follows(Long followId, Long userId) {
        return null;
    }

    @Override
    public Boolean isFollows(Long followId, Long userId) {
        return null;
    }
}
