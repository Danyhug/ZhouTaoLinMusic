package cn.zhoutaolinmusic.service.impl;

import cn.zhoutaolinmusic.config.LocalCache;
import cn.zhoutaolinmusic.config.QiNiuConfig;
import cn.zhoutaolinmusic.entity.File;
import cn.zhoutaolinmusic.exception.BaseException;
import cn.zhoutaolinmusic.mapper.FileMapper;
import cn.zhoutaolinmusic.service.FileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Log4j2
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {

    @Autowired
    private QiNiuConfig qiNiuConfig;

    @Override
    public Long save(String fileKey, Long userId) {
        return 0L;
    }

    @Override
    public Long generatePhoto(Long fileId, Long userId) {
        return 0L;
    }

    @Override
    public File getFileTrustUrl(Long fileId) {
        File file = this.getById(fileId);
        if (Objects.isNull(file)) {
            throw new BaseException("文件不存在");
        }

        String uuid = UUID.randomUUID().toString();
        String url = qiNiuConfig.getCname() + "/" + file.getFileKey() + "?uuid=" + uuid;
        log.info("转换后的文件 url: {}", url);

        // 文件 uuid 存储到缓存中
        LocalCache.put(uuid, null);

        // 把真实的 url 存到 file 中
        file.setFileKey(url);
        return file;
    }
}
