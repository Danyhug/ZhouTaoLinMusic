package cn.zhoutaolinmusic.service.user.impl;

import cn.zhoutaolinmusic.entity.user.UserRole;
import cn.zhoutaolinmusic.mapper.user.UserRoleMapper;
import cn.zhoutaolinmusic.service.user.UserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

}