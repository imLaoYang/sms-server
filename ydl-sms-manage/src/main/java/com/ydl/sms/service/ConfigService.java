package com.ydl.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ydl.sms.entity.ConfigEntity;

import java.util.List;

public interface ConfigService extends IService<ConfigEntity> {

  ConfigEntity getByName(String name);

  void setNewLevel(ConfigEntity entity);

  List<ConfigEntity> getLevel(List<String> ids);


  void sendUpdateMessage();
}
