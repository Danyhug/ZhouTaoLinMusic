package cn.zhoutaolinmusic.service.user.impl;

import cn.zhoutaolinmusic.entity.user.RolePermission;
import cn.zhoutaolinmusic.mapper.user.RolePermissionMapper;
import cn.zhoutaolinmusic.service.user.RolePermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {

}
