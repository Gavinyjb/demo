package com.example.model.bo;

import com.example.model.ApiMetaConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApiMetaConfigBO extends ApiMetaConfig {
    
    public static ApiMetaConfigBO fromDO(ApiMetaConfig apiMetaConfig) {
        if (apiMetaConfig == null) {
            return null;
        }
        ApiMetaConfigBO bo = new ApiMetaConfigBO();
        BeanUtils.copyProperties(apiMetaConfig, bo, "gmtCreate", "gmtModified");
        
        // 显式处理时间字段
        bo.setGmtCreate(apiMetaConfig.getGmtCreate());
        bo.setGmtModified(apiMetaConfig.getGmtModified());
        
        return bo;
    }
    
    public ApiMetaConfig toDO() {
        ApiMetaConfig apiMetaConfig = new ApiMetaConfig();
        BeanUtils.copyProperties(this, apiMetaConfig, "gmtCreate", "gmtModified");
        
        // 显式处理时间字段
        apiMetaConfig.setGmtCreate(this.getGmtCreate() != null ? this.getGmtCreate() : LocalDateTime.now());
        apiMetaConfig.setGmtModified(this.getGmtModified() != null ? this.getGmtModified() : LocalDateTime.now());
        
        return apiMetaConfig;
    }
} 