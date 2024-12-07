package com.zw.project.model.dto.chart;

import com.zw.project.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 图表查询请求
 *
 * @author yupi
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChartQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 分析目标
     */
    private String goal;


    /**
     * 图表类型
     */
    private String chartType;

    /**
     * 图标名称
     */
    private String chartName;

    /**
     * 创建用户 id
     */
    private Long userId;


    private static final long serialVersionUID = 1L;
}