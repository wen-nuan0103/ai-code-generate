package com.xuenai.aicodegenerate.ai.saver.impl;

import cn.hutool.core.util.StrUtil;
import com.xuenai.aicodegenerate.ai.mode.MultiFileCodeResult;
import com.xuenai.aicodegenerate.ai.saver.CodeFileSaverTemplate;
import com.xuenai.aicodegenerate.exception.BusinessException;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.model.enums.CodeGeneratorTypeEnum;

/**
 * 多文件代码生成器模板
 */
public class MultiFileCodeSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {
    @Override
    protected CodeGeneratorTypeEnum getGeneratorType() {
        return CodeGeneratorTypeEnum.MULTI_FILE;
    }

    @Override
    protected void saveFiles(MultiFileCodeResult result, String path) {
        writeToFile(path, "index.html", result.getHtml());
        writeToFile(path, "style.css", result.getCss());
        writeToFile(path, "script.js", result.getJavaScript());
    }

    @Override
    protected void validateInput(MultiFileCodeResult result) {
        super.validateInput(result);
        if (StrUtil.isBlank(result.getHtml())) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码不能为空");
    }
}
