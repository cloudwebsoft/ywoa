package cn.js.fan.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import cn.js.fan.cache.jcs.ICacheMgr;
import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.db.*;
import cn.js.fan.resource.Constant;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 *
 * <p>Title: 与ObjectDb对象相对应的缓存</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ObjectCache implements ICacheMgr, java.io.Serializable {
    protected String group;
    protected String COUNT_GROUP_NAME;

    protected transient RMCache rmCache;
    protected String connname = "";

    protected ObjectDb objectDb;

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
        if (connname.equals("")) {
            LogUtil.getLog(getClass()).info(Constant.DB_NAME_NOT_FOUND);
        }
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

    public String getGroup() {
        return group;
    }

    public RMCache getRmCache() {
        return rmCache;
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

    public void setObjectDb(ObjectDb objectDb) {
        this.objectDb = objectDb;
    }

    public void setListCachable(boolean listCachable) {
        this.listCachable = listCachable;
    }

    public void setObjCachable(boolean objCachable) {
        this.objCachable = objCachable;
    }

    public void refreshCreate() {
        if (!listCachable) {
            return;
        }
        refreshList();
    }

    /**
     * 刷新列表缓存
     */
    public void refreshList() {
        if (!listCachable) {
            return;
        }
        try {
            // 当在修改OA中档案模块时，发现如果置用户为invalid，则在置用户职位时，因为user_sel.jsp中使用了getObjects，而当save时，并未刷新列表，就是因为未刷新列表
            // 因此添加此方法
            rmCache.invalidateGroup(COUNT_GROUP_NAME);
            rmCache.invalidateGroup(group);
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
        try {
            if (objCachable) {
                removeFromCache(pk);
            }
            if (listCachable) {
                refreshList();
            }
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("refreshDel:" + e.getMessage());
        }
    }

    /**
     * 每个节点有两个Cache，一是本身，另一个是用于存储其孩子结点的cache
     * @param pk PrimaryKey
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

    /**
     * 将数据提取并缓存
     * @param pk PrimaryKey
     * @return ObjectDb
     */
    public ObjectDb getObjectDb(PrimaryKey pk) {
        ObjectDb obj = null;
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

    /**
     *
     * @param sql String
     * @return int -1 表示sql语句不合法
     */
    public int getObjectCount(String sql) {
        //根据sql语句得出计算总数的sql查询语句
        String query = SQLFilter.getCountSql(sql);
        if (!SecurityUtil.isValidSql(query))
            return -1;
        Integer count = null;
        if (listCachable) {
            try {
                count = (Integer) rmCache.getFromGroup(query, COUNT_GROUP_NAME);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e.getMessage());
            }
            // If already in cache, return the count.
            if (count != null) {
                return count.intValue();
            }
        }
        // Otherwise, we have to load the count from the db.
        int docCount = 0;
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            rs = conn.executeQuery(query);
            if (rs.next())
                docCount = rs.getInt(1);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        // Add the count to cache
        if (listCachable) {
            try {
                rmCache.putInGroup(query, COUNT_GROUP_NAME,
                                   new Integer(docCount));
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e.getMessage());
            }
        }
        return docCount;
    }

    /**
     * 取出缓存的主键放在数组中
     * @param sql String
     * @param startIndex int
     * @return Object[]
     */
    public Object[] getObjectBlock(String sql, int startIndex) {
        // First, discover what block number the results will be in.
        int blockSize = objectDb.getBlockSize();
        int blockID = startIndex / blockSize;
        int blockStart = blockID * blockSize;

        PrimaryKey primaryKey = objectDb.getPrimaryKey();
        // 取得根据主键的查询语句
        String pk = primaryKey.getName();
        // String query = "select " + pk + " " + SQLFilter.getFromSql(sql); // 当为联合查询时，此句中的pk会带来问题，因为缺少表的别名作为前缀 2006.6.9
        // String query = "select " + objectDb.getTableName() + "." + pk + " " + SQLFilter.getFromSql(sql); // 加表名作为前缀在oracle中也不行
        String query = sql;

        // 缓存所用的key
        String key = query + blockID;

        Object[] objArray = null;
        if (listCachable) {
            try {
                objArray = (Object[]) rmCache.getFromGroup(key,
                        group);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e.getMessage());
            }
            //If already in cache, return the block.
            if (objArray != null) {
                /**
                 * The actual block may be smaller than THREAD_BLOCK_SIZE. If that's
                 * the case, it means two things:
                 *  1) We're at the end boundary of all the results.
                 *  2) If the start index is greater than the length of the current
                 *     block, than there aren't really any results to return.
                 */
                Object[] objkeys = objArray;
                //当startIndex过大时
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
        Conn conn = new Conn(connname);
        ResultSet rs = null;
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
                    if (primaryKey.getType() == primaryKey.TYPE_LONG)
                        block.addElement(new Long(rs.getLong(1)));
                    else if (primaryKey.getType() == primaryKey.TYPE_INT)
                        block.addElement(new Integer(rs.getInt(1)));
                    else if (primaryKey.getType() == primaryKey.TYPE_STRING)
                        block.addElement(rs.getString(1));
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
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        int len = block.size();
        Object[] objkeys = new Object[len];
        int i = 0;
        Iterator ir = block.iterator();
        while (ir.hasNext()) {
            objkeys[i] = ir.next();
            i++;
        }

        // Add the block to cache
        if (listCachable) {
            try {
                rmCache.putInGroup(key, group, objkeys);
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

    public boolean isListCachable() {
        return listCachable;
    }

    public boolean isObjCachable() {
        return objCachable;
    }

    public boolean listCachable = true;
    public boolean objCachable = true;

}


