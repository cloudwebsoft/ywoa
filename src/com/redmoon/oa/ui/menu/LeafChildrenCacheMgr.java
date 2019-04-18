package com.redmoon.oa.ui.menu;

import cn.js.fan.web.Global;
import org.apache.log4j.Logger;
import java.util.Vector;
import cn.js.fan.cache.jcs.*;
import java.util.Iterator;

public class LeafChildrenCacheMgr {
    String parentCode;
    String connname;
    RMCache rmCache = RMCache.getInstance();
    static String cachePrix = "oa_menu_list_";
    static String group = "OAMenuChildren";
    static Logger logger = Logger.getLogger(LeafChildrenCacheMgr.class.getName());
    Vector list = null;

    public LeafChildrenCacheMgr(String parentCode) {
        this.parentCode = parentCode;
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("LeafChildrenCacheMgr:connname is empty");
        list = getChildren();
    }

    public Vector getList() {
        return list;
    }

    public Vector getChildren() {
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
        //Vector v = new Vector();
        // v.addElement(leaf);
        return leaf.getChildren();
        //if (!childv.isEmpty())
        //    v.addAll(childv);
        //return v;
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
