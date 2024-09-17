package cn.zhoutaolinmusic.controller;

import cn.zhoutaolinmusic.config.QiNiuConfig;
import cn.zhoutaolinmusic.entity.user.Favorites;
import cn.zhoutaolinmusic.entity.vo.BasePage;
import cn.zhoutaolinmusic.entity.vo.Model;
import cn.zhoutaolinmusic.entity.vo.UpdateUserVO;
import cn.zhoutaolinmusic.entity.vo.UserModel;
import cn.zhoutaolinmusic.service.user.FavoritesService;
import cn.zhoutaolinmusic.service.user.UserService;
import cn.zhoutaolinmusic.utils.Result;
import cn.zhoutaolinmusic.utils.UserHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashSet;

@RestController
@RequestMapping("/jjjmusic/customer")
public class CustomerController {

    @Autowired
    private QiNiuConfig qiNiuConfig;

    @Autowired
    private UserService userService;

    @Autowired
    private FavoritesService favoritesService;

    /**
     * 获取用户信息
     * @param uid
     * @return
     */
    @GetMapping("/getInfo/{uid}")
    public Result getInfo(@PathVariable Long uid) {
        return Result.ok(userService.getInfo(uid));
    }

    /**
     * 获取个人信息
     * @return
     */
    @GetMapping("/getInfo")
    public Result getDefaultInfo() {
        return Result.ok(userService.getInfo(UserHolder.get()));
    }

    /**
     * 获取所有收藏夹
     * @return
     */
    @GetMapping("/favorites")
    public Result listFavorites() {
        return Result.ok(favoritesService.listByUserId(UserHolder.get()));
    }

    /**
     * 添加收藏夹 / 修改
     * @param favorites
     * @return
     */
    @PostMapping("/favorites")
    public Result addFavorites(@RequestBody @Validated Favorites favorites) {
        Long userId = UserHolder.get();
        Long id = favorites.getId();
        favorites.setUserId(userId);

        int count = favoritesService.count(new LambdaQueryWrapper<Favorites>()
                .eq(Favorites::getUserId, userId)
                .eq(Favorites::getName, favorites.getName())
                .eq(Favorites::getId, id)
                .eq(Favorites::getDescription, favorites.getDescription()));

        if (count != 0) return Result.error("已存在相同的收藏夹");

        favoritesService.saveOrUpdate(favorites);
        return Result.ok(id == null ? "添加成功" : "修改成功");
    }

    /**
     * 订阅分类
     * @param types
     * @return
     */
    @PostMapping("/subscribe")
    public Result subscribe(@RequestParam(required = false) String types) {
        HashSet<Long> typeSet = new HashSet<>();
        String msg = "取消订阅";

        if (!ObjectUtils.isEmpty(types)) {
            for (String s: types.split(",")) {
                typeSet.add(Long.parseLong(s));
            }
            msg = "订阅成功";
        }
        userService.subscribe(typeSet);
        return Result.ok(msg);
    }

    /**
     * 获取用户订阅的分类
     * @return
     */
    @GetMapping("/subscribe")
    public Result listSubscribeType() {
        return Result.ok(userService.listSubscribeType(UserHolder.get()));
    }

    /**
     * 获取用户未订阅的分类
     * @return
     */
    @GetMapping("/noSubscribe")
    public Result listNoSubscribeType() {
        return Result.ok(userService.listNoSubscribeType(UserHolder.get()));
    }

    /**
     * 获取粉丝
     * @param basePage
     * @param userId
     * @return
     */
    @GetMapping("/fans")
    public Result getFans(BasePage basePage, Long userId) {
        return Result.ok(userService.getFans(userId, basePage));
    }


    /**
     * 获取关注
     * @param basePage
     * @param userId
     * @return
     */
    @GetMapping("/follows")
    public Result getFollows(BasePage basePage,Long userId){
        return Result.ok(userService.getFollows(userId,basePage));
    }

    /**
     * 关注/取关
     * @param followsUserId
     * @return
     */
    @PostMapping("/follows")
    public Result follows(@RequestParam Long followsUserId){

        return Result.ok(userService.follows(followsUserId) ? "已关注" : "已取关");
    }


    /**
     * 修改用户信息
     * @param user
     * @return
     */
    @PutMapping("")
    public Result updateUser(@RequestBody @Validated UpdateUserVO user){
        userService.updateUser(user);
        return Result.ok("修改成功");
    }


    /**
     * 用户停留时长修改模型
     * @param model
     * @return
     */
    @PostMapping("/updateUserModel")
    public Result updateUserModel(@RequestBody Model model){
        final Double score = model.getScore();
        if (score == -0.5 || score == 1.0){
            final UserModel userModel = new UserModel();
            userModel.setUserId(UserHolder.get());
            userModel.setModels(Collections.singletonList(model));
            userService.updateUserModel(userModel);
        }
        return Result.ok();
    }
}
