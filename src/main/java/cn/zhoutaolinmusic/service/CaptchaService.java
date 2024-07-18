package cn.zhoutaolinmusic.service;

import cn.zhoutaolinmusic.entity.Captcha;
import com.baomidou.mybatisplus.extension.service.IService;

import java.awt.image.BufferedImage;

public interface CaptchaService extends IService<Captcha> {
    BufferedImage getCaptcha(String uuid);

    boolean validate(Captcha captcha) throws Exception;
}
