package cn.zhoutaolinmusic.service.user;


import cn.zhoutaolinmusic.entity.user.Role;
import cn.zhoutaolinmusic.entity.user.Tree;
import cn.zhoutaolinmusic.entity.vo.AssignRoleVO;
import cn.zhoutaolinmusic.entity.vo.AuthorityVO;
import cn.zhoutaolinmusic.utils.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface RoleService extends IService<Role> {

    List<Tree> tree();

    Result removeRole(String id);

    Result gavePermission(AuthorityVO authorityVO);

    Result gaveRole(AssignRoleVO assignRoleVO);

}
