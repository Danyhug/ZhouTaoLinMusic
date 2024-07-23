package cn.zhoutaolinmusic.service.video.impl;

import cn.zhoutaolinmusic.entity.video.VideoType;
import cn.zhoutaolinmusic.mapper.video.TypeMapper;
import cn.zhoutaolinmusic.service.video.TypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TypeServiceImpl extends ServiceImpl<TypeMapper, VideoType> implements TypeService {
    @Override
    public List<String> getLabels(Long typeId) {
        VideoType videoType = this.getById(typeId);
        return videoType.buildLabel();
    }

    @Override
    public List<String> random10Labels() {
        // 获取所有分类
        List<VideoType> types = this.list(null);
        // 打乱
        Collections.shuffle(types);
        // 返回数据
        List<String> labels = new ArrayList<>();

        for (VideoType type : types) {
            for (String label: type.buildLabel()) {
                if (labels.size() == 10) return labels;
                labels.add(label);
            }
        }
        return labels;
    }
}
