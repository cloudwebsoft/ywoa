package com.redmoon.forum.plugin.reward;

import java.sql.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.forum.plugin.ScoreMgr;
import com.redmoon.forum.plugin.ScoreUnit;
import com.redmoon.forum.MsgDb;

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
public class RewardDb extends ObjectDb {

    public RewardDb() {
        super();
    }

    public RewardDb(long msgId) {
        this.msgId = msgId;
        init();
        load();
    }

    public void initDB() {
        this.tableName = "plugin_reward";
        primaryKey = new PrimaryKey("msgId", PrimaryKey.TYPE_LONG);
        objectCache = new RewardCache(this);

        this.QUERY_CREATE = "insert into plugin_reward (msg_id, score, money_code, is_end, score_given) values (?, ?, ?, 0, 0)";
        this.QUERY_SAVE = "update plugin_reward set score=?,is_end=?,money_code=?,score_given=? where msg_id=?";
        this.QUERY_DEL = "delete from plugin_reward where msg_id=?";
        this.QUERY_LOAD =
            "select score, is_end, money_code, score_given from plugin_reward where msg_id=?";
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
            RewardCache cc = new RewardCache(this);
            primaryKey.setValue(new Long(msgId));
            cc.refreshDel(primaryKey);

            // 删除其附带的所有的InfoWorthDb

            // 删除其所有订单
        }
        return rowcount>0? true:false;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new RewardDb(pk.getLongValue());
    }

    public boolean create() throws ResKeyException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setLong(1, msgId);
            ps.setInt(2, score);
            ps.setString(3, moneyCode);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ResKeyException(new SkinUtil(), SkinUtil.ERR_DB);
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    public boolean save() throws ResKeyException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
            ps.setInt(1, score);
            ps.setInt(2, end?1:0);
            ps.setString(3, moneyCode);
            ps.setInt(4, scoreGiven);
            ps.setLong(5, msgId);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB);
        } finally {
            RewardCache uc = new RewardCache(this);
            primaryKey.setValue(new Long(msgId));
            uc.refreshSave(primaryKey);
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    public RewardDb getRewardDb(long id) {
        return (RewardDb)getObjectDb(new Long(id));
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
                score = rs.getInt(1);
                end = rs.getInt(2)==1;
                moneyCode = rs.getString(3);
                scoreGiven = rs.getInt(4);
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

    public void setScore(int score) {
        this.score = score;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public void setMoneyCode(String moneyCode) {
        this.moneyCode = moneyCode;
    }

    public void setScoreGiven(int scoreGiven) {
        this.scoreGiven = scoreGiven;
    }

    public long getMsgId() {
        return msgId;
    }

    public int getScore() {
        return score;
    }

    public boolean isEnd() {
        return end;
    }

    public String getMoneyCode() {
        return moneyCode;
    }

    public int getScoreGiven() {
        return scoreGiven;
    }

    /**
     *
     * @param curMsgDb MsgDb
     * @param score int
     * @return int 0 表示失败 1 表示成功 2 表示结贴
     * @throws ResKeyException
     */
    public int doPay(MsgDb curMsgDb, int score) throws ResKeyException {
        RewardDb rootRd = getRewardDb(curMsgDb.getRootid());
        // 检查送的分数是否已超过了剩余可送的分数
        int syScore = rootRd.getScore() - rootRd.getScoreGiven();
        if (syScore < score) {
            throw new ResKeyException(RewardSkin.getResource(), "err_score_inadequate", new Object[] {"" + rootRd.getScore(), "" + syScore});
        }

        String moneyCode = rootRd.getMoneyCode();
        ScoreMgr sm = new ScoreMgr();
        ScoreUnit su = sm.getScoreUnit(moneyCode);

        int r = 0;
        // 送分
        boolean re = su.getScore().pay(su.getScore().SELLER_SYSTEM, curMsgDb.getName(), score);
        if (re) {
            // 记录送的分数，使与回贴ID相关联
            RewardDb rd = new RewardDb();
            rd.setMsgId(curMsgDb.getId());
            rd.setScore(score);
            re = rd.create();

            if (re) {
                r = 1;
                rootRd.setScoreGiven(rootRd.getScoreGiven() + score);
                // 如果分数已经送完，则置为end
                if (rootRd.getScore() - rootRd.getScoreGiven()==0) {
                    rootRd.setEnd(true);
                    r = 2;
                }
                re = rootRd.save();
            }
        }
        return r;
    }

    private long msgId;
    private int score = 0;
    private boolean end = false;
    private String moneyCode;
    private int scoreGiven = 0;

}
