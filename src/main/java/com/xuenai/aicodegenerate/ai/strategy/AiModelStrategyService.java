package com.xuenai.aicodegenerate.ai.strategy;

import cn.hutool.core.collection.CollectionUtil;
import com.xuenai.aicodegenerate.model.entity.AiApiKey;
import com.xuenai.aicodegenerate.model.entity.AiModel;
import com.xuenai.aicodegenerate.service.AiApiKeyService;
import com.xuenai.aicodegenerate.service.AiModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiModelStrategyService {

    private final AiModelService aiModelService;

    private final AiApiKeyService keyService;

    public record AiModelContext(AiModel model, AiApiKey apiKey) {
    }

    /**
     * 根据所需的模型能力类型，获取一个可用的模型配置
     *
     * @param type 模型能力类型
     * @return 包含模型信息与Key的组合对象
     */
    public AiModelContext pickModelByType(String type) {
        List<AiModel> aiModels = aiModelService.listAiModelByType(type);

        if (CollectionUtil.isEmpty(aiModels)) {
            log.error("未找到类型为 [{}] 的可用模型，请检查 ai_model_info 表配置", type);
            throw new RuntimeException("系统繁忙，暂无该类型的可用 AI 模型: " + type);
        }

        AiModel aiModel = aiModels.get(ThreadLocalRandom.current().nextInt(aiModels.size()));
        AiApiKey apiKey = keyService.getNextAvailableKey(aiModel.getProvider());
        return new AiModelContext(aiModel, apiKey);
    }

}
