package com.redmoon.oa.project.forum;

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
public class ProjectDb extends ObjectDb {
    
    public ProjectDb() {
        super();
    }

    public ProjectDb(long msgId) {
        this.msgId = msgId;
        init();
        load();
    }

    public void initDB() {
        this.tableName = "plugin_project";
        primaryKey = new PrimaryKey("msgId", PrimaryKey.TYPE_LONG);
        objectCache = new ProjectCache(this);

        this.QUERY_CREATE = "insert into plugin_project (msg_id,project_id) values (?,?)";
        this.QUERY_SAVE = "update plugin_project set project_id=? where msg_id=?";
        this.QUERY_DEL = "delete from plugin_project where msg_id=?";
        this.QUERY_LOAD =
            "select project_id from plugin_project where msg_id=?";
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
            ProjectCache cc = new ProjectCache(this);
            primaryKey.setValue(new Long(msgId));
            cc.refreshDel(primaryKey);
        }
        return rowcount>0? true:false;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new ProjectDb(pk.getLongValue());
    }

    public boolean create() throws ResKeyException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setLong(1, msgId);
            ps.setLong(2, projectId);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ResKeyException(new SkinUtil(), SkinUtil.ERR_DB);
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
            ProjectCache uc = new ProjectCache(this);
            uc.refreshCreate();
        }
        return rowcount>0? true:false;
    }

    public boolean save() throws ResKeyException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            // "update plugin_activity set organizer=?,user_count=?,expire_date=?,money_code=?,attend_money_count=?,exit_money_count=?,tel=?,user_level_type=?,user_level_count=? where msg_id=?";
            PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
            ps.setLong(1, projectId);
            ps.setLong(2, msgId);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB);
        } finally {
            ProjectCache uc = new ProjectCache(this);
            primaryKey.setValue(new Long(msgId));
            uc.refreshSave(primaryKey);
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    public ProjectDb getProjectDb(long id) {
        return (ProjectDb)getObjectDb(new Long(id));
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            // "select organizer,user_count,expire_date,money_code,attend_money_count,exit_money_count,tel,user_level_type,user_level_count from plugin_activity where msg_id=?";
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setLong(1, msgId);
            primaryKey.setValue(new Long(msgId));
            rs = conn.executePreQuery();
            if (rs.next()) {
            	projectId = rs.getLong(1);
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

    private long msgId;
    
    private long projectId;

	public long getProjectId() {
		return projectId;
	}

	public void setProjectId(long projectId) {
		this.projectId = projectId;
	}
    
}
