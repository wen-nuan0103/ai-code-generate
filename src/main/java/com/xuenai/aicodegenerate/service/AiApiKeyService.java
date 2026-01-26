package com.xuenai.aicodegenerate.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.xuenai.aicodegenerate.model.dto.ai.key.AiKeyAddRequest;
import com.xuenai.aicodegenerate.model.dto.ai.key.AiKeyQueryRequest;
import com.xuenai.aicodegenerate.model.entity.AiApiKey;
import com.xuenai.aicodegenerate.model.vo.ai.AiKeyVO;

public interface AiApiKeyService extends IService<AiApiKey> {

    /**
     * 轮询算法：获取下一个 key
     */
    AiApiKey getNextAvailableKey(String provider);

    /**
     * 分页查询
     *
     * @param request 查询条件
     * @return
     */
    Page<AiKeyVO> listAiApiKeyVOByPage(AiKeyQueryRequest request);

    /**
     * 添加模型 KeY
     *
     * @param request 添加请求 DTO
     */
    boolean addAiApiKey(AiKeyAddRequest request);
}
