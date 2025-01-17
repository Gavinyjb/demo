# 数据模型


ER 关系图
```mermaid
erDiagram
    BaseVersionedConfig ||--|| data_source_config : "extends"
    BaseVersionedConfig ||--|| api_record_config : "extends"
    BaseVersionedConfig ||--|| api_meta_config : "extends"
    
    data_source_config ||--o{ publish_history : "has"
    api_record_config ||--o{ publish_history : "has"
    api_meta_config ||--o{ publish_history : "has"

    BaseVersionedConfig {
        bigint id PK
        varchar version_id UK
        varchar status
        text effective_gray_groups
        datetime gmt_create
        datetime gmt_modified
    }

    data_source_config {
        bigint id PK
        bigint at_work_id
        varchar source
        varchar source_group
        varchar gateway_type
        varchar dm
        varchar loghub_endpoint
        varchar loghub_project
        varchar loghub_stream
        varchar loghub_accesskey_id
        varchar loghub_accesskey_secret
        varchar loghub_assume_role_arn
        varchar loghub_cursor
        text consume_region
        int data_fetch_interval_millis
    }

    api_record_config {
        bigint id PK
        varchar gateway_type
        varchar gateway_code
        varchar api_version
        varchar api_name
        varchar loghub_stream
        text basic_config
        text event_config
        text user_identity_config
        text request_config
        text response_config
        text filter_config
        text reference_resource_config
        varchar type
    }

    api_meta_config {
        bigint id PK
        varchar api_name
        varchar product
        varchar gateway_type
        varchar dm
        varchar gateway_code
        varchar api_version
        varchar actiontrail_code
        varchar operation_type
        text description
        varchar visibility
        varchar isolation_type
        varchar service_type
        boolean response_body_log
        varchar invoke_type
        text resource_spec
        varchar effective_flag
        varchar audit_status
    }

    publish_history {
        bigint id PK
        varchar version_id FK
        varchar config_type
        varchar status
        text gray_groups
        varchar operator
        datetime gmt_create
        datetime gmt_modified
    }
```

```mermaid
erDiagram
    data_source_config {
        bigint id PK
        varchar version_id UK
        varchar source
        varchar source_group
        varchar gateway_type
        varchar dm
        varchar loghub_endpoint
        varchar loghub_project
        varchar loghub_stream
        varchar loghub_accesskey_id
        varchar loghub_accesskey_secret
        varchar loghub_assume_role_arn
        varchar loghub_cursor
        text consume_region
        int data_fetch_interval_millis
        varchar status
        text effective_gray_groups
        datetime gmt_create
        datetime gmt_modified
    }

    api_record_config {
        bigint id PK
        varchar version_id UK
        varchar gateway_type
        varchar gateway_code
        varchar api_version
        varchar api_name
        varchar loghub_stream
        text basic_config
        text event_config
        text user_identity_config
        text request_config
        text response_config
        text filter_config
        text reference_resource_config
        varchar type
        varchar status
        text effective_gray_groups
        datetime gmt_create
        datetime gmt_modified
    }

    api_meta_config {
        bigint id PK
        varchar version_id UK
        varchar api_name
        varchar product
        varchar gateway_type
        varchar dm
        varchar gateway_code
        varchar api_version
        varchar actiontrail_code
        varchar operation_type
        varchar description
        varchar visibility
        varchar isolation_type
        varchar service_type
        tinyint response_body_log
        varchar invoke_type
        varchar resource_spec
        varchar status
        text effective_gray_groups
        varchar effective_flag
        varchar audit_status
        datetime gmt_create
        datetime gmt_modified
    }

    publish_history {
        bigint id PK
        varchar version_id
        varchar config_type
        varchar status
        text gray_groups
        varchar operator
        datetime gmt_create
        datetime gmt_modified
    }

    data_source_config ||--o{ publish_history : "records"
    api_record_config ||--o{ publish_history : "records"
    api_meta_config ||--o{ publish_history : "records"
```


```mermaid
stateDiagram-v2
    [*] --> Draft: 创建配置
    Draft --> V1_Stage1: 发布配置-阶段 1
    
    state "版本1" as V1 {
        V1_Stage1 --> V1_Stage2: 发布配置-阶段 2
        V1_Stage2 --> V1_Full: 发布配置-全量发布
        note right of V1_Stage1: ap-southeast-2
        note right of V1_Stage2: cn-chengdu,ap-southeast-2,cn-shanghai
        note right of V1_Full: 所有地域
    }
    
    V1_Full --> V2_Draft: 更新配置(创建新版本)
    
    state "版本共存阶段" as Coexist {
        state "版本1(已发布)" as V1_Active {
            V1_Published: 全量发布状态
        }
        
        state "版本2(灰度中)" as V2_Active {
            V2_Draft --> V2_Stage1: 发布新版本--阶段 1
            V2_Stage1 --> V2_Stage2: 发布配置-阶段 2
            V2_Stage2 --> V2_Full: 全量发布
            note right of V2_Stage1: ap-southeast-2
            note right of V2_Stage2: cn-chengdu,ap-southeast-2,cn-shanghai
            note right of V2_Full: 所有地域
        }
    }
    
    V2_Full --> V1_Deprecated: 版本2全量发布后,版本1自动废弃
    V1_Deprecated --> [*]

```

```mermaid
sequenceDiagram
    participant Client
    participant Service
    participant DB
    participant V1 as 版本1
    participant V2 as 版本2
    
    %% 创建和发布版本1
    Client->>Service: 创建配置
    Service->>DB: 插入配置(status=DRAFT)
    Service-->>Client: 返回版本1配置(versionId=DS202501090001)
    
    Client->>Service: 发布版本1到阶段1(ap-southeast-2)
    Service->>V1: 更新状态(status=PUBLISHED)
    Service->>V1: 设置灰度组(effective_gray_groups=ap-southeast-2)
    Service->>DB: 记录发布历史
    
    Client->>Service: 发布版本1到阶段2
    Service->>V1: 更新灰度组(effective_gray_groups=cn-chengdu,ap-southeast-2,cn-shanghai)
    Service->>DB: 记录发布历史
    
    Client->>Service: 版本1全量发布
    Service->>V1: 更新灰度组(effective_gray_groups=所有地域)
    Service->>DB: 记录发布历史
    
    %% 创建和发布版本2
    Client->>Service: 更新配置(创建新版本)
    Service->>DB: 插入配置(status=DRAFT)
    Service-->>Client: 返回版本2配置(versionId=DS202501090002)
    
    %% 版本共存阶段
    Note over V1,V2: 版本共存阶段开始
    
    Client->>Service: 发布版本2到阶段1
    Service->>V2: 更新状态(status=PUBLISHED)
    Service->>V2: 设置灰度组(effective_gray_groups=ap-southeast-2)
    Service->>DB: 记录发布历史
    
    Note over V1,V2: 此时版本1在其他地域生效,版本2在ap-southeast-2生效
    
    Client->>Service: 发布版本2到阶段2
    Service->>V2: 更新灰度组(effective_gray_groups=cn-chengdu,ap-southeast-2,cn-shanghai)
    Service->>DB: 记录发布历史
    
    Note over V1,V2: 此时版本1在剩余地域生效,版本2在指定地域生效
    
    Client->>Service: 版本2全量发布
    Service->>V2: 更新灰度组(effective_gray_groups=所有地域)
    Service->>V1: 更新状态(status=DEPRECATED)
    Service->>DB: 记录发布历史
    
    Note over V1,V2: 版本2完全接管,版本1废弃
```

### 发布接口

#### 发布配置

```
POST /api/publish
```

#### 按照阶段发布配置

```
POST /api/publish/stage
```

#### 回滚配置

```
POST /api/publish/rollback
```

#### 废弃配置

```
POST /api/publish/deprecate
```

#### 回滚到上一个版本

```
POST /api/publish/rollback/previous
```

#### 获取发布历史

```
GET /api/publish/history
```

