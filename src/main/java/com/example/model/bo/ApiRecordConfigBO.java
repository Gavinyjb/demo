package com.example.model.bo;

import com.example.model.ApiRecordConfig;
import com.example.model.config.BasicConfig;
import com.example.utils.JsonUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApiRecordConfigBO extends ApiRecordConfig {
    /**
     * 基础配置
     */
    private BasicConfig basicConfigObject;
    
    public static ApiRecordConfigBO fromDO(ApiRecordConfig apiRecordConfig) {
        ApiRecordConfigBO bo = new ApiRecordConfigBO();
        BeanUtils.copyProperties(apiRecordConfig, bo);
        bo.setBasicConfigObject(JsonUtils.parseObject(apiRecordConfig.getBasicConfig(), BasicConfig.class));
        return bo;
    }
    
    public ApiRecordConfig toDO() {
        ApiRecordConfig apiRecordConfig = new ApiRecordConfig();
        BeanUtils.copyProperties(this, apiRecordConfig);
        apiRecordConfig.setBasicConfig(JsonUtils.toJsonString(this.basicConfigObject));
        return apiRecordConfig;
    }
} 