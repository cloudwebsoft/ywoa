package com.redmoon.oa.basic;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;

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
public class SelectKindDb extends ObjectDb {
    private int id;

    public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_USER = 0;

    public SelectKindDb() {
        init();
    }

    public SelectKindDb(int id) {
        this.id = id;
        init();
        load();
    }

    public int getId() {
        return id;
    }

    public void initDB() {
        tableName = "oa_select_kind";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new SelectKindCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE =
                "insert into " + tableName + " (name,orders) values (?,?)";
        QUERY_SAVE = "update " + tableName + " set name=?,orders=? where id=?";
        QUERY_LIST =
                "select id from " + tableName + " order by orders";
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select name,orders from " + tableName + " where id=?";
    }

    public SelectKindDb getSelectKindDb(int id) {
        return (SelectKindDb)getObjectDb(new Integer(id));
    }

    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setString(1, name);
            ps.setInt(2, orders);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                SelectKindCache rc = new SelectKindCache(this);
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
                SelectKindCache rc = new SelectKindCache(this);
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
        return new SelectKindDb(pk.getIntValue());
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
        // QUERY_LOAD = "select name,reason,direction,type,myDate from " + tableName + " where id=?";
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                name = rs.getString(1);
                orders = rs.getInt(2);
                loaded = true;
                primaryKey.setValue(new Integer(id));
            }
        } catch (SQLException e) {
            logger.error("load: " + e.getMessage());
        } finally {
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
             ps.setString(1, name);
             ps.setInt(2, orders);
             ps.setInt(3, id);
             re = conn.executePreUpdate()==1?true:false;
             if (re) {
                 SelectKindCache rc = new SelectKindCache(this);
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
                    result.addElement(getSelectKindDb(rs.getInt(1)));
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


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    private String name;
    
    private int orders = 0;

	public int getOrders() {
		return orders;
	}

	public void setOrders(int orders) {
		this.orders = orders;
	}


}
