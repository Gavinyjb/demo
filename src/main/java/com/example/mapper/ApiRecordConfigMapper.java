package com.example.mapper;

import com.example.model.ApiRecordConfig;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ApiRecordConfigMapper {
    @Insert("INSERT INTO api_record_config (" +
            "version_id, gateway_type, gateway_code, api_version, api_name, " +
            "basic_config, event_config, user_identity_config, request_config, " +
            "response_config, filter_config, reference_resource_config, " +
            "gmt_create, gmt_modified" +
            ") VALUES (" +
            "#{versionId}, #{gatewayType}, #{gatewayCode}, #{apiVersion}, #{apiName}, " +
            "#{basicConfig}, #{eventConfig}, #{userIdentityConfig}, #{requestConfig}, " +
            "#{responseConfig}, #{filterConfig}, #{referenceResourceConfig}, " +
            "NOW(), NOW()" +
            ")")
    void insertApiRecord(ApiRecordConfig config);

    @Insert("INSERT INTO config_version (" +
            "version_id, identifier, config_type, config_status, " +
            "gmt_create, gmt_modified" +
            ") VALUES (" +
            "#{versionId}, #{identifier}, 'API_RECORD', #{configStatus}, " +
            "NOW(), NOW()" +
            ")")
    void insertVersion(ApiRecordConfig config);

    @Select("SELECT r.*, v.config_status " +
            "FROM api_record_config r " +
            "INNER JOIN config_version v ON r.version_id = v.version_id " +
            "WHERE r.version_id = #{versionId}")
    ApiRecordConfig findByVersionId(String versionId);

    @Select("SELECT r.*, v.config_status " +
            "FROM api_record_config r " +
            "INNER JOIN config_version v ON r.version_id = v.version_id " +
            "WHERE v.config_status = 'PUBLISHED'")
    List<ApiRecordConfig> findAllPublished();

    @Select("SELECT r.*, v.config_status " +
            "FROM api_record_config r " +
            "INNER JOIN config_version v ON r.version_id = v.version_id " +
            "LEFT JOIN config_gray_release g ON v.version_id = g.version_id " +
            "WHERE v.identifier = #{identifier} " +
            "AND v.config_status = 'PUBLISHED' " +
            "AND (g.stage = #{stage} OR g.stage = 'FULL') " +
            "ORDER BY g.stage = 'FULL' DESC, v.gmt_modified DESC " +
            "LIMIT 1")
    ApiRecordConfig findActiveConfigByIdentifierAndStage(
        @Param("identifier") String identifier,
        @Param("stage") String stage
    );

    @Select("SELECT r.*, v.config_status " +
            "FROM api_record_config r " +
            "INNER JOIN config_version v ON r.version_id = v.version_id " +
            "WHERE r.gateway_type = #{gatewayType} " +
            "AND r.gateway_code = #{gatewayCode} " +
            "AND r.api_version = #{apiVersion} " +
            "AND r.api_name = #{apiName} " +
            "AND v.config_status = 'PUBLISHED' " +
            "ORDER BY v.gmt_modified DESC")
    List<ApiRecordConfig> findPublishedConfigsByIdentifier(
        @Param("gatewayType") String gatewayType,
        @Param("gatewayCode") String gatewayCode,
        @Param("apiVersion") String apiVersion,
        @Param("apiName") String apiName
    );

    @Update("UPDATE config_version SET config_status = #{configStatus} WHERE version_id = #{versionId}")
    void updateVersionStatus(@Param("versionId") String versionId, @Param("configStatus") String configStatus);

    @Insert("INSERT INTO config_gray_release (version_id, stage) VALUES (#{versionId}, #{stage})")
    void insertGrayRelease(@Param("versionId") String versionId, @Param("stage") String stage);

    @Select("SELECT r.*, v.config_status " +
            "FROM api_record_config r " +
            "INNER JOIN config_version v ON r.version_id = v.version_id " +
            "LEFT JOIN config_gray_release g ON v.version_id = g.version_id " +
            "WHERE v.config_status = 'PUBLISHED' " +
            "AND (g.stage = #{stage} OR g.stage = 'FULL')")
    List<ApiRecordConfig> findByStage(@Param("stage") String stage);

    @Delete("DELETE FROM api_record_config WHERE version_id = #{versionId}")
    void deleteByVersionId(@Param("versionId") String versionId);
} 