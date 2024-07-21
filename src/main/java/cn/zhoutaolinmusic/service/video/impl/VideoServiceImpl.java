package cn.zhoutaolinmusic.service.video.impl;

import cn.zhoutaolinmusic.entity.video.Video;
import cn.zhoutaolinmusic.entity.video.VideoShare;
import cn.zhoutaolinmusic.entity.video.VideoStar;
import cn.zhoutaolinmusic.entity.vo.BasePage;
import cn.zhoutaolinmusic.entity.vo.HotVideo;
import cn.zhoutaolinmusic.exception.BaseException;
import cn.zhoutaolinmusic.mapper.video.VideoMapper;
import cn.zhoutaolinmusic.service.video.VideoService;
import cn.zhoutaolinmusic.service.video.VideoShareService;
import cn.zhoutaolinmusic.service.video.VideoStarService;
import cn.zhoutaolinmusic.utils.UserHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.UserDataHandler;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private VideoStarService videoStarService;
    @Autowired
    private VideoShareService videoShareService;

    @Override
    public Video getVideoById(Long id, Long userId) {
        return null;
    }

    @Override
    public void publishVideo(Video video) {

    }

    @Override
    public void deleteVideo(Long id) {
        Long userId = UserHolder.get();
        Video video = this.getOne(
                new LambdaQueryWrapper<Video>()
                        .eq(Video::getId, id).eq(Video::getUserId, userId)
        );

        if (video == null) {
            throw new BaseException("要删除的视频不存在");
        }

        if (this.removeById(id)) {
            new Thread(() -> {
                // 删除 分享量 & 点赞量
                videoShareService.remove(
                        new LambdaQueryWrapper<VideoShare>()
                                .eq(VideoShare::getVideoId, id)
                                .eq(VideoShare::getUserId, userId)
                );
                videoStarService.remove(
                        new LambdaQueryWrapper<VideoStar>()
                                .eq(VideoStar::getVideoId, id)
                                .eq(VideoStar::getUserId, userId)
                );

                // 兴趣推送删除
                // 删除对应标签的视频数据
                // interestPushService.deleteSystemStockIn(video);
                // 删除对应分类的视频数据
                // interestPushService.deleteSystemTypeStockIn(video);
            }).start();
        }

    }

    @Override
    public Collection<Video> pushVideos(Long userId) {
        return null;
    }

    @Override
    public Collection<Video> getVideoByTypeId(Long typeId) {
        return null;
    }

    @Override
    public IPage<Video> searchVideo(String search, BasePage basePage, Long userId) {
        return null;
    }

    @Override
    public void auditProcess(Video video) {

    }

    @Override
    public boolean startVideo(Long videoId) {
        return false;
    }

    @Override
    public boolean shareVideo(VideoShare videoShare) {
        return false;
    }

    @Override
    public void historyVideo(Long videoId, Long userId) throws Exception {

    }

    @Override
    public boolean favoritesVideo(Long fId, Long vId) {
        return false;
    }

    @Override
    public LinkedHashMap<String, List<Video>> getHistory(BasePage basePage) {
        return null;
    }

    @Override
    public Collection<Video> listVideoByFavorites(Long favoritesId) {
        return null;
    }

    @Override
    public Collection<HotVideo> hotRank() {
        return null;
    }

    @Override
    public Collection<Video> listSimilarVideo(Video video) {
        return null;
    }

    @Override
    public IPage<Video> listByUserIdOpenVideo(Long userId, BasePage basePage) {
        return null;
    }

    @Override
    public String getAuditQueueState() {
        return "";
    }

    @Override
    public List<Video> selectNDaysAgeVideo(long id, int days, int limit) {
        return null;
    }

    @Override
    public Collection<Video> listHotVideo() {
        return null;
    }

    @Override
    public Collection<Video> followFeed(Long userId, Long lastTime) {
        return null;
    }

    @Override
    public void initFollowFeed(Long userId) {

    }

    @Override
    public IPage<Video> listByUserIdVideo(BasePage basePage, Long userId) {
        return null;
    }

    @Override
    public Collection<Long> listVideoIdByUserId(Long userId) {
        return null;
    }

    @Override
    public void violations(Long id) {

    }
}
