package com.example.dto;

import com.example.model.bo.DataSourceConfigBO;
import lombok.Data;

@Data
public class UpdateConfigRequest {
    /**
     * 版本ID
     */
    private String versionId;
    
    /**
     * 配置内容
     */
    private DataSourceConfigBO config;
} 