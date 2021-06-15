package com.cloudweb.oa.utils;

import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

/**
 * 外置Tomcat启动时，必须要有此类，否则会报NoSuchBeanDefinitionException ErrorAttributes No qualifying bean of type 'org.springframework.boot.web.servlet.error.ErrorAttributes' available
 */
@Component
public class MyErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
        Map<String, Object> map = super.getErrorAttributes(webRequest, includeStackTrace);
/*        if ((Integer)map.get("status") == 500) {
            map.put("message", "服务器内部错误!");
        }*/
        return map;
    }
}