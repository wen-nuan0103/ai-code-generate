create database if not exists `ai_auto_generate`;

use `ai_auto_generate`;

drop table if exists `user`;

create table `user`
(
    id              bigint auto_increment primary key comment "唯一",
    user_account    varchar(256) unique not null comment "账号",
    password        varchar(512)        not null comment "密码",
    user_name       varchar(256)                 DEFAULT null comment "用户名",
    avatar          varchar(1024)                DEFAULT null comment "头像",
    profile         varchar(1024)                DEFAULT null comment "个人简介",
    role            varchar(32)         not null comment "角色",
    vip_expire_time datetime                     DEFAULT null comment "会员过期时间",
    vip_code        varchar(128)                 DEFAULT null comment "会员兑换码",
    vip_number      bigint                       DEFAULT null comment "会员编号",
    share_code      varchar(20)                  DEFAULT NULL COMMENT "分享码",
    invite_user     bigint                       DEFAULT NULL COMMENT "邀请用户 id",
    edit_time       datetime            not null default current_timestamp comment "编辑时间",
    create_time     datetime            not null default current_timestamp comment "创建时间",
    update_time     datetime            not null default current_timestamp on update current_timestamp comment "更新时间",
    is_delete       tinyint             not null default 0 comment "是否删除",
    index idx_user_name (user_name)
) comment "用户表" collate = utf8mb4_unicode_ci;

-- 应用表
drop table if exists `app`;
create table app
(
    id                  bigint auto_increment comment 'id' primary key,
    app_name            varchar(256)                       null comment '应用名称',
    cover               varchar(512)                       null comment '应用封面',
    init_prompt         text                               null comment '应用初始化的 prompt',
    code_generator_type varchar(64)                        null comment '代码生成类型（枚举）',
    tags                varchar(1024)                      null comment '标签',
    deploy_key          varchar(64)                        null comment '部署标识',
    deploy_status       tinyint  default 0                 not null comment '部署状态（枚举 0:下线 1:上线 2:上线失败）',
    deployed_time       datetime                           null comment '部署时间',
    priority            int      default 0                 not null comment '优先级',
    user_id             bigint                             not null comment '创建用户id',
    scope_status        tinyint  default 0                 not null comment '可见范围状态（枚举 0:仅本人可见 1:全部可见）',
    current_status      tinyint  default 0                 not null comment '当前状态（枚举 0:生成中 1:生成完成 2:中断）',
    version             bigint   default 0                 not null comment '版本号',
    edit_time           datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    create_time         datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time         datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete           tinyint  default 0                 not null comment '是否删除',
    UNIQUE KEY uk_deployKey (deploy_key), -- 确保部署标识唯一
    INDEX idx_appName (app_name),         -- 提升基于应用名称的查询性能
    INDEX idx_userId (user_id)            -- 提升基于用户 ID 的查询性能
) comment '应用' collate = utf8mb4_unicode_ci;
