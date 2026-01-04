-- 用户表创建脚本
-- 如果表已存在，可以忽略此脚本

CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `nickname` VARCHAR(100) DEFAULT NULL COMMENT '用户昵称（来自微信）',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名，必填',
  `birthday` DATE DEFAULT NULL COMMENT '生日，可选',
  `gender` INT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `openid` VARCHAR(100) NOT NULL COMMENT '微信用户的唯一标识 openid，不可重复',
  `is_member` TINYINT(1) DEFAULT 0 COMMENT '是否为会员：0-否，1-是',
  `member_start_time` DATETIME DEFAULT NULL COMMENT '会员开始时间',
  `member_expire_time` DATETIME DEFAULT NULL COMMENT '会员到期时间',
  `register_time` DATETIME DEFAULT NULL COMMENT '用户注册时间',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号，必填且唯一',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱，选填',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

