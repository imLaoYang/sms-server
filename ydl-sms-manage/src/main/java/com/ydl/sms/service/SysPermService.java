package com.ydl.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ydl.sms.entity.SysPerm;

import java.util.List;

/**
 * @author Yang
 * @description 针对表【sys_perm(权限表)】的数据库操作Service
 * @createDate 2023-10-31 10:38:10
 */
public interface SysPermService extends IService<SysPerm> {
  List<String> getPermKeyByUserId(Long userId);

}
