package com.cloudweb.oa.exception;

import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.utils.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ValidateException extends Exception {
    String msg;

    Object[] objects;

    @Autowired
    @Qualifier("validMessageSource")
    MessageSource messageSource;

    private static ValidateException staticValidateException;

    //被@PostConstruct修饰的方法会在服务器加载Servlet的时候运行，并且只会被服务器调用一次，类似于Servlet的init()方法。被@PostConstruct修饰的方法会在构造函数之后，init()方法之前运行
    @PostConstruct //通过@PostConstruct实现初始化bean之前进行的操作，本处是为了支持非spring扫描的包中调用，当然也可以通过SpringHelper.getBean获取
    public void init() {
        staticValidateException = this;
    }

    public ValidateException() {
        super();
    }

    public ValidateException(String msg) {
        super(msg);
        this.msg = msg;
        if (messageSource==null) {
            messageSource = staticValidateException.messageSource;
        }
    }

    public ValidateException(String msg, @Nullable Object[] objects) {
        super(msg);
        this.msg = msg;
        this.objects = objects;
        if (messageSource==null) {
            messageSource = staticValidateException.messageSource;
        }
    }

    @Override
    public String getMessage() {
        if (msg.startsWith("#")) {
            String key = msg.substring(1);
            msg = messageSource.getMessage(key, objects, LocaleContextHolder.getLocale());
        }
        else {
            if (objects!=null) {
                msg = StrUtil.format(msg, objects);
            }
        }

        return msg;
    }
}
