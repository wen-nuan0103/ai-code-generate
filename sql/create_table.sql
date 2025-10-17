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

