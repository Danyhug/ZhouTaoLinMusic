package cn.zhoutaolinmusic.entity.vo;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @description: 找回密码
 * @Author: Xhy
 * @CreateTime: 2023-11-03 14:37
 */
@Data
public class FindPWVO {

    @Email(message = "邮箱格式不正确")
    String email;

    @NotNull(message = "code不能为空")
    Integer code;

    @NotBlank(message = "新密码不能为空")
    String newPassword;
}
