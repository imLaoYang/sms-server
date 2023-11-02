package com.ydl.sms.config;

import com.ydl.sms.filter.JwtAuthenticationTokenFilter;
import com.ydl.sms.security.AccessDeniedHandlerImpl;
import com.ydl.sms.security.AuthenticationEntryPointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;


@EnableWebSecurity(debug = true)
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {


  @Autowired
  private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

  @Autowired
  private AccessDeniedHandlerImpl accessDeniedHandler;

  @Autowired
  private AuthenticationEntryPointImpl authenticationEntryPoint;

  @Autowired
  private UserDetailsService userDetailsService;



  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {
    // 由于使用的是JWT，我们这里不需要csrf
    httpSecurity.csrf()
            .disable()
//            .formLogin().disable()
            // 基于token，所以不需要session
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            // 对登录注册要允许匿名访问
            .antMatchers("/user/login")
            .permitAll()
            .anyRequest()// 除上面外的所有请求全部需要鉴权认证
            .authenticated();

    // 添加JWT filter
    httpSecurity.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
    //添加自定义未授权和未登录结果返回
    httpSecurity.exceptionHandling()
            .accessDeniedHandler(accessDeniedHandler)
            .authenticationEntryPoint(authenticationEntryPoint);
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Override
  @Bean
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  // 跨域配置
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    // 允许跨域访问的 URL
    List<String> allowedOriginsUrl = new ArrayList<>();
    allowedOriginsUrl.add("*");
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    // 设置允许跨域访问的 URL
    config.setAllowedOrigins(allowedOriginsUrl);
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

}
