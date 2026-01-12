package com.xuenai.aicodegenerate.controller;

import com.mybatisflex.core.paginate.Page;
import com.xuenai.aicodegenerate.annotation.AuthCheck;
import com.xuenai.aicodegenerate.common.BaseResponse;
import com.xuenai.aicodegenerate.common.DeleteRequest;
import com.xuenai.aicodegenerate.common.ResultUtils;
import com.xuenai.aicodegenerate.constant.UserConstant;
import com.xuenai.aicodegenerate.exception.BusinessException;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.exception.ThrowUtils;
import com.xuenai.aicodegenerate.model.dto.ai.model.AiModelAddRequest;
import com.xuenai.aicodegenerate.model.dto.ai.model.AiModelQueryRequest;
import com.xuenai.aicodegenerate.model.dto.ai.model.AiModelUpdateRequest;
import com.xuenai.aicodegenerate.model.entity.AiModel;
import com.xuenai.aicodegenerate.model.vo.ai.AiModelVO;
import com.xuenai.aicodegenerate.service.AiModelService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai/model")
public class AiModelController {

    @Resource
    private AiModelService aiModelService;
    
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AiModel>> listAiModelByPage(@RequestBody AiModelQueryRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<AiModel> result = aiModelService.listAiModelByPage(request);
        return ResultUtils.success(result);
    }
    
    
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Void> addAiModel(@RequestBody AiModelAddRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        aiModelService.addAiModel(request);
        return ResultUtils.success(null);
    }
    
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAiModel(@RequestBody AiModelUpdateRequest request) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = aiModelService.updateAiModel(request);
        return ResultUtils.success(result);
    }
    
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAiModel(@RequestBody DeleteRequest request) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = aiModelService.removeById(request.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
    
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AiModel> getAiModelById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AiModel aiModel = aiModelService.getById(id);
        ThrowUtils.throwIf(aiModel == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(aiModel);
    }
    
    @PostMapping("/list/vo")
    public BaseResponse<List<AiModelVO>> listAiModelVO(String modelName) {
        List<AiModelVO> list = aiModelService.listAiModelVO(modelName);
        return ResultUtils.success(list);
    }
}
