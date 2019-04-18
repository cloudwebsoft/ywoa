package com.redmoon.oa.basic;

import org.apache.log4j.Logger;
import cn.js.fan.cache.jcs.*;
import cn.js.fan.web.Global;
import cn.js.fan.resource.Constant;

public class TreeSelectCache implements ICacheMgr {
    final String group = "TREE_SELECT_";

    static boolean isRegisted = false;

    Logger logger = Logger.getLogger(TreeSelectCache.class.getName());
    RMCache rmCache = RMCache.getInstance();

    String connname = "";

    public TreeSelectCache() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info(Constant.DB_NAME_NOT_FOUND);

        regist();
    }


    /**
     * 定时刷新缓存
     */
    public void timer() {

    }

    /**
     * regist in RMCache
     */
    public void regist() {
    }

    public void refreshAddChild(String code ) {
        removeFromCache(code);
        TreeSelectChildrenCache.remove(code);        
    }

    public void refreshSave(String code, String parentCode) {
        removeFromCache(code);
        TreeSelectChildrenCache.remove(parentCode);
    }

    public void refreshDel(String code, String parentCode) {
        // removeFromCache(code);
        // removeFromCache(parentCode);
        removeAllFromCache();
    }

    public void removeAllFromCache() {
        try {
            rmCache.invalidateGroup(group);
            TreeSelectChildrenCache.removeAll();
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
            TreeSelectChildrenCache.remove(code);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public TreeSelectDb getTreeSelectDb(String code) {
    	TreeSelectDb leaf = null;
        try {
            leaf = (TreeSelectDb) rmCache.getFromGroup(code, group);
        } catch (Exception e) {
            logger.error("getTreeSelectDb1:" + e.getMessage());
        }
        if (leaf == null) {
            leaf = new TreeSelectDb(code);
            if (leaf != null && leaf.isLoaded()) {
                try {
                    rmCache.putInGroup(code, group, leaf);
                } catch (Exception e) {
                    logger.error("getTreeSelectDb2:" + e.getMessage());
                }
            }
        } else
            leaf.renew();

        return leaf;
    }
}


