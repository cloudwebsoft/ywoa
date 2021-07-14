package com.redmoon.forum.plugin.present;

import java.sql.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.forum.*;
import com.redmoon.forum.plugin.*;

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
public class PresentDb extends ObjectDb {
    public PresentDb() {
        super();
    }

    public PresentDb(long id) {
        this.id = id;
        init();
        load();
    }

    public void initDB() {
        this.tableName = "plugin_present";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_LONG);
        objectCache = new PresentCache(this);

        this.QUERY_CREATE = "insert into plugin_present (msg_id, user_name, money_code, score, give_date,id,reason) values (?,?,?,?,?,?,?)";
        // this.QUERY_SAVE = "update plugin_present set flow_count=?,egg_count=? where msg_id=?";
        this.QUERY_DEL = "delete from plugin_present where id=?";
        this.QUERY_LOAD =
            "select user_name, money_code, score, give_date, msg_id, reason from plugin_present where id=?";
        isInitFromConfigDB = false;
    }

    public boolean del() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_DEL);
            ps.setLong(1, id);
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
            PresentCache cc = new PresentCache(this);
            primaryKey.setValue(new Long(msgId));
            cc.refreshDel(primaryKey);
        }
        return rowcount>0? true:false;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new PresentDb(pk.getLongValue());
    }

    public boolean create() throws ResKeyException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            // msg_id, user_name, money_code, score, give_code
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setLong(1, msgId);
            ps.setString(2, userName);
            ps.setString(3, moneyCode);
            ps.setInt(4, score);
            ps.setString(5, "" + new java.util.Date().getTime());
            ps.setLong(6, SequenceMgr.nextID(SequenceMgr.PLUGIN_PRESENT));
            ps.setString(7, reason);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ResKeyException(new SkinUtil(), SkinUtil.ERR_DB);
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
            PresentCache uc = new PresentCache(this);
            uc.refreshCreate();
        }
        return rowcount>0? true:false;
    }

    public boolean save() throws ResKeyException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
            ps.setLong(3, msgId);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB);
        } finally {
            PresentCache uc = new PresentCache(this);
            primaryKey.setValue(new Long(msgId));
            uc.refreshSave(primaryKey);
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    public PresentDb getPresentDb(long id) {
        return (PresentDb)getObjectDb(new Long(id));
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            // user_name, money_code, score, give_code
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setLong(1, id);
            primaryKey.setValue(new Long(id));
            rs = conn.executePreQuery();
            if (rs.next()) {
                userName = rs.getString(1);
                moneyCode = rs.getString(2);
                score = rs.getInt(3);
                giveDate = DateUtil.parse(rs.getString(4));
                msgId = rs.getLong(5);
                reason = StrUtil.getNullStr(rs.getString(6));
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

    public ObjectBlockIterator listPresentOfMsg(long msgId) {
        String sql = "select id from " + tableName + " where msg_id=" + msgId + " order by id";
        int count = getObjectCount(sql);
        ObjectBlockIterator oi = getObjects(sql, 0, count);
        return oi;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setMoneyCode(String moneyCode) {
        this.moneyCode = moneyCode;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setGiveDate(Date giveDate) {
        this.giveDate = giveDate;
    }

    public long getMsgId() {
        return msgId;
    }

    public String getUserName() {
        return userName;
    }

    public int getScore() {
        return score;
    }

    public String getMoneyCode() {
        return moneyCode;
    }

    public java.util.Date getGiveDate() {
        return giveDate;
    }

    public String getReason() {
        return reason;
    }

    public boolean doGive(MsgDb md, String giver, String moneyCode, int score, String reason) throws ResKeyException {
        ScoreMgr sm = new ScoreMgr();
        ScoreUnit su = sm.getScoreUnit(moneyCode);

        // 送分
        boolean re = su.getScore().pay(giver, md.getName(), score);
        if (re) {
            PresentDb pd = new PresentDb();
            pd.setUserName(giver);
            pd.setMsgId(md.getId());
            pd.setMoneyCode(moneyCode);
            pd.setScore(score);
            pd.setReason(reason);
            re = pd.create();
        }
        return re;
    }

    private long msgId;
    private String userName;
    private int score;
    private String moneyCode;
    private java.util.Date giveDate;
    private long id;
    private String reason;

}
