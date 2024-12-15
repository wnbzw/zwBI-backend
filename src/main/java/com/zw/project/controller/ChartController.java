package com.zw.project.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zw.project.common.BaseResponse;
import com.zw.project.common.DeleteRequest;
import com.zw.project.common.ErrorCode;
import com.zw.project.common.ResultUtils;
import com.zw.project.exception.BusinessException;
import com.zw.project.exception.ThrowUtils;
import com.zw.project.manager.AiManager;
import com.zw.project.manager.RedisLimiterManager;
import com.zw.project.model.dto.chart.*;
import com.zw.project.model.entity.Chart;
import com.zw.project.model.vo.BiResponse;
import com.zw.project.model.vo.UserVO;
import com.zw.project.mq.BiMqConstant;
import com.zw.project.mq.MyMessageProducer;
import com.zw.project.service.ChartService;
import com.zw.project.service.UserService;
import com.zw.project.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 图表接口
 *
 * @author yupi
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private MyMessageProducer myMessageProducer;

    @Resource
    private RedisTemplate redisTemplate;

    // region

    /**
     * 智能分析(同步)
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String chartName = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        // 如果分析目标为空，就抛出请求参数错误异常，并给出提示
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        // 如果名称不为空，并且名称长度大于100，就抛出异常，并给出提示
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");

        UserVO loginUser = userService.getLoginUser(request);
        long biModelId = 1659171950288818178L;

        // 判断文件大小
        long size=multipartFile.getSize();
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件大小超过1MB");

        //获取原始文件名
        String originalFilename = multipartFile.getOriginalFilename();
        //获取文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);

        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "不支持的文件类型");
        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        // 拼接分析目标
        String userGoal = goal;
        // 如果图表类型不为空
        if (StringUtils.isNotBlank(chartType)) {
            // 就将分析目标拼接上“请使用”+图表类型
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        // 压缩后的数据（把multipartFile传进来）
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        // 拿到返回结果
        String result = aiManager.sendMsgToXingHuo(true,userInput.toString());
        // 对返回结果做拆分,按照5个中括号进行拆分
        String[] splits = result.split("'【【【【【'");
        // 拆分之后还要进行校验
        if (splits.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI 生成错误");
        }

        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        // 插入到数据库
        Chart chart = new Chart();
        chart.setChartName(chartName);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setGenerateChart(genChart);
        chart.setGenerateResult(genResult);
        chart.setUserId(loginUser.getId());
        chart.setStatus("succeed");
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        BiResponse biResponse = new BiResponse();
        biResponse.setGenerateChart(genChart);
        biResponse.setGenerateResult(genResult);
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);

    }

    /**
     * 智能分析(异步)
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<BiResponse> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                             GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String chartName = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        // 如果分析目标为空，就抛出请求参数错误异常，并给出提示
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        // 如果名称不为空，并且名称长度大于100，就抛出异常，并给出提示
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");

        UserVO loginUser = userService.getLoginUser(request);
        long biModelId = 1659171950288818178L;

        // 判断文件大小
        long size=multipartFile.getSize();
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件大小超过1MB");

        //获取原始文件名
        String originalFilename = multipartFile.getOriginalFilename();
        //获取文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);

        // 判断文件类型
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "不支持的文件类型");

        redisLimiterManager.doRateLimit("genChartByAi"+loginUser.getId());

        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        // 拼接分析目标
        String userGoal = goal;
        // 如果图表类型不为空
        if (StringUtils.isNotBlank(chartType)) {
            // 就将分析目标拼接上“请使用”+图表类型
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        // 压缩后的数据（把multipartFile传进来）
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        //先把chart保存到数据库中
        Chart chart = new Chart();
        chart.setChartName(chartName);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        CompletableFuture.runAsync(()->{
            //更新图表的状态
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus("running");
            boolean b = chartService.updateById(updateChart);
            if (!b) {
                handleChartUpdateError(chart.getId(), "更新图表执行状态失败");
                return ;
            }
            // 调用AI
            try {
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> aiManager.sendMsgToXingHuo(true, userInput.toString()), threadPoolExecutor);
                String result = future.get(20, TimeUnit.SECONDS); // 设置20秒超时

                // 对返回结果做拆分,按照5个中括号进行拆分
                String[] splits = result.split("'【【【【【'");
                // 拆分之后还要进行校验
                if (splits.length < 3) {
                    handleChartUpdateError(chart.getId(), "AI 生成错误");
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
                }
                String genChart = splits[1].trim();
                String genResult = splits[2].trim();
                // 更新图表
                Chart updateChartInfo = new Chart();
                updateChartInfo.setId(chart.getId());
                updateChartInfo.setGenerateChart(genChart);
                updateChartInfo.setGenerateResult(genResult);
                updateChartInfo.setStatus("succeed");
                boolean updateResult = chartService.updateById(updateChartInfo);
                if (!updateResult) {
                    handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
                }
            } catch (TimeoutException e) {
                handleChartUpdateError(chart.getId(), "AI 生成超时");
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成超时");
            } catch (InterruptedException | ExecutionException e) {
                handleChartUpdateError(chart.getId(), "AI 生成异常");
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成异常");
            }

        }, threadPoolExecutor);

        BiResponse biResponse = new BiResponse();
//        biResponse.setGenerateChart(genChart);
//        biResponse.setGenerateResult(genResult);
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);

    }

    /**
     * 智能分析(异步消息队列)
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async/mq")
    public BaseResponse<BiResponse> genChartByAiAsyncMq(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String chartName = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        // 如果分析目标为空，就抛出请求参数错误异常，并给出提示
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        // 如果名称不为空，并且名称长度大于100，就抛出异常，并给出提示
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");

        UserVO loginUser = userService.getLoginUser(request);

        // 判断文件大小
        long size=multipartFile.getSize();
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件大小超过1MB");

        //获取原始文件名
        String originalFilename = multipartFile.getOriginalFilename();
        //获取文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);

        // 判断文件类型
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "不支持的文件类型");

        redisLimiterManager.doRateLimit("genChartByAi"+loginUser.getId());

        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        // 拼接分析目标
        String userGoal = goal;
        // 如果图表类型不为空
        if (StringUtils.isNotBlank(chartType)) {
            // 就将分析目标拼接上“请使用”+图表类型
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        // 压缩后的数据（把multipartFile传进来）
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        //先把chart保存到数据库中
        Chart chart = new Chart();
        chart.setChartName(chartName);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

       long newChartId = chart.getId();
       myMessageProducer.sendMessage(String.valueOf(newChartId));
       BiResponse biResponse= new BiResponse();
       biResponse.setChartId(newChartId);
       return ResultUtils.success(biResponse);

    }
    private void handleChartUpdateError(Long chartId, String execMessage) {
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setStatus("failed");
        updateChart.setExecMessage(execMessage);
        boolean b = chartService.updateById(updateChart);
        if (!b) {
            log.error("更新图表状态失败"+chartId+","+execMessage);
        }
    }


    /**
     * 获取我创建的图表
     */
    @PostMapping("/list/mypage")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest, HttpServletRequest request)  {
        UserVO userVo = userService.getLoginUser(request);
        String redisKey=String.format("zwbi:user:mychart:%s", userVo.getId());
        Page<Chart> page = (Page<Chart>) redisTemplate.opsForValue().get(redisKey);
        if (page!=null){
            return ResultUtils.success(page);
        }
        long current = 1;
        long size = 10;
        Chart chartQuery = new Chart();
        if (chartQueryRequest != null) {
            BeanUtils.copyProperties(chartQueryRequest, chartQuery);
            current = chartQueryRequest.getCurrent();
            size = chartQueryRequest.getPageSize();
        }
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>(chartQuery);
        queryWrapper.eq("userId", userVo.getId());
        queryWrapper.orderByDesc("createTime"); // 添加排序条件
        Page<Chart> chartPage = chartService.page(new Page<>(current, size), queryWrapper);
        if(chartPage.getTotal()>0){
            try {
                redisTemplate.opsForValue().set(redisKey,chartPage,30000, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.error("redis set key error",e);
            }
        }
        return ResultUtils.success(chartPage);

    }

    /**
     * 创建图表
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
        //设置创建人ID
        UserVO userVo = userService.getLoginUser(request);
        chart.setUserId(userVo.getId());
        boolean result = chartService.save(chart);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(chart.getId());
    }

    /**
     * 删除图表
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
        Long chartId = deleteRequest.getId();
        Chart chart = chartService.getById(chartId);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        UserVO loginUser = userService.getLoginUser(request);

        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        Long userId = loginUser.getId();
        if (userId != null && !userId.equals(chart.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(chartId);
        return ResultUtils.success(b);
    }

    /**
     * 更新图表
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
     * 根据 id 获取图表
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        return ResultUtils.success(chart);
    }

    /**
     * 获取图表列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list")
    public BaseResponse<List<Chart>> listChart(ChartQueryRequest chartQueryRequest, HttpServletRequest request) {
        Chart chartQuery = new Chart();
        if (chartQueryRequest != null) {
            BeanUtils.copyProperties(chartQueryRequest, chartQuery);
        }
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>(chartQuery);
        List<Chart> chartList = chartService.list(queryWrapper);
//        List<ChartVO> chartVOList = chartList.stream().map(chart -> {
//            ChartVO chartVO = new ChartVO();
//            BeanUtils.copyProperties(chart, chartVO);
//            return chartVO;
//        }).collect(Collectors.toList());
        return ResultUtils.success(chartList);
    }

    /**
     * 分页获取图表列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(ChartQueryRequest chartQueryRequest, HttpServletRequest request) {
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
//        Page<ChartVO> chartVOPage = new PageDTO<>(chartPage.getCurrent(), chartPage.getSize(), chartPage.getTotal());
//        List<ChartVO> chartVOList = chartPage.getRecords().stream().map(chart -> {
//            ChartVO chartVO = new ChartVO();
//            BeanUtils.copyProperties(chart, chartVO);
//            return chartVO;
//        }).collect(Collectors.toList());
        //chartVOPage.setRecords(chartVOList);
        return ResultUtils.success(chartPage);
    }

    // endregion
}
