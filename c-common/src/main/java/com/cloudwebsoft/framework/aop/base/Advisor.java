package com.cloudwebsoft.framework.aop.base;

import com.cloudwebsoft.framework.aop.base.Pointcut;
import java.lang.reflect.Method;
import com.cloudwebsoft.framework.aop.base.Advice;

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
public class Advisor {
    Advice advice;
    Pointcut pointcut;

    public Advisor() {
    }

    public Advice getAdvice() {
        return advice;
    }

    public void setAdvice(Advice advice) {
        this.advice = advice;
    }

    public Pointcut getPointcut() {
        return this.pointcut;
    }

    public void setPointcut(Pointcut pointcut) {
        this.pointcut = pointcut;
    }

    public void Before(Object proxy, Method method, Object[] args) throws
            Throwable {
        if (pointcut.hit(proxy, method, args))
            advice.Before(proxy, method, args);
    }

    public void After(Object proxy, Method method, Object[] args) throws
            Throwable {
        if (pointcut.hit(proxy, method, args)) {
            advice.After(proxy, method, args);
        }
    }

    public void Throw(Object proxy, Method method, Object[] args, Exception e) throws
            Throwable {
        if (pointcut.hit(proxy, method, args)) {
           advice.Throw(proxy, method, args, e);
        }
    }
}
