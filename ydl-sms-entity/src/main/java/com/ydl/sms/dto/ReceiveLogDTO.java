package com.ydl.sms.dto;

import com.ydl.sms.entity.ReceiveLogEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接收日志表
 *
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "接收日志表")
public class ReceiveLogDTO extends ReceiveLogEntity {

}
