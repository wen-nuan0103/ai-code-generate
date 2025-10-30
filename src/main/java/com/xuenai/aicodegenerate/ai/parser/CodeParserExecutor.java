package com.xuenai.aicodegenerate.ai.parser;

import com.xuenai.aicodegenerate.ai.parser.impl.HtmlCodeParser;
import com.xuenai.aicodegenerate.ai.parser.impl.MultiFileCodeParser;
import com.xuenai.aicodegenerate.exception.BusinessException;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.model.enums.CodeGeneratorTypeEnum;

/**
 * 代码解析执行器
 */
public class CodeParserExecutor {

    private static final HtmlCodeParser HTML_CODE_PARSER = new HtmlCodeParser();
    private static final MultiFileCodeParser MULTI_FILE_CODE_PARSER = new MultiFileCodeParser();

    /**
     * 执行代码解析
     * 
     * @param code 代码
     * @param generatorType 解析类型
     * @return 对应解析类型的结果集
     */
    public static Object executeParser(String code, CodeGeneratorTypeEnum generatorType) {
        return switch (generatorType) {
          case HTML -> HTML_CODE_PARSER.parserCode(code);
          case MULTI_FILE -> MULTI_FILE_CODE_PARSER.parserCode(code);
          default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,"暂不支持该类型: " + generatorType);
        };
    }

}
