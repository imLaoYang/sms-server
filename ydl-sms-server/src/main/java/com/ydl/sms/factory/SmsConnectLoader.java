package com.ydl.sms.factory;


import com.alibaba.fastjson.JSON;
import com.ydl.sms.config.RedisLock;
import com.ydl.sms.entity.ConfigEntity;
import com.ydl.sms.entity.SmsConfig;
import com.ydl.sms.model.ServerTopic;
import com.ydl.sms.service.ConfigService;
import com.ydl.sms.service.SignatureService;
import com.ydl.sms.service.TemplateService;
import com.ydl.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 通道实例加载器
 * 执行时间：
 * 1、项目启动时
 * 2、通道重新排序时
 */
@Component
@Slf4j
public class SmsConnectLoader implements CommandLineRunner {

  // 第一次初始化的通道集合
  private static final List<Object> CONFIG_LIST = new ArrayList<>();

  // redis加锁的token
  private static String BUILD_NEW_CONFIG_TOKEN = null;


  // 新构建的通道集合
  private static List<ConfigEntity> FUTURE_CONFIG_LIST;

  @Autowired
  private RedisTemplate redisTemplate;


  @Autowired
  private ConfigService configService;

  @Autowired
  private RedisLock redisLock;

  @Override
  public void run(String... args) throws Exception {
    initConnect();
  }

  /**
   * 根据通道配置，初始化每个通道的bean对象
   */
  public void initConnect() {

    // 1、查询数据库获得通道列表
    List<ConfigEntity> configEntities = configService.listForConnect();
    ArrayList<Object> constructList = new ArrayList<>();

    // 2、遍历通道列表，通过反射创建每个通道的Bean对象（例如AliyunSmsService、MengWangSmsService等）
    configEntities.forEach(configEntity -> {

      try {
        // 注入smsConfig属性值
        SmsConfig smsConfig = new SmsConfig();
        smsConfig.setId(configEntity.getId());
        smsConfig.setDomain(configEntity.getDomain());
        smsConfig.setName(configEntity.getName());
        // trim去除多余空格
        smsConfig.setPlatform(configEntity.getPlatform().trim());
        smsConfig.setAccessKeyId(configEntity.getAccessKeyId().trim());
        smsConfig.setAccessKeySecret(configEntity.getAccessKeySecret().trim());
        if (StringUtils.isNotBlank(configEntity.getOther())) {
          // 用JSON格式传入other
          LinkedHashMap linkedHashMap = JSON.parseObject(configEntity.getOther(), LinkedHashMap.class);
          smsConfig.setOtherConfig(linkedHashMap);
        }

        // 通过反射创建通道实例
        String className = "com.ydl.sms.sms." + configEntity.getPlatform() + "SmsService";
        Class<?> aClass = Class.forName(className);
        Constructor<?> constructor = aClass.getConstructor(SmsConfig.class); // 拿到构造方法
        Object constructors = constructor.newInstance(smsConfig);// 创建实例

        // 从spring容器获取bean
        SignatureService signatureService = SpringUtils.getBean(SignatureService.class);
        TemplateService templateService = SpringUtils.getBean(TemplateService.class);
        // 拿到父类对象
        Field signatureField = aClass.getSuperclass().getDeclaredField("signatureService");
        Field templateField = aClass.getSuperclass().getDeclaredField("templateService");
        // 打开权限（原权限为protected）
        signatureField.setAccessible(true);
        templateField.setAccessible(true);
        // 设置属性
        signatureField.set(constructors, signatureService);
        templateField.set(constructors, templateService);
        // 添加进list里
        constructList.add(constructors);

        log.info("初始化通道成功：{}，{}", smsConfig.getName(), smsConfig.getPlatform());
      } catch (Exception e) {
        log.warn("初始化异常,{}", e.getMessage());
      }

    });

    // 3、将每个通道的Bean对象保存到CONNECT_LIST集合中
    if (!CONFIG_LIST.isEmpty()) {
      CONFIG_LIST.clear(); // 清空
    }

    CONFIG_LIST.addAll(constructList);

    // redis解锁
    if (StringUtils.isNotBlank(BUILD_NEW_CONFIG_TOKEN)) {
      redisLock.unlock("buildNewConnect", BUILD_NEW_CONFIG_TOKEN);
    }

    log.info("通道初始化完成,{}", CONFIG_LIST);

  }


  /**
   * 通过通道等级获取通道实例
   *
   * @param level 通道等级
   * @param <T>   泛型
   * @return 通道实体类
   */
  public <T> T getConfigByLevel(Integer level) {
    return (T) CONFIG_LIST.get(level - 1);
  }


  /**
   * 检查通道等级
   *
   * @param level 通道等级
   * @return 是否小于传入的通道等级
   */
  public boolean checkConfigLevel(Integer level) {
    return CONFIG_LIST.size() < level;
  }


  /**
   * 通道调整： 构建新的通道
   */
  public void buildNewConfig() {

    // 加锁一个小时（一次只能一台机器操作）
    String token = redisLock.tryLock("buildNewConfig", 1000 * 60 * 60);
    log.info("构建新通道token:{}", token);
    if (StringUtils.isNotBlank(token)) {
      List<ConfigEntity> listForNewConfig = configService.listForNewConnect();
      FUTURE_CONFIG_LIST = listForNewConfig;
      redisTemplate.opsForValue().set("NEW_CONFIG_SERVER", ServerRegister.SERVER_ID);
      BUILD_NEW_CONFIG_TOKEN = token;
    }
  }

  /**
   * 通道调整：通知redis发布订阅消息，通知其他服务，应用新的通道
   */
  public void changeNewConfigMessage() {
    redisTemplate.convertAndSend("TOPIC_HIGH_SERVER", ServerTopic
            .builder().option(ServerTopic.USE_NEW_CONNECT).value(ServerRegister.SERVER_ID).build().toString());
  }

  /**
   * 通道调整：通知redis发布订阅消息，通知其他服务，初始化通道
   */
  public void changeNewConfig(){

    Object newConfigServer = redisTemplate.opsForValue().get("NEW_CONFIG_SERVER");

    if (ServerRegister.SERVER_ID.equals(newConfigServer) && !CollectionUtils.isEmpty(FUTURE_CONFIG_LIST)){
      // 不为空则执行数据库操作 并清空缓存
      boolean b = configService.updateBatchById(FUTURE_CONFIG_LIST);
      log.info("更新通道{}",b);
      FUTURE_CONFIG_LIST.clear();
      // 初始化通道
      redisTemplate.convertAndSend("TOPIC_HIGH_SERVER", ServerTopic
              .builder().option(ServerTopic.INIT_CONNECT).value(ServerRegister.SERVER_ID).build().toString());
    }

  }

}
