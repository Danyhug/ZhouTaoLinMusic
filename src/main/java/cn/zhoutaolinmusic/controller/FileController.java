package cn.zhoutaolinmusic.controller;

import cn.zhoutaolinmusic.config.LocalCache;
import cn.zhoutaolinmusic.config.QiNiuConfig;
import cn.zhoutaolinmusic.entity.File;
import cn.zhoutaolinmusic.service.FileService;
import cn.zhoutaolinmusic.utils.Result;
import cn.zhoutaolinmusic.utils.UserHolder;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@Log4j2
@RequestMapping("/jjjmusic/file")
public class FileController {

    @Autowired
    private QiNiuConfig qiNiuConfig;
    @Autowired
    private FileService fileService;

    /**
     * 视频上传逻辑
     * 前端点击上传按钮 -》 前端向后端请求token
     * -》 后端返回access_key数据 -》 前端请求七牛云携带ak
     * -> 前端访问文件上传接口
     */
    @PostMapping("")
    public Result<Long> save(String fileKey) {
        Long id = fileService.save(fileKey, UserHolder.get());
        return Result.ok(id);
    }

    /**
     * 携带文件类型获取上传七牛云需要的token
     * @return
     */
    @GetMapping("/getToken")
    public Result<String> token() {
        return Result.ok(qiNiuConfig.uploadToken());
    }

    /**
     * 获取文件url, 获取成功后重定向到目标地址
     * 逻辑：
     *      前端携带视频id访问接口 -》 后端验证成功后重定向到 真实url+uuid，并将 uuid 存储到本地 -》前端请求七牛云
     *              -》 七牛云鉴权请求本地auth接口 -》 鉴权成功后重定向到目标地址并删除uuid | 鉴权失败返回 401
     * @param request
     * @param response
     * @param fileId
     * @throws IOException
     */
    @GetMapping("/{fileId}")
    public void getFileUrl(HttpServletRequest request, HttpServletResponse response,
                                     @PathVariable Long fileId) throws IOException
        {
            // 判断请求来源
            // String referer = request.getHeader("Referer");
            // if (referer == null || !referer.contains("jjjmusic.cn")) {
            //     response.sendError(403, "非法请求");
            //     return;
            // }
            File file = fileService.getFileTrustUrl(fileId);

            // 设置响应类型
            response.setContentType(file.getFormat());
            // 重定向到目标地址
            response.sendRedirect(file.getFileKey());
        }

    @PostMapping("/auth")
    public void auth(@RequestParam(required = false) String uuid, HttpServletResponse response) throws IOException {
        if (LocalCache.containsKey(uuid)) {
            // 访问一次后 uuid 失效
            LocalCache.rm(uuid);
            response.sendError(200);

        } else {
            response.sendError(401);
        }
    }
}
