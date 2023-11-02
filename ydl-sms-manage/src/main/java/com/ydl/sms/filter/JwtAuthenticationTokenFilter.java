package com.ydl.sms.filter;

import com.auth0.jwt.interfaces.Claim;
import com.ydl.sms.security.LoginUser;
import com.ydl.sms.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * 判断前端传来的token
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

  @Autowired
  private RedisTemplate redisTemplate;


  @Override
  protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

    String token = httpServletRequest.getHeader("token");
    if (StringUtils.isEmpty(token)) {
      //放行，让后面的过滤器执行
      filterChain.doFilter(httpServletRequest, httpServletResponse);
      return;
    }

    Map<String, Claim> claimMap = JwtUtil.verifyJwt(token);
    Claim claim = claimMap.get("userId");
    Long userId = claim.asLong();

    LoginUser loginUser = (LoginUser) redisTemplate.opsForValue().get("user:" + userId);
    if (Objects.isNull(loginUser)) {
      throw new RuntimeException("未登录");
    }

    //4封装Authentication
    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
            = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());

    //5存入SecurityContextHolder
    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

    //放行，让后面的过滤器执行
    filterChain.doFilter(httpServletRequest, httpServletResponse);

  }
}
