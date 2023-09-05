package com.redmoon.oa.fileark;

import cn.js.fan.web.Global;
import java.util.Vector;
import cn.js.fan.cache.jcs.*;
import com.cloudwebsoft.framework.util.LogUtil;

import java.util.Iterator;

public class LeafChildrenCacheMgr {
    String parentCode;
    String connname;
    RMCache rmCache = RMCache.getInstance();
    static String cachePrix = "CMSdirlist_";
    static String group = "CMSLeafChildren";
    Vector<Leaf> list = null;

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
            v = (Vector) rmCache.getFromGroup(cachePrix + parentCode,
                                              group);
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
            LogUtil.getLog(getClass()).error("getDirList:" + e.getMessage());
        }
        return v;
    }

    public Vector<Leaf> load() {
        Directory dir = new Directory();
        Leaf leaf = dir.getLeaf(parentCode);
        if (leaf==null || !leaf.isLoaded()) {
            return new Vector<>();
        }
        return leaf.getChildren();
    }

    public static void remove(String parentCode) {
        try {
            RMCache rmCache = RMCache.getInstance();
            rmCache.remove(cachePrix + parentCode, group);
        } catch (Exception e) {
            LogUtil.getLog(LeafChildrenCacheMgr.class).error(e.getMessage());
        }
    }

    public static void removeAll() {
        try {
            RMCache rmCache = RMCache.getInstance();
            rmCache.invalidateGroup(group);
        } catch (Exception e) {
            LogUtil.getLog(LeafChildrenCacheMgr.class).error(e.getMessage());
        }
    }
}
