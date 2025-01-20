package com.example.dto;

import com.example.model.bo.ApiRecordConfigBO;
import lombok.Data;

@Data
public class UpdateApiRecordRequest {
    /**
     * 版本ID
     */
    private String versionId;
    
    /**
     * 配置内容
     */
    private ApiRecordConfigBO config;
} 