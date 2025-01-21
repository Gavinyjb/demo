package com.example.model.bo;

import com.example.model.ApiMetaConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApiMetaConfigBO extends ApiMetaConfig {
    
    public static ApiMetaConfigBO fromDO(ApiMetaConfig apiMetaConfig) {
        if (apiMetaConfig == null) {
            return null;
        }
        ApiMetaConfigBO bo = new ApiMetaConfigBO();
        BeanUtils.copyProperties(apiMetaConfig, bo);
        return bo;
    }
    
    public ApiMetaConfig toDO() {
        ApiMetaConfig apiMetaConfig = new ApiMetaConfig();
        BeanUtils.copyProperties(this, apiMetaConfig);
        return apiMetaConfig;
    }
} 