package com.example.mapper;

import com.example.model.ApiRecordConfig;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ApiRecordConfigMapper {
    @Insert("INSERT INTO api_record_config (" +
            "version_id, name, gateway_type, gateway_code, api_version, api_name, " +
            "worker_config, status, gmt_create, gmt_modified" +
            ") VALUES (" +
            "#{versionId}, #{name}, #{gatewayType}, #{gatewayCode}, #{apiVersion}, #{apiName}, " +
            "#{workerConfig}, #{status}, NOW(), NOW()" +
            ")")
    void insert(ApiRecordConfig config);

    @Select("SELECT * FROM api_record_config WHERE version_id = #{versionId}")
    ApiRecordConfig findByVersionId(@Param("versionId") String versionId);

    @Select("SELECT c.* FROM api_record_config c " +
            "INNER JOIN config_version v ON c.version_id = v.version_id " +
            "INNER JOIN config_gray_release g ON v.version_id = g.version_id " +
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

    @Select("SELECT c.* FROM api_record_config c " +
            "INNER JOIN config_version v ON c.version_id = v.version_id " +
            "WHERE v.config_status = 'PUBLISHED'")
    List<ApiRecordConfig> findAllPublished();

    @Delete("DELETE FROM api_record_config WHERE version_id = #{versionId}")
    void deleteByVersionId(@Param("versionId") String versionId);

    @Update("UPDATE api_record_config SET " +
            "name = #{name}, " +
            "gateway_type = #{gatewayType}, " +
            "gateway_code = #{gatewayCode}, " +
            "api_version = #{apiVersion}, " +
            "api_name = #{apiName}, " +
            "worker_config = #{workerConfig}, " +
            "status = #{status}, " +
            "gmt_modified = NOW() " +
            "WHERE version_id = #{versionId}")
    void update(ApiRecordConfig config);

    @Select("SELECT c.* FROM api_record_config c " +
            "INNER JOIN config_version v ON c.version_id = v.version_id " +
            "WHERE v.identifier = #{identifier} " +
            "AND v.config_type = 'API_RECORD' " +
            "AND v.config_status = 'PUBLISHED' " +
            "ORDER BY v.gmt_modified DESC")
    List<ApiRecordConfig> findPublishedByIdentifier(@Param("identifier") String identifier);

    @Select("SELECT * FROM api_record_config c " +
            "INNER JOIN config_version v ON c.version_id = v.version_id " +
            "WHERE c.gateway_type = #{gatewayType} " +
            "AND c.gateway_code = #{gatewayCode} " +
            "AND c.api_version = #{apiVersion} " +
            "AND c.api_name = #{apiName} " +
            "AND v.config_status = 'PUBLISHED'")
    List<ApiRecordConfig> findPublishedConfigsByIdentifier(
        @Param("gatewayType") String gatewayType,
        @Param("gatewayCode") String gatewayCode,
        @Param("apiVersion") String apiVersion,
        @Param("apiName") String apiName
    );

    @Insert("INSERT INTO config_version (" +
            "version_id, identifier, config_type, config_status, gmt_create, gmt_modified" +
            ") VALUES (" +
            "#{versionId}, " +
            "CONCAT(#{gatewayType}, ':', #{gatewayCode}, ':', #{apiVersion}, ':', #{apiName}), " +
            "'API_RECORD', #{configStatus}, NOW(), NOW()" +
            ")")
    void insertVersion(ApiRecordConfig config);

    @Select("SELECT c.*, v.config_status " +
            "FROM api_record_config c " +
            "INNER JOIN config_version v ON c.version_id = v.version_id " +
            "LEFT JOIN config_gray_release g ON v.version_id = g.version_id " +
            "WHERE v.identifier = #{identifier} " +
            "AND v.config_status IN ('PUBLISHED', 'GRAYING') " +
            "AND (g.stage = #{stage} OR g.stage = 'FULL') " +
            "ORDER BY g.stage = 'FULL' DESC, v.gmt_modified DESC " +
            "LIMIT 1")
    ApiRecordConfig findActiveConfigByIdentifierAndStage(
        @Param("identifier") String identifier,
        @Param("stage") String stage
    );
} 