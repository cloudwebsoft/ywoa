package com.cloudwebsoft.framework.aop;

import com.cloudwebsoft.framework.aop.base.Advisor;
import com.cloudwebsoft.framework.aop.advice.TransactionAdvice;
import com.cloudwebsoft.framework.aop.Pointcut.RegexpMethodPointcut;
import com.cloudwebsoft.framework.aop.base.Pointcut;

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
public class TransactionProxyFactory extends ProxyFactory {

    Advisor tranAdvisor;

    public TransactionProxyFactory(String className) {
        super(className);

        TransactionAdvice ta = new TransactionAdvice();
        tranAdvisor.setAdvice(ta);
        // adv.setPointcut(new MethodNamePointcut("find", true));
        tranAdvisor.setPointcut(new RegexpMethodPointcut("[create|save|update].*"));
        addAdvisor(tranAdvisor);
    }

    public void setTranPointCut(Pointcut pointcut) {
        tranAdvisor.setPointcut(pointcut);
    }
}
