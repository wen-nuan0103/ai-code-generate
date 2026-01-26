package com.xuenai.aicodegenerate.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xuenai.aicodegenerate.exception.BusinessException;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.mapper.AiApiKeyMapper;
import com.xuenai.aicodegenerate.model.dto.ai.key.AiKeyAddRequest;
import com.xuenai.aicodegenerate.model.dto.ai.key.AiKeyQueryRequest;
import com.xuenai.aicodegenerate.model.entity.AiApiKey;
import com.xuenai.aicodegenerate.model.vo.ai.AiKeyVO;
import com.xuenai.aicodegenerate.service.AiApiKeyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public Page<AiKeyVO> listAiApiKeyVOByPage(AiKeyQueryRequest request) {
        QueryWrapper queryWrapper = getQueryWrapper(request);
        Page<AiApiKey> page = this.page(new Page<>(request.getCurrent(), request.getPageSize()), queryWrapper);
        List<AiKeyVO> list = page.getRecords().stream().map(item -> {
            AiKeyVO vo = new AiKeyVO();
            BeanUtil.copyProperties(item, vo);
            return vo;
        }).toList();
        Page<AiKeyVO> result = new Page<>();
        result.setRecords(list);
        result.setPageNumber(page.getPageNumber());
        result.setPageSize(page.getPageSize());
        result.setTotalPage(page.getTotalPage());
        result.setTotalRow(page.getTotalRow());
        return result;
    }

    @Override
    public boolean addAiApiKey(AiKeyAddRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String keys = request.getAccessKey();
        if (StrUtil.isBlank(keys)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "必要参数不能为空");
        }
        
        String[] keyArr = keys.split("\n");
        List<AiApiKey> list = new ArrayList<>();
        
        for (String key : keyArr) {
            AiApiKey aiApiKey = new AiApiKey();
            BeanUtil.copyProperties(request, aiApiKey);

            if (aiApiKey.getStatus() == null) {
                aiApiKey.setStatus(1);
            }

            long count = this.queryChain()
                    .where(AiApiKey::getProvider).eq(request.getProvider())
                    .and(AiApiKey::getAccessKey).eq(key)
                    .count();
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "key已存在");
            }
            list.add(aiApiKey);
        }

        
        return this.saveBatch(list);
    }

    /**
     * 获取查询包装类
     */
    private QueryWrapper getQueryWrapper(AiKeyQueryRequest request) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (request == null) {
            return queryWrapper;
        }

        String provider = request.getProvider();
        String key = request.getKey();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();

        queryWrapper.where(AiApiKey::getProvider).like(provider).and(AiApiKey::getAccessKey).likeRight(key);

        // 拼接排序条件
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间倒序
            queryWrapper.orderBy(AiApiKey::getCreateTime, false);
        }

        return queryWrapper;
    }
}
