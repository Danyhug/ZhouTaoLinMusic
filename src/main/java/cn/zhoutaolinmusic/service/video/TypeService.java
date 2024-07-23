package cn.zhoutaolinmusic.service.video;

import cn.zhoutaolinmusic.entity.video.VideoType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface TypeService extends IService<VideoType> {

    /**
     * 获取分类下的标签
     * @param typeId
     * @return
     */
    List<String> getLabels(Long typeId);

    /**
     * 随机获取标签
     * @return
     */
    List<String> random10Labels();
}
