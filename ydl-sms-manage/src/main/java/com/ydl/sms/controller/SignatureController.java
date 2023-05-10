package com.ydl.sms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ydl.base.BaseController;
import com.ydl.base.R;
import com.ydl.database.mybatis.conditions.Wraps;
import com.ydl.database.mybatis.conditions.query.LbqWrapper;
import com.ydl.sms.annotation.DefaultParams;
import com.ydl.sms.dto.SignatureDTO;
import com.ydl.sms.entity.SignatureEntity;
import com.ydl.sms.service.SignatureService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短信服务-签名管理
 */
@RestController
@RequestMapping("signature")
@Api(tags = "签名表")
public class SignatureController extends BaseController {

  @Autowired
  SignatureService signatureService;

  // 分页
  @ApiOperation("分页")
  @PostMapping("page")
  public R<Page<SignatureEntity>> page(@RequestBody SignatureDTO signatureDTO) {
    Page<SignatureEntity> page = getPage();
    LbqWrapper<SignatureEntity> wrapper = Wraps.lbQ();
    wrapper.like(SignatureEntity::getName, signatureDTO.getName())
            .like(SignatureEntity::getCode, signatureDTO.getCode())
            .like(SignatureEntity::getContent, signatureDTO.getContent())
            .orderByDesc(SignatureEntity::getCreateTime);
    signatureService.page(page, wrapper);

    return R.success(page);
  }

  // 获得所有数据
//  @GetMapping("list")
//  @ApiOperation("获得所有数据")
//  public R list() {
//    signatureService.list();
//    return R.success();
//  }

  // 通过id查询数据
//  @GetMapping("{id}")
//  @ApiOperation("获得数据")
//  public R<SignatureEntity> get(@PathVariable("id") String id) {
//
//    SignatureEntity entity = signatureService.getById(id);
//    return R.success(entity);
//
//  }


  // 添加签名
  @ApiOperation("添加签名")
  @PostMapping()
  @DefaultParams  // 用AOP统一添加 创建者，创建时间
  public R addSignature(@RequestBody SignatureEntity entity) {
    // 判断签名是否存在
    String name = entity.getName();
    if (signatureService.getByName(name) != null) {
      return R.fail("签名已存在");
    }

    // 自动生成code签名编码
    String code = signatureService.getCode();
    entity.setCode(code);
    entity.setContent(entity.getName());

    signatureService.save(entity);
    return R.success();
  }

  // 修改签名
  @ApiOperation("修改签名")
  @PutMapping
  @DefaultParams  // 用AOP统一添加 修改者，修改时间
  public R editSignature(@RequestBody SignatureDTO signatureDTO) {
    // 判断名称是否已经存在
    String name = signatureDTO.getName();
    SignatureEntity entity = signatureService.getByName(name);
    if (entity != null && name.equals(entity.getName())) {
      return R.fail("名称已存在");
    }

    // 更新操作
    signatureService.updateById(signatureDTO);

    return R.success("修改成功");
  }

  // 删除签名
  @ApiOperation("删除签名")
  @DeleteMapping
  public R deleteSignature(@RequestBody List<String> ids) {
    // 判断是否被使用，查询数据库
     signatureService.removeByIds(ids);

     // 返回查出的数组
    return R.success("删除成功");
  }

}
