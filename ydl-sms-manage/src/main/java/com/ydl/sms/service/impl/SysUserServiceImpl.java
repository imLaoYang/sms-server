package com.ydl.sms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ydl.base.R;
import com.ydl.sms.entity.SysUser;
import com.ydl.sms.mapper.SysUserMapper;
import com.ydl.sms.security.LoginUser;
import com.ydl.sms.service.SysUserService;
import com.ydl.sms.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Yang
 * @description 针对表【sys_user(用户表)】的数据库操作Service实现
 * @createDate 2023-10-30 09:33:03
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {
  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private BCryptPasswordEncoder bCryptPasswordEncoder;
  @Autowired
  private RedisTemplate redisTemplate;

  @Override
  public R login(SysUser sysUser) {
    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(sysUser.getUserName(), sysUser.getPassword());
    Authentication authenticate = authenticationManager.authenticate(authenticationToken);

    //校验失败了
    if (Objects.isNull(authenticate)) {
      throw new RuntimeException("用户名或密码错误！");
    }

    //4自己生成jwt给前端
    LoginUser loginUser = (LoginUser) (authenticate.getPrincipal());
    String userId = loginUser.getSysUser().getId().toString();
    String jwt = JwtUtil.createJWT(loginUser.getSysUser(), 60);
    Map<String, String> map = new HashMap();
    map.put("token", jwt);
    map.put("userName", loginUser.getSysUser().getUserName());
    // 存入redis
    redisTemplate.opsForValue().set("user:" + userId, loginUser);

    return R.success(map, "登录成功");
  }

  @Override
  public R logout() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    LoginUser loginUser = (LoginUser) authentication.getPrincipal();
    Long userId = loginUser.getSysUser().getId();
    redisTemplate.delete("user:" + userId);
    return R.success("退出成功");
  }

  /**
   * 修改密码
   *
   * @param sysUser
   * @param newPwd
   * @return
   */
  @Override
  public R updatePwd(SysUser sysUser, String newPwd) {

    SysUser user = getSysUserByUserName(sysUser.getUserName());
    String encodePwd = sysUser.getPassword();
    String rawPwd = user.getPassword();
    boolean matches = bCryptPasswordEncoder.matches(rawPwd, encodePwd);
    if (!matches) {
      return R.fail("原密码不正确");
    }
    // 改密码
    String pwd = bCryptPasswordEncoder.encode(newPwd);
    sysUser.setPassword(pwd);
    baseMapper.updateById(sysUser);

    return R.success("修改密码成功");
  }



  private SysUser getSysUserByUserName(String userName) {
    LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SysUser::getUserName, userName);
    return baseMapper.selectOne(wrapper);
  }
}




