package cn.zhoutaolinmusic.service.user.impl;

import cn.zhoutaolinmusic.constant.RedisConstant;
import cn.zhoutaolinmusic.entity.user.Favorites;
import cn.zhoutaolinmusic.entity.user.User;
import cn.zhoutaolinmusic.entity.user.UserSubscribe;
import cn.zhoutaolinmusic.entity.video.Video;
import cn.zhoutaolinmusic.entity.video.VideoType;
import cn.zhoutaolinmusic.entity.vo.FindPWVO;
import cn.zhoutaolinmusic.entity.vo.ModelVO;
import cn.zhoutaolinmusic.entity.vo.RegisterVO;
import cn.zhoutaolinmusic.entity.vo.UserVO;
import cn.zhoutaolinmusic.exception.BaseException;
import cn.zhoutaolinmusic.mapper.user.UserMapper;
import cn.zhoutaolinmusic.service.FileService;
import cn.zhoutaolinmusic.service.InterestPushService;
import cn.zhoutaolinmusic.service.user.FavoritesService;
import cn.zhoutaolinmusic.service.user.FollowService;
import cn.zhoutaolinmusic.service.user.UserService;
import cn.zhoutaolinmusic.service.user.UserSubscribeService;
import cn.zhoutaolinmusic.service.video.TypeService;
import cn.zhoutaolinmusic.utils.RedisCacheUtil;
import cn.zhoutaolinmusic.utils.UserHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

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
    private UserSubscribeService userSubscribeService;

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
        return this.list(new LambdaQueryWrapper<User>().in(User::getId, userIds)
                .select(User::getId, User::getNickName, User::getSex, User::getAvatar, User::getDescription));
    }

    @Override
    public void subscribe(Set<Long> typeIds) {
        if (ObjectUtils.isEmpty(typeIds)) return;

        // 校验分类
        Collection<VideoType> types = typeService.listByIds(typeIds);
        if (types.size() != typeIds.size()) {
            throw new BaseException("分类不存在");
        }

        Long userId = UserHolder.get();
        ArrayList<UserSubscribe> userSubscribe = new ArrayList<>();
        for (Long typeId : typeIds) {
            UserSubscribe temp = new UserSubscribe();
            temp.setTypeId(typeId);
            temp.setUserId(userId);
            userSubscribe.add(temp);
        }
        // 删除订阅的所有标签
        userSubscribeService.remove(new LambdaQueryWrapper<UserSubscribe>().eq(UserSubscribe::getUserId, userId));
        userSubscribeService.saveBatch(userSubscribe);

        // 初始化模型
        ModelVO modelVO = new ModelVO();
        modelVO.setUserId(UserHolder.get());
        // 获取分类下的标签
        List<String> labels = new ArrayList<>();
        for (VideoType type: types) {
            labels.addAll(type.buildLabel());
        }
        modelVO.setLabels(labels);
        initModel(modelVO);
    }

    /**
     * 初始化模型
     * @param modelVO
     */
    public void initModel(ModelVO modelVO) {
        // 初始化模型
        interestPushService.initUserModel(modelVO.getUserId(), modelVO.getLabels());
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
        String key = RedisConstant.USER_SEARCH_HISTORY + userId;
        List<String> search = new ArrayList<>();
        if (userId == null) return search;

        // 缓存中有数据
        search.addAll(redisCacheUtil.getSortList(key).stream().map(Object::toString).collect(Collectors.toList()));

        return search;
    }

    @Override
    public void addSearchHistory(Long userId, String search) {

    }

    @Override
    public void deleteSearchHistory(Long userId) {

    }

    @Override
    public Collection<VideoType> listSubscribeType(Long userId) {
        if (userId == null) return Collections.emptyList();

        // 查询用户订阅的所有分类id
        List<Long> typeIds = userSubscribeService.list(new LambdaQueryWrapper<UserSubscribe>().eq(UserSubscribe::getUserId, userId))
                .stream().map(UserSubscribe::getTypeId).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(typeIds)) return Collections.emptyList();

        // 根据分类id查询所有分类信息
        List<VideoType> types = typeService.list(new LambdaQueryWrapper<VideoType>()
                .in(VideoType::getId, typeIds).select(VideoType::getId, VideoType::getName, VideoType::getIcon));
        return types;
    }

    @Override
    public Collection<VideoType> listNoSubscribeType(Long uid) {
        if (uid == null) return Collections.emptyList();

        // 获取所有用户订阅分类
        List<Long> typeIds = userSubscribeService.list(new LambdaQueryWrapper<UserSubscribe>()).stream().map(UserSubscribe::getTypeId).collect(Collectors.toList());
        // 获取所有分类
        List<VideoType> types = typeService.list(null);

        ArrayList<VideoType> result = new ArrayList<>();
        for (VideoType type : types) {
            // 获取用户未订阅的分类
            if (!typeIds.contains(type.getId())) {
                result.add(type);
            }
        }
        return result;
    }
}
