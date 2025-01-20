package com.example.mapper;

import com.example.model.DataSourceConfig;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface DataSourceConfigMapper {
    @Insert("INSERT INTO config_version (" +
            "version_id, identifier, config_type, status, " +
            "gmt_create, gmt_modified" +
            ") VALUES (" +
            "#{versionId}, #{source}, 'DATA_SOURCE', #{status}, " +
            "NOW(), NOW()" +
            ")")
    void insertVersion(DataSourceConfig config);

    @Insert("INSERT INTO data_source_config (" +
            "version_id, source, source_group, gateway_type, dm, " +
            "sls_endpoint, sls_project, sls_logstore, " +
            "sls_account_id, sls_assume_role_arn, sls_cursor, " +
            "consume_region, worker_config, " +
            "gmt_create, gmt_modified" +
            ") VALUES (" +
            "#{versionId}, #{source}, #{sourceGroup}, #{gatewayType}, #{dm}, " +
            "#{slsEndpoint}, #{slsProject}, #{slsLogstore}, " +
            "#{slsAccountId}, #{slsAssumeRoleArn}, #{slsCursor}, " +
            "#{consumeRegion}, #{workerConfig}, " +
            "NOW(), NOW()" +
            ")")
    void insertDataSource(DataSourceConfig config);

    @Select("SELECT d.*, v.status " +
            "FROM data_source_config d " +
            "INNER JOIN config_version v ON d.version_id = v.version_id " +
            "WHERE d.version_id = #{versionId}")
    DataSourceConfig findByVersionId(String versionId);

    @Select("SELECT d.*, v.status " +
            "FROM data_source_config d " +
            "INNER JOIN config_version v ON d.version_id = v.version_id " +
            "WHERE v.status = 'PUBLISHED'")
    List<DataSourceConfig> findAllPublished();

    @Select("SELECT d.*, v.status " +
            "FROM data_source_config d " +
            "INNER JOIN config_version v ON d.version_id = v.version_id " +
            "LEFT JOIN config_gray_release g ON v.version_id = g.version_id " +
            "WHERE d.source = #{source} " +
            "AND v.status = 'PUBLISHED' " +
            "AND (g.stage = #{stage} OR g.stage = 'FULL') " +
            "ORDER BY g.stage = 'FULL' DESC, " +
            "v.gmt_modified DESC " +
            "LIMIT 1")
    DataSourceConfig findActiveConfigBySourceAndStage(
        @Param("source") String source,
        @Param("stage") String stage
    );

    @Select("SELECT d.*, v.status " +
            "FROM data_source_config d " +
            "INNER JOIN config_version v ON d.version_id = v.version_id " +
            "WHERE d.source = #{source} " +
            "AND v.status = 'PUBLISHED' " +
            "ORDER BY v.gmt_modified DESC")
    List<DataSourceConfig> findPublishedConfigsBySource(String source);

    @Select("SELECT d.*, v.status " +
            "FROM data_source_config d " +
            "INNER JOIN config_version v ON d.version_id = v.version_id " +
            "WHERE d.source = #{source} " +
            "ORDER BY v.gmt_modified DESC")
    List<DataSourceConfig> findAllVersionsBySource(String source);

    @Update("UPDATE config_version SET status = #{status} WHERE version_id = #{versionId}")
    void updateVersionStatus(@Param("versionId") String versionId, @Param("status") String status);

    @Insert("INSERT INTO config_gray_release (version_id, stage) VALUES (#{versionId}, #{stage})")
    void insertGrayRelease(@Param("versionId") String versionId, @Param("stage") String stage);

    @Select("SELECT d.*, v.status " +
            "FROM data_source_config d " +
            "INNER JOIN config_version v ON d.version_id = v.version_id " +
            "LEFT JOIN config_gray_release g ON v.version_id = g.version_id " +
            "WHERE v.status = 'PUBLISHED' " +
            "AND (g.stage = #{stage} OR g.stage = 'FULL')")
    List<DataSourceConfig> findByStage(@Param("stage") String stage);

    @Delete("DELETE d, v, g " +
            "FROM data_source_config d " +
            "LEFT JOIN config_version v ON d.version_id = v.version_id " +
            "LEFT JOIN config_gray_release g ON v.version_id = g.version_id " +
            "WHERE d.version_id = #{versionId}")
    void deleteByVersionId(String versionId);

    @Delete("DELETE d, v, g " +
            "FROM data_source_config d " +
            "LEFT JOIN config_version v ON d.version_id = v.version_id " +
            "LEFT JOIN config_gray_release g ON v.version_id = g.version_id " +
            "WHERE d.source = #{source} " +
            "AND v.status = #{status}")
    void deleteBySourceAndStatus(
        @Param("source") String source,
        @Param("status") String status
    );
} 