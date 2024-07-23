package cn.zhoutaolinmusic.controller;

import cn.zhoutaolinmusic.entity.video.Video;
import cn.zhoutaolinmusic.limit.Limit;
import cn.zhoutaolinmusic.service.QiNiuFileService;
import cn.zhoutaolinmusic.service.video.VideoService;
import cn.zhoutaolinmusic.utils.JwtUtils;
import cn.zhoutaolinmusic.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

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


}
