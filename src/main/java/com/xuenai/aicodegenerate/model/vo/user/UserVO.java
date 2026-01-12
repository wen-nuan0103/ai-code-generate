package com.xuenai.aicodegenerate.model.vo.user;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户简介
     */
    private String profile;

    /**
     * 用户角色：user/admin
     */
    private String role;

    /**
     * 是否为VIP用户
     */
    private Boolean isVip;

    /**
     * 用户状态：0-正常，1-禁用
     */
    private Integer userStatus;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    

    private static final long serialVersionUID = 1L;
}

