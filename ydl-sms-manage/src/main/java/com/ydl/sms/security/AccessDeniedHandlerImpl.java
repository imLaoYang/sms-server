package com.ydl.sms.security;

import com.alibaba.fastjson.JSON;
import com.ydl.base.R;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 权限不足异常处理器
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
    // 权限不足
    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType("application/json");
    response.setCharacterEncoding("utf-8");
    R<Object> r = R.fail(HttpStatus.FORBIDDEN.value(), "对不起,您的权限不足!");
    String json = JSON.toJSONString(r);
    response.getWriter().print(json);
  }
}
