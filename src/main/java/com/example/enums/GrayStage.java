package com.example.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 灰度发布阶段枚举
 */
@Getter
public enum GrayStage {
    /**
     * 阶段1：小规模测试区域
     * - 马来西亚(ap-southeast-3)
     * - 菲律宾(ap-southeast-6)
     * - 泰国(ap-southeast-7)
     */
    STAGE_1(Arrays.asList(
        "ap-southeast-3",  // 马来西亚
        "ap-southeast-6",  // 菲律宾
        "ap-southeast-7"   // 泰国
    )),

    /**
     * 阶段2：包含阶段1,并扩展到次要区域
     * - 阶段1的所有地域
     * - 成都(cn-chengdu)
     * - 青岛(cn-qingdao)
     * - 张家口(cn-zhangjiakou)
     * - 呼和浩特(cn-headnote)
     */
    STAGE_2(Arrays.asList(
        // 阶段1地域
        "ap-southeast-3",  // 马来西亚
        "ap-southeast-6",  // 菲律宾
        "ap-southeast-7",  // 泰国
        // 阶段2新增地域
        "cn-chengdu",     // 成都
        "cn-qingdao",     // 青岛
        "cn-zhangjiakou", // 张家口
        "cn-huhehaote"    // 呼和浩特
    )),

    /**
     * 阶段3：包含阶段1和2,并扩展到重要海外区域
     * - 阶段1和2的所有地域
     * - 日本(ap-northeast-1)
     * - 韩国(ap-northeast-2)
     * - 德国(eu-central-1)
     * - 英国(eu-west-1)
     * - 美国(us-east-1, us-west-1)
     */
    STAGE_3(Arrays.asList(
        // 阶段1地域
        "ap-southeast-3",  // 马来西亚
        "ap-southeast-6",  // 菲律宾
        "ap-southeast-7",  // 泰国
        // 阶段2地域
        "cn-chengdu",     // 成都
        "cn-qingdao",     // 青岛
        "cn-zhangjiakou", // 张家口
        "cn-headnote",   // 呼和浩特
        // 阶段3新增地域
        "ap-northeast-1",  // 日本
        "ap-northeast-2",  // 韩国
        "eu-central-1",    // 德国
        "eu-west-1",       // 英国
        "us-east-1",       // 美国(弗吉尼亚)
        "us-west-1"        // 美国(硅谷)
    )),

    /**
     * 全量发布：所有地域生效
     */
    FULL(Arrays.asList(
        // 核心地域
        "cn-hangzhou", "cn-shanghai", "cn-beijing", "cn-shenzhen", "ap-southeast-1",
        // 中国大陆其他地域
        "cn-chengdu", "cn-guangzhou", "cn-qingdao", "cn-zhangjiakou",
        "cn-huhehaote", "cn-wulanchabu", "cn-heyuan", "cn-nanjing",
        "cn-fuzhou", "cn-wuhan-lr",
        // 中国香港
        "cn-hongkong",
        // 亚太其他地域
        "ap-southeast-3", "ap-southeast-5", "ap-southeast-6", 
        "ap-southeast-7", "ap-northeast-1", "ap-northeast-2",
        // 欧洲
        "eu-central-1", "eu-west-1",
        // 美国
        "us-east-1", "us-west-1",
        // 中东
        "me-east-1"
    ));

    private final List<String> regions;

    GrayStage(List<String> regions) {
        this.regions = regions;
    }

    public static boolean contains(String stage) {
        try {
            valueOf(stage);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
} 