package com.example.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 灰度发布阶段枚举
 */
public enum GrayStage {
    /**
     * 阶段1：仅在 ap-southeast-2 生效
     */
    STAGE_1(Collections.singletonList("ap-southeast-2")),

    /**
     * 阶段2：在 cn-chengdu、ap-southeast-2、cn-shanghai 生效
     */
    STAGE_2(Arrays.asList("cn-chengdu", "ap-southeast-2", "cn-shanghai")),

    /**
     * 全量发布：所有地域生效
     */
    FULL(Collections.singletonList("all"));

    private final List<String> regions;

    GrayStage(List<String> regions) {
        this.regions = regions;
    }

    public List<String> getRegions() {
        return regions;
    }
} 