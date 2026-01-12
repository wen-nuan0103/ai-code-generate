package com.xuenai.aicodegenerate.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xuenai.aicodegenerate.exception.BusinessException;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.exception.ThrowUtils;
import com.xuenai.aicodegenerate.mapper.AiModelMapper;
import com.xuenai.aicodegenerate.model.dto.ai.model.AiModelAddRequest;
import com.xuenai.aicodegenerate.model.dto.ai.model.AiModelQueryRequest;
import com.xuenai.aicodegenerate.model.dto.ai.model.AiModelUpdateRequest;
import com.xuenai.aicodegenerate.model.entity.AiModel;
import com.xuenai.aicodegenerate.model.vo.ai.AiModelVO;
import com.xuenai.aicodegenerate.service.AiModelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiModelServiceImpl extends ServiceImpl<AiModelMapper, AiModel> implements AiModelService {

    @Override
    public Page<AiModel> listAiModelByPage(AiModelQueryRequest request) {
        QueryWrapper queryWrapper = getQueryWrapper(request);
        return this.page(new Page<>(request.getCurrent(), request.getPageSize()), queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAiModel(AiModelAddRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        AiModel aiModel = new AiModel();
        BeanUtil.copyProperties(request, aiModel);

        if (aiModel.getStatus() == null) {
            aiModel.setStatus(1);
        }

        long count = this.queryChain()
                .where(AiModel::getModelCode).eq(aiModel.getModelCode())
                .and(AiModel::getModelType).eq(aiModel.getModelType())
                .count();
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该类型的模型编码已存在");
        }

        boolean save = this.save(aiModel);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAiModel(AiModelUpdateRequest request) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        AiModel aiModel = new AiModel();
        BeanUtil.copyProperties(request, aiModel);

        aiModel.setId(Long.valueOf(request.getId()));

        if (StrUtil.isNotBlank(request.getModelCode())) {
            long count = this.queryChain()
                    .where(AiModel::getModelCode).eq(request.getModelCode())
                    .and(AiModel::getModelType).eq(aiModel.getModelType()) 
                    .and(AiModel::getId).ne(aiModel.getId())
                    .count();
            ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "模型编码已存在");
        }

        return this.updateById(aiModel);
    }

    @Override
    public List<AiModelVO> listAiModelVO(String searchText) {
        List<AiModel> list = this.queryChain().where(AiModel::getStatus).eq(1).and(o_qw -> {
            o_qw.like(AiModel::getModelName, searchText).or(i_qw -> {
                i_qw.like(AiModel::getModelCode, searchText);
            });
        }).list();

        return list.stream().map(item -> {
            AiModelVO vo = new AiModelVO();
            BeanUtil.copyProperties(item, vo);
            vo.setId(String.valueOf(item.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取查询包装类
     */
    private QueryWrapper getQueryWrapper(AiModelQueryRequest request) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (request == null) {
            return queryWrapper;
        }

        String modelName = request.getModelName();
        String modelType = request.getModelType();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();

        queryWrapper.where(AiModel::getModelName).like(modelName).and(AiModel::getModelType).eq(modelType);

        // 拼接排序条件
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间倒序
            queryWrapper.orderBy(AiModel::getCreateTime, false);
        }

        return queryWrapper;
    }
}
