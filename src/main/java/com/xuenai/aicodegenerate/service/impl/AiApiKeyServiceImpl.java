package com.xuenai.aicodegenerate.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xuenai.aicodegenerate.mapper.AiApiKeyMapper;
import com.xuenai.aicodegenerate.model.entity.AiApiKey;
import com.xuenai.aicodegenerate.service.AiApiKeyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AiApiKeyServiceImpl extends ServiceImpl<AiApiKeyMapper, AiApiKey> implements AiApiKeyService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiApiKey getNextAvailableKey(String provider) {
        AiApiKey apiKey = queryChain()
                .where(AiApiKey::getProvider).eq(provider)
                .and(AiApiKey::getStatus).eq(1)
                .orderBy(AiApiKey::getLastUsedTime).asc() 
                .limit(1)
                .one();

        if (apiKey == null) {
            return null;
        }
        
        boolean update = updateChain()
                .set(AiApiKey::getLastUsedTime, LocalDateTime.now())
                .where(AiApiKey::getId).eq(apiKey.getId())
                .update();

        return apiKey;
    }
}
