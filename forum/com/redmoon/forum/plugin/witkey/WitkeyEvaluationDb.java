package com.redmoon.forum.plugin.witkey;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.forum.SequenceMgr;

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
public class WitkeyEvaluationDb extends ObjectDb {
    public WitkeyEvaluationDb() {
    }

    public WitkeyEvaluationDb(int id) {
        this.id = id;
        init();
        load();
    }

    public void initDB() {
        this.tableName = "plugin_witkey_evaluation";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new WitkeyEvaluationCache(this);

        this.QUERY_CREATE = "insert into plugin_witkey_evaluation(id, msg_id, user_name, content, add_date) values (?,?,?,?,?)";
        this.QUERY_SAVE = "update plugin_witkey_evaluation set msg_id=?, user_name=?, content=?, add_date=? where id=?";
        this.QUERY_DEL = "delete from plugin_witkey_evaluation where id=?";
        this.QUERY_LOAD = "select msg_id, user_name, content, add_date from plugin_witkey_evaluation where id=?";
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
            WitkeyEvaluationCache wec = new WitkeyEvaluationCache(this);
            primaryKey.setValue(new Integer(id));
            wec.refreshDel(primaryKey);
        }
        return rowcount > 0 ? true : false;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new WitkeyEvaluationDb(pk.getIntValue());
    }

    public boolean create() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            id = (int)SequenceMgr.nextID(SequenceMgr.PLUGIN_WITKEY_EVALUATION);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setInt(1, id);
            ps.setLong(2, msgId);
            ps.setString(3, userName);
            ps.setString(4, content);
            ps.setString(5, Long.toString(System.currentTimeMillis()));
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
            WitkeyEvaluationCache wec = new WitkeyEvaluationCache(this);
            wec.refreshCreate();
        }
        return rowcount > 0 ? true : false;
    }

    public boolean save() throws ResKeyException {
        return true;
    }

    public WitkeyEvaluationDb getWitkeyEvaluationDb(int id) {
        return (WitkeyEvaluationDb) getObjectDb(new Integer(id));
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setInt(1, id);
            primaryKey.setValue(new Integer(id));
            rs = conn.executePreQuery();
            if (rs.next()) {
                msgId = rs.getLong(1);
                userName = rs.getString(2);
                content = rs.getString(3);
                addDate = rs.getString(4);
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
                    id = rs.getInt(1);
                    v.addElement(getWitkeyEvaluationDb(id));
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


    public void setId(int id) {
        this.id = id;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAddDate(String addDate) {
        this.addDate = addDate;
    }

    public int getId() {
        return id;
    }

    public long getMsgId() {
        return msgId;
    }

    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }

    public String getAddDate() {
        return addDate;
    }

    private int id;
    private long msgId;
    private String userName;
    private String content;
    private String addDate;
}
