package com.ydl.sms.job;

/**
 * 定时短信发送业务接口
 */
public interface SendTimingSms {
    void execute(String timing) throws InterruptedException;
}
