package com.example.mapper;

import com.example.model.DataSourceConfig;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface DataSourceConfigMapper {
    @Insert("INSERT INTO data_source_config (version_id, source, source_group, gateway_type, " +
            "dm, loghub_endpoint, loghub_project, loghub_stream, loghub_accesskey_id, " +
            "loghub_accesskey_secret, loghub_assume_role_arn, loghub_cursor, consume_region, " +
            "data_fetch_interval_millis, status, effective_gray_groups, gmt_create, gmt_modified) " +
            "VALUES (#{versionId}, #{source}, #{sourceGroup}, #{gatewayType}, #{dm}, " +
            "#{loghubEndpoint}, #{loghubProject}, #{loghubStream}, #{loghubAccesskeyId}, " +
            "#{loghubAccesskeySecret}, #{loghubAssumeRoleArn}, #{loghubCursor}, #{consumeRegion}, " +
            "#{dataFetchIntervalMillis}, #{status}, #{effectiveGrayGroups}, NOW(), NOW())")
    void insert(DataSourceConfig config);

    @Select("SELECT * FROM data_source_config WHERE version_id = #{versionId}")
    DataSourceConfig findByVersionId(String versionId);

    @Select("SELECT * FROM data_source_config WHERE status = 'PUBLISHED'")
    List<DataSourceConfig> findAllPublished();

    @Update("UPDATE data_source_config SET status = #{status}, " +
            "effective_gray_groups = #{effectiveGrayGroups}, gmt_modified = NOW() " +
            "WHERE version_id = #{versionId}")
    void updateStatus(@Param("versionId") String versionId, 
                     @Param("status") String status,
                     @Param("effectiveGrayGroups") String effectiveGrayGroups);

    @Select("SELECT * FROM data_source_config WHERE status = 'PUBLISHED' " +
            "AND effective_gray_groups LIKE CONCAT('%', #{region}, '%')")
    List<DataSourceConfig> findByRegion(@Param("region") String region);

    /**
     * 根据source和地域查询生效的配置
     * 优先返回在指定地域灰度生效的配置，如果没有则返回全量发布的配置
     */
    @Select("SELECT * FROM data_source_config " +
            "WHERE source = #{source} " +
            "AND status = 'PUBLISHED' " +
            "AND (effective_gray_groups LIKE CONCAT('%', #{region}, '%') " +
            "    OR (effective_gray_groups NOT LIKE '%,%' AND effective_gray_groups IS NOT NULL)) " +
            "ORDER BY effective_gray_groups LIKE CONCAT('%', #{region}, '%') DESC, " +
            "gmt_modified DESC LIMIT 1")
    DataSourceConfig findActiveConfigBySourceAndRegion(@Param("source") String source, 
                                                     @Param("region") String region);

    /**
     * 查询某个source的所有已发布配置
     */
    @Select("SELECT * FROM data_source_config " +
            "WHERE source = #{source} " +
            "AND status = 'PUBLISHED' " +
            "ORDER BY gmt_modified DESC")
    List<DataSourceConfig> findPublishedConfigsBySource(String source);
} 