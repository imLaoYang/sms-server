package com.ydl.sms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ydl.sms.entity.SendLogEntity;
import com.ydl.sms.vo.SendLogPageVO;
import com.ydl.sms.vo.SendLogVO;

import java.util.Map;

public interface SendLogService extends IService<SendLogEntity> {

  Page<SendLogVO> pageLog(Page<SendLogVO> page, Map<String,Object> map);
  Page<SendLogPageVO> sendLogpage(Page<SendLogPageVO> page, SendLogPageVO sendLogPageVO);



}
