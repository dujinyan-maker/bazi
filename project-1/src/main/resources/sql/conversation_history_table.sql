-- 对话历史表创建脚本
-- 用于存储AI助手的对话记录，支持上下文记忆

CREATE TABLE IF NOT EXISTS `conversation_history` (
                                                      `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                      `user_id` BIGINT DEFAULT NULL COMMENT '用户ID（关联users表），可选，支持匿名对话',
                                                      `conversation_id` VARCHAR(100) DEFAULT NULL COMMENT '会话ID，用于区分不同的对话会话',
                                                      `role` VARCHAR(20) NOT NULL COMMENT '角色：user-用户，assistant-AI助手',
                                                      `content` TEXT NOT NULL COMMENT '对话内容',
                                                      `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                      PRIMARY KEY (`id`),
                                                      KEY `idx_user_id` (`user_id`),
                                                      KEY `idx_conversation_id` (`conversation_id`),
                                                      KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话历史表';

