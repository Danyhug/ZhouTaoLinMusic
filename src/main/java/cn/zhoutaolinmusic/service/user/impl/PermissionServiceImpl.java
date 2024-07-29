package cn.zhoutaolinmusic.service.user.impl;

import cn.zhoutaolinmusic.authority.AuthorityUtils;
import cn.zhoutaolinmusic.entity.user.*;
import cn.zhoutaolinmusic.mapper.user.PermissionMapper;
import cn.zhoutaolinmusic.service.user.PermissionService;
import cn.zhoutaolinmusic.service.user.RolePermissionService;
import cn.zhoutaolinmusic.service.user.UserRoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Resource
    private UserRoleService userRoleService;

    @Resource
    private RolePermissionService rolePermissionService;

    /**
     * 自定义比较器
     */
    private static class PermissionComparator implements Comparator<Permission> {
        @Override
        public int compare(Permission o1, Permission o2) {
            return o1.getSort() - o2.getSort();
        }
    }

    @Override
    public Map<String, Object> initMenu(Long userId) {
        // 创建返回结果 Map，后台界面需要这个格式
        Map<String, Object> data = new HashMap<>();
        List<Menu> menus = new ArrayList<>();
        List<Menu> parentMenu = new ArrayList<>();

        // 封装权限集合
        Set<String> set = new HashSet<>();

        // 根据用户id查询对应角色id
        List<Long> rIds = userRoleService.list(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)
                .select(UserRole::getRoleId)).stream().map(UserRole::getRoleId).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(rIds)) return Collections.emptyMap();

        // 根据角色查询权限id
        List<Integer> pIds = rolePermissionService.list(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, rIds).select(RolePermission::getPermissionId)).stream()
                .map(RolePermission::getPermissionId).collect(Collectors.toList());

        // 根据权限id查出所有权限
        // 查出的所有权限 -》 转为对应的菜单对象
        this.list(new LambdaQueryWrapper<Permission>().in(Permission::getId, pIds)).stream().sorted(new PermissionComparator()).forEach(permission -> {
            Menu menu = new Menu();
            BeanUtils.copyProperties(permission, menu);
            menu.setTitle(permission.getName());
            menus.add(menu);
        });

        // list转树形结构
        // 1. 先找根节点
        for (Menu menu: menus) {
            // 校验是根节点且不为按钮的节点
            if (menu.getPId().compareTo(0L) == 0 && menu.getIsMenu() != 1) {
                // 添加占位子元素
                menu.setChild(new ArrayList<>());
                parentMenu.add(menu);
            }
        }

        // 2. 根据根节点找到子元素
        for (Menu menu: parentMenu) {
            menu.getChild().add(findChild(menu, menus, set));
        }

        // 保存用户权限
        AuthorityUtils.setAuthority(userId, set);

        MenuKey menuKey1 = new MenuKey();
        menuKey1.setTitle("首页");
        menuKey1.setImage("images/logo.jpg");
        menuKey1.setHref("page/welcome.html?t=1");

        MenuKey menuKey2 = new MenuKey();
        menuKey2.setTitle("周陶林乐");
        menuKey2.setImage("images/logo.jpg");
        menuKey2.setHref("/index.html");

        data.put("menuInfo", parentMenu);
        data.put("homeInfo", menuKey1);
        data.put("logoInfo", menuKey2);
        return data;
    }

    /**
     * 递归查找子元素
     * @param menu
     * @param menus
     * @param set
     * @return
     */
    private Menu findChild(Menu menu, List<Menu> menus, Set<String> set) {
        menu.setChild(new ArrayList<>());
        for (Menu m : menus) {
            if (!ObjectUtils.isEmpty(m.getPath())){
                set.add(m.getPath());
            }
            if (m.getIsMenu() != 1){
                if (menu.getId().compareTo(m.getPId()) == 0) {
                    // 递归调用该方法
                    menu.getChild().add(findChild(m, menus, set));
                }
            }
        }
        return menu;
    }

    @Override
    public List<Permission> treeSelect() {
        return null;
    }

    @Override
    public void removeMenu(Long id) {

    }
}
