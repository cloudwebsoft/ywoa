package cn.js.fan.module.cms;

import java.util.*;
import cn.js.fan.cache.jcs.*;
import org.apache.log4j.*;

public class SoftwareDirChildrenCache {
    String parentCode;
    RMCache rmCache = RMCache.getInstance();
    static String cachePrix = "software_dir_list_";
    static Logger logger = Logger.getLogger(SoftwareDirChildrenCache.class.getName());
    Vector list = null;
    static String group = "Software_Leaf_Children";

    public SoftwareDirChildrenCache(String parentCode) {
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
            logger.error("getDirList:" + e.getMessage());
        }
        if (v == null) {
            try {
                v = load();
                rmCache.putInGroup(cachePrix + parentCode, group, v);
            } catch (Exception e) {
                logger.error("getDirList:" + e.getMessage());
            }
        }
        else {
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                SoftwareDirDb lf = (SoftwareDirDb) ir.next();
                lf.renew();
            }
        }
        return v;
    }

    public Vector load() {
        SoftwareDirDb dd = new SoftwareDirDb();
        dd = dd.getSoftwareDirDb(parentCode);
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
