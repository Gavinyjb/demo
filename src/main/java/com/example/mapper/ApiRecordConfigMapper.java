package com.example.mapper;

import com.example.model.ApiRecordConfig;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ApiRecordConfigMapper {
    @Insert("INSERT INTO api_record_config (version_id, gateway_type, gateway_code, api_version, " +
            "api_name, loghub_stream, basic_config, event_config, user_identity_config, " +
            "request_config, response_config, filter_config, reference_resource_config, type, " +
            "status, effective_gray_groups, gmt_create, gmt_modified) " +
            "VALUES (#{versionId}, #{gatewayType}, #{gatewayCode}, #{apiVersion}, #{apiName}, " +
            "#{loghubStream}, #{basicConfig}, #{eventConfig}, #{userIdentityConfig}, " +
            "#{requestConfig}, #{responseConfig}, #{filterConfig}, #{referenceResourceConfig}, " +
            "#{type}, #{status}, #{effectiveGrayGroups}, NOW(), NOW())")
    void insert(ApiRecordConfig config);

    @Select("SELECT * FROM api_record_config WHERE version_id = #{versionId}")
    ApiRecordConfig findByVersionId(String versionId);

    @Select("SELECT * FROM api_record_config WHERE status = 'PUBLISHED'")
    List<ApiRecordConfig> findAllPublished();

    @Update("UPDATE api_record_config SET status = #{status}, " +
            "effective_gray_groups = #{effectiveGrayGroups}, gmt_modified = NOW() " +
            "WHERE version_id = #{versionId}")
    void updateStatus(@Param("versionId") String versionId,
                     @Param("status") String status,
                     @Param("effectiveGrayGroups") String effectiveGrayGroups);
} 