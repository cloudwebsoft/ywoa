package com.cloudwebsoft.framework.aop.Pointcut;

import com.cloudwebsoft.framework.aop.base.Pointcut;
import java.lang.reflect.Method;

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
public class MethodNamePointcut implements Pointcut {
    String methodName;
    boolean isStartWith;

    public MethodNamePointcut(String methodName, boolean isStartWith) {
        this.methodName = methodName;
        this.isStartWith = isStartWith;
    }

    public boolean hit(Object proxy, Method method, Object[] args) {
        if (isStartWith) {
            if (method.getName().startsWith(methodName))
                return true;
        }
        else {
            if (method.getName().equals(methodName))
                return true;
        }
        return false;
    }
}
