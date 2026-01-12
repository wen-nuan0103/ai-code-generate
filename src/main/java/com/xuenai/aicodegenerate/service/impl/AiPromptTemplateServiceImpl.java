package com.xuenai.aicodegenerate.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xuenai.aicodegenerate.mapper.AiPromptTemplateMapper;
import com.xuenai.aicodegenerate.model.entity.AiPromptTemplate;
import com.xuenai.aicodegenerate.service.AiPromptTemplateService;
import org.springframework.stereotype.Service;

@Service
public class AiPromptTemplateServiceImpl extends ServiceImpl<AiPromptTemplateMapper, AiPromptTemplate> implements AiPromptTemplateService {
}