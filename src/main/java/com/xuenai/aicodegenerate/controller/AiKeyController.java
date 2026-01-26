package com.xuenai.aicodegenerate.controller;

import com.mybatisflex.core.paginate.Page;
import com.xuenai.aicodegenerate.annotation.AuthCheck;
import com.xuenai.aicodegenerate.common.BaseResponse;
import com.xuenai.aicodegenerate.common.ResultUtils;
import com.xuenai.aicodegenerate.constant.UserConstant;
import com.xuenai.aicodegenerate.exception.BusinessException;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.exception.ThrowUtils;
import com.xuenai.aicodegenerate.model.dto.ai.key.AiKeyAddRequest;
import com.xuenai.aicodegenerate.model.dto.ai.key.AiKeyQueryRequest;
import com.xuenai.aicodegenerate.model.vo.ai.AiKeyVO;
import com.xuenai.aicodegenerate.service.AiApiKeyService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/key")
public class AiKeyController {

    @Resource
    private AiApiKeyService aiApiKeyService;

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AiKeyVO>> listAiApiKeyByPage(@RequestBody AiKeyQueryRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<AiKeyVO> result = aiApiKeyService.listAiApiKeyVOByPage(request);
        return ResultUtils.success(result);
    }


    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Void> addAiApiKey(@RequestBody AiKeyAddRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = aiApiKeyService.addAiApiKey(request);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "添加 Key 失败");
        return ResultUtils.success(null);
    }

}
