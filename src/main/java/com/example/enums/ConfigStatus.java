package com.example.enums;

/**
 * 配置状态枚举
 */
public enum ConfigStatus {
    /**
     * 草稿状态
     */
    DRAFT("草稿"),
    
    /**
     * 灰度中状态
     */
    GRAYING("灰度中"),
    
    /**
     * 已发布状态
     */
    PUBLISHED("已发布"),
    
    /**
     * 已废弃状态
     */
    DEPRECATED("废弃");

    private final String description;

    ConfigStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static boolean contains(String status) {
        try {
            valueOf(status);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
 
 