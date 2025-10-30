package com.xuenai.aicodegenerate.ai.parser;

/**
 * 通用的代码解析器
 *
 * @param <T> 解析类型
 */
public interface CodeParser<T> {

    /**
     * 解析代码
     *
     * @param codeContent 代码
     * @return 解析后的对象
     */
    T parserCode(String codeContent);

}
