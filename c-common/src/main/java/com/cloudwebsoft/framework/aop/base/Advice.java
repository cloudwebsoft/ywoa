package com.cloudwebsoft.framework.aop.base;

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
public interface Advice {

    public void Before(Object proxy, Method method, Object[] args) throws
            Throwable;

    public void After(Object proxy, Method method, Object[] args) throws
            Throwable;

    public void Throw(Object proxy, Method method, Object[] args, Exception e) throws
            Throwable;

}
