package com.zw.project.model.dto.chart;

import lombok.Data;

import java.io.Serializable;


/**
 * 图表创建请求
 *
 * @author wzw
 */
@Data
public class ChartAddRequest implements Serializable {

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表数据
     */
    private String chartData;

    /**
     * 图表类型
     */
    private String chartType;

    /**
     * 图标名称
     */
    private String chartName;



    private static final long serialVersionUID = 1L;
}