package com.cloudweb.oa.config;

import cn.js.fan.util.ErrMsgException;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;

/**
 * 填充器
 *
 * @author nieqiurong 2018-08-10 22:59:23.
 */
@Slf4j
public class EJMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        if(metaObject.hasSetter("createDateTime")){
            int uId = SpringUtil.getBean(AuthUtil.class).getUserId();
            if(uId <= 0){
                log.warn("EJMetaObjectHandler insertFill 当前用户id 为"+ uId);
            }
            Date date =  new Date();
            this.setFieldValByName("createDateTime",date,metaObject);
            this.setFieldValByName("createUserId",(long)uId,metaObject);

            this.setFieldValByName("updateDateTime",date,metaObject);
            this.setFieldValByName("updateUserId",(long)uId,metaObject);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if(metaObject.hasSetter("updateDateTime")){
            int uId = SpringUtil.getBean(AuthUtil.class).getUserId();
            if(uId <= 0){
                log.warn("EJMetaObjectHandler updateFill 当前用户id 为"+ uId);
            }

            this.setFieldValByName("updateDateTime",new Date(),metaObject);
            this.setFieldValByName("updateUserId",(long)uId,metaObject);
        }
    }
}
