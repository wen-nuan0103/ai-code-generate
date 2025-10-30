package com.xuenai.aicodegenerate.ai.core;

import com.xuenai.aicodegenerate.model.enums.CodeGeneratorTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.List;

@SpringBootTest
class AiCodeGeneratorFacadeTest {
    
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generatorAndSaveCode() {
        Flux<String> result = aiCodeGeneratorFacade.generatorStreamAndSaveCode("生成一个登录页面，不超过300行代码，内容丰富", CodeGeneratorTypeEnum.MULTI_FILE);
        List<String> blocked = result.collectList().block();
    }
}