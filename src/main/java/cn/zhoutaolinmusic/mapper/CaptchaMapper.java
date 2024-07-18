package cn.zhoutaolinmusic.mapper;

import cn.zhoutaolinmusic.entity.Captcha;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 系统验证码 Mapper 接口
 * </p>
 *
 * @author xhy
 * @since 2023-10-25
 */
@Mapper
public interface CaptchaMapper extends BaseMapper<Captcha> {

}
