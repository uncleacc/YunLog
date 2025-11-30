-- 云日记用户系统迁移脚本
-- 创建时间: 2025-10-27
-- 说明: 添加用户表，为现有表添加用户关联

USE yunlog;

-- =============================================
-- 1. 创建用户表
-- =============================================
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) UNIQUE COMMENT '用户名（用于账号密码登录）',
  `password` VARCHAR(255) COMMENT '密码（BCrypt加密）',
  `phone` VARCHAR(20) UNIQUE COMMENT '手机号（预留）',
  `wechat_openid` VARCHAR(100) UNIQUE COMMENT '微信OpenID',
  `wechat_union_id` VARCHAR(100) UNIQUE COMMENT '微信UnionID（预留）',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `avatar_url` VARCHAR(255) COMMENT '头像URL',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_username` (`username`),
  INDEX `idx_phone` (`phone`),
  INDEX `idx_wechat_openid` (`wechat_openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 2. 为日记表添加用户关联
-- =============================================
-- 添加 user_id 列
ALTER TABLE `diaries` 
ADD COLUMN `user_id` BIGINT COMMENT '用户ID' AFTER `id`;

-- 为现有数据创建默认用户（如果需要）
-- 这里先设为NULL，后续可以手动关联或删除无主数据
-- UPDATE `diaries` SET `user_id` = 1 WHERE `user_id` IS NULL;

-- 添加索引和外键约束
ALTER TABLE `diaries` 
ADD INDEX `idx_user_id` (`user_id`);

-- 可选：添加外键约束（如果需要严格的引用完整性）
-- ALTER TABLE `diaries` 
-- ADD CONSTRAINT `fk_diary_user` 
-- FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE;

-- =============================================
-- 3. 为分类表添加用户关联
-- =============================================
-- 添加 user_id 列
ALTER TABLE `categories` 
ADD COLUMN `user_id` BIGINT COMMENT '用户ID' AFTER `id`;

-- 添加索引
ALTER TABLE `categories` 
ADD INDEX `idx_user_id` (`user_id`);

-- 可选：添加外键约束
-- ALTER TABLE `categories` 
-- ADD CONSTRAINT `fk_category_user` 
-- FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE;

-- =============================================
-- 4. 为附件表添加用户关联（间接通过diary_id关联）
-- =============================================
-- 附件已经通过 diary_id 关联到日记，日记关联到用户，因此不需要直接添加 user_id

-- =============================================
-- 5. 创建测试用户（可选）
-- =============================================
-- 插入一个测试用户：用户名 test，密码 123456（BCrypt加密后）
-- 密码123456的BCrypt: $2a$10$xn3LI/AjqicFYZFruSwve.681477XaVOGLbfJfvBdKhFCvSMI4S66
INSERT INTO `user` (`username`, `password`, `nickname`, `status`) 
VALUES ('test', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVOGLbfJfvBdKhFCvSMI4S66', '测试用户', 1)
ON DUPLICATE KEY UPDATE `username` = `username`;

-- =============================================
-- 迁移完成提示
-- =============================================
-- 1. 执行此脚本后，所有现有日记和分类的 user_id 都为 NULL
-- 2. 建议：为现有数据分配默认用户，或者清理无主数据
-- 3. 后续新增的日记/分类将自动关联当前登录用户
-- 4. 测试用户：test / 123456
