package com.example.mapper;

import com.example.model.ConfigVersion;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConfigVersionMapper {
    // ... 其他方法保持不变

    @Select("SELECT * FROM config_version WHERE version_id = #{versionId}")
    ConfigVersion findByVersionId(@Param("versionId") String versionId);
    
    @Update("UPDATE config_version SET config_status = #{status} WHERE version_id = #{versionId}")
    void updateStatus(
        @Param("versionId") String versionId, 
        @Param("status") String status
    );

    @Select("SELECT * FROM config_version " +
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
} 