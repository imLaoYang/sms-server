package com.ydl.sms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ydl.sms.entity.ConfigTemplateEntity;
import com.ydl.sms.mapper.ConfigTemplateMapper;
import com.ydl.sms.service.ConfigTemplateService;
import org.springframework.stereotype.Service;

@Service
public class ConfigTemplateServiceImpl extends ServiceImpl<ConfigTemplateMapper, ConfigTemplateEntity> implements ConfigTemplateService {
}
