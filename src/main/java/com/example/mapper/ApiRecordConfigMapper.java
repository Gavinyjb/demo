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
            "version_id, identifier, config_type, status, " +
            "gmt_create, gmt_modified" +
            ") VALUES (" +
            "#{versionId}, #{identifier}, 'API_RECORD', #{status}, " +
            "NOW(), NOW()" +
            ")")
    void insertVersion(ApiRecordConfig config);

    @Select("SELECT a.*, v.status " +
            "FROM api_record_config a " +
            "INNER JOIN config_version v ON a.version_id = v.version_id " +
            "WHERE a.version_id = #{versionId}")
    ApiRecordConfig findByVersionId(String versionId);

    @Select("SELECT a.*, v.status " +
            "FROM api_record_config a " +
            "INNER JOIN config_version v ON a.version_id = v.version_id " +
            "WHERE v.status = 'PUBLISHED'")
    List<ApiRecordConfig> findAllPublished();

    @Select("SELECT a.*, v.status " +
            "FROM api_record_config a " +
            "INNER JOIN config_version v ON a.version_id = v.version_id " +
            "LEFT JOIN config_gray_release g ON v.version_id = g.version_id " +
            "WHERE a.gateway_type = #{gatewayType} " +
            "AND a.gateway_code = #{gatewayCode} " +
            "AND a.api_version = #{apiVersion} " +
            "AND a.api_name = #{apiName} " +
            "AND v.status = 'PUBLISHED' " +
            "AND (g.stage = #{stage} OR g.stage = 'FULL') " +
            "ORDER BY g.stage = 'FULL' DESC, v.gmt_modified DESC " +
            "LIMIT 1")
    ApiRecordConfig findActiveConfigByIdentifierAndStage(
        @Param("gatewayType") String gatewayType,
        @Param("gatewayCode") String gatewayCode,
        @Param("apiVersion") String apiVersion,
        @Param("apiName") String apiName,
        @Param("stage") String stage
    );

    @Select("SELECT a.*, v.status " +
            "FROM api_record_config a " +
            "INNER JOIN config_version v ON a.version_id = v.version_id " +
            "WHERE a.gateway_type = #{gatewayType} " +
            "AND a.gateway_code = #{gatewayCode} " +
            "AND a.api_version = #{apiVersion} " +
            "AND a.api_name = #{apiName} " +
            "AND v.status = 'PUBLISHED' " +
            "ORDER BY v.gmt_modified DESC")
    List<ApiRecordConfig> findPublishedConfigsByIdentifier(
        @Param("gatewayType") String gatewayType,
        @Param("gatewayCode") String gatewayCode,
        @Param("apiVersion") String apiVersion,
        @Param("apiName") String apiName
    );

    @Update("UPDATE config_version SET status = #{status} WHERE version_id = #{versionId}")
    void updateVersionStatus(@Param("versionId") String versionId, @Param("status") String status);

    @Insert("INSERT INTO config_gray_release (version_id, stage) VALUES (#{versionId}, #{stage})")
    void insertGrayRelease(@Param("versionId") String versionId, @Param("stage") String stage);

    @Select("SELECT a.*, v.status " +
            "FROM api_record_config a " +
            "INNER JOIN config_version v ON a.version_id = v.version_id " +
            "LEFT JOIN config_gray_release g ON v.version_id = g.version_id " +
            "WHERE v.status = 'PUBLISHED' " +
            "AND (g.stage = #{stage} OR g.stage = 'FULL')")
    List<ApiRecordConfig> findByStage(@Param("stage") String stage);
} 