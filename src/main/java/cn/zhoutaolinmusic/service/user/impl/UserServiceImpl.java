package cn.zhoutaolinmusic.service.user.impl;

import cn.zhoutaolinmusic.constant.RedisConstant;
import cn.zhoutaolinmusic.entity.user.Favorites;
import cn.zhoutaolinmusic.entity.user.User;
import cn.zhoutaolinmusic.entity.vo.FindPWVO;
import cn.zhoutaolinmusic.entity.vo.RegisterVO;
import cn.zhoutaolinmusic.entity.vo.UserVO;
import cn.zhoutaolinmusic.exception.BaseException;
import cn.zhoutaolinmusic.mapper.user.UserMapper;
import cn.zhoutaolinmusic.service.user.FavoritesService;
import cn.zhoutaolinmusic.service.user.UserService;
import cn.zhoutaolinmusic.utils.RedisCacheUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private FavoritesService favoritesService;

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
        return null;
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
