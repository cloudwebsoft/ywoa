package cn.js.fan.base;

import cn.js.fan.web.Global;
import java.io.Serializable;
import cn.js.fan.resource.Constant;
import java.sql.ResultSet;
import java.sql.SQLException;
import cn.js.fan.db.Conn;
import java.util.Vector;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.db.KeyUnit;
import java.util.Iterator;
import java.util.HashMap;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.db.ListResult;
import cn.js.fan.db.SQLFilter;
import com.cloudwebsoft.framework.util.LogUtil;

public abstract class ObjectDb implements Serializable {
    public static final PrimaryKey[] EMPTY_BLOCK = new PrimaryKey[0];

    protected String connname = "";
    protected String QUERY_LOAD;
    protected String QUERY_DEL;
    protected String QUERY_SAVE;
    protected String QUERY_CREATE;
    protected String QUERY_LIST;

    protected boolean isInitFromConfigDB = true;

    protected String tableName = "";

    protected PrimaryKey primaryKey;
    protected ObjectCache objectCache;

    public ObjectDb() {
        init();
    }

    /**
     * 初始化，在构造函数中调用
     */
    public void init() {
        connname = Global.getDefaultDB();
        if ("".equals(connname)) {
            LogUtil.getLog(getClass()).info(Constant.DB_NAME_NOT_FOUND);
        }
        setQueryCreate();
        setQuerySave();
        setQueryDel();
        setQueryLoad();
        setQueryList();

        initDB();

        setPrimaryKey();
    }

    /**
     * 当从缓存中取出后，用以初始化transient的变量
     * @return Logger
     */
    public void renew() {
        if (objectCache!=null) {
            objectCache.renew();
        }
    }

    // 在cn.js.fan.module.Message中启用了
    public void initDB() {}

    public void setQueryCreate() {}
    public void setQuerySave() {};
    public void setQueryDel() {};
    public void setQueryLoad() {};
    public void setQueryList() {};

