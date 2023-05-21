package com.ydl.sms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ydl.sms.dto.TemplateDTO;
import com.ydl.sms.entity.TemplateEntity;
import com.ydl.sms.entity.base.BaseEntity;
import com.ydl.sms.mapper.TemplateMapper;
import com.ydl.sms.service.TemplateService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TemplateServiceImpl extends ServiceImpl<TemplateMapper, TemplateEntity> implements TemplateService {

  @Override
  public String getCode() {
    LambdaQueryWrapper<TemplateEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.orderByDesc(BaseEntity::getCreateTime)
            .last("limit 1");
    TemplateEntity entity = baseMapper.selectOne(wrapper);
    if (entity != null) {
      String code = entity.getCode();
      if (code.startsWith("DXMB")) {
        int num = Integer.parseInt(code.split("_")[1]) + 1;
        return "DXMB_" + String.format("%09d", num);
      }
    }

    return "DXMB_000000001";
  }

  @Override
  public TemplateEntity getByName(String name) {
    LambdaQueryWrapper<TemplateEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(TemplateEntity::getName,name);
    return baseMapper.selectOne(wrapper);
  }

  @Override
  public void customPage(Page<TemplateDTO> page, Map<String, String> params) {
    IPage<TemplateDTO> templateDTOIPage = baseMapper.custom(page, params);
    page.setRecords(templateDTOIPage.getRecords());
  }

  @Override
  public List<TemplateDTO> customList(Map<String, String> params) {
    return baseMapper.custom(params);
  }
}
