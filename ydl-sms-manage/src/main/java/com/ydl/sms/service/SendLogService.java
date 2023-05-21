package com.ydl.sms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ydl.sms.entity.SendLogEntity;
import com.ydl.sms.vo.MarketingStatisticsCountVO;
import com.ydl.sms.vo.SendLogPageVO;
import com.ydl.sms.vo.SendLogVO;
import com.ydl.sms.vo.StatisticsCountVO;

import java.util.List;
import java.util.Map;

public interface SendLogService extends IService<SendLogEntity> {

  Page<SendLogVO> pageLog(Page<SendLogVO> page, Map<String,Object> map);
  Page<SendLogPageVO> sendLogpage(Page<SendLogPageVO> page, SendLogPageVO sendLogPageVO);

  Page<StatisticsCountVO> countPage( Page<StatisticsCountVO> page, Map<String,Object> map);

  List<StatisticsCountVO> trend(Map<String,Object> params);


  List<Map> countForConfig(Map<String,Object> params);

  List<Map> rateForConfig(Map<String,Object> params);

  MarketingStatisticsCountVO getMarketingCount(Map<String,Object> params);



}
