package com.ydl.sms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ydl.sms.entity.ReceiveLogEntity;
import com.ydl.sms.vo.ReceiveLogVO;

import java.util.Map;

public interface ReceiveLogService extends IService<ReceiveLogEntity> {

  Page<ReceiveLogVO> pageLog(Page<ReceiveLogVO> page,Map<String,Object> map);
}
