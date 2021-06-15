package com.redmoon.forum.plugin.debate;

import java.sql.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.forum.SequenceMgr;

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
public class DebateViewpointDb extends ObjectDb {
    public static final int TYPE_SUPPORT = 0;
    public static final int TYPE_OPPOSE = 1;
    public static final int TYPE_OTHERS = 2;

    public DebateViewpointDb() {
        super();
    }

    public DebateViewpointDb(long msgId) {
        this.msgId = msgId;
        init();
        load();
    }

    public void initDB() {
        this.tableName = "plugin_debate_viewpoint";
        primaryKey = new PrimaryKey("msg_id", PrimaryKey.TYPE_LONG);
        objectCache = new DebateViewpointCache(this);

        this.QUERY_CREATE = "insert into plugin_debate_viewpoint (msg_id,viewpoint_type) values (?,?)";
        this.QUERY_SAVE = "update plugin_debate_viewpoint set viewpoint_type=? where msg_id=?";
        this.QUERY_DEL = "delete from plugin_debate_viewpoint where msg_id=?";
        this.QUERY_LOAD =
            "select viewpoint_type from plugin_debate_viewpoint where msg_id=?";
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
            DebateViewpointCache cc = new DebateViewpointCache(this);
            primaryKey.setValue(new Long(msgId));
            cc.refreshDel(primaryKey);
        }
        return rowcount>0? true:false;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new DebateViewpointDb(pk.getLongValue());
    }

    public boolean create() throws ResKeyException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setLong(1, msgId);
            ps.setInt(2, type);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ResKeyException(new SkinUtil(), SkinUtil.ERR_DB);
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
            DebateViewpointCache uc = new DebateViewpointCache(this);
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
            ps.setInt(1, type);
            ps.setLong(2, msgId);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB);
        } finally {
            DebateViewpointCache uc = new DebateViewpointCache(this);
            primaryKey.setValue(new Long(msgId));
            uc.refreshSave(primaryKey);
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    public DebateViewpointDb getDebateViewpointDb(long id) {
        return (DebateViewpointDb)getObjectDb(new Long(id));
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
                type = rs.getInt(1);
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

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    private long msgId;
    private int type;
}
