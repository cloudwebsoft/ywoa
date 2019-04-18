package com.redmoon.oa.basic;

import java.util.*;
import cn.js.fan.cache.jcs.*;
import org.apache.log4j.*;

public class TreeSelectChildrenCache {
    String parentCode;
    RMCache rmCache = RMCache.getInstance();
    static String cachePrix = "tree_select_dirlist_";
    static Logger logger = Logger.getLogger(TreeSelectChildrenCache.class.getName());
    Vector list = null;
    static String group = "tree_select_children";

    public TreeSelectChildrenCache(String parentCode) {
        this.parentCode = parentCode;
        list = getTreeSelectList();
    }

    public Vector getList() {
        return list;
    }

    public Vector getTreeSelectList() {
        Vector v = null;
        try {
            v = (Vector) rmCache.getFromGroup(cachePrix + parentCode, group);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (v == null) {
            try {
                v = load();
                rmCache.putInGroup(cachePrix + parentCode, group, v);
            } catch (Exception e) {
                logger.error("getDocList:" + e.getMessage());
            }
        }
        else {
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                TreeSelectDb lf = (TreeSelectDb) ir.next();
                lf.renew();
            }
        }
        return v;
    }

    public Vector load() {
        TreeSelectDb dd = new TreeSelectDb();
        dd = dd.getTreeSelectDb(parentCode);
        return dd.getChildren();
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
