package cn.zhoutaolinmusic.controller;

import cn.zhoutaolinmusic.entity.Captcha;
import cn.zhoutaolinmusic.entity.response.UserLoginRes;
import cn.zhoutaolinmusic.entity.user.User;
import cn.zhoutaolinmusic.entity.vo.RegisterVO;
import cn.zhoutaolinmusic.service.LoginService;
import cn.zhoutaolinmusic.utils.Result;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Result<UserLoginRes> login(@RequestBody User user) {
        user = this.loginService.login(user);
        log.info("用户登录成功 {}", user);
        return null;
    }

    /**
     * 注册
     * @param registerVO
     * @return
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterVO registerVO) throws Exception {
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
    public Result<String> getCode(@RequestBody Captcha captcha) throws Exception {
        if (loginService.getCode(captcha)) {
            return Result.ok("发送成功,请耐心等待");
        }
        return Result.error("验证码错误");
    }
}
