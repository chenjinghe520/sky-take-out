package com.sky.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.sky.context.BaseContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    // 新增数据时自动填充
    @Override
    public void insertFill(MetaObject metaObject) {

        this.strictInsertFill(
                metaObject,
                "createTime",
                LocalDateTime.class,
                LocalDateTime.now()
        );

        this.strictInsertFill(
                metaObject,
                "updateTime",
                LocalDateTime.class,
                LocalDateTime.now()
        );

        this.strictInsertFill(
                metaObject,
                "createUser",
                Long.class,
                BaseContext.getCurrentId()
        );

        this.strictInsertFill(
                metaObject,
                "updateUser",
                Long.class,
                BaseContext.getCurrentId()
        );
    }

    // 修改数据时自动填充
    @Override
    public void updateFill(MetaObject metaObject) {

        this.strictUpdateFill(
                metaObject,
                "updateTime",
                LocalDateTime.class,
                LocalDateTime.now()
        );

        this.strictUpdateFill(
                metaObject,
                "updateUser",
                Long.class,
                BaseContext.getCurrentId()
        );
    }
}