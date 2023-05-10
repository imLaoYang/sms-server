package com.ydl.test;

import com.ydl.sms.entity.SignatureEntity;
import com.ydl.sms.service.SignatureService;
import org.springframework.beans.factory.annotation.Autowired;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = SmsManageApplication.class)
public class RedisTest {

    @Autowired
    SignatureService signatureService;


    //@Test
    public void testSendToRedis(){

        SignatureEntity byName = signatureService.getByName("123");
        System.out.println(byName);

    }

}
