-- 图片模板表
-- 用于存储图片模板信息

CREATE TABLE IF NOT EXISTS `image_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
  `file_path` VARCHAR(500) NOT NULL COMMENT '模板文件路径（相对于模板目录）',
  `width` INT DEFAULT NULL COMMENT '模板宽度（像素）',
  `height` INT DEFAULT NULL COMMENT '模板高度（像素）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '模板描述',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片模板表';

-- 插入示例模板数据（需要手动上传对应的图片文件）
-- INSERT INTO `image_template` (`name`, `file_path`, `description`) VALUES
-- ('模板1', 'template1.jpg', '示例模板1'),
-- ('模板2', 'template2.jpg', '示例模板2'),
-- ('模板3', 'template3.jpg', '示例模板3');

