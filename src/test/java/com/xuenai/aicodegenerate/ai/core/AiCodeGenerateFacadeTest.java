package com.xuenai.aicodegenerate.ai.core;

import com.xuenai.aicodegenerate.model.enums.CodeGenerateTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.List;

@SpringBootTest
class AiCodeGenerateFacadeTest {
    
    @Resource
    private AiCodeGenerateFacade aiCodeGenerateFacade;

    @Test
    void generatorAndSaveCode() {
        Flux<String> result = aiCodeGenerateFacade.generateStreamAndSaveCode("生成一个登录页面，不超过300行代码，内容丰富", CodeGenerateTypeEnum.MULTI_FILE);
        List<String> blocked = result.collectList().block();
    }
}