package com.redmoon.forum.person;

import cn.js.fan.web.Global;
import org.apache.log4j.Logger;
import java.util.Vector;
import cn.js.fan.cache.jcs.*;
import java.util.Iterator;

public class FactionChildrenCacheMgr {
    String parentCode;
    String connname;
    RMCache rmCache = RMCache.getInstance();
    static String cachePrix = "factionlist_";
    static String group = "FactionChildren";
    static Logger logger = Logger.getLogger(FactionChildrenCacheMgr.class.getName());
    Vector list = null;

    public FactionChildrenCacheMgr(String parentCode) {
        this.parentCode = parentCode;
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("FactionChildrenCacheMgr:默认数据库名不能为空");
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
                    FactionDb lf = (FactionDb) ir.next();
                    lf.renew();
                }
            }
        } catch (Exception e) {
            logger.error("getDirList:" + e.getMessage());
        }
        return v;
    }

    public Vector load() {
        FactionMgr dir = new FactionMgr();
        FactionDb leaf = dir.getFactionDb(parentCode);
        //Vector v = new Vector();
        // v.addElement(leaf);
        return leaf.getChildren();
        //if (!childv.isEmpty())
        //    v.addAll(childv);
        //return v;
    }

    public static void remove(String parentCode) {
    	/*
        try {
            RMCache rmCache = RMCache.getInstance();
            rmCache.remove(cachePrix + parentCode, group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        */
    	removeAll();
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
