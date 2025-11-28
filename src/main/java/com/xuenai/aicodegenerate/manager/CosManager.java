package com.xuenai.aicodegenerate.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectResult;
import com.xuenai.aicodegenerate.config.CosClientConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * COS 对象存储管理器
 */
@Slf4j
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 上传文件
     * @return 上传结果
     */
    public PutObjectResult putObject(String key, File file) {
        return cosClient.putObject(cosClientConfig.getBucket(), key, file);
    }

    /**
     * 上传文件
     *
     * @param key  唯一键
     * @param file 上传文件
     * @return 访问链接
     */
    public String uploadFile(String key, File file) {
        PutObjectResult result = putObject(key, file);
        if (result != null) {
            String url = String.format("%s%s", cosClientConfig.getHost(), key);
            log.info("文件上传 COS 成功: {} -> {}", file.getName(), url);
            return url;
        } else {
            log.error("文件上传 COS 失败: {}", key);
            return null;
        }
    }

}
