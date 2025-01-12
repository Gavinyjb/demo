package com.example.mapper;

import com.example.model.ApiMetaConfig;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ApiMetaConfigMapper {
    @Insert("INSERT INTO api_meta_config (version_id, api_name, product, gateway_type, dm, " +
            "gateway_code, api_version, actiontrail_code, operation_type, description, " +
            "visibility, isolation_type, service_type, response_body_log, invoke_type, " +
            "resource_spec, status, effective_gray_groups, effective_flag, audit_status, " +
            "gmt_create, gmt_modified) " +
            "VALUES (#{versionId}, #{apiName}, #{product}, #{gatewayType}, #{dm}, " +
            "#{gatewayCode}, #{apiVersion}, #{actiontrailCode}, #{operationType}, #{description}, " +
            "#{visibility}, #{isolationType}, #{serviceType}, #{responseBodyLog}, #{invokeType}, " +
            "#{resourceSpec}, #{status}, #{effectiveGrayGroups}, #{effectiveFlag}, #{auditStatus}, " +
            "NOW(), NOW())")
    void insert(ApiMetaConfig config);

    @Select("SELECT * FROM api_meta_config WHERE version_id = #{versionId}")
    ApiMetaConfig findByVersionId(String versionId);

    @Select("SELECT * FROM api_meta_config WHERE status = 'PUBLISHED'")
    List<ApiMetaConfig> findAllPublished();

    @Update("UPDATE api_meta_config SET status = #{status}, " +
            "effective_gray_groups = #{effectiveGrayGroups}, gmt_modified = NOW() " +
            "WHERE version_id = #{versionId}")
    void updateStatus(@Param("versionId") String versionId,
                     @Param("status") String status,
                     @Param("effectiveGrayGroups") String effectiveGrayGroups);

    @Select("SELECT * FROM api_meta_config " +
            "WHERE gateway_type = #{gatewayType} " +
            "AND gateway_code = #{gatewayCode} " +
            "AND api_version = #{apiVersion} " +
            "AND api_name = #{apiName} " +
            "AND status = 'PUBLISHED' " +
            "AND (effective_gray_groups = 'all' " +
            "    OR effective_gray_groups LIKE CONCAT('%', #{region}, '%')) " +
            "ORDER BY effective_gray_groups != 'all' DESC, " +
            "gmt_modified DESC LIMIT 1")
    ApiMetaConfig findActiveConfigByIdentifierAndRegion(@Param("gatewayType") String gatewayType,
                                                      @Param("gatewayCode") String gatewayCode,
                                                      @Param("apiVersion") String apiVersion,
                                                      @Param("apiName") String apiName,
                                                      @Param("region") String region);

    @Select("SELECT * FROM api_meta_config " +
            "WHERE gateway_type = #{gatewayType} " +
            "AND gateway_code = #{gatewayCode} " +
            "AND api_version = #{apiVersion} " +
            "AND api_name = #{apiName} " +
            "AND status = 'PUBLISHED' " +
            "ORDER BY gmt_modified DESC")
    List<ApiMetaConfig> findPublishedConfigsByIdentifier(@Param("gatewayType") String gatewayType,
                                                       @Param("gatewayCode") String gatewayCode,
                                                       @Param("apiVersion") String apiVersion,
                                                       @Param("apiName") String apiName);

    @Select("SELECT * FROM api_meta_config WHERE status = 'PUBLISHED' " +
            "AND (effective_gray_groups = 'all' " +
            "    OR effective_gray_groups LIKE CONCAT('%', #{region}, '%'))")
    List<ApiMetaConfig> findByRegion(@Param("region") String region);

    @Select("SELECT * FROM api_meta_config " +
            "WHERE gateway_type = #{gatewayType} " +
            "AND gateway_code = #{gatewayCode} " +
            "AND api_version = #{apiVersion} " +
            "AND api_name = #{apiName} " +
            "ORDER BY gmt_modified DESC")
    List<ApiMetaConfig> findAllVersionsByIdentifier(@Param("gatewayType") String gatewayType,
                                                  @Param("gatewayCode") String gatewayCode,
                                                  @Param("apiVersion") String apiVersion,
                                                  @Param("apiName") String apiName);

    @Delete("DELETE FROM api_meta_config WHERE version_id = #{versionId}")
    void deleteByVersionId(String versionId);
} 