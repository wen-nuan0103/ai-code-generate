package com.xuenai.aicodegenerate.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 简介
     */
    private String profile;

    /**
     * 用户角色：user/admin
     */
    private String role;

    private static final long serialVersionUID = 1L;
}

