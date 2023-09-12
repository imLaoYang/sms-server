package com.ydl.sms.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ydl.sms.entity.TimingPushEntity;
import com.ydl.sms.entity.base.BaseEntity;
import com.ydl.sms.factory.SmsFactory;
import com.ydl.sms.mapper.TimingPushMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时短信业务处理器
 */
@Component
@Slf4j
public class SendTimingSmsImpl implements SendTimingSms {

  @Autowired
  private TimingPushMapper timingPushMapper;

  @Autowired
  private SmsFactory smsFactory;

  /**
   * 发送定时短信
   * @param timing
   */
  @Override
  @Async
  public void execute(String timing) {
    // 1、查询数据库获取本次需要发送的定时短信
    LambdaQueryWrapper<TimingPushEntity> wrapper = new LambdaQueryWrapper<>();
    // status：0 未处理， 1 已处理
    wrapper.eq(TimingPushEntity::getStatus,0);
    wrapper.eq(TimingPushEntity::getTiming,timing);
    wrapper.orderByAsc(BaseEntity::getCreateTime);
    List<TimingPushEntity> list = timingPushMapper.selectList(wrapper);

    log.info("需要发送的短信条数是：{}，{}",timing,list.size());

    list.forEach( entity -> {
      // 2、调用短信工厂发送短信
      String request = entity.getRequest();
      smsFactory.send(request);
      // 3、更新短信发送状态为“已处理”
      entity.setStatus(1);
      entity.setUpdateTime(LocalDateTime.now());
      timingPushMapper.updateById(entity);
    });

    log.info("发送定时短信成功：{}",timing);
  }
}
