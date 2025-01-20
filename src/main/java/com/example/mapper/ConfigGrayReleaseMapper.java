package com.example.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface ConfigGrayReleaseMapper {
    
    @Insert("INSERT INTO config_gray_release (version_id, stage) VALUES (#{versionId}, #{stage})")
    void insert(
        @Param("versionId") String versionId, 
        @Param("stage") String stage
    );
    
    @Select("SELECT stage FROM config_gray_release WHERE version_id = #{versionId}")
    String findStageByVersionId(@Param("versionId") String versionId);
    
    @Delete("DELETE FROM config_gray_release WHERE version_id = #{versionId}")
    void deleteByVersionId(@Param("versionId") String versionId);
} 