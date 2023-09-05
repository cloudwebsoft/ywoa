package com.redmoon.oa.address;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.*;

/**
 * <p>Title: 通讯录类别</p>
 *
 * <p>Description: 已无用，被树形结构替代，但为了兼容手机客户端的版本更新而保留</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class AddressTypeDb extends ObjectDb {
    private int id;
    private String unitCode;
    
    public String getUnitCode() {
		return unitCode;
	}

	public void setUnitCode(String unitCode) {
		this.unitCode = unitCode;
	}

	public static final String PUBLIC = "public";

    public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_USER = 0;

    public AddressTypeDb() {
        init();
    }

    public AddressTypeDb(int id) {
        this.id = id;
        init();
        load();
    }

    public int getId() {
        return id;
    }

    public void initDB() {
        tableName = "address_type";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new AddressTypeCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE =
                "insert into " + tableName + " (id,name,USER_NAME,unit_code) values (?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set name=? where id=?";
        QUERY_LIST =
                "select id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select name,USER_NAME,unit_code from " + tableName + " where id=?";
    }

    public AddressTypeDb getAddressTypeDb(int id) {
        return (AddressTypeDb)getObjectDb(new Integer(id));
    }

    public boolean create() throws ErrMsgException {
        id = (int) SequenceManager.nextID(SequenceManager.OA_ADDRESS_GROUP);
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setString(3, userName);
            ps.setString(4, unitCode);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                AddressTypeCache rc = new AddressTypeCache(this);
                rc.refreshCreate();
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
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
                AddressTypeCache rc = new AddressTypeCache(this);
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
        return new AddressTypeDb(pk.getIntValue());
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
                userName = rs.getString(2);
                unitCode = rs.getString(3);
                loaded = true;
                primaryKey.setValue(new Integer(id));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load: " + e.getMessage());
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
             ps.setString(1, name);
             ps.setInt(2, id);
             re = conn.executePreUpdate()==1?true:false;
             if (re) {
                 AddressTypeCache rc = new AddressTypeCache(this);
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

    public boolean isExist(String name, String userName) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            rs = conn.executeQuery("select id from address_type where name =" +
                                   StrUtil.sqlstr(name) + " and USER_NAME=" +
                                   StrUtil.sqlstr(userName));
            if (rs.next())return true;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("list:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return false;
    }

    public boolean isExist(String name, String userName, String unitCode) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            rs = conn.executeQuery("select id from address_type where name =" +
                                   StrUtil.sqlstr(name) + " and USER_NAME=" +
                                   StrUtil.sqlstr(userName) + " and unit_code=" + StrUtil.sqlstr(unitCode));
            if (rs.next())return true;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("list:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return false;
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
                    result.addElement(getAddressTypeDb(rs.getInt(1)));
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("list:" + e.getMessage());
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

    public String getUserName() {
        return userName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    private String name;
    private String userName;

}
