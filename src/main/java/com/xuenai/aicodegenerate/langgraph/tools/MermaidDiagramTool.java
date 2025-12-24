package com.xuenai.aicodegenerate.langgraph.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.xuenai.aicodegenerate.exception.BusinessException;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.langgraph.model.dto.ImageResource;
import com.xuenai.aicodegenerate.langgraph.model.enums.ImageCategoryEnum;
import com.xuenai.aicodegenerate.manager.CosManager;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class MermaidDiagramTool {

    @Resource
    private CosManager cosManager;

    @Tool("将 Mermaid 代码转换为架构图图片，用于展示系统架构和技术关系")
    public List<ImageResource> generateArchitectureDiagram(@P("Mermaid 图表代码") String mermaidCode, @P("架构图描述") String description) {
        if (StrUtil.isBlank(mermaidCode)) {
            return new ArrayList<>();
        }

        try {
            File diagramFile = convertMermaidToSvg(mermaidCode);
            String keyName = String.format("/mermaid/%s/%s", RandomUtil.randomString(5), diagramFile.getName());
            String url = cosManager.uploadFile(keyName, diagramFile);
            FileUtil.del(diagramFile);
            if (StrUtil.isBlank(url)) {
                return Collections.singletonList(ImageResource.builder().category(ImageCategoryEnum.ARCHITECTURE).description(description).url(url).build());
            }
        } catch (Exception e) {
            log.error("架构图生成失败: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
    }


    /**
     * 将 Mermaid 代码转换为 SVG 文件
     *
     * @param mermaidCode Mermaid 代码
     * @return SVG 文件
     */
    private File convertMermaidToSvg(String mermaidCode) {
        File tempInput = FileUtil.createTempFile("mermaid_input", ".mmd", true);
        FileUtil.writeUtf8String(mermaidCode, tempInput);
        File tempOutput = FileUtil.createTempFile("mermaid_output", ".svg", true);
        String command = SystemUtil.getOsInfo().isWindows() ? "mmdc.cmd" : "mmdc";
        String[] cmdArray = new String[] {
                command,
                "-i", tempInput.getAbsolutePath(),
                "-o", tempOutput.getAbsolutePath(),
                "-b", "transparent"
        };
        try {
            Process process = RuntimeUtil.exec(null, cmdArray);
            String error = RuntimeUtil.getErrorResult(process);
            if (!tempOutput.exists() || tempOutput.length() == 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Mermaid CLI 生成失败，原因: " + (StrUtil.isNotBlank(error) ? error : "未知错误，请查看日志"));
            }

            return tempOutput;

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Mermaid 转换服务异常: " + e.getMessage());
        } finally {
            FileUtil.del(tempInput);
        }
    }
}
