package cn.zhoutaolinmusic.entity.video;

import cn.zhoutaolinmusic.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 *
 * </p>
 * 分类,隐藏视频标签
 * @author xhy
 * @since 2023-10-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("type")
public class VideoType extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "分类名称不可为空")
    private String name;

    private String description;

    private String icon;

    private Boolean open;

    private String labelNames;

    private Integer sort;

    @TableField(exist = false)
    private Boolean used;

    public List<String> buildLabel(){
        if (labelNames == null) return new ArrayList<>();
        return Arrays.asList(labelNames.split(","));
    }
}

