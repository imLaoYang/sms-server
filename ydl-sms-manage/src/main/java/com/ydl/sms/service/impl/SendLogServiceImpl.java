package com.ydl.sms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ydl.sms.entity.SendLogEntity;
import com.ydl.sms.mapper.SendLogMapper;
import com.ydl.sms.service.SendLogService;
import com.ydl.sms.vo.SendLogPageVO;
import com.ydl.sms.vo.SendLogVO;
import org.springframework.stereotype.Service;

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

}
