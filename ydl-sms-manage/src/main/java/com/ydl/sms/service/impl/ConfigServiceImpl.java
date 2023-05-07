package com.ydl.sms.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ydl.sms.dto.ConfigDTO;
import com.ydl.sms.entity.ConfigEntity;
import com.ydl.sms.mapper.ConfigMapper;
import com.ydl.sms.model.ServerTopic;
import com.ydl.sms.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 通道配置表
 */
@Service
@Slf4j
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, ConfigEntity> implements ConfigService {

  @Autowired
  private RedisTemplate redisTemplate;

  @Override
  public ConfigEntity getByName(String name) {
    LambdaUpdateWrapper<ConfigEntity> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(ConfigEntity::getName, name);
    return this.getOne(wrapper);
  }

  @Override
  public void getNewLevel(ConfigDTO entity) {
    LambdaUpdateWrapper<ConfigEntity> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(ConfigEntity::getIsEnable, 1);
    wrapper.eq(ConfigEntity::getIsActive, 1);
    wrapper.orderByDesc(ConfigEntity::getLevel);
    wrapper.last("limit 1");
    ConfigEntity configEntity = this.getOne(wrapper);
    if (configEntity == null) {
      entity.setLevel(1);
    } else {
      entity.setLevel(configEntity.getLevel() + 1);
    }
  }

  @Override
  public void sendUpdateMessage() {

    // TODO 发送消息，通知短信发送服务更新内存中的通道优先级
    //1 获取存活发送端
    Map map = redisTemplate.opsForHash().entries("SERVER_ID_HASH");
    log.info("全部发送端有：" + map);

    //获取当前时间
    long currentTimeMillis = System.currentTimeMillis();

    for (Object key : map.keySet()) {
      Object value = map.get(key);
      long lastTiem = Long.parseLong(value.toString());
      //某个实例未超过5分钟 这个服务上线的呢
      if (currentTimeMillis - lastTiem < (1000 * 60 * 5)) {
        //2 删除已经redis缓存的通道优先级
        redisTemplate.delete("listForConnect");

        //3通知 优先级变了
        ServerTopic serverTopic = ServerTopic.builder().option(ServerTopic.INIT_CONNECT).value(key.toString()).build();
        redisTemplate.convertAndSend("TOPIC_HIGH_SERVER", serverTopic.toString());

        return;
      }
    }
  }
}
