package com.xuenai.aicodegenerate.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xuenai.aicodegenerate.mapper.AiGenerationLogMapper;
import com.xuenai.aicodegenerate.model.dto.ai.log.AiGenerateLogDTO;
import com.xuenai.aicodegenerate.model.dto.ai.log.AiLogQueryRequest;
import com.xuenai.aicodegenerate.model.entity.AiGenerationLog;
import com.xuenai.aicodegenerate.model.vo.ai.AiGenerateLogVO;
import com.xuenai.aicodegenerate.service.AiGenerationLogService;
import org.springframework.stereotype.Service;

@Service
public class AiGenerationLogServiceImpl extends ServiceImpl<AiGenerationLogMapper, AiGenerationLog>
        implements AiGenerationLogService {
    @Override
    public void saveAiGenerationLog(AiGenerateLogDTO logDTO) {
        AiGenerationLog log = new AiGenerationLog();
        BeanUtil.copyProperties(logDTO, log);
        this.save(log);
    }

    @Override
    public Page<AiGenerateLogVO> listAiGenerateLogByPage(AiLogQueryRequest request) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq("trace_id", request.getTraceId())
                .eq("status", request.getStatus());
        Page<AiGenerationLog> page = this
                .page(new Page<>(request.getCurrent(), request.getPageSize()), queryWrapper);
        
        Page<AiGenerateLogVO> voPage = new Page<>(
                page.getPageNumber(), page.getPageSize(), page.getTotalRow());
        java.util.List<AiGenerateLogVO> voList = page.getRecords().stream()
                .map(log -> {
                    AiGenerateLogVO vo = new AiGenerateLogVO();
                    BeanUtil.copyProperties(log, vo);
                    return vo;
                }).collect(java.util.stream.Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }
}
