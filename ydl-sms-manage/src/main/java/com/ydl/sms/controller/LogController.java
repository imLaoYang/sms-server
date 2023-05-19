package com.ydl.sms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ydl.base.BaseController;
import com.ydl.base.R;
import com.ydl.sms.entity.SendLogEntity;
import com.ydl.sms.service.ReceiveLogService;
import com.ydl.sms.service.SendLogService;
import com.ydl.sms.vo.ReceiveLogVO;
import com.ydl.sms.vo.SendLogPageVO;
import com.ydl.sms.vo.SendLogVO;
import com.ydl.utils.DateUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 短信服务-服务日志
 */
@RestController
@RequestMapping("log")
@Api(tags = "短信服务-服务日志")
public class LogController extends BaseController {

  @Autowired
  ReceiveLogService receiveLogService;

  @Autowired
  SendLogService sendLogService;

  // 接收日志-分页
  @GetMapping("receivePage")
  @ApiOperation("接收日志-分页")
  public R<Page<ReceiveLogVO>> receivePage(ReceiveLogVO receiveLogVO) {
    Page<ReceiveLogVO> page = getPage();
    // 封装一个查询map
    HashMap<String, Object> receiveLogMap = new HashMap<>();
    LocalDateTime startCreateTime = getStartCreateTime();
    LocalDateTime endCreateTime = getEndCreateTime();
    if (startCreateTime != null) {
      receiveLogMap.put("startCreateTime", DateUtils.format(startCreateTime, DateUtils.DEFAULT_DATE_TIME_FORMAT));
    }
    if (endCreateTime != null) {
      receiveLogMap.put("endCreateTime", DateUtils.format(endCreateTime, DateUtils.DEFAULT_DATE_TIME_FORMAT));
    }
    receiveLogMap.put("platformName", receiveLogVO.getPlatformName());
    receiveLogMap.put("signatureName", receiveLogVO.getSignatureName());
    receiveLogMap.put("templateName", receiveLogVO.getTemplateName());
    // 查询
    Page<ReceiveLogVO> receiveLogVOPage = receiveLogService.pageLog(page, receiveLogMap);

    return R.success(receiveLogVOPage);
  }

  // 发送日志-分页
  @GetMapping("sendPage")
  @ApiOperation("接收日志-分页")
  public R<Page<SendLogVO>> sendPage(SendLogVO sendLogVO) {
    Page<SendLogVO> page = getPage();
    // 封装一个查询map
    HashMap<String, Object> sendLogMap = new HashMap<>();
    LocalDateTime startCreateTime = getStartCreateTime();
    LocalDateTime endCreateTime = getEndCreateTime();
    if (startCreateTime != null) {
      sendLogMap.put("startCreateTime", DateUtils.format(startCreateTime, DateUtils.DEFAULT_DATE_TIME_FORMAT));
    }
    if (endCreateTime != null) {
      sendLogMap.put("endCreateTime", DateUtils.format(endCreateTime, DateUtils.DEFAULT_DATE_TIME_FORMAT));
    }
    sendLogMap.put("signatureName", sendLogVO.getSignatureName());
    sendLogMap.put("templateName", sendLogVO.getTemplateName());
    sendLogMap.put("mobile", sendLogVO.getMobile());
    sendLogMap.put("platformName", sendLogVO.getPlatformName());
    // 查询
    Page<SendLogVO> sendLogVOPage = sendLogService.pageLog(page, sendLogMap);

    return R.success(sendLogVOPage);
  }

  // 业务统计-发送记录列表页获取
  @GetMapping("sendLogPage")
  @ApiOperation("发送记录-分页")
  public R sendLogPage(SendLogPageVO vo) {
    Page<SendLogPageVO> page = getPage();
    Page<SendLogPageVO> sendLogPageVOPage = sendLogService.sendLogpage(page, vo);
    List<SendLogPageVO> record = sendLogPageVOPage.getRecords().stream().map(i -> {
      if (StringUtils.isNotBlank(i.getTemplateContent())) {
        String content = i.getTemplateContent().replaceAll("(\\$\\{)([\\w]+)(\\})", "******");
        i.setContentText(content);
        i.buildRemark();
        i.cleanBigField();
      }
      return i;
    }).collect(Collectors.toList());
    page.setRecords(record);

    return R.success(page);
  }


  @GetMapping("{id}")
  @ApiOperation("信息")
  public R<SendLogEntity> get(@PathVariable("id") Long id) {
    SendLogEntity data = sendLogService.getById(id);

    return R.success(data);
  }


}
