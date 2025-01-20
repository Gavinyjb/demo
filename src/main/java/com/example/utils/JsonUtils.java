package com.example.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtils {
    
    /**
     * 将JSON字符串解析为指定类型的对象
     */
    public static <T> T parseObject(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return JSON.parseObject(json, clazz);
        } catch (Exception e) {
            log.error("Parse json failed: {}", json, e);
            return null;
        }
    }
    
    /**
     * 将对象转换为JSON字符串
     */
    public static String toJsonString(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return JSON.toJSONString(object);
        } catch (Exception e) {
            log.error("Convert to json failed: {}", object, e);
            return null;
        }
    }
    
    /**
     * 将JSON字符串解析为JSONObject
     */
    public static JSONObject parseObject(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return JSON.parseObject(json);
        } catch (Exception e) {
            log.error("Parse json to JSONObject failed: {}", json, e);
            return null;
        }
    }
    
    /**
     * 判断字符串是否为有效的JSON
     */
    public static boolean isValidJson(String json) {
        try {
            JSON.parse(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 格式化JSON字符串
     */
    public static String formatJson(String json) {
        try {
            Object obj = JSON.parse(json);
            return JSON.toJSONString(obj, true);
        } catch (Exception e) {
            log.error("Format json failed: {}", json, e);
            return json;
        }
    }
} 