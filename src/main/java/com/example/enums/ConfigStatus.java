package com.example.enums;

/**
 * 配置状态枚举
 */
public enum ConfigStatus {
    /**
     * 草稿状态：新建或修改后的配置状态
     */
    DRAFT,
    
    /**
     * 已发布状态：配置已发布到一个或多个灰度组
     */
    PUBLISHED,
    
    /**
     * 已废弃状态：配置已被废弃或被新版本替代
     */
    DEPRECATED
}
 
 