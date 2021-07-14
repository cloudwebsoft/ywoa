package com.redmoon.forum.music;

import java.util.*;
import cn.js.fan.cache.jcs.*;
import org.apache.log4j.*;

public class MusicDirChildrenCache {
    String parentCode;
    RMCache rmCache = RMCache.getInstance();
    static String cachePrix = "forum_music_dir_list_";
    static Logger logger = Logger.getLogger(MusicDirChildrenCache.class.getName());
    Vector list = null;
    static String group = "Forum_Music_Leaf_Children";

    public MusicDirChildrenCache(String parentCode) {
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
                MusicDirDb lf = (MusicDirDb) ir.next();
                lf.renew();
            }
        }
        return v;
    }

    public Vector load() {
        MusicDirDb dd = new MusicDirDb();
        dd = dd.getMusicDirDb(parentCode);
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
