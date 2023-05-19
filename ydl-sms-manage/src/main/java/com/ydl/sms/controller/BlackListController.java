package com.ydl.sms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ydl.base.BaseController;
import com.ydl.base.R;
import com.ydl.database.mybatis.conditions.Wraps;
import com.ydl.database.mybatis.conditions.query.LbqWrapper;
import com.ydl.sms.dto.BlackListDTO;
import com.ydl.sms.entity.BlackListEntity;
import com.ydl.sms.service.BlackListService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static com.ydl.exception.code.ExceptionCode.BASE_VALID_PARAM;

/**
 * 系统管理-黑名单列表
 */
@RestController
@RequestMapping("blackList")
@Api(tags = "系统管理-黑名单列表")
public class BlackListController extends BaseController {

  @Autowired
  private BlackListService blackListService;

  @Autowired
  private RedisTemplate redisTemplate;

  // 列表页获取-分页
  @GetMapping("page")
  @ApiOperation("列表页获取-分页")
  public R getBlackListPage(BlackListDTO blackListDTO) {
    Page<BlackListEntity> page = getPage();
    LbqWrapper<BlackListEntity> wrapper = Wraps.lbQ();
    //构建查询条件
    wrapper.like(BlackListEntity::getContent, blackListDTO.getContent())
            .like(BlackListEntity::getType, blackListDTO.getType())
            .orderByDesc(BlackListEntity::getCreateTime);
    //执行查询
    blackListService.page(page, wrapper);

    return R.success(page);
  }

  @GetMapping("{id}")
  @ApiOperation("信息")
  public R<BlackListEntity> get(@PathVariable("id") String id) {
    BlackListEntity data = blackListService.getById(id);

    return R.success(data);
  }

  // 新增保存
  @PostMapping
  @ApiOperation("新增保存")
  public R addBlackList(@RequestBody BlackListDTO blackListDTO) {
    blackListService.save(blackListDTO);
    redisTemplate.delete("Black_" + 1);

    return R.success("添加成功");

  }

  // 修改
  @PutMapping
  @ApiOperation("修改")
  public R editBlackList(@RequestBody BlackListDTO blackListDTO) {
    blackListService.updateById(blackListDTO);
    redisTemplate.delete("Black_" + 1);

    return R.success();
  }

  // 删除黑名单
  @DeleteMapping
  @ApiOperation("删除黑名单")
  public R deleteBlacklist(@RequestBody List<String> ids) {
    blackListService.removeByIds(ids);
    redisTemplate.delete("Black_" + 1);

    return R.success("删除成功");

  }


  @PostMapping("upload")
  @ApiOperation("导入")
  public R<? extends Object> upload(@RequestParam(value = "file") MultipartFile file) {
    if (file.isEmpty()) {
      return fail(BASE_VALID_PARAM.build("导入内容为空"));
    }
    R<Boolean> res = blackListService.upload(file);
    redisTemplate.delete("Black_" + 1);

    return res;
  }

  @GetMapping("export")
  @ApiOperation("导出")
  public void export(@ApiIgnore @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {

  }


}
