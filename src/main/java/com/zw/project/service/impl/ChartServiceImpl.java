package com.zw.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zw.project.model.entity.Chart;
import com.zw.project.service.ChartService;
import com.zw.project.mapper.ChartMapper;
import org.springframework.stereotype.Service;

/**
* @author 16247
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2024-12-05 23:10:47
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{

}




