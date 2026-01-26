package com.xuenai.aicodegenerate.controller;

import com.mybatisflex.core.paginate.Page;
import com.xuenai.aicodegenerate.common.BaseResponse;
import com.xuenai.aicodegenerate.common.ResultUtils;
import com.xuenai.aicodegenerate.model.dto.ai.log.AiLogQueryRequest;
import com.xuenai.aicodegenerate.model.vo.ai.AiGenerateLogVO;
import com.xuenai.aicodegenerate.service.AiGenerationLogService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 日志控制器
 */
@RestController
@RequestMapping("/ai/log")
public class AiLogController {

    @Resource
    private AiGenerationLogService aiGenerationLogService;

    /**
     * 分页查询 AI 生成日志
     *
     * @param request 查询条件
     * @return 分页列表
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<AiGenerateLogVO>> listAiGenerateLogByPage(@RequestBody AiLogQueryRequest request) {
        Page<AiGenerateLogVO> page = aiGenerationLogService.listAiGenerateLogByPage(request);
        return ResultUtils.success(page);
    }
}
