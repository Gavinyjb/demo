package com.example.enums;

/**
 * 配置状态枚举
 */
public enum ConfigStatus {
    /**
     * 草稿状态
     */
    DRAFT,
    
    /**
     * 已发布状态
     */
    PUBLISHED,
    
    /**
     * 已废弃状态
     */
    DEPRECATED;

    public static boolean contains(String status) {
        try {
            valueOf(status);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
 
 