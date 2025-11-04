package com.xuenai.aicodegenerate.ai.core;

import com.xuenai.aicodegenerate.ai.AiCodeGenerateService;
import com.xuenai.aicodegenerate.ai.mode.HtmlCodeResult;
import com.xuenai.aicodegenerate.ai.mode.MultiFileCodeResult;
import com.xuenai.aicodegenerate.ai.mode.ProjectInfoResult;
import com.xuenai.aicodegenerate.ai.parser.CodeParserExecutor;
import com.xuenai.aicodegenerate.ai.saver.CodeFileSaverExecutor;
import com.xuenai.aicodegenerate.exception.BusinessException;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.model.enums.CodeGenerateTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存的功能
 */
@Slf4j
@Service
public class AiCodeGenerateFacade {

    @Resource
    private AiCodeGenerateService aiCodeGenerateService;

    /**
     * 统一对外提供的方法，生成并保存文件
     *
     * @param userMessage   提示词
     * @param generatorType 生成文件类型
     * @param appId         应用 ID
     * @return 生成文件
     */
    public File generateAndSaveCode(String userMessage, CodeGenerateTypeEnum generatorType, Long appId) {
        if (generatorType == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        return switch (generatorType) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGenerateService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executorSaverCode(result, generatorType, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGenerateService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executorSaverCode(result, generatorType, appId);
            }
            default -> {
                String errorMessage = "不支持生成该类型: " + generatorType;
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一对外提供的方法，生成并保存文件（流式）
     *
     * @param userMessage 提示词
     * @param typeEnum    生成文件类型
     * @param appId       应用 ID
     * @return 生成文件
     */
    public Flux<String> generateStreamAndSaveCode(String userMessage, CodeGenerateTypeEnum typeEnum, Long appId) {
        if (typeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        return switch (typeEnum) {
            case HTML -> {
                Flux<String> result = aiCodeGenerateService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(result, typeEnum, appId);
            }
            case MULTI_FILE -> {
                Flux<String> result = aiCodeGenerateService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(result, typeEnum, appId);
            }
            default -> {
                String errorMessage = "不支持生成该类型: " + typeEnum;
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
            }
        };
    }
    
    /**
     * 生成项目信息
     *
     * @param userMessage 提示词
     * @return 项目信息
     */
    public ProjectInfoResult generateProjectInfo(String userMessage) {
        return aiCodeGenerateService.generateProjectInfo(userMessage);
    }

    /**
     * 通用流式代码处理
     *
     * @param codeStream    流式代码
     * @param generatorType 生成类型
     * @param appId         应用 ID
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenerateTypeEnum generatorType, Long appId) {
        StringBuilder builder = new StringBuilder();
        return codeStream.doOnNext(builder::append).doOnComplete(() -> {
            try {
                String result = builder.toString();
                Object parserResult = CodeParserExecutor.executeParser(result, generatorType);
                File file = CodeFileSaverExecutor.executorSaverCode(parserResult, generatorType,appId);
                log.info("代码保存成功: {}", file.getAbsolutePath());
            } catch (Exception e) {
                log.error("代码保存失败: {}", e.getMessage());
            }
        });
    }

}
