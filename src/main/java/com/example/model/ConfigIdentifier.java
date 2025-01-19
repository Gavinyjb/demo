package com.example.model;

/**
 * 配置标识接口
 * 用于标识不同类型配置的唯一性
 */
public interface ConfigIdentifier {
    /**
     * 获取配置唯一标识
     * 数据源配置：source
     * API配置：gatewayType:gatewayCode:apiVersion:apiName
     */
    String getIdentifier();

    /**
     * 获取版本ID
     */
    String getVersionId();
} 