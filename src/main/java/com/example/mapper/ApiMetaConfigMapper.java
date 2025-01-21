package com.example.mapper;

import com.example.model.ApiMetaConfig;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ApiMetaConfigMapper {
    @Insert("INSERT INTO amp_api_meta (" +
            "version_id, api_name, product, gateway_type, dm, " +
            "gateway_code, api_version, actiontrail_code, operation_type, description, " +
            "visibility, isolation_type, service_type, response_body_log, invoke_type, " +
            "resource_spec, effective_flag, audit_status, " +
            "gmt_create, gmt_modified) " +
            "VALUES (#{versionId}, #{apiName}, #{product}, #{gatewayType}, #{dm}, " +
            "#{gatewayCode}, #{apiVersion}, #{actiontrailCode}, #{operationType}, #{description}, " +
            "#{visibility}, #{isolationType}, #{serviceType}, #{responseBodyLog}, #{invokeType}, " +
            "#{resourceSpec}, #{effectiveFlag}, #{auditStatus}, " +
            "NOW(), NOW())")
    void insertApiMeta(ApiMetaConfig config);

    @Insert("INSERT INTO config_version (" +
            "version_id, identifier, config_type, config_status, " +
            "gmt_create, gmt_modified" +
            ") VALUES (" +
            "#{versionId}, " +
            "CONCAT(#{gatewayType}, ':', #{gatewayCode}, ':', #{apiVersion}, ':', #{apiName}), " +
            "'API_META', #{configStatus}, " +
            "NOW(), NOW()" +
            ")")
    void insertVersion(ApiMetaConfig config);

    @Select("SELECT m.id, m.version_id, m.api_name, m.product, m.gateway_type, " +
            "m.dm, m.gateway_code, m.api_version, m.actiontrail_code, m.operation_type, " +
            "m.description, m.visibility, m.isolation_type, m.service_type, " +
            "m.response_body_log, m.invoke_type, m.resource_spec, m.effective_flag, " +
            "m.audit_status, m.gmt_create, m.gmt_modified, v.config_status " +
            "FROM amp_api_meta m " +
            "INNER JOIN config_version v ON m.version_id = v.version_id " +
            "WHERE m.version_id = #{versionId}")
    ApiMetaConfig findByVersionId(String versionId);

    @Select("SELECT m.id, m.version_id, m.api_name, m.product, m.gateway_type, " +
            "m.dm, m.gateway_code, m.api_version, m.actiontrail_code, m.operation_type, " +
            "m.description, m.visibility, m.isolation_type, m.service_type, " +
            "m.response_body_log, m.invoke_type, m.resource_spec, m.effective_flag, " +
            "m.audit_status, m.gmt_create, m.gmt_modified, v.config_status " +
            "FROM amp_api_meta m " +
            "INNER JOIN config_version v ON m.version_id = v.version_id " +
            "WHERE v.config_status = 'PUBLISHED'")
    List<ApiMetaConfig> findAllPublished();

    @Select("SELECT m.id, m.version_id, m.api_name, m.product, m.gateway_type, " +
            "m.dm, m.gateway_code, m.api_version, m.actiontrail_code, m.operation_type, " +
            "m.description, m.visibility, m.isolation_type, m.service_type, " +
            "m.response_body_log, m.invoke_type, m.resource_spec, m.effective_flag, " +
            "m.audit_status, m.gmt_create, m.gmt_modified, v.config_status " +
            "FROM amp_api_meta m " +
            "INNER JOIN config_version v ON m.version_id = v.version_id " +
            "LEFT JOIN config_gray_release g ON v.version_id = g.version_id " +
            "WHERE v.identifier = #{identifier} " +
            "AND v.config_status IN ('PUBLISHED', 'GRAYING') " +
            "AND (g.stage = #{stage} OR g.stage = 'FULL') " +
            "ORDER BY CASE WHEN g.stage = #{stage} THEN 1 ELSE 0 END DESC, " +
            "v.gmt_modified DESC " +
            "LIMIT 1")
    ApiMetaConfig findActiveConfigByIdentifierAndStage(
        @Param("identifier") String identifier,
        @Param("stage") String stage
    );

    @Select("SELECT m.id, m.version_id, m.api_name, m.product, m.gateway_type, " +
            "m.dm, m.gateway_code, m.api_version, m.actiontrail_code, m.operation_type, " +
            "m.description, m.visibility, m.isolation_type, m.service_type, " +
            "m.response_body_log, m.invoke_type, m.resource_spec, m.effective_flag, " +
            "m.audit_status, m.gmt_create, m.gmt_modified, v.config_status " +
            "FROM amp_api_meta m " +
            "INNER JOIN config_version v ON m.version_id = v.version_id " +
            "WHERE v.identifier = #{identifier} " +
            "AND v.config_status = 'PUBLISHED' " +
            "ORDER BY v.gmt_modified DESC")
    List<ApiMetaConfig> findPublishedByIdentifier(@Param("identifier") String identifier);

    @Select("SELECT m.id, m.version_id, m.api_name, m.product, m.gateway_type, " +
            "m.dm, m.gateway_code, m.api_version, m.actiontrail_code, m.operation_type, " +
            "m.description, m.visibility, m.isolation_type, m.service_type, " +
            "m.response_body_log, m.invoke_type, m.resource_spec, m.effective_flag, " +
            "m.audit_status, m.gmt_create, m.gmt_modified, v.config_status " +
            "FROM amp_api_meta m " +
            "INNER JOIN config_version v ON m.version_id = v.version_id " +
            "LEFT JOIN config_gray_release g ON v.version_id = g.version_id " +
            "WHERE v.config_status = 'PUBLISHED' " +
            "AND (g.stage = #{stage} OR g.stage = 'FULL')")
    List<ApiMetaConfig> findByStage(@Param("stage") String stage);

    @Delete("DELETE FROM amp_api_meta WHERE version_id = #{versionId}")
    void deleteByVersionId(@Param("versionId") String versionId);
} 