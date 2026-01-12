package com.xuenai.aicodegenerate.model.dto.app;

import com.xuenai.aicodegenerate.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询应用请求
 *
 * @author 小菜
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppQueryRequest extends PageRequest implements Serializable {


    private static final long serialVersionUID = 1L;
    
    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面
     */
    private String cover;

    /**
     * 应用初始化的 prompt
     */
    private String initPrompt;

    /**
     * 代码生成类型（枚举）
     */
    private String codeGeneratorType;
    
    /**
     * 标签
     */
    private String tags;

    /**
     * 部署标识
     */
    private String deployKey;
    
    /**
     * 部署状态（枚举 0:下线 1:上线 2:上线失败）
     */
    private Integer deployStatus;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 当前状态（枚举 0:生成中 1:生成完成 2:中断）
     */
    private Integer currentStatus;

    /**
     * 版本号
     */
    private Long version;

}
