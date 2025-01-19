package com.example.dto;

import com.example.model.BaseVersionedConfig;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConfigDiffResponse<T extends BaseVersionedConfig> {
    /**
     * 新增或更新的配置
     */
    private List<T> updatedConfigs;
    
    /**
     * 已失效的配置版本ID列表
     */
    private List<String> deprecatedVersionIds;
} 