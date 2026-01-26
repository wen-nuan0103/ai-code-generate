package com.xuenai.aicodegenerate.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.xuenai.aicodegenerate.model.dto.ai.log.AiGenerateLogDTO;
import com.xuenai.aicodegenerate.model.dto.ai.log.AiLogQueryRequest;
import com.xuenai.aicodegenerate.model.entity.AiGenerationLog;
import com.xuenai.aicodegenerate.model.vo.ai.AiGenerateLogVO;

public interface AiGenerationLogService extends IService<AiGenerationLog> {

    /**
     * 保存AI生成日志
     *
     * @param logDTO dto
     */
    void saveAiGenerationLog(AiGenerateLogDTO logDTO);

    /**
     * 获取 AI 生成日志分页列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    Page<AiGenerateLogVO> listAiGenerateLogByPage(AiLogQueryRequest request);
}
