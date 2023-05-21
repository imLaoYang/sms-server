package com.ydl.sms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ydl.sms.entity.SendLogEntity;
import com.ydl.sms.mapper.SendLogMapper;
import com.ydl.sms.service.SendLogService;
import com.ydl.sms.vo.MarketingStatisticsCountVO;
import com.ydl.sms.vo.SendLogPageVO;
import com.ydl.sms.vo.SendLogVO;
import com.ydl.sms.vo.StatisticsCountVO;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

@Service
public class SendLogServiceImpl extends ServiceImpl<SendLogMapper, SendLogEntity> implements SendLogService {
  @Override
  public Page<SendLogVO> pageLog(Page<SendLogVO> page, Map<String, Object> map) {
    IPage<SendLogVO> iPage = baseMapper.selectLogPage(page, map);
    page.setRecords(iPage.getRecords());

    return page;
  }

  @Override
  public Page<SendLogPageVO> sendLogpage(Page<SendLogPageVO> page, SendLogPageVO sendLogPageVO) {
    IPage<SendLogPageVO> iPage = baseMapper.sendLogPage(page, sendLogPageVO);
    page.setRecords(iPage.getRecords());

    return page;
  }

  @Override
  public Page<StatisticsCountVO> countPage(Page<StatisticsCountVO> page, Map<String, Object> map) {
    IPage<StatisticsCountVO> iPage = baseMapper.countPage(page, map);
    page.setRecords(iPage.getRecords());
    return page;
  }

  @Override
  public List<StatisticsCountVO> trend(Map<String, Object> params) {
    List<StatisticsCountVO> list = baseMapper.trend(params);
    return list;
  }

  @Override
  public List<Map> countForConfig(Map<String, Object> params) {
    List<Map> maps = baseMapper.countForConfig(params);
    return maps;
  }

  /**
   *
   * @param params
   * @return 计算完各通道送达率的List集合
   */
  @Override
  public List<Map> rateForConfig(Map<String, Object> params) {
    List<Map> list = baseMapper.countForConfig(params);
    for (Map map : list) {
      int count = Integer.parseInt(map.get("count").toString());
      int success = Integer.parseInt(map.get("value").toString());
      DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance();
      // 最大保留两位小数
      decimalFormat.setMaximumFractionDigits(2);
      // 四舍五入模式
      decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
      double rate = (double) success / (double) count * 100;
      // 格式化结果
      String formatRate = decimalFormat.format(rate);
      map.put("value",formatRate);
    }
    return list;
  }

  @Override
  public MarketingStatisticsCountVO getMarketingCount(Map<String, Object> params) {
    MarketingStatisticsCountVO marketingCount = baseMapper.getMarketingCount(params);
    return marketingCount;
  }

}
