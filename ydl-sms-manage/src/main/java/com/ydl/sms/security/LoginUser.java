package com.ydl.sms.security;

import com.ydl.sms.entity.SysUser;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
public class LoginUser implements UserDetails {

  private SysUser sysUser;

  List<String> permission;


  public LoginUser(SysUser sysUser) {
    this.sysUser = sysUser;
  }

  public LoginUser(SysUser sysUser, List<String> permKeys) {
    this.sysUser = sysUser;
    this.permission = permKeys;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
    permission.forEach(perm -> {
      authorities.add(new SimpleGrantedAuthority(perm));
    });
    return authorities;
  }

  @Override
  public String getPassword() {
    return sysUser.getPassword();
  }

  @Override
  public String getUsername() {
    return sysUser.getUserName();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
