package com.redmoon.forum.plugin.refer;

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
public class ReferDb extends ObjectDb {
    public static final int SECRET_LEVEL_PUBLIC = 0; // 贴子的秘级 0 公共 1 本贴内用户可见(相当于回复可见) 2 被回复者可见 3 楼主可见
    public static final int SECRET_LEVEL_MSG_OWNER = 1; // 主题贴作者可见
    public static final int SECRET_LEVEL_MANAGER = 2; // 版主可见
    
    public ReferDb() {
        super();
    }

    public ReferDb(long msgId) {
        this.msgId = msgId;
        init();
        load();
    }

    public void initDB() {
        this.tableName = "plugin_refer";
        primaryKey = new PrimaryKey("msgId", PrimaryKey.TYPE_LONG);
        objectCache = new ReferCache(this);

        this.QUERY_CREATE = "insert into plugin_refer (msg_id,secret_level) values (?,?)";
        this.QUERY_SAVE = "update plugin_refer set secret_level=?,is_replied=? where msg_id=?";
        this.QUERY_DEL = "delete from plugin_refer where msg_id=?";
        this.QUERY_LOAD =
            "select secret_level,is_replied from plugin_refer where msg_id=?";
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
            ReferCache cc = new ReferCache(this);
            primaryKey.setValue(new Long(msgId));
            cc.refreshDel(primaryKey);
        }
        return rowcount>0? true:false;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new ReferDb(pk.getLongValue());
    }

    public boolean create() throws ResKeyException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setLong(1, msgId);
            ps.setInt(2, secretLevel);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ResKeyException(new SkinUtil(), SkinUtil.ERR_DB);
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
            ReferCache uc = new ReferCache(this);
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
            ps.setInt(1, secretLevel);
            ps.setInt(2, replied?1:0);
            ps.setLong(3, msgId);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB);
        } finally {
            ReferCache uc = new ReferCache(this);
            primaryKey.setValue(new Long(msgId));
            uc.refreshSave(primaryKey);
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    public ReferDb getReferDb(long id) {
        return (ReferDb)getObjectDb(new Long(id));
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
            	secretLevel = rs.getInt(1);
            	replied = rs.getInt(2)==1;
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

    public void setSecretLevel(int secretLevel) {
    	this.secretLevel = secretLevel;
    }
    
    public int getSecretLevel() {
    	return secretLevel;
    }
    
    public void setMsgId(long msgId) {
    	this.msgId = msgId;
    }
    
    public boolean isReplied() {
    	return replied;
    }
    
    public void setReplied(boolean replied) {
    	this.replied = replied;
    }

    private long msgId;
    
    private int secretLevel;
    
    private boolean replied = false;
}
