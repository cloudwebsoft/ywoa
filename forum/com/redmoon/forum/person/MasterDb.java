package com.redmoon.forum.person;

import java.sql.*;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.Conn;
import cn.js.fan.db.PrimaryKey;
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
public class MasterDb extends ObjectDb {

    public MasterDb() {
        super();
    }

    public MasterDb(String name) {
        this.name = name;
        init();
        load();
    }

    public String getName() {
        return name;
    }

    public int getSort() {
        return sort;
    }

    public String getDesc() {
        return desc;
    }

    public ObjectDb getObjectDb(Object primaryKeyValue) {
        MasterCache fc = new MasterCache(this);
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
            ps.setString(1, name);
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
        return new MasterDb(pk.getStrValue());
    }

    public void setQueryCreate() {
        this.QUERY_CREATE = "insert into sq_master (name, sort, description, id) values (?, ?, ?, ?)";
    }

    public void setQuerySave() {
        this.QUERY_SAVE =
            "update sq_master set sort=?,description=? where name=?";
    }

    public void setQueryDel() {
        this.QUERY_DEL = "delete from sq_master where name=?";
    }

    public void setQueryLoad() {
        this.QUERY_LOAD =
            "select sort,description from sq_master where name=?";
    }

    public void setQueryList() {
        QUERY_LIST = "select name from sq_master order by sort";
    }

    public boolean create() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setString(1, name);
            ps.setInt(2, sort);
            ps.setString(3, desc);
            int id = (int)SequenceMgr.nextID(SequenceMgr.SQ_MASTER);
            ps.setInt(4, id);
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
            ps.setInt(1, sort);
            ps.setString(2, desc);
            ps.setString(3, name);
            rowcount = conn.executePreUpdate();
            MasterCache uc = new MasterCache(this);

            primaryKey.setValue(name);
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

    public MasterDb getMasterDb(String name) {
        return (MasterDb)getObjectDb(name);
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setString(1, name);
            primaryKey.setValue(name);
            rs = conn.executePreQuery();
            if (rs.next()) {
                sort = rs.getInt(1);
                desc = rs.getString(2);
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
        primaryKey = new PrimaryKey("name", PrimaryKey.TYPE_STRING);
    }

    private String name;
    private int sort = 0;

    public void setSort(int sort) {
        this.sort = sort;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String desc;
}
