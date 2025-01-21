package com.example.mapper;

import org.apache.ibatis.annotations.*;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Mapper
public interface ConfigMigrationMapper {
    
    @Select("SELECT * FROM ${tableName} WHERE version_id IS NULL")
    List<Map<String, Object>> findUnmigratedRecords(@Param("tableName") String tableName);
    
    @Update("UPDATE ${tableName} SET version_id = #{versionId} WHERE id = #{id}")
    void updateVersionId(
        @Param("tableName") String tableName,
        @Param("versionId") String versionId,
        @Param("id") BigInteger id
    );
    
    @Select("SELECT COUNT(*) FROM ${tableName} WHERE version_id IS NULL")
    int countUnmigratedRecords(@Param("tableName") String tableName);
    
    @Select("SELECT COUNT(*) FROM ${tableName} t " +
            "LEFT JOIN config_version v ON t.version_id = v.version_id " +
            "WHERE v.version_id IS NULL AND t.version_id IS NOT NULL")
    int countMissingVersionRecords(@Param("tableName") String tableName);
    
    @Select("SELECT COUNT(*) FROM ${tableName} t " +
            "LEFT JOIN publish_history h ON t.version_id = h.version_id " +
            "WHERE h.version_id IS NULL AND t.version_id IS NOT NULL")
    int countMissingHistoryRecords(@Param("tableName") String tableName);
} 