package com.ydl.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ydl.sms.entity.ConfigEntity;

import java.util.List;

/**
 * 配置表
 *
 */
public interface ConfigService extends IService<ConfigEntity> {

    List<ConfigEntity> findByTemplateSignature(String template, String signature);
}
