package com.example.dto;

import com.example.enums.GrayStage;
import lombok.Data;

@Data
public class PublishStageRequest {
    /**
     * 配置版本号
     */
    private String versionId;
    
    /**
     * 配置类型
     */
    private String configType;
    
    /**
     * 灰度阶段
     */
    private GrayStage stage;
    
    /**
     * 操作人
     */
    private String operator;
} 