package com.ydl.sms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ydl.sms.dto.ConfigDTO;
import com.ydl.sms.entity.ConfigSignatureEntity;
import com.ydl.sms.mapper.ConfigSignatureMapper;
import com.ydl.sms.service.ConfigSignatureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConfigSignatureServiceImpl extends ServiceImpl<ConfigSignatureMapper, ConfigSignatureEntity> implements ConfigSignatureService {

  /**
   * 合并操作，增删都在这个方法里面判断
   */
  @Override
  public void merge(ConfigDTO configDTO) {
    if (!CollectionUtils.isEmpty(configDTO.getSignatureIds())) {
      LambdaQueryWrapper<ConfigSignatureEntity> wrapper = new LambdaQueryWrapper<>();
      wrapper.eq(ConfigSignatureEntity::getConfigId, configDTO.getId());
      List<ConfigSignatureEntity> configSignatureEntityList = this.list(wrapper);
      // 把查出的数据id拿出来，封装成id集合
      List<String> CSidList = configSignatureEntityList.stream().map(i -> i.getSignatureId())
              .collect(Collectors.toList());
      // 前端传进来的Signature的id集合
      List<String> signatureIds = configDTO.getSignatureIds();
      // 过滤添加的id集合
      List<String> addIdList = signatureIds.stream().filter(i -> !CSidList.contains(i)).collect(Collectors.toList());
      // 过滤删除的id集合
      List<String> deleteList = CSidList.stream().filter(i -> !configDTO.getId().contains(i)).collect(Collectors.toList());


      // 判断是添加还删除操作
      if (!CollectionUtils.isEmpty(deleteList)){
        this.removeByIds(deleteList);
        log.info("删除成功 configId:{},deleteId:{}", configDTO.getId(), deleteList);
      }else {
        log.info("删除失败");
      }

      if (!CollectionUtils.isEmpty(addIdList)) {
        // 把configDto的数据封装进List进行批量增删
        List<ConfigSignatureEntity> addEntity = addIdList.stream().map(i -> {
          ConfigSignatureEntity configSignatureEntity = new ConfigSignatureEntity();
          configSignatureEntity.setConfigId(configDTO.getId());
          configSignatureEntity.setSignatureId(i);
          return configSignatureEntity;
        }).collect(Collectors.toList());
        this.saveBatch(addEntity);
        log.info("添加成功 configId:{},addIds:{}", configDTO.getId(), addIdList);
      }{
        log.info("添加失败");
      }

    }
  }
}
