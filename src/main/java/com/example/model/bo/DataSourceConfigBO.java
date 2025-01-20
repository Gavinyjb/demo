package com.example.model.bo;

import com.example.model.DataSourceConfig;
import com.example.model.config.WorkerConfig;
import com.example.utils.JsonUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

@Data
@EqualsAndHashCode(callSuper = true)
public class DataSourceConfigBO extends DataSourceConfig {
    /**
     * 工作配置
     */
    private WorkerConfig workerConfigObject;
    
    public static DataSourceConfigBO fromDO(DataSourceConfig dataSourceConfig) {
        DataSourceConfigBO bo = new DataSourceConfigBO();
        BeanUtils.copyProperties(dataSourceConfig, bo);
        bo.setWorkerConfigObject(JsonUtils.parseObject(dataSourceConfig.getWorkerConfig(), WorkerConfig.class));
        return bo;
    }
    
    public DataSourceConfig toDO() {
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        BeanUtils.copyProperties(this, dataSourceConfig);
        dataSourceConfig.setWorkerConfig(JsonUtils.toJsonString(this.workerConfigObject));
        return dataSourceConfig;
    }
} 