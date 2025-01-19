package com.example.enums;

/**
 * 配置类型枚举
 */
public enum ConfigType {
    /**
     * 数据源配置
     */
    DATA_SOURCE,

    /**
     * API记录配置
     */
    API_RECORD,

    /**
     * API元数据配置
     */
    API_META;

    public static boolean contains(String type) {
        try {
            valueOf(type);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
} 