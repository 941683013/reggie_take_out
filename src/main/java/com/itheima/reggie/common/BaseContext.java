package com.itheima.reggie.common;

import org.springframework.stereotype.Component;

/**
 * 基于ThreadLocal的辅助类，用于保存当前线程的用户登录ID
 */
@Component
public class BaseContext {
   private ThreadLocal<Long> threadLocal = new ThreadLocal<>();

   public void setCurrentId(Long id) {
       threadLocal.set(id);
   }

   public Long getCurrentId() {
       return threadLocal.get();
   }
}
