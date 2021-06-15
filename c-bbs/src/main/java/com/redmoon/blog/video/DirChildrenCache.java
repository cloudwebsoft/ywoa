package com.redmoon.blog.video;

import java.util.*;
import cn.js.fan.cache.jcs.*;
import org.apache.log4j.*;
import com.cloudwebsoft.framework.util.LogUtil;

public class DirChildrenCache {
    String parentCode;
    RMCache rmCache = RMCache.getInstance();
    static String cachePrix = "blog_video_dir_list_";
    Vector list = null;
    static String group = "Blog_Video_Leaf_Children";

    public DirChildrenCache(String parentCode) {
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
            LogUtil.getLog(getClass()).error("getDirList:" + e.getMessage());
        }
        if (v == null) {
            try {
                v = load();
                rmCache.putInGroup(cachePrix + parentCode, group, v);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error("getDirList:" + e.getMessage());
            }
        }
        else {
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                DirDb lf = (DirDb) ir.next();
                lf.renew();
            }
        }
        return v;
    }

    public Vector load() {
        DirDb dd = new DirDb();
        dd = dd.getDirDb(parentCode);
        return dd.getChildren();
    }

    public static void remove(String parentCode) {
        try {
            RMCache rmCache = RMCache.getInstance();
            rmCache.remove(cachePrix + parentCode, group);
        } catch (Exception e) {
            LogUtil.getLog("com.redmoon.blog.video.DirChildrenCache").error(e.getMessage());
        }
    }

    public static void removeAll() {
        try {
            RMCache rmCache = RMCache.getInstance();
            rmCache.invalidateGroup(group);
        } catch (Exception e) {
            LogUtil.getLog("com.redmoon.blog.video.DirChildrenCache").error(e.getMessage());
        }
    }
}
