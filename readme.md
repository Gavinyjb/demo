# 配置管理服务

一个用于管理和发布配置的服务系统，支持灰度发布和版本控制。

## 功能特性

- 支持多种配置类型（数据源配置、API元数据、API记录）
- 基于阶段的灰度发布策略
- 版本控制和回滚机制
- 发布历史记录
- 配置变更检测

## 系统架构

### 数据模型

```mermaid
erDiagram
    config_version ||--o{ config_gray_release : "灰度发布"
    config_version ||--o{ publish_history : "发布历史"
    config_version ||--|| data_source_config : "数据源配置"
    config_version ||--|| api_record_config : "API记录"
    config_version ||--|| amp_api_meta : "API元数据"

    config_version {
        varchar(256) version_id PK "版本ID"
        varchar(512) identifier "配置标识"
        varchar(64) config_type "配置类型"
        varchar(64) status "状态"
        datetime gmt_create "创建时间"
        datetime gmt_modified "修改时间"
    }

    config_gray_release {
        varchar(256) version_id FK "版本ID"
        varchar(64) stage "灰度阶段"
        datetime gmt_create "创建时间"
        datetime gmt_modified "修改时间"
    }

    publish_history {
        varchar(256) version_id FK "版本ID"
        varchar(64) config_type "配置类型"
        varchar(64) status "状态"
        varchar(64) stage "灰度阶段"
        varchar(256) operator "操作人"
        datetime gmt_create "创建时间"
        datetime gmt_modified "修改时间"
    }

    data_source_config {
        varchar(256) version_id PK,FK "版本ID"
        varchar(256) source "数据源标识"
        varchar(256) source_group "数据源分组"
        varchar(256) gateway_type "网关类型"
        varchar(64) dm "数据|管控"
        varchar(256) sls_endpoint "SLS访问地址"
        varchar(256) sls_project "SLS项目"
        varchar(256) sls_logstore "SLS日志库"
        varchar(256) sls_account_id "SLS账号ID"
        varchar(256) sls_assume_role_arn "SLS角色ARN"
        varchar(256) sls_cursor "SLS游标"
        varchar(256) consume_region "消费地域"
        varchar(1024) worker_config "工作配置JSON"
    }

    api_record_config {
        varchar(256) version_id PK,FK "版本ID"
        varchar(64) gateway_type "网关类型"
        varchar(64) gateway_code "网关编码"
        varchar(64) api_version "API版本"
        varchar(64) api_name "API名称"
        varchar(15000) basic_config "基础配置JSON"
        varchar(15000) event_config "事件配置JSON"
        varchar(15000) user_identity_config "用户身份配置JSON"
        varchar(15000) request_config "请求配置JSON"
        varchar(15000) response_config "响应配置JSON"
        varchar(15000) filter_config "过滤配置JSON"
        varchar(15000) reference_resource_config "引用资源配置JSON"
    }

    amp_api_meta {
        varchar(256) version_id PK,FK "版本ID"
        varchar(256) api_name "API名称"
        varchar(256) product "产品名称"
        varchar(64) gateway_type "网关类型"
        varchar(64) dm "数据|管控"
        varchar(256) gateway_code "网关编码"
        varchar(64) api_version "API版本"
        varchar(256) actiontrail_code "操作审计编码"
        varchar(64) operation_type "操作类型"
        varchar(3072) description "API描述"
        varchar(64) visibility "可见性"
        varchar(64) isolation_type "隔离类型"
        varchar(64) service_type "服务类型"
        tinyint(4) response_body_log "响应体日志"
        varchar(64) invoke_type "调用类型"
        varchar(7168) resource_spec "资源规格JSON"
        varchar(64) effective_flag "生效标识"
        varchar(64) audit_status "审计状态"
    }
```

### 灰度发布策略

采用基于阶段(Stage)的灰度发布策略：
- STAGE_1: 首批灰度地域
- STAGE_2: 第二批灰度地域
- FULL: 全量发布

### 配置状态流转

- DRAFT: 草稿状态
- PUBLISHED: 已发布状态
- DEPRECATED: 已废弃状态

## API接口

### 数据源配置接口

```http
POST   /api/datasource              # 创建配置
PUT    /api/datasource/{versionId}  # 更新配置
GET    /api/datasource/{versionId}  # 获取指定版本
GET    /api/datasource/published    # 获取所有已发布配置
GET    /api/datasource/active       # 获取指定地域生效的配置
POST   /api/datasource/diff         # 获取配置变更信息
```

### 发布管理接口

```http
POST   /api/publish/{versionId}              # 发布配置
POST   /api/publish/{versionId}/deprecate    # 废弃配置
POST   /api/publish/rollback/previous        # 回滚到上一版本
POST   /api/publish/rollback/{targetVersionId}# 回滚到指定版本
GET    /api/publish/history/{versionId}      # 获取发布历史
GET    /api/publish/stages                   # 获取灰度阶段信息
```

## 配置示例

### 数据源配置

```json
{
  "source": "example-source",
  "sourceGroup": "example-group",
  "gatewayType": "API",
  "dm": "DATA",
  "slsEndpoint": "cn-hangzhou.log.aliyuncs.com",
  "slsProject": "example-project",
  "slsLogstore": "example-logstore",
  "workerConfig": "{\"fetchIntervalMillis\": 1000}"
}
```

## 部署要求

- Java 8
- MySQL 5.7+
- Spring Boot 2.7+

## 配置说明

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/config_management
    username: root
    password: password

config:
  version:
    max-datasource-versions: 5
    max-apirecord-versions: 5
    max-api-meta-versions: 5

diff:
  ignore-fields: gmt_create,gmt_modified
```

## 开发指南

1. 克隆项目
2. 配置数据库连接
3. 执行 schema.sql 创建数据库表
4. 运行应用

```bash
./mvnw spring-boot:run
```

## API文档

访问 Swagger UI：
```
http://localhost:8080/swagger-ui.html
```

## 注意事项

- 配置发布前需要先创建版本
- 灰度发布需要指定正确的阶段
- 回滚操作会创建新的版本
