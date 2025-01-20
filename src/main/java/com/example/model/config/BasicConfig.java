package com.example.model.config;

import lombok.Data;

@Data
public class BasicConfig {
    /**
     * 请求方法
     */
    private String method;
    
    /**
     * 请求路径
     */
    private String path;
    
    /**
     * 服务名称
     */
    private String serviceName;
    
    /**
     * 操作名称
     */
    private String actionName;
} 