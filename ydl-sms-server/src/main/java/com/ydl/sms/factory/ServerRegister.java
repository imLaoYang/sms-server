package com.ydl.sms.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 服务注册器，将短信发送服务注册到Redis中，定时服务上报，定时服务检查
 */
@Component
@Slf4j
@Order(value = 100)
public class ServerRegister implements CommandLineRunner {
    //当前服务实例的唯一标识，可以使用UUID随机生成
    public static String SERVER_ID = null;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 项目启动时自动执行此方法，将当前服务实例注册到redis
     *
     * @param args
     */
    @Override
    public void run(String... args) {
        //TODO 服务注册器，项目启动时将当前服务id注册到Redis中，使用Redis的Hash结构，key为SERVER_ID_HASH，Hash结构的key为服务id，value为时间戳
        // SERVER_ID_HASH   key  value
        SERVER_ID = UUID.randomUUID().toString(); //当前实例key
        log.info("server实例启动，生成的id:{}", SERVER_ID);
        redisTemplate.opsForHash().put("SERVER_ID_HASH", SERVER_ID, System.currentTimeMillis());
    }

    /**
     * 定时服务报告
     * 报告服务信息证明服务存在 每三分钟报告一次，并传入当前时间戳
     */
    @Scheduled(cron = "1 1/3 * * * ?")
    public void serverReport() {
        //TODO 服务注册器，每三分钟报告一次，并传入当前时间戳

        log.info("服务定时上报。id:{}",SERVER_ID);
        redisTemplate.opsForHash().put("SERVER_ID_HASH", SERVER_ID, System.currentTimeMillis());
    }

    /**
     * 定时服务检查
     * 每十分钟检查一次服务列表，清空超过五分钟没有报告的服务
     */
    @Scheduled(cron = "30 1/10 * * * ?")
    public void checkServer() {
        //TODO 服务注册器，定时检查redis,每隔10分钟查看，超过5分钟还没上报自己信息的服务，清除掉
        log.info("定时服务检查。id:{}",SERVER_ID);
        Map map = redisTemplate.opsForHash().entries("SERVER_ID_HASH");
        log.info("当前服务有："+map);

        long now = System.currentTimeMillis();
        List removeKeys=new ArrayList(); //该要删除的key
        //jdk8 lambda表达式
        map.forEach((key,value)->{
            long registrTime = Long.parseLong(value.toString());
            if(now-registrTime>(5*60*1000)){
                removeKeys.add(key);
            }
        });

        log.info("该要删除的key有：{}",removeKeys);
        removeKeys.forEach(key->{
            redisTemplate.opsForHash().delete("SERVER_ID_HASH", key);
        });

    }
}

