package com.ydl.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ydl.sms.dto.ConfigDTO;
import com.ydl.sms.entity.ConfigTemplateEntity;

/**
 * 配置—模板表
 *
 *
 */
public interface ConfigTemplateService extends IService<ConfigTemplateEntity> {

    void merge(ConfigDTO entity);
}
