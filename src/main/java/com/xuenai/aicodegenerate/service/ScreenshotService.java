package com.xuenai.aicodegenerate.service;

/**
 * 截图 服务层
 */
public interface ScreenshotService {

    /**
     * 截图并保存至 COS
     *
     * @param webUrl 需要截图网页
     * @return 图片访问地址
     */
    String generateAndSaveScreenshot(String webUrl);

}
