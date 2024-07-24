package cn.zhoutaolinmusic.service.video.impl;

import cn.zhoutaolinmusic.entity.video.VideoShare;
import cn.zhoutaolinmusic.mapper.video.VideoShareMapper;
import cn.zhoutaolinmusic.service.video.VideoShareService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoShareServiceImpl extends ServiceImpl<VideoShareMapper, VideoShare> implements VideoShareService {
    @Override
    public boolean share(VideoShare videoShare) {
        return false;
    }

    @Override
    public List<Long> getShareUserId(Long videoId) {
        return null;
    }
}
