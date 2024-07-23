package cn.zhoutaolinmusic.controller;

import cn.zhoutaolinmusic.entity.video.Video;
import cn.zhoutaolinmusic.entity.vo.BasePage;
import cn.zhoutaolinmusic.service.user.UserService;
import cn.zhoutaolinmusic.service.video.TypeService;
import cn.zhoutaolinmusic.service.video.VideoService;
import cn.zhoutaolinmusic.utils.JwtUtils;
import cn.zhoutaolinmusic.utils.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

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
}
