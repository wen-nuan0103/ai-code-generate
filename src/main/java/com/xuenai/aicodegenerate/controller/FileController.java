package com.xuenai.aicodegenerate.controller;

import cn.hutool.core.io.FileUtil;
import com.xuenai.aicodegenerate.common.BaseResponse;
import com.xuenai.aicodegenerate.common.ResultUtils;
import com.xuenai.aicodegenerate.exception.BusinessException;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.manager.CosManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private CosManager cosManager;

    /**
     * 文件上传
     *
     * @param multipartFile 文件
     * @return 返回文件的在线地址 URL
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile) {
        validFile(multipartFile);
        
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + originalFilename;
        String filepath = String.format("/temp/avatar/%s", filename);

        File file = null;
        try {
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            String path = cosManager.uploadFile(filepath, file);

            return ResultUtils.success(path);
        } catch (Exception e) {
            log.error("文件上传失败, 文件路径 = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                boolean delete = file.delete();
                if (!delete) {
                    log.error("临时文件删除失败, 文件路径 = " + filepath);
                }
            }
        }
    }

    /**
     * 校验文件 (大小、后缀等)
     */
    private void validFile(MultipartFile multipartFile) {
        long fileSize = multipartFile.getSize();
        final long ONE_MB = 1024 * 1024L;
        if (fileSize > 2 * ONE_MB) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
        }
        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        if (!Arrays.asList("jpg", "jpeg", "png", "webp").contains(suffix)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
        }
    }
}