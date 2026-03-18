/*
 Navicat Premium Dump SQL

 Source Server         : localhost_13306
 Source Server Type    : MySQL
 Source Server Version : 80032 (8.0.32)
 Source Host           : localhost:13306
 Source Schema         : ai_auto_generate

 Target Server Type    : MySQL
 Target Server Version : 80032 (8.0.32)
 File Encoding         : 65001

 Date: 18/03/2026 20:01:34
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE `ai_auto_generate`;
USE `ai_auto_generate`;

-- ----------------------------
-- Table structure for ai_api_key
-- ----------------------------
DROP TABLE IF EXISTS `ai_api_key`;
CREATE TABLE `ai_api_key`  (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `provider` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '对应供应商 (OpenAI/DeepSeek)',
                               `access_key` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'sk-xxxxxx',
                               `balance` decimal(10, 4) NULL DEFAULT NULL COMMENT '剩余额度(可选)',
                               `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1-正常 0-停用 -1-余额耗尽',
                               `last_used_time` datetime NULL DEFAULT NULL COMMENT '上次使用时间(用于轮询算法)',
                               `error_count` int NULL DEFAULT 0 COMMENT '连续报错次数',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`) USING BTREE,
                               INDEX `idx_provider`(`provider` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 391839391803408385 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = 'API密钥管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_generation_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_generation_log`;
CREATE TABLE `ai_generation_log`  (
                                      `id` bigint NOT NULL AUTO_INCREMENT,
                                      `trace_id` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
                                      `user_id` bigint NOT NULL,
                                      `task_type` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '任务类型',
                                      `model_name` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '当时使用的模型',
                                      `input_tokens` int NULL DEFAULT 0,
                                      `output_tokens` int NULL DEFAULT 0,
                                      `total_cost` decimal(10, 6) NULL DEFAULT NULL COMMENT '计算出的成本',
                                      `status` tinyint NULL DEFAULT NULL COMMENT '1-成功 2-失败',
                                      `error_msg` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '报错信息',
                                      `duration` bigint NULL DEFAULT NULL COMMENT '耗时(ms)',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      INDEX `idx_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 391845533250105345 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = 'AI生成日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_model_info
-- ----------------------------
DROP TABLE IF EXISTS `ai_model_info`;
CREATE TABLE `ai_model_info`  (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `model_name` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '展示名称',
                                  `model_code` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '调用传参值',
                                  `model_type` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'CHAT' COMMENT '模型类型: CHAT-通用对话, CODE-代码生成, CODE_AUDIT-代码审计, IMAGE_COLLECT-图片收集, EMBEDDING-向量化',
                                  `provider` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '供应商',
                                  `base_url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '接口地址 (支持反代地址)',
                                  `max_tokens` int NULL DEFAULT 4096 COMMENT '最大上下文窗口',
                                  `input_price` decimal(10, 8) NULL DEFAULT 0.00000000 COMMENT '输入价格/1k token',
                                  `output_price` decimal(10, 8) NULL DEFAULT 0.00000000 COMMENT '输出价格/1k token',
                                  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  PRIMARY KEY (`id`) USING BTREE,
                                  UNIQUE INDEX `uk_code_type`(`model_code` ASC, `model_type` ASC) USING BTREE,
                                  INDEX `idx_model_type`(`model_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 391842863399456769 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = 'AI模型配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_prompt_template
-- ----------------------------
DROP TABLE IF EXISTS `ai_prompt_template`;
CREATE TABLE `ai_prompt_template`  (
                                       `id` bigint NOT NULL AUTO_INCREMENT,
                                       `code` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '模板编码 ',
                                       `name` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '模板名称',
                                       `model_id` bigint NULL DEFAULT NULL COMMENT '默认建议模型ID',
                                       `system_message` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '系统预设 (System Prompt)',
                                       `user_message` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '用户输入模板 (含 {{variable}})',
                                       `parameters` json NULL COMMENT '默认参数 (温度、TopP等)',
                                       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       PRIMARY KEY (`id`) USING BTREE,
                                       UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = 'Prompt提示词模板' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for app
-- ----------------------------
DROP TABLE IF EXISTS `app`;
CREATE TABLE `app`  (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                        `app_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '应用名称',
                        `cover` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '应用封面',
                        `init_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '应用初始化的 prompt',
                        `code_generator_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '代码生成类型（枚举）',
                        `tags` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签',
                        `deploy_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '部署标识',
                        `deploy_status` tinyint NOT NULL DEFAULT 0 COMMENT '部署状态（枚举 0:下线 1:上线 2:上线失败）',
                        `deployed_time` datetime NULL DEFAULT NULL COMMENT '部署时间',
                        `priority` int NOT NULL DEFAULT 0 COMMENT '优先级',
                        `user_id` bigint NOT NULL COMMENT '创建用户id',
                        `scope_status` tinyint NOT NULL DEFAULT 0 COMMENT '可见范围状态（枚举 0:仅本人可见 1:全部可见）',
                        `chat_scope_status` tinyint NOT NULL DEFAULT 0 COMMENT '聊天可见范围状态（枚举 0:仅本人可见 1:全部可见）',
                        `current_status` tinyint NOT NULL DEFAULT 0 COMMENT '当前状态（枚举 0:生成中 1:生成完成 2:中断）',
                        `version` bigint NOT NULL DEFAULT 0 COMMENT '版本号',
                        `edit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
                        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
                        PRIMARY KEY (`id`) USING BTREE,
                        UNIQUE INDEX `uk_deployKey`(`deploy_key` ASC) USING BTREE,
                        INDEX `idx_appName`(`app_name` ASC) USING BTREE,
                        INDEX `idx_userId`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 391845013810720769 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '应用' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for chat_history
-- ----------------------------
DROP TABLE IF EXISTS `chat_history`;
CREATE TABLE `chat_history`  (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                                 `thinking_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '思考过程(JSON数组)',
                                 `message` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息',
                                 `message_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'user/ai',
                                 `parent_id` bigint NULL DEFAULT NULL COMMENT '父消息id（用于上下文关联）',
                                 `app_id` bigint NOT NULL COMMENT '应用id',
                                 `user_id` bigint NOT NULL COMMENT '创建用户id',
                                 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
                                 PRIMARY KEY (`id`) USING BTREE,
                                 INDEX `idx_appId`(`app_id` ASC) USING BTREE,
                                 INDEX `idx_createTime`(`create_time` ASC) USING BTREE,
                                 INDEX `idx_appId_createTime`(`app_id` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 391845535225622529 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '对话历史' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '唯一',
                         `user_account` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号',
                         `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
                         `password` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
                         `user_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
                         `avatar` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
                         `profile` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人简介',
                         `role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色',
                         `vip_expire_time` datetime NULL DEFAULT NULL COMMENT '会员过期时间',
                         `vip_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会员兑换码',
                         `vip_number` bigint NULL DEFAULT NULL COMMENT '会员编号',
                         `points` bigint NOT NULL DEFAULT 0 COMMENT 'AI积分余额',
                         `share_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分享码',
                         `invite_user` bigint NULL DEFAULT NULL COMMENT '邀请用户 id',
                         `edit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         `user_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态 0-正常 1-禁用',
                         `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
                         PRIMARY KEY (`id`) USING BTREE,
                         UNIQUE INDEX `user_account`(`user_account` ASC) USING BTREE,
                         UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
                         INDEX `idx_user_name`(`user_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 367201073535053825 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_points_log
-- ----------------------------
DROP TABLE IF EXISTS `user_points_log`;
CREATE TABLE `user_points_log`  (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                    `user_id` bigint NOT NULL COMMENT '用户id',
                                    `amount` bigint NOT NULL COMMENT '变动金额(正入负出)',
                                    `type` tinyint NOT NULL COMMENT '1-注册赠送 \r\n2-邀请奖励\r\n3-每日签到\r\n4-充值购买\r\n5-失败退款\r\n6-系统补偿 (增加)\r\n7-系统补偿（扣除）\r\n101-AI消耗\r\n102-应用发布消耗',
                                    `current_points` bigint NOT NULL COMMENT '变动后余额',
                                    `biz_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务去重号',
                                    `ref_id` bigint NULL DEFAULT NULL COMMENT '关联业务id',
                                    `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    PRIMARY KEY (`id`) USING BTREE,
                                    UNIQUE INDEX `uk_biz_no`(`biz_no` ASC) USING BTREE,
                                    INDEX `idx_user_time`(`user_id` ASC, `create_time` ASC) USING BTREE,
                                    INDEX `idx_type_time`(`type` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 367254343855042561 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户积分流水表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
