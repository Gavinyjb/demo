package com.example.mapper;

import com.example.model.PublishHistory;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PublishHistoryMapper {
    @Insert("INSERT INTO publish_history (version_id, config_type, status, gray_groups, " +
            "operator, gmt_create, gmt_modified) " +
            "VALUES (#{versionId}, #{configType}, #{status}, #{grayGroups}, " +
            "#{operator}, NOW(), NOW())")
    void insert(PublishHistory history);

    @Select("SELECT * FROM publish_history WHERE version_id = #{versionId} " +
            "ORDER BY gmt_create DESC")
    List<PublishHistory> findByVersionId(String versionId);
} 