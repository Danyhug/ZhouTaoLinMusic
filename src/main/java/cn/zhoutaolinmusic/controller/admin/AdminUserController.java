package cn.zhoutaolinmusic.controller.admin;

import cn.zhoutaolinmusic.authority.Authority;
import cn.zhoutaolinmusic.entity.user.Role;
import cn.zhoutaolinmusic.entity.user.User;
import cn.zhoutaolinmusic.entity.user.UserRole;
import cn.zhoutaolinmusic.entity.vo.BasePage;
import cn.zhoutaolinmusic.service.user.RoleService;
import cn.zhoutaolinmusic.service.user.UserRoleService;
import cn.zhoutaolinmusic.service.user.UserService;
import cn.zhoutaolinmusic.utils.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/user")
public class AdminUserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RoleService roleService;

    /**
     * 获取用户列表
     * @return
     */
    @GetMapping("/list")
    @Authority("admin:user:list")
    public Result list() {
        return Result.ok(userService.list(new QueryWrapper<>()));
    }

    /**
     * 分页获取用户列表
     * @param basePage
     * @param name
     * @return
     */
    @GetMapping("/page")
    @Authority("admin:user:page")
    public Result list(BasePage basePage, @RequestParam(required = false) String name) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name != null, User::getEmail, name);
        IPage<User> page = userService.page(basePage.page(), wrapper);

        // 查出用户角色中间表 用户ID，用户ID对应的权限ID
        Map<Long, List<UserRole>> userRoleMap =  userRoleService.list(null).stream().collect(Collectors.groupingBy(UserRole::getUserId));
        // 根据角色查出角色表信息
        Map<Long, String> roleMap = roleService.list(null).stream().collect(Collectors.toMap(Role::getId, Role::getName));

        Map<Long, Set<String>> map = new HashMap<>();
        userRoleMap.forEach((uid, rIds) -> {
            Set<String> roles = new HashSet<>();
            for (UserRole rId: rIds) {
                roles.add(roleMap.get(rId.getRoleId()));
            }
            map.put(uid, roles);
        });

        for (User user: page.getRecords()) {
            user.setRoleName(map.get(user.getId()));
        }
        return Result.ok(page.getRecords(), page.getTotal());
    }
}
