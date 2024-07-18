package cn.zhoutaolinmusic.entity.response;

import cn.zhoutaolinmusic.entity.user.User;
import lombok.Data;

@Data
public class UserLoginRes {
    private String token;
    private String name;
    private User user;
}
