-- 1. 先备份原表
CREATE TABLE data_source_config_backup AS SELECT * FROM data_source_config;

-- 2. 添加新字段
ALTER TABLE data_source_config 
ADD COLUMN version_id VARCHAR(64) COMMENT '版本ID' AFTER id,
ADD COLUMN status VARCHAR(32) DEFAULT 'PUBLISHED' COMMENT '状态' AFTER data_fetch_interval_millis,
ADD COLUMN effective_gray_groups TEXT COMMENT '生效的灰度组' AFTER status,
ADD UNIQUE KEY uk_version (version_id);

-- 3. 更新存量数据，使用 'all' 表示全量生效
UPDATE data_source_config 
SET version_id = CONCAT('DS', DATE_FORMAT(IFNULL(gmt_create, NOW()), '%Y%m%d'), LPAD(@row_number:=@row_number+1, 4, '0')),
    status = 'PUBLISHED',
    effective_gray_groups = 'all'
WHERE version_id IS NULL;

-- 4. 为每个存量配置生成发布历史记录
INSERT INTO publish_history (version_id, config_type, status, gray_groups, operator, gmt_create, gmt_modified)
SELECT 
    version_id,
    'DATA_SOURCE',
    'PUBLISHED',
    effective_gray_groups,
    'system_migration',
    gmt_create,
    gmt_modified
FROM data_source_config; 