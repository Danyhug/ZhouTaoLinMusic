package cn.zhoutaolinmusic.controller;

import cn.zhoutaolinmusic.entity.video.Video;
import cn.zhoutaolinmusic.entity.vo.BasePage;
import cn.zhoutaolinmusic.limit.Limit;
import cn.zhoutaolinmusic.service.QiNiuFileService;
import cn.zhoutaolinmusic.service.video.VideoService;
import cn.zhoutaolinmusic.utils.Result;
import cn.zhoutaolinmusic.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jjjmusic/video")
public class VideoController {

    @Autowired
    private QiNiuFileService qiNiuFileService;

    @Autowired
    private VideoService videoService;

    /**
     * 获取上传token
     * @return
     */
    @GetMapping("/token")
    public Result<String> token()
    {
        return Result.ok(qiNiuFileService.getToken());
    }

    /**
     * 发布视频
     * @param video
     * @return
     */
    @PostMapping("")
    @Limit(limit = 5, time = 3600L, msg = "每小时最多可发布5条视频")
    public Result<String> publishVideo(@RequestBody @Validated Video video) {
        videoService.publishVideo(video);
        return Result.ok("已成功发布，请等待审核");
    }

    /**
     * 删除视频
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteVideo(@PathVariable Long id) {
        videoService.deleteVideo(id);
        return Result.ok("删除成功");
    }

    /**
     * 查看用户管理的视频 — 稿件管理
     * @param basePage
     * @return
     */
    @GetMapping("")
    public Result getListVideo(BasePage basePage) {
        return Result.ok(videoService.listByUserIdVideo(basePage, UserHolder.get()));
    }

    /**
     * 点赞视频
     * @param vid
     * @return
     */
    @PostMapping("/star/{vid}")
    public Result<String> starVideo(@PathVariable Long vid) {
        return Result.ok(
                videoService.startVideo(vid) ? "已点赞" : "取消点赞"
        );
    }

    /**
     * 添加历史记录
     * @param id
     * @return
     * @throws Exception
     */
    @PostMapping("/history/{id}")
    public Result addHistory(@PathVariable Long id) throws Exception {
        videoService.historyVideo(id, UserHolder.get());
        return Result.ok();
    }

}
