package com.redmoon.forum.plugin.flower;

import java.sql.*;
import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.forum.MsgDb;
import com.redmoon.forum.plugin.ScoreMgr;
import com.redmoon.forum.plugin.ScoreUnit;
import com.redmoon.forum.person.UserPropDb;
import com.redmoon.forum.plugin.base.IPluginScore;

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
public class FlowerDb extends ObjectDb {
    public FlowerDb() {
        super();
    }

    public FlowerDb(long msgId) {
        this.msgId = msgId;
        init();
        load();
    }

    public void initDB() {
        this.tableName = "plugin_flower";
        primaryKey = new PrimaryKey("msgId", PrimaryKey.TYPE_LONG);
        objectCache = new FlowerCache(this);

        this.QUERY_CREATE = "insert into plugin_flower (msg_id, flow_count, egg_count) values (?, ?, ?)";
        this.QUERY_SAVE = "update plugin_flower set flow_count=?,egg_count=? where msg_id=?";
        this.QUERY_DEL = "delete from plugin_flower where msg_id=?";
        this.QUERY_LOAD =
            "select flow_count, egg_count from plugin_flower where msg_id=?";
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
            FlowerCache cc = new FlowerCache(this);
            primaryKey.setValue(new Long(msgId));
            cc.refreshDel(primaryKey);
        }
        return rowcount>0? true:false;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new FlowerDb(pk.getLongValue());
    }

    public boolean create() throws ResKeyException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setLong(1, msgId);
            ps.setInt(2, flowerCount);
            ps.setInt(3, eggCount);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ResKeyException(new SkinUtil(), SkinUtil.ERR_DB);
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
            FlowerCache uc = new FlowerCache(this);
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
            ps.setInt(1, flowerCount);
            ps.setInt(2, eggCount);
            ps.setLong(3, msgId);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB);
        } finally {
            FlowerCache uc = new FlowerCache(this);
            primaryKey.setValue(new Long(msgId));
            uc.refreshSave(primaryKey);
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    public FlowerDb getFlowerDb(long id) {
        return (FlowerDb)getObjectDb(new Long(id));
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setLong(1, msgId);
            primaryKey.setValue(new Long(msgId));
            rs = conn.executePreQuery();
            if (rs.next()) {
                flowerCount = rs.getInt(1);
                eggCount = rs.getInt(2);
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

    public void setEggCount(int eggCount) {
        this.eggCount = eggCount;
    }

    public long getMsgId() {
        return msgId;
    }

    public int getFlowerCount() {
        return flowerCount;
    }

    public int getEggCount() {
        return eggCount;
    }

    public boolean doGive(MsgDb curMsgDb, String giver, int type) throws ResKeyException {
        FlowerConfig fc = new FlowerConfig();
        String moneyCode = fc.getProperty("moneyCode");
        ScoreMgr sm = new ScoreMgr();
        ScoreUnit su = sm.getScoreUnit(moneyCode);

        int score = 0;
        if (type==1)
            score = fc.getIntProperty("flower");
        else
            score = fc.getIntProperty("egg");
        boolean re = su.getScore().pay(giver, IPluginScore.SELLER_SYSTEM,  score);
        if (re) {
            if (type==1) {
                flowerCount ++;
            }
            else {
                eggCount ++;
            }
            re = save();
            if (re) {
                UserPropDb up = new UserPropDb();
                up = up.getUserPropDb(curMsgDb.getName());
                if (type==1)
                    up.set("flower_count", new Integer(up.getInt("flower_count") + 1));
                else
                    up.set("egg_count", new Integer(up.getInt("egg_count") + 1));
                re = up.save();
            }
        }
        return re;
    }

    private long msgId;
    private int flowerCount = 0;
    private int eggCount = 0;

}
