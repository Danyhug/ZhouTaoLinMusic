package cn.zhoutaolinmusic.entity.user;

import lombok.Data;

/**
 * @description: 用户订阅表
 * @Author: Xhy
 * @CreateTime: 2023-11-01 14:59
 */
@Data
public class UserSubscribe {
    private Long id;
    private Long typeId;
    private Long userId;
}
