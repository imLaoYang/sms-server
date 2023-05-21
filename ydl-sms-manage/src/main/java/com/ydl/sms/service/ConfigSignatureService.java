package com.ydl.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ydl.sms.dto.ConfigDTO;
import com.ydl.sms.entity.ConfigSignatureEntity;

public interface ConfigSignatureService extends IService<ConfigSignatureEntity> {

  // 合并操作，增删都在这个方法里面判断
  void merge(ConfigDTO configDTO);


}
