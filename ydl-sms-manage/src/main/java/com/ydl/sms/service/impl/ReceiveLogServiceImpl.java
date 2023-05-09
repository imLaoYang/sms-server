package com.ydl.sms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ydl.sms.entity.ReceiveLogEntity;
import com.ydl.sms.mapper.ReceiveLogMapper;
import com.ydl.sms.service.ReceiveLogService;
import org.springframework.stereotype.Service;

@Service
public class ReceiveLogServiceImpl extends ServiceImpl<ReceiveLogMapper, ReceiveLogEntity> implements ReceiveLogService {
}
