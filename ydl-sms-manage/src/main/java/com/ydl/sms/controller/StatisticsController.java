package com.ydl.sms.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ydl.base.BaseController;
import com.ydl.base.R;
import com.ydl.sms.entity.ReceiveLogEntity;
import com.ydl.sms.entity.SendLogEntity;
import com.ydl.sms.entity.base.BaseEntity;
import com.ydl.sms.service.ReceiveLogService;
import com.ydl.sms.service.SendLogService;
import com.ydl.sms.utils.DaysUtil;
import com.ydl.sms.vo.SendLogVO;
import com.ydl.sms.vo.StatisticsCountVO;
import com.ydl.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 首页-统计
 */
@RestController
@RequestMapping("statistics")
public class StatisticsController extends BaseController {

  @Autowired
  private SendLogService sendLogService;

  @Autowired
  private ReceiveLogService receiveLogService;

  /**
   * 发送量统计(列表)
   *
   * @param sendLogVO
   * @return 统计结果VO
   */
  @GetMapping("count/page")
  public R<Page<StatisticsCountVO>> countPage(SendLogVO sendLogVO) {
    Page<StatisticsCountVO> page = getPage();
    Map<String, Object> map = new HashMap<String, Object>();
    if (getStartCreateTime() != null) {
      map.put("startCreateTime", DateUtils.format(getStartCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
    }
    if (getEndCreateTime() != null) {
      map.put("endCreateTime", DateUtils.format(getEndCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
    }
    map.put("signatureName", sendLogVO.getSignatureName());
    map.put("templateName", sendLogVO.getTemplateName());
    Page<StatisticsCountVO> statisticsCountVOPage = sendLogService.countPage(page, map);

    return R.success(statisticsCountVOPage);
  }

  /**
   * 首页-发送量统计
   *
   * @return 数量统计的VO
   */
  @GetMapping("count")
  public R<StatisticsCountVO> count() {
    LocalDateTime startCreateTime = getStartCreateTime();
    LocalDateTime endCreateTime = getEndCreateTime();
    LambdaQueryWrapper<SendLogEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.between((startCreateTime != null && endCreateTime != null), SendLogEntity::getCreateTime, startCreateTime, endCreateTime);
    // 先查出所有的总数
    int count = sendLogService.count(wrapper);
    wrapper.eq(SendLogEntity::getStatus, 1);
    // 查出成功的的数
    int success = sendLogService.count(wrapper);
    // 封装VO
    StatisticsCountVO statisticsCountVO = StatisticsCountVO.builder().count(count).success(success).fail(count - success).build();

    return R.success(statisticsCountVO);
  }

  /**
   * 首页-营销短信发送量趋势(条）
   * @return 发送量的map集合
   */
  @GetMapping("marketingTrend")
  public R marketingTrend() {
    // 营销平台id  先写死
    String platformId = "00000";
    LocalDateTime startCreateTime = getStartCreateTime();
    LocalDateTime endCreateTime = getEndCreateTime();
    Map<String, Object> params = new HashMap<>();
    if (startCreateTime != null) {
      params.put("startCreateTime", DateUtils.format(startCreateTime, DateUtils.DEFAULT_DATE_TIME_FORMAT));
    }
    if (endCreateTime != null) {
      params.put("endCreateTime", DateUtils.format(endCreateTime, DateUtils.DEFAULT_DATE_TIME_FORMAT));
    }
    params.put("platformId", platformId);
    // 数据库查询
    List<StatisticsCountVO> trendList = receiveLogService.trend(params);
    // 把查出的trendList变成map
    Map<String, StatisticsCountVO> trendMap = trendList.stream().collect(Collectors.toMap(StatisticsCountVO::getDate, statisticsCountVO -> statisticsCountVO));

    LambdaQueryWrapper<ReceiveLogEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.between((startCreateTime != null && endCreateTime != null), ReceiveLogEntity::getCreateTime, startCreateTime, endCreateTime)
            .eq(ReceiveLogEntity::getPlatformId, platformId);
    // 总数
    int count = receiveLogService.count(wrapper);
    wrapper.eq(ReceiveLogEntity::getStatus, 1);
    // 成功数
    int success = receiveLogService.count(wrapper);

    // 构建时间数组
     // 计算天数
    List<String> days = DaysUtil.getDays(startCreateTime, endCreateTime, "M.d");
    List<Integer> countList = new ArrayList<>();
    List<Integer> successList = new ArrayList<>();
    List<Integer> failList = new ArrayList<>();
    for (String day : days) {
      if (trendMap.containsKey(day)){
        StatisticsCountVO statisticsCountVO = trendMap.get(day);
        countList.add(statisticsCountVO.getCount());
        successList.add(statisticsCountVO.getSuccess());
        failList.add(statisticsCountVO.getCount() - statisticsCountVO.getSuccess());
      }else {
        countList.add(0);
        successList.add(0);
        failList.add(0);
      }
    }

    Map<String, Object> result = new HashMap<>();
    result.put("date",days);
    result.put("count", countList);
    result.put("success", successList);
    result.put("fail", failList);
    result.put("dataInfo", new HashMap<String, Integer>() {{
      put("count", count);
      put("success", success);
      put("fail", count - success);
    }});

    return R.success(result);
  }

  /**
   * 首页-总发送量趋势
   * @return Map结果集
   */
  @GetMapping("trend")
  public R<Map<String, Object>> trend() {
    Map<String, Object> params = new HashMap<>();
    if (getStartCreateTime() != null) {
      params.put("startCreateTime", DateUtils.format(getStartCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
    }
    if (getEndCreateTime() != null) {
      params.put("endCreateTime", DateUtils.format(getEndCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
    }
    // 数据库查询
    List<StatisticsCountVO> trendList = sendLogService.trend(params);
    Map<String, StatisticsCountVO> logsMap = trendList.stream().collect(Collectors.toMap(item -> item.getDate(), item -> item));
    // 构建时间数组
    List<String> days = DaysUtil.getDays(getStartCreateTime(), getEndCreateTime(), "M.d");
    List<Integer> count = new ArrayList<>();
    List<Integer> success = new ArrayList<>();
    List<Integer> fail = new ArrayList<>();
    for (String day : days) {
      if (logsMap.containsKey(day)) {
        StatisticsCountVO statisticsCountVO = logsMap.get(day);
        count.add(statisticsCountVO.getCount());
        success.add(statisticsCountVO.getSuccess());
        fail.add(statisticsCountVO.getCount() - statisticsCountVO.getSuccess());
      } else {
        count.add(0);
        success.add(0);
        fail.add(0);
      }
    }

    Map<String, Object> result = new HashMap<String, Object>();
    // 发送总量（条）
    result.put("count", count);
    // 发送成功量（条）
    result.put("success", success);
    // 发送失败量（条）
    result.put("fail", fail);
    result.put("date", days);

    return R.success(result);
  }


  /**
   * 首页-应用发送数量Top10(单位：个）
   *
   * @return Map集合，包含前十的发送数和总发送数
   */
  @GetMapping("top10")
  public R<Map<String, Object>> top10() {
    Map<String, Object> params = new HashMap();
    if (getStartCreateTime() != null) {
      params.put("startCreateTime", DateUtils.format(getStartCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
    }
    if (getEndCreateTime() != null) {
      params.put("endCreateTime", DateUtils.format(getEndCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
    }
    // 数据库查出前十的
    List<StatisticsCountVO> logList = receiveLogService.top10(params);

    List<String> platformNames = new ArrayList<>();
    List<Integer> countList = new ArrayList<>();
    List<Integer> successList = new ArrayList<>();
    List<Integer> failList = new ArrayList<>();
    logList.forEach(item -> {
      platformNames.add(item.getDate());
      successList.add(item.getSuccess());
      countList.add(item.getCount());
      failList.add(item.getCount() - item.getSuccess());
    });

    Map<String, Object> result = new HashMap<String, Object>();
    result.put("platformNames", platformNames);
    result.put("count", countList);
    result.put("success", successList);
    result.put("fail", failList);

    // 数据库查出总数
    LambdaQueryWrapper<ReceiveLogEntity> countWrapper = new LambdaQueryWrapper<>();
    countWrapper.between((getStartCreateTime() != null && getEndCreateTime() != null), BaseEntity::getCreateTime, getStartCreateTime(), getEndCreateTime());
    // 总数
    int count = receiveLogService.count(countWrapper);
    countWrapper.eq(ReceiveLogEntity::getStatus, 1);
    // 成功数
    int success = receiveLogService.count(countWrapper);
    // 放入Map中
    result.put("dataInfo", new HashMap<String, Integer>() {
      {
        put("count", count);
        put("success", success);
        put("fail", count - success);
      }
    });

    return R.success(result);
  }

  /**
   * 首页-各通道成功量（条）
   * @return
   */
  @GetMapping("countForConfig")
  public R countForConfig() {
    Map<String, Object> params = new HashMap<>();
    if (getStartCreateTime() != null) {
      params.put("startCreateTime", DateUtils.format(getStartCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
    }
    if (getEndCreateTime() != null) {
      params.put("endCreateTime", DateUtils.format(getEndCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
    }
    // SQL查询
    List<Map> countForConfig = sendLogService.countForConfig(params);

    return R.success(countForConfig);
  }

  /**
   * 首页-各通道送达率（%）
   * @return Map集合，第一个参数是通道名（config_name），第二个是rate
   */
  @GetMapping("rateForConfig")
  public R rateForConfig() {
    Map<String, Object> params = new HashMap<>();
    if (getStartCreateTime() != null) {
      params.put("startCreateTime", DateUtils.format(getStartCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
    }
    if (getEndCreateTime() != null) {
      params.put("endCreateTime", DateUtils.format(getEndCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
    }
    // 计算通道送达率
    List<Map> rateForConfig = sendLogService.rateForConfig(params);
    // 封装return的集合
    Map<String,Object> result = new HashMap<>();
    result.put("name",rateForConfig.stream().map(i -> i.get("name")).collect(Collectors.toList()));
    result.put("rate",rateForConfig.stream().map(i -> i.get("value")).collect(Collectors.toList()));

    return R.success(result);
  }


// 营销短信统计-列表页接口获取(暂时无功能)
//  @GetMapping("marketingCount/page")
//  public R marketingCountPage() {
//    return R.success();}

}
