package com.redmoon.oa.netdisk;

import cn.js.fan.web.Global;
import org.apache.log4j.Logger;
import java.util.Vector;
import cn.js.fan.cache.jcs.*;

public class PublicLeafChildrenCacheMgr {
    String parentCode;
    String connname;
    RMCache rmCache = RMCache.getInstance();
    static String cachePrix = "netdisk_public_dirlist_";
    static String group = "netdisk_public_LeafChildren";
    static Logger logger = Logger.getLogger(PublicLeafChildrenCacheMgr.class.getName());
    Vector list = null;

    public PublicLeafChildrenCacheMgr(String parentCode) {
        this.parentCode = parentCode;
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("LeafChildrenCacheMgr:默认数据库名不能为空");
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
        } catch (Exception e) {
            logger.error("getDirList:" + e.getMessage());
        }
        return v;
    }

    public Vector load() {
        PublicDirectory dir = new PublicDirectory();
        PublicLeaf leaf = dir.getLeaf(parentCode);
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
