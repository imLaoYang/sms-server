package com.ydl.sms.factory;


import com.alibaba.fastjson.JSON;
import com.ydl.sms.dto.SmsSendDTO;
import com.ydl.sms.entity.ManualProcessEntity;
import com.ydl.sms.entity.SendLogEntity;
import com.ydl.sms.properties.SmsProperties;
import com.ydl.sms.service.ManualProcessService;
import com.ydl.sms.service.SendLogService;
import com.ydl.sms.sms.AbstractSmsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 短信发送工厂
 * 1、获得构造好的短信通道
 * 2、调用短信通道方法发送短信
 * 3、如果出现异常则，进行通道降级、选举
 * . 通道选举：smsConnectLoader.buildNewConnect()
 * . 通道降级：smsConnectLoader.changeNewConnectMessage()
 * 4、记录短信发送日志
 */


@Slf4j
@Component
public class SmsFactory {

  @Autowired
  private SmsConnectLoader smsConnectLoader;

  @Autowired
  private SmsProperties smsProperties;

  @Autowired
  private SendLogService sendLogService;
  // 人工处理表
  @Autowired
  private ManualProcessService manualProcessService;

  @Autowired
  private ServerRegister serverRegister;

  @Autowired
  private RedisTemplate redisTemplate;

  /**
   * 根据级别获得通道对象
   *
   * @param level 通道等级
   * @return 通道对象
   */
  public AbstractSmsService getAbstractSmsServiceByLevel(Integer level) {
    return smsConnectLoader.getConfigByLevel(level);
  }


  public String getConfigIdByLevel(Integer level) {
    AbstractSmsService abstractSmsService = smsConnectLoader.getConfigByLevel(level);
    return abstractSmsService.getConfig().getId();
  }


