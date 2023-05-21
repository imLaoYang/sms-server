package com.ydl.sms.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ydl.sms.entity.ConfigEntity;
import com.ydl.sms.mapper.ConfigMapper;
import com.ydl.sms.model.ServerTopic;
import com.ydl.sms.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, ConfigEntity> implements ConfigService {

  @Autowired
  private RedisTemplate redisTemplate;
  @Override
  public ConfigEntity getByName(String name) {
    LambdaUpdateWrapper<ConfigEntity> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(ConfigEntity::getName, name)
            .orderByAsc(ConfigEntity::getLevel);

    return baseMapper.selectOne(wrapper);
  }

  @Override
  public void setNewLevel(ConfigEntity entity) {
    LambdaUpdateWrapper<ConfigEntity> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(ConfigEntity::getIsEnable, 1)
            .eq(ConfigEntity::getIsActive, 1)
            .orderByDesc(ConfigEntity::getLevel)
            .last("limit 1");
    ConfigEntity configEntity = baseMapper.selectOne(wrapper);
    if (configEntity == null) {
      entity.setLevel(1);
    } else {
      entity.setLevel(configEntity.getLevel() + 1);
    }

  }

  @Override
  public List<ConfigEntity> getLevel(List<String> ids) {
    return baseMapper.selectBatchIds(ids);
  }


  @Override
  public void sendUpdateMessage() {
    // TODO 发送消息，通知短信发送服务更新内存中的通道优先级
    // 先查询Redis中存活的发送端
    Map serveMap = redisTemplate.opsForHash().entries("SERVER_ID_HASH");
    log.info("全部的发送端有:{}",serveMap);
    // 获得当前时间戳
    long currentTimeMillis = System.currentTimeMillis();
    for (Object key : serveMap.keySet()) {
      // 获得Redis中的时间
      Object value = serveMap.get(key);
      long time = Long.parseLong(value.toString());
      // 跟当前时间比较，超过五分钟即为失效
      if (currentTimeMillis - time < (1000 * 60 * 5)){
        // 删除已经redis缓存的通道优先级
        redisTemplate.delete("listForConnect");
        // 发送消息构建，没有特别的含义，只是项目规定
        ServerTopic serverTopic = ServerTopic.builder().option(ServerTopic.INIT_CONNECT).value(key.toString()).build();
        // 通知,优先级变了；用Redis的发布订阅模式
        redisTemplate.convertAndSend("TOPIC_HIGH_SERVER",serverTopic);
        return;
      }


    }



  }


}
