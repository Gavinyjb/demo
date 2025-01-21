-- 1. 备份原表
CREATE TABLE conf_data_source_config_backup AS SELECT * FROM conf_data_source_config;
CREATE TABLE amp_api_meta_backup AS SELECT * FROM amp_api_meta;

-- 2. 为存量表添加版本ID字段
ALTER TABLE conf_data_source_config 
ADD COLUMN version_id VARCHAR(256) COMMENT '版本ID',
ADD INDEX idx_version_id (version_id);

ALTER TABLE amp_api_meta 
ADD COLUMN version_id VARCHAR(256) COMMENT '版本ID',
ADD INDEX idx_version_id (version_id);

-- 3. 创建回滚脚本（需要时使用）
-- 回滚数据源配置
-- DROP INDEX idx_version_id ON conf_data_source_config;
-- ALTER TABLE conf_data_source_config DROP COLUMN version_id;
-- TRUNCATE TABLE conf_data_source_config;
-- INSERT INTO conf_data_source_config SELECT * FROM conf_data_source_config_backup;
-- DROP TABLE conf_data_source_config_backup;

-- 回滚API元数据配置
-- DROP INDEX idx_version_id ON amp_api_meta;
-- ALTER TABLE amp_api_meta DROP COLUMN version_id;
-- TRUNCATE TABLE amp_api_meta;
-- INSERT INTO amp_api_meta SELECT * FROM amp_api_meta_backup;
-- DROP TABLE amp_api_meta_backup; 