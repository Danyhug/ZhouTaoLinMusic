package cn.zhoutaolinmusic.controller.admin;


import cn.zhoutaolinmusic.authority.Authority;
import cn.zhoutaolinmusic.entity.user.Permission;
import cn.zhoutaolinmusic.service.user.PermissionService;
import cn.zhoutaolinmusic.utils.Result;
import cn.zhoutaolinmusic.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 *  前端控制器
 * 权限
 */
@RestController
@RequestMapping("/authorize/permission")
public class PermissionController {

    @Resource
    private PermissionService permissionService;

    /**
     * 权限列表
     * @return
     */
    @GetMapping("/list")
    @Authority("permission:list")
    public List<Permission> list(){
        return permissionService.list(null);
    }


    /**
     * 新增权限时树形结构
     * @return
     */
    @GetMapping("/treeSelect")
    @Authority("permission:treeSelect")
    public List<Permission> treeSelect(){
        List<Permission> data = permissionService.treeSelect();

        return data;
    }

    /**
     * 添加权限
     * @param permission
     * @return
     */
    @PostMapping
    @Authority("permission:add")
    public Result add(@RequestBody Permission permission){
        permission.setIcon("fa "+permission.getIcon());
        permissionService.save(permission);

        return Result.ok();
    }

    /**
     * 修改权限
     * @param permission
     * @return
     */
    @PutMapping
    @Authority("permission:update")
    public Result update(@RequestBody Permission permission){
        permission.setIcon("fa "+permission.getIcon());
        permissionService.updateById(permission);
        return Result.ok();

    }

    /**
     * 删除权限
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @Authority("permission:delete")
    public Result delete(@PathVariable Long id){
        permissionService.removeMenu(id);
        return Result.ok("删除成功");
    }


    /**
     * 初始化菜单
     * @return
     */
    @GetMapping("/initMenu")
    public Map<String, Object> initMenu(){
        return permissionService.initMenu(UserHolder.get());
    }
}

