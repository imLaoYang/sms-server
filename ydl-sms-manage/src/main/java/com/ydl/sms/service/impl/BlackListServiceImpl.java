package com.ydl.sms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ydl.sms.entity.BlackListEntity;
import com.ydl.sms.mapper.BlackListMapper;
import com.ydl.sms.service.BlackListService;
import org.springframework.stereotype.Service;

@Service
public class BlackListServiceImpl extends ServiceImpl<BlackListMapper, BlackListEntity> implements BlackListService {
}
