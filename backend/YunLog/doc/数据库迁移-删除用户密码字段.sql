-- 删除用户表的密码字段
-- 执行日期: 2025-11-21
-- 说明: 由于移除了账号密码登录功能，不再需要密码字段

USE yunlog;

-- 删除 password 列
ALTER TABLE `user` DROP COLUMN `password`;

-- 同时删除 username 的唯一索引约束（因为可能有重复的默认用户名）
-- ALTER TABLE `user` DROP INDEX `username`; -- 如果需要的话可以执行这行
