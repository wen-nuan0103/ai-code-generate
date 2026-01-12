package com.xuenai.aicodegenerate.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateByAdminRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String userName;
    private String avatar;
    private String profile;
    private String role;
    private Integer userStatus;
}
