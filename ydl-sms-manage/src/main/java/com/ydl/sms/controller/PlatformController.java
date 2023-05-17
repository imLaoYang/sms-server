package com.ydl.sms.controller;


import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ydl.base.BaseController;
import com.ydl.base.R;
import com.ydl.sms.dto.PlatformDTO;
import com.ydl.sms.entity.PlatformEntity;
import com.ydl.sms.entity.base.BaseEntity;
import com.ydl.sms.service.PlatformService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短信服务-应用管理
 */
@RestController
@RequestMapping("platform")
@Api(tags = "短信服务-应用管理")
public class PlatformController extends BaseController {

  @Autowired
  PlatformService platformService;

  // 分页
  @GetMapping("page")
  @ApiOperation("分页")
  public R<Page<PlatformEntity>> getPlatformPage(PlatformDTO platformDTO){
    Page<PlatformEntity> page = getPage();
    LambdaUpdateWrapper<PlatformEntity> wrapper = new LambdaUpdateWrapper<>();
    wrapper.like(PlatformEntity::getName,platformDTO.getName())
            .orderByDesc(BaseEntity::getCreateTime);
    platformService.page(page,wrapper);
    return  R.success(page);
  }

  // 详情信息
  @GetMapping
  @ApiOperation("详细页")
  public R getPlatform(){
    return R.success();
  }

  // 插入
  @PostMapping
  @ApiOperation("插入")
  public R addPlatform(@RequestBody PlatformDTO platformDTO){
    // 判断名称是否存在
    PlatformEntity entity = platformService.getByName(platformDTO.getName());
    if (entity != null){
      return R.fail("名称已存在");
    }
    platformService.save(platformDTO);
    return R.success("添加成功");
  }

  // 删除
  @DeleteMapping
  @ApiOperation("删除")
  public R deletePlatform(List<String> ids){
    platformService.removeByIds(ids);
    return R.success("删除成功");

  }

  // 修改
  @PutMapping
  @ApiOperation("修改")
  public R editPlatform(){

    return R.success();

  }




}
