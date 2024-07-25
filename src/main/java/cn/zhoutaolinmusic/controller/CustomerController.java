package cn.zhoutaolinmusic.controller;

import cn.zhoutaolinmusic.config.QiNiuConfig;
import cn.zhoutaolinmusic.service.user.FavoritesService;
import cn.zhoutaolinmusic.service.user.UserService;
import cn.zhoutaolinmusic.utils.Result;
import cn.zhoutaolinmusic.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
