package com.redmoon.forum.music;

import org.apache.log4j.Logger;
import cn.js.fan.cache.jcs.*;
import cn.js.fan.web.Global;
import cn.js.fan.resource.Constant;

public class MusicDirCache implements ICacheMgr {
    final String group = "FORUM_MUSIC";

    static boolean isRegisted = false;

    Logger logger = Logger.getLogger(MusicDirCache.class.getName());
    RMCache rmCache = RMCache.getInstance();

    String connname = "";

    public MusicDirCache() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info(Constant.DB_NAME_NOT_FOUND);

        regist();
    }


    /**
     * 定时刷新缓存
     */
    public void timer() {
/*      // 刷新全文检索
        curFulltextLife--;
        if (curFulltextLife<=0) {
            refreshFulltext();
            curFulltextLife = FULLTEXTMAXLIFE;
        }
*/
    }

    /**
     * regist in RMCache
     */
    public void regist() {
/*        if (!isRegisted) {
            rmCache.regist(this);
            isRegisted = true;
        }
 */
    }

    public void refreshAddChild(String code ) {
        removeFromCache(code);
    }

    public void refreshSave(String code, String parentCode) {
        removeFromCache(code);
        MusicDirChildrenCache.remove(parentCode);
    }

    public void refreshDel(String code, String parentCode) {
        // removeFromCache(code);
        // removeFromCache(parentCode);
        removeAllFromCache();
    }

    public void removeAllFromCache() {
        try {
            rmCache.invalidateGroup(group);
            MusicDirChildrenCache.removeAll();
        } catch (Exception e) {
            logger.error("removeAllFromCache: " + e.getMessage());
        }
    }

    public void refreshMove(String code, String brotherCode) {
        removeFromCache(code);
        removeFromCache(brotherCode);
    }

    /**
     * 每个节点有两个Cache，一是本身，另一个是用于存储其孩子结点的cache
     * @param code String
     */
    public void removeFromCache(String code) {
        try {
            rmCache.remove(code, group);
            MusicDirChildrenCache.remove(code);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public MusicDirDb getMusicDirDb(String code) {
        MusicDirDb leaf = null;
        try {
            leaf = (MusicDirDb) rmCache.getFromGroup(code, group);
        } catch (Exception e) {
            logger.error("getMusicDirDb1:" + e.getMessage());
        }
        if (leaf == null) {
            leaf = new MusicDirDb(code);
            if (leaf != null) {
                try {
                    rmCache.putInGroup(code, group, leaf);
                } catch (Exception e) {
                    logger.error("getMusicDirDb2:" + e.getMessage());
                }
            }
        } else
            leaf.renew();

        return leaf;
    }
}


