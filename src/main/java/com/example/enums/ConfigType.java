package com.example.enums;

import lombok.Getter;

/**
 * 配置类型枚举
 */
@Getter
public enum ConfigType {
    /**
     * 数据源配置
     */
    DATA_SOURCE("数据源配置", "DS", "conf_data_source_config"),

    /**
     * API记录配置
     */
    API_RECORD("API记录配置", "AR", "api_record_config"),

    /**
     * API元数据配置
     */
    API_META("API元数据配置", "AM", "amp_api_meta");

    /**
     * 配置类型描述
     */
    private final String description;

    /**
     * 版本ID前缀
     */
    private final String versionPrefix;

    /**
     * 对应的数据库表名
     */
    private final String tableName;

    ConfigType(String description, String versionPrefix, String tableName) {
        this.description = description;
        this.versionPrefix = versionPrefix;
        this.tableName = tableName;
    }

    /**
     * 检查是否包含指定的配置类型
     */
    public static boolean contains(String type) {
        try {
            valueOf(type);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 根据类型名称获取枚举实例
     */
    public static ConfigType fromString(String type) {
        try {
            return valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported config type: " + type);
        }
    }

    /**
     * 获取配置类型的字符串值
     */
    public String getValue() {
        return this.name();
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", this.name(), this.description);
    }
} 