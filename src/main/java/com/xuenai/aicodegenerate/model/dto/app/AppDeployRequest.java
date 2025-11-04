package com.xuenai.aicodegenerate.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 部署应用请求
 *
 * @author 小菜
 */
@Data
public class AppDeployRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long appId;

    private static final long serialVersionUID = 1L;
}

