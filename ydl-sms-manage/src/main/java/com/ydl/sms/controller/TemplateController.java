package com.ydl.sms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ydl.base.BaseController;
import com.ydl.base.R;
import com.ydl.database.mybatis.conditions.Wraps;
import com.ydl.database.mybatis.conditions.query.LbqWrapper;
import com.ydl.sms.annotation.DefaultParams;
import com.ydl.sms.dto.TemplateDTO;
import com.ydl.sms.entity.ReceiveLogEntity;
import com.ydl.sms.entity.TemplateEntity;
import com.ydl.sms.service.ReceiveLogService;
import com.ydl.sms.service.TemplateService;
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
 * 短信服务-模板管理
 */
@RestController
@RequestMapping("template")
@Api(tags = "模板管理")
public class TemplateController extends BaseController {

  @Autowired
  TemplateService templateService;

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
  public R<Page<TemplateEntity>> getTemplate(TemplateDTO templateDTO) {
    // 获取前端页面数据
    Page<TemplateEntity> page = getPage();
    LbqWrapper<TemplateEntity> wrapper = Wraps.lbQ();
    wrapper.like(TemplateEntity::getName,templateDTO.getName())
            .like(TemplateEntity::getId,templateDTO.getId())
            .orderByDesc(TemplateEntity::getCreateTime);
     templateService.page(page, wrapper);

    return R.success(page);
  }

  // 通道管理中的关联模板页面
  @GetMapping("customPage")
  @ApiOperation("关联模板页面")
  @ApiImplicitParams({
          @ApiImplicitParam(name = "current", value = "当前页码，从1开始", paramType = "query", required = true, dataType = "int"),
          @ApiImplicitParam(name = "size", value = "每页显示记录数", paramType = "query", required = true, dataType = "int"),
          @ApiImplicitParam(name = "排序字段", value = "排序字段", paramType = "query", dataType = "String"),
          @ApiImplicitParam(name = "排序方式", value = "排序方式，可选值(asc、desc)", paramType = "query", dataType = "String"),
          @ApiImplicitParam(name = "startCreateTime", value = "开始时间（yyyy-MM-dd HH:mm:ss）", paramType = "query", dataType = "String"),
          @ApiImplicitParam(name = "endCreateTime", value = "结束时间（yyyy-MM-dd HH:mm:ss）", paramType = "query", dataType = "String")
  })
  public R customPage(TemplateDTO templateDTO){
    Page<TemplateDTO> page = getPage();
    Map<String,String> params = new HashMap<String,String>();
    // 连表查
    templateService.customPage(page,params);

    return R.success(page);
  }

  //
  @GetMapping("{id}")
  @ApiOperation("通过id获取数据")
  public R<TemplateEntity> id(@PathVariable("id")String id){
    TemplateEntity entity = templateService.getById(id);
    return R.success(entity);
  }


  // 添加模板
  @PostMapping
  @ApiOperation("添加模板")
  @DefaultParams  // 用AOP统一添加 修改者，修改时间
  public R<String> addTemplate(@RequestBody TemplateDTO templateDTO) {

    if (templateService.getByName(templateDTO.getName()) != null) {
      return R.fail("模板名称重复");
    }
    String code = templateService.getCode();
    templateDTO.setCode(code);
    templateService.save(templateDTO);

    return R.success("添加成功");

  }

  // 修改模板
  @PutMapping
  @ApiOperation("修改模板")
  @DefaultParams  // 用AOP统一添加 修改者，修改时间
  public R<String> editTemplate(@RequestBody TemplateDTO templateDTO) {

    templateService.updateById(templateDTO);

    return R.success("修改成功");
  }

  // 删除模板
  @DeleteMapping
  @ApiOperation("删除模板")
  public R deleteTemplate(@RequestBody List<String> ids) {
    // 判断是否被使用，查询发送日志表
    List<String> codes = new ArrayList<>();
    List<String> nids = new ArrayList<>();
    for (String id : ids) {
      TemplateEntity template = templateService.getById(id);
      if (template == null) {
        continue;
      }
      LambdaQueryWrapper<ReceiveLogEntity> wrapper = new LambdaQueryWrapper<>();
      wrapper.eq(ReceiveLogEntity::getTemplate, template.getCode());
      List<ReceiveLogEntity> logs = receiveLogService.list(wrapper);
      if (logs.size() > 0) {
        // 已使用过无法删除
        codes.add(template.getCode());
      } else {
        nids.add(id);
      }
    }
    // 删除
    if (nids.size() > 0) {
      templateService.removeByIds(nids);
    }

    return R.success(codes);
  }

  @GetMapping("list")
  @ApiOperation("全部")
  @ApiImplicitParams({
          @ApiImplicitParam(name = "排序字段", value = "排序字段", paramType = "query", dataType = "String"),
          @ApiImplicitParam(name = "排序方式", value = "排序方式，可选值(asc、desc)", paramType = "query", dataType = "String")
  })
  public R<List<TemplateEntity>> list(TemplateDTO templateDTO) {
    LbqWrapper<TemplateEntity> wrapper = Wraps.lbQ();

    wrapper.like(TemplateEntity::getName, templateDTO.getName())
            .like(TemplateEntity::getCode, templateDTO.getCode())
            .like(TemplateEntity::getContent, templateDTO.getContent())
            .orderByDesc(TemplateEntity::getCreateTime);

    List<TemplateEntity> list = templateService.list(wrapper);
    return R.success(list);
  }

}
