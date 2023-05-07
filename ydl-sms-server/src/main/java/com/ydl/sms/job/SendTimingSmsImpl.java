package com.ydl.sms.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ydl.sms.entity.TimingPushEntity;
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
    public void execute(String timing) {//timing格式：yyyy-MM-dd HH:mm  2021-12-25 18:00
        //TODO 查询数据库获取本次需要发送的定时短信，调用短信工厂发送短信
        //1、查询数据库获取本次需要发送的定时短信
        LambdaQueryWrapper<TimingPushEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TimingPushEntity::getStatus,0);
        wrapper.eq(TimingPushEntity::getTiming,timing);
        wrapper.orderByAsc(TimingPushEntity::getCreateTime);
        List<TimingPushEntity> list = timingPushMapper.selectList(wrapper);

        log.info("这一批次要发的短信条数是：{}，{}",timing,list.size());

        list.forEach(x->{
            //2、调用短信工厂发送短信
            String request = x.getRequest();
            smsFactory.send(request);
            //3、更新短信发送状态为“已处理”
            x.setStatus(1);
            x.setUpdateTime(LocalDateTime.now());
            x.setUpdateUser("system");
            timingPushMapper.updateById(x);
        });

        log.info("任务执行完毕"+timing);

    }
}
