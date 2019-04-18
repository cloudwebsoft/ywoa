package com.redmoon.oa.vehicle;

import java.sql.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import java.util.Vector;

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
public class VehicleDriverDb extends ObjectDb{
    public VehicleDriverDb() {
         init();
    }

    public VehicleDriverDb(int id) {
        this.id = id;
        init();
        load();
    }

    public VehicleDriverDb getVehicleDriverDb(int id) {
        return (VehicleDriverDb) getObjectDb(new Integer(id));
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new VehicleDriverDb(pk.getIntValue());
    }

    public void initDB() {
        this.tableName = "vehicle_Driver";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new VehicleDriverCache(this);

        this.QUERY_DEL =
                "delete FROM " + tableName + " WHERE id=?";
        this.QUERY_CREATE =
                "INSERT into " + tableName + " (username) VALUES (?)";
        this.QUERY_LOAD =
                "SELECT id,username FROM " +
                tableName + " WHERE id=?";
        this.QUERY_SAVE =
                "UPDATE " + tableName + " SET username=? WHERE id=?";
        this.QUERY_LIST = "SELECT id FROM " + tableName;
        isInitFromConfigDB = false;
    }

    public boolean create() throws ErrMsgException {
        Conn conn = null;
        boolean re = false;
        String[] str = null;
        try {
            conn = new Conn(connname);
            PreparedStatement pstmt = conn.prepareStatement(this.QUERY_CREATE);
            pstmt.setString(1,userName);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                VehicleDriverCache vdc = new VehicleDriverCache(this);
                vdc.refreshCreate();
            }
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ErrMsgException("插入vehicle_Driver时出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public boolean save() throws ErrMsgException {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        boolean re = false;
        try {
            pstmt = conn.prepareStatement(QUERY_SAVE);
            pstmt.setString(1, userName);
            pstmt.setInt(2, id);
            re = conn.executePreUpdate() > 0 ? true : false;
            if (re) {
                VehicleDriverCache vdc = new VehicleDriverCache(this);
                primaryKey.setValue(new Integer(id));
                vdc.refreshSave(primaryKey);
            }
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public void load() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(this.QUERY_LOAD);
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs.next()) {
                this.userName = rs.getString("username");
                loaded = true;
                primaryKey.setValue(new Integer(id));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
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
    }

    public boolean del() throws ErrMsgException {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        boolean re = false;
        try {
            pstmt = conn.prepareStatement(QUERY_DEL);
            pstmt.setInt(1, id);
            re = conn.executePreUpdate() > 0 ? true : false;
            if (re) {
                VehicleDriverCache vdc = new VehicleDriverCache(this);
                primaryKey.setValue(new Integer(id));
                vdc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            logger.error("del:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;

    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    private String userName;
    private int id;

}
