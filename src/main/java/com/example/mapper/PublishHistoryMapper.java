package com.example.mapper;

import com.example.model.PublishHistory;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PublishHistoryMapper {
    @Insert("INSERT INTO publish_history (" +
            "version_id, config_type, status, stage, operator, " +
            "gmt_create, gmt_modified" +
            ") VALUES (" +
            "#{versionId}, #{configType}, #{status}, #{stage}, #{operator}, " +
            "NOW(), NOW()" +
            ")")
    void insert(PublishHistory history);

    @Select("SELECT * FROM publish_history " +
            "WHERE version_id = #{versionId} " +
            "ORDER BY gmt_create DESC")
    List<PublishHistory> findByVersionId(String versionId);

    @Select("SELECT * FROM publish_history " +
            "WHERE config_type = #{configType} " +
            "AND stage = #{stage} " +
            "ORDER BY gmt_create DESC")
    List<PublishHistory> findByConfigTypeAndStage(
        @Param("configType") String configType,
        @Param("stage") String stage
    );

    @Insert("INSERT INTO publish_history (" +
            "version_id, config_type, status, stage, operator, " +
            "gmt_create, gmt_modified" +
            ") VALUES (" +
            "#{versionId}, #{configType}, #{status}, #{stage}, #{operator}, " +
            "NOW(), NOW()" +
            ")")
    void insertMigrationHistory(
        @Param("versionId") String versionId,
        @Param("configType") String configType,
        @Param("status") String status,
        @Param("stage") String stage,
        @Param("operator") String operator
    );

    @Select("SELECT * FROM publish_history " +
            "WHERE config_type = #{configType} " +
            "AND status = #{status} " +
            "ORDER BY gmt_create DESC")
    List<PublishHistory> findByConfigTypeAndStatus(
        @Param("configType") String configType,
        @Param("status") String status
    );

    @Select("SELECT * FROM publish_history " +
            "WHERE version_id IN (" +
            "SELECT version_id FROM config_version " +
            "WHERE identifier = #{identifier} " +
            "AND config_type = #{configType}" +
            ") ORDER BY gmt_create DESC")
    List<PublishHistory> findByIdentifier(
        @Param("identifier") String identifier,
        @Param("configType") String configType
    );
} 