package com.ydl.sms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ydl.sms.entity.ReceiveLogEntity;
import com.ydl.sms.mapper.ReceiveLogMapper;
import com.ydl.sms.service.ReceiveLogService;
import com.ydl.sms.vo.ReceiveLogVO;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ReceiveLogServiceImpl extends ServiceImpl<ReceiveLogMapper, ReceiveLogEntity> implements ReceiveLogService {
  @Override
  public Page<ReceiveLogVO> pageLog(Page<ReceiveLogVO> page, Map<String, Object> map) {
    IPage<ReceiveLogVO> iPage = baseMapper.selectLogPage(page, map);
    page.setRecords(iPage.getRecords());
    return page;
  }
}
