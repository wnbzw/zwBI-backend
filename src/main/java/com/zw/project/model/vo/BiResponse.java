package com.zw.project.model.vo;

import lombok.Data;

/**
 * Bi 的返回结果
 */
@Data
public class BiResponse {

    /**
     * 生成的图表数据
     */
    private String generateChart;

    /**
     * 生成的分析结果
     */
    private String generateResult;
    // 新生成的图标id
    private Long chartId;
}
