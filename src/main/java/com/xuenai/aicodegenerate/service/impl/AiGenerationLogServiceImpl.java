package com.xuenai.aicodegenerate.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xuenai.aicodegenerate.mapper.AiGenerationLogMapper;
import com.xuenai.aicodegenerate.model.entity.AiGenerationLog;
import com.xuenai.aicodegenerate.service.AiGenerationLogService;
import org.springframework.stereotype.Service;

@Service
public class AiGenerationLogServiceImpl extends ServiceImpl<AiGenerationLogMapper, AiGenerationLog> implements AiGenerationLogService {
}
