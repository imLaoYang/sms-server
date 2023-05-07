package com.ydl.sms.factory;

import com.alibaba.fastjson.JSON;
import com.ydl.sms.config.RedisLock;
import com.ydl.sms.entity.ConfigEntity;
import com.ydl.sms.entity.SmsConfig;
import com.ydl.sms.model.ServerTopic;
import com.ydl.sms.service.ConfigService;
import com.ydl.sms.service.SignatureService;
import com.ydl.sms.service.TemplateService;
import com.ydl.sms.service.impl.SignatureServiceImpl;
import com.ydl.sms.service.impl.TemplateServiceImpl;
import com.ydl.utils.SpringUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
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
@Order(value = 101)
public class SmsConnectLoader implements CommandLineRunner {

    private static final List<Object> CONNECT_LIST = new ArrayList<>();

    private static String BUILD_NEW_CONNECT_TOKEN = null;

    private static List<ConfigEntity> FUTURE_CONFIG_LIST;

    @Autowired
    private ConfigService configService;

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void run(String... args) {
        initConnect();
    }

    /**
     * 根据通道配置，初始化每个通道的bean对象
     */
    @SneakyThrows
    public void initConnect() {
        //TODO 根据通道配置，初始化每个通道的bean对象
        //1、查询数据库获得通道列表
        List<ConfigEntity> configEntitiesList = configService.listForConnect();
        List constructorList=new ArrayList();
        //2、遍历通道列表，通过反射创建每个通道的Bean对象（例如AliyunSmsService、MengWangSmsService等）
        configEntitiesList.forEach(configEntity->{
            try {
                SmsConfig config=new SmsConfig();
                config.setId(configEntity.getId());
                config.setDomain(configEntity.getDomain());
                config.setName(configEntity.getName());
                config.setPlatform(configEntity.getPlatform().trim());
                config.setAccessKeyId(configEntity.getAccessKeyId().trim());
                config.setAccessKeySecret(configEntity.getAccessKeySecret().trim());
                if(StringUtils.isNotBlank(configEntity.getOther())){
                    LinkedHashMap linkedHashMap = JSON.parseObject(configEntity.getOther(), LinkedHashMap.class);
                    config.setOtherConfig(linkedHashMap);
                }

                //反射 创建Service
                //全限定名 com.ydl.sms.sms.AliyunSmsService
                String className="com.ydl.sms.sms."+config.getPlatform()+"SmsService";
                System.out.println(className);

                Class<?> aClass = Class.forName(className); //加载类
                Constructor<?> constructor = aClass.getConstructor(SmsConfig.class);//拿到具体的构造方法
                Object obj = constructor.newInstance(config); //创建对象

                //从容器中获取签名和模板的service
                SignatureService signatureService = SpringUtils.getBean(SignatureService.class);
                TemplateService templateService = SpringUtils.getBean(TemplateService.class);
                //找到这两个service在父类中的属性
                Field signatureServiceField = aClass.getSuperclass().getDeclaredField("signatureService");
                Field templateServiceField = aClass.getSuperclass().getDeclaredField("templateService");
                //打开访问权限
                signatureServiceField.setAccessible(true);
                templateServiceField.setAccessible(true);
                //设置属性值了
                signatureServiceField.set(obj,signatureService);
                templateServiceField.set(obj,templateService);

                constructorList.add(obj);

                log.info("初始化通道成功：{}，{}",config.getName(),config.getPlatform());
            }catch (Exception e){
                log.warn("初始化通道异常：{}",e.getMessage());
            }

        });
        //3、将每个通道的Bean对象保存到CONNECT_LIST集合中
        if(!CONNECT_LIST.isEmpty()){
            CONNECT_LIST.clear();
        }
        CONNECT_LIST.addAll(constructorList);

        //解锁逻辑
        if(StringUtils.isNotBlank(BUILD_NEW_CONNECT_TOKEN)){
            redisLock.unlock("buildNewConnect", BUILD_NEW_CONNECT_TOKEN);
        }

        log.info("通道初始化完成了。{}",CONNECT_LIST);
    }

    public <T> T getConnectByLevel(Integer level) {
        return (T) CONNECT_LIST.get(level - 1);
    }

    public boolean checkConnectLevel(Integer level) {
        return CONNECT_LIST.size() <= level;
    }

    /**
     * 通道调整：
     * 通道初始化：构建新的通道配置
     * 只能有一台机器执行，所以需要加锁
     */
    public void buildNewConnect() {
        // 一小时内有效
        String token = redisLock.tryLock("buildNewConnect", 1000 * 60 * 60 * 1);
        log.info("buildNewConnect token:{}", token);
        if (StringUtils.isNotBlank(token)) {
            List<ConfigEntity> list = configService.listForNewConnect();
            FUTURE_CONFIG_LIST = list;
            redisTemplate.opsForValue().set("NEW_CONNECT_SERVER", ServerRegister.SERVER_ID);
            BUILD_NEW_CONNECT_TOKEN = token;
        }
        // 获取不到锁 证明已经有服务在计算或者计算结果未得到使用
    }

    /**
     * 通道调整：
     * 发布订阅消息，通知其他服务：应用新的通道
     */
    public void changeNewConnectMessage() {
        redisTemplate.convertAndSend("TOPIC_HIGH_SERVER", ServerTopic.builder().option(ServerTopic.USE_NEW_CONNECT).value(ServerRegister.SERVER_ID).build().toString());
    }

    /**
     * 通道调整
     * 发布订阅消息，通知其他服务：初始化新通道
     */
    public void changeNewConnect() {
        // 初始化通道
        Object newConnectServer = redisTemplate.opsForValue().get("NEW_CONNECT_SERVER");

        /**
         * 为了通道调整发布的消息中，带有server id
         * 确保只有此server id的服务执行当前代码
         */
        if (null != newConnectServer && ServerRegister.SERVER_ID.equals(newConnectServer) &&
                !CollectionUtils.isEmpty(FUTURE_CONFIG_LIST)) {
            // 配置列表不为空则执行数据库操作 并清空缓存
            boolean result = configService.updateBatchById(FUTURE_CONFIG_LIST);
            log.info("批量修改配置级别:{}", result);
            FUTURE_CONFIG_LIST.clear();
            redisTemplate.convertAndSend("TOPIC_HIGH_SERVER", ServerTopic.builder().option(ServerTopic.INIT_CONNECT).value(ServerRegister.SERVER_ID).build().toString());
        }
    }
}
