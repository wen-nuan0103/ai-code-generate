package com.xuenai.aicodegenerate.model.vo.app;

import com.xuenai.aicodegenerate.model.vo.user.UserVO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 应用视图对象
 *
 * @author 小菜
 */
@Data
public class AppVO implements Serializable {

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
    private List<String> tags;

    /**
     * 部署标识
     */
    private String deployKey;

    /**
     * 部署状态（枚举 0:下线 1:上线 2:上线失败）
     */
    private Integer deployStatus;

    /**
     * 部署时间
     */
    private LocalDateTime deployedTime;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 可见范围状态（枚举 0:仅本人可见 1:全部可见）
     */
    private Integer scopeStatus;

    /**
     * 可见范围状态（枚举 0:仅本人可见 1:全部可见）
     */
    private Integer chatScopeStatus;

    /**
     * 当前状态（枚举 0:生成中 1:生成完成 2:中断）
     */
    private Integer currentStatus;

    /**
     * 版本号
     */
    private Long version;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建用户信息
     */
    private UserVO user;

    private static final long serialVersionUID = 1L;
}
