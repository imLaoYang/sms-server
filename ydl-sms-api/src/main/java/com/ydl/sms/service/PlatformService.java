package com.ydl.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ydl.sms.entity.PlatformEntity;

/**
 * 平台表
 *
 */
public interface PlatformService extends IService<PlatformEntity> {

    PlatformEntity getByAccessKeyId(String accessKeyId);
}
