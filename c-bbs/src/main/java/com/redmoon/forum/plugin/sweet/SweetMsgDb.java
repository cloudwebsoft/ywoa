package com.redmoon.forum.plugin.sweet;

import cn.js.fan.base.ObjectDb;
import java.sql.ResultSet;
import java.sql.SQLException;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.db.Conn;
import java.sql.PreparedStatement;
import javax.servlet.http.HttpServletRequest;

public class SweetMsgDb extends ObjectDb {
    public static final int SECRET_LEVEL_FORUM_PUBLIC = 0; // 贴子的秘级 0 公共 1 本贴内用户可见 2 被回复者可见 3 楼主可见
    public static final int SECRET_LEVEL_MSG_USER = 1;
    public static final int SECRET_LEVEL_MSG_USER_REPLIED = 2; // 被回复者可见
    public static final int SECRET_LEVEL_MSG_OWNER = 3; // 主题贴作者可见

    public static final int USER_ACTION_GENERAL = 0;
    public static final int USER_ACTION_APPLY    = 1;
    public static final int USER_ACTION_APPLY_DECLINE = 2;
    public static final int USER_ACTION_APPLY_MARRY = 3;
    public static final int USER_ACTION_DECLINE_MARRY = 4;

    public SweetMsgDb() {
        super();
    }

    public SweetMsgDb(long msgId) {
        this.msgId = msgId;
        init();
        load();
    }

    public static String getSecretLevelDesc(HttpServletRequest request, int secretlevel) {
        String str = "";
        switch(secretlevel) {
        case SECRET_LEVEL_FORUM_PUBLIC:
            str = SweetSkin.LoadString(request, "SECRET_LEVEL_FORUM_PUBLIC");
            break;
        case SECRET_LEVEL_MSG_USER:
            str = SweetSkin.LoadString(request, "SECRET_LEVEL_MSG_USER");
            break;
        case SECRET_LEVEL_MSG_USER_REPLIED:
            str = SweetSkin.LoadString(request, "SECRET_LEVEL_MSG_USER_REPLIED");
            break;
        case SECRET_LEVEL_MSG_OWNER:
            str = SweetSkin.LoadString(request, "SECRET_LEVEL_MSG_OWNER");
            break;
        default:
        }

        return str;

    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public void setScretLevel(int scretLevel) {
        this.secretLevel = scretLevel;
    }

    public void setUserAction(int userAction) {
        this.userAction = userAction;
    }

    public long getMsgId() {
        return msgId;
    }

    public int getSecretLevel() {
        return secretLevel;
    }

    public int getUserAction() {
        return userAction;
    }

    public SweetMsgDb getSweetMsgDb(long msgId) {
        return (SweetMsgDb)getObjectDb(new Long(msgId));
    }

    public ObjectDb getObjectDb(Object primaryKeyValue) {
        SweetMsgCache fc = new SweetMsgCache(this);
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setValue(primaryKeyValue);
        return fc.getObjectDb(pk);
    }

    public boolean del() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_DEL);
            ps.setLong(1, msgId);
            rowcount = conn.executePreUpdate();
            SweetMsgCache uc = new SweetMsgCache(this);
            primaryKey.setValue(new Long(this.msgId));
            uc.refreshDel(primaryKey);
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

    public int getObjectCount(String sql) {
        return 0;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new SweetMsgDb(pk.getLongValue());
    }

    public void setQueryCreate() {
        this.QUERY_CREATE =
            "insert into plugin_sweet_sq_message (msgId,scretLevel,userAction) values (?,?,?)";
    }

    public boolean create() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setLong(1, msgId);
            ps.setInt(2, secretLevel);
            ps.setInt(3, userAction);
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

    public void setQuerySave() {
        this.QUERY_SAVE =
            "update plugin_sweet_sq_message set scretLevel=?,userAction=? where msgId=?";
    }

    public void setQueryDel() {
        this.QUERY_DEL =
            "delete from plugin_sweet_sq_message where msgId=?";
    }

    public void setQueryLoad() {
        this.QUERY_LOAD =
            "select scretLevel,userAction from plugin_sweet_sq_message where msgId=?";
    }

    public void setQueryList() {
    }

    public boolean save() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
            ps.setInt(1, secretLevel);
            ps.setInt(2, userAction);
            ps.setLong(3, msgId);
            rowcount = conn.executePreUpdate();
            SweetMsgCache uc = new SweetMsgCache(this);
            primaryKey.setValue(new Long(this.msgId));
            uc.refreshSave(primaryKey);
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

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setLong(1, msgId);
            primaryKey.setValue(new Long(msgId));
            rs = conn.executePreQuery();
            if (rs.next()) {
                secretLevel = rs.getInt(1);
                userAction = rs.getInt(2);
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

    public Object[] getObjectBlock(String query, int startIndex) {
        return null;
    }

    public void setPrimaryKey() {
        primaryKey = new PrimaryKey("msgId", PrimaryKey.TYPE_LONG);
    }

    private long msgId;
    private int secretLevel = SECRET_LEVEL_FORUM_PUBLIC;
    private int userAction = 0;
}
