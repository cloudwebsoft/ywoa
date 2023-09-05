package com.cloudwebsoft.framework.aop;

import java.util.Vector;
import com.cloudwebsoft.framework.aop.base.Advisor;
import com.cloudwebsoft.framework.util.LogUtil;

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
        } catch (ClassNotFoundException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
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
            LogUtil.getLog(getClass()).error("getProxy: Can't get the proxyobj");
        }
        return proxy;
    }

    public Object getObject() {
        return obj;
    }

}
