package com.ydl.sms.redismq;


import com.ydl.sms.factory.SmsFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Redis消息队列监听：监听消息队列：TOPIC_HIGH_SMS，高优先级的短信，如验证码之类的短信
 * extends Thread 为这个类单独开辟一个线程来监听队列
 */
@Component
@Slf4j
public class HighSmsListener extends Thread {


  @Autowired
  private RedisTemplate redisTemplate;

  // Redis高优先级队列的key
  private String queueKey = "TOPIC_HIGH_SMS";

  // 监听时间
  @Value("${spring.redis.queue.pop.timeout}")
  private Long popTimeout = 8000L;

  @Autowired
  private SmsFactory smsFactory;

  private ListOperations opsForList;

  /**
   * 容器创建完成后执行
   */
  @PostConstruct
  private void init() {
    opsForList = redisTemplate.opsForList();
    // 启动线程，随即调用run()方法
    this.start();
  }


  // 监听TOPIC_HIGH_SMS队列，如果有消息则调用短信发送工厂发送实时短信；
  @Override
  public void run() {

    while (true) {
      log.debug("队列{}正在监听中", queueKey);
      // 拿到的是一个dto对象，转成string
      String message = (String) opsForList.rightPop(queueKey, popTimeout, TimeUnit.MILLISECONDS);
      if (StringUtils.isNotBlank(message)) {
        log.info("{}收到消息了：{}",queueKey,message);
        // 发送
        smsFactory.send(message);
      }
    }
  }
}
