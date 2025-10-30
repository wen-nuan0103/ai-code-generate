package com.xuenai.aicodegenerate.ai.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.xuenai.aicodegenerate.exception.BusinessException;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.model.enums.CodeGeneratorTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 抽象文件代码保存器
 */
public abstract class CodeFileSaverTemplate<T> {

    protected static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 保存代码的标准流程
     *
     * @param result 代码
     * @return 代码文件对象
     */
    public final File saveCode(T result) {
        validateInput(result);
        String path = buildUniqueDir();
        saveFiles(result, path);
        return new File(path);
    }

    /**
     * 验证输入参数,生产的代码
     * 可由子类进行覆写
     *
     * @param result 代码
     */
    protected void validateInput(T result) {
        if (result == null) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码结果对象不能为空");
    }

    /**
     * 生成一个唯一的目录
     * tmp/code_output/bizType_slowly
     *
     * @return 唯一目录
     */
    protected final String buildUniqueDir() {
        String type = getGeneratorType().getValue();
        String unique = StrUtil.format("{}_{}",type , IdUtil.getSnowflakeNextId());
        String path = FILE_SAVE_ROOT_DIR + File.separator + unique;
        FileUtil.mkdir(path);
        return path;
    }

    /**
     * 将 AI 生成的代码保存为文件
     *
     * @param path     文件路径
     * @param fileName 文件名
     * @param content  AI 生成的代码
     */
    protected final void writeToFile(String path, String fileName, String content) {
        String filePath = path + File.separator + fileName;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }

    /**
     * 获取生产代码类型,由具体的子类进行实习
     *
     * @return 代码生产类型
     */
    protected abstract CodeGeneratorTypeEnum getGeneratorType();

    /**
     * 保存文件
     *
     * @param result 代码
     * @param path   保存路径
     */
    protected abstract void saveFiles(T result, String path);

}
