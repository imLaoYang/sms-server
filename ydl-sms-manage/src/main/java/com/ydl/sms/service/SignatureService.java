package com.ydl.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ydl.sms.entity.SignatureEntity;

public interface SignatureService extends IService<SignatureEntity> {

  // 自动生成signature表中的code签名编码
  String getCode();

  SignatureEntity getByName(String name);

}
