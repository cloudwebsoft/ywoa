package com.redmoon.forum.security;

import java.sql.*;
import java.util.Date;
import java.util.Vector;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.Conn;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;

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
public class ForbidIPDb extends ObjectDb {
    public ForbidIPDb() {
    }


    public ForbidIPDb(String ip) {
        this.ip = ip;
        init();
        load();
    }

    public ForbidIPDb getForbidIPDb(String ip) {
        return (ForbidIPDb)getObjectDb(ip);
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new ForbidIPDb(pk.getStrValue());
    }

    public void initDB() {
        this.tableName = "sq_forbid_ip";
        primaryKey = new PrimaryKey("ip", PrimaryKey.TYPE_STRING);
        objectCache = new ForbidIPCache(this);

        this.QUERY_DEL =
                "delete FROM " + tableName + " WHERE ip=?";
        this.QUERY_CREATE =
                "INSERT into " + tableName + " (ip,user_name,add_date,reason) VALUES (?,?,?,?)";
        this.QUERY_LOAD =
                "SELECT user_name,add_date,reason FROM " + tableName + " WHERE ip=?";
        this.QUERY_SAVE =
                "UPDATE " + tableName + " SET user_name=?,reason=? WHERE ip=?";
        this.QUERY_LIST = "select ip from " + tableName + " order by add_date desc";
        isInitFromConfigDB = false;
    }

    public boolean save() throws ErrMsgException {
        return false;
    }

    public void load() {
        // Based on the id in the object, get the message data from the database.
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        // "SELECT user_name,add_date,reason FROM " + tableName + " WHERE ip=?";
        Conn conn = new Conn(connname);
        try {
            pstmt = conn.prepareStatement(this.QUERY_LOAD);
            pstmt.setString(1, ip);
            //url,title,image,userName,sort,kind
            rs = conn.executePreQuery();
            if (rs.next()) {
                userName = rs.getString(1);
                try {
                    addDate = DateUtil.parse(rs.getString(2));
                }
                catch (Exception e) {

                }
                reason = rs.getString(3);
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {}
                pstmt = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean del() throws ErrMsgException {
        Conn conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = new Conn(connname);
            pstmt = conn.prepareStatement(this.QUERY_DEL);
            pstmt.setString(1, ip);
            if (conn.executePreUpdate()==1) {
                ForbidIPCache mc = new ForbidIPCache(this);
                mc.refreshDel(primaryKey);
                return true;
            }
            else
                return false;
        } catch (SQLException e) {
            logger.error("del:" + e.getMessage());
            throw new ErrMsgException("Error db operate.");
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {}
                pstmt = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean create() throws ErrMsgException {
        Conn conn = null;
        boolean re = false;
        PreparedStatement pstmt = null;
        // "INSERT into " + tableName + " (ip,user_name,add_date,reason) VALUES (?,?,?,?)";
        try {
            conn = new Conn(connname);
            pstmt = conn.prepareStatement(this.QUERY_CREATE);
            pstmt.setString(1, ip);
            pstmt.setString(2, userName);
            pstmt.setString(3, "" + System.currentTimeMillis());
            pstmt.setString(4, reason);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                ForbidIPCache mc = new ForbidIPCache(this);
                mc.refreshCreate();
            }
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ErrMsgException("Error db operate.");
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {}
                pstmt = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public String getIp() {
        return ip;
    }

    public String getUserName() {
        return userName;
    }

    public Date getAddDate() {
        return addDate;
    }

    public String getReason() {
        return reason;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setAddDate(Date addDate) {
        this.addDate = addDate;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    private String ip;
    private String userName;
    private Date addDate;
    private String reason;

}
