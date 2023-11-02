package com.ydl.sms.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.IncorrectClaimException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.ydl.sms.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {
  private static final Base64.Encoder ENCODER = Base64.getEncoder();

  private static  String jwtSecret;


  @Value("${jwt.secret}")
  public  void setJwtSecret( String jwtSecret) {


   JwtUtil.jwtSecret = jwtSecret;

  }

  public static String createJWT(SysUser sysUser, Integer expired) {
    Long currentUnixTimeStamp = System.currentTimeMillis();
    long endTimeUnixTimeStamp = currentUnixTimeStamp + expired * 1000 * 60;

    return JWT.create()
            .withClaim("userId", sysUser.getId())
            .withExpiresAt(new Date(endTimeUnixTimeStamp))
            .sign(getAlgorithm());
  }



  /**
   * 生成加密字符串
   * @return 加密字符串
   */
  public static Algorithm getAlgorithm() {
    byte[] bytes = ENCODER.encode(jwtSecret.getBytes(StandardCharsets.UTF_8));
    return Algorithm.HMAC256(bytes);
  }

  public static Map<String, Claim> verifyJwt(String token) {
    try {
      return JWT
              .require(getAlgorithm())
              .build()
              .verify(token)
              .getClaims();
    } catch (AlgorithmMismatchException e) {
      throw new RuntimeException("加密算法签名不匹配");
    } catch (IncorrectClaimException e) {
      throw new RuntimeException("未经授权的token");
    } catch (TokenExpiredException e) {
      throw new RuntimeException("token 已经过期, 请重新登录");
    }
  }
}