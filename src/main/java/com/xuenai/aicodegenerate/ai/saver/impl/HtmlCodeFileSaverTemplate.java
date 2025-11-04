package com.xuenai.aicodegenerate.ai.saver.impl;

import cn.hutool.core.util.StrUtil;
import com.xuenai.aicodegenerate.ai.mode.HtmlCodeResult;
import com.xuenai.aicodegenerate.ai.saver.CodeFileSaverTemplate;
import com.xuenai.aicodegenerate.exception.BusinessException;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.model.enums.CodeGenerateTypeEnum;

/**
 * HTML 代码文件保存模板
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {
    @Override
    protected CodeGenerateTypeEnum getGeneratorType() {
        return CodeGenerateTypeEnum.HTML;
    }

    @Override
    protected void saveFiles(HtmlCodeResult result, String path) {
        writeToFile(path, "index.html", result.getHtml());
    }

    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);
        if (StrUtil.isBlank(result.getHtml())) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码不能为空");
    }
}
