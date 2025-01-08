package com.example.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PublishHistory {
    private Long id;
    private String versionId;
    private String configType;
    private String status;
    private String grayGroups;
    private String operator;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
} 