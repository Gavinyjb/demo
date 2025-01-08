package com.example.util;

import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
     * 检查地域是否支持
     *
     * @param region 地域标识
     * @return true 如果地域受支持，否则返回 false
     */
    public boolean isRegionSupported(String region) {
        return SUPPORTED_REGIONS.contains(region);
    }
} 