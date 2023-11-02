package com.ydl.sms.controller;

import com.ydl.base.R;
import com.ydl.sms.dto.UserDTO;
import com.ydl.sms.entity.SysUser;
import com.ydl.sms.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user")
public class UserController {

  @Autowired
  private SysUserService sysUserService;

  @PostMapping("login")
  public R login(@RequestBody SysUser sysUser) {

    return sysUserService.login(sysUser);
  }

  @GetMapping("logout")
  public R logout() {
    return sysUserService.logout();
  }

  @PostMapping("updatepwd")
  public R updatePwd(@RequestBody UserDTO userDTO){

    return sysUserService.updatePwd(userDTO);
  }

}
