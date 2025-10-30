package com.xuenai.aicodegenerate.ai.saver;

import com.xuenai.aicodegenerate.ai.mode.HtmlCodeResult;
import com.xuenai.aicodegenerate.ai.mode.MultiFileCodeResult;
import com.xuenai.aicodegenerate.ai.saver.impl.HtmlCodeFileSaverTemplate;
import com.xuenai.aicodegenerate.ai.saver.impl.MultiFileCodeSaverTemplate;
import com.xuenai.aicodegenerate.exception.BusinessException;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.model.enums.CodeGeneratorTypeEnum;

import java.io.File;

/**
 * 代码保存模版执行器
 */
public class CodeFileSaverExecutor {
    
    private static final HtmlCodeFileSaverTemplate HTML_CODE_FILE_SAVER_TEMPLATE = new HtmlCodeFileSaverTemplate();
    private static final MultiFileCodeSaverTemplate MULTI_FILE_CODE_SAVER_TEMPLATE = new MultiFileCodeSaverTemplate();

    /**
     * 执行代码保存
     * 
     * @param code 代码
     * @param generatorType 保存类型
     * @return 保存的文件
     */
    public static File executorSaverCode(Object code, CodeGeneratorTypeEnum generatorType) {
        return switch (generatorType) {
            case HTML -> HTML_CODE_FILE_SAVER_TEMPLATE.saveCode((HtmlCodeResult) code);
            case MULTI_FILE -> MULTI_FILE_CODE_SAVER_TEMPLATE.saveCode((MultiFileCodeResult) code);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,"暂不支持该类型: " + generatorType);
        };
    }
    
}
