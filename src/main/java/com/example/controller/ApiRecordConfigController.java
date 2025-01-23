package com.example.controller;

import com.example.dto.ConfigDiffRequest;
import com.example.dto.ConfigDiffResponse;
import com.example.model.bo.ApiRecordConfigBO;
import com.example.service.ApiRecordConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import com.example.model.response.OpenApiResponse;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;
import java.time.LocalDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.UUID;
import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/api/api-record")
@Tag(name = "API记录配置", description = "API记录配置管理接口")
public class ApiRecordConfigController {

    @Autowired
    private ApiRecordConfigService apiRecordConfigService;

    @Data
    public static class CreateRequest {
        @Schema(description = "网关类型", required = true)
        @NotBlank(message = "网关类型不能为空")
        private String gatewayType;

        @Schema(description = "网关编码", required = true)
        @NotBlank(message = "网关编码不能为空")
        private String gatewayCode;

        @Schema(description = "API版本", required = true)
        @NotBlank(message = "API版本不能为空")
        private String apiVersion;

        @Schema(description = "API名称", required = true)
        @NotBlank(message = "API名称不能为空")
        private String apiName;

        @Schema(description = "基础配置JSON", required = true)
        @NotBlank(message = "基础配置不能为空")
        private String basicConfig;

        @Schema(description = "事件配置JSON")
        private String eventConfig;

        @Schema(description = "用户身份配置JSON")
        private String userIdentityConfig;

        @Schema(description = "请求配置JSON")
        private String requestConfig;

        @Schema(description = "响应配置JSON")
        private String responseConfig;

        @Schema(description = "过滤配置JSON")
        private String filterConfig;

        @Schema(description = "引用资源配置JSON")
        private String referenceResourceConfig;
    }

    @Data
    public static class UpdateRequest extends CreateRequest {
        @Schema(description = "版本ID", required = true)
        @NotBlank(message = "版本ID不能为空")
        private String versionId;
        private String gatewayType;
        private String gatewayCode;
        private String apiVersion;
        private String apiName;
        private String basicConfig;
        private String eventConfig;
        private String userIdentityConfig;
        private String requestConfig;
        private String responseConfig;
        private String filterConfig;
        private String referenceResourceConfig;
    }

    @Data
    public static class GetByApiRequest {
        private String gatewayType;
        private String gatewayCode;
        private String apiVersion;
        private String apiName;
    }

    @Data 
    public static class GetActiveRequest {
        private String gatewayType;
        private String gatewayCode;
        private String apiVersion;
        private String apiName;
        private String region;
    }

    @Data
    public static class ApiRecordResponse {
        @Schema(description = "版本ID")
        private String versionId;

        @Schema(description = "网关类型")
        private String gatewayType;

        @Schema(description = "网关编码")
        private String gatewayCode;

        @Schema(description = "API版本")
        private String apiVersion;

        @Schema(description = "API名称")
        private String apiName;

        @Schema(description = "配置状态")
        private String configStatus;

        @Schema(description = "基础配置JSON")
        private String basicConfig;

        @Schema(description = "事件配置JSON")
        private String eventConfig;

        @Schema(description = "用户身份配置JSON")
        private String userIdentityConfig;

        @Schema(description = "请求配置JSON")
        private String requestConfig;

        @Schema(description = "响应配置JSON")
        private String responseConfig;

        @Schema(description = "过滤配置JSON")
        private String filterConfig;

        @Schema(description = "引用资源配置JSON")
        private String referenceResourceConfig;

        @Schema(description = "创建时间")
        private LocalDateTime gmtCreate;

        @Schema(description = "修改时间")
        private LocalDateTime gmtModified;

        public static ApiRecordResponse fromBO(ApiRecordConfigBO bo) {
            if (bo == null) {
                return null;
            }
            ApiRecordResponse response = new ApiRecordResponse();
            BeanUtils.copyProperties(bo, response);
            return response;
        }
    }

    @Data
    public static class DeleteRequest {
        @Schema(description = "版本ID")
        private String versionId;

        @Schema(description = "网关类型")
        private String gatewayType;

        @Schema(description = "网关编码")
        private String gatewayCode;

