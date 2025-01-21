package com.example.model.bo;

import com.example.model.ApiRecordConfig;
import com.example.model.config.BasicConfig;
import com.example.util.JsonUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApiRecordConfigBO extends ApiRecordConfig {

    private BasicConfig basicConfigObject;


    public static ApiRecordConfigBO fromDO(ApiRecordConfig apiRecordConfig) {
        if (apiRecordConfig == null) {
            return null;
        }
        
        ApiRecordConfigBO bo = new ApiRecordConfigBO();
        BeanUtils.copyProperties(apiRecordConfig, bo);
        bo.setBasicConfigObject(JsonUtils.parseObject(apiRecordConfig.getBasicConfig(), BasicConfig.class));
        return bo;
    }

    public ApiRecordConfig toDO() {
        ApiRecordConfig entity = new ApiRecordConfig();
        BeanUtils.copyProperties(this, entity);
        entity.setBasicConfig(JsonUtils.toJsonString(this.getBasicConfigObject()));
        return entity;
    }
} 