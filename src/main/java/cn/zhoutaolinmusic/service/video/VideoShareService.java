package cn.zhoutaolinmusic.service.video;

import cn.zhoutaolinmusic.entity.video.VideoShare;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface VideoShareService extends IService<VideoShare> {

    /**
     * 添加分享记录
     * @param videoShare
     */
    boolean share(VideoShare videoShare);



    /**
     * 获取分享用户id
     * @param videoId
     * @return
     */
    List<Long> getShareUserId(Long videoId);
}