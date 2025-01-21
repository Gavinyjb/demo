-- 配置版本表
CREATE TABLE IF NOT EXISTS config_version (
    id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    version_id VARCHAR(256) NOT NULL COMMENT '版本唯一标识',
    identifier VARCHAR(512) NOT NULL COMMENT '配置唯一标识',
    config_type VARCHAR(64) NOT NULL COMMENT '配置类型:DATA_SOURCE|API_RECORD|API_META',
    config_status VARCHAR(64) NOT NULL COMMENT '配置状态:DRAFT|PUBLISHED|DEPRECATED',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_version_id (version_id(128)),
    INDEX idx_identifier (identifier(128)),
    INDEX idx_config_type (config_type),
    INDEX idx_identifier_status_time (identifier(128), config_status, gmt_modified),
    INDEX idx_status_time (config_status, gmt_modified)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配置版本表';

-- 灰度发布表
CREATE TABLE IF NOT EXISTS config_gray_release (
    id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    version_id VARCHAR(256) NOT NULL COMMENT '关联版本表',
    stage VARCHAR(64) NOT NULL COMMENT '灰度阶段:STAGE_1|STAGE_2|FULL',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_version_id (version_id(128)),
    INDEX idx_stage (stage)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='灰度发布表';

-- 发布历史表
CREATE TABLE IF NOT EXISTS publish_history (
    id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    version_id VARCHAR(256) NOT NULL COMMENT '关联版本表',
    config_type VARCHAR(64) NOT NULL COMMENT '配置类型:DATA_SOURCE|API_RECORD|API_META',
    config_status VARCHAR(64) NOT NULL COMMENT '配置状态:DRAFT|PUBLISHED|DEPRECATED',
    stage VARCHAR(64) COMMENT '灰度阶段:STAGE_1|STAGE_2|FULL',
    operator VARCHAR(256) NOT NULL COMMENT '操作人',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    INDEX idx_version_time (version_id(128), gmt_create),
    INDEX idx_type_status_time (config_type, config_status, gmt_create)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布历史表';

-- 数据源配置表
CREATE TABLE IF NOT EXISTS conf_data_source_config (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `version_id` varchar(255) DEFAULT NULL COMMENT '版本ID',
    `name` varchar(255) NOT NULL COMMENT '数据源名称',
    `source_group` varchar(255) DEFAULT NULL COMMENT '数据源分组',
    `gateway_type` varchar(255) DEFAULT NULL COMMENT '网关类型',
    `dm` varchar(64) DEFAULT NULL COMMENT '数据|管控',
    `sls_region_id` varchar(255) NOT NULL COMMENT 'SLS RegionId',
    `sls_endpoint` varchar(255) DEFAULT NULL COMMENT 'SLS Endpoint',
    `sls_project` varchar(255) NOT NULL COMMENT 'SLS Project',
    `sls_log_store` varchar(255) NOT NULL COMMENT 'SLS LogStore',
    `sls_account_id` varchar(255) NOT NULL COMMENT 'SLS 所属账号',
    `sls_role_arn` varchar(255) NOT NULL COMMENT '拉取日志的 SLS 角色',
    `sls_cursor` varchar(256) DEFAULT NULL COMMENT 'SLS游标',
    `consume_region` varchar(255) DEFAULT NULL COMMENT '消费地域',
    `consumer_group_name` varchar(255) NOT NULL COMMENT '消费组名称',
    `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态',
    `worker_config` varchar(1024) DEFAULT NULL COMMENT '消费配置',
    `comment` varchar(255) DEFAULT NULL COMMENT '备注',
    `gmt_create` datetime NOT NULL COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    INDEX idx_version_id (version_id(128)),
    INDEX idx_name (`name`(128))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计数据源表';

-- API元数据配置表
CREATE TABLE IF NOT EXISTS amp_api_meta (
    id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    version_id VARCHAR(256) NOT NULL COMMENT '关联版本表',
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
    response_body_log TINYINT(4) COMMENT '响应体日志',
    invoke_type VARCHAR(64) COMMENT '调用类型',
    resource_spec VARCHAR(7168) COMMENT '资源规格JSON',
    effective_flag VARCHAR(64) COMMENT '生效标识',
    audit_status VARCHAR(64) COMMENT '审计状态',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    INDEX idx_version_id (version_id(128)),
    INDEX idx_api_info (gateway_type, gateway_code, api_version, api_name(128))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API元数据配置表';

-- API记录配置表
CREATE TABLE IF NOT EXISTS api_record_config (
    id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    version_id VARCHAR(256) NOT NULL COMMENT '关联版本表',
    gateway_type VARCHAR(64) NOT NULL COMMENT '网关类型',
    gateway_code VARCHAR(256) NOT NULL COMMENT '网关编码',
    api_version VARCHAR(64) NOT NULL COMMENT 'API版本',
    api_name VARCHAR(256) NOT NULL COMMENT 'API名称',
    basic_config TEXT COMMENT '基础配置JSON',
    event_config TEXT COMMENT '事件配置JSON',
    user_identity_config TEXT COMMENT '用户身份配置JSON',
    request_config TEXT COMMENT '请求配置JSON',
    response_config TEXT COMMENT '响应配置JSON',
    filter_config TEXT COMMENT '过滤配置JSON',
    reference_resource_config TEXT COMMENT '引用资源配置JSON',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_version_id (version_id(128)),
    INDEX idx_api_info (gateway_type, gateway_code, api_version,api_name(128))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API记录配置表'; 