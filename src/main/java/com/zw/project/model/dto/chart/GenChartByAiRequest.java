package com.zw.project.model.dto.chart;

import java.io.Serializable;
import lombok.Data;

/**
 *
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class GenChartByAiRequest implements Serializable {

    /**
     * 图表名称
     */
    private String chartName;

    /**
     * 图表目标
     */
    private String goal;


    /**
     * 图表类型
     */
    private String chartType;

    private static final long serialVersionUID = 1L;
}