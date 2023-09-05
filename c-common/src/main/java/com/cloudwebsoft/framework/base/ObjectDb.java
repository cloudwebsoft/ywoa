package com.cloudwebsoft.framework.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.Iterator;
import java.util.HashMap;

import com.alibaba.fastjson.annotation.JSONField;
import com.cloudwebsoft.framework.db.Connection;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.db.ListResult;
import cn.js.fan.web.Global;
import cn.js.fan.resource.Constant;
import cn.js.fan.db.KeyUnit;
import cn.js.fan.db.SQLFilter;
import com.cloudwebsoft.framework.util.LogUtil;

public abstract class ObjectDb implements IObjectDb {
    public static final PrimaryKey[] EMPTY_BLOCK = new PrimaryKey[0];

    @JSONField(serialize = false)
    public String connname = "";

    @JSONField(serialize = false)
    public String QUERY_LOAD;
    @JSONField(serialize = false)
    public String QUERY_DEL;
    @JSONField(serialize = false)
    public String QUERY_SAVE;
    @JSONField(serialize = false)
    public String QUERY_CREATE;
    @JSONField(serialize = false)
    public String QUERY_LIST;

    @JSONField(serialize = false)
    public boolean isInitFromConfigDB = true;

    @JSONField(serialize = false)
    protected String tableName = "";

    @JSONField(serialize = false)
    public PrimaryKey primaryKey;

    /**
     * 注意要忽略序列化，否则会导致stackoverlow，如：return new Result<>(DocTemplateDb.list())
     */
    @JSONField(serialize = false)
    public ObjectCache objectCache;

    public ObjectDb() {
        init();
    }

    public void init() {
        connname = Global.getDefaultDB();
        if (connname.equals("")) {
            LogUtil.getLog(getClass()).info(Constant.DB_NAME_NOT_FOUND);
        }

        initDB();

        initFromConfigDB();
    }

    /**
     * 当从缓存中取出后，用以初始化transient的变量
     * @return Logger
     */
    public void renew() {
        if (objectCache != null) {
            objectCache.renew();
        }
    }

    public void initDB() {
        isInitFromConfigDB = false;
    }

    public void initFromConfigDB() {
        if (!isInitFromConfigDB) {
            return;
        }
        DBConfig dc = new DBConfig();
        DBTable dt = dc.getDBTable(this.getClass().getName());
        if (dt == null) {
            LogUtil.getLog(getClass()).info(this +" cann't find table defination in config file.");
            return;
        }
        this.tableName = dt.getName();
        this.primaryKey = (PrimaryKey) dt.getPrimaryKey().clone();

        this.QUERY_CREATE = dt.getQueryCreate();
        this.QUERY_DEL = dt.getQueryDel();
        this.QUERY_LIST = dt.getQueryList();
        this.QUERY_LOAD = dt.getQueryLoad();
        this.QUERY_SAVE = dt.getQuerySave();
        this.objectCache = dt.getObjectCache(this);

        this.objectCache.setObjCachable(dt.isObjCachable());
        this.objectCache.setListCachable(dt.isListCachable());
    }

