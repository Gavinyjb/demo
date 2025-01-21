package com.example.mapper;

import com.example.model.ConfigVersion;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConfigVersionMapper {

    @Select("SELECT id, version_id, identifier, config_type, config_status, " +
            "gmt_create, gmt_modified " +
            "FROM config_version " +
            "WHERE version_id = #{versionId}")
    ConfigVersion findByVersionId(@Param("versionId") String versionId);
    
    @Update("UPDATE config_version SET config_status = #{status} WHERE version_id = #{versionId}")
    void updateStatus(
        @Param("versionId") String versionId, 
        @Param("status") String status
    );

    @Select("SELECT id, version_id, identifier, config_type, config_status, " +
            "gmt_create, gmt_modified " +
            "FROM config_version " +
            "WHERE identifier = #{identifier} " +
            "AND config_type = #{configType} " +
            "AND config_status = 'PUBLISHED' " +
            "AND version_id != #{currentVersionId}")
    List<ConfigVersion> findPublishedVersionsByIdentifier(
        @Param("identifier") String identifier,
        @Param("configType") String configType,
        @Param("currentVersionId") String currentVersionId
    );

    @Select("SELECT * FROM config_version " +
            "WHERE identifier = #{identifier} " +
            "AND config_type = #{configType} " +
            "AND config_status = 'DEPRECATED' " +
            "ORDER BY gmt_create")
    List<ConfigVersion> findDeprecatedVersionsByIdentifier(
        @Param("identifier") String identifier,
        @Param("configType") String configType
    );

    @Delete("DELETE FROM config_version WHERE version_id = #{versionId}")
    void deleteByVersionId(@Param("versionId") String versionId);

    @Select("SELECT * FROM config_version " +
            "WHERE identifier = #{identifier} " +
            "AND config_type = #{configType} " +
            "AND config_status = 'PUBLISHED' " +
            "ORDER BY gmt_modified DESC LIMIT 1")
    ConfigVersion findActiveVersionByIdentifier(
        @Param("identifier") String identifier,
        @Param("configType") String configType
    );

    @Select("SELECT * FROM config_version " +
            "WHERE identifier = #{identifier} " +
            "AND config_type = #{configType} " +
            "AND config_status = #{status} " +
            "ORDER BY gmt_modified DESC LIMIT 1")
    ConfigVersion findVersionByIdentifierAndStatus(
        @Param("identifier") String identifier,
        @Param("configType") String configType,
        @Param("status") String status
    );

    @Select("SELECT version_id, identifier, config_type, config_status, " +
            "gmt_create, gmt_modified " +
            "FROM config_version " +
            "WHERE identifier = #{identifier} " +
            "AND config_type = #{configType} " +
            "AND config_status = #{configStatus}")
    List<ConfigVersion> findByIdentifierAndTypeAndStatus(
        @Param("identifier") String identifier,
        @Param("configType") String configType,
        @Param("configStatus") String configStatus
    );

    @Select("SELECT v.id, v.version_id, v.identifier, v.config_type, v.config_status, " +
            "v.gmt_create, v.gmt_modified " +
            "FROM config_version v " +
            "LEFT JOIN config_gray_release g ON v.version_id = g.version_id " +
            "WHERE v.identifier = #{identifier} " +
            "AND v.config_type = #{configType} " +
            "AND v.config_status = 'PUBLISHED' " +
            "AND v.version_id != #{currentVersionId} " +
            "AND g.stage = 'FULL'")
    List<ConfigVersion> findPublishedFullVersionsByIdentifier(
        @Param("identifier") String identifier,
        @Param("configType") String configType,
        @Param("currentVersionId") String currentVersionId
    );

    @Insert("INSERT INTO config_version (version_id, identifier, config_type, config_status, " +
            "gmt_create, gmt_modified) VALUES (" +
            "#{versionId}, #{identifier}, #{configType}, #{configStatus}, " +
            "NOW(), NOW())")
    void insert(ConfigVersion configVersion);
} 