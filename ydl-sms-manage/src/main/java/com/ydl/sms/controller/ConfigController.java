package com.ydl.sms.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ydl.base.BaseController;
import com.ydl.base.R;
import com.ydl.database.mybatis.conditions.Wraps;
import com.ydl.database.mybatis.conditions.query.LbqWrapper;
import com.ydl.sms.annotation.DefaultParams;
import com.ydl.sms.dto.ConfigDTO;
import com.ydl.sms.dto.ConfigUpdateOtherDTO;
import com.ydl.sms.dto.SignatureDTO;
import com.ydl.sms.dto.TemplateDTO;
import com.ydl.sms.entity.ConfigEntity;
import com.ydl.sms.entity.ConfigSignatureEntity;
import com.ydl.sms.entity.ConfigTemplateEntity;
import com.ydl.sms.entity.base.BaseEntity;
import com.ydl.sms.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 短信服务-通道管理
 */
@RestController
@RequestMapping("config")
@Api(tags = "通道管理")
public class ConfigController extends BaseController {

  @Autowired
  ConfigService configService;

  @Autowired
  SignatureService signatureService;

  @Autowired
  TemplateService templateService;

  @Autowired
  ConfigSignatureService configSignatureService;

  @Autowired
  ConfigTemplateService configTemplateService;

  // 分页
  @GetMapping("page")
  @ApiOperation("通道分页")
  public R<Page<ConfigEntity>> getConfigPage(ConfigDTO configDTO) {
    Page<ConfigEntity> page = getPage();
    LbqWrapper<ConfigEntity> wrapper = Wraps.lbQ();
    wrapper.like(ConfigEntity::getName, configDTO.getName())
            .orderByAsc(ConfigEntity::getLevel);
    configService.page(page, wrapper);

    return R.success(page);

  }

  // 添加
  @PostMapping
  @ApiOperation("添加通道")
  @DefaultParams // aop 默认添加创建人
  public R<String> addConfig(@RequestBody ConfigDTO configDTO) {
    // 判断名称是否存在
    ConfigEntity entity = configService.getByName(configDTO.getName());
    if (entity != null) {
      return R.fail("通道名称已存在");
    }
    configService.setNewLevel(configDTO);
    configService.save(configDTO);

    return R.success("添加成功");
  }

  // 修改
  @PutMapping
  @ApiOperation("修改通道")
  @DefaultParams // aop 默认添加修改人
  public R<String> editConfig(@RequestBody ConfigDTO configDTO) {
    // 判断名称是否存在
//    ConfigEntity entity = configService.getByName(configDTO.getName());
//    if (entity != null) {
//      return R.fail("通道名称重复");
//    }
    configService.updateById(configDTO);

    // config_signature表插入数据
    configSignatureService.merge(configDTO);
    // config_template表插入数据
    configTemplateService.merge(configDTO);
    // 通知短信发送服务修改通道优先级
    configService.sendUpdateMessage();

    return R.success("修改成功");
  }

  // 删除
  @DeleteMapping
  @ApiOperation("删除通道")
  public R deleteConfig(@RequestBody List<String> ids) {
    // 判断是否被使用

    configService.removeByIds(ids);

    return R.success();
  }

  // 排序
  @PostMapping("level")
  @ApiOperation("排序")
  public R postConfigLevel(@RequestBody List<String> ids) {

    for (int i = 0; i < ids.size(); i++) {
      LambdaUpdateWrapper<ConfigEntity> wrapper = new LambdaUpdateWrapper<>();
      wrapper.eq(ConfigEntity::getId, ids.get(i))
              .eq(ConfigEntity::getIsActive, 1)
              .set(ConfigEntity::getLevel, (i + 1));
      configService.update(wrapper);
    }
    // 通知Redis优先级变更，通知短信发送服务修改通道优先级
    configService.sendUpdateMessage();

    return R.success();
  }

  // 通过id获取数据
  @GetMapping("{id}")
  @ApiOperation("通过id获取数据")
  public R getById(@PathVariable("id") String id) {

    ConfigEntity entity = configService.getById(id);
    if (entity == null) {
      return R.fail("没有对应配置");
    }
    ConfigDTO configDTO = new ConfigDTO();
    // 把entity的数据复制给configDto
    BeanUtils.copyProperties(entity, configDTO);

    Map<String, String> params = new HashMap<String, String>();
    params.put("configId", id);
    List<SignatureDTO> signatureDtos = signatureService.customList(params);
    //configDTO.setSignatureDTOS(signatureDtos);
    configDTO.setSignatureIds(signatureDtos.stream().map(BaseEntity::getId).collect(Collectors.toList()));

    List<TemplateDTO> templateDtos = templateService.customList(params);
    //configDTO.setTemplateDTOS(templateDtos);
    configDTO.setTemplateIds(templateDtos.stream().map(BaseEntity::getId).collect(Collectors.toList()));

    return R.success(configDTO);
  }

  // 信息详情接口(目前未使用)
  @GetMapping
  @ApiOperation("信息详情接口(目前未使用)")
  public R getDetailsConfig() {
    return R.success();
  }

  // 修改其他配置接口
  @PutMapping("other")
  @ApiOperation("修改其他配置接口")
  public R<String> editConfigOther(@RequestBody ConfigUpdateOtherDTO dto) {
    if (org.apache.commons.lang3.StringUtils.isNotBlank(dto.getSignatureId())) {
      LambdaUpdateWrapper<ConfigSignatureEntity> wrapper = new LambdaUpdateWrapper<>();
      wrapper.eq(ConfigSignatureEntity::getConfigId, dto.getConfigId());
      wrapper.eq(ConfigSignatureEntity::getSignatureId, dto.getSignatureId());
      wrapper.set(ConfigSignatureEntity::getConfigSignatureCode, dto.getConfigSignatureCode());
      configSignatureService.update(wrapper);
    }
    if (org.apache.commons.lang3.StringUtils.isNotBlank(dto.getTemplateId())) {
      LambdaUpdateWrapper<ConfigTemplateEntity> wrapper = new LambdaUpdateWrapper<>();
      wrapper.eq(ConfigTemplateEntity::getConfigId, dto.getConfigId());
      wrapper.eq(ConfigTemplateEntity::getTemplateId, dto.getTemplateId());
      wrapper.set(ConfigTemplateEntity::getConfigTemplateCode, dto.getConfigTemplateCode());
      configTemplateService.update(wrapper);
    }

    return R.success("修改成功");
  }

}
