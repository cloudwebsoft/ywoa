package com.redmoon.oa.pvg;

import cn.js.fan.base.*;
import com.cloudwebsoft.framework.util.LogUtil;

import java.util.Iterator;
import java.util.Vector;

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
public class PrivCache extends ObjectCache {
    static final String ALLPRIVS = "ALLPRIVS";

    public PrivCache() {
    }

    public PrivCache(PrivDb pd) {
        super(pd);
    }


    public PrivDb[] getAllPrivs() {
        PrivDb[] p = null;
        try {
            p = (PrivDb[]) rmCache.getFromGroup(ALLPRIVS, group);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("getPrivs:" + e.getMessage());
        }
        if (p != null)
            return p;

        PrivDb pd = new PrivDb();
        Vector v = pd.list();
        int count = v.size();
        if (count > 0) {
            p = new PrivDb[count];
            Iterator ir = v.iterator();
            int i = 0;
            while (ir.hasNext()) {
                pd = (PrivDb) ir.next();
                p[i] = pd;
                i++;
            }
        }

        if (p != null) {
            try {
                rmCache.putInGroup(ALLPRIVS, group, p);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error("getPrivs:" + e.getMessage());
            }
        }
        return p;
    }

    public void refreshAllPrivs() {
        try {
            rmCache.remove(ALLPRIVS, group);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("refreshAllPrivs:" + e.getMessage());
        }
    }

}
