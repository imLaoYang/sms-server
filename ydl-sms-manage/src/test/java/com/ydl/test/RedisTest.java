package com.ydl.test;

import com.ydl.sms.SmsManageApplication;
import com.ydl.sms.entity.SignatureEntity;
import com.ydl.sms.service.SignatureService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SmsManageApplication.class)
public class RedisTest {

    @Autowired
    SignatureService signatureService;


    //@Test
    public void testSendToRedis(){

        SignatureEntity byName = signatureService.getByName("123");
        System.out.println(byName);

    }

}
