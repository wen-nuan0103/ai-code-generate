package com.xuenai.aicodegenerate.pool;

import com.xuenai.aicodegenerate.exception.BusinessException;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * WebDriver 池
 */
@Slf4j
@Component
public class WebDriverPool {

    private final BlockingQueue<WebDriver> pool;
    private final List<WebDriver> allDrivers = new ArrayList<>();

    @Value("${screenshot.pool-size}")
    private int poolSize;

    @Value("${screenshot.browser-width}")
    private int width;

    @Value("${screenshot.browser-height}")
    private int height;

    public WebDriverPool() {
        this.pool = new ArrayBlockingQueue<>(10);
    }

    @PostConstruct
    public void init() {
        BlockingQueue<WebDriver> newPool = new ArrayBlockingQueue<>(Math.max(1, poolSize));
        for (int i = 0; i < poolSize; i++) {
            WebDriver driver = createDriver();
            allDrivers.add(driver);
            newPool.offer(driver);
        }
        pool.clear();
        newPool.forEach(pool::offer);
        log.info("初始化 WebDriver 池，大小 = {}", poolSize);
    }

    /**
     * 创建驱动
     *
     * @return 驱动类
     */
    private WebDriver createDriver() {
        try {
            System.setProperty("wdm.chromeDriverMirrorUrl", "https://registry.npmmirror.com/binary.html?path=chromedriver");
            WebDriverManager.chromedriver().useMirror().setup();
            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            // 无头模式
            options.addArguments("--headless");
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            // 创建驱动
            WebDriver driver = new ChromeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }


    /**
     * 借出驱动（进行阻塞）
     *
     * @return 驱动
     * @throws InterruptedException
     */
    public WebDriver borrowDriver() throws InterruptedException {
        WebDriver driver = pool.take();
        log.debug("borrowDriver: {}", driver);
        return driver;
    }


    /**
     * 归还驱动
     *
     * @param driver 驱动
     */
    public void returnDriver(WebDriver driver) {
        if (driver != null) {
            pool.offer(driver);
            log.debug("returnDriver: {}", driver);
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("关闭所有 WebDriver");
        for (WebDriver d : allDrivers) {
            try {
                d.quit();
            } catch (Exception e) {
                log.warn("关闭驱动失败", e);
            }
        }
    }
}
