package com.xuenai.aicodegenerate.ai;

import com.xuenai.aicodegenerate.ai.service.AiCodeGenerateTypeRoutingService;
import com.xuenai.aicodegenerate.ai.service.AiProjectInfoService;
import com.xuenai.aicodegenerate.ai.service.ImageCollectionPlanService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class AiConcurrentTest {

    @Resource
    private AiCodeGenerateTypeRoutingService aiCodeGenerateTypeRoutingService;

    @Resource
    private AiProjectInfoService aiProjectInfoService;

    @Resource
    private ImageCollectionPlanService imageCollectionPlanService;

    @Test
    public void testConcurrentRoutingCalls() throws InterruptedException {
        String[] prompts = {"做一个简单的HTML页面", "做一个多页面网站项目", "做一个Vue管理系统"};
        // 使用虚拟线程并发执行
        Thread[] threads = new Thread[prompts.length];
        for (int i = 0; i < prompts.length; i++) {
            final String prompt = prompts[i];
            final int index = i + 1;
            threads[i] = Thread.ofVirtual().start(() -> {
//                var result = aiCodeGenerateTypeRoutingService.generateRouteCodeType(prompt);
//                var result = aiProjectInfoService.generateProjectInfo("project_info_" + index, prompt);
                var result = imageCollectionPlanService.planImageCollection(prompt);
                log.info("线程 {}: {} -> {}:{}", index, prompt, result);
            });
        }
        // 等待所有任务完成
        for (Thread thread : threads) {
            thread.join();
        }
    }

}
