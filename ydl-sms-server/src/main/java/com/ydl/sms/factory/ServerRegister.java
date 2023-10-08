package com.ydl.sms.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 介绍：（springboot的接口）实现CommandLineRunner，在spring启动时会运行run方法（只会运行一次）
 * 功能：服务注册器，将短信发送服务注册到Redis中，定时服务上报，定时服务检查
 */
@Component
@Slf4j
public class ServerRegister implements CommandLineRunner {

  // 当前服务实例的唯一标识，使用UUID随机生成
  public static String SERVER_ID = null;

  @Autowired
  private RedisTemplate redisTemplate;

  /**
   * 项目启动时自动执行此方法，将当前服务实例注册到redis
   *
   * @param args
   * @throws Exception
   */
  @Override
  public void run(String... args) throws Exception {

    // 使用redis的hash结构,key为SERVER_ID_HASH，Hash结构的key为服务id，value为时间戳
    SERVER_ID = String.valueOf(UUID.randomUUID());
    log.info("服务器启动生成uuid为:{}", SERVER_ID);
    redisTemplate.opsForHash().put("SERVER_ID_HASH", SERVER_ID, System.currentTimeMillis());

  }

  /**
   * 定时任务报告
   * 服务报告，每三分钟重新插入一次时间戳，证明服务存在
   */
  @Scheduled(cron = "0 1/3 * * * ?")
  public void serverReport() {

    log.info("服务定时上报,uuid为:{}", SERVER_ID);
    redisTemplate.opsForHash().put("SERVER_ID_HASH", SERVER_ID, System.currentTimeMillis());

  }


  /**
   * 服务定期检查，删除5分钟内没有更新的id
   */
  @Scheduled(cron = "0 1/10 * * * ?")
  public void serverCheck() {

    log.info("定时服务检查,id:{}",SERVER_ID);
    Map map = redisTemplate.opsForHash().entries("SERVER_ID_HASH");
    log.info("当前服务有："+map);
    long nowTime = System.currentTimeMillis();
    List<String> deleteKeys = new ArrayList<>();

    map.forEach((key, value) -> {

      if (nowTime - Long.parseLong(value.toString()) > (5 * 60 * 1000)) {
        deleteKeys.add(key.toString());
      }

    });

    log.info("需要删除的key有:{}", deleteKeys);

    deleteKeys.forEach((keys) -> {
      redisTemplate.opsForHash().delete("SERVER_ID_HASH",keys);
    });


  }


}
