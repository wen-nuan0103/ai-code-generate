package com.xuenai.aicodegenerate.service;

import com.mybatisflex.core.service.IService;
import com.xuenai.aicodegenerate.model.entity.AiApiKey;

public interface AiApiKeyService extends IService<AiApiKey> {

    /**
     * 轮询算法：获取下一个 key 
     */
    AiApiKey getNextAvailableKey(String provider);
    
}
