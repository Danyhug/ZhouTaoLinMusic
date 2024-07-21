package cn.zhoutaolinmusic.service.impl;

import cn.zhoutaolinmusic.constant.RedisConstant;
import cn.zhoutaolinmusic.entity.Captcha;
import cn.zhoutaolinmusic.entity.user.User;
import cn.zhoutaolinmusic.entity.vo.FindPWVO;
import cn.zhoutaolinmusic.entity.vo.RegisterVO;
import cn.zhoutaolinmusic.exception.BaseException;
import cn.zhoutaolinmusic.service.CaptchaService;
import cn.zhoutaolinmusic.service.LoginService;
import cn.zhoutaolinmusic.service.user.UserService;
import cn.zhoutaolinmusic.utils.RedisCacheUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserService userService;

    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Override
    public User login(User user) throws BaseException {
        // 基于邮箱查询账号数据
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        User result = userService.getOne(wrapper.eq(User::getEmail, user.getEmail()));
        if (ObjectUtils.isEmpty(result)) {
            throw new BaseException("用户不存在");
        }

        if (!result.getPassword().equals(user.getPassword())) {
            throw new BaseException("密码错误");
        }

        return result;
    }

    @Override
    public Boolean checkCode(String email, Integer code) {
        if (ObjectUtils.isEmpty(code) || ObjectUtils.isEmpty(email)) {
            throw new BaseException("参数不能为空");
        }

        Object o = redisCacheUtil.get(RedisConstant.EMAIL_CODE + email);
        if (ObjectUtils.isEmpty(o)) {
            return false;
        }

        return code.toString().equals(o);
    }

    @Override
    public void captcha(String uuid, HttpServletResponse response) throws IOException {
        // 检查uuid是否为空，如果为空则抛出异常
        if (ObjectUtils.isEmpty(uuid)) throw new IllegalArgumentException("uuid不能为空");

        // 设置响应头，确保验证码图像不会被缓存
        // 设置返回头
        response.setHeader("Cache-Control", "no-store, no-cache");
        // 设置响应内容类型为JPEG图像
        response.setContentType("image/jpeg");

        // 从验证码服务中获取指定uuid的验证码图像
        BufferedImage image = this.captchaService.getCaptcha(uuid);
        // 获取响应输出流，用于写入验证码图像数据
        ServletOutputStream out = response.getOutputStream();
        // 将验证码图像以JPEG格式写入输出流
        ImageIO.write(image, "jpg", out);
        // 关闭输出流
        IOUtils.closeQuietly(out);
    }

    @Override
    public Boolean getCode(Captcha captcha) throws Exception {
        return captchaService.validate(captcha);
    }

    @Override
    public Boolean register(RegisterVO registerVO) throws Exception {
        if (userService.register(registerVO)) {
            // 注册成功，删除图形验证码
            captchaService.removeById(registerVO.getUuid());
            return true;
        }
        return false;
    }

    @Override
    public Boolean findPassword(FindPWVO findPWVO) {
        return userService.findPassword(findPWVO);
    }
}
