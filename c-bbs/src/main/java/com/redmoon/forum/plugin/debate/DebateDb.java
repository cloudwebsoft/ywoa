package com.redmoon.forum.plugin.debate;

import java.sql.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;

/**
 *
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
public class DebateDb extends ObjectDb {
    public DebateDb() {
        super();
    }

    public DebateDb(long msgId) {
        this.msgId = msgId;
        init();
        load();
    }

    public void initDB() {
        this.tableName = "plugin_debate";
        primaryKey = new PrimaryKey("msg_id", PrimaryKey.TYPE_LONG);
        objectCache = new DebateCache(this);

        this.QUERY_CREATE = "insert into plugin_debate (msg_id,viewpoint1,viewpoint2,begin_date,end_date) values (?,?,?,?,?)";
        this.QUERY_SAVE = "update plugin_debate set viewpoint1=?,viewpoint2=?,begin_date=?,end_date=?,vote_count1=?,vote_count2=?,user_count1=?,user_count2=?,user_count3=?,vote_user1=?,vote_user2=? where msg_id=?";
        this.QUERY_DEL = "delete from plugin_debate where msg_id=?";
        this.QUERY_LOAD =
            "select viewpoint1,viewpoint2,begin_date,end_date,vote_count1,vote_count2,user_count1,user_count2,user_count3,vote_user1,vote_user2 from plugin_debate where msg_id=?";
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
            DebateCache cc = new DebateCache(this);
            primaryKey.setValue(new Long(msgId));
            cc.refreshDel(primaryKey);
        }
        return rowcount>0? true:false;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new DebateDb(pk.getLongValue());
    }

    public boolean create() throws ResKeyException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setLong(1, msgId);
            ps.setString(2, viewpoint1);
            ps.setString(3, viewpoint2);
            ps.setString(4, DateUtil.toLongString(beginDate));
            ps.setString(5, DateUtil.toLongString(endDate));
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ResKeyException(new SkinUtil(), SkinUtil.ERR_DB);
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
            DebateCache uc = new DebateCache(this);
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
            ps.setString(1, viewpoint1);
            ps.setString(2, viewpoint2);
            ps.setString(3, DateUtil.toLongString(beginDate));
            ps.setString(4, DateUtil.toLongString(endDate));
            ps.setInt(5, voteCount1);
            ps.setInt(6, voteCount2);
            ps.setInt(7, userCount1);
            ps.setInt(8, userCount2);
            ps.setInt(9, userCount3);
            ps.setString(10, voteUser1);
            ps.setString(11, voteUser2);
            ps.setLong(12, msgId);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB);
        } finally {
            DebateCache uc = new DebateCache(this);
            primaryKey.setValue(new Long(msgId));
            uc.refreshSave(primaryKey);
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    public DebateDb getDebateDb(long id) {
        return (DebateDb)getObjectDb(new Long(id));
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
                viewpoint1 = rs.getString(1);
                viewpoint2 = rs.getString(2);
                beginDate = DateUtil.parse(rs.getString(3));
                endDate = DateUtil.parse(rs.getString(4));
                voteCount1 = rs.getInt(5);
                voteCount2 = rs.getInt(6);
                userCount1 = rs.getInt(7);
                userCount2 = rs.getInt(8);
                userCount3 = rs.getInt(9);
                voteUser1 = StrUtil.getNullStr(rs.getString(10));
                voteUser2 = StrUtil.getNullStr(rs.getString(11));
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

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public void setViewpoint1(String viewpoint1) {
        this.viewpoint1 = viewpoint1;
    }

    public void setViewpoint2(String viewpoint2) {
        this.viewpoint2 = viewpoint2;
    }

    public void setVoteCount1(int voteCount1) {
        this.voteCount1 = voteCount1;
    }

    public void setVoteCount2(int voteCount2) {
        this.voteCount2 = voteCount2;
    }

    public void setUserCount1(int userCount1) {
        this.userCount1 = userCount1;
    }

    public void setUserCount2(int userCount2) {
        this.userCount2 = userCount2;
    }

    public void setUserCount3(int userCount3) {
        this.userCount3 = userCount3;
    }

    public void setBeginDate(java.util.Date beginDate) {
        this.beginDate = beginDate;
    }

    public void setEndDate(java.util.Date endDate) {
        this.endDate = endDate;
    }

    public void setVoteUser1(String voteUser1) {
        this.voteUser1 = voteUser1;
    }

    public void setVoteUser2(String voteUser2) {
        this.voteUser2 = voteUser2;
    }

    public String getViewpoint1() {
        return viewpoint1;
    }

    public String getViewpoint2() {
        return viewpoint2;
    }

    public int getVoteCount1() {
        return voteCount1;
    }

    public int getVoteCount2() {
        return voteCount2;
    }

    public int getUserCount1() {
        return userCount1;
    }

    public int getUserCount2() {
        return userCount2;
    }

    public int getUserCount3() {
        return userCount3;
    }

    public java.util.Date getBeginDate() {
        return beginDate;
    }

    public java.util.Date getEndDate() {
        return endDate;
    }

    public String getVoteUser1() {
        return voteUser1;
    }

    public String getVoteUser2() {
        return voteUser2;
    }

    private long msgId;
    private String viewpoint1;
    private String viewpoint2;
    private int voteCount1 = 0;
    private int voteCount2;
    private int userCount1 = 0;
    private int userCount2 = 0;
    private int userCount3 = 0;
    private java.util.Date beginDate;
    private java.util.Date endDate;
    private String voteUser1;
    private String voteUser2;
}
