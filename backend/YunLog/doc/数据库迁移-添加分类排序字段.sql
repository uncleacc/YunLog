-- 为 categories 表添加 sort_order 字段
-- 用于支持分类拖动排序功能

-- 1. 添加 sort_order 字段
ALTER TABLE categories ADD COLUMN sort_order INT NOT NULL DEFAULT 0 COMMENT '排序序号，数字越小越靠前';

-- 2. 为现有数据设置初始排序值（按创建时间）
SET @row_number = 0;
UPDATE categories 
SET sort_order = (@row_number:=@row_number + 1) - 1
ORDER BY create_time ASC;

-- 3. 添加索引以提高查询性能
ALTER TABLE categories ADD INDEX idx_sort_order (sort_order);

-- 验证
SELECT id, name, sort_order, create_time FROM categories ORDER BY sort_order ASC;
