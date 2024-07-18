package cn.zhoutaolinmusic.controller;

import cn.zhoutaolinmusic.entity.Captcha;
import cn.zhoutaolinmusic.entity.response.UserLoginRes;
import cn.zhoutaolinmusic.entity.user.User;
import cn.zhoutaolinmusic.entity.vo.FindPWVO;
import cn.zhoutaolinmusic.entity.vo.RegisterVO;
import cn.zhoutaolinmusic.service.LoginService;
import cn.zhoutaolinmusic.utils.JwtUtils;
import cn.zhoutaolinmusic.utils.Result;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Log4j2
@RestController
@RequestMapping("/jjjmusic/login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("")
    public Result<UserLoginRes> login(@RequestBody @Validated User user) {
        user = this.loginService.login(user);
        log.info("用户登录成功 {}", user);

        UserLoginRes userLoginRes = new UserLoginRes();
        // 保存 token 信息
        String token = JwtUtils.getJwtToken(user.getId(), user.getNickName());
        userLoginRes.setToken(token);
        userLoginRes.setUser(user);
        userLoginRes.setName(user.getNickName());

        return Result.ok(userLoginRes);
    }

    /**
     * 注册
     * @param registerVO
     * @return
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody @Validated RegisterVO registerVO) throws Exception {
        if (!loginService.register(registerVO)) {
            return Result.error("注册失败，验证码有误");
        }
        return Result.ok("注册成功");
    }

    /**
     * 获取图形验证码（注册验证码）
     * @param response
     * @param uuId
     * @throws IOException
     */
    @GetMapping("/captcha.jpg/{uuId}")
    public void captcha(HttpServletResponse response, @PathVariable String uuId) throws IOException {
        loginService.captcha(uuId, response);
    }

    /**
     * 获取邮箱验证码
     * @param captcha
     * @return
     * @throws Exception
     */
    @PostMapping("/getCode")
    public Result<String> getCode(@RequestBody @Validated Captcha captcha) throws Exception {
        if (loginService.getCode(captcha)) {
            return Result.ok("发送成功,请耐心等待");
        }
        return Result.error("验证码错误");
    }

    @PostMapping("/check")
    public Result<String> check(String email, Integer code) {
        if (!loginService.checkCode(email, code)) {
            return Result.error("验证码错误");
        }

        loginService.checkCode(email, code);
        return Result.ok("验证成功");
    }

    @PostMapping("/findPassword")
    public Result<String> findPassword(@RequestBody @Validated FindPWVO findPWVO) {
        if (!loginService.findPassword(findPWVO)) {
            return Result.error("修改失败，验证码错误");
        }
        return Result.ok("修改成功");
    }
}
