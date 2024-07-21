package cn.zhoutaolinmusic.service;

import com.qiniu.storage.model.FileInfo;

import java.io.File;

public interface QiNiuFileService {
    /**
     * 获取签名
     * @return
     */
    String getToken();

    /**
     * 上传文件
     * @param file
     */
    String uploadFile(File file);

    /**
     * 删除文件
     * @param url
     */
    void deleteFile(String url);

    /**
     * 获取文件信息
     * @param url
     * @return
     */
    FileInfo getFileInfo(String url);
}
