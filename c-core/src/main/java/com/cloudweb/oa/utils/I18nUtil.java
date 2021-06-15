package com.cloudweb.oa.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
// @PropertySource({"classpath:application.properties"})
public class I18nUtil {

    @Autowired
    // @Qualifier(value = "messageSource")
    private MessageSource messageSource;

    @Autowired
    @Qualifier("validMessageSource")
    MessageSource validMessageSource;

    /**
     * 取单个国际化值
     */
    public String get(String key) {
        try {
            return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return key;
        }
    }

    /**
     * 取国际化值，带有参数
     * @param key
     * @param args
     * @return
     */
    public String get(String key, Object... args) {
        try {
            return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return key;
        }
    }

    /**
     * 取校验国际化值
     * @param key
     * @return
     */
    public String getValid(String key) {
        try {
            return validMessageSource.getMessage(key, null, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return key;
        }
    }

    /**
     * 取校验国际化值，带参数
     * @param key
     * @param args
     * @return
     */
    public String getValid(String key, Object... args) {
        try {
            return validMessageSource.getMessage(key, args, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return key;
        }
    }
}