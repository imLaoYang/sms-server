package com.ydl.sms.controller;

import com.ydl.base.BaseController;
import com.ydl.base.R;
import com.ydl.sms.service.ReceiveLogService;
import com.ydl.sms.service.SendLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
  public R receivePage() {
    return R.success();

  }

  // 发送日志-分页
  @GetMapping("sendPage")
  @ApiOperation("接收日志-分页")
  public R sendPage() {
    return R.success();
  }

  // 业务统计-发送记录列表页获取
  @GetMapping("sendLogPage")
  @ApiOperation("发送记录-分页")
  public R sendLogPage() {
    return R.success();
  }

//
//  @GetMapping("{id}")
//  @ApiOperation("信息")
//  public R<SendLogEntity> get(@PathVariable("id") Long id) {
//    SendLogEntity data = sendLogService.getById(id);
//
//    return R.success(data);
//  }


}
