package cn.zhoutaolinmusic.config;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "qiniu.kodo")
public class QiNiuConfig {
    /**
     * 账号
     */
    private String accessKey;
    /**
     * 密钥
     */
    private String secretKey;
    /**
     * bucketName
     */
    private String bucketName;

    public String cname;
    public String videoUrl;
    public String imageUrl;

    // 视频审核
    public static final String fops = "avthumb/mp4";

    // 构建鉴权对象
    public Auth buildAuth() {
        return Auth.create(this.accessKey, this.secretKey);
    }

    // 上传凭证
    public String uploadToken() {
        Auth auth = buildAuth();
        return auth.uploadToken(this.bucketName, null, 3600,
                new StringMap().put("mimeLimit", "video/*;image/*"));
    }

    // 视频上传凭证
    public String videoUploadToken() {
        Auth auth = buildAuth();
        return auth.uploadToken(this.bucketName, null, 3600,
                new StringMap().put("mimeLimit", "video/*").putNotEmpty("fops", fops));
    }
}
