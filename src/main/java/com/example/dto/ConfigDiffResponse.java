package com.example.dto;

import com.example.model.DataSourceConfig;
import lombok.Data;

import java.util.List;

@Data
public class ConfigDiffResponse {
    /**
     * 新增或更新的配置
     */
    private List<DataSourceConfig> updatedConfigs;
    
    /**
     * 已失效的配置版本ID列表
     */
    private List<String> deprecatedVersionIds;
} 