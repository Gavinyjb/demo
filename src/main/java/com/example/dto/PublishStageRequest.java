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
    private String configType;  // DATA_SOURCE|API_RECORD|API_META
    
    /**
     * 灰度阶段
     */
    private String stage;       // STAGE_1|STAGE_2|FULL
    
    /**
     * 操作人
     */
    private String operator;
} 