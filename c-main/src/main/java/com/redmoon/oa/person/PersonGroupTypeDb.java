package com.redmoon.oa.person;

import java.sql.*;
import java.util.*;

import com.cloudwebsoft.framework.util.LogUtil;

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
public class PersonGroupTypeDb extends ObjectDb {
    private int id;

    public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_USER = 0;

    public PersonGroupTypeDb() {
        init();
    }

    public PersonGroupTypeDb(int id) {
        this.id = id;
        init();
        load();
    }

    public int getId() {
        return id;
    }

    public void initDB() {
        tableName = "oa_person_group_type";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new PersonGroupTypeCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE =
                "insert into " + tableName + " (name,orders,user_name) values (?,?,?)";
        QUERY_SAVE = "update " + tableName + " set name=?,orders=? where id=?";
        QUERY_LIST =
                "select id from " + tableName + " order by orders";
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select name,orders,user_name from " + tableName + " where id=?";
    }

    public PersonGroupTypeDb getPersonGroupTypeDb(int id) {
        return (PersonGroupTypeDb)getObjectDb(new Integer(id));
    }

    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setString(1, name);
            ps.setInt(2, orders);
            ps.setString(3, userName);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
            	PersonGroupTypeCache rc = new PersonGroupTypeCache(this);
                rc.refreshCreate();
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + StrUtil.trace(e));
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
            	PersonGroupTypeCache rc = new PersonGroupTypeCache(this);
                primaryKey.setValue(new Integer(id));
                rc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del: " + e.getMessage());
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
        return new PersonGroupTypeDb(pk.getIntValue());
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
                userName = rs.getString(3);
                loaded = true;
                primaryKey.setValue(new Integer(id));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load: " + e.getMessage());
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
            	 PersonGroupTypeCache rc = new PersonGroupTypeCache(this);
                 primaryKey.setValue(new Integer(id));
                 rc.refreshSave(primaryKey);
             }
         } catch (SQLException e) {
             LogUtil.getLog(getClass()).error("save: " + e.getMessage());
         } finally {
             if (conn != null) {
                 conn.close();
                 conn = null;
             }
         }
        return re;
    }
    
    public Vector listOfUser(String userName) {
    	String sql = "select id from " + tableName + " where user_name=" + StrUtil.sqlstr(userName) + " order by orders desc";
    	LogUtil.getLog(getClass()).info("listOfUser:" + sql);
    	return list(sql);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    private String name;
    
    private int orders = 0;
    
    private String userName;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getOrders() {
		return orders;
	}

	public void setOrders(int orders) {
		this.orders = orders;
	}


}
