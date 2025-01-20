package com.example.mapper;

import com.example.model.DataSourceConfig;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface DataSourceConfigMapper {
    @Insert("INSERT INTO config_version (" +
            "version_id, identifier, config_type, config_status, " +
            "gmt_create, gmt_modified" +
            ") VALUES (" +
            "#{versionId}, #{name}, 'DATA_SOURCE', #{configStatus}, " +
            "NOW(), NOW()" +
            ")")
    void insertVersion(DataSourceConfig config);

    @Insert("INSERT INTO conf_data_source_config (" +
            "version_id, name, source_group, gateway_type, dm, " +
            "sls_region_id, sls_endpoint, sls_project, sls_log_store, " +
            "sls_account_id, sls_role_arn, sls_cursor, " +
            "consume_region, consumer_group_name, status, worker_config, comment, " +
            "gmt_create, gmt_modified" +
            ") VALUES (" +
            "#{versionId}, #{name}, #{sourceGroup}, #{gatewayType}, #{dm}, " +
            "#{slsRegionId}, #{slsEndpoint}, #{slsProject}, #{slsLogStore}, " +
            "#{slsAccountId}, #{slsRoleArn}, #{slsCursor}, " +
            "#{consumeRegion}, #{consumerGroupName}, #{status}, #{workerConfig}, #{comment}, " +
            "NOW(), NOW()" +
            ")")
    void insertDataSource(DataSourceConfig config);

    @Select("SELECT d.*, v.config_status " +
            "FROM conf_data_source_config d " +
            "INNER JOIN config_version v ON d.version_id = v.version_id " +
            "WHERE d.version_id = #{versionId}")
    DataSourceConfig findByVersionId(String versionId);

    @Select("SELECT d.*, v.config_status " +
            "FROM conf_data_source_config d " +
            "INNER JOIN config_version v ON d.version_id = v.version_id " +
            "WHERE v.config_status = 'PUBLISHED'")
    List<DataSourceConfig> findAllPublished();

    @Select("SELECT d.*, v.config_status " +
            "FROM conf_data_source_config d " +
            "INNER JOIN config_version v ON d.version_id = v.version_id " +
            "WHERE v.config_status = 'PUBLISHED'")
    List<DataSourceConfig> findAllPublishedConfigs();

    @Select("SELECT d.*, v.config_status " +
            "FROM conf_data_source_config d " +
            "INNER JOIN config_version v ON d.version_id = v.version_id " +
            "WHERE d.name = #{name} " +
            "AND v.config_status = 'PUBLISHED' " +
            "ORDER BY v.gmt_modified DESC")
    List<DataSourceConfig> findPublishedConfigsByName(String name);

    @Select("SELECT d.*, v.config_status " +
            "FROM conf_data_source_config d " +
            "INNER JOIN config_version v ON d.version_id = v.version_id " +
            "WHERE d.name = #{name} " +
            "ORDER BY v.gmt_modified DESC")
    List<DataSourceConfig> findAllVersionsByName(String name);

    @Update("UPDATE config_version SET config_status = #{configStatus} WHERE version_id = #{versionId}")
    void updateVersionStatus(@Param("versionId") String versionId, @Param("configStatus") String configStatus);

    @Insert("INSERT INTO config_gray_release (version_id, stage) VALUES (#{versionId}, #{stage})")
    void insertGrayRelease(@Param("versionId") String versionId, @Param("stage") String stage);

    @Select("SELECT d.*, v.config_status " +
            "FROM conf_data_source_config d " +
            "INNER JOIN config_version v ON d.version_id = v.version_id " +
            "LEFT JOIN config_gray_release g ON v.version_id = g.version_id " +
            "WHERE v.config_status IN ('PUBLISHED', 'GRAYING') " +
            "AND (g.stage = #{stage} OR g.stage = 'FULL')")
    List<DataSourceConfig> findByStage(@Param("stage") String stage);

    @Delete("DELETE FROM conf_data_source_config WHERE version_id = #{versionId}")
    void deleteByVersionId(@Param("versionId") String versionId);

    @Delete("DELETE d, v, g " +
            "FROM conf_data_source_config d " +
            "LEFT JOIN config_version v ON d.version_id = v.version_id " +
            "LEFT JOIN config_gray_release g ON v.version_id = g.version_id " +
            "WHERE d.name = #{name} " +
            "AND v.config_status = #{configStatus}")
    void deleteByNameAndStatus(
        @Param("name") String name,
        @Param("configStatus") String configStatus
    );

    @Select("SELECT d.*, v.config_status " +
            "FROM conf_data_source_config d " +
            "INNER JOIN config_version v ON d.version_id = v.version_id " +
            "LEFT JOIN config_gray_release g ON v.version_id = g.version_id " +
            "WHERE d.name = #{name} " +
            "AND v.config_status IN ('PUBLISHED', 'GRAYING') " +
            "AND (g.stage = #{stage} OR g.stage = 'FULL') " +
            "ORDER BY g.stage = #{stage} DESC, v.gmt_modified DESC " +
            "LIMIT 1")
    DataSourceConfig findActiveConfigByNameAndStage(
        @Param("name") String name,
        @Param("stage") String stage
    );
} 