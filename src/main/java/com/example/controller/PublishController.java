package com.example.controller;

import com.example.enums.GrayStage;
import com.example.model.PublishHistory;
import com.example.model.response.OpenApiResponse;
import com.example.service.PublishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.Data;
import io.swagger.v3.oas.annotations.Parameter;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/publish")
@Tag(name = "配置发布", description = "配置发布相关接口")
public class PublishController {
    
    @Autowired
    private PublishService publishService;

    @Data
    @Schema(description = "发布请求")
    public static class PublishRequest {
        @Schema(description = "版本ID", required = true)
        @NotBlank(message = "版本ID不能为空")
        private String versionId;
        
        @Schema(description = "配置类型", required = true)
        @NotBlank(message = "配置类型不能为空")
        private String configType;
        
        @Schema(description = "灰度阶段", required = true)
        @NotBlank(message = "灰度阶段不能为空")
        private String stage;
        
        @Schema(description = "操作人", required = true)
        @NotBlank(message = "操作人不能为空")
        private String operator;
    }

    @Data
    @Schema(description = "回滚请求")
    public static class RollbackRequest {
        @Schema(description = "配置标识", required = true)
        @NotBlank(message = "配置标识不能为空")
        private String identifier;
        
        @Schema(description = "目标版本ID")
        private String targetVersionId;
        
        @Schema(description = "配置类型", required = true)
        @NotBlank(message = "配置类型不能为空")
        private String configType;
        
        @Schema(description = "操作人", required = true)
        @NotBlank(message = "操作人不能为空")
        private String operator;
    }

    @Data
    @Schema(description = "发布历史查询请求")
    public static class HistoryRequest {
        @Schema(description = "版本ID")
        private String versionId;
        
        @Schema(description = "配置类型")
        private String configType;
        
        @Schema(description = "灰度阶段")
        private String stage;
    }

    /**
     * 按阶段发布配置
     */
    @PostMapping("/stage")
    @Operation(summary = "按阶段发布配置")
    public OpenApiResponse<Boolean> publishByStage(
        @RequestBody 
        @Valid 
        @Parameter(description = "发布参数", required = true)
        PublishRequest request
    ) {
        log.info("Publishing config by stage: {}", request);

        publishService.publishByStage(
            request.getVersionId(),
            request.getConfigType(),
            GrayStage.valueOf(request.getStage()),
            request.getOperator()
        );
        return OpenApiResponse.success(true, UUID.randomUUID().toString());
    }

    /**
     * 废弃配置
     */
    @PostMapping("/deprecate")
    @Operation(summary = "废弃配置")
    public OpenApiResponse<Boolean> deprecate(
        @RequestBody 
        @Valid 
        @Parameter(description = "废弃参数", required = true)
        PublishRequest request
    ) {
        log.info("Deprecating config: {}", request);
        publishService.deprecateConfig(
            request.getVersionId(),
            request.getConfigType(),
            request.getOperator()
        );
        return OpenApiResponse.success(true, UUID.randomUUID().toString());
    }

    /**
     * 终止灰度发布
     */
    @PostMapping("/rollback/gray")
    @Operation(summary = "终止灰度发布", description = "终止当前正在灰度中的配置，使用全量发布的配置")
    public OpenApiResponse<Boolean> rollbackGray(
        @RequestBody 
        @Valid 
        @Parameter(description = "回滚参数", required = true)
        RollbackRequest request
    ) {
        log.info("Rolling back graying config: {}", request);
        publishService.rollbackGrayConfig(
            request.getIdentifier(),
            request.getConfigType(),
            request.getOperator()
        );
        return OpenApiResponse.success(true, UUID.randomUUID().toString());
    }

    /**
     * 回滚到历史版本
     */
    @PostMapping("/rollback/version")
    @Operation(summary = "回滚到历史版本", description = "废弃当前生效的配置，使用指定的历史版本")
    public OpenApiResponse<Boolean> rollbackVersion(
        @RequestBody 
        @Valid 
        @Parameter(description = "回滚参数", required = true)
        RollbackRequest request
    ) {
        log.info("Rolling back to version: {}", request);
        if (request.getTargetVersionId() == null) {
            throw new IllegalArgumentException("Target version ID is required");
        }
        publishService.rollbackToVersion(
            request.getIdentifier(),
            request.getTargetVersionId(),
            request.getConfigType(),
            request.getOperator()
        );
        return OpenApiResponse.success(true, UUID.randomUUID().toString());
    }

    /**
     * 获取发布历史
     */
    @PostMapping("/history")
    @Operation(summary = "获取发布历史")
    public OpenApiResponse<List<PublishHistory>> getPublishHistory(
        @RequestBody 
        @Valid 
        @Parameter(description = "查询参数", required = true)
        HistoryRequest request
    ) {
        log.info("Getting publish history: {}", request);
        
        List<PublishHistory> history;
        if (request.getVersionId() != null) {
            history = publishService.getPublishHistory(request.getVersionId());
        } else if (request.getConfigType() != null && request.getStage() != null) {
            history = publishService.getHistoryByTypeAndStage(
                request.getConfigType(), 
                request.getStage()
            );
        } else {
            throw new IllegalArgumentException("Must provide either versionId or (configType and stage)");
        }
        return OpenApiResponse.success(history, UUID.randomUUID().toString());
    }

    /**
     * 获取所有灰度阶段信息
     */
    @PostMapping("/stages")
    @Operation(summary = "获取所有灰度阶段信息")
    public OpenApiResponse<Map<String, List<String>>> getGrayStages() {
        Map<String, List<String>> stages = new HashMap<>();
        for (GrayStage stage : GrayStage.values()) {
            stages.put(stage.name(), stage.getRegions());
        }
        return OpenApiResponse.success(stages, UUID.randomUUID().toString());
    }
} 