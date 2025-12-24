package com.xuenai.aicodegenerate.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.exception.ThrowUtils;
import com.xuenai.aicodegenerate.manager.CosManager;
import com.xuenai.aicodegenerate.pool.WebDriverPool;
import com.xuenai.aicodegenerate.service.ScreenshotService;
import com.xuenai.aicodegenerate.utils.WebScreenshotUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private CosManager cosManager;

    @Lazy
    @Resource
    private WebDriverPool webDriverPool;

    @Override
    public String generateAndSaveScreenshot(String webUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR, "网页url不能为空");
        log.info("开始截图url:{}", webUrl);
        WebDriver driver = null;
        String localScreenshotPath = null;
        try {
            driver = webDriverPool.borrowDriver();
            localScreenshotPath = WebScreenshotUtil.saveWebScreenshot(driver, webUrl);
            ThrowUtils.throwIf(StrUtil.isBlank(localScreenshotPath), ErrorCode.OPERATION_ERROR, "本地截图生成失败");
            String cosUrl = uploadScreenshotToCos(localScreenshotPath);
            ThrowUtils.throwIf(StrUtil.isBlank(cosUrl), ErrorCode.OPERATION_ERROR, "截图上传失败");
            log.info("截图生产并上传成功:{} -> {}", webUrl, cosUrl);
            return cosUrl;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            cleanLocalFile(localScreenshotPath);
            if (driver != null) {
                webDriverPool.returnDriver(driver);
            }
        }
    }

    /**
     * 将本地截图文件上传至 COS
     *
     * @param localScreenshotPath 本地文件路径
     * @return COS 文件路径
     */
    private String uploadScreenshotToCos(String localScreenshotPath) {
        if (StrUtil.isBlank(localScreenshotPath)) {
            return null;
        }
        File screenshotFile = new File(localScreenshotPath);
        if (!screenshotFile.exists()) {
            log.error("截图文件不存在:{}", localScreenshotPath);
            return null;
        }
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";
        String cosKey = generateScreenshotKey(fileName);
        return cosManager.uploadFile(cosKey, screenshotFile);
    }

    /**
     * 生成文件完整路径（带日期分割）
     *
     * @param fileName 文件名称
     * @return 全路径
     */
    private String generateScreenshotKey(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshots/%s/%s", datePath, fileName);
    }

    /**
     * 清理本地截图文件
     *
     * @param localFilePath 本地文件路径
     */
    private void cleanLocalFile(String localFilePath) {
        File localFile = new File(localFilePath);
        if (localFile.exists()) {
            File parentDir = localFile.getParentFile();
            FileUtil.del(parentDir);
            log.info("删除本地截图文件:{}", localFilePath);
        }
    }
    
    

}
