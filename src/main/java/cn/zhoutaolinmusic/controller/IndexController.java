package cn.zhoutaolinmusic.controller;

import cn.zhoutaolinmusic.entity.video.Video;
import cn.zhoutaolinmusic.entity.video.VideoShare;
import cn.zhoutaolinmusic.entity.video.VideoType;
import cn.zhoutaolinmusic.entity.vo.BasePage;
import cn.zhoutaolinmusic.entity.vo.HotVideo;
import cn.zhoutaolinmusic.service.user.UserService;
import cn.zhoutaolinmusic.service.video.TypeService;
import cn.zhoutaolinmusic.service.video.VideoService;
import cn.zhoutaolinmusic.utils.JwtUtils;
import cn.zhoutaolinmusic.utils.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/jjjmusic/index")
public class IndexController {
    @Autowired
    private UserService userService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private TypeService typeService;

    /**
     * 兴趣推送视频
     * @param request
     * @return
     */
    @GetMapping("/pushVideos")
    public Result<Collection<Video>> pushVideos(HttpServletRequest request) {
        Long userId = JwtUtils.getUserId(request);
        return Result.ok(videoService.pushVideos(userId));
    }

    /**
     * 搜索视频
     * @param searchName
     * @param basePage
     * @param request
     * @return
     */
    @GetMapping("/search")
    public Result<IPage<Video>> searchVideo(@RequestParam(required = false) String searchName, BasePage basePage, HttpServletRequest request){
        return Result.ok(
                videoService.searchVideo(searchName,basePage,JwtUtils.getUserId(request))
        );
    }

    /**
     * 获取所有分类
     * @return
     */
    @GetMapping("/types")
    public Result<List<VideoType>> types(HttpServletRequest request){
        // 获取所有分类信息
        List<VideoType> types = typeService.list(
                new LambdaQueryWrapper<VideoType>().select(VideoType::getIcon, VideoType::getId, VideoType::getName).orderByDesc(VideoType::getSort)
        );

        // 查询用户自己创建的分类
        Set<Long> set = userService.listSubscribeType(JwtUtils.getUserId(request)).stream()
                .map(VideoType::getId).collect(Collectors.toSet());

        for (VideoType type : types) {
            if (set.contains(type.getId())) {
                type.setUsed(true);
            } else {
                type.setUsed(false);
            }
        }
        return Result.ok(types);
    }

    /**
     * 获取搜索历史
     * @param request
     * @return
     */
    @GetMapping("/search/history")
    public Result<Collection<String>> searchHistory(HttpServletRequest request){
        return Result.ok(
                userService.searchHistory(JwtUtils.getUserId(request))
        );
    }

    /**
     * 获取热门视频
     * @return
     */
    @GetMapping("/video/hot")
    public Result<Collection<Video>> listHostVideo() {
        return Result.ok(videoService.listHotVideo());
    }

    /**
     * 获取热门排行
     * @return
     */
    @GetMapping("/video/hot/rank")
    public Result<Collection<HotVideo>> hotRank() {
        return Result.ok(videoService.hotRank());
    }

    /**
     * 根据用户id获取视频
     * @param userId
     * @param basePage
     * @param request
     * @return
     */
    @GetMapping("/video/user")
    public Result listVideoByUserId(@RequestParam(required = false) Long userId,
                               BasePage basePage,HttpServletRequest request){
        userId = userId == null ? JwtUtils.getUserId(request) : userId;
        return Result.ok(videoService.listByUserIdOpenVideo(userId, basePage));
    }

    /**
     * 根据视频标签推送相似视频
     * @param video
     * @return
     */
    @GetMapping("/video/similar")
    public Result pushVideoSimilar(Video video) {
        return Result.ok(videoService.listSimilarVideo(video));
    }

    /**
     * 根据视频id获取视频
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/video/{id}")
    public Result<Video> getVideoById(@PathVariable Long id, HttpServletRequest request) {
        return Result.ok(videoService.getVideoById(id, JwtUtils.getUserId(request)));
    }

    @GetMapping("/video/type/{id}")
    public Result<Collection<Video>> listVideoByTypeId(@PathVariable Long id) {
        return Result.ok(videoService.getVideoByTypeId(id));
    }


    /**
     * 分享视频
     * @param videoId
     * @param request
     * @return
     */
    @PostMapping("/share/{videoId}")
    public Result share(@PathVariable Long videoId, HttpServletRequest request){
        final VideoShare videoShare = new VideoShare();

        videoShare.setVideoId(videoId);
        videoShare.setIp(null);
        if (JwtUtils.checkToken(request)) {
            videoShare.setUserId(JwtUtils.getUserId(request));
        }
        videoService.shareVideo(videoShare);
        return Result.ok();
    }
}
