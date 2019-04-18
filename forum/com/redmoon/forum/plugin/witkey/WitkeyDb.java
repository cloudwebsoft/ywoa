package com.redmoon.forum.plugin.witkey;

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
public class WitkeyDb extends ObjectDb {
    public static final int WITKEY_STATUS_OVER = 1; //任务完成
    public static final int WITKEY_STATUS_DELAY = 2; //任务延期
    public static final int WITKEY_STATUS_CHOOSEDRAFT = 3; //任务选稿
    public static final int WITKEY_STATUS_PAY = 4; //任务等待支付


    public WitkeyDb() {
        super();
    }

    public WitkeyDb(long msgRootId) {
        this.msgRootId = msgRootId;
        init();
        load();
    }

    public void initDB() {
        this.tableName = "plugin_witkey";
        primaryKey = new PrimaryKey("msgRootId", PrimaryKey.TYPE_LONG);
        objectCache = new WitkeyCache(this);

        this.QUERY_CREATE = "insert into plugin_witkey(msg_root_id, catalog_code, money_code, score, city, end_date, level, user_name, contribution_count, user_count, contact, msg_id) values (?,?,?,?,?,?,?,?,?,?,?,?)";
        this.QUERY_SAVE = "update plugin_witkey set catalog_code=?, money_code=?, score=?, city=?, end_date=?, level=?, user_name=?, contribution_count=?, user_count=?, contact=?, status=?, msg_id=? where msg_root_id=?";
        this.QUERY_DEL = "delete from plugin_witkey where msg_root_id=?";
        this.QUERY_LOAD =
            "select catalog_code, money_code, score, city, end_date, level, user_name, contribution_count, user_count, contact, status, msg_id from plugin_witkey where msg_root_id=?";
        isInitFromConfigDB = false;
    }

    public boolean del() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_DEL);
            ps.setLong(1, msgRootId);
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
            WitkeyCache gc = new WitkeyCache(this);
            primaryKey.setValue(new Long(msgRootId));
            gc.refreshDel(primaryKey);
        }
        return rowcount>0? true:false;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new WitkeyDb(pk.getLongValue());
    }

    public boolean create() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setLong(1, msgRootId);
            ps.setString(2, catalogCode);
            ps.setString(3, moneyCode);
            ps.setInt(4, score);
            ps.setString(5, city);
            ps.setString(6, endDate);
            ps.setInt(7, level);
            ps.setString(8, userName);
            ps.setInt(9, contributionCount);
            ps.setInt(10, userCount);
            ps.setString(11, contact);
            ps.setLong(12, msgId);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
            WitkeyCache gc = new WitkeyCache(this);
            gc.refreshCreate();
        }
        return rowcount>0? true:false;
    }

    public boolean save() throws ResKeyException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
            ps.setString(1, catalogCode);
            ps.setString(2, moneyCode);
            ps.setInt(3, score);
            ps.setString(4, city);
            ps.setString(5, endDate);
            ps.setInt(6, level);
            ps.setString(7, userName);
            ps.setInt(8, contributionCount);
            ps.setInt(9, userCount);
            ps.setString(10, contact);
            ps.setInt(11, status);
            ps.setLong(12, msgId);
            ps.setLong(13, msgRootId);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB);
        } finally {
            WitkeyCache gc = new WitkeyCache(this);
            primaryKey.setValue(new Long(msgRootId));
            gc.refreshSave(primaryKey);
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    public WitkeyDb getWitkeyDb(long id) {
        return (WitkeyDb)getObjectDb(new Long(id));
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setLong(1, msgRootId);
            primaryKey.setValue(new Long(msgRootId));
            rs = conn.executePreQuery();
            if (rs.next()) {
                catalogCode = rs.getString(1);
                moneyCode = rs.getString(2);
                score = rs.getInt(3);
                city = rs.getString(4);
                endDate = rs.getString(5);
                level = rs.getInt(6);
                userName = rs.getString(7);
                contributionCount = rs.getInt(8);
                userCount = rs.getInt(9);
                contact = rs.getString(10);
                status = rs.getInt(11);
                msgId = rs.getLong(12);
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

    public void setMsgRootId(long msgRootId) {
        this.msgRootId = msgRootId;
    }

    public void setCatalogCode(String catalogCode) {
        this.catalogCode = catalogCode;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setMoneyCode(String moneyCode) {
        this.moneyCode = moneyCode;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setContributionCount(int contributionCount) {
        this.contributionCount = contributionCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public long getMsgRootId() {
        return msgRootId;
    }

    public String getCatalogCode() {
        return catalogCode;
    }

    public int getScore() {
        return score;
    }

    public String getCity() {
        return city;
    }

    public String getMoneyCode() {
        return moneyCode;
    }

    public String getEndDate() {
        return endDate;
    }

    public int getLevel() {
        return level;
    }

    public String getUserName() {
        return userName;
    }

    public int getContributionCount() {
        return contributionCount;
    }

    public int getUserCount() {
        return userCount;
    }

    public String getContact() {
        return contact;
    }

    public int getStatus() {
        return status;
    }

    public long getMsgId() {
        return msgId;
    }

    private long msgRootId;
    private String catalogCode;
    private int score;
    private String city;
    private String moneyCode;
    private String endDate;
    private int level;
    private String userName;
    private int contributionCount;
    private int userCount;
    private String contact;
    private int status;
    private long msgId = -1;
}
