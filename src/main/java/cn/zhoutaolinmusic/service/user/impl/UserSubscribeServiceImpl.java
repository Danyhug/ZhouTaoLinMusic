package cn.zhoutaolinmusic.service.user.impl;

import cn.zhoutaolinmusic.entity.user.UserSubscribe;
import cn.zhoutaolinmusic.mapper.user.UserSubscribeMapper;
import cn.zhoutaolinmusic.service.user.UserSubscribeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserSubscribeServiceImpl extends ServiceImpl<UserSubscribeMapper, UserSubscribe> implements UserSubscribeService {
}
