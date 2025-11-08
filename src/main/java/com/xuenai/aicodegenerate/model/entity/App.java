package com.xuenai.aicodegenerate.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 应用 实体类。
 *
 * @author 小菜
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("app")
public class App implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Generator,value = KeyGenerators.snowFlakeId)
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
    @Column(typeHandler = JacksonTypeHandler.class)
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
     * 聊天可见范围状态（枚举 0:仅本人可见 1:全部可见）
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
     * 编辑时间
     */
    private LocalDateTime editTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(isLogicDelete = true)
    private Integer isDelete;

}
