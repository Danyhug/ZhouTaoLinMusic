package cn.zhoutaolinmusic.service.video.impl;

import cn.zhoutaolinmusic.entity.video.VideoStar;
import cn.zhoutaolinmusic.mapper.video.VideoStarMapper;
import cn.zhoutaolinmusic.service.video.VideoStarService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoStarServiceImpl extends ServiceImpl<VideoStarMapper, VideoStar> implements VideoStarService {
    @Override
    public boolean starVideo(VideoStar videoStar) {
        return false;
    }

    @Override
    public List<Long> getStarUserIds(Long videoId) {
        return null;
    }

    @Override
    public Boolean starState(Long videoId, Long userId) {
        return null;
    }
}