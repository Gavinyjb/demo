package com.example.model;


import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PublishHistory {
    private String versionId;
    private String configType;  // DATA_SOURCE|API_RECORD|API_META
    private String configStatus;  // DRAFT|PUBLISHED|DEPRECATED
    private String stage;       // STAGE_1|STAGE_2|FULL
    private String operator;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
} 