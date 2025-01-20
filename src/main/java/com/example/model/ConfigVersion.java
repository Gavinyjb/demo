package com.example.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Builder
@Data
public class ConfigVersion {
    private String versionId;
    private String identifier;
    private String configType;
    private String configStatus;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
} 