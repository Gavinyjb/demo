package com.example.util;

import com.example.model.ConfigIdentifier;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 配置标识工具类
 */
public class ConfigIdentifierUtils {
    
    /**
     * 从配置列表中找出与目标配置具有相同标识的配置
     */
    public static <T extends ConfigIdentifier> List<T> findSameIdentifier(List<T> configs, T target) {
        if (configs == null || target == null) {
            return Collections.emptyList();
        }
        return configs.stream()
            .filter(config -> config.getIdentifier().equals(target.getIdentifier()))
            .collect(Collectors.toList());
    }

    /**
     * 检查配置列表中是否存在与目标配置具有相同标识的配置
     */
    public static <T extends ConfigIdentifier> boolean hasSameIdentifier(List<T> configs, T target) {
        if (configs == null || target == null) {
            return false;
        }
        return configs.stream()
            .anyMatch(config -> config.getIdentifier().equals(target.getIdentifier()));
    }
} 