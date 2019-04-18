package com.redmoon.forum.plugin.sweet;

import java.sql.*;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.Conn;
import cn.js.fan.db.PrimaryKey;

/**
 *
 * <p>Title: </p>
 *
 * <p>Description:存放情人路贴子的属性，msgRootId,state等 </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SweetDb extends ObjectDb {
    public final int STATE_PURSUE = 0;
    public final int STATE_MARRY = 1;

    public SweetDb() {
        init();
    }

    public SweetDb(long msgRootId) {
        this.msgRootId = msgRootId;
        init();
        load();
    }

    public SweetDb(String name) {
        this.name = name;
        init();
        loadByName();
    }

    public void setmsgRootId(long msgRootId) {
        this.msgRootId = msgRootId;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSpouse(String spouse) {
        this.spouse = spouse;
    }

    public long getMsgRootId() {
        return msgRootId;
    }

    public int getState() {
        return state;
    }

    public String getStateDesc(HttpServletRequest request) {
        String str = "";
        switch (state) {
        case STATE_PURSUE:
            str = SweetSkin.LoadString(request, "STATE_PURSUE");
            break;
        case STATE_MARRY:
            str = SweetSkin.LoadString(request, "STATE_MARRY");
            break;
        default:
        }
        return str;
    }

    public String getName() {
        return name;
    }

    public String getSpouse() {
        return spouse;
    }

    public ObjectDb getObjectDb(Object primaryKeyValue) {
        SweetCache fc = new SweetCache(this);
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setValue(primaryKeyValue);
        return (SweetDb)fc.getObjectDb(pk);
    }

    public boolean del() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_DEL);
            ps.setLong(1, msgRootId);
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

    public int getObjectCount(String sql) {
        return 0;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new SweetDb(pk.getLongValue());
    }

    public void setQueryCreate() {
        this.QUERY_CREATE = "insert into plugin_sweet (msgRootId, state, name) values (?, ?, ?)";
    }

    public void setQuerySave() {
        this.QUERY_SAVE =
            "update plugin_sweet set state=?,spouse=? where msgRootId=?";
    }

    public void setQueryDel() {
        this.QUERY_DEL = "delete from plugin_sweet where msgRootId=?";
    }

    public void setQueryLoad() {
        this.QUERY_LOAD =
            "select name,state,spouse from plugin_sweet where msgRootId=?";
    }

    public void setQueryList() {
    }

    public boolean create() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setLong(1, msgRootId);
            ps.setInt(2, state);
            ps.setString(3, name);
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

    public boolean save() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
            ps.setInt(1, state);
            ps.setString(2, spouse);
            ps.setLong(3, msgRootId);
            rowcount = conn.executePreUpdate();
            SweetCache uc = new SweetCache(this);
            primaryKey.setValue(new Long(this.msgRootId));
            uc.refreshSave(primaryKey);
            // 更新由name得到的缓存
            try {
                uc.rmCache.remove(name, uc.group);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
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

    public int getMsgRootIdOfUser(String name) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            String sql = "select msgRootId from plugin_sweet where name=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            rs = conn.executePreQuery();
            if (rs.next()) {
                return rs.getInt(1);
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
        return -1;
    }

    public SweetDb getSweetDb(String name) {
        SweetCache sc = new SweetCache(this);
        return sc.getSweetDb(name);
    }

    public SweetDb getSweetDb(long msgRootId) {
        return (SweetDb)getObjectDb(new Long(msgRootId));
    }

    public void loadByName() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            String sql = "select msgRootId,state from plugin_sweet where name=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            rs = conn.executePreQuery();
            if (rs.next()) {
                msgRootId = rs.getInt(1);
                state = rs.getInt(2);
                loaded = true;
            }
            primaryKey.setValue(new Long(msgRootId));
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

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setLong(1, msgRootId);
            primaryKey.setValue(new Long(msgRootId));
            rs = conn.executePreQuery();
            if (rs.next()) {
                name = rs.getString(1);
                state = rs.getInt(2);
                spouse = rs.getString(3);
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
        primaryKey = new PrimaryKey("msgRootId", PrimaryKey.TYPE_LONG);
    }

    private long msgRootId;
    private int state;
    private String name;
    private String spouse;

}
