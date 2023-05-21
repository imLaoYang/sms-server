package com.ydl.sms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ydl.sms.entity.ReceiveLogEntity;
import com.ydl.sms.vo.ReceiveLogVO;
import com.ydl.sms.vo.StatisticsCountVO;

import java.util.List;
import java.util.Map;

public interface ReceiveLogService extends IService<ReceiveLogEntity> {

  Page<ReceiveLogVO> pageLog(Page<ReceiveLogVO> page,Map<String,Object> map);


  List<StatisticsCountVO> top10(Map<String,Object> params);

  List<StatisticsCountVO> trend(Map<String,Object> params);
}
