package com.xuenai.aicodegenerate.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static com.xuenai.aicodegenerate.constant.AppConstant.CODE_OUTPUT_ROOT_DIR;

/**
 * 文件写入工具类
 */
@Slf4j
@Component
public class FileWriteTool extends BaseTool {

    /**
     * 写入文件到指定路径
     *
     * @param relativePath 文件的相对路径
     * @param content      写入文件的具体内容
     * @param appId        应用 ID,通过LangChain4J上下文传参
     * @return 文件路径
     */
    @Tool(name = "writeFile", value = "写入文件到指定路径")
    public String writeFile(@P(value = "文件的相对路径") String relativePath, @P(value = "要写入文件的内容") String content, @ToolMemoryId Long appId) {
        try {
            Path path = Paths.get(relativePath);
            if (!path.isAbsolute()) {
                String projectDirName = getProjectDirName(appId);
                Path projectRoot = Paths.get(CODE_OUTPUT_ROOT_DIR, projectDirName);
                path = projectRoot.resolve(relativePath);
            }
            Path parentPath = path.getParent();
            if (parentPath != null) {
                Files.createDirectories(parentPath);
            }
            Files.write(path, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("成功写入文件: {}", path.toAbsolutePath());
            return "文件写入成功: " + relativePath;
        } catch (Exception e) {
            String errorMessage = "文件写入失败: " + relativePath + ", 错误信息: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    @Override
    public String getToolName() {
        return "writeFile";
    }

    @Override
    public String getDisplayName() {
        return "文件读取";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        String suffix = FileUtil.getSuffix(relativeFilePath);
        String content = arguments.getStr("content");
        return String.format("""
                [工具调用] %s %s
                ```%s
                %s
                ```
                """, getDisplayName(), relativeFilePath, suffix, content);
    }
}
