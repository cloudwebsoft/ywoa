package com.redmoon.oa.address;

import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;

import java.util.Vector;
import cn.js.fan.cache.jcs.*;
import java.util.Iterator;

public class LeafChildrenCacheMgr {
    String parentCode;
    String connname;
    RMCache rmCache = RMCache.getInstance();
    static String cachePrix = "addr_dirlist_";
    static String group = "ADDR_LeafChildren";

    Vector list = null;

    public LeafChildrenCacheMgr(String parentCode) {
        this.parentCode = parentCode;
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).error("LeafChildrenCacheMgr:默认数据库名不能为空");
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
            } else {
                Iterator ir = v.iterator();
                while (ir.hasNext()) {
                    Leaf lf = (Leaf)ir.next();
                    lf.renew();
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
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
            // RMCache rmCache = RMCache.getInstance();
            // rmCache.remove(cachePrix + parentCode, group);
        	removeAll();
        } catch (Exception e) {
        	LogUtil.getLog(LeafChildrenCacheMgr.class.getName()).error(e.getMessage());
        }
    }


    public static void removeAll() {
        try {
            RMCache rmCache = RMCache.getInstance();
            rmCache.invalidateGroup(group);
        } catch (Exception e) {
        	LogUtil.getLog(LeafChildrenCacheMgr.class.getName()).error(e.getMessage());
        }
    }
}
