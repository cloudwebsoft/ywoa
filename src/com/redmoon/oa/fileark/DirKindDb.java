package com.redmoon.oa.fileark;

import java.sql.*;
import java.util.*;

import com.cloudwebsoft.framework.db.JdbcTemplate;

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
public class DirKindDb extends ObjectDb {
    private int id;

    public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_USER = 0;

    public DirKindDb() {
        init();
    }

    public DirKindDb(int id) {
        this.id = id;
        init();
        load();
    }

    public int getId() {
        return id;
    }

    public void initDB() {
        tableName = "dir_kind";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new DirKindCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE =
                "insert into " + tableName + " (dir_code,kind,orders) values (?,?,?)";
        QUERY_SAVE = "update " + tableName + " set kind=?,orders=? where id=?";
        QUERY_LIST =
                "select id from " + tableName + " order by orders";
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select dir_code,kind,orders from " + tableName + " where id=?";
    }

    public DirKindDb getDirKindDb(int id) {
        return (DirKindDb)getObjectDb(new Integer(id));
    }
    
    public boolean isKindExist(String dirCode, String kind) {
    	String sql = "select id from dir_kind where dir_code=? and kind=?";
    	JdbcTemplate jt = new JdbcTemplate();
    	ResultIterator ri;
		try {
			ri = jt.executeQuery(sql, new Object[]{dirCode, kind});
	    	if (ri.hasNext())
	    		return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	return false;
    }

    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setString(1, dirCode);
            ps.setString(2, kind);
            ps.setInt(3, orders);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                DirKindCache rc = new DirKindCache(this);
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
                DirKindCache rc = new DirKindCache(this);
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
        return new DirKindDb(pk.getIntValue());
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
                dirCode = rs.getString(1);
                kind = rs.getString(2);
                orders = rs.getInt(3);
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
             ps.setString(1, kind);
             ps.setInt(2, orders);
             ps.setInt(3, id);
             re = conn.executePreUpdate()==1?true:false;
             if (re) {
                 DirKindCache rc = new DirKindCache(this);
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
    
    public Vector listOfDir(String dirCode) {
    	return list("select id from " + tableName + " where dir_code=" + StrUtil.sqlstr(dirCode) + " order by orders");
    }
   
    /*
    public Vector listOfAll() {
    	return list("select id from " + tableName + " order by orders");    	
    }
    */

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
                    result.addElement(getDirKindDb(rs.getInt(1)));
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


    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }


    private String kind;
    
    private int orders = 0;
    
    private String dirCode;

	public String getDirCode() {
		return dirCode;
	}

	public void setDirCode(String dirCode) {
		this.dirCode = dirCode;
	}

	public int getOrders() {
		return orders;
	}

	public void setOrders(int orders) {
		this.orders = orders;
	}


}
