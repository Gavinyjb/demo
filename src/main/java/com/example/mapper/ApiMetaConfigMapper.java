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
            "version_id, identifier, config_type, status, " +
            "gmt_create, gmt_modified) " +
            "VALUES (#{versionId}, #{identifier}, 'API_META', #{status}, " +
            "NOW(), NOW())")
    void insertVersion(ApiMetaConfig config);

    @Select("SELECT m.*, v.status " +
            "FROM amp_api_meta m " +
            "INNER JOIN config_version v ON m.version_id = v.version_id " +
            "WHERE m.version_id = #{versionId}")
    ApiMetaConfig findByVersionId(String versionId);

    @Select("SELECT m.*, v.status " +
            "FROM amp_api_meta m " +
            "INNER JOIN config_version v ON m.version_id = v.version_id " +
            "WHERE v.status = 'PUBLISHED'")
    List<ApiMetaConfig> findAllPublished();

    @Select("SELECT m.*, v.status " +
            "FROM amp_api_meta m " +
            "INNER JOIN config_version v ON m.version_id = v.version_id " +
            "LEFT JOIN config_gray_release g ON v.version_id = g.version_id " +
            "WHERE v.identifier = #{identifier} " +
            "AND v.status = 'PUBLISHED' " +
            "AND (g.stage = #{stage} OR g.stage = 'FULL') " +
            "ORDER BY g.stage = 'FULL' DESC, v.gmt_modified DESC " +
            "LIMIT 1")
    ApiMetaConfig findActiveConfigByIdentifierAndStage(
        @Param("identifier") String identifier,
        @Param("stage") String stage
    );

    @Select("SELECT m.*, v.status " +
            "FROM amp_api_meta m " +
            "INNER JOIN config_version v ON m.version_id = v.version_id " +
            "WHERE v.identifier = #{identifier} " +
            "AND v.status = 'PUBLISHED' " +
            "ORDER BY v.gmt_modified DESC")
    List<ApiMetaConfig> findPublishedConfigsByIdentifier(@Param("identifier") String identifier);

    @Update("UPDATE config_version SET status = #{status} WHERE version_id = #{versionId}")
    void updateVersionStatus(@Param("versionId") String versionId, @Param("status") String status);

    @Insert("INSERT INTO config_gray_release (version_id, stage) VALUES (#{versionId}, #{stage})")
    void insertGrayRelease(@Param("versionId") String versionId, @Param("stage") String stage);

    @Delete("DELETE m, v, g " +
            "FROM amp_api_meta m " +
            "LEFT JOIN config_version v ON m.version_id = v.version_id " +
            "LEFT JOIN config_gray_release g ON v.version_id = g.version_id " +
            "WHERE m.version_id = #{versionId}")
    void deleteByVersionId(String versionId);

    @Select("SELECT m.*, v.status " +
            "FROM amp_api_meta m " +
            "INNER JOIN config_version v ON m.version_id = v.version_id " +
            "LEFT JOIN config_gray_release g ON v.version_id = g.version_id " +
            "WHERE v.status = 'PUBLISHED' " +
            "AND (g.stage = #{stage} OR g.stage = 'FULL')")
    List<ApiMetaConfig> findByStage(@Param("stage") String stage);
} 