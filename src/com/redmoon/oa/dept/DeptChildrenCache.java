package com.redmoon.oa.dept;

import java.util.*;
import cn.js.fan.cache.jcs.*;
import org.apache.log4j.*;

public class DeptChildrenCache {
    String parentCode;
    RMCache rmCache = RMCache.getInstance();
    static String cachePrix = "dept_dirlist_";
    static Logger logger = Logger.getLogger(DeptChildrenCache.class.getName());
    Vector list = null;
    static String group = "DEPT_LeafChildren";

    public DeptChildrenCache(String parentCode) {
        this.parentCode = parentCode;
        list = getDirList();
    }

    public Vector getList() {
        return list;
    }

    public Vector getDirList() {
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
                DeptDb lf = (DeptDb) ir.next();
                lf.renew();
            }
        }
        return v;
    }

    public Vector load() {
        DeptDb dd = new DeptDb();
        dd = dd.getDeptDb(parentCode);
        return dd.getChildren();
    }

    public static void remove(String parentCode) {
        try {
            // RMCache rmCache = RMCache.getInstance();
            // rmCache.remove(cachePrix + parentCode, group);
            
						removeAll();
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
