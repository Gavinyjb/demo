package com.example.model.bo;

import com.example.model.DataSourceConfig;
import com.example.model.config.WorkerConfig;
import com.example.util.JsonUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class DataSourceConfigBO extends DataSourceConfig {
    /**
     * 工作配置
     */
    private WorkerConfig workerConfigObject;
    
    public static DataSourceConfigBO fromDO(DataSourceConfig dataSourceConfig) {
        if (dataSourceConfig == null) {
            return null;
        }
        DataSourceConfigBO bo = new DataSourceConfigBO();
        BeanUtils.copyProperties(dataSourceConfig, bo, "gmtCreate", "gmtModified");
        
        // 显式处理时间字段
        bo.setGmtCreate(dataSourceConfig.getGmtCreate());
        bo.setGmtModified(dataSourceConfig.getGmtModified());
        
        bo.setWorkerConfigObject(JsonUtils.parseObject(dataSourceConfig.getWorkerConfig(), WorkerConfig.class));
        return bo;
    }
    
    public DataSourceConfig toDO() {
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        BeanUtils.copyProperties(this, dataSourceConfig, "gmtCreate", "gmtModified");
        
        // 显式处理时间字段
        dataSourceConfig.setGmtCreate(this.getGmtCreate() != null ? this.getGmtCreate() : LocalDateTime.now());
        dataSourceConfig.setGmtModified(this.getGmtModified() != null ? this.getGmtModified() : LocalDateTime.now());
        
        dataSourceConfig.setWorkerConfig(JsonUtils.toJsonString(this.workerConfigObject));
        return dataSourceConfig;
    }
} 