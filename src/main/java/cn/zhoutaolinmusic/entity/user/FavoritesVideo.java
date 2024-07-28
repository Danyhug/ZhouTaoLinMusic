package cn.zhoutaolinmusic.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 *  管理收藏夹下的视频
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FavoritesVideo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long favoritesId;

    private Long videoId;

    // 冗余字段
    private Long userId;

}
