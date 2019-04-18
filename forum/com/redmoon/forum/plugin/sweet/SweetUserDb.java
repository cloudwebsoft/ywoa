package com.redmoon.forum.plugin.sweet;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.db.Conn;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import cn.js.fan.db.KeyUnit;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;

public class SweetUserDb extends ObjectDb {
    public static final int TYPE_APPLIER = 0;
    public static final int TYPE_PERSUATER = 1;
    public static final int TYPE_SPOUSE = 2;

    public static final int STATE_NORMAL = 0;
    public static final int STATE_SHIELD = 1;

    public SweetUserDb() {
        super();
    }

    public SweetUserDb(long msgRootId, String name) {
        this.msgRootId = msgRootId;
        this.name = name;
        init();
        load();
    }

    public String getTypeDesc(HttpServletRequest request, int type) {
        String str = "";
        switch(type) {
        case TYPE_APPLIER:
            str = SweetSkin.LoadString(request, "USER_TYPE_APPLIER");
            break;
        case TYPE_PERSUATER:
            str = SweetSkin.LoadString(request, "USER_TYPE_PERSUATER");
            break;
        case TYPE_SPOUSE:
            str = SweetSkin.LoadString(request, "USER_TYPE_SPOUSE");
        default:
        }
        return str;
    }

    public String getStateDesc(HttpServletRequest request, int state) {
        String str = "";
        switch(state) {
        case STATE_NORMAL:
            str = SweetSkin.LoadString(request, "USER_STATE_NORMAL");
            break;
        case STATE_SHIELD:
            str = SweetSkin.LoadString(request, "USER_STATE_SHIELD");
            break;
        default:
        }
        return str;
    }

    public ObjectDb getObjectDb(Object primaryKeyValue) {
        SweetUserCache fc = new SweetUserCache(this);
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setValue(primaryKeyValue);
        return fc.getObjectDb(pk);
    }

    public boolean del() {
        return true;
    }

    public int getObjectCount(String sql) {
        return 0;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new SweetUserDb(pk.getKeyLongValue("msgRootId"), pk.getKeyStrValue("name"));
    }

    public void setQueryCreate() {
        this.QUERY_CREATE = "insert into plugin_sweet_user (msgRootId,name,type,state) values (?,?,?,?)";
    }

    public boolean create() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setLong(1, msgRootId);
            ps.setString(2, name);
            ps.setInt(3, type);
            ps.setInt(4, state);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
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
            "update plugin_sweet_user set type=?,state=? where msgRootId=? and name=?";
    }

    public void setQueryDel() {

    }

    public void setQueryLoad() {
        this.QUERY_LOAD =
            "select type,state from plugin_sweet_user where msgRootId=? and name=?";
    }

    public void setQueryList() {
    }

    public boolean save() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
            ps.setInt(1, type);
            ps.setInt(2, state);
            ps.setLong(3, msgRootId);
            ps.setString(4, name);
            rowcount = conn.executePreUpdate();
            SweetUserCache uc = new SweetUserCache(this);
            primaryKey.setKeyValue("msgRootId", new Long(msgRootId));
            primaryKey.setKeyValue("name", name);
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

    public SweetUserDb getSweetUserDb(long msgRootId, String name) {
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setKeyValue("msgRootId", new Long(msgRootId));
        pk.setKeyValue("name", name);
        return (SweetUserDb)getObjectDb(pk.getKeys());
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setLong(1, msgRootId);
            ps.setString(2, name);
            primaryKey.setKeyValue("msgRootId", new Long(msgRootId));
            primaryKey.setKeyValue("name", name);
            rs = conn.executePreQuery();
            if (rs.next()) {
                type = rs.getInt(1);
                state = rs.getInt(2);
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
        HashMap key = new HashMap();
        key.put("msgRootId", new KeyUnit(primaryKey.TYPE_LONG, 0));
        key.put("name", new KeyUnit(primaryKey.TYPE_STRING, 1));
        primaryKey = new PrimaryKey(key);
    }

    /**
     * 取得贴子中所有的追求者
     * @param msgRootId int
     * @return Vector
     */
    public Vector getAllPersuater(long msgRootId) {
        Vector v = new Vector();
        String sql = "select name from plugin_sweet_user where msgRootId=? order by type desc";
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, msgRootId);
            rs = conn.executePreQuery();
            while (rs.next()) {
                String pname = rs.getString(1);
                SweetUserDb su = new SweetUserDb();
                su.primaryKey.setKeyValue("msgRootId", new Long(msgRootId));
                su.primaryKey.setKeyValue("name", pname);
                su = (SweetUserDb) su.getObjectDb(su.primaryKey.getKeys());
                v.addElement(su);
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    /**
     * 取得用户参与的记录
     * @param msgRootId int
     * @return Vector
     */
    public Vector getUserAttend(String name) {
        Vector v = new Vector();
        String sql = "select msgRootId from plugin_sweet_user where name=?";
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            rs = conn.executePreQuery();
            while (rs.next()) {
                int msgRootId = rs.getInt(1);
                SweetUserDb su = new SweetUserDb();
                su = su.getSweetUserDb(msgRootId, name);
                v.addElement(su);
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    public void setMsgRootId(long msgRootId) {
        this.msgRootId = msgRootId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getMsgRootId() {
        return msgRootId;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public int getState() {
        return state;
    }

    private long msgRootId = 0;
    private String name;
    private int type = 0;
    private int state = 0;
}
