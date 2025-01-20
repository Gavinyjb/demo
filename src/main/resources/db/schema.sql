-- 配置版本表
CREATE TABLE IF NOT EXISTS config_version (
    version_id VARCHAR(256) PRIMARY KEY COMMENT '版本唯一标识',
    identifier VARCHAR(512) NOT NULL COMMENT '配置唯一标识',
    config_type VARCHAR(64) NOT NULL COMMENT '配置类型:DATA_SOURCE|API_RECORD|API_META',
    status VARCHAR(64) NOT NULL COMMENT '状态:DRAFT|PUBLISHED|DEPRECATED',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_identifier_type (identifier, config_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配置版本表';

-- 灰度发布表
CREATE TABLE IF NOT EXISTS config_gray_release (
    version_id VARCHAR(256) NOT NULL COMMENT '关联版本表',
    stage VARCHAR(64) NOT NULL COMMENT '灰度阶段:STAGE_1|STAGE_2|FULL',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (version_id, stage)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='灰度发布表';

-- 发布历史表
CREATE TABLE IF NOT EXISTS publish_history (
    version_id VARCHAR(256) NOT NULL COMMENT '关联版本表',
    config_type VARCHAR(64) NOT NULL COMMENT '配置类型:DATA_SOURCE|API_RECORD|API_META',
    status VARCHAR(64) NOT NULL COMMENT '状态:DRAFT|PUBLISHED|DEPRECATED',
    stage VARCHAR(64) COMMENT '灰度阶段:STAGE_1|STAGE_2|FULL',
    operator VARCHAR(256) NOT NULL COMMENT '操作人',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (version_id, gmt_create)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布历史表';

-- 数据源配置表
CREATE TABLE IF NOT EXISTS data_source_config (
    version_id VARCHAR(256) PRIMARY KEY COMMENT '关联版本表',
    source VARCHAR(256) NOT NULL COMMENT '数据源标识',
    source_group VARCHAR(256) COMMENT '数据源分组',
    gateway_type VARCHAR(64) COMMENT '网关类型',
    dm VARCHAR(64) COMMENT '数据|管控',
    sls_endpoint VARCHAR(256) COMMENT 'SLS访问地址',
    sls_project VARCHAR(256) COMMENT 'SLS项目',
    sls_logstore VARCHAR(256) COMMENT 'SLS日志库',
    sls_account_id VARCHAR(256) COMMENT 'SLS账号ID',
    sls_assume_role_arn VARCHAR(256) COMMENT 'SLS角色ARN',
    sls_cursor VARCHAR(256) COMMENT 'SLS游标',
    consume_region VARCHAR(256) COMMENT '消费地域',
    worker_config VARCHAR(1024) COMMENT '工作配置JSON',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源配置表';

-- API元数据配置表
CREATE TABLE IF NOT EXISTS amp_api_meta (
    version_id VARCHAR(256) PRIMARY KEY COMMENT '关联版本表',
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
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API元数据配置表';

-- API记录配置表
CREATE TABLE IF NOT EXISTS api_record_config (
    version_id VARCHAR(256) PRIMARY KEY COMMENT '关联版本表',
    gateway_type VARCHAR(64) NOT NULL COMMENT '网关类型',
    gateway_code VARCHAR(64) NOT NULL COMMENT '网关编码',
    api_version VARCHAR(64) NOT NULL COMMENT 'API版本',
    api_name VARCHAR(64) NOT NULL COMMENT 'API名称',
    basic_config VARCHAR(15000) COMMENT '基础配置JSON',
    event_config VARCHAR(15000) COMMENT '事件配置JSON',
    user_identity_config VARCHAR(15000) COMMENT '用户身份配置JSON',
    request_config VARCHAR(15000) COMMENT '请求配置JSON',
    response_config VARCHAR(15000) COMMENT '响应配置JSON',
    filter_config VARCHAR(15000) COMMENT '过滤配置JSON',
    reference_resource_config VARCHAR(15000) COMMENT '引用资源配置JSON',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API记录配置表'; 