package com.cloudwebsoft.framework.util;

import java.sql.ResultSet;
import java.util.Map;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import cn.js.fan.util.StrUtil;

/**
 *
 * <p>Title: 用以获取对象实例的属性</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class BeanUtil {

    public static Object getProperty(Object bean, String propertyName) {
        if (bean == null || propertyName == null)return null;

        boolean isMap = bean instanceof Map;
        if (isMap) {
            Object value = ((Map) bean).get(propertyName);
            if (value != null)return value;
        }

        if (bean instanceof ResultSet) {
            ResultSet rs = (ResultSet) bean;
            try {
                return rs.getObject(propertyName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return getBeanProperty(bean, propertyName);
    }

    public static Object getBeanProperty(Object bean, String propertyName) {
        Object obj = getBeanPropertyMethodOrField(bean, propertyName);
        if (obj==null)
            return null;
        Object[] params = null;
        Object propertyValue = null;
        try {
            if (obj instanceof Method) {
                propertyValue = ((Method) obj).invoke(bean, params);
            } else {
                propertyValue = ((Field) obj).get(bean);
            }
        } catch (Exception e) {
            LogUtil.getLog(BeanUtil.class).error("getBeanProperty:" +
                                                 e.getMessage());
        }

        return propertyValue;
    }

    public static Object getBeanPropertyMethodOrField(Object bean, String propertyName) {
        if (bean == null || propertyName == null)
            return null;

        Class beanClass = bean.getClass();

        boolean hit = false;
        Object ret = null;

        String propName = Character.toUpperCase(propertyName.charAt(0))
                          + propertyName.substring(1);
        String methodName = "get" + propName;

        Method method = null;
        try {
            Class[] args = null;
            method = beanClass.getMethod(methodName, args);
            ret = method;
            hit = true;
        } catch (Exception e) {
            method = null;
            LogUtil.getLog(BeanUtil.class).info("getBeanPropertyMethodOrField:" + e.getMessage());
        }

        if (!hit) {
            try {
                methodName = "is" + propName;
                Class[] args = null;
                method = beanClass.getMethod(methodName, args);
                ret = method;
                hit = true;
            } catch (Exception e) {
                method = null;
                LogUtil.getLog(BeanUtil.class).info("getBeanPropertyMethodOrField:" + e.getMessage());
            }
        }

        Field field = null;
        if (!hit) {
            try {
                field = beanClass.getField(propertyName);
                ret = field;
                hit = true;
            } catch (Exception e) {
                field = null;
                LogUtil.getLog(BeanUtil.class).info("getBeanPropertyMethodOrField:" + e.getMessage());
            }
        }
        if (ret == null) {
            LogUtil.getLog(BeanUtil.class).error("getBeanPropertyMethodOrField:" + propertyName + " is not found in " + bean.getClass());
            LogUtil.getLog(BeanUtil.class).error("getBeanPropertyMethodOrField:" + StrUtil.trace(new Exception()));
        }

        return ret;

    }
}
