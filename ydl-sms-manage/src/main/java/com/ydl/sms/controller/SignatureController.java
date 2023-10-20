package com.ydl.sms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ydl.base.BaseController;
import com.ydl.base.R;
import com.ydl.database.mybatis.conditions.Wraps;
import com.ydl.database.mybatis.conditions.query.LbqWrapper;
import com.ydl.sms.annotation.DefaultParams;
import com.ydl.sms.dto.SignatureDTO;
import com.ydl.sms.entity.ReceiveLogEntity;
import com.ydl.sms.entity.SignatureEntity;
import com.ydl.sms.service.ReceiveLogService;
import com.ydl.sms.service.SignatureService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短信服务-签名管理
 */
@RestController
@RequestMapping("signature")
@Api(tags = "签名管理")
public class SignatureController extends BaseController {

  @Autowired
  private ReceiveLogService receiveLogService;

  @Autowired
  SignatureService signatureService;


  // 分页
  @GetMapping("page")
  @ApiOperation("分页")
  @ApiImplicitParams({
          @ApiImplicitParam(name = "current", value = "当前页码，从1开始", paramType = "query", required = true, dataType = "int"),
          @ApiImplicitParam(name = "size", value = "每页显示记录数", paramType = "query", required = true, dataType = "int"),
          @ApiImplicitParam(name = "排序字段", value = "排序字段", paramType = "query", dataType = "String"),
          @ApiImplicitParam(name = "排序方式", value = "排序方式，可选值(asc、desc)", paramType = "query", dataType = "String"),
          @ApiImplicitParam(name = "startCreateTime", value = "开始时间（yyyy-MM-dd HH:mm:ss）", paramType = "query", dataType = "String"),
          @ApiImplicitParam(name = "endCreateTime", value = "结束时间（yyyy-MM-dd HH:mm:ss）", paramType = "query", dataType = "String")
  })
  public R<Page<SignatureEntity>> page(SignatureDTO signatureDTO) {
    Page<SignatureEntity> page = getPage();
    LbqWrapper<SignatureEntity> wrapper = Wraps.lbQ();
    wrapper.like(SignatureEntity::getName, signatureDTO.getName())
            .like(SignatureEntity::getCode, signatureDTO.getCode())
            .like(SignatureEntity::getContent, signatureDTO.getContent())
            .orderByDesc(SignatureEntity::getCreateTime);
    signatureService.page(page, wrapper);

    return R.success(page);
  }

  // 通道管理中的关联签名页面
  @GetMapping("customPage")
  @ApiOperation("关联签名页面")
  @ApiImplicitParams({
          @ApiImplicitParam(name = "current", value = "当前页码，从1开始", paramType = "query", required = true, dataType = "int"),
          @ApiImplicitParam(name = "size", value = "每页显示记录数", paramType = "query", required = true, dataType = "int"),
          @ApiImplicitParam(name = "排序字段", value = "排序字段", paramType = "query", dataType = "String"),
          @ApiImplicitParam(name = "排序方式", value = "排序方式，可选值(asc、desc)", paramType = "query", dataType = "String"),
          @ApiImplicitParam(name = "startCreateTime", value = "开始时间（yyyy-MM-dd HH:mm:ss）", paramType = "query", dataType = "String"),
          @ApiImplicitParam(name = "endCreateTime", value = "结束时间（yyyy-MM-dd HH:mm:ss）", paramType = "query", dataType = "String")
  })
  public R customPage(SignatureDTO signatureDTO) {
    Page<SignatureDTO> page = getPage();
    Map<String, String> params = new HashMap<String, String>();
    params.put("name", signatureDTO.getName());
    params.put("code", signatureDTO.getCode());
    params.put("configId", signatureDTO.getConfigId());
    // 连表查
    signatureService.customPage(page, params);

    return R.success(page);
  }


  @GetMapping("{id}")
  @ApiOperation("通过id获取数据")
  public R<SignatureEntity> id(@PathVariable("id") String id) {
    SignatureEntity entity = signatureService.getById(id);
    return R.success(entity);
  }

  // 添加签名
  @ApiOperation("添加签名")
  @PostMapping()
  @DefaultParams  // 用AOP统一添加 创建者，创建时间
  public R<String> addSignature(@RequestBody SignatureEntity entity) {
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

    return R.success("添加成功");
  }

  // 修改签名
  @ApiOperation("修改签名")
  @PutMapping
  @DefaultParams  // 用AOP统一添加 修改者，修改时间
  public R<String> editSignature(@RequestBody SignatureDTO signatureDTO) {
    // 更新操作
    signatureService.updateById(signatureDTO);

    return R.success("修改成功");
  }

  // 删除签名
  @ApiOperation("删除签名")
  @DeleteMapping
  public R deleteSignature(@RequestBody List<String> ids) {
    // 判断是否被使用，查询发送日志表
    List<String> codes = new ArrayList<>();
    List<String> nids = new ArrayList<>();

    for (String id : ids) {
      SignatureEntity signature = signatureService.getById(id);
      if (signature == null) {
        continue;
      }

      LambdaQueryWrapper<ReceiveLogEntity> wrapper = new LambdaQueryWrapper<>();
      wrapper.eq(ReceiveLogEntity::getSignature, signature.getCode());
      List<ReceiveLogEntity> logs = receiveLogService.list(wrapper);
      if (logs.size() > 0) {
        // 已使用过无法删除
        codes.add(signature.getCode());
      } else {
        nids.add(id);
      }
    }

    if (nids.size() > 0) {
      signatureService.removeByIds(nids);
    }

    // 返回查出的数组
    return R.success(codes);
  }

  @GetMapping("list")
  @ApiOperation("全部")
  @ApiImplicitParams({
          @ApiImplicitParam(name = "排序字段", value = "排序字段", paramType = "query", dataType = "String"),
          @ApiImplicitParam(name = "排序方式", value = "排序方式，可选值(asc、desc)", paramType = "query", dataType = "String")
  })
  public R<List<SignatureEntity>> list(SignatureDTO signatureDTO) {
    LbqWrapper<SignatureEntity> wrapper = Wraps.lbQ();

    wrapper.like(SignatureEntity::getName, signatureDTO.getName())
            .like(SignatureEntity::getCode, signatureDTO.getCode())
            .like(SignatureEntity::getContent, signatureDTO.getContent())
            .orderByDesc(SignatureEntity::getCreateTime);

    List<SignatureEntity> list = signatureService.list(wrapper);
    return R.success(list);
  }


}
