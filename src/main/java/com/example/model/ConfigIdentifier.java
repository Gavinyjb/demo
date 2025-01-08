package com.example.model;

/**
 * 配置标识接口
 * 用于标识不同类型配置的唯一性
 */
public interface ConfigIdentifier {
    /**
     * 获取配置的唯一标识
     * 不同类型的配置通过实现此方法返回其唯一标识
     *
     * @return 配置的唯一标识字符串
     */
    String getIdentifier();
} 