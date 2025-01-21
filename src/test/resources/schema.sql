-- 配置版本表
CREATE TABLE IF NOT EXISTS config_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version_id VARCHAR(256) NOT NULL,
    identifier VARCHAR(512) NOT NULL,
    config_type VARCHAR(64) NOT NULL,
    config_status VARCHAR(64) NOT NULL,
    gmt_create TIMESTAMP NOT NULL,
    gmt_modified TIMESTAMP NOT NULL,
    UNIQUE KEY uk_version_id (version_id)
);

-- 灰度发布表
CREATE TABLE IF NOT EXISTS config_gray_release (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version_id VARCHAR(256) NOT NULL,
    stage VARCHAR(64) NOT NULL,
    gmt_create TIMESTAMP NOT NULL,
    gmt_modified TIMESTAMP NOT NULL,
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
    gmt_create TIMESTAMP NOT NULL,
    gmt_modified TIMESTAMP NOT NULL
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
    gmt_create TIMESTAMP NOT NULL,
    gmt_modified TIMESTAMP NOT NULL
);

-- API元数据配置表
CREATE TABLE IF NOT EXISTS amp_api_meta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version_id VARCHAR(256),
    api_name VARCHAR(256) NOT NULL COMMENT 'API名称',
    product VARCHAR(256) COMMENT '产品名称',
    gateway_type VARCHAR(64) NOT NULL COMMENT '网关类型',
    dm VARCHAR(64) COMMENT '数据|管控',
    gateway_code VARCHAR(256) NOT NULL COMMENT '网关编码',
    api_version VARCHAR(64) NOT NULL COMMENT 'API版本',
    actiontrail_code VARCHAR(256) COMMENT '操作审计编码',
    operation_type VARCHAR(64) COMMENT '操作类型',
    description VARCHAR(3072) COMMENT 'API描述',
    visibility VARCHAR(64) COMMENT '可见性',
    isolation_type VARCHAR(64) COMMENT '隔离类型',
    service_type VARCHAR(64) COMMENT '服务类型',
    response_body_log TINYINT COMMENT '响应体日志',
    invoke_type VARCHAR(64) COMMENT '调用类型',
    resource_spec VARCHAR(7168) COMMENT '资源规格JSON',
    effective_flag VARCHAR(64) COMMENT '生效标识',
    audit_status VARCHAR(64) COMMENT '审计状态',
    gmt_create TIMESTAMP NOT NULL,
    gmt_modified TIMESTAMP NOT NULL,
    INDEX idx_api_info (gateway_type, gateway_code, api_version, api_name(128)),
    INDEX idx_version_id (version_id(128))
);

