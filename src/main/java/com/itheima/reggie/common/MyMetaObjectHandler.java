package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 元对象句柄类，可以通过对实体类字段添加@TableFile注解来进行一个实体类公共字段的
 * 自定义填充数据，降低开发难度
 */
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {

    BaseContext baseContext;

    @Autowired
    public MyMetaObjectHandler(BaseContext baseContext) {
        this.baseContext = baseContext;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段填充[insert]...");
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());

        metaObject.setValue("createUser", baseContext.getCurrentId());
        metaObject.setValue("updateUser", baseContext.getCurrentId());
        log.info(metaObject.toString());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段填充[update]...");
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", baseContext.getCurrentId());
        log.info(metaObject.toString());
    }
}
