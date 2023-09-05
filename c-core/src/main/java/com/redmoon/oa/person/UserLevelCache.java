package com.redmoon.oa.person;

import cn.js.fan.base.ObjectCache;
import com.cloudwebsoft.framework.util.LogUtil;

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
public class UserLevelCache extends ObjectCache {
    final String ALLLEVEL = "OA_AllUserLevel";

    public UserLevelCache() {
        super();
    }

    public UserLevelCache(UserLevelDb uld) {
        super(uld);
    }

    public Vector<UserLevelDb> getAllLevel() {
        Vector<UserLevelDb> v = null;
        try {
            v = (Vector) rmCache.getFromGroup(ALLLEVEL, group);
            if (v != null) {
                return v;
            }
            UserLevelDb uld = new UserLevelDb();
            v = uld.list();
            rmCache.putInGroup(ALLLEVEL, group, v);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("getAllLevel:" + e.getMessage());
        }
        return v;
    }

}
