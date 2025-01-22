package com.example.controller;

import com.example.model.bo.ApiRecordConfigBO;
import com.example.service.ApiRecordConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/apirecord")
@Tag(name = "API记录配置", description = "API记录配置相关接口")
public class ApiRecordConfigController {

    @Autowired
    private ApiRecordConfigService apiRecordConfigService;

    @Data
    public static class CreateRequest {
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
    public static class UpdateRequest {
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
    public static class DiffRequest {
        private List<String> versionIds;
        private String region;
    }

    @Data
    public static class ApiRecordResponse {
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
        private String configStatus;
        private String gmtCreate;
        private String gmtModified;

        public static ApiRecordResponse fromBO(ApiRecordConfigBO bo) {
            if (bo == null) {
                return null;
            }
            ApiRecordResponse response = new ApiRecordResponse();
            response.setVersionId(bo.getVersionId());
            response.setGatewayType(bo.getGatewayType());
            response.setGatewayCode(bo.getGatewayCode());
            response.setApiVersion(bo.getApiVersion());
            response.setApiName(bo.getApiName());
            response.setBasicConfig(bo.getBasicConfig());
            response.setEventConfig(bo.getEventConfig());
            response.setUserIdentityConfig(bo.getUserIdentityConfig());
            response.setRequestConfig(bo.getRequestConfig());
            response.setResponseConfig(bo.getResponseConfig());
            response.setFilterConfig(bo.getFilterConfig());
            response.setReferenceResourceConfig(bo.getReferenceResourceConfig());
            response.setConfigStatus(bo.getConfigStatus());
            response.setGmtCreate(bo.getGmtCreate() != null ? bo.getGmtCreate().toString() : null);
            response.setGmtModified(bo.getGmtModified() != null ? bo.getGmtModified().toString() : null);
            return response;
        }
    }

    /**
     * 创建API记录配置
     */
    @PostMapping("/create")
    @Operation(summary = "创建API记录配置")
    public ResponseEntity<ApiRecordResponse> create(@RequestBody CreateRequest request) {
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
        
        return ResponseEntity.ok(ApiRecordResponse.fromBO(apiRecordConfigService.create(config)));
    }

    /**
     * 更新API记录配置
     */
    @PostMapping("/update")
    @Operation(summary = "更新API记录配置")
    public ResponseEntity<ApiRecordResponse> update(@RequestBody UpdateRequest request) {
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
        
        return ResponseEntity.ok(ApiRecordResponse.fromBO(
            apiRecordConfigService.update(request.getVersionId(), config)));
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
     * 获取配置变更信息
     */
    @PostMapping("/diff")
    @Operation(summary = "获取配置变更信息")
    public ResponseEntity<List<String>> getConfigDiff(@RequestBody DiffRequest request) {
        return ResponseEntity.ok(
            apiRecordConfigService.getVersionDiff(request.getVersionIds(), request.getRegion())
        );
    }
}