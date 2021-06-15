package com.redmoon.oa.fileark.plugin.sms;

import java.sql.*;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.Conn;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.util.ErrMsgException;

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
public class SMSDocumentDb extends ObjectDb {
    public static final int SPEED_COUNT_GENERAL = 1;
    public static final int SPEED_COUNT_GENERAL_SLOW = 2;
    public static final int SPEED_COUNT_GENERAL_SLOW_QUICK = 3;

    public static final int SPEED_COUNT_NONE = 0;

    public SMSDocumentDb() {
    }

    public SMSDocumentDb(int docId) {
        this.docId = docId;
        init();
        load();
    }

    /**
     * del
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean del() throws ErrMsgException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_DEL);
            ps.setInt(1, docId);
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

    /**
     *
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new SMSDocumentDb(pk.getIntValue());
    }

    public SMSDocumentDb getSMSDocumentDb(int docId) {
        return (SMSDocumentDb)getObjectDb(new Integer(docId));
    }

    public boolean create() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setInt(1, docId);
            ps.setInt(2, speedCount);
            ps.setString(3, pptPath);
            ps.setInt(4, useCard?1:0);
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


    /**
     * load
     *
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setInt(1, docId);
            primaryKey.setValue(new Integer(docId));
            rs = conn.executePreQuery();

            if (rs.next()) {
                speedCount = rs.getInt(1);
                pptPath = rs.getString(2);
                useCard = rs.getInt(3)==1?true:false;
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

    /**
     * save
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean save() {
            int rowcount = 0;
            Conn conn = null;
            try {
                conn = new Conn(connname);
                PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
                ps.setInt(1, speedCount);
                ps.setString(2, pptPath);
                ps.setInt(3, useCard?1:0);
                ps.setInt(4, docId);
                rowcount = conn.executePreUpdate();

                primaryKey.setValue(new Integer(docId));
                objectCache.refreshSave(primaryKey);
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

    public void initDB() {
        this.tableName = "cms_plugin_teach_document";

        primaryKey = new PrimaryKey("docId", PrimaryKey.TYPE_INT);
        objectCache = new SMSDocumentCache(this);

        this.QUERY_DEL =
                "delete FROM " + tableName + " WHERE docId=?";
        this.QUERY_CREATE =
                "INSERT " + tableName + " (docId,speedCount,pptPath,isUseCard) VALUES (?,?,?,?)";
        this.QUERY_LOAD =
                "SELECT speedCount,pptPath,isUseCard FROM " + tableName + " WHERE docId=?";
        this.QUERY_SAVE =
                "UPDATE " + tableName + " SET speedCount=?,pptPath=?,isUseCard=? WHERE docId=?";
        isInitFromConfigDB = false;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public void setSpeedCount(int speedCount) {
        this.speedCount = speedCount;
    }

    public void setPptPath(String pptPath) {
        this.pptPath = pptPath;
    }

    public void setUseCard(boolean useCard) {
        this.useCard = useCard;
    }

    public int getDocId() {
        return docId;
    }

    public int getSpeedCount() {
        return speedCount;
    }

    public String getPptPath() {
        return pptPath;
    }

    public boolean isUseCard() {
        return useCard;
    }

    private int docId;
    private int speedCount = SPEED_COUNT_GENERAL;
    private String pptPath = "";
    private boolean useCard;
}
