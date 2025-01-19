package com.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConfigDiffService {
    @Value("${diff.ignore-fields}")
    private String ignoreFields;

    private static final String SELECT_CONFIG_SQL = 
        "SELECT * FROM %s WHERE version_id = ?";

    private static final String COMPARE_CONFIG_SQL = 
        "SELECT * FROM %s WHERE version_id IN (?, ?)";

    // ... 其他代码保持不变 ...
} 