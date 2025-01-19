package com.example.util;

import com.example.enums.GrayStage;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 地域信息提供器
 */
@Component
public class RegionProvider {
    
    /**
     * 支持的地域列表
     */
    private static final List<String> SUPPORTED_REGIONS = Arrays.asList(
        "cn-hangzhou",
        "cn-shanghai",
        "ap-southeast-1",
        "cn-chengdu",
        "ap-southeast-2"
    );
    
    private final Random random = new Random();

    /**
     * 地域与灰度阶段的映射关系
     */
    private static final Map<String, String> REGION_STAGE_MAP = new HashMap<>();
    
    static {
        // STAGE_1: ap-southeast-2
        REGION_STAGE_MAP.put("ap-southeast-2", "STAGE_1");
        
        // STAGE_2: cn-chengdu, ap-southeast-2, cn-shanghai
        REGION_STAGE_MAP.put("cn-chengdu", "STAGE_2");
        REGION_STAGE_MAP.put("cn-shanghai", "STAGE_2");
        
        // 其他地域为全量发布
        REGION_STAGE_MAP.put("cn-hangzhou", "FULL");
    }

    /**
     * 获取当前应用的地域
     * 随机返回一个支持的地域
     *
     * @return 地域标识
     */
    public String getCurrentRegion() {
        return SUPPORTED_REGIONS.get(random.nextInt(SUPPORTED_REGIONS.size()));
    }

    /**
     * 获取所有支持的地域列表
     *
     * @return 地域列表
     */
    public List<String> getSupportedRegions() {
        return SUPPORTED_REGIONS;
    }

    /**
     * 获取地域对应的灰度阶段
     */
    public String getStageByRegion(String region) {
        return REGION_STAGE_MAP.getOrDefault(region, "FULL");
    }

    /**
     * 获取灰度阶段包含的地域列表
     */
    public List<String> getRegionsByStage(String stage) {
        return REGION_STAGE_MAP.entrySet().stream()
            .filter(e -> e.getValue().equals(stage))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * 检查地域是否支持
     */
    public boolean isRegionSupported(String region) {
        return SUPPORTED_REGIONS.contains(region);
    }

    public Map<String, List<String>> getStageRegionMapping() {
        Map<String, List<String>> mapping = new HashMap<>();
        for (GrayStage stage : GrayStage.values()) {
            mapping.put(stage.name(), stage.getRegions());
        }
        return mapping;
    }
} 