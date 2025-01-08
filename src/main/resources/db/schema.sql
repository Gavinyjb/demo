-- 创建数据源配置表
CREATE TABLE IF NOT EXISTS data_source_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    version_id VARCHAR(64) NOT NULL COMMENT '版本ID，格式：DS202501080001',
    source VARCHAR(128) NOT NULL COMMENT '数据源标识',
    source_group VARCHAR(128) COMMENT '数据源分组',
    gateway_type VARCHAR(128) COMMENT '网关类型',
    dm VARCHAR(128) COMMENT 'DM标识',
    loghub_endpoint VARCHAR(512) COMMENT 'LogHub终端节点',
    loghub_project VARCHAR(128) COMMENT 'LogHub项目',
    loghub_stream VARCHAR(128) COMMENT 'LogHub数据流',
    loghub_accesskey_id VARCHAR(512) COMMENT 'LogHub访问密钥ID',
    loghub_accesskey_secret VARCHAR(512) COMMENT 'LogHub访问密钥密文',
    loghub_assume_role_arn VARCHAR(512) COMMENT 'LogHub角色ARN',
    loghub_cursor VARCHAR(64) COMMENT 'LogHub游标',
    consume_region TEXT COMMENT '消费区域',
    data_fetch_interval_millis INT COMMENT '数据获取间隔(毫秒)',
    status VARCHAR(32) NOT NULL COMMENT '状态: DRAFT/PUBLISHED/DEPRECATED',
    effective_gray_groups TEXT COMMENT '生效的灰度组，JSON数组格式',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_version (version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源配置表';

-- 创建API记录配置表
CREATE TABLE IF NOT EXISTS api_record_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    version_id VARCHAR(64) NOT NULL COMMENT '版本ID，格式：AR202501080001',
    gateway_type VARCHAR(128) COMMENT '网关类型',
    gateway_code VARCHAR(128) COMMENT '网关编码',
    api_version VARCHAR(128) COMMENT 'API版本',
    api_name VARCHAR(128) COMMENT 'API名称',
    loghub_stream VARCHAR(128) COMMENT 'LogHub数据流',
    basic_config TEXT COMMENT '基础配置(JSON)',
    event_config TEXT COMMENT '事件配置(JSON)',
    user_identity_config TEXT COMMENT '用户身份配置(JSON)',
    request_config TEXT COMMENT '请求配置(JSON)',
    response_config TEXT COMMENT '响应配置(JSON)',
    filter_config TEXT COMMENT '过滤配置(JSON)',
    reference_resource_config TEXT COMMENT '引用资源配置(JSON)',
    type VARCHAR(32) COMMENT '类型',
    status VARCHAR(32) NOT NULL COMMENT '状态: DRAFT/PUBLISHED/DEPRECATED',
    effective_gray_groups TEXT COMMENT '生效的灰度组，JSON数组格式',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_version (version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API记录配置表';

-- 创建发布历史表
CREATE TABLE IF NOT EXISTS publish_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    version_id VARCHAR(64) NOT NULL COMMENT '版本ID',
    config_type VARCHAR(32) NOT NULL COMMENT '配置类型: DATA_SOURCE/API_RECORD',
    status VARCHAR(32) NOT NULL COMMENT '发布状态',
    gray_groups TEXT COMMENT '发布的灰度组，JSON数组格式',
    operator VARCHAR(64) NOT NULL COMMENT '操作人',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    KEY idx_version (version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布历史表'; 