package com.redmoon.forum.treasure;

import java.util.Date;

import cn.js.fan.base.ObjectCache;
import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import java.util.HashMap;
import cn.js.fan.db.KeyUnit;
import cn.js.fan.db.Conn;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import cn.js.fan.util.DateUtil;

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
public class TreasureUserDb extends ObjectDb {
    public TreasureUserDb() {
    }

    public TreasureUserDb(String userName, String treasureCode) {
        this.userName = userName;
        this.treasureCode = treasureCode;
        init();
        load();
    }

    public int delTreasureOfUser(String userName) throws ErrMsgException {
        int count = 0;
        String sql = "select treasureCode from " + tableName + " where userName=?";
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userName);
            rs = conn.executePreQuery();
            while (rs.next()) {
                getTreasureUserDb(userName, rs.getString(1)).del();
                count++;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return count;
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
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_DEL);
            ps.setString(1, userName);
            ps.setString(2, treasureCode);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    /**
     *
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        // logger.info("pk=" + pk.toString());
        return new TreasureUserDb(pk.getKeyStrValue("userName"), pk.getKeyStrValue("treasureCode"));
    }

    public TreasureUserDb getTreasureUserDb(String userName, String treasureCode) {
        primaryKey.setKeyValue("userName", userName);
        primaryKey.setKeyValue("treasureCode", treasureCode);
        return (TreasureUserDb)getObjectDb(primaryKey.getKeys());
    }

    public boolean create() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setString(1, userName);
            ps.setString(2, treasureCode);
            ps.setInt(3, amount);
            java.util.Date curDate = new java.util.Date();
            ps.setString(4, DateUtil.toLongString(curDate));
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
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
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setString(1, userName);
            ps.setString(2, treasureCode);
            primaryKey.setKeyValue("userName", userName);
            primaryKey.setKeyValue("treasureCode", treasureCode);
            rs = conn.executePreQuery();

            if (rs.next()) {
                buyDate = DateUtil.parse(rs.getString(1));
                amount = rs.getInt(2);
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

    /**
     * save
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean save() {
            int rowcount = 0;
            Conn conn = null;
            try {
                conn = new Conn(connname);
                PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
                ps.setString(1, DateUtil.toLongString(buyDate));
                ps.setInt(2, amount);
                ps.setString(3, userName);
                ps.setString(4, treasureCode);
                rowcount = conn.executePreUpdate();

                primaryKey.setKeyValue("userName", userName);
                primaryKey.setKeyValue("treasureCode", treasureCode);
                objectCache.refreshSave(primaryKey);
            } catch (SQLException e) {
                logger.error(e.getMessage());
            } finally {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
        return rowcount>0? true:false;
    }

    public void initDB() {
        this.tableName = "sq_user_treasure";

        HashMap key = new HashMap();
        key.put("userName", new KeyUnit(primaryKey.TYPE_STRING, 0));
        key.put("treasureCode", new KeyUnit(primaryKey.TYPE_STRING, 1));
        primaryKey = new PrimaryKey(key);

        objectCache = new TreasureUserCache(this);

        this.QUERY_DEL =
                "delete FROM " + tableName + " WHERE userName=? and treasureCode=?";
        this.QUERY_CREATE =
                "INSERT into " + tableName + " (userName,treasureCode,amount,buyDate) VALUES (?,?,?,?)";
        this.QUERY_LOAD =
                "SELECT buyDate,amount FROM " + tableName + " WHERE userName=? and treasureCode=?";
        this.QUERY_SAVE =
                "UPDATE " + tableName + " SET buyDate=?,amount=? WHERE userName=? and treasureCode=?";
        isInitFromConfigDB = false;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setTreasureCode(String treasureCode) {
        this.treasureCode = treasureCode;
    }

    public void setBuyDate(Date buyDate) {
        this.buyDate = buyDate;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getUserName() {
        return userName;
    }

    public String getTreasureCode() {
        return treasureCode;
    }

    public Date getBuyDate() {
        return buyDate;
    }

    public int getAmount() {
        return amount;
    }

    private String userName;
    private String treasureCode;
    private java.util.Date buyDate;
    private int amount;
}
