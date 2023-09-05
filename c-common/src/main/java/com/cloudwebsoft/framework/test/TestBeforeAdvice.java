package com.cloudwebsoft.framework.test;

import java.lang.reflect.Method;

import com.cloudwebsoft.framework.aop.advice.BeforeAdvice;

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
public class TestBeforeAdvice extends BeforeAdvice {
    public TestBeforeAdvice() {
    }

    /**
     * Before
     *
     * @param proxy Object
     * @param method Method
     * @param args Object[]
     * @throws Throwable
     */
    @Override
    public void Before(Object proxy, Method method, Object[] args) throws
            Throwable {
        // LogUtil.getLog(getClass()).info(this.getClass().getName() + " log here!");
    }
}