        @Schema(description = "API版本")
        private String apiVersion;

        @Schema(description = "API名称")
        private String apiName;
    }

    @Data
    @Builder
    public static class ApiRecordDiffResponse {
        private List<ApiRecordResponse> updatedConfigs;  // 新增或更新的配置
        private List<String> activeVersionIds;           // 当前生效的版本号
        private List<String> deprecatedVersionIds;       // 已失效的版本号
    }

    @Data
    public static class QueryApiRecordRequest {
        @Schema(description = "按版本ID查询")
        private String versionId;
        
        @Schema(description = "按网关类型查询")
        private String gatewayType;
        
        @Schema(description = "按网关编码查询")
        private String gatewayCode;
        
        @Schema(description = "按API版本查询")
        private String apiVersion;
        
        @Schema(description = "按API名称查询")
        private String apiName;
        
        @Schema(description = "按地域查询")
        private String region;
        
        @Schema(description = "是否只查询已发布配置")
        private Boolean onlyPublished;
        
        @Schema(description = "是否只查询当前生效配置")
        private Boolean activeOnly;
        
        @Schema(description = "按配置状态查询，支持多个状态", example = "['DRAFT', 'GRAYING']")
        private List<String> configStatus;
    }

    /**
     * 创建API记录配置
     */
    @Operation(summary = "创建API记录配置")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @PostMapping("/create")
    public OpenApiResponse<ApiRecordResponse> create(
        @RequestBody 
        @Valid 
        @Parameter(description = "创建参数", required = true) 
        CreateRequest request
    ) {
        ApiRecordConfigBO config = new ApiRecordConfigBO();
        config.setGatewayType(request.getGatewayType());
        config.setGatewayCode(request.getGatewayCode());
        config.setApiVersion(request.getApiVersion());
        config.setApiName(request.getApiName());
        config.setBasicConfig(request.getBasicConfig());
        config.setEventConfig(request.getEventConfig());
        config.setUserIdentityConfig(request.getUserIdentityConfig());
        config.setRequestConfig(request.getRequestConfig());
        config.setResponseConfig(request.getResponseConfig());
        config.setFilterConfig(request.getFilterConfig());
        config.setReferenceResourceConfig(request.getReferenceResourceConfig());
        
        ApiRecordConfigBO created = apiRecordConfigService.create(config);
        return OpenApiResponse.success(ApiRecordResponse.fromBO(created), UUID.randomUUID().toString());
    }

