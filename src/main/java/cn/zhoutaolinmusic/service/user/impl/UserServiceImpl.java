package cn.zhoutaolinmusic.service.user.impl;

import cn.zhoutaolinmusic.constant.RedisConstant;
import cn.zhoutaolinmusic.entity.user.Favorites;
import cn.zhoutaolinmusic.entity.user.User;
import cn.zhoutaolinmusic.entity.vo.FindPWVO;
import cn.zhoutaolinmusic.entity.vo.RegisterVO;
import cn.zhoutaolinmusic.entity.vo.UserVO;
import cn.zhoutaolinmusic.exception.BaseException;
import cn.zhoutaolinmusic.mapper.user.UserMapper;
import cn.zhoutaolinmusic.service.FileService;
import cn.zhoutaolinmusic.service.InterestPushService;
import cn.zhoutaolinmusic.service.user.FavoritesService;
import cn.zhoutaolinmusic.service.user.FollowService;
import cn.zhoutaolinmusic.service.user.UserService;
import cn.zhoutaolinmusic.service.video.TypeService;
import cn.zhoutaolinmusic.utils.RedisCacheUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Log4j2
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private TypeService typeService;

    // @Autowired
    // private UserSubscribeService userSubscribeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private FileService fileService;

    @Autowired
    private InterestPushService interestPushService;

    @Autowired
    private FavoritesService favoritesService;
    //
    // @Autowired
    // private TextAuditService textAuditService;

    // @Autowired
    // private ImageAuditService imageAuditService;

    @Override
    public boolean register(RegisterVO registerVO) throws Exception {
        // 查看邮箱是否存在
        int count = count(new LambdaQueryWrapper<User>().eq(User::getEmail, registerVO.getEmail()));
        if (count == 1) {
            throw new BaseException("邮箱已被注册");
        }

        // 验证码校验
        String code = registerVO.getCode();
        Object o = redisCacheUtil.get(RedisConstant.EMAIL_CODE + registerVO.getEmail());
        if (o == null) {
            throw new BaseException("验证码为空");
        }

        if (!code.equals(o)) {
            return false;
        }

        // 此时验证码正确
        User user = new User();
        user.setEmail(registerVO.getEmail());
        user.setNickName(registerVO.getNickName());
        user.setPassword(registerVO.getPassword());
        user.setDescription("懒得写");
        // 将用户信息保存到数据库中
        this.save(user);

        // 创建默认收藏夹
        Favorites favorites = new Favorites();
        favorites.setName("默认收藏夹");
        favorites.setUserId(user.getId());
        favoritesService.save(favorites);

        // 将收藏夹和用户关联
        user.setDefaultFavoritesId(favorites.getId());
        this.updateById(user);

        return true;
    }

    @Override
    public UserVO getInfo(Long userId) {
        User user = this.getById(userId);
        if (ObjectUtils.isEmpty(user)) {
            return new UserVO();
        }

        UserVO uservo = new UserVO();
        log.info("获取id为 {} 的个人信息 {}", userId, user);

        BeanUtils.copyProperties(user, uservo);

        // 查询关注量
        long followCount = followService.getFollowCount(userId);

        // 查粉丝量
        long fansCount = followService.getFansCount(userId);
        uservo.setFollow(followCount);
        uservo.setFans(fansCount);

        return uservo;
    }

    @Override
    public List<User> list(Collection<Long> userIds) {
        return null;
    }

    @Override
    public void subscribe(Set<Long> typeIds) {

    }

    @Override
    public boolean follows(Long followsUserId) {
        return false;
    }

    @Override
    public Boolean findPassword(FindPWVO findPWVO) {
        // 判断验证码是否正确
        Object o = redisCacheUtil.get(RedisConstant.EMAIL_CODE + findPWVO.getEmail());
        if (ObjectUtils.isEmpty(o)) {
            return false;
        }

        // 验证码不一致
        if (findPWVO.getCode() != Integer.parseInt(o.toString())) {
            return false;
        }

        // 更新用户密码
        User user = new User();
        user.setEmail(findPWVO.getEmail());
        user.setPassword(findPWVO.getNewPassword());
        this.update(user, new UpdateWrapper<User>().lambda().set(User::getPassword, user.getPassword()).eq(User::getEmail, user.getEmail()));
        return true;
    }

    // @Override
    // public void updateUser(UpdateUserVO user) {
    //
    // }

    @Override
    public Collection<String> searchHistory(Long userId) {
        return null;
    }

    @Override
    public void addSearchHistory(Long userId, String search) {

    }

    @Override
    public void deleteSearchHistory(Long userId) {

    }
}
