package cn.js.fan.module.cms;

import cn.js.fan.web.Global;
import org.apache.log4j.Logger;
import java.util.Vector;
import cn.js.fan.cache.jcs.*;
import java.util.Iterator;

public class LeafChildrenCacheMgr {
    String parentCode;
    String connname;
    RMCache rmCache = RMCache.getInstance();
    static String cachePrix = "CMSdirlist_";
    static String group = "CMSLeafChildren";
    static Logger logger = Logger.getLogger(LeafChildrenCacheMgr.class.getName());
    Vector list = null;

    public LeafChildrenCacheMgr(String parentCode) {
        this.parentCode = parentCode;
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("LeafChildrenCacheMgr:conname is empty");
        list = getDirList();
    }

    public Vector getList() {
        return list;
    }

    public Vector getDirList() {
        Vector v = null;
        try {
            v = (Vector) rmCache.getFromGroup(cachePrix + parentCode,
                                              group);
            if (v == null) {
                v = load();
                rmCache.putInGroup(cachePrix + parentCode, group, v);
            }
            else {
                Iterator ir = v.iterator();
                while (ir.hasNext()) {
                    Leaf lf = (Leaf) ir.next();
                    lf.renew();
                }
            }
        } catch (Exception e) {
            logger.error("getDirList:" + e.getMessage());
        }
        return v;
    }

    public Vector load() {
        Directory dir = new Directory();
        Leaf leaf = dir.getLeaf(parentCode);
        if (leaf==null || !leaf.isLoaded())
            return new Vector();
        return leaf.getChildren();
    }

    public static void remove(String parentCode) {
        try {
            RMCache rmCache = RMCache.getInstance();
            rmCache.remove(cachePrix + parentCode, group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public static void removeAll() {
        try {
            RMCache rmCache = RMCache.getInstance();
            rmCache.invalidateGroup(group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
