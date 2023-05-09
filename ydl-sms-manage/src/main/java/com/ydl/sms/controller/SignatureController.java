package com.ydl.sms.controller;

import com.ydl.base.R;
import com.ydl.sms.entity.SignatureEntity;
import com.ydl.sms.service.SignatureService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 短信服务-签名管理
 */
@RestController
@RequestMapping("signature")
@Api(tags = "签名表")
public class SignatureController {

  @Autowired
  SignatureService signatureService;

  // 分页
  @ApiOperation("分页")
  @GetMapping("page")
  public R page() {
    return R.success();
  }

  // 添加签名
  @ApiOperation("添加签名")
  @PostMapping()
  public R addSignature(@RequestBody SignatureEntity entity) {

    // 自动生成code签名编码


    // 判断签名是否存在
    String name = entity.getName();
    if (signatureService.getByName(name) != null){
      return  R.fail("签名已存在");
    }

    signatureService.save(entity);
    return R.success();
  }

  // 修改签名
  @ApiOperation("修改签名")
  @PutMapping
  public R editSignature() {
    return R.success();
  }

  // 删除签名
  @ApiOperation("修改签名")
  @DeleteMapping
  public R deleteSignature() {
  return R.success();
  }


}
