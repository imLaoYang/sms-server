package com.ydl.sms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ydl.sms.entity.SendLogEntity;
import com.ydl.sms.mapper.SendLogMapper;
import com.ydl.sms.service.SendLogService;
import org.springframework.stereotype.Service;

@Service
public class SendLogServiceImpl extends ServiceImpl<SendLogMapper, SendLogEntity> implements SendLogService {
}
