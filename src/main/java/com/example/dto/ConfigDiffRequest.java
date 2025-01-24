package com.example.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConfigDiffRequest {
    /**
     * 当前持有的配置版本ID列表
     */
    private List<String> versionIds;
    
    /**
     * 查询的地域
     */
    private String regionId;
} 