# DataSource Config 生命周期测试流程

## 1. 创建配置

```bash
curl -X POST 'http://localhost:8080/api/datasource' \
-H 'Content-Type: application/json' \
-d '{
    "source": "test-source",
    "sourceGroup": "test-group",
    "gatewayType": "API",
    "dm": "DATA",
    "slsEndpoint": "cn-hangzhou.log.aliyuncs.com",
    "slsProject": "test-project",
    "slsLogstore": "test-logstore",
    "slsAccountId": "123456789",
    "slsAssumeRoleArn": "acs:ram::123456789:role/test-role",
    "slsCursor": "begin",
    "consumeRegion": "cn-hangzhou",
    "workerConfig": "{\"fetchIntervalMillis\": 1000}"
}'

# 返回包含 version_id，记录下来用于后续操作，假设为 v1
```

## 2. 灰度阶段1发布

```bash
curl -X POST 'http://localhost:8080/api/publish/stage' \
-H 'Content-Type: application/json' \
-d '{
    "versionId": "DS202501200001",
    "configType": "DATA_SOURCE",
    "stage": "STAGE_1",
    "operator": "admin"
}'

# 验证灰度阶段1的配置
curl -X GET 'http://localhost:8080/api/datasource/active?region=ap-southeast-2'
```

## 3. 灰度阶段2发布

```bash
curl -X POST 'http://localhost:8080/api/publish/stage' \
-H 'Content-Type: application/json' \
-d '{
    "versionId": "DS202501200001",
    "configType": "DATA_SOURCE",
    "stage": "STAGE_2",
    "operator": "admin"
}'

# 验证灰度阶段2的配置
curl -X GET 'http://localhost:8080/api/datasource/active?region=cn-shanghai'
```

## 4. 全量发布

```bash
curl -X POST 'http://localhost:8080/api/publish/stage' \
-H 'Content-Type: application/json' \
-d '{
    "versionId": "DS202501200001",
    "configType": "DATA_SOURCE",
    "stage": "FULL",
    "operator": "admin"
}'

# 验证全量发布的配置
curl -X GET 'http://localhost:8080/api/datasource/active?region=cn-hangzhou'
```

## 5. 更新配置

创建新版本：

```bash
curl -X PUT 'http://localhost:8080/api/datasource/v1' \
-H 'Content-Type: application/json' \
-d '{
    "source": "test-source",
    "sourceGroup": "test-group",
    "gatewayType": "API",
    "dm": "DATA",
    "slsEndpoint": "cn-hangzhou.log.aliyuncs.com",
    "slsProject": "test-project-v2",
    "slsLogstore": "test-logstore-v2",
    "slsAccountId": "123456789",
    "slsAssumeRoleArn": "acs:ram::123456789:role/test-role-v2",
    "slsCursor": "begin",
    "consumeRegion": "cn-hangzhou",
    "workerConfig": "{\"fetchIntervalMillis\": 2000}"
}'

# 返回新的 version_id，假设为 v2
```

## 6. 新版本灰度发布

```bash
curl -X POST 'http://localhost:8080/api/publish/stage' \
-H 'Content-Type: application/json' \
-d '{
    "versionId": "v2",
    "configType": "DATA_SOURCE",
    "stage": "STAGE_1",
    "operator": "admin"
}'

# 验证新版本在灰度地域生效
curl -X GET 'http://localhost:8080/api/datasource/active?region=ap-southeast-2'
```

## 7. 新版本全量发布

```bash
curl -X POST 'http://localhost:8080/api/publish/stage' \
-H 'Content-Type: application/json' \
-d '{
    "versionId": "v2",
    "configType": "DATA_SOURCE",
    "stage": "FULL",
    "operator": "admin"
}'
```

## 8. 验证配置

### 查看发布历史

```bash
curl -X GET 'http://localhost:8080/api/publish/history/v2'
```

### 查看当前生效配置

```bash
curl -X GET 'http://localhost:8080/api/datasource/active/test-source?region=cn-hangzhou'
```

### 查看配置变更

```bash
curl -X POST 'http://localhost:8080/api/datasource/diff' \
-H 'Content-Type: application/json' \
-d '{
    "region": "cn-hangzhou",
    "versionIds": ["v1"]
}'
```

### 查看所有已发布配置

```bash
curl -X GET 'http://localhost:8080/api/datasource/published/test-source'
```