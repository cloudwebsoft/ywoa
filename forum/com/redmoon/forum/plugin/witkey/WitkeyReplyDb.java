package com.redmoon.forum.plugin.witkey;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;

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
public class WitkeyReplyDb extends ObjectDb {
    public static final int SECRET_LEVEL_FORUM_PUBLIC = 0; // 贴子的秘级 0 公共 1 本贴内用户可见 2 被回复者可见 3 楼主可见 4 版主可见
    public static final int SECRET_LEVEL_MSG_USER = 1;
    public static final int SECRET_LEVEL_MSG_USER_REPLIED = 2; // 被回复者可见
    public static final int SECRET_LEVEL_MSG_OWNER = 3; // 主题贴作者可见
    public static final int SECRET_LEVEL_MASTER = 4; //版主可见

    public static final int REPLY_TYPE_CONTRIBUTION = 0; //我要投稿
    public static final int REPLY_TYPE_COMMUNICATION = 1;//任务交流


    public WitkeyReplyDb() {
    }


    public WitkeyReplyDb(long msgId) {
        this.msgId = msgId;
        init();
        load();
    }


    public void initDB() {
        this.tableName = "plugin_witkey_reply";
        primaryKey = new PrimaryKey("msgId", PrimaryKey.TYPE_LONG);
        objectCache = new WitkeyReplyCache(this);

        this.QUERY_CREATE = "insert into plugin_witkey_reply(msg_id, user_name, view_type, reply_type) values (?,?,?,?)";
        this.QUERY_SAVE = "update plugin_witkey_reply set user_name=?, view_type=?, reply_type=? where msg_id=?";
        this.QUERY_DEL = "delete from plugin_witkey_reply where msg_id=?";
        this.QUERY_LOAD = "select user_name, view_type, reply_type from plugin_witkey_reply where msg_id=?";
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
            WitkeyReplyCache wrc = new WitkeyReplyCache(this);
            primaryKey.setValue(new Long(msgId));
            wrc.refreshDel(primaryKey);
        }
        return rowcount > 0 ? true : false;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new WitkeyReplyDb(pk.getLongValue());
    }

    public boolean create() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setLong(1, msgId);
            ps.setString(2, userName);
            ps.setInt(3, viewType);
            ps.setInt(4, replyType);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
            WitkeyReplyCache wrc = new WitkeyReplyCache(this);
            wrc.refreshCreate();
        }
        return rowcount > 0 ? true : false;
    }

    public boolean save() throws ResKeyException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
            ps.setString(1, userName);
            ps.setInt(2, viewType);
            ps.setInt(3, replyType);
            ps.setLong(4, msgId);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB);
        } finally {
            WitkeyReplyCache wrc = new WitkeyReplyCache(this);
            primaryKey.setValue(new Long(msgId));
            wrc.refreshSave(primaryKey);
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount > 0 ? true : false;
    }

    public WitkeyReplyDb getWitkeyReplyDb(long msgRootId) {
        return (WitkeyReplyDb)getObjectDb(new Long(msgRootId));
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
                userName = rs.getString(1);
                viewType = rs.getInt(2);
                replyType = rs.getInt(3);
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public Vector list() {
        Vector v = new Vector();
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            rs = conn.executeQuery(this.QUERY_LIST);
            if (rs != null) {
                while (rs.next()) {
                    msgId = rs.getLong(1);
                    v.addElement(getWitkeyReplyDb(msgId));
                }
            }
        } catch (SQLException e) {
            logger.error("list: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public void setReplyType(int replyType) {
        this.replyType = replyType;
    }

    public String getUserName() {
        return userName;
    }

    public int getViewType() {
        return viewType;
    }

    public int getReplyType() {
        return replyType;
    }

    public long getMsgId() {
        return msgId;
    }

    private String userName;
    private int viewType;
    private int replyType;
    private long msgId;

}
