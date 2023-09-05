package com.cloudwebsoft.framework.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import cn.js.fan.cache.jcs.ICacheMgr;
import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.db.KeyUnit;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.resource.Constant;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.db.Connection;
import com.cloudwebsoft.framework.util.LogUtil;

public class ObjectCache implements ICacheMgr, java.io.Serializable {
    public String group;
    public String COUNT_GROUP_NAME;

    public transient RMCache rmCache;
    public String connname = "";

    public ObjectDb objectDb;

    public ObjectCache() {
        init();
        regist();
    }

    public ObjectCache(ObjectDb obj) {
        this.objectDb = obj;
        init();
        regist();
    }

    public void renew() {
        if (rmCache==null) {
            rmCache = RMCache.getInstance();
        }
    }

    public void init() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).info(Constant.DB_NAME_NOT_FOUND);
        rmCache = RMCache.getInstance();
        setGroup();
        setGroupCount();
    }

    public void setGroup() {
        group = this.getClass().getName();
    }

    public void setGroupCount() {
        this.COUNT_GROUP_NAME = group + ".Count";
    }

    /**
     * 定时刷新缓存
     */
    @Override
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
    @Override
    public void regist() {
/*        if (!isRegisted) {
            rmCache.regist(this);
            isRegisted = true;
        }
 */
    }

    public void setObjectDb(ObjectDb objectDb) {
        this.objectDb = objectDb;
    }

    public void setObjCachable(boolean objCachable) {
        this.objCachable = objCachable;
    }

    public void setListCachable(boolean listCachable) {
        this.listCachable = listCachable;
    }

    public void refreshCreate() {
        refreshCreate("");
    }

    public void refreshCreate(String groupName) {
        if (!listCachable) {
            return;
        }
        refreshList(groupName);
    }

    public void refreshList() {
        refreshList("");
    }

    public void refreshList(String groupName) {
        if (!listCachable) {
            return;
        }
        try {
            // 当在修改OA中档案模块时，发现如果置用户为invalid，则在置用户职位时，因为user_sel.jsp中使用了getObjects，而当save时，并未刷新列表，就是因为未刷新
            // 因此添加此方法
            rmCache.invalidateGroup(COUNT_GROUP_NAME + groupName);
            rmCache.invalidateGroup(group + groupName);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
    }

    public void refreshSave(PrimaryKey pk) {
        if (objCachable) {
            removeFromCache(pk);
        }
    }

    public void refreshDel(PrimaryKey pk) {
        refreshDel(pk, "");
    }

    public void refreshDel(PrimaryKey pk, String groupName) {
        try {
            if (objCachable) {
                removeFromCache(pk);
            }
            if (listCachable) {
                refreshList(groupName);
            }
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("refreshDel:" + e.getMessage());
        }
    }

    /**
     * 每个节点有两个Cache，一是本身，另一个是用于存储其孩子结点的cache
     */
    public void removeFromCache(PrimaryKey pk) {
        if (objCachable) {
            try {
                rmCache.remove(pk.getValue(), group);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e.getMessage());
            }
        }
    }

    public IObjectDb getObjectDb(PrimaryKey pk) {
        IObjectDb obj = null;
        if (objCachable) {
            try {
                obj = (ObjectDb) rmCache.getFromGroup(pk.getValue(), group);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error("getObjectDb:" + e.getMessage());
            }

            // LogUtil.getLog(getClass()).info("obj=" + obj + " pk=" + pk.getValue() + " group=" + group);
            if (obj == null) {
                obj = objectDb.getObjectRaw(pk);
                // 如果没能载入就不能放入缓存，否则放入缓存后，这个对象后来又生成了，
                // 比如SweetUserDb，就不会再从数据库中获取，就会被认为user未加入SweetUserDb对应的表中
                if (obj != null && obj.isLoaded()) {
                    try {
                        rmCache.putInGroup(pk.getValue(), group, obj);
                    } catch (Exception e) {
                        LogUtil.getLog(getClass()).error("getObjectDb1:" + e.getMessage());
                    }
                }
            } else {
                // LogUtil.getLog(getClass()).info("LogUtil.getLog(getClass())=" + obj.LogUtil.getLog(getClass()));
                obj.renew();
                // obj.LogUtil.getLog(getClass()).info("yes");
            }
        }
        else {
            obj = objectDb.getObjectRaw(pk);
        }
        return obj;
    }

    public int getObjectCount(String sql) {
        return getObjectCount(sql, "");
    }

    /**
     * 获取记录数目
     * @param sql String
     * @return int -1 表示sql语句不合法
     */
    public int getObjectCount(String sql, String groupName) {
        //根据sql语句得出计算总数的sql查询语句
        String query = cn.js.fan.db.SQLFilter.getCountSql(sql);
        if (!SecurityUtil.isValidSql(query)) {
            return -1;
        }
        Integer count = null;
        if (listCachable) {
            try {
                count = (Integer) rmCache.getFromGroup(query, COUNT_GROUP_NAME+groupName);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e.getMessage());
            }
            // If already in cache, return the count.
            if (count != null) {
                return count.intValue();
            }
        }
        // Otherwise, we have to load the count from the db.
        int docCount = objectDb.getObjectCountRaw(query);
        // Add the count to cache
        if (listCachable) {
            try {
                rmCache.putInGroup(query, COUNT_GROUP_NAME+groupName,
                                   new Integer(docCount));
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e.getMessage());
            }
        }
        return docCount;
    }

    public Object[] getObjectBlock(String sql, String groupName, int startIndex) {
        // First, discover what block number the results will be in.
        int blockSize = objectDb.getBlockSize();
        int blockID = startIndex / blockSize;
        int blockStart = blockID * blockSize;

        PrimaryKey primaryKey = objectDb.getPrimaryKey();
        // 取得根据主键的查询语句
        // String pk = primaryKey.getName();
        // String query = "select " + pk + " " + SQLFilter.getFromSql(sql); // 当为联合查询时，此句中的pk会带来问题，因为缺少表的别名作为前缀 2006.6.9
        // String query = "select " + objectDb.getTableName() + "." + pk + " " + SQLFilter.getFromSql(sql); // 加表名作为前缀在oracle中也不行
        String query = sql;

        // 缓存所用的key
        String key = query + blockID;

        Object[] objArray = null;
        // 如果使用列表缓存
        if (listCachable) {
            try {
                objArray = (Object[]) rmCache.getFromGroup(key,
                        group + groupName);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e.getMessage());
            }
            // If already in cache, return the block.
            if (objArray != null) {
                /**
                 * The actual block may be smaller than THREAD_BLOCK_SIZE. If that's
                 * the case, it means two things:
                 *  1) We're at the end boundary of all the results.
                 *  2) If the start index is greater than the length of the current
                 *     block, than there aren't really any results to return.
                 */
                Object[] objkeys = objArray;
                // 当startIndex过大时
                if (startIndex >= blockStart + objkeys.length) {
                    // Return an empty array
                    return ObjectDb.EMPTY_BLOCK;
                } else {
                    return objkeys;
                }
            }
        }
        // Otherwise, we have to load up the block from the database.

        Vector block = new Vector();
        ResultSet rs = null;
        Connection conn = new Connection(connname);
        try {
            // Set the maxium number of rows to end at the end of this block.
            conn.setMaxRows(blockSize * (blockID + 1));
            rs = conn.executeQuery(query);
            //LogUtil.getLog(getClass()).info("query=" + query);
            // Grab THREAD_BLOCK_ROWS rows at a time.
            conn.setFetchSize(blockSize);
            // Many JDBC drivers don't implement scrollable cursors the real
            // way, but instead load all results into memory. Looping through
            // the results ourselves is more efficient.
            for (int i = 0; i < blockStart; i++) {
                rs.next();
            }
            // Keep reading results until the result set is exaughsted or
            // we come to the end of the block.
            int count = 0;
            while (rs.next() && count < blockSize) {
                // 如果不是复合主键
                if (primaryKey.getKeyCount() == 1) {
                    if (primaryKey.getType() == primaryKey.TYPE_INT)
                        block.addElement(new Integer(rs.getInt(1)));
                    else if (primaryKey.getType() == primaryKey.TYPE_STRING)
                        block.addElement(rs.getString(1));
                    else if (primaryKey.getType() == primaryKey.TYPE_LONG)
                        block.addElement(new Long(rs.getLong(1)));
                    else if (primaryKey.getType() == primaryKey.TYPE_DATE)
                        block.addElement(new java.util.Date(rs.getTimestamp(1).getTime()));
                } else if (primaryKey.getType() == primaryKey.TYPE_COMPOUND) { // 如果是复合主键
                    HashMap keys = ((PrimaryKey) primaryKey.clone()).getKeys();
                    Iterator ir = keys.keySet().iterator();
                    while (ir.hasNext()) {
                        String keyName = (String) ir.next();
                        KeyUnit ku = (KeyUnit) keys.get(keyName);
                        if (ku.getType() == primaryKey.TYPE_INT) {
                            ku.setValue(new Integer(rs.getInt(ku.getOrders() + 1)));
                        } else if (ku.getType() == primaryKey.TYPE_LONG) {
                            ku.setValue(new Long(rs.getLong(ku.getOrders() + 1)));
                        }
                        else if (ku.getType() == PrimaryKey.TYPE_DATE) {
                            ku.setValue(new java.util.Date(rs.getTimestamp(ku.getOrders() + 1).getTime()));
                        }
                        else {
                            ku.setValue(rs.getString(ku.getOrders() + 1));
                        }
                    }
                    // 遗漏了下句，补于2005-8-22
                    block.addElement(keys);
                }
                count++;
            }
        } catch (SQLException sqle) {
            LogUtil.getLog(getClass()).error("getObjectBlock:" + sqle.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        int len = block.size();
        Object[] objkeys = new Object[len];
        for (int i = 0; i < len; i++) {
            objkeys[i] = block.elementAt(i);
        }
        // 将block加至缓存，len为0时也加入至缓存，因为如果有新数据插入了表中，则相关操作应刷新缓存，此处缓存也能得到更新
        if (listCachable) {
            try {
                rmCache.putInGroup(key, group + groupName, objkeys);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e.getMessage());
            }
        }
        /**
         * The actual block may be smaller than THREAD_BLOCK_SIZE. If that's
         * the case, it means two things:
         *  1) We're at the end boundary of all the results.
         *  2) If the start index is greater than the length of the current
         *     block, than there aren't really any results to return.
         */
        if (startIndex >= blockStart + objkeys.length) {
            // Return an empty array
            return ObjectDb.EMPTY_BLOCK;
        } else {
            return objkeys;
        }
    }

    public boolean isObjCachable() {
        return objCachable;
    }

    public boolean isListCachable() {
        return listCachable;
    }

    public boolean objCachable = true;
    public boolean listCachable = true;

}


