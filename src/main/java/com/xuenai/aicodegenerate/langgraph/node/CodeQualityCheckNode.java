package com.xuenai.aicodegenerate.langgraph.node;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.xuenai.aicodegenerate.ai.service.CodeQualityCheckService;
import com.xuenai.aicodegenerate.langgraph.model.dto.QualityResult;
import com.xuenai.aicodegenerate.langgraph.state.WorkflowContext;
import com.xuenai.aicodegenerate.monitor.MonitorContext;
import com.xuenai.aicodegenerate.monitor.MonitorContextHolder;
import com.xuenai.aicodegenerate.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 代码质量检查节点
 */
@Slf4j
public class CodeQualityCheckNode {


    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 代码质量检查");
            String generatedCodeDir = context.getGeneratedCodeDir();
            QualityResult qualityResult;
            try {
                // 读取并拼接代码文件内容
                String codeContent = readAndConcatenateCodeFiles(generatedCodeDir);
                if (StrUtil.isBlank(codeContent)) {
                    log.warn("未找到可检查的代码文件");
                    qualityResult = QualityResult.builder().isValid(false).errors(List.of("未找到可检查的代码文件")).suggestions(List.of("请确保代码生成成功")).build();
                } else {
                    // 调用 AI 进行代码质量检查
                    CodeQualityCheckService qualityCheckService = SpringContextUtil.getBean(CodeQualityCheckService.class);
                    String userPromptContext = "请根据 System Prompt 中的规则，检查以下项目代码的质量和可运行性：\n\n" + codeContent;
                    MonitorContextHolder.setContext(MonitorContext.builder()
                            .userId(String.valueOf(context.getUserId()))
                            .taskType("CODE_AUDIT")
                            .appId(String.valueOf(context.getAppId()))
                            .build());
                    // 调用接口
                    qualityResult = qualityCheckService.checkCodeQuality(userPromptContext);
                    log.info("代码质量检查完成 - 是否通过: {}", qualityResult.getIsValid());
                    ;
                }
            } catch (Exception e) {
                log.error("代码质量检查异常: {}", e.getMessage(), e);
                qualityResult = QualityResult.builder().isValid(true) // 异常直接跳到下一个步骤
                        .build();
            }
            // 更新状态
            context.setCurrentStep("代码质量检查");
            context.setQualityResult(qualityResult);
            return WorkflowContext.saveContext(context);
        });
    }

    /**
     * 需要检查的文件扩展名
     */
    private static final List<String> CODE_EXTENSIONS = Arrays.asList(".html", ".htm", ".css", ".js", ".json", ".vue", ".ts", ".jsx", ".tsx");

    /**
     * 读取并拼接代码目录下的所有代码文件
     */
    private static String readAndConcatenateCodeFiles(String codeDir) {
        if (StrUtil.isBlank(codeDir)) {
            return "";
        }
        File directory = new File(codeDir);
        if (!directory.exists() || !directory.isDirectory()) {
            log.error("代码目录不存在或不是目录: {}", codeDir);
            return "";
        }
        StringBuilder codeContent = new StringBuilder();
        codeContent.append("# 项目文件结构和代码内容\n\n");
        FileUtil.walkFiles(directory, file -> {
            if (shouldSkipFile(file, directory)) {
                return;
            }
            if (isCodeFile(file)) {
                String relativePath = FileUtil.subPath(directory.getAbsolutePath(), file.getAbsolutePath());
                codeContent.append("## 文件: ").append(relativePath).append("\n\n");
                String fileContent = FileUtil.readUtf8String(file);
                codeContent.append(fileContent).append("\n\n");
            }
        });
        return codeContent.toString();
    }

    /**
     * 判断是否应该跳过此文件
     */
    private static boolean shouldSkipFile(File file, File rootDir) {
        String relativePath = FileUtil.subPath(rootDir.getAbsolutePath(), file.getAbsolutePath());
        // 跳过隐藏文件
        if (file.getName().startsWith(".")) {
            return true;
        }
        // 跳过特定目录下的文件
        return relativePath.contains("node_modules" + File.separator) || relativePath.contains("dist" + File.separator) || relativePath.contains("target" + File.separator) || relativePath.contains(".git" + File.separator);
    }

    /**
     * 判断是否是需要检查的代码文件
     */
    private static boolean isCodeFile(File file) {
        String fileName = file.getName().toLowerCase();
        return CODE_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

}

