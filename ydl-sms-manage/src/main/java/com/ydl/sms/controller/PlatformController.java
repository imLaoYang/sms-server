package com.ydl.sms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ydl.base.BaseController;
import com.ydl.base.R;
import com.ydl.database.mybatis.conditions.query.LbqWrapper;
import com.ydl.sms.annotation.DefaultParams;
import com.ydl.sms.dto.PlatformDTO;
import com.ydl.sms.entity.PlatformEntity;
import com.ydl.sms.entity.base.BaseEntity;
import com.ydl.sms.service.PlatformService;
import com.ydl.sms.service.ReceiveLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 短信服务-应用管理
 */
@RestController
@RequestMapping("platform")
@Api(tags = "短信服务-应用管理")
public class PlatformController extends BaseController {

  @Autowired
  private PlatformService platformService;

  @Autowired
  private ReceiveLogService receiveLogService;

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
  public R<Page<PlatformEntity>> getPlatformPage(PlatformDTO platformDTO) {
    Page<PlatformEntity> page = getPage();
    LbqWrapper<PlatformEntity> wrapper = new LbqWrapper<>();
    wrapper.like(PlatformEntity::getName, platformDTO.getName())
            .orderByDesc(BaseEntity::getCreateTime);
    platformService.page(page, wrapper);

    return success(page);
  }

  // 详情信息
  @GetMapping
  @ApiOperation("详细页")
  public R getPlatform() {
    List<PlatformEntity> list = platformService.list();

    return R.success(list);
  }

  // 插入
  @PostMapping
  @ApiOperation("插入")
  @DeleteMapping //aop
  public R addPlatform(@RequestBody PlatformDTO platformDTO) {
    // 判断名称是否存在
    PlatformEntity entity = platformService.getByName(platformDTO.getName());
    if (entity != null) {
      return R.fail("名称已存在");
    }
    if (StringUtils.isBlank(platformDTO.getAccessKeyId())) {
      platformDTO.setAccessKeyId(UUID.randomUUID().toString().replace("-", ""));
    }
    if (StringUtils.isBlank(platformDTO.getAccessKeySecret())) {
      platformDTO.setAccessKeySecret(UUID.randomUUID().toString().replace("-", ""));
    }
    platformService.save(platformDTO);

    return R.success("添加成功");
  }

  /**
   * 删除
   * @param ids
   * @return
   */
  @DeleteMapping
  @ApiOperation("删除")
  public R deletePlatform(@RequestBody List<String> ids) {
    platformService.removeByIds(ids);
    return R.success("删除成功");
  }

  // 修改
  @PutMapping
  @ApiOperation("修改")
  @DefaultParams //aop
  public R editPlatform(@RequestBody PlatformDTO platformDTO) {

    platformService.updateById(platformDTO);

    return R.success("修改成功");
  }


}
