package com.redmoon.forum.plugin.activity;

import java.sql.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;

/**
 *
 * <p>Title: </p>
 *
 * <p>Description:存放贴子的属性，msgRootId,state等 </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ActivityDb extends ObjectDb {
    public ActivityDb() {
        super();
    }

    public ActivityDb(long msgId) {
        this.msgId = msgId;
        init();
        load();
    }

    public void initDB() {
        this.tableName = "plugin_activity";
        primaryKey = new PrimaryKey("msgId", PrimaryKey.TYPE_LONG);
        objectCache = new ActivityCache(this);

        this.QUERY_CREATE = "insert into plugin_activity (msg_id,organizer,user_count,expire_date,money_code,attend_money_count,exit_money_count,tel,user_level) values (?,?,?,?,?,?,?,?,?)";
        this.QUERY_SAVE = "update plugin_activity set organizer=?,user_count=?,expire_date=?,money_code=?,attend_money_count=?,exit_money_count=?,tel=?,user_level=?,users=? where msg_id=?";
        this.QUERY_DEL = "delete from plugin_activity where msg_id=?";
        this.QUERY_LOAD =
            "select organizer,user_count,expire_date,money_code,attend_money_count,exit_money_count,tel,user_level,users from plugin_activity where msg_id=?";
        isInitFromConfigDB = false;
    }

    public boolean del() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_DEL);
            ps.setLong(1, msgId);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("del:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (rowcount > 0) {
            ActivityCache cc = new ActivityCache(this);
            primaryKey.setValue(new Long(msgId));
            cc.refreshDel(primaryKey);
        }
        return rowcount>0? true:false;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new ActivityDb(pk.getLongValue());
    }

    public boolean create() throws ResKeyException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            // this.QUERY_CREATE = "insert into plugin_activity (msg_id,organizer,user_count,expire_date,money_code,attend_money_count,exit_money_count,tel,user_level_type,user_level_count) values (?,?,?,?,?,?,?,?,?,?)";
            ps.setLong(1, msgId);
            ps.setString(2, organizer);
            ps.setInt(3, userCount);
            ps.setString(4, DateUtil.toLongString(expireDate));
            ps.setString(5, moneyCode);
            ps.setInt(6, attendMoneyCount);
            ps.setInt(7, exitMoneyCount);
            ps.setString(8, tel);
            ps.setInt(9, userLevel);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ResKeyException(new SkinUtil(), SkinUtil.ERR_DB);
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
            ActivityCache uc = new ActivityCache(this);
            uc.refreshCreate();
        }
        return rowcount>0? true:false;
    }

    public boolean save() throws ResKeyException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            // "update plugin_activity set organizer=?,user_count=?,expire_date=?,money_code=?,attend_money_count=?,exit_money_count=?,tel=?,user_level_type=?,user_level_count=? where msg_id=?";
            PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
            ps.setString(1, organizer);
            ps.setInt(2, userCount);
            ps.setString(3, DateUtil.toLongString(expireDate));
            ps.setString(4, moneyCode);
            ps.setInt(5, attendMoneyCount);
            ps.setInt(6, exitMoneyCount);
            ps.setString(7, tel);
            ps.setInt(8, userLevel);
            ps.setString(9, users);
            ps.setLong(10, msgId);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB);
        } finally {
            ActivityCache uc = new ActivityCache(this);
            primaryKey.setValue(new Long(msgId));
            uc.refreshSave(primaryKey);
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    public ActivityDb getActivityDb(long id) {
        return (ActivityDb)getObjectDb(new Long(id));
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            // "select organizer,user_count,expire_date,money_code,attend_money_count,exit_money_count,tel,user_level_type,user_level_count from plugin_activity where msg_id=?";
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setLong(1, msgId);
            primaryKey.setValue(new Long(msgId));
            rs = conn.executePreQuery();
            if (rs.next()) {
                organizer = rs.getString(1);
                userCount = rs.getInt(2);
                expireDate = DateUtil.parse(rs.getString(3));
                moneyCode = StrUtil.getNullStr(rs.getString(4));
                attendMoneyCount = rs.getInt(5);
                exitMoneyCount = rs.getInt(6);
                tel = StrUtil.getNullStr(rs.getString(7));
                userLevel = rs.getInt(8);
                users = StrUtil.getNullString(rs.getString(9));
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public void setMoneyCode(String moneyCode) {
        this.moneyCode = moneyCode;
    }

    public void setAttendMoneyCount(int attendMoneyCount) {
        this.attendMoneyCount = attendMoneyCount;
    }

    public void setExitMoneyCount(int exitMoneyCount) {
        this.exitMoneyCount = exitMoneyCount;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public void setExpireDate(java.util.Date expireDate) {
        this.expireDate = expireDate;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public void setUserLevel(int userLevel) {
        this.userLevel = userLevel;
    }

    public void setUsers(String users) {
        this.users = users;
    }

    public long getMsgId() {
        return msgId;
    }

    public String getOrganizer() {
        return organizer;
    }

    public String getMoneyCode() {
        return moneyCode;
    }

    public int getAttendMoneyCount() {
        return attendMoneyCount;
    }

    public int getExitMoneyCount() {
        return exitMoneyCount;
    }

    public String getTel() {
        return tel;
    }

    public java.util.Date getExpireDate() {
        return expireDate;
    }

    public int getUserCount() {
        return userCount;
    }

    public int getUserLevel() {
        return userLevel;
    }

    public String getUsers() {
        return users;
    }

    private long msgId;
    private String organizer;
    private String moneyCode;
    private int attendMoneyCount = 0;
    private int exitMoneyCount = 0;
    private String tel;
    private java.util.Date expireDate;
    private int userCount;
    private int userLevel = 0;
    private String users;

}
