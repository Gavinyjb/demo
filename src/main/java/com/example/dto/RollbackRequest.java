package com.example.dto;

import lombok.Data;

@Data
public class RollbackRequest {
    /**
     * 配置标识
     */
    private String identifier;
    
    /**
     * 目标版本ID（可选，如果不提供则回滚到上一个版本）
     */
    private String targetVersionId;
    
    /**
     * 配置类型
     */
    private String configType;
    
    /**
     * 操作人
     */
    private String operator;
} 