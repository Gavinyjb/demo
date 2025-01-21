package com.example.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 版本化配置基类
 * 包含版本控制和灰度发布所需的公共字段
 */
@Data
public abstract class BaseVersionedConfig {
    /**
     * 主键ID
     */
    protected Long id;
    
    /**
     * 版本ID
     */
    protected String versionId;
    
    /**
     * 配置状态
     */
    protected String configStatus;  // DRAFT|PUBLISHED|DEPRECATED
    
    /**
     * 创建时间
     */
    protected LocalDateTime gmtCreate;
    
    /**
     * 修改时间
     */
    protected LocalDateTime gmtModified;

    /**
     * 获取配置标识
     */
    public abstract String getIdentifier();
} 