    /**
     * 更新API记录配置
     */
    @Operation(summary = "更新API记录配置")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "404", description = "未找到配置")
    })
    @PostMapping("/update")
    public OpenApiResponse<ApiRecordResponse> update(
        @RequestBody 
        @Valid 
        @Parameter(description = "更新参数", required = true) 
        UpdateRequest request
    ) {
        ApiRecordConfigBO config = new ApiRecordConfigBO();
        config.setGatewayType(request.getGatewayType());
        config.setGatewayCode(request.getGatewayCode());
        config.setApiVersion(request.getApiVersion());
        config.setApiName(request.getApiName());
        config.setBasicConfig(request.getBasicConfig());
        config.setEventConfig(request.getEventConfig());
        config.setUserIdentityConfig(request.getUserIdentityConfig());
        config.setRequestConfig(request.getRequestConfig());
        config.setResponseConfig(request.getResponseConfig());
        config.setFilterConfig(request.getFilterConfig());
        config.setReferenceResourceConfig(request.getReferenceResourceConfig());
        
        ApiRecordConfigBO updated = apiRecordConfigService.update(request.getVersionId(), config);
        return OpenApiResponse.success(ApiRecordResponse.fromBO(updated), UUID.randomUUID().toString());
    }

    /**
     * 获取指定版本的配置
     */
    @GetMapping("/get")
    @Operation(summary = "获取指定版本的配置")
    public ResponseEntity<ApiRecordResponse> getConfig(@RequestParam String versionId) {
        return ResponseEntity.ok(ApiRecordResponse.fromBO(
            apiRecordConfigService.findByVersionId(versionId)));
    }

    /**
     * 获取所有已发布的配置
     */
    @GetMapping("/published")
    @Operation(summary = "获取所有已发布的配置")
    public ResponseEntity<List<ApiRecordResponse>> getAllPublished() {
        return ResponseEntity.ok(
            apiRecordConfigService.getAllPublished().stream()
                .map(ApiRecordResponse::fromBO)
                .collect(Collectors.toList())
        );
    }

    /**
     * 获取指定API的所有已发布配置
     */
    @PostMapping("/published/by-api")
    @Operation(summary = "获取指定API的所有已发布配置")
    public ResponseEntity<List<ApiRecordResponse>> getPublishedByApi(@RequestBody GetByApiRequest request) {
        String identifier = String.format("%s:%s:%s:%s",
            request.getGatewayType(),
            request.getGatewayCode(),
            request.getApiVersion(),
            request.getApiName()
        );
        return ResponseEntity.ok(
            apiRecordConfigService.getPublishedByIdentifier(identifier).stream()
                .map(ApiRecordResponse::fromBO)
                .collect(Collectors.toList())
        );
    }

    /**
     * 获取指定API在指定地域生效的配置
     */
    @PostMapping("/active")
    @Operation(summary = "获取指定API在指定地域生效的配置")
    public ResponseEntity<ApiRecordResponse> getActiveConfig(@RequestBody GetActiveRequest request) {
        String identifier = String.format("%s:%s:%s:%s",
            request.getGatewayType(),
            request.getGatewayCode(),
            request.getApiVersion(),
            request.getApiName()
        );
        return ResponseEntity.ok(ApiRecordResponse.fromBO(
            apiRecordConfigService.getActiveByIdentifierAndRegion(identifier, request.getRegion())
        ));
    }

    /**
     * 获取指定地域生效的所有配置
     */
    @GetMapping("/active/by-region")
    @Operation(summary = "获取指定地域生效的所有配置")
    public ResponseEntity<List<ApiRecordResponse>> getActiveByRegion(@RequestParam String region) {
        return ResponseEntity.ok(
            apiRecordConfigService.getActiveByRegion(region).stream()
                .map(ApiRecordResponse::fromBO)
                .collect(Collectors.toList())
        );
    }

    /**
     * 查询API记录配置
     * 支持以下查询场景：
     * 1. 按版本ID查询指定配置
     * 2. 查询所有已发布配置
     * 3. 查询指定API的所有已发布配置
     * 4. 查询指定API在指定地域生效的配置
     * 5. 查询指定地域生效的所有配置
     * 6. 按配置状态查询（如草稿、灰度中等）
     */
    @Operation(summary = "查询API记录配置", description = "统一查询接口，支持多种查询场景")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "404", description = "未找到配置")
    })
    @PostMapping("/query")
    public OpenApiResponse<List<ApiRecordResponse>> queryApiRecords(
        @RequestBody 
        @Valid 
        @Parameter(description = "查询参数", required = true)
        QueryApiRecordRequest request
    ) {
        // 1. 按版本ID查询
        if (request.getVersionId() != null) {
            ApiRecordConfigBO config = apiRecordConfigService.findByVersionId(request.getVersionId());
            return OpenApiResponse.success(Collections.singletonList(ApiRecordResponse.fromBO(config)), UUID.randomUUID().toString());
        }

        // 2. 按配置状态查询
        if (request.getConfigStatus() != null && !request.getConfigStatus().isEmpty()) {
            // 如果同时指定了API标识，则查询指定API的特定状态配置
            if (isApiIdentifierComplete(request)) {
                String identifier = buildIdentifier(request);
                return OpenApiResponse.success(
                        apiRecordConfigService.findByIdentifierAndStatus(identifier, request.getConfigStatus())
                                .stream()
                                .map(ApiRecordResponse::fromBO)
                                .collect(Collectors.toList()),
                        UUID.randomUUID().toString()
                );
            }
            // 否则查询所有特定状态的配置
            return OpenApiResponse.success(
                    apiRecordConfigService.findByStatus(request.getConfigStatus())
                            .stream()
                            .map(ApiRecordResponse::fromBO)
                            .collect(Collectors.toList()),
                    UUID.randomUUID().toString()
            );
        }

        // 3. 查询所有已发布配置
        if (Boolean.TRUE.equals(request.getOnlyPublished())
                && request.getGatewayType() == null
                && request.getRegion() == null) {
            return OpenApiResponse.success(
                    apiRecordConfigService.getAllPublished().stream()
                            .map(ApiRecordResponse::fromBO)
                            .collect(Collectors.toList()),
                    UUID.randomUUID().toString()
            );
        }

        // 4. 查询指定API的已发布配置
        if (isApiIdentifierComplete(request) && Boolean.TRUE.equals(request.getOnlyPublished())) {
            String identifier = buildIdentifier(request);
            return OpenApiResponse.success(
                    apiRecordConfigService.getPublishedByIdentifier(identifier).stream()
                            .map(ApiRecordResponse::fromBO)
                            .collect(Collectors.toList()),
                    UUID.randomUUID().toString()
            );
        }

        // 5. 查询指定地域生效的配置
        if (request.getRegion() != null) {
            // 5.1 查询指定API在指定地域的生效配置
            if (isApiIdentifierComplete(request) && Boolean.TRUE.equals(request.getActiveOnly())) {
                String identifier = buildIdentifier(request);
                ApiRecordConfigBO config = apiRecordConfigService.getActiveByIdentifierAndRegion(
                        identifier,
                        request.getRegion()
                );
                return OpenApiResponse.success(Collections.singletonList(ApiRecordResponse.fromBO(config)), UUID.randomUUID().toString());
            }

            // 5.2 查询指定地域的所有生效配置
            return OpenApiResponse.success(
                    apiRecordConfigService.getActiveByRegion(request.getRegion()).stream()
                            .map(ApiRecordResponse::fromBO)
                            .collect(Collectors.toList()),
                    UUID.randomUUID().toString()
            );
        }

        throw new IllegalArgumentException("Invalid query parameters combination");
    }

    private boolean isApiIdentifierComplete(QueryApiRecordRequest request) {
        return request.getGatewayType() != null
                && request.getGatewayCode() != null
                && request.getApiVersion() != null
                && request.getApiName() != null;
    }

    private String buildIdentifier(QueryApiRecordRequest request) {
        return String.format("%s:%s:%s:%s",
                request.getGatewayType(),
                request.getGatewayCode(),
                request.getApiVersion(),
                request.getApiName()
        );
    }
    /**
     * 获取配置变更信息
     */
    @PostMapping("/diff")
    @Operation(summary = "获取配置变更信息", description = "比较客户端版本与服务端版本的差异，返回需要更新的配置")
    public ResponseEntity<ApiRecordDiffResponse> getConfigDiff(@RequestBody ConfigDiffRequest request) {
        ConfigDiffResponse<ApiRecordConfigBO> boResponse = apiRecordConfigService.getConfigDiff(
            request.getVersionIds(),
            request.getRegion()
        );
        
        return ResponseEntity.ok(ApiRecordDiffResponse.builder()
            .updatedConfigs(boResponse.getUpdatedConfigs().stream()
                .map(ApiRecordResponse::fromBO)
                .collect(Collectors.toList()))
            .activeVersionIds(boResponse.getActiveVersionIds())
            .deprecatedVersionIds(boResponse.getDeprecatedVersionIds())
            .build());
    }

    /**
     * 删除配置（仅支持删除草稿和废弃状态的配置）
     */
    @Operation(summary = "删除API记录配置", description = "只能删除草稿或废弃状态的配置")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "404", description = "未找到配置")
    })
    @PostMapping("/delete")
    public OpenApiResponse<Void> delete(
        @RequestBody 
        @Valid 
        @Parameter(description = "删除参数", required = true) 
        DeleteRequest request
    ) {
        if (request.getVersionId() != null) {
            apiRecordConfigService.deleteByVersionIdWithStatusCheck(request.getVersionId());
        } else {
            String identifier = String.format("%s:%s:%s:%s",
                request.getGatewayType(),
                request.getGatewayCode(),
                request.getApiVersion(),
                request.getApiName()
            );
            apiRecordConfigService.deleteByIdentifierWithStatusCheck(identifier);
        }
        return OpenApiResponse.success(null, UUID.randomUUID().toString());
    }
}