  /**
   * @param msg 短信要发送全部信息，数据库的（request字段）
   */
  public boolean send(String msg) {

    Integer level = 1; // 真实场景level应该为2
    Integer msgErrorNum = 0;
    do {
      log.info("发送短信 level:{},msg:{}", level, msg);
      SendLogEntity sendLogEntity = new SendLogEntity();
      LocalDateTime createTime = LocalDateTime.now();
      sendLogEntity.setCreateTime(createTime);
      sendLogEntity.setUpdateTime(createTime);
      long begin = System.currentTimeMillis();
      try {
        SmsSendDTO smsSendDTO = JSON.parseObject(msg, SmsSendDTO.class);
        AbstractSmsService abstractSmsService = null;

        // 检查可用通道等级，如果没有则需要人工处理
        if (smsConnectLoader.checkConfigLevel(level)) {
          ManualProcessEntity manualProcessEntity = new ManualProcessEntity();
          manualProcessEntity.setTemplate(smsSendDTO.getTemplate());
          manualProcessEntity.setSignature(smsSendDTO.getSignature());
          manualProcessEntity.setMobile(smsSendDTO.getMobile());
          manualProcessEntity.setConfigIds(StringUtils.join(smsSendDTO.getConfigIds()));
          manualProcessEntity.setRequest(JSON.toJSONString(smsSendDTO.getParams()));
          manualProcessEntity.setCreateTime(LocalDateTime.now());
          manualProcessService.save(manualProcessEntity);

          sendLogEntity.setConfigId("404");
          sendLogEntity.setConfigName("未找到");
          sendLogEntity.setConfigPlatform("未找到");
          sendLogEntity.setMobile(smsSendDTO.getMobile());
          sendLogEntity.setSignature(smsSendDTO.getSignature());
          sendLogEntity.setTemplate(smsSendDTO.getTemplate());
          sendLogEntity.setRequest(JSON.toJSONString(smsSendDTO));
          sendLogEntity.setApiLogId(smsSendDTO.getLogId());
          sendLogEntity.setStatus(0);
          sendLogEntity.setResponse("@未找到合适配置，需人工处理");
          return false;
        }

        //根据级别获取通道id
        String configId = getConfigIdByLevel(level);
        if (smsSendDTO.getConfigIds().contains(configId)) {

          // 获得通道实例化对象
          abstractSmsService = getAbstractSmsServiceByLevel(level);

          log.info("获得通道：{}，level:{}", abstractSmsService.getConfig().getName(), level);
          if (abstractSmsService == null) {

            // 获得下一个level通道
            log.info("通道为空 获取下一级别通道 :{}", level);
            level++;
            continue;
          }
        } else {
          log.info("当前级别不符合:{} 查找下一级别 :{}", configId, level);
          level++;
          continue;
        }


        // 2、已找到可用通道,构建日志对象
        sendLogEntity.setConfigId(abstractSmsService.getConfig().getId());
        sendLogEntity.setConfigName(abstractSmsService.getConfig().getName());
        sendLogEntity.setConfigPlatform(abstractSmsService.getConfig().getPlatform());
        sendLogEntity.setMobile(smsSendDTO.getMobile());
        sendLogEntity.setSignature(smsSendDTO.getSignature());
        sendLogEntity.setTemplate(smsSendDTO.getTemplate());
        sendLogEntity.setRequest(JSON.toJSONString(smsSendDTO));
        sendLogEntity.setApiLogId(smsSendDTO.getLogId());
        sendLogEntity.setStatus(1);

        // 3、发送短信
        String response = abstractSmsService.send(smsSendDTO.getMobile(), smsSendDTO.getParams(), smsSendDTO.getSignature(), smsSendDTO.getTemplate());

        // 4、检查发送结果(如果发送失败则会抛出异常)
        sendLogEntity.checkResponse(response);
        log.info("发送成功{}", response);
        return true;

      } catch (Exception e) {
        log.warn("发送异常 返回值：{}", sendLogEntity.getResponse(), e);
        sendLogEntity.setStatus(0);
        sendLogEntity.setError(getExceptionMessage(e));
        smsConnectLoader.changeNewConfig();


        /*
          5、通道重新排序
          * 检查通道失败次数是否超过阈值或一定比例，如果失败次数超过阈值，则降级通道，通道重新排序，如果失败次数超过一定比例则启动新通道备用
         */
        if (resetConfig(level)) {
          level++;
          continue;
        }

        // 6、通道热切换
        if (msgErrorNum >= smsProperties.getMessageErrorNum()) {
          // 消息失败数超过阈值，切换下一个通道
          msgErrorNum = 0;
          level++;
          log.info("短信单通道失败次数达到阈值,切换下一级别通道");
        } else {
          //短信单通道失败次数尚未达到阈值
          msgErrorNum++;
          log.info("短信单通道失败次数尚未达到阈值");
        }
      } finally {

        if (StringUtils.isNotBlank(sendLogEntity.getConfigId())) {
          long endTime = System.currentTimeMillis();
          sendLogEntity.setUseTime(begin - endTime);
          // 保存发送日志
          sendLogService.save(sendLogEntity);
        }
      }
    } while (true);
  }

  /**
   * 重新设置通道
   *
   * @return
   */
  private boolean resetConfig(Integer level) {
    ValueOperations<String, Integer> ops = redisTemplate.opsForValue();
    Integer configLevelFailNum = ops.get("config_level_" + level);
    if (configLevelFailNum == null) {
      configLevelFailNum = 0;
    }

    if (configLevelFailNum >= smsProperties.getConfigLevelFailNum()) {
      log.info("通道等级失败数超过阈值，重新排序");
      smsConnectLoader.changeNewConfig();

      return true;
    } else {
      if (configLevelFailNum >= smsProperties.getConfigBuildScale()) {
        log.info("通道等级失败数超过比例，重新选举");
        smsConnectLoader.buildNewConfig();
      }
      // 设置通道失败次数异常时间10分钟，代表十分钟内失败次数达到阈值就会被切换掉
      ops.set("config_level_" + level, configLevelFailNum + 1, 10, TimeUnit.MINUTES);
    }
    return false;
  }


  /**
   * 输出异常信息
   *
   * @param e 异常
   * @return
   */
  private String getExceptionMessage(Exception e) {
    StringWriter sw = new StringWriter();
    PrintWriter printWriter = new PrintWriter(sw);
    e.printStackTrace(printWriter);
    return sw.toString();
  }


}