#### 获取所有灰度阶段配置

```
GET /api/publish/stage
```

### 数据源配置接口

#### 创建数据源配置

```
POST /api/config/create
```

#### 更新数据源配置

```
POST /api/config/update
```

#### 获取当前地域生效的所有数据源配置

```
GET /api/config/active
```

#### 获取指定source的所有已发布数据源配置

```
GET /api/config/published
```

#### 获取指定source在指定地域生效的数据源配置

```
GET /api/config/active
```

#### 获取配置变更信息

```
POST /api/datasource/diff
```

### 1. 数据源配置接口
| 接口描述 | 请求方式 | 接口路径 |
|---------|---------|---------|
| 创建数据源配置 | POST | /api/datasource |
| 更新数据源配置 | PUT | /api/datasource/{versionId} |
| 获取当前地域的数据源配置 | GET | /api/datasource/current-region |
| 获取指定地域的数据源配置 | GET | /api/datasource/region/{region} |
| 获取指定source在当前地域生效的配置 | GET | /api/datasource/{source}/active |
| 获取指定source的所有已发布配置 | GET | /api/datasource/{source}/published |
| 获取指定source在指定地域的生效配置 | GET | /api/datasource/{source}/region/{region} |
| 获取配置变更信息 | POST | /api/datasource/diff |

### 2. API记录配置接口
| 接口描述 | 请求方式 | 接口路径 |
|---------|---------|---------|
| 创建API记录配置 | POST | /api/api-record |
| 更新API记录配置 | PUT | /api/api-record/{versionId} |
| 获取所有已发布的API记录配置 | GET | /api/api-record/published/all |
| 获取指定地域的API记录配置 | GET | /api/api-record/region/{region} |
| 获取指定API在指定地域生效的配置 | GET | /api/api-record/active |
| 获取指定API的所有已发布配置 | GET | /api/api-record/published |

### 3. API Meta配置接口
| 接口描述 | 请求方式 | 接口路径 |
|---------|---------|---------|
| 创建API Meta配置 | POST | /api/api-meta |
| 更新API Meta配置 | PUT | /api/api-meta/{versionId} |
| 获取所有已发布的API Meta配置 | GET | /api/api-meta/published/all |
| 获取指定地域的API Meta配置 | GET | /api/api-meta/region/{region} |
| 获取指定API在指定地域生效的配置 | GET | /api/api-meta/active |
| 获取指定API的所有已发布配置 | GET | /api/api-meta/published |
| 根据版本ID查询配置 | GET | /api/api-meta/version/{versionId} |

### 4. 配置发布接口
| 接口描述 | 请求方式 | 接口路径 |
|---------|---------|---------|
| 发布配置 | POST | /api/publish |
| 按阶段发布配置 | POST | /api/publish/stage |
| 回滚配置 | POST | /api/publish/rollback |
| 废弃配置 | POST | /api/publish/deprecate |
| 获取发布历史 | GET | /api/publish/history/{versionId} |
| 获取所有灰度阶段信息 | GET | /api/publish/stages |

## 灰度发布阶段说明
- **阶段1 (STAGE_1)**: 仅在 ap-southeast-2 生效
- **阶段2 (STAGE_2)**: 在 cn-chengdu、ap-southeast-2、cn-shanghai 生效
- **全量发布 (FULL)**: 在所有地域生效，使用 "all" 标识

## 配置状态说明
- **DRAFT**: 草稿状态，新建或修改后的配置状态
- **PUBLISHED**: 已发布状态，配置已发布到一个或多个灰度组
- **DEPRECATED**: 已废弃状态，配置已被废弃或被新版本替代

## 版本号规则
- 数据源配置：DS + 年月日 + 4位序号，如：DS202401150001
- API记录配置：AR + 年月日 + 4位序号，如：AR202401150001
- API Meta配置：AM + 年月日 + 4位序号，如：AM202401150001

## 支持的地域列表
- cn-hangzhou
- cn-shanghai
- ap-southeast-1
- cn-chengdu
- ap-southeast-2
- ...