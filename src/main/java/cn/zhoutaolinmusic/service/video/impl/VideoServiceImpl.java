package cn.zhoutaolinmusic.service.video.impl;

import cn.zhoutaolinmusic.config.LocalCache;
import cn.zhoutaolinmusic.config.QiNiuConfig;
import cn.zhoutaolinmusic.constant.AuditStatus;
import cn.zhoutaolinmusic.entity.File;
import cn.zhoutaolinmusic.entity.VideoTask;
import cn.zhoutaolinmusic.entity.user.User;
import cn.zhoutaolinmusic.entity.video.Video;
import cn.zhoutaolinmusic.entity.video.VideoShare;
import cn.zhoutaolinmusic.entity.video.VideoStar;
import cn.zhoutaolinmusic.entity.video.VideoType;
import cn.zhoutaolinmusic.entity.vo.BasePage;
import cn.zhoutaolinmusic.entity.vo.HotVideo;
import cn.zhoutaolinmusic.entity.vo.UserVO;
import cn.zhoutaolinmusic.exception.BaseException;
import cn.zhoutaolinmusic.mapper.video.VideoMapper;
import cn.zhoutaolinmusic.service.FileService;
import cn.zhoutaolinmusic.service.InterestPushService;
import cn.zhoutaolinmusic.service.user.FavoritesService;
import cn.zhoutaolinmusic.service.user.FollowService;
import cn.zhoutaolinmusic.service.user.UserService;
import cn.zhoutaolinmusic.service.video.TypeService;
import cn.zhoutaolinmusic.service.video.VideoService;
import cn.zhoutaolinmusic.service.video.VideoShareService;
import cn.zhoutaolinmusic.service.video.VideoStarService;
import cn.zhoutaolinmusic.utils.FileUtil;
import cn.zhoutaolinmusic.utils.UserHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private QiNiuConfig qiNiuConfig;

    @Autowired
    private InterestPushService interestPushService;

    @Autowired
    private UserService userService;

    // @Autowired
    // private VideoAuditService videoAuditService;

    @Autowired
    private TypeService typeService;
    @Autowired
    private VideoStarService videoStarService;
    @Autowired
    private VideoShareService videoShareService;
    @Autowired
    private FavoritesService favoritesService;
    @Autowired
    private FollowService followService;

    @Override
    public Video getVideoById(Long videoId, Long userId) {
        Video video = this.getOne(new LambdaQueryWrapper<Video>().eq(Video::getId, videoId));
        if (video == null) throw new BaseException("指定视频信息不存在");

        // 如果是私密视频返回空
        if (video.getOpen()) return new Video();

        // 收集用户自己对该视频的 点赞 / 收藏 信息
        video.setStart(videoStarService.starState(videoId, userId));
        video.setFavorites(favoritesService.favoritesState(videoId, userId));
        video.setFollow(followService.isFollows(video.getUserId(), userId));
        return video;
    }

    @Override
    public void publishVideo(Video video) {
        Long userId = UserHolder.get();
        Video oldVideo = null;

        Long videoId = video.getId();
        if (videoId != null) {
            // 获取视频源信息
            oldVideo = this.getOne(new LambdaQueryWrapper<Video>().eq(Video::getId, videoId).eq(Video::getUserId, userId));

            // 不允许修改视频封片和视频源
            if (!oldVideo.buildVideoUrl().equals(video.buildVideoUrl())
                    || !video.buildCoverUrl().equals(oldVideo.buildCoverUrl())) {
                throw new BaseException("视频源和封面不能修改");
            }
        }

        // 校验标签最多不能超过5个
        if (video.buildLabel().size() > 5) {
            throw new BaseException("标签最多只能选择5个");
        }

        // 修改状态
        video.setAuditStatus(AuditStatus.PROCESS);
        video.setUserId(userId);

        // 判断是添加还是修改
        boolean isAdd = videoId == null;

        // 设置一个空 Yv 号
        video.setYv(null);

        if (!isAdd) {
            // 修改时，下面字段不被修改，设置为空
            video.setVideoType(null);
            video.setLabelNames(null);
            video.setUrl(null);
            video.setCover(null);
        } else {
            // 未设置封面生成默认封面
            if (ObjectUtils.isEmpty(video.getCover())) {
                video.setCover(fileService.generatePhoto(video.getUrl(), userId));
            }

            // 生成视频号
            video.setYv("YV" + UUID.randomUUID().toString().replace("-", "").substring(8));
        }

        // 填充视频时长
        if (isAdd || !StringUtils.hasLength(oldVideo.getDuration())) {
            String uuid = UUID.randomUUID().toString();
            LocalCache.put(uuid, true);
            try {
                Long url = video.getUrl();
                if (url == null || url == 0) url = oldVideo.getUrl();
                String fileKey = fileService.getById(url).getFileKey();
                String duration = FileUtil.getVideoDuration(qiNiuConfig.getCname() + "/" + fileKey + "?uuid=" + uuid);
                video.setDuration(duration);
            } finally {
                LocalCache.rm(uuid);
            }
        }
        this.saveOrUpdate(video);

        // 提交审核
        VideoTask videoTask = new VideoTask();
        videoTask.setOldVideo(video);
        videoTask.setVideo(video);
        videoTask.setIsAdd(isAdd);
        videoTask.setOldState(isAdd || video.getOpen());
        videoTask.setNewState(true);
        // TODO
        // videoPublishAuditService.audit(videoTask, false);
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
        User user = null;
        // 获取用户信息
        if (userId != null) user = userService.getById(userId);

        // 获取用户喜欢的视频id
        Collection<Long> videoIds = interestPushService.listVideoIdByUserModel(user);

        if (ObjectUtils.isEmpty(videoIds)) {
            // 获取所有的视频id，创建时间 倒序
            videoIds = list(new LambdaQueryWrapper<Video>().orderByDesc(Video::getGmtCreated))
                    .stream().map(Video::getId).collect(Collectors.toList());
        }

        Collection<Video> videos = this.listByIds(videoIds);
        addVideosDetailInfo(videos);
        return videos;
    }

    // 添加推送视频的用户信息（用户信息和视频对应的文件信息）
    public void addVideosDetailInfo(Collection<Video> videos) {
        if (ObjectUtils.isEmpty(videos)) return;

        Set<Long> userIds = new HashSet<>();
        ArrayList<Long> fileIds = new ArrayList<>();
        // 遍历所有视频信息
        for (Video video: videos) {
            // 获取用户id
            userIds.add(video.getUserId());
            // 获取视频id
            fileIds.add(video.getUrl());
            // 获取视频封面对应的fileId
            fileIds.add(video.getCover());
        }

        // 获取所有的文件信息 key为id，value为本身（identity）
        Map<Long, File> fileMap = fileService.listByIds(fileIds).stream()
                .collect(Collectors.toMap(File::getId, Function.identity()));
        // 获取所有的用户信息 key为id，value为本身（identity）
        Map<Long, User> userMap = userService.list(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        for (Video video: videos) {
            UserVO userVO = new UserVO();
            // 获取视频对应的用户信息
            User user = userMap.get(video.getUserId());
            userVO.setId(video.getUserId());
            userVO.setNickName(user.getNickName());
            userVO.setDescription(user.getDescription());
            userVO.setSex(user.getSex());
            // 视频关联用户信息
            video.setUser(userVO);
            // 视频关联文件信息
            File file = fileMap.get(video.getUrl());
            video.setVideoType(file.getFormat());
        }
    }

    @Override
    public Collection<Video> getVideoByTypeId(Long typeId) {
        if (typeId == null) return Collections.emptyList();

        // 获取视频分类
        VideoType videoType = typeService.getById(typeId);
        // 分类不存在
        if (videoType == null) throw new BaseException("分类不存在");

        // 获取该分类下的视频
        Collection<Long> videoIds = interestPushService.listVideoIdByTypeId(typeId);
        if (ObjectUtils.isEmpty(videoIds)) return Collections.emptyList();
        // 获取所有视频信息
        Collection<Video> videos = this.listByIds(videoIds);

        addVideosDetailInfo(videos);
        return videos;
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
