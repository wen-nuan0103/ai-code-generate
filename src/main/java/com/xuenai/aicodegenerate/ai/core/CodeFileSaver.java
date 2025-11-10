package com.xuenai.aicodegenerate.ai.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.xuenai.aicodegenerate.ai.mode.result.HtmlCodeResult;
import com.xuenai.aicodegenerate.ai.mode.result.MultiFileCodeResult;
import com.xuenai.aicodegenerate.model.enums.CodeGenerateTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 文件写入工具类
 */
@Deprecated
public class CodeFileSaver {
    
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";


    /**
     * 保存 AI 生成的 HTML 代码
     * 
     * @param codeResult AI 生成的 HTML 代码
     * @return 保存的文件
     */
    public static File saveHtmlCodeResult(HtmlCodeResult codeResult){
        String basePath = buildUniqueDir(CodeGenerateTypeEnum.HTML.getValue());
        writeToFile(basePath, "index.html", codeResult.getHtml());
        return new File(basePath);
    }

    /**
     * 保存 AI 生成的多文件代码
     * 
     * @param codeResult AI 生成的多文件代码
     * @return 保存的文件路径
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult codeResult){
        String basePath = buildUniqueDir(CodeGenerateTypeEnum.MULTI_FILE.getValue());
        writeToFile(basePath, "index.html", codeResult.getHtml());
        writeToFile(basePath, "style.css", codeResult.getCss());
        writeToFile(basePath, "script.js", codeResult.getJavaScript());
        return new File(basePath);
    }
    
    /**
     * 生成一个唯一的目录
     * tmp/code_output/bizType_slowly
     * 
     * @param bizType 业务类型
     * @return 唯一目录
     */
    private static String buildUniqueDir(String bizType){
        String unique = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextId());
        String path = FILE_SAVE_ROOT_DIR + File.separator + unique;
        FileUtil.mkdir(path);
        return path;
    }
    
    
    /**
     * 将 AI 生成的代码保存为文件
     * 
     * @param path 文件路径
     * @param fileName 文件名
     * @param content  AI 生成的代码
     */
    private static void writeToFile(String path,String fileName, String content) {
        String filePath = path + File.separator + fileName;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }
    
}
