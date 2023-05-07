package com.ydl.sms.aspect;

import com.ydl.context.BaseContextHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 通过切面方式，自定义注解，实现实体基础数据的注入（创建者、创建时间、修改者、修改时间）
 */
@Component //交给spring
@Aspect
@Slf4j
public class DefaultParamsAspect {


  @SneakyThrows
  @Before("@annotation(com.ydl.sms.annotation.DefaultParams)")
  public void beforeEvent(JoinPoint point) {

    // TODO 自动注入基础属性（创建者、创建时间、修改者、修改时间）

    //threadlocal  中获取userId
    Long userId = BaseContextHandler.getUserId();

    // 先判断当前的请求是否有带id,用id来判断是save请求还是update请求
    // 拿到参数entity  method.save(entity)
    Object[] args = point.getArgs();

    for (Object arg : args) {
      Class<?> aClass = arg.getClass();
      Object id = null;
      Method method = getMethod(aClass, "getId");
      if ( method != null) {
        id = method.invoke(arg);
      }


      if (id == null) {
        // id为空是save请求，添加创建者、创建时间
        Method setCreateUser = aClass.getMethod("setCreateUser", String.class);
        setCreateUser.invoke(arg, userId.toString()); // 还没做登入功能，先用id来代替
        Method setCreateTime = aClass.getMethod("setCreateTime", LocalDateTime.class);
        setCreateTime.invoke(arg, LocalDateTime.now());
      } else {
        // id为空是update请求，添加修改者、修改时间
        Method setUpdateUser = aClass.getMethod("setUpdateUser", String.class);
        setUpdateUser.invoke(arg, userId.toString()); // 还没做登入功能，先用id来代替
        Method setUpdateTime = aClass.getMethod("setUpdateTime", LocalDateTime.class);
        setUpdateTime.invoke(arg, LocalDateTime.now());
      }

    }

  }

  /**
   * classes.getMethod（）的封装,获得方法对象
   *
   * @param classes
   * @param name    方法名
   * @param types   参数类型
   * @return
   */
  private Method getMethod(Class classes, String name, Class... types) {
    try {
      return classes.getMethod(name, types);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }
}
