package cn.zhoutaolinmusic.controller;

import cn.zhoutaolinmusic.config.QiNiuConfig;
import cn.zhoutaolinmusic.entity.File;
import cn.zhoutaolinmusic.service.FileService;
import cn.zhoutaolinmusic.utils.Result;
import com.qiniu.util.Auth;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
