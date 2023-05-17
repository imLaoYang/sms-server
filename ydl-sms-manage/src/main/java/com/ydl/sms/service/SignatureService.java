package com.ydl.sms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ydl.sms.dto.SignatureDTO;
import com.ydl.sms.entity.SignatureEntity;

import java.util.List;
import java.util.Map;

public interface SignatureService extends IService<SignatureEntity> {

  // 自动生成signature表中的code签名编码
  String getCode();

  SignatureEntity getByName(String name);

  void customPage(Page<SignatureDTO> page, Map<String, String> params);
  List<SignatureDTO> customList(Map<String, String> params);

}
