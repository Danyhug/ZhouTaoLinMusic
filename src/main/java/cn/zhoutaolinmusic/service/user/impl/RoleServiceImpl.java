package cn.zhoutaolinmusic.service.user.impl;

import cn.zhoutaolinmusic.entity.user.Role;
import cn.zhoutaolinmusic.entity.user.Tree;
import cn.zhoutaolinmusic.entity.vo.AssignRoleVO;
import cn.zhoutaolinmusic.entity.vo.AuthorityVO;
import cn.zhoutaolinmusic.mapper.user.RoleMapper;
import cn.zhoutaolinmusic.service.user.RoleService;
import cn.zhoutaolinmusic.utils.Result;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
    @Override
    public List<Tree> tree() {
        return null;
    }

    @Override
    public Result removeRole(String id) {
        return null;
    }

    @Override
    public Result gavePermission(AuthorityVO authorityVO) {
        return null;
    }

    @Override
    public Result gaveRole(AssignRoleVO assignRoleVO) {
        return null;
    }
}
