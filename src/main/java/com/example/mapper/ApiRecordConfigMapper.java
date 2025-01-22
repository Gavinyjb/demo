package com.example.mapper;

import com.example.model.ApiRecordConfig;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ApiRecordConfigMapper {
    @Insert("INSERT INTO config_version (" +
            "version_id, identifier, config_type, config_status, " +
            "gmt_create, gmt_modified" +
            ") VALUES (" +
            "#{versionId}, " +
            "CONCAT(#{gatewayType}, ':', #{gatewayCode}, ':', #{apiVersion}, ':', #{apiName}), " +
            "'API_RECORD', #{configStatus}, " +
            "NOW(), NOW()" +
            ")")
    void insertVersion(ApiRecordConfig config);

    @Insert("INSERT INTO api_record_config (" +
            "version_id, gateway_type, gateway_code, api_version, api_name, " +
            "basic_config, event_config, user_identity_config, request_config, " +
            "response_config, filter_config, reference_resource_config, " +
            "gmt_create, gmt_modified) " +
            "VALUES (#{versionId}, #{gatewayType}, #{gatewayCode}, #{apiVersion}, #{apiName}, " +
            "#{basicConfig}, #{eventConfig}, #{userIdentityConfig}, #{requestConfig}, " +
            "#{responseConfig}, #{filterConfig}, #{referenceResourceConfig}, " +
            "NOW(), NOW())")
    void insertApiRecord(ApiRecordConfig config);

    @Select("SELECT c.id, c.version_id, c.gateway_type, c.gateway_code, " +
            "c.api_version, c.api_name, c.basic_config, c.event_config, " +
            "c.user_identity_config, c.request_config, c.response_config, " +
            "c.filter_config, c.reference_resource_config, " +
            "c.gmt_create, c.gmt_modified, v.config_status " +
            "FROM api_record_config c " +
            "INNER JOIN config_version v ON c.version_id = v.version_id " +
            "WHERE c.version_id = #{versionId}")
    ApiRecordConfig findByVersionId(@Param("versionId") String versionId);

    @Select("SELECT c.id, c.version_id, c.gateway_type, c.gateway_code, " +
            "c.api_version, c.api_name, c.basic_config, c.event_config, " +
            "c.user_identity_config, c.request_config, c.response_config, " +
            "c.filter_config, c.reference_resource_config, " +
            "c.gmt_create, c.gmt_modified, v.config_status " +
            "FROM api_record_config c " +
            "INNER JOIN config_version v ON c.version_id = v.version_id " +
            "WHERE v.config_status = 'PUBLISHED'")
    List<ApiRecordConfig> findAllPublished();

    @Select("SELECT c.id, c.version_id, c.gateway_type, c.gateway_code, " +
            "c.api_version, c.api_name, c.basic_config, c.event_config, " +
            "c.user_identity_config, c.request_config, c.response_config, " +
            "c.filter_config, c.reference_resource_config, " +
            "c.gmt_create, c.gmt_modified, v.config_status " +
            "FROM api_record_config c " +
            "INNER JOIN config_version v ON c.version_id = v.version_id " +
            "WHERE v.identifier = #{identifier} " +
            "AND v.config_status = 'PUBLISHED' " +
            "ORDER BY v.gmt_modified DESC")
    List<ApiRecordConfig> findPublishedByIdentifier(@Param("identifier") String identifier);

    @Select("SELECT c.id, c.version_id, c.gateway_type, c.gateway_code, " +
            "c.api_version, c.api_name, c.basic_config, c.event_config, " +
            "c.user_identity_config, c.request_config, c.response_config, " +
            "c.filter_config, c.reference_resource_config, " +
            "c.gmt_create, c.gmt_modified, v.config_status " +
            "FROM api_record_config c " +
            "INNER JOIN config_version v ON c.version_id = v.version_id " +
            "WHERE v.identifier = #{identifier} " +
            "ORDER BY v.gmt_modified DESC")
    List<ApiRecordConfig> findAllVersionsByIdentifier(@Param("identifier") String identifier);

    @Update("UPDATE config_version SET config_status = #{configStatus} " +
            "WHERE version_id = #{versionId}")
    void updateVersionStatus(
        @Param("versionId") String versionId,
        @Param("configStatus") String configStatus
    );

    @Insert("INSERT INTO config_gray_release (version_id, stage) " +
            "VALUES (#{versionId}, #{stage})")
    void insertGrayRelease(
        @Param("versionId") String versionId,
        @Param("stage") String stage
    );

    @Select("SELECT c.id, c.version_id, c.gateway_type, c.gateway_code, " +
            "c.api_version, c.api_name, c.basic_config, c.event_config, " +
            "c.user_identity_config, c.request_config, c.response_config, " +
            "c.filter_config, c.reference_resource_config, " +
            "c.gmt_create, c.gmt_modified, v.config_status " +
            "FROM api_record_config c " +
            "INNER JOIN config_version v ON c.version_id = v.version_id " +
            "LEFT JOIN config_gray_release g ON v.version_id = g.version_id " +
            "WHERE v.config_status IN ('PUBLISHED', 'GRAYING') " +
            "AND g.stage IN (" +
            "  SELECT DISTINCT stage FROM (" +
            "    SELECT 'FULL' as stage " +
            "    UNION " +
            "    SELECT 'STAGE_1' WHERE #{stage} = 'STAGE_1' " +
            "    UNION " +
            "    SELECT 'STAGE_2' WHERE #{stage} IN ('STAGE_1', 'STAGE_2')" +
            "  ) stages" +
            ")")
    List<ApiRecordConfig> findByStage(@Param("stage") String stage);

    @Delete("DELETE FROM api_record_config WHERE version_id = #{versionId}")
    void deleteByVersionId(@Param("versionId") String versionId);

    @Delete("DELETE c, v, g " +
            "FROM api_record_config c " +
            "LEFT JOIN config_version v ON c.version_id = v.version_id " +
            "LEFT JOIN config_gray_release g ON v.version_id = g.version_id " +
            "WHERE v.identifier = #{identifier} " +
            "AND v.config_status = #{configStatus}")
    void deleteByIdentifierAndStatus(
        @Param("identifier") String identifier,
        @Param("configStatus") String configStatus
    );

    @Select("SELECT c.id, c.version_id, c.gateway_type, c.gateway_code, " +
            "c.api_version, c.api_name, c.basic_config, c.event_config, " +
            "c.user_identity_config, c.request_config, c.response_config, " +
            "c.filter_config, c.reference_resource_config, " +
            "c.gmt_create, c.gmt_modified, v.config_status " +
            "FROM api_record_config c " +
            "INNER JOIN config_version v ON c.version_id = v.version_id " +
            "LEFT JOIN config_gray_release g ON v.version_id = g.version_id " +
            "WHERE v.identifier = #{identifier} " +
            "AND v.config_status IN ('PUBLISHED', 'GRAYING') " +
            "AND (g.stage = #{stage} OR g.stage = 'FULL') " +
            "ORDER BY g.stage = #{stage} DESC, v.gmt_modified DESC " +
            "LIMIT 1")
    ApiRecordConfig findActiveConfigByIdentifierAndStage(
        @Param("identifier") String identifier,
        @Param("stage") String stage
    );

    /**
     * 按版本ID和状态列表删除配置
     */
    @Delete({
        "<script>",
        "DELETE c, v FROM api_record_config c ",
        "LEFT JOIN config_version v ON c.version_id = v.version_id ",
        "WHERE c.version_id = #{versionId} ",
        "AND v.config_status IN ",
        "<foreach collection='statusList' item='status' open='(' separator=',' close=')'>",
        "#{status}",
        "</foreach>",
        "</script>"
    })
    void deleteByVersionIdAndStatusIn(
        @Param("versionId") String versionId,
        @Param("statusList") List<String> statusList
    );

    /**
     * 按标识和状态列表删除配置
     */
    @Delete({
        "<script>",
        "DELETE c, v FROM api_record_config c ",
        "LEFT JOIN config_version v ON c.version_id = v.version_id ",
        "WHERE v.identifier = #{identifier} ",
        "AND v.config_status IN ",
        "<foreach collection='statusList' item='status' open='(' separator=',' close=')'>",
        "#{status}",
        "</foreach>",
        "</script>"
    })
    void deleteByIdentifierAndStatusIn(
        @Param("identifier") String identifier,
        @Param("statusList") List<String> statusList
    );
} 