package com.ydl.sms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ydl.sms.entity.PlatformEntity;
import com.ydl.sms.mapper.PlatformMapper;
import com.ydl.sms.service.PlatformService;
import org.springframework.stereotype.Service;

@Service
public class PlatformServiceImpl extends ServiceImpl<PlatformMapper, PlatformEntity> implements PlatformService {
}
