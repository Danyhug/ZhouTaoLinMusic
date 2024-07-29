package cn.zhoutaolinmusic.controller.admin;

import cn.zhoutaolinmusic.authority.Authority;
import cn.zhoutaolinmusic.constant.AuditStatus;
import cn.zhoutaolinmusic.entity.video.Video;
import cn.zhoutaolinmusic.entity.video.VideoType;
import cn.zhoutaolinmusic.entity.vo.BasePage;
import cn.zhoutaolinmusic.entity.vo.VideoStatistics;
import cn.zhoutaolinmusic.service.user.UserService;
import cn.zhoutaolinmusic.service.video.TypeService;
import cn.zhoutaolinmusic.service.video.VideoService;
import cn.zhoutaolinmusic.utils.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import cn.zhoutaolinmusic.entity.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description: 视频管理端
 */
@RestController
@RequestMapping("/admin/video")
public class AdminVideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserService userService;

    @Autowired
    private TypeService typeService;

    @GetMapping("/{id}")
    @Authority("admin:video:get")
    public Result get(@PathVariable Long id){
        return Result.ok(videoService.getVideoById(id,null));
    }


    @GetMapping("/page")
    @Authority("admin:video:page")
    public Result list(BasePage basePage, @RequestParam(required = false) String YV, @RequestParam(required = false) String title){

        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(!ObjectUtils.isEmpty(YV),Video::getYv,YV).like(!ObjectUtils.isEmpty(title),Video::getTitle,title);

        IPage<Video> page = videoService.page(basePage.page(), wrapper);

        List<Video> records = page.getRecords();
        if (ObjectUtils.isEmpty(records)) return Result.ok();

        ArrayList<Long> userIds = new ArrayList<>();
        ArrayList<Long> typeIds = new ArrayList<>();
        for (Video video : records) {
            userIds.add(video.getUserId());
            typeIds.add(video.getTypeId());
        }

        Map<Long, String> userMap = userService.list(new LambdaQueryWrapper<User>().select(User::getId, User::getNickName)
        .in(User::getId,userIds))
                .stream().collect(Collectors.toMap(User::getId, User::getNickName));

        Map<Long, String> typeMap = typeService.listByIds(typeIds).stream().collect(Collectors.toMap(VideoType::getId, VideoType::getName));

        for (Video video : records) {
            video.setAuditStateName(AuditStatus.getName(video.getAuditStatus()));
            video.setUserName(userMap.get(video.getUserId()));
            video.setOpenName(video.getOpen() ? "私密" : "公开");
            video.setTypeName(typeMap.get(video.getTypeId()));
        }
        // TODO return Result.ok(records.count(page.getTotal()));
        return null;
    }

    /**
     * 删除视频
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @Authority("admin:video:delete")
    public Result delete(@PathVariable Long id){
        videoService.deleteVideo(id);
        return Result.ok("删除成功");
    }

    /**
     * 放行视频
     * @param video
     * @return
     */
    @PostMapping("/audit")
    @Authority("admin:video:audit")
    public Result audit(@RequestBody Video video){
        videoService.auditProcess(video);
        return Result.ok("审核放行");
    }

    /**
     * 下架视频
     * @param id
     * @return
     */
    @PostMapping("/violations/{id}")
    @Authority("admin:video:violations")
    public Result Violations(@PathVariable Long id){
        videoService.violations(id);
        return Result.ok("下架成功");
    }


    /**
     * 视频数据统计
     * @return
     */
    @GetMapping("/statistics")
    @Authority("admin:video:statistics")
    public Result show(){
        VideoStatistics videoStatistics = new VideoStatistics();
        int allCount = videoService.count(new LambdaQueryWrapper<Video>());
        int processCount = videoService.count(new LambdaQueryWrapper<Video>().eq(Video::getAuditStatus, AuditStatus.PROCESS));
        int successCount = videoService.count(new LambdaQueryWrapper<Video>().eq(Video::getAuditStatus, AuditStatus.SUCCESS));
        int passCount = videoService.count(new LambdaQueryWrapper<Video>().eq(Video::getAuditStatus, AuditStatus.PASS));
        videoStatistics.setAllCount(allCount);
        videoStatistics.setPassCount(passCount);
        videoStatistics.setSuccessCount(successCount);
        videoStatistics.setProcessCount(processCount);

        return Result.ok(videoStatistics);
    }
}
