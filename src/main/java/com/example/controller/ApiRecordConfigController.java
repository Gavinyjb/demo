//package com.example.controller;
//
//import com.example.dto.*;
//import com.example.model.bo.ApiRecordConfigBO;
//import com.example.service.ApiRecordConfigService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/apirecord")
//@Tag(name = "API记录配置", description = "API记录配置相关接口")
//public class ApiRecordConfigController {
//
//    @Autowired
//    private ApiRecordConfigService apiRecordConfigService;
//
//    /**
//     * 创建API记录配置
//     */
//    @PostMapping("/create")
//    @Operation(summary = "创建API记录配置")
//    public ResponseEntity<ApiRecordConfigBO> create(@RequestBody ApiRecordConfigBO config) {
//        return ResponseEntity.ok(apiRecordConfigService.create(config));
//    }
//
//    /**
//     * 更新API记录配置
//     */
//    @PostMapping("/update")
//    @Operation(summary = "更新API记录配置")
//    public ResponseEntity<ApiRecordConfigBO> update(@RequestBody UpdateApiRecordRequest request) {
//        return ResponseEntity.ok(
//            apiRecordConfigService.update(request.getVersionId(), request.getConfig())
//        );
//    }
//
//    /**
//     * 获取指定版本的配置
//     */
//    @PostMapping("/get")
//    @Operation(summary = "获取指定版本的配置")
//    public ResponseEntity<ApiRecordConfigBO> getConfig(@RequestBody GetApiRecordRequest request) {
//        return ResponseEntity.ok(apiRecordConfigService.findByVersionId(request.getVersionId()));
//    }
//
//    /**
//     * 获取所有已发布的配置
//     */
//    @PostMapping("/published")
//    @Operation(summary = "获取所有已发布的配置")
//    public ResponseEntity<List<ApiRecordConfigBO>> getAllPublished() {
//        return ResponseEntity.ok(apiRecordConfigService.getAllPublished());
//    }
//
//    /**
//     * 获取指定API的所有已发布配置
//     */
//    @PostMapping("/published/by-api")
//    @Operation(summary = "获取指定API的所有已发布配置")
//    public ResponseEntity<List<ApiRecordConfigBO>> getPublishedByApi(@RequestBody GetApiRecordRequest request) {
//        String identifier = String.format("%s:%s:%s:%s",
//            request.getGatewayType(),
//            request.getGatewayCode(),
//            request.getApiVersion(),
//            request.getApiName()
//        );
//        return ResponseEntity.ok(apiRecordConfigService.getPublishedByIdentifier(identifier));
//    }
//
//    /**
//     * 获取指定API在指定地域生效的配置
//     */
//    @PostMapping("/active")
//    @Operation(summary = "获取指定API在指定地域生效的配置")
//    public ResponseEntity<ApiRecordConfigBO> getActiveConfig(@RequestBody GetApiRecordRequest request) {
//        String identifier = String.format("%s:%s:%s:%s",
//            request.getGatewayType(),
//            request.getGatewayCode(),
//            request.getApiVersion(),
//            request.getApiName()
//        );
//        return ResponseEntity.ok(
//            apiRecordConfigService.getActiveByIdentifierAndRegion(identifier, request.getRegion())
//        );
//    }
//
//    /**
//     * 获取指定地域生效的所有配置
//     */
//    @PostMapping("/active/by-region")
//    @Operation(summary = "获取指定地域生效的所有配置")
//    public ResponseEntity<List<ApiRecordConfigBO>> getActiveByRegion(@RequestBody GetApiRecordRequest request) {
//        return ResponseEntity.ok(apiRecordConfigService.getActiveByRegion(request.getRegion()));
//    }
//
//    /**
//     * 获取配置变更信息
//     */
//    @PostMapping("/diff")
//    @Operation(summary = "获取配置变更信息")
//    public ResponseEntity<ConfigDiffResponse<ApiRecordConfigBO>> getConfigDiff(
//            @RequestBody ConfigDiffRequest request) {
//        return ResponseEntity.ok(apiRecordConfigService.getConfigDiff(request));
//    }
//}