package com.redmoon.oa.questionnaire;

import java.sql.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import java.util.Iterator;
import java.util.Vector;
import com.redmoon.oa.db.SequenceManager;

/**
 * 问卷中多选题的答案
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
public class QuestionnaireSubitemDb extends ObjectDb{
    private int subitemId,questionnaireNum,itemId,questionnaireFormId;//子项目编号,问卷编号,父项目编号,问卷表单编号
    private int subitemValue;//子项目值

    public QuestionnaireSubitemDb() {
        init();
    }

    public QuestionnaireSubitemDb(int subitemId) {
        this.subitemId = subitemId;
        init();
        load();
    }

    public void initDB() {
        tableName = "oa_questionnaire_subitem";
        primaryKey = new PrimaryKey("subitemId", PrimaryKey.TYPE_INT);
        objectCache = new QuestionnaireSubitemCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE = "insert into " + tableName + " (subitem_id,questionnaire_num,item_id,subitem_value,questionnaire_form_id) values (?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set subitem_value=? where subitem_id=?";
        QUERY_LIST = "select subitem_id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where subitem_id=?";
        QUERY_LOAD = "select subitem_id,questionnaire_num,item_id,subitem_value,questionnaire_form_id from " + tableName + " where subitem_id=?";
    }

    public QuestionnaireSubitemDb getQuestionnaireSubitemDb(int subitemId) {
        return (QuestionnaireSubitemDb)getObjectDb(new Integer(subitemId));
    }

    public Vector getSubitems(int questionnaireNum) {
        String sql = "select subitem_id from " + tableName + " where questionnaire_num=" + questionnaireNum + " order by questionnaire_num asc";
        return list(sql);
    }

    public boolean create() throws ErrMsgException {
        subitemId = (int) SequenceManager.nextID(SequenceManager.QUESTIONNAIRE_SUBITEM);
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setInt(1, subitemId);
            ps.setInt(2, questionnaireNum);
            ps.setInt(3, itemId);
            ps.setInt(4, subitemValue);
            ps.setInt(5, questionnaireFormId);
            re = conn.executePreUpdate()==1 ? true : false;
            if (re) {
                QuestionnaireSubitemCache qfsc = new QuestionnaireSubitemCache(this);
                qfsc.refreshCreate();
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
        return new QuestionnaireSubitemDb(pk.getIntValue());
    }

    public boolean del() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, subitemId);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                QuestionnaireSubitemCache qfsc = new QuestionnaireSubitemCache(this);
                primaryKey.setValue(new Integer(subitemId));
                qfsc.refreshDel(primaryKey);
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
            ps.setInt(1, subitemId);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                subitemId = rs.getInt(1);
                questionnaireNum = rs.getInt(2);
                itemId = rs.getInt(3);
                subitemValue = rs.getInt(4);
                questionnaireFormId = rs.getInt(5);
                loaded = true;
                primaryKey.setValue(new Integer(subitemId));
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
            ps.setInt(1, subitemValue);
            ps.setInt(2, subitemId);
            re = conn.executePreUpdate()==1 ? true : false;
            if (re) {
                QuestionnaireSubitemCache qfsc = new QuestionnaireSubitemCache(this);
                primaryKey.setValue(new Integer(subitemId));
                qfsc.refreshSave(primaryKey);
                qfsc.refreshList();
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

    public void onDelFormItem(int itemId) throws ErrMsgException {
        String sql = "select subitem_id from " + tableName + " where item_id=" + itemId;
        Iterator ir = list(sql).iterator();
        while (ir.hasNext()) {
            QuestionnaireSubitemDb qsd = (QuestionnaireSubitemDb)ir.next();
            qsd.del();
        }
    }

    public void onDelAnswer(int questionnaireNum) throws ErrMsgException {
        String sql = "select subitem_id from " + tableName + " where questionnaire_num=" + questionnaireNum;
        Iterator ir = list(sql).iterator();
        while (ir.hasNext()) {
            QuestionnaireSubitemDb qsd = (QuestionnaireSubitemDb)ir.next();
            qsd.del();
        }
    }

    public int getItemId() {
        return itemId;
    }

    public int getQuestionnaireNum() {
        return questionnaireNum;
    }

    public int getSubitemId() {
        return subitemId;
    }

    public int getSubitemValue() {
        return subitemValue;
    }

    public int getQuestionnaireFormId() {
        return questionnaireFormId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public void setQuestionnaireNum(int questionnaireNum) {
        this.questionnaireNum = questionnaireNum;
    }

    public void setSubitemId(int subitemId) {
        this.subitemId = subitemId;
    }

    public void setSubitemValue(int subitemValue) {
        this.subitemValue = subitemValue;
    }

    public void setQuestionnaireFormId(int questionnaireFormId) {
        this.questionnaireFormId = questionnaireFormId;
    }
}
