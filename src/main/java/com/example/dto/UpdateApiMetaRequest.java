package com.example.dto;

import com.example.model.bo.ApiMetaConfigBO;
import lombok.Data;

@Data
public class UpdateApiMetaRequest {
    /**
     * 版本ID
     */
    private String versionId;
    
    /**
     * 配置内容
     */
    private ApiMetaConfigBO config;
} 