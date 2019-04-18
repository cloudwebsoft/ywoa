package com.redmoon.oa.questionnaire;

import java.sql.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import java.util.Vector;
import com.redmoon.oa.db.SequenceManager;

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
public class QuestionnaireFormSubitemDb extends ObjectDb {
    /**
     * 项目编号
     */
    private int itemId;
    private int id;

    public QuestionnaireFormSubitemDb() {
        init();
    }

    public QuestionnaireFormSubitemDb(int id) {
        this.id = id;
        init();
        load();
    }

    public void initDB() {
        tableName = "oa_questionnaire_form_subitem";
        primaryKey = new PrimaryKey("itemId", PrimaryKey.TYPE_INT);
        objectCache = new QuestionnaireFormSubitemCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE = "insert into " + tableName + " (id,item_id,name,orders) values (?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set name=? where id=?";
        QUERY_LIST = "select id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select item_id,name,orders from " + tableName + " where id=?";
    }

    public QuestionnaireFormSubitemDb getQuestionnaireFormSubitemDb(int itemId) {
        return (QuestionnaireFormSubitemDb)getObjectDb(new Integer(itemId));
    }

    public boolean create() throws ErrMsgException {
        id = (int) SequenceManager.nextID(SequenceManager.QUESTIONNAIRE_FORM_SUBITEM);
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setInt(1, id);
            ps.setInt(2, itemId);
            ps.setString(3, name);
            ps.setInt(4, orders);
            re = conn.executePreUpdate()==1 ? true : false;
            if (re) {
                QuestionnaireFormSubitemCache qfic = new QuestionnaireFormSubitemCache(this);
                qfic.refreshCreate();
            }
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ErrMsgException("数据库操作失败！");
        } finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new QuestionnaireFormSubitemDb(pk.getIntValue());
    }

    public boolean del() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                QuestionnaireFormSubitemCache qfic = new QuestionnaireFormSubitemCache(this);
                primaryKey.setValue(new Integer(itemId));
                qfic.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            logger.error("del: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                itemId = rs.getInt(1);
                name = rs.getString(2);
                orders = rs.getInt(3);
                loaded = true;
                primaryKey.setValue(new Integer(itemId));
            }
        } catch (SQLException e) {
            logger.error("load: " + e.getMessage());
        } finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean save() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1, name);
            ps.setInt(6, id);
            re = conn.executePreUpdate()==1 ? true : false;
            if (re) {
                QuestionnaireFormSubitemCache qfic = new QuestionnaireFormSubitemCache(this);
                primaryKey.setValue(new Integer(itemId));
                qfic.refreshSave(primaryKey);
                qfic.refreshList();
            }
        } catch (SQLException e) {
            logger.error("save: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public Vector listSubItems(int itemId) {
        String sql = "select id from oa_questionnaire_form_subitem where item_id= " +
                     StrUtil.sqlstr(String.valueOf(itemId)) + " order by orders asc";
        return list(sql);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getOrders() {
        return orders;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    private String name;
    private int orders = 0;
}
