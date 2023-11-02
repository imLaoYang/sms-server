package com.ydl.sms.service;

import com.ydl.base.R;
import com.ydl.sms.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Yang
* @description 针对表【sys_user(用户表)】的数据库操作Service
* @createDate 2023-10-30 09:33:03
*/
public interface SysUserService extends IService<SysUser> {

  R login(SysUser sysUser);

  R logout();

  R updatePwd(SysUser sysUser,String newPwd);
}
