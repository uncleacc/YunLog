-- 删除日记表的 title 字段
-- 此操作不可逆，请先备份数据库

-- 1. 备份提醒
-- 执行前请先备份数据库：
-- mysqldump -u root -p yunlog > yunlog_backup_$(date +%Y%m%d_%H%M%S).sql

-- 2. 删除 title 字段
ALTER TABLE diaries DROP COLUMN title;

-- 3. 验证
DESC diaries;

-- 4. 查看前几条数据确认
SELECT id, content, category_id, create_time FROM diaries LIMIT 5;
