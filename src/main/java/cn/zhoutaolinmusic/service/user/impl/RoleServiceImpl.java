package cn.zhoutaolinmusic.service.user.impl;

import cn.zhoutaolinmusic.authority.Authority;
import cn.zhoutaolinmusic.entity.user.Role;
import cn.zhoutaolinmusic.entity.user.RolePermission;
import cn.zhoutaolinmusic.entity.user.Tree;
import cn.zhoutaolinmusic.entity.user.UserRole;
import cn.zhoutaolinmusic.entity.vo.AssignRoleVO;
import cn.zhoutaolinmusic.entity.vo.AuthorityVO;
import cn.zhoutaolinmusic.mapper.user.RoleMapper;
import cn.zhoutaolinmusic.service.user.PermissionService;
import cn.zhoutaolinmusic.service.user.RolePermissionService;
import cn.zhoutaolinmusic.service.user.RoleService;
import cn.zhoutaolinmusic.service.user.UserRoleService;
import cn.zhoutaolinmusic.utils.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RolePermissionService rolePermissionService;

    @Override
    public List<Tree> tree() {
        // 获取权限列表的所有数据
        List<Tree> trees = permissionService.list(null).stream().map(permission -> {
            Tree tree = new Tree();
            BeanUtils.copyProperties(permission, tree);
            tree.setTitle(permission.getName());
            tree.setSpread(true);
            return tree;
        }).collect(Collectors.toList());

        // 找到根节点
        List<Tree> parent = trees.stream().filter(tree -> tree.getPId().compareTo(0L) == 0).collect(Collectors.toList());
        for (Tree item: parent) {
            // 生成树
            item.setChildren(new ArrayList<>());
            item.getChildren().add(findChildren(item, trees));
        }

        return parent;
    }

    /**
     * 递归查询子节点
     * @param datum 当前元素
     * @param trees 所有数据
     * @return
     */
    private Tree findChildren(Tree datum, List<Tree> trees) {
        datum.setChildren(new ArrayList<>());
        for (Tree tree: trees) {
            // 判断当前元素的id是否等于 tree 的pId （当前元素是否为他的父元素）
            if (tree.getPId().compareTo(datum.getId()) == 0) {
                // 说明tree是datum的子元素
                datum.getChildren().add(findChildren(tree, trees));
            }
        }
        return datum;
    }

    @Override
    public Result removeRole(String id) {
        return null;
    }

    @Override
    public Result gavePermission(AuthorityVO authorityVO) {
        try{
            rolePermissionService.remove(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId,authorityVO.getRid()));
            List<RolePermission> list = new ArrayList<>();
            Integer rid = authorityVO.getRid();
            for (Integer pId : authorityVO.getPid()) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(rid);
                rolePermission.setPermissionId(pId);
                list.add(rolePermission);
            }
            rolePermissionService.saveBatch(list);
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.error("分配权限失败");
        }
        return Result.ok("分配权限成功");
    }

    @Override
    @Transactional
    public Result gaveRole(AssignRoleVO assignRoleVO) {
        try {
            // 获取用户信息
            Long uId = assignRoleVO.getUId();
            // 先删除所对应用户的所有角色
            userRoleService.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, uId));
            List<UserRole> userRoles = new ArrayList<>();
            for (Long id: assignRoleVO.getRId()) {
                UserRole userRole = new UserRole();
                userRole.setRoleId(id);
                userRole.setUserId(uId);
                userRoles.add(userRole);
            }
            userRoleService.saveBatch(userRoles);
        } catch (Exception e) {
            // 设置事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.error("分配权限失败");
        }

        return Result.ok("分配权限成功");
    }
}
