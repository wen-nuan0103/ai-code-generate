package com.xuenai.aicodegenerate.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.xuenai.aicodegenerate.exception.BusinessException;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

/**
 * Web截图工具类
 */
@Slf4j
public class WebScreenshotUtil {
    
    /**
     * 保存网页截图
     *
     * @param webUrl 网页地址
     * @return 截图文件路径
     */
    public static String saveWebScreenshot(WebDriver webDriver,String webUrl) {
        if (StrUtil.isBlank(webUrl)) {
            log.error("网页url不能为空");
            return null;
        }
        try {
            String rootDir = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "screenshots" + File.separator + UUID.randomUUID().toString().substring(0, 8);
            FileUtil.mkdir(rootDir);
            final String IMAGE_SUFFIX = ".png";
            String screenshotPath = rootDir + File.separator + RandomUtil.randomNumbers(5) + IMAGE_SUFFIX;
            webDriver.get(webUrl);
            waitForPageLoad(webDriver);
            byte[] screenshotBytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
            saveImage(screenshotBytes, screenshotPath);
            log.info("保存网页截图成功: {}", screenshotPath);
            final String COMPRESSED_SUFFIX = "_compressed.jpg";
            String compressedImagePath = rootDir + File.separator + RandomUtil.randomNumbers(5) + COMPRESSED_SUFFIX;
            compressImage(screenshotPath, compressedImagePath);
            log.info("压缩网页截图成功: {}", compressedImagePath);
            FileUtil.del(screenshotPath);
            return compressedImagePath;
        } catch (Exception e) {
            log.error("保存网页截图失败", e);
            return null;
        }
    }

    /**
     * 等待页面加载完毕
     *
     * @param webDriver 网页驱动
     */
    private static void waitForPageLoad(WebDriver webDriver) {
        try {
            WebDriverWait driverWait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
            driverWait.until(driver -> Objects.equals(((JavascriptExecutor) driver).executeScript("return document.readyState"), "complete"));
            Thread.sleep(2000);
            log.info("页面加载完成");
        } catch (Exception e) {
            log.error("页面加载失败: ", e);
        }
    }

    /**
     * 保存图片
     *
     * @param image 图片二进制数组
     * @param path  保存路径
     */
    private static void saveImage(byte[] image, String path) {
        try {
            FileUtil.writeBytes(image, path);
        } catch (Exception e) {
            log.error("保存图片失败: {}", path, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    /**
     * 压缩图片
     *
     * @param originalImagePath   原始图片路径
     * @param compressedImagePath 压缩后图片路径
     */
    private static void compressImage(String originalImagePath, String compressedImagePath) {
        final float COMPRESS_QUANTITY = 0.3f;
        try {
            ImgUtil.compress(FileUtil.file(originalImagePath), FileUtil.file(compressedImagePath), COMPRESS_QUANTITY);
        } catch (Exception e) {
            log.error("压缩图片失败: {} -> {}", originalImagePath, compressedImagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }
}

