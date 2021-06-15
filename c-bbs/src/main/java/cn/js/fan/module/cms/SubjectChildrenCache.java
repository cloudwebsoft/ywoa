package cn.js.fan.module.cms;

import java.util.*;
import cn.js.fan.cache.jcs.*;
import org.apache.log4j.*;

public class SubjectChildrenCache {
    String parentCode;
    RMCache rmCache = RMCache.getInstance();
    static String cachePrix = "subject_dir_list_";
    static Logger logger = Logger.getLogger(SubjectChildrenCache.class.getName());
    Vector list = null;
    static String group = "Subject_Leaf_Children";

    public SubjectChildrenCache(String parentCode) {
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
                SubjectDb lf = (SubjectDb) ir.next();
                lf.renew();
            }
        }
        return v;
    }

    public Vector load() {
        SubjectDb dd = new SubjectDb();
        dd = dd.getSubjectDb(parentCode);
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
