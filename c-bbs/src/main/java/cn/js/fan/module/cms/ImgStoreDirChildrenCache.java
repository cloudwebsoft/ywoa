package cn.js.fan.module.cms;

import java.util.*;
import cn.js.fan.cache.jcs.*;
import org.apache.log4j.*;

public class ImgStoreDirChildrenCache {
    String parentCode;
    RMCache rmCache = RMCache.getInstance();
    static String cachePrix = "img_store_dir_list_";
    static Logger logger = Logger.getLogger(ImgStoreDirChildrenCache.class.getName());
    Vector list = null;
    static String group = "Img_Store_Leaf_Children";

    public ImgStoreDirChildrenCache(String parentCode) {
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
                ImgStoreDirDb lf = (ImgStoreDirDb) ir.next();
                lf.renew();
            }
        }
        return v;
    }

    public Vector load() {
        ImgStoreDirDb dd = new ImgStoreDirDb();
        dd = dd.getImgStoreDirDb(parentCode);
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
