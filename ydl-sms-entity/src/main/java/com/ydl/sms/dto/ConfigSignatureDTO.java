package com.ydl.sms.dto;

import com.ydl.sms.entity.ConfigSignatureEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;



@Data
@ApiModel(description = "配置—签名表")
public class ConfigSignatureDTO extends ConfigSignatureEntity {

}
