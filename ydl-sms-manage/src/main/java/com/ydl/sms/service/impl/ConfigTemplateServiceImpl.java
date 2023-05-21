package com.ydl.sms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ydl.sms.dto.ConfigDTO;
import com.ydl.sms.entity.ConfigTemplateEntity;
import com.ydl.sms.mapper.ConfigTemplateMapper;
import com.ydl.sms.service.ConfigTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConfigTemplateServiceImpl extends ServiceImpl<ConfigTemplateMapper, ConfigTemplateEntity> implements ConfigTemplateService {
  @Override
  public void merge(ConfigDTO configDTO) {
    if (!CollectionUtils.isEmpty(configDTO.getTemplateIds())) {
      LambdaQueryWrapper<ConfigTemplateEntity> wrapper = new LambdaQueryWrapper<>();
      wrapper.eq(ConfigTemplateEntity::getConfigId, configDTO.getId());
      // 去config_template表查询数据，封装id成List
      List<ConfigTemplateEntity> entityList = this.list(wrapper);
      List<String> idList = entityList.stream().map(i -> i.getConfigId()).collect(Collectors.toList());
      // 前端传来的templateId集合
      List<String> templateIds = configDTO.getTemplateIds();
      // 删除id集合
      List<String> deleIdList = idList.stream().filter(i -> !templateIds.contains(i)).collect(Collectors.toList());
      // 添加id集合
      List<String> addIdList = templateIds.stream().filter(i -> !idList.contains(i)).collect(Collectors.toList());
      // 判断是添加还是删除操作
      if (!CollectionUtils.isEmpty(deleIdList)){
          this.removeByIds(deleIdList);
          log.info("删除成功 configId：{} templateIds:{}",configDTO.getId(),deleIdList);
      }else {
          log.info("删除失败");
      }


      if (!CollectionUtils.isEmpty(addIdList)){
        List<ConfigTemplateEntity> addEntity = addIdList.stream().map(i -> {
          ConfigTemplateEntity configTemplateEntity = new ConfigTemplateEntity();
          configTemplateEntity.setConfigId(configDTO.getId());
          configTemplateEntity.setTemplateId(i);
          return configTemplateEntity;
        }).collect(Collectors.toList());
        this.saveBatch(addEntity);
        log.info("添加成功 configId：{} templateIds:{}",configDTO.getId(),addIdList);
      }else {
        log.info("添加失败----可能id已经存在---configId：{} templateIds:{}",configDTO.getId(),addIdList);
      }

    }


  }
}
