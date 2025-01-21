-- 配置版本表
CREATE TABLE IF NOT EXISTS config_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version_id VARCHAR(256) NOT NULL,
    identifier VARCHAR(512) NOT NULL,
    config_type VARCHAR(64) NOT NULL,
    config_status VARCHAR(64) NOT NULL,
    gmt_create TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_version_id (version_id)
);

-- 灰度发布表
CREATE TABLE IF NOT EXISTS config_gray_release (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version_id VARCHAR(256) NOT NULL,
    stage VARCHAR(64) NOT NULL,
    gmt_create TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_version_id (version_id)
);

-- 发布历史表
CREATE TABLE IF NOT EXISTS publish_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version_id VARCHAR(256) NOT NULL,
    config_type VARCHAR(64) NOT NULL,
    config_status VARCHAR(64) NOT NULL,
    stage VARCHAR(64),
    operator VARCHAR(256) NOT NULL,
    gmt_create TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 数据源配置表
CREATE TABLE IF NOT EXISTS conf_data_source_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version_id VARCHAR(256),
    name VARCHAR(256) NOT NULL,
    source_group VARCHAR(64),
    gateway_type VARCHAR(64),
    dm VARCHAR(64),
    sls_region_id VARCHAR(64),
    sls_endpoint VARCHAR(256),
    sls_project VARCHAR(256),
    sls_log_store VARCHAR(256),
    sls_account_id VARCHAR(64),
    sls_role_arn VARCHAR(256),
    sls_cursor VARCHAR(256),
    consume_region VARCHAR(64),
    consumer_group_name VARCHAR(256),
    status INT,
    worker_config TEXT,
    comment VARCHAR(512),
    gmt_create TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
); 