    /**
     * 从数据库中取出记录数
     * @param query String 已经过getCountSql转换
     * @return int -1 表示sql语句不合法
     */
    public int getObjectCountRaw(String query) {
        // 根据sql语句得出计算总数的sql查询语句
        // Otherwise, we have to load the count from the db.
        int docCount = 0;
        Connection conn = new Connection(connname);
        ResultSet rs = null;
        try {
            rs = conn.executeQuery(query);
            if (rs.next()) {
                docCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
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
        return docCount;
    }

    /**
     *
     * @param startIndex int
     * @return Object[] 存放的是对应于主键的值
     */
    // abstract public Object[] getObjectBlock(String sql, int startIndex);
    public Object[] getObjectBlock(String query, String groupKey,
                                   int startIndex) {
        return objectCache.getObjectBlock(query, groupKey, startIndex);
    }

    public IObjectDb getObjectDb(Object primaryKeyValue) {
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setValue(primaryKeyValue);
        return objectCache.getObjectDb(pk);
    }

    /*
    public IObjectDb getObjectDb(Object primaryKeyValue) {
        primaryKey.setValue(primaryKeyValue);
        return objectCache.getObjectDb(primaryKey);
    }
    */

    public ObjectBlockIterator getObjects(String query, String groupKey,
                                          int startIndex,
                                          int endIndex) {
        // 可能取得的infoBlock中的元素的顺序号小于endIndex
        Object[] blockValues = getObjectBlock(query, groupKey, startIndex);
        // for (int i=0; i<blockValues.length; i++)
        //     LogUtil.getLog(getClass()).info("getObjects i=" + i + " " + blockValues[i]);
        // LogUtil.getLog(getClass()).info(getClass() + " getObjects:" + groupKey + " blockValues.length=" + blockValues.length);
        return new ObjectBlockIterator(this, blockValues, query, groupKey,
                                       startIndex, endIndex);
    }

    public ObjectBlockIterator getObjects(String query,
                                          int startIndex,
                                          int endIndex) {
        return getObjects(query, "", startIndex, endIndex);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    abstract public boolean create(JdbcTemplate jt) throws ErrMsgException,
            ResKeyException;

    abstract public void load(JdbcTemplate jt) throws ErrMsgException,
            ResKeyException;

    abstract public boolean save(JdbcTemplate jt) throws ErrMsgException,
            ResKeyException;

    abstract public boolean del(JdbcTemplate jt) throws ErrMsgException,
            ResKeyException;

    /**
     * 从数据库中取得对象
     * @return Object
     */
    abstract public IObjectDb getObjectRaw(PrimaryKey pk);

    public int getObjectCount(String sql) {
        return objectCache.getObjectCount(sql, "");
    }

    public int getObjectCount(String sql, String groupName) {
        return objectCache.getObjectCount(sql, groupName);
    }

    public boolean loaded = false;

    public Vector list() {
        return list(QUERY_LIST);
    }

    /**
     * 全部的记录列表，当记录不多时，可以使用本方法，如列出友情链接，而当记录很多时，则不宜使用
     * @param QUERY_LIST String
     * @return Vector
     */
    public Vector list(String QUERY_LIST) {
        ResultSet rs = null;
        int total = 0;
        Vector result = new Vector();
        Connection conn = new Connection(connname);
        try {
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(QUERY_LIST);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            conn.prepareStatement(QUERY_LIST);
            if (total != 0) {
                // sets the limit of the maximum number of rows in a ResultSet object
                conn.setMaxRows(total); // 尽量减少内存的使用
            }
            rs = conn.executePreQuery();
            if (rs == null) {
                return result;
            } else {
                // defines the number of rows that will be read from the database when the ResultSet needs more rows
                rs.setFetchSize(total); // rs一次从POOL中所获取的记录数
                if (rs.absolute(1) == false) {
                    return result;
                }
                do {
                    if (primaryKey.getType() == PrimaryKey.TYPE_INT) {
                        result.addElement(getObjectDb(new Integer(rs.getInt(1))));
                    } else if (primaryKey.getType() == PrimaryKey.TYPE_STRING) {
                        result.addElement(getObjectDb(rs.getString(1)));
                    } else if (primaryKey.getType() == PrimaryKey.TYPE_LONG) {
                        result.addElement(getObjectDb(new Long(rs.getLong(1))));
                    }
                    else if (primaryKey.getType() == primaryKey.TYPE_DATE)
                        result.addElement(getObjectDb(new java.util.Date(rs.getTimestamp(1).getTime())));
                    else if (primaryKey.getType() == PrimaryKey.TYPE_COMPOUND) {
                        HashMap keys = ((PrimaryKey) primaryKey.clone()).
                                       getKeys();
                        Iterator ir = keys.keySet().iterator();
                        while (ir.hasNext()) {
                            String keyName = (String) ir.next();
                            KeyUnit ku = (KeyUnit) keys.get(keyName);
                            if (ku.getType() == primaryKey.TYPE_INT) {
                                ku.setValue(new Integer(rs.getInt(ku.getOrders() +
                                        1)));
                            } else if (ku.getType() == primaryKey.TYPE_LONG) {
                                ku.setValue(new Long(rs.getLong(ku.getOrders() +
                                        1)));
                            }
                            else if (ku.getType() == PrimaryKey.TYPE_DATE) {
                                ku.setValue(new java.util.Date(rs.getTimestamp(ku.getOrders() + 1).getTime()));
                            }
                            else {
                                ku.setValue(rs.getString(ku.getOrders() + 1));
                            }
                        }
                        result.addElement(getObjectDb(keys));
                    }
                } while (rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("list: " + e.getMessage());
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
        return result;
    }

    public Vector list(int start, int end) {
        return list(QUERY_LIST, start, end);
    }

    /**
     * 类似于Hiebernate中的list，从缓存中取出对象，但又有所区别，因为总记录条数也是取自缓存
     * 取得的记录在ResultSet中的索引为 start+1 ~ end+1，总共为end-start+1条
     * @param sql String
     * @param start int 从0开始算起
     * @param end int 当能够取满end-start+1条数据时，list的最后一条记录在ResultSet中的索引为end+1
     * @return Vector
     */
    public Vector list(String sql, int start, int end) {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();
        Connection conn = new Connection(connname);
        try {
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(sql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            conn.prepareStatement(sql);
            if (total != 0) {
                // sets the limit of the maximum number of rows in a ResultSet object
                conn.setMaxRows(end + 1); // 尽量减少内存的使用
            }
            rs = conn.executePreQuery();
            if (rs == null) {
                return result;
            } else {
                // defines the number of rows that will be read from the database when the ResultSet needs more rows
                int count = end - start + 1;
                rs.setFetchSize(count); // rs一次从POOL中所获取的记录数
                if (rs.absolute(start + 1) == false) {
                    return result;
                }

                int k = 0;
                do {
                    if (primaryKey.getType() == PrimaryKey.TYPE_INT) {
                        result.addElement(getObjectDb(new Integer(rs.getInt(1))));
                    } else if (primaryKey.getType() == PrimaryKey.TYPE_STRING) {
                        result.addElement(getObjectDb(rs.getString(1)));
                    } else if (primaryKey.getType() == PrimaryKey.TYPE_LONG) {
                        result.addElement(getObjectDb(new Long(rs.getLong(1))));
                    }
                    else if (primaryKey.getType() == primaryKey.TYPE_DATE)
                        result.addElement(getObjectDb(new java.util.Date(rs.getTimestamp(1).getTime())));
                    else if (primaryKey.getType() == PrimaryKey.TYPE_COMPOUND) {
                        HashMap keys = ((PrimaryKey) primaryKey.clone()).
                                       getKeys();
                        Iterator ir = keys.keySet().iterator();
                        while (ir.hasNext()) {
                            String keyName = (String) ir.next();
                            KeyUnit ku = (KeyUnit) keys.get(keyName);
                            if (ku.getType() == primaryKey.TYPE_INT) {
                                ku.setValue(new Integer(rs.getInt(ku.getOrders() +
                                        1)));
                            } else if (ku.getType() == primaryKey.TYPE_LONG) {
                                ku.setValue(new Long(rs.getLong(ku.getOrders() +
                                        1)));
                            }
                            else if (ku.getType() == PrimaryKey.TYPE_DATE) {
                                ku.setValue(new java.util.Date(rs.getTimestamp(ku.getOrders() + 1).getTime()));
                            }
                            else {
                                ku.setValue(rs.getString(ku.getOrders() + 1));
                            }
                        }
                        result.addElement(getObjectDb(keys));
                    }
                    k++;
                    //if (k>=count)
                    //    break;
                } while (rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("list: " + e.getMessage());
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
        return result;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public String getTableName() {
        return tableName;
    }

    public ListResult listResult(String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();

        ListResult lr = new ListResult();
        lr.setTotal(total);
        lr.setResult(result);

        Connection conn = new Connection(connname);
        try {
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(listsql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            // 防止受到攻击时，curPage被置为很大，或者很小
            int totalpages = (int) Math.ceil((double) total / pageSize);
            if (curPage > totalpages) {
                curPage = totalpages;
            }
            if (curPage <= 0) {
                curPage = 1;
            }

            if (total != 0) {
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用
            }

            rs = conn.executeQuery(listsql);
            if (rs == null) {
                return lr;
            } else {
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (rs.absolute(absoluteLocation) == false) {
                    return lr;
                }
                do {
                    if (primaryKey.getType() == PrimaryKey.TYPE_INT) {
                        result.addElement(getObjectDb(new Integer(rs.getInt(1))));
                    } else if (primaryKey.getType() == PrimaryKey.TYPE_STRING) {
                        result.addElement(getObjectDb(rs.getString(1)));
                    } else if (primaryKey.getType() == PrimaryKey.TYPE_LONG) {
                        result.addElement(getObjectDb(new Long(rs.getLong(1))));
                    }
                    else if (primaryKey.getType() == primaryKey.TYPE_DATE)
                        result.addElement(getObjectDb(new java.util.Date(rs.getTimestamp(1).getTime())));
                    else if (primaryKey.getType() == PrimaryKey.TYPE_COMPOUND) {
                        HashMap keys = ((PrimaryKey) primaryKey.clone()).
                                       getKeys();
                        Iterator ir = keys.keySet().iterator();
                        while (ir.hasNext()) {
                            String keyName = (String) ir.next();
                            KeyUnit ku = (KeyUnit) keys.get(keyName);
                            if (ku.getType() == primaryKey.TYPE_INT) {
                                ku.setValue(new Integer(rs.getInt(ku.getOrders() +
                                        1)));
                            } else if (ku.getType() == primaryKey.TYPE_LONG) {
                                ku.setValue(new Long(rs.getLong(ku.getOrders() +
                                        1)));
                            }
                            else if (ku.getType() == PrimaryKey.TYPE_DATE) {
                                ku.setValue(new java.util.Date(rs.getTimestamp(ku.getOrders() + 1).getTime()));
                            }
                            else {
                                ku.setValue(rs.getString(ku.getOrders() + 1));
                            }
                        }
                        result.addElement(getObjectDb(keys));
                    }
                } while (rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("listResult:" + e.getMessage());
            throw new ErrMsgException("数据库出错！");
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

        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }

    @JSONField(serialize = false)
    private int blockSize = 100;
}
