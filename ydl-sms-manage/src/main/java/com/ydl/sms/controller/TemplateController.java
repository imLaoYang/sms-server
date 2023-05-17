package com.ydl.sms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ydl.base.BaseController;
import com.ydl.base.R;
import com.ydl.database.mybatis.conditions.Wraps;
import com.ydl.database.mybatis.conditions.query.LbqWrapper;
import com.ydl.sms.annotation.DefaultParams;
import com.ydl.sms.dto.TemplateDTO;
import com.ydl.sms.entity.TemplateEntity;
import com.ydl.sms.service.TemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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


  // 分页
  @GetMapping("page")
  @ApiOperation("分页")
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
    // 判断名称是否已经存在
    String name = templateDTO.getName();
    TemplateEntity entity = templateService.getByName(name);
    if (entity != null && name.equals(entity.getName())) {
      return R.fail("模板名称已存在");
    }
    templateService.updateById(templateDTO);

    return R.success("修改成功");
  }

  // 删除模板
  @DeleteMapping
  @ApiOperation("删除模板")
  public R<String> deleteTemplate(@RequestBody List<String> ids) {
    // 判断是否被使用，查询发送日志表

    templateService.removeByIds(ids);

    return R.success("删除成功");
  }
}
