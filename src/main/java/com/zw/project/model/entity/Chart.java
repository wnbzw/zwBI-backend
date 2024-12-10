package com.zw.project.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 图表信息表
 * @TableName chart
 */
@TableName(value ="chart")
@Data
public class Chart implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

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
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 状态
     */
    private String status;

    /**
     * 执行信息
     */
    private String execMessage;

    /**
     * 生成的图表数据
     */
    private String generateChart;

    /**
     * 生成的分析结果
     */
    private String generateResult;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Chart other = (Chart) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getGoal() == null ? other.getGoal() == null : this.getGoal().equals(other.getGoal()))
            && (this.getChartData() == null ? other.getChartData() == null : this.getChartData().equals(other.getChartData()))
            && (this.getChartType() == null ? other.getChartType() == null : this.getChartType().equals(other.getChartType()))
            && (this.getChartName() == null ? other.getChartName() == null : this.getChartName().equals(other.getChartName()))
            && (this.getGenerateChart() == null ? other.getGenerateChart() == null : this.getGenerateChart().equals(other.getGenerateChart()))
            && (this.getGenerateResult() == null ? other.getGenerateResult() == null : this.getGenerateResult().equals(other.getGenerateResult()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getGoal() == null) ? 0 : getGoal().hashCode());
        result = prime * result + ((getChartData() == null) ? 0 : getChartData().hashCode());
        result = prime * result + ((getChartType() == null) ? 0 : getChartType().hashCode());
        result = prime * result + ((getChartName() == null) ? 0 : getChartName().hashCode());
        result = prime * result + ((getGenerateChart() == null) ? 0 : getGenerateChart().hashCode());
        result = prime * result + ((getGenerateResult() == null) ? 0 : getGenerateResult().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getIsDelete() == null) ? 0 : getIsDelete().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", goal=").append(goal);
        sb.append(", chartData=").append(chartData);
        sb.append(", chartType=").append(chartType);
        sb.append(", chartName=").append(chartName);
        sb.append(", generateChart=").append(generateChart);
        sb.append(", generateResult=").append(generateResult);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", isDelete=").append(isDelete);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}