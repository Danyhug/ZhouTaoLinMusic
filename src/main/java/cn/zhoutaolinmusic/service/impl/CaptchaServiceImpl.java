package cn.zhoutaolinmusic.service.impl;

import cn.zhoutaolinmusic.constant.RedisConstant;
import cn.zhoutaolinmusic.entity.Captcha;
import cn.zhoutaolinmusic.mapper.CaptchaMapper;
import cn.zhoutaolinmusic.service.CaptchaService;
import cn.zhoutaolinmusic.service.EmailService;
import cn.zhoutaolinmusic.utils.DateUtil;
import cn.zhoutaolinmusic.utils.RedisCacheUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.code.kaptcha.Producer;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.Date;

/**
 * <p>
 * 系统验证码 服务实现类
 * </p>
 *
 */
@Service
public class CaptchaServiceImpl extends ServiceImpl<CaptchaMapper, Captcha> implements CaptchaService {

    @Autowired
    private Producer producer;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private EmailService emailService;

    @Override
    public BufferedImage getCaptcha(String uuid) {
        // 生成验证码
        String code = this.producer.createText();
        Captcha captcha = new Captcha();
        captcha.setUuid(uuid);
        captcha.setCode(code);
        // 设置过期时间为 5 分钟
        captcha.setExpireTime(DateUtil.addDateMinute(new Date(), 5));

        // 存入数据库
        this.save(captcha);

        return this.producer.createImage(code);
    }

    @Override
    public boolean validate(Captcha captcha) throws Exception {
        // 将用户发来的数据存一份，后面要更改 验证码 的值
        Captcha sendCaptcha = new Captcha();
        BeanUtils.copyProperties(captcha, sendCaptcha);

        // 获取uuid对应的验证码
        captcha = this.getOne(new LambdaQueryWrapper<Captcha>().eq(Captcha::getUuid, captcha.getUuid()));
        if (captcha == null) {
            throw new Exception("验证码不存在或已过期");
        }

        // 判断验证码是否一致
        if (!captcha.getCode().equals(sendCaptcha.getCode())) {
            throw new Exception("验证码不正确");
        }

        // 判断验证码是否已过期
        if (captcha.getExpireTime().getTime() < System.currentTimeMillis()) {
            throw new Exception("验证码已过期");
        }

        // 获取六位验证码
        String code = getRandomCode(6);

        // 存储到 redis 中
        redisCacheUtil.set(RedisConstant.EMAIL_CODE + sendCaptcha.getEmail(), code, RedisConstant.EMAIL_CODE_TIME);
        emailService.send(sendCaptcha.getEmail(), "验证码", "您的验证码为：" + code + "，有效期 5 分钟，请勿泄露。");

        return true;
    }

    // 获取指定位数的随机字符
    public static String getRandomCode(int len) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i ++) {
            int code = (int)(Math.random() * 10);
            builder.append(code);
        }
        return builder.toString();
    }
}
