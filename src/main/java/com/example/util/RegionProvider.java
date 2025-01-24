package com.example.util;

import com.example.enums.GrayStage;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 地域信息提供器
 */
@Component
public class RegionProvider {
    
    private final Random random = new Random();

    /**
     * 获取当前应用的地域
     * 随机返回一个支持的地域
     *
     * @return 地域标识
     */
    public String getCurrentRegion() {
        List<String> allRegions = GrayStage.FULL.getRegions();
        return allRegions.get(random.nextInt(allRegions.size()));
    }

    /**
     * 获取所有支持的地域列表
     *
     * @return 地域列表
     */
    public List<String> getSupportedRegions() {
        return GrayStage.FULL.getRegions();
    }

    /**
     * 获取地域对应的灰度阶段
     */
    public String getStageByRegion(String regionId) {

        // 按照灰度阶段从低到高匹配
        if (GrayStage.STAGE_1.getRegions().contains(regionId)) {
            return GrayStage.STAGE_1.name();
        }

        if (GrayStage.STAGE_2.getRegions().contains(regionId)) {
            return GrayStage.STAGE_2.name();
        }

        if (GrayStage.STAGE_3.getRegions().contains(regionId)) {
            return GrayStage.STAGE_3.name();
        }

        // 默认返回 FULL
        return GrayStage.FULL.name();
    }

} 