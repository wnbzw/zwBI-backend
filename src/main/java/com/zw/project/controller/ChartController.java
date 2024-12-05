package com.zw.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.zw.project.common.BaseResponse;
import com.zw.project.common.DeleteRequest;
import com.zw.project.common.ErrorCode;
import com.zw.project.common.ResultUtils;
import com.zw.project.exception.BusinessException;
import com.zw.project.model.dto.chart.*;
import com.zw.project.model.entity.Chart;
import com.zw.project.model.vo.ChartVO;
import com.zw.project.service.ChartService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图表接口
 *
 * @author yupi
 */
@RestController
@RequestMapping("/chart")
public class ChartController {

    @Resource
    private ChartService chartService;
    
    // region 增删改查

    /**
     * 创建用户
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        boolean result = chartService.save(chart);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(chart.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = chartService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     *
     * @param chartUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest, HttpServletRequest request) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取用户
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<ChartVO> getChartById(int id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        ChartVO chartVO = new ChartVO();
        BeanUtils.copyProperties(chart, chartVO);
        return ResultUtils.success(chartVO);
    }

    /**
     * 获取用户列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<ChartVO>> listChart(ChartQueryRequest chartQueryRequest, HttpServletRequest request) {
        Chart chartQuery = new Chart();
        if (chartQueryRequest != null) {
            BeanUtils.copyProperties(chartQueryRequest, chartQuery);
        }
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>(chartQuery);
        List<Chart> chartList = chartService.list(queryWrapper);
        List<ChartVO> chartVOList = chartList.stream().map(chart -> {
            ChartVO chartVO = new ChartVO();
            BeanUtils.copyProperties(chart, chartVO);
            return chartVO;
        }).collect(Collectors.toList());
        return ResultUtils.success(chartVOList);
    }

    /**
     * 分页获取用户列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<ChartVO>> listChartByPage(ChartQueryRequest chartQueryRequest, HttpServletRequest request) {
        long current = 1;
        long size = 10;
        Chart chartQuery = new Chart();
        if (chartQueryRequest != null) {
            BeanUtils.copyProperties(chartQueryRequest, chartQuery);
            current = chartQueryRequest.getCurrent();
            size = chartQueryRequest.getPageSize();
        }
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>(chartQuery);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size), queryWrapper);
        Page<ChartVO> chartVOPage = new PageDTO<>(chartPage.getCurrent(), chartPage.getSize(), chartPage.getTotal());
        List<ChartVO> chartVOList = chartPage.getRecords().stream().map(chart -> {
            ChartVO chartVO = new ChartVO();
            BeanUtils.copyProperties(chart, chartVO);
            return chartVO;
        }).collect(Collectors.toList());
        chartVOPage.setRecords(chartVOList);
        return ResultUtils.success(chartVOPage);
    }

    // endregion
}
