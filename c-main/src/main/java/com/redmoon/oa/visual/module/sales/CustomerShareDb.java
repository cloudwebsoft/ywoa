package com.redmoon.oa.visual.module.sales;

import java.sql.*;
import java.util.*;

import com.cloudwebsoft.framework.util.LogUtil;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.Global;

/**
 * <p>Title: </p>
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
public class CustomerShareDb extends ObjectDb {
    private int id;
    public CustomerShareDb() {
        init();
    }

    public CustomerShareDb(int id) {
        this.id = id;
        init();
        load();
    }

    public int getId() {
        return id;
    }

    public void initDB() {
        tableName = "customer_share";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new CustomerShareCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE =
                "insert into " + tableName + " (customerId,sharePerson) values (?,?)";
        QUERY_SAVE = "update " + tableName + " set customerId=?,sharePerson=? where id=?";
        QUERY_LIST =
                "select id from " + tableName + " order by mydate desc";
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select customerId,sharePerson from " + tableName + " where id=?";
    }

    public CustomerShareDb getCustomerShareDb(int id) {
        return (CustomerShareDb)getObjectDb(new Integer(id));
    }

    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setInt(1, customerId);
            ps.setString(2, sharePerson);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                CustomerShareCache rc = new CustomerShareCache(this);
                rc.refreshCreate();
            }
        }
        catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ErrMsgException("数据库操作失败！");
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     * del
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean del() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                CustomerShareCache rc = new CustomerShareCache(this);
                primaryKey.setValue(new Integer(id));
                rc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            logger.error("del: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     *
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new CustomerShareDb(pk.getIntValue());
    }

    /**
     * load
     *
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
        // QUERY_LOAD = "select name,sharePerson,direction,type,myDate from " + tableName + " where id=?";
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                customerId = rs.getInt(1);
                sharePerson = rs.getString(2);
                loaded = true;
                primaryKey.setValue(new Integer(id));
            }
        } catch (SQLException e) {
            logger.error("load: " + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {}
                rs = null;
            }
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
    }

    /**
     * save
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean save() throws ErrMsgException {
        Conn conn = new Conn(connname);
         boolean re = false;
         try {
             PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
             ps.setInt(1, customerId);
             ps.setString(2, sharePerson);
             ps.setInt(3, id);
             re = conn.executePreUpdate()==1?true:false;

             if (re) {
                 CustomerShareCache rc = new CustomerShareCache(this);
                 primaryKey.setValue(new Integer(id));
                 rc.refreshSave(primaryKey);
             }
         } catch (SQLException e) {
             logger.error("save: " + e.getMessage());
         } finally {
             if (conn != null) {
                 conn.close();
                 conn = null;
             }
         }
        return re;
    }

    /**
     * 取出全部信息置于result中
     */
    public Vector list(String sql) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        try {
            rs = conn.executeQuery(sql);
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    result.addElement(getCustomerShareDb(rs.getInt(1)));
                }
            }
        } catch (SQLException e) {
            logger.error("list:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }
    
    /**
     * 判断是否某用户是否共享了客户
     * @param person
     * @param customerId
     * @return
     */
    public static boolean isExist(String person, long customerId) {
        ResultSet rs = null;
        String sql =  "select id from customer_share where sharePerson=? and customerId=?";
        Conn conn = new Conn(Global.getDefaultDB());
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, person);
            ps.setLong(2, customerId);
            rs = conn.executePreQuery();
            if (rs!=null && rs.next())
                return true;
        } catch (SQLException e) {
            LogUtil.getLog(CustomerShareDb.class).error("isExist:" + StrUtil.trace(e));
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return false;
    }

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

            if (total != 0)
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用

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
                    CustomerShareDb ug = getCustomerShareDb(rs.getInt(1));
                    result.addElement(ug);
                } while (rs.next());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("数据库出错！");
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {e.printStackTrace();}
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

    public int getCustomerId() {
        return customerId;
    }

    public String getSharePerson() {
        return sharePerson;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public void setSharePerson(String sharePerson) {
        this.sharePerson = sharePerson;
    }

    private int customerId;
    private String sharePerson;
}
