package cn.zhoutaolinmusic.entity.video;

import cn.zhoutaolinmusic.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class VideoStar extends BaseEntity {

    private static final long serialVersionUID = 1L;


    private Long videoId;

    private Long userId;


}
