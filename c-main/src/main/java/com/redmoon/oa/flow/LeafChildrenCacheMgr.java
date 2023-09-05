package com.redmoon.oa.flow;

import cn.js.fan.web.Global;
import java.util.Vector;
import cn.js.fan.cache.jcs.*;
import com.cloudwebsoft.framework.util.LogUtil;

import java.util.Iterator;

public class LeafChildrenCacheMgr {
    String parentCode;
    String connname;
    RMCache rmCache = RMCache.getInstance();
    static String cachePrix = "FLOW_dirlist_";
    static String group = "FLOW_LeafChildren";
    Vector<Leaf> list;

    public LeafChildrenCacheMgr(String parentCode) {
        this.parentCode = parentCode;
        connname = Global.getDefaultDB();
        if ("".equals(connname)) {
            LogUtil.getLog(getClass()).info("LeafChildrenCacheMgr:默认数据库名不能为空");
        }
        list = getDirList();
    }

    public Vector<Leaf> getList() {
        return list;
    }

    public Vector<Leaf> getDirList() {
        Vector<Leaf> v = null;
        try {
            v = (Vector<Leaf>) rmCache.getFromGroup(cachePrix + parentCode, group);
            if (v == null) {
                v = load();
                rmCache.putInGroup(cachePrix + parentCode, group, v);
            }
            else {
                for (Leaf lf : v) {
                    lf.renew();
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return v;
    }

    public Vector<Leaf> load() {
        Directory dir = new Directory();
        Leaf leaf = dir.getLeaf(parentCode);
        return leaf.getChildren();
    }

    public static void remove(String parentCode) {
        try {
            RMCache rmCache = RMCache.getInstance();
            rmCache.remove(cachePrix + parentCode, group);
        } catch (Exception e) {
            LogUtil.getLog(LeafChildrenCacheMgr.class).error(e);
        }
    }

    public static void removeAll() {
        try {
            RMCache rmCache = RMCache.getInstance();
            rmCache.invalidateGroup(group);
        } catch (Exception e) {
            LogUtil.getLog(LeafChildrenCacheMgr.class).error(e);
        }
    }
}
