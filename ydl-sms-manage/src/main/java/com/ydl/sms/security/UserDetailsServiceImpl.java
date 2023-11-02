package com.ydl.sms.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ydl.sms.entity.SysUser;
import com.ydl.sms.service.SysPermService;
import com.ydl.sms.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 创建UserDetailsService实现类，重写其中的方法。用户名从数据库中查询用户信息。
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired
  private SysUserService sysUserService;

  @Autowired
  private SysPermService permService;

  @Override
  public UserDetails loadUserByUsername(String username){

    LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SysUser::getUserName,username);
    SysUser sysUser = sysUserService.getOne(wrapper);
    if (sysUser == null){
      throw new UsernameNotFoundException("用户名或密码错误");
    }

    // todo 添加权限
    List<String> permKey = permService.getPermKeyByUserId(sysUser.getId());

    return new LoginUser(sysUser,permKey);
  }
}