    /**
     * 置主键、SQL语句及是否使用对象缓存和列表缓存
     */
    public void setPrimaryKey() {
        if (!isInitFromConfigDB) {
            return;
        }
        DBConfig dc = new DBConfig();
        DBTable dt = dc.getDBTable(this.getClass().getName());
        if (dt == null) {
            // LogUtil.getLog(getClass()).info(this +" cann't find table defination in config file.");
            return;
        }
        this.tableName = dt.getName();
        this.primaryKey = (PrimaryKey)dt.getPrimaryKey().clone();

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
     * 从缓存中取ObjectBlock
     * @param query String
     * @param startIndex int
     * @return Object[] 存放的是对应于主键的值
     */
    public Object[] getObjectBlock(String query, int startIndex) {
        return objectCache.getObjectBlock(query, startIndex);
    }

    /**
     * 从缓存中取对象
     * @param primaryKeyValue Object
     * @return ObjectDb
     */
    public ObjectDb getObjectDb(Object primaryKeyValue) {
        // 只能用clone方法，因为如果new，则primaryKey中的keys会为null，当为复合主键时，如果setKeyValue时会出错
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setValue(primaryKeyValue);
        return objectCache.getObjectDb(pk);
    }

    /*
    public ObjectDb getObjectDb(Object primaryKeyValue) {
        primaryKey.setValue(primaryKeyValue);
        return objectCache.getObjectDb(primaryKey);
    }
    */

    /**
     * 从缓存中取对象列表
     * @param query String
     * @param startIndex int 索引开始值
     * @param endIndex int 索引结束值
     * @return ObjectBlockIterator 取得的最后一个对象的索引<=endIndex - 1
     */
    public ObjectBlockIterator getObjects(String query,
                                         int startIndex,
                                         int endIndex) {
        // 可能取得的infoBlock中的元素的顺序号小于endIndex
        Object[] blockValues = getObjectBlock(query, startIndex);
        return new ObjectBlockIterator(this, blockValues, query,
                                    startIndex, endIndex);
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

    public boolean create() throws ErrMsgException, ResKeyException {
        return false;
    }

    abstract public void load() throws ErrMsgException, ResKeyException;
    abstract public boolean save() throws ErrMsgException, ResKeyException;
    abstract public boolean del() throws ErrMsgException, ResKeyException;
    /**
     * 从数据库中取得对象
     * @param pk PrimaryKey
     * @return Object
     */
    abstract public ObjectDb getObjectRaw(PrimaryKey pk);

    /**
     * 从缓存中根据sql语句获取对象的数量
     * @param sql String SQL语句
     * @return int
     */
    public int getObjectCount(String sql) {
        return objectCache.getObjectCount(sql);
    }

    public boolean loaded = false;

    public Vector list() {
        return list(QUERY_LIST);
    }

    /**
     * 全部的记录列表
     * @return Vector
     */
    public Vector list(String QUERY_LIST) {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();
        Conn conn = new Conn(connname);
        try {
            // 取得总记录条数
            total = getObjectCount(QUERY_LIST);
            conn.prepareStatement(QUERY_LIST);
            if (total != 0) {
                // sets the limit of the maximum nuber of rows in a ResultSet object
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
                    if (primaryKey.getType()==PrimaryKey.TYPE_INT) {
                        result.addElement(getObjectDb(new Integer(rs.getInt(1))));
                    } else if (primaryKey.getType()==PrimaryKey.TYPE_STRING) {
                        result.addElement(getObjectDb(rs.getString(1)));
                    } else if (primaryKey.getType()==PrimaryKey.TYPE_LONG) {
                        result.addElement(getObjectDb(new Long(rs.getLong(1))));
                    } else if (primaryKey.getType() == PrimaryKey.TYPE_DATE) {
                        result.addElement(getObjectDb(new java.util.Date(rs.getTimestamp(1).getTime())));
                    } else if (primaryKey.getType() == PrimaryKey.TYPE_COMPOUND) {
                        HashMap keys = ((PrimaryKey)primaryKey.clone()).getKeys();
                        Iterator ir = keys.keySet().iterator();
                        while (ir.hasNext()) {
                            String keyName = (String) ir.next();
                            KeyUnit ku = (KeyUnit) keys.get(keyName);
                            if (ku.getType() == PrimaryKey.TYPE_INT) {
                                ku.setValue(new Integer(rs.getInt(ku.getOrders() + 1)));
                            } else if (ku.getType() == PrimaryKey.TYPE_LONG) {
                                ku.setValue(new Long(rs.getLong(ku.getOrders() + 1)));
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
                } while(rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("list: " + e.getMessage());
        } finally {
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
        Conn conn = new Conn(connname);
        try {
            // 取得总记录条数
            total = getObjectCount(sql);
            conn.prepareStatement(sql);
            if (total != 0)
                // sets the limit of the maximum number of rows in a ResultSet object
                conn.setMaxRows(end + 1); // 尽量减少内存的使用
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
                    if (primaryKey.getType()==PrimaryKey.TYPE_INT)
                        result.addElement(getObjectDb(new Integer(rs.getInt(1))));
                    else if (primaryKey.getType()==PrimaryKey.TYPE_STRING)
                        result.addElement(getObjectDb(rs.getString(1)));
                    else if (primaryKey.getType()==PrimaryKey.TYPE_LONG) {
                        result.addElement(getObjectDb(new Long(rs.getLong(1))));
                    }
                    else if (primaryKey.getType() == primaryKey.TYPE_DATE)
                        result.addElement(getObjectDb(new java.util.Date(rs.getTimestamp(1).getTime())));
                    else if (primaryKey.getType() == PrimaryKey.TYPE_COMPOUND) {
                        HashMap keys = ((PrimaryKey)primaryKey.clone()).getKeys();
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
                        result.addElement(getObjectDb(keys));
                    }
                    k++;
                    //if (k>=count)
                    //    break;
                } while(rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("list: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }

    /**
     * 分页，不使用缓存
     * @param listsql String
     * @param curPage int 当前页
     * @param pageSize int 每页条数
     * @return ListResult
     * @throws ErrMsgException
     */
    public ListResult listResult(String listsql, int curPage, int pageSize) throws
             ErrMsgException {
         int total = 0;
         ResultSet rs = null;
         Vector result = new Vector();

         ListResult lr = new ListResult();
         lr.setTotal(total);
         lr.setResult(result);

         Conn conn = new Conn(connname);
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

             conn.prepareStatement(listsql);

             if (total != 0) {
                 conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用
             }

             // rs = conn.executeQuery(listsql); // MySQL中效率很低，70万行的数据，原本30毫秒的数据，需要2秒多才能查出
             rs = conn.executePreQuery();

             if (rs == null) {
                 return lr;
             } else {
                 rs.setFetchSize(pageSize);
                 int absoluteLocation = pageSize * (curPage - 1) + 1;
                 if (rs.absolute(absoluteLocation) == false) {
                     return lr;
                 }
                 do {
                     if (primaryKey.getType()==PrimaryKey.TYPE_INT) {
                         result.addElement(getObjectDb(new Integer(rs.getInt(1))));
                     }
                     else if (primaryKey.getType()==PrimaryKey.TYPE_STRING) {
                         result.addElement(getObjectDb(rs.getString(1)));
                     } else if (primaryKey.getType()==PrimaryKey.TYPE_LONG) {
                         result.addElement(getObjectDb(new Long(rs.getLong(1))));
                     } else if (primaryKey.getType() == PrimaryKey.TYPE_DATE) {
                         result.addElement(getObjectDb(new java.util.Date(rs.getTimestamp(1).getTime())));
                     } else if (primaryKey.getType() == PrimaryKey.TYPE_COMPOUND) {
                         HashMap keys = ((PrimaryKey)primaryKey.clone()).getKeys();
                         Iterator ir = keys.keySet().iterator();
                         while (ir.hasNext()) {
                             String keyName = (String) ir.next();
                             KeyUnit ku = (KeyUnit) keys.get(keyName);
                             if (ku.getType() == PrimaryKey.TYPE_INT) {
                                 ku.setValue(new Integer(rs.getInt(ku.getOrders() + 1)));
                             } else if (ku.getType() == PrimaryKey.TYPE_LONG ) {
                                 ku.setValue(new Long(rs.getLong(ku.getOrders() + 1)));
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
             throw new ErrMsgException("Db error.");
         } finally {
             if (rs != null) {
                 try {
                     rs.close();
                 } catch (Exception e) {}
             }
             conn.close();
         }

         lr.setResult(result);
         lr.setTotal(total);
         return lr;
    }

    /**
     * 置缓存中block的大小
     * @param blockSize int
     */
    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public String getTableName() {
        return tableName;
    }

    private int blockSize = 100;
}
