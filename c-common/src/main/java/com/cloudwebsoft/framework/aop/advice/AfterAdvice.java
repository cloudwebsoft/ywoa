package com.cloudwebsoft.framework.aop.advice;

import com.cloudwebsoft.framework.aop.base.Advice;
import java.lang.reflect.Method;
import cn.js.fan.util.ErrMsgException;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public abstract class AfterAdvice implements Advice {
    public AfterAdvice() {
    }

    public void Before(Object proxy, Method method, Object[] args) throws
            Throwable {

    }

    abstract public void After(Object proxy, Method method, Object[] args) throws
            Throwable;

    public void Throw(Object proxy, Method method, Object[] args, Exception e) throws
            Throwable {
        throw new ErrMsgException(e.getMessage());
    }
}
