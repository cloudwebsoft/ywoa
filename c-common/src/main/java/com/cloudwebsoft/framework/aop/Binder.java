package com.cloudwebsoft.framework.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.util.Vector;
import java.util.Iterator;
import com.cloudwebsoft.framework.aop.base.Advisor;
import cn.js.fan.util.ErrMsgException;
import com.cloudwebsoft.framework.util.LogUtil;

import java.lang.reflect.InvocationTargetException;

/**
 * <p>Title: </p>
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
public class Binder implements InvocationHandler {
    Vector advisors;
    public Object proxyObj;

    public Binder() {
    }

    public Object bind(Object obj) {
        this.proxyObj = obj;
        return Proxy.newProxyInstance(obj.getClass().getClassLoader(),
                                      obj.getClass().getInterfaces(), this);
    }

    public void setAdvisors(Vector advisors) {
        this.advisors = advisors;
    }

    public void Before(Object proxy, Method method, Object[] args) throws
            Throwable {
        if (advisors==null)
            return;
        Iterator ir = advisors.iterator();
        while (ir.hasNext()) {
            Advisor advisor = (Advisor)ir.next();
            advisor.Before(proxy, method, args);
        }
    }

    public void After(Object proxy, Method method, Object[] args) throws
            Throwable {
        if (advisors == null)
            return;
        Iterator ir = advisors.iterator();
        while (ir.hasNext()) {
            Advisor advisor = (Advisor) ir.next();
            advisor.After(proxy, method, args);
        }
    }

    public void Throw(Object proxy, Method method, Object[] args, Exception e) throws
            Throwable {
        if (advisors == null)
            return;
        Iterator ir = advisors.iterator();
        while (ir.hasNext()) {
            Advisor advisor = (Advisor) ir.next();
            advisor.Throw(proxy, method, args, e);
        }
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws
            Throwable {
        Before(proxy, method, args);
        Object result = null;
        try {
            // 请在这里插入代码，在方法前调用
            // 如果method.invoke中出现了异常，如ErrMsgException，则并不抛出，注意invoke只抛出几种异常，e.getMessage()得到的将是null
            result = method.invoke(proxyObj, args); // 原方法
            // 请在这里插入代码，方法后调用
        }
        catch (InvocationTargetException e) {
            LogUtil.getLog(getClass()).error(e);
            // Throw(proxy, method, args, e.getTargetException());
        }
        After(proxy, method, args);
        return result;
    }
}
