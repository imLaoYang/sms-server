package com.ydl.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ydl.sms.entity.PlatformEntity;

public interface PlatformService extends IService<PlatformEntity> {

  PlatformEntity getByName(String name);

}
