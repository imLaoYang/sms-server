package com.ydl.sms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ydl.sms.entity.ConfigSignatureEntity;
import com.ydl.sms.mapper.ConfigSignatureMapper;
import com.ydl.sms.service.ConfigSignatureService;
import org.springframework.stereotype.Service;

@Service
public class ConfigSignatureServiceImpl extends ServiceImpl<ConfigSignatureMapper, ConfigSignatureEntity> implements ConfigSignatureService {
}
