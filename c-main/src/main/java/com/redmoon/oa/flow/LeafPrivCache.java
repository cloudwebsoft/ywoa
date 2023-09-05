package com.redmoon.oa.flow;

import cn.js.fan.base.ObjectCache;
import com.cloudwebsoft.framework.util.LogUtil;

import java.util.Vector;

public class LeafPrivCache extends ObjectCache {
    public static final String PREFIX = "flow_";

    public LeafPrivCache(LeafPriv leafPriv) {
        super(leafPriv);
        listCachable = true;
    }

    public Vector listPriv(String dirCode, int priv) {
        Vector v = null;
        try {
            v = (Vector) rmCache.getFromGroup(PREFIX + dirCode + "_priv" + priv, group);
            if (v != null) {
                return v;
            }
            LeafPriv leafPriv = new LeafPriv(dirCode);
            v = leafPriv.listPrivByDb(priv);
            rmCache.putInGroup(PREFIX + dirCode + "_priv" + priv, group, v);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("listPriv:" + e.getMessage());
        }
        return v;
    }
}
