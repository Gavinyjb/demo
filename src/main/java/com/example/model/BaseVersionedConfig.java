package com.example.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 版本化配置基类
 * 包含版本控制和灰度发布所需的公共字段
 */
@Data
public abstract class BaseVersionedConfig implements ConfigIdentifier {
    private Long id;
    private String versionId;
    private String status;
    private String effectiveGrayGroups;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
} 