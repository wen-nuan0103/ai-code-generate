package com.xuenai.aicodegenerate.ai.utils;

import com.xuenai.aicodegenerate.model.entity.AiModel;
import com.xuenai.aicodegenerate.service.AiModelService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class AiPriceCalculatorUtil {

    @Resource
    private AiModelService aiModelService;

    public BigDecimal calculateCost(String modelCode,String modelType, int inputTokens, int outputTokens) {
        AiModel model = aiModelService.getModelPriceConfig(modelCode,modelType);

        if (model == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal inputPrice = model.getInputPrice();
        BigDecimal outputPrice = model.getOutputPrice();
        
        BigDecimal inputCost = new BigDecimal(inputTokens)
                .divide(new BigDecimal(1000), 8, RoundingMode.HALF_UP)
                .multiply(inputPrice);

        BigDecimal outputCost = new BigDecimal(outputTokens)
                .divide(new BigDecimal(1000), 8, RoundingMode.HALF_UP)
                .multiply(outputPrice);

        return inputCost.add(outputCost);
    }
    
}
