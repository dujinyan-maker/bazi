-- 祝福语/弹幕表
-- 用于存储用户发送的祝福语内容

CREATE TABLE IF NOT EXISTS `blessing_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID（关联users表）',
  `content` TEXT NOT NULL COMMENT '祝福语内容',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：1-正常显示，0-已删除/隐藏',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='祝福语/弹幕表';



