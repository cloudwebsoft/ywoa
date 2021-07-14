package com.redmoon.forum.plugin.sweet;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.db.Conn;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import cn.js.fan.db.KeyUnit;
import java.util.HashMap;
import cn.js.fan.util.DateUtil;

public class SweetLifeDb extends ObjectDb {
    public SweetLifeDb() {
    }

    public void setMsgRootId(long msgRootId) {
        this.msgRootId = msgRootId;
    }

    public void setMarryDate(Date marryDate) {
        this.marryDate = marryDate;
    }

    public void setDivorceDate(Date divorceDate) {
        this.divorceDate = divorceDate;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public void setSpouseName(String spouseName) {
        this.spouseName = spouseName;
    }

    public long getMsgRootId() {
        return msgRootId;
    }

    public Date getMarryDate() {
        return marryDate;
    }

    public Date getDivorceDate() {
        return divorceDate;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getSpouseName() {
        return spouseName;
    }

    public boolean create() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setLong(1, msgRootId);
            ps.setString(2, DateUtil.toLongString(marryDate));
            ps.setString(3, null);
            ps.setString(4, ownerName);
            ps.setString(5, spouseName);
            rowcount = conn.executePreUpdate();
            primaryKey.setKeyValue("ownerName", ownerName);
            primaryKey.setKeyValue("spouseName", spouseName);
            // 更新缓存
            SweetLifeCache uc = new SweetLifeCache(this);
            uc.refreshCreate();
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

    public ObjectDb getObjectDb(Object primaryKeyValue) {
        SweetLifeCache fc = new SweetLifeCache(this);
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setValue(primaryKeyValue);
        return (SweetLifeDb)fc.getObjectDb(pk);
    }

    public boolean del() {
        return true;
    }

    public int getObjectCount(String sql) {
        return 0;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new SweetDb(pk.getLongValue());
    }

    public void setQueryCreate() {
        this.QUERY_CREATE = "insert into plugin_sweet_life (msgRootId, marryDate, divorceDate,ownerName,spouseName) values (?,?,?,?,?)";
    }

    public void setQuerySave() {
        this.QUERY_SAVE =
            "update plugin_sweet_life set msgRootId=?,marryDate=?,divorceDate=? where ownerName=? and spouseName=?";
    }

    public void setQueryDel() {

    }

    public void setQueryLoad() {
        this.QUERY_LOAD =
            "select msgRootId,marryDate,divorceDate, from plugin_sweet_life where ownerName=? and spouseName=?";
    }

    public void setQueryList() {
    }

    public boolean save() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
            ps.setLong(1, msgRootId);
            ps.setString(2, DateUtil.toLongString(marryDate));
            ps.setString(3, DateUtil.toLongString(divorceDate));
            ps.setString(4, ownerName);
            ps.setString(5, spouseName);
            rowcount = conn.executePreUpdate();
            SweetLifeCache uc = new SweetLifeCache(this);
            primaryKey.setKeyValue("ownerName", ownerName);
            primaryKey.setKeyValue("spouseName", spouseName);
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
            ps.setString(1, ownerName);
            ps.setString(2, spouseName);
            primaryKey.setKeyValue("ownerName", ownerName);
            primaryKey.setKeyValue("spouseName", spouseName);
            rs = conn.executePreQuery();
            if (rs.next()) {
                msgRootId = rs.getInt(1);
                marryDate = DateUtil.parse(rs.getString(2));
                divorceDate = DateUtil.parse(rs.getString(3));
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
        key.put("ownerName", new KeyUnit(primaryKey.TYPE_STRING, 0));
        key.put("spouseName", new KeyUnit(primaryKey.TYPE_STRING, 1));
        primaryKey = new PrimaryKey(key);
    }

    private long msgRootId;
    private Date marryDate;
    private Date divorceDate;
    private String ownerName;
    private String spouseName;
}
