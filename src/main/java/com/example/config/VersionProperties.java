package com.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "config.version")
public class VersionProperties {
    /**
     * 每个数据源配置保留的最大版本数
     */
    private int maxDatasourceVersions = 5;
    
    /**
     * 每个API配置保留的最大版本数
     */
    private int maxApirecordVersions = 5;
} 