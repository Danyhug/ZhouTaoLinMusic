package cn.zhoutaolinmusic.service.user;

import cn.zhoutaolinmusic.entity.user.User;
import cn.zhoutaolinmusic.entity.video.VideoType;
import cn.zhoutaolinmusic.entity.vo.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface UserService extends IService<User> {

    /**
     * 注册
     * @param registerVO
     * @return
     * @throws Exception
     */
    boolean register(RegisterVO registerVO) throws Exception;

    /**
     * 获取用户信息:
     * 1.用户基本信息
     * 2.关注数量
     * 3.粉丝数量
     * @param userId 用户id
     * @return
     */
    UserVO getInfo(Long userId);



    /**
     * 获取关注
     * @param userId
     * @param basePage
     * @return
     */
    // Page<User> getFollows(Long userId, BasePage basePage);

    /**
     * 获取粉丝
     * @param userId
     * @param basePage
     * @return
     */
    // Page<User> getFans(Long userId, BasePage basePage);

    /**
     * 获取用户基本信息
     * @param userIds
     * @return
     */
    List<User> list(Collection<Long> userIds);

    /**
     * 订阅分类
     * @param typeIds
     */
    void subscribe(Set<Long> typeIds);

    /**
     * 获取订阅分类
     * @param userId
     * @return
     */
    // Collection<Type> listSubscribeType(Long userId);

    /**
     * 关注/取关
     * @param followsUserId
     * @return
     */
    boolean follows(Long followsUserId);

    /**
     * 修改用户模型
     * @param userModel
     */
    // void updateUserModel(UserModel userModel);

    /**
     * 找回密码
     * @param findPWVO
     * @return
     */
    Boolean findPassword(FindPWVO findPWVO);

    /**
     * 修改用户资料
     * @param user
     */
    // void updateUser(UpdateUserVO user);

    /**
     * 获取用户搜索记录
     * @return 搜索的值
     */
    Collection<String> searchHistory(Long userId);

    /**
     * 添加搜索记录
     * @param userId
     * @param search
     */
    void addSearchHistory(Long userId, String search);

    /**
     * 删除搜索记录
     * @param userId
     */
    void deleteSearchHistory(Long userId);

    /**
     * 获取订阅分类
     * @return
     */
    Collection<VideoType> listSubscribeType(Long uid);

    /**
     * 获取未订阅分类
     * @return
     */
    Collection<VideoType> listNoSubscribeType(Long uid);

    /**
     * 获取粉丝
     * @param userId
     * @param basePage
     * @return
     */
    Page<User> getFans(Long userId, BasePage basePage);

    /**
     * 获取关注
     *
     * @param userId
     * @param basePage
     * @return
     */
    Page<User> getFollows(Long userId, BasePage basePage);

    /**
     * 修改用户资料
     * @param user
     */
    void updateUser(UpdateUserVO user);
}
