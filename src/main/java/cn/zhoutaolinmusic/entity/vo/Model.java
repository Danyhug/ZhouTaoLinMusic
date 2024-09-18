package cn.zhoutaolinmusic.entity.vo;

import lombok.Data;

@Data
public class Model {
    private String labels;
    private Long videoId;
    /**
     * 暴漏的接口只有根据停留时长 {@link cn.zhoutaolinmusic.controller.CustomerController#updateUserModel}
     */

    private Double score;
}
