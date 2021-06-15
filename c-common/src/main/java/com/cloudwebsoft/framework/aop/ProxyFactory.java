package com.cloudwebsoft.framework.aop;

import java.util.Vector;
import com.cloudwebsoft.framework.aop.base.Advisor;

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

public class ProxyFactory {
    Object obj;
    String className;

    Vector advisors = new Vector();

    public ProxyFactory(String className) {
        this.className = className;
        obj = getClassInstance();
    }

    public void addAdvisor(Advisor advisor) {
        advisors.addElement(advisor);
    }

    private Object getClassInstance() {
        Object obj = null;
        try {
            Class cls = Class.forName(className);
            obj = (Object) cls.newInstance();
        } catch (ClassNotFoundException cnfe) {
            System.out.println(ProxyFactory.class.getName() + " getClassInstance: ClassNotFoundException:" + cnfe.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public Object getProxy() {
        Object proxy = null;
        Binder binder = new Binder();
        binder.setAdvisors(advisors);
        if (obj != null) {
            proxy = binder.bind(obj);
        } else {
            System.out.println(ProxyFactory.class.getName() + " getProxy: Can't get the proxyobj");
            // throw
        }
        return proxy;
    }

    public Object getObject() {
        return obj;
    }

}
