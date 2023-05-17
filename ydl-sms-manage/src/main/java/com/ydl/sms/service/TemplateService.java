package com.ydl.sms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ydl.sms.dto.TemplateDTO;
import com.ydl.sms.entity.TemplateEntity;

import java.util.List;
import java.util.Map;

public interface TemplateService extends IService<TemplateEntity> {

  // 获得模板编码
  String getCode();

  TemplateEntity getByName(String name);

  void customPage(Page<TemplateDTO> page, Map<String,String> params);
  List<TemplateDTO> customList(Map<String,String> params);

}
