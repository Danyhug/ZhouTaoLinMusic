package cn.zhoutaolinmusic.controller.admin;

import cn.zhoutaolinmusic.authority.Authority;
import cn.zhoutaolinmusic.entity.user.Role;
import cn.zhoutaolinmusic.entity.user.RolePermission;
import cn.zhoutaolinmusic.entity.user.Tree;
import cn.zhoutaolinmusic.entity.user.UserRole;
import cn.zhoutaolinmusic.entity.vo.AssignRoleVO;
import cn.zhoutaolinmusic.entity.vo.AuthorityVO;
import cn.zhoutaolinmusic.entity.vo.BasePage;
import cn.zhoutaolinmusic.service.user.RolePermissionService;
import cn.zhoutaolinmusic.service.user.RoleService;
import cn.zhoutaolinmusic.service.user.UserRoleService;
import cn.zhoutaolinmusic.utils.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/authorize/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RolePermissionService rolePermissionService;

    @Autowired
    private UserRoleService userRoleService;

    /**
     * 获取用户角色
     * @param userId
     * @return
     */
    @GetMapping("/getUserRole/{userId}")
    @Authority("role:getRole")
    public List getRole(@PathVariable Integer userId){
        List<Long> list = userRoleService.list(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId,userId).select(UserRole::getRoleId))
                .stream().map(UserRole::getRoleId).collect(Collectors.toList());
        return list;
    }

    /**
     * 初始化角色
     * @return
     */
    @GetMapping("/initRole")
    @Authority("role:initRole")
    public List<Map<String, Object>> initRole(){
        // 查出所有角色
        List<Map<String, Object>> list = roleService.list(null).stream()
                .map(role -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("value", role.getId());
                    data.put("title", role.getName());
                    return data;
                }).collect(Collectors.toList());

        return list;
    }

    /**
     * 分配角色
     * @param assignRoleVO
     * @return
     */
    @PostMapping("/assignRole")
    @Authority("role:assignRole")
    public Result assignRole(@RequestBody AssignRoleVO assignRoleVO) {
        return roleService.gaveRole(assignRoleVO);
    }

    /**
     * 获取角色树
     * @return
     */
    @GetMapping("/treeList")
    @Authority("permission:treeList")
    public List<Tree> treeList(){
        List<Tree> data = roleService.tree();
        return data;
    }

    /**
     * 获取角色列表
     * @param basePage
     * @param name
     * @return
     */
    @GetMapping("/list")
    @Authority("role:list")
    public Result list(BasePage basePage, @RequestParam(required = false) String name) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(!ObjectUtils.isEmpty(name), Role::getName, name);

        IPage iPage = basePage.page();
        IPage page = roleService.page(iPage, wrapper);
        return Result.ok(page.getRecords(), page.getTotal());
    }


    /**
     * 获取角色权限
     * @param id
     * @return
     */
    @GetMapping("/getPermission/{id}")
    @Authority("role:getPermission")
    public Integer[] getPermission(@PathVariable Integer id){
        Integer[] list = rolePermissionService.list(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId,id).select(RolePermission::getPermissionId))
                .stream().map(RolePermission::getPermissionId).toArray(Integer[]::new);
        return list;
    }

    /**
     * 给角色分配权限
     * @param authorityVO
     * @return
     * 给角色分配权限前先把该角色的权限都删了
     */
    @PostMapping("/authority")
    @Authority("role:authority")
    public Result authority(@RequestBody AuthorityVO authorityVO){
        return roleService.gavePermission(authorityVO);
    }
}
