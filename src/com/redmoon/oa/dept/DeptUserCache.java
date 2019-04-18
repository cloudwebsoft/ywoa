package com.redmoon.oa.dept;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.cache.jcs.*;
import cn.js.fan.db.*;
import cn.js.fan.resource.*;
import cn.js.fan.security.*;
import cn.js.fan.web.*;
import org.apache.log4j.*;

public class DeptUserCache implements ICacheMgr {
    final String group = "DEPT_USER";
    final String COUNT_GROUP_NAME = "DEPT_USER_COUNT";

    static boolean isRegisted = false;

    Logger logger = Logger.getLogger(DeptUserCache.class.getName());
    RMCache rmCache = RMCache.getInstance();

    String connname = "";

    public DeptUserCache() {
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

    public void refreshCreate() {
            //清DOCBLOCKCACHEPRIX缓存
            try {
                rmCache.invalidateGroup(COUNT_GROUP_NAME);
            } catch (Exception e) {
                logger.error(e.getMessage());
        }
    }

    public void refreshSave(int id) {
        removeFromCache(id);
    }

    public void refreshDel(int id) {
        try {
            removeFromCache(id);
            rmCache.invalidateGroup(COUNT_GROUP_NAME);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
    }


    /**
     * 每个节点有两个Cache，一是本身，另一个是用于存储其孩子结点的cache
     * @param code String
     */
    public void removeFromCache(int id) {
        try {
            rmCache.remove("" + id, group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public DeptUserDb getDeptUserDb(int id) {
        DeptUserDb user = null;
        try {
            user = (DeptUserDb) rmCache.getFromGroup("" + id, group);
        }
        catch (Exception e) {
            logger.error("getPostUserDb:" + e.getMessage());
        }
        if (user == null) {
            user = new DeptUserDb(id);
            if (user != null) {
                try {
                    rmCache.putInGroup("" + id, group, user);
                } catch (Exception e) {
                    logger.error("getDeptUserDb:" + e.getMessage());
                }
            }
        }
        return user;
    }

    /**
     *
     * @param sql String
     * @return int -1 表示sql语句不合法
     */
    public int getDeptUserCount(String sql) {
        //根据sql语句得出计算总数的sql查询语句
        String query = SQLFilter.getCountSql(sql);
        if (!SecurityUtil.isValidSql(query))
            return -1;
        Integer count = null;
        try {
            count = (Integer) rmCache.getFromGroup(query, COUNT_GROUP_NAME);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        // If already in cache, return the count.
        if (count != null) {
            return count.intValue();
        }
        // Otherwise, we have to load the count from the db.
        else {
            int docCount = 0;
            Conn conn = new Conn(connname);
            ResultSet rs = null;
            try {
                rs = conn.executeQuery(query);
                if (rs.next())
                    docCount = rs.getInt(1);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
            // Add the thread count to cache
            try {
                rmCache.putInGroup(query, COUNT_GROUP_NAME,
                                   new Integer(docCount));
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            return docCount;
        }
    }

    public void refreshMove(int id, int brotherId) {
        removeFromCache(id);
        removeFromCache(brotherId);
    }

}
