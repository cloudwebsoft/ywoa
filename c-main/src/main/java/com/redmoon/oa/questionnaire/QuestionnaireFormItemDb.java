package com.redmoon.oa.questionnaire;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
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
public class QuestionnaireFormItemDb extends ObjectDb {
    /**
     * 项目编号
     */
    private int itemId;
    /**
     * 所属表单编号
     */
    private int formId;
    /**
     * 项目类型
     */
    private int itemType;
    /**
     * 项目索引
     */
    private int itemIndex;
    /**
     * 校验类型
     */
    private int checkedType;
    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 填空型
     */
    public static final int ITEM_TYPE_INPUT = 0;
    /**
     * 问答型
     */
    public static final int ITEM_TYPE_TEXTAREA = 1;
    /**
     * 单选
     */
    public static final int ITEM_TYPE_RADIO_GROUP = 2;
    /**
     * 多选
     */
    public static final int ITEM_TYPE_CHECKBOX = 3;
    /**
     * 是否必填项
     */
    public static final int MUST_BE_FILLED = 0;

    public QuestionnaireFormItemDb() {
        init();
    }

    public QuestionnaireFormItemDb(int itemId) {
        this.itemId = itemId;
        init();
        load();
    }

    public void initDB() {
        tableName = "oa_questionnaire_form_item";
        primaryKey = new PrimaryKey("itemId", PrimaryKey.TYPE_INT);
        objectCache = new QuestionnaireFormItemCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE = "insert into " + tableName + " (item_id,form_id,item_name,item_type,item_index,checked_type) values (?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set item_name=?,item_type=?,item_index=?,checked_type=? where item_id=?";
        QUERY_LIST = "select item_id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where item_id=?";
        QUERY_LOAD = "select item_id,form_id,item_name,item_type,item_index,checked_type from " + tableName + " where item_id=?";
    }

    public QuestionnaireFormItemDb getQuestionnaireFormItemDb(int itemId) {
        return (QuestionnaireFormItemDb)getObjectDb(new Integer(itemId));
    }

    public boolean create() throws ErrMsgException {
        itemId = (int) SequenceManager.nextID(SequenceManager.QUESTIONNAIRE_FORM_ITEM);
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setInt(1, itemId);
            ps.setInt(2, formId);
            ps.setString(3, itemName);
            ps.setInt(4, itemType);
            ps.setInt(5, itemIndex);
            ps.setInt(6, checkedType);
            re = conn.executePreUpdate()==1 ? true : false;
            if (re) {
                QuestionnaireFormItemCache qfic = new QuestionnaireFormItemCache(this);
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
        return new QuestionnaireFormItemDb(pk.getIntValue());
    }

    public boolean del() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, itemId);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                QuestionnaireFormItemCache qfic = new QuestionnaireFormItemCache(this);
                primaryKey.setValue(new Integer(itemId));
                qfic.refreshDel(primaryKey);

                QuestionnaireItemDb qid = new QuestionnaireItemDb();
                qid.onDelFormItem(itemId);
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
            ps.setInt(1, itemId);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                itemId = rs.getInt(1);
                formId = rs.getInt(2);
                itemName = rs.getString(3);
                itemType = rs.getInt(4);
                itemIndex = rs.getInt(5);
                checkedType = rs.getInt(6);
                loaded = true;
                primaryKey.setValue(new Integer(itemId));

                QuestionnaireFormSubitemDb qfsd = new QuestionnaireFormSubitemDb();
                subItems = qfsd.listSubItems(itemId);
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
            ps.setString(1, itemName);
            ps.setInt(2, itemType);
            ps.setInt(3, itemIndex);
            ps.setInt(4, checkedType);
            ps.setInt(5, itemId);
            re = conn.executePreUpdate()==1 ? true : false;
            if (re) {
                QuestionnaireFormItemCache qfic = new QuestionnaireFormItemCache(this);
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

    public int getFormId() {
        return formId;
    }

    public int getItemId() {
        return itemId;
    }

    public int getItemIndex() {
        return itemIndex;
    }

    public String getItemName() {
        return itemName;
    }

    public int getItemType() {
        return itemType;
    }

    public int getCheckedType() {
        return checkedType;
    }

    public Vector getSubItems() {
        return subItems;
    }

    public void setFormId(int formId) {
        this.formId = formId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public void setItemIndex(int itemIndex) {
        this.itemIndex = itemIndex;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public void setCheckedType(int checkedType) {
        this.checkedType = checkedType;
    }

    public void setSubItems(Vector subItems) {
        this.subItems = subItems;
    }

    private Vector subItems;
}
