package com.ydl.sms.sms.service.impl;

import com.alibaba.fastjson.JSON;
import com.ydl.sms.sms.dto.BaseParamsDTO;
import com.ydl.sms.sms.dto.R;
import com.ydl.sms.sms.dto.SmsBatchParamsDTO;
import com.ydl.sms.sms.dto.SmsParamsDTO;
import com.ydl.sms.sms.service.SmsSendService;
import com.ydl.sms.sms.utils.SmsEncryptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmsSendServiceImpl implements SmsSendService {

  // 网站是否认证
  @Value("${ydlclass.sms.auth}")
  private boolean auth;
  // 网站域名
  @Value("${ydlclass.sms.domain}")
  private String domain;
  @Value("${ydlclass.sms.accessKeyId}")
  private String accessKeyId;
  @Value("${ydlclass.sms.accessKeySecret}")
  private String accessKeySecret;
  private String send = "/sms/send";
  private String batchSend = "/sms/batchSend";


  /**
   * 单条短信发送
   *
   * @param smsParamsDTO
   * @return
   */
  @Override
  public R sendSms(SmsParamsDTO smsParamsDTO) {
    String url = domain + send;

    return send(smsParamsDTO, url);
  }

  /**
   * 批量发送
   *
   * @param smsBatchParamsDTO
   * @return
   */
  @Override
  public R batchSendSms(SmsBatchParamsDTO smsBatchParamsDTO) {
    String url = domain + batchSend;

    return send(smsBatchParamsDTO, url);
  }

  /**
   * 发送http请求
   *
   * @param baseParamsDTO
   * @param url           请求url
   * @return 返回请求的响应
   */
  private R send(BaseParamsDTO baseParamsDTO, String url) {
    // 设置平台秘钥
    baseParamsDTO.setAccessKeyId(accessKeyId);

    // 认证
    if (auth) {
      if (StringUtils.isBlank(accessKeyId) || StringUtils.isBlank(accessKeySecret)) {

        return R.fail("accessKeyId和accessKeySecret不能为空");
      }
    } else {

      return R.fail("平台未认证");
    }

    if (StringUtils.isBlank(domain)) {

      return R.fail("domain不能为空");
    }

    baseParamsDTO.setTimestamp(System.currentTimeMillis() + "");
    // 加密编码
    String encode = SmsEncryptionUtils.encode(baseParamsDTO.getTimestamp(), accessKeyId, accessKeySecret);
    baseParamsDTO.setEncryption(encode);


    // 创建客户端
    CloseableHttpClient httpClient = HttpClients.createDefault();
    // 构建请求
    HttpPost post = new HttpPost(url);
    // 设置请求头
    post.setHeader("Content-Type", "application/json; charset=UTF-8");
    // 设置请求体
    StringEntity stringEntity = new StringEntity(JSON.toJSONString(baseParamsDTO), "UTF-8");
    post.setEntity(stringEntity);


    // 发送post请求
    CloseableHttpResponse response = null;
    try {
      response = httpClient.execute(post);
      // 拿到响应体
      HttpEntity responseEntity = response.getEntity();
      // 拿到响应状态码
      int statusCode = response.getStatusLine().getStatusCode();

      // 解析响应体
      if (statusCode == 200) {
        // 成功
        log.info("httpRequest access success, StatusCode is:{}", statusCode);
        String responseEntityStr = EntityUtils.toString(responseEntity);
        log.info("responseContent is : {}", responseEntityStr);

        return JSON.parseObject(responseEntityStr, R.class);

      } else {
        // 失败
        log.error("httpRequest access fail ,StatusCode is:{}", statusCode);

        return R.fail("status is ", statusCode);
      }

    } catch (Exception e) {
      log.error("error", e);

      return R.fail(e.getMessage());
    } finally {
      post.releaseConnection();
    }


  }


}

