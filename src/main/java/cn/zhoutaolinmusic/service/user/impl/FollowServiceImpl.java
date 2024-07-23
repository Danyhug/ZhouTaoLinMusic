package cn.zhoutaolinmusic.service.user.impl;

import cn.zhoutaolinmusic.entity.user.Follow;
import cn.zhoutaolinmusic.entity.vo.BasePage;
import cn.zhoutaolinmusic.mapper.FollowMapper;
import cn.zhoutaolinmusic.service.user.FollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService
{
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
        return null;
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
