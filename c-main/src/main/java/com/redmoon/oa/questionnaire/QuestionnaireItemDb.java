package com.redmoon.oa.questionnaire;

import java.sql.*;
import java.sql.Date;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.redmoon.oa.db.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * 问卷填写，填空和单选题
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
public class QuestionnaireItemDb extends ObjectDb {
    private int questionnaireNum,itemId;//问卷编号，项目编号
    private String itemValue;//项目值
    private java.sql.Date fillDate;//填写日期
    private int id;
    private String userName;
    private int formId;
    
    private int weight = 1;
    
    private String kind = "";

    public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public QuestionnaireItemDb() {
        init();
    }

    public QuestionnaireItemDb(int id) {
        this.id = id;
        init();
        load();
    }

    public void initDB() {
        tableName = "oa_questionnaire_item";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new QuestionnaireItemCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE = "insert into " + tableName + " (questionnaire_num,item_id,item_value,fill_date,id,user_name,form_id,weight,kind) values (?,?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set item_value=? where id=?";
        QUERY_LIST = "select id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select questionnaire_num,item_id,item_value,fill_date,id,user_name,form_id,weight,kind from " + tableName + " where id=?";
    }

    /**
     * 取得用户参与的答卷ID
     * @param userName String
     * @return int
     */
    public int getUserAttended(int formId, String userName) {
        String sql = "select questionnaire_num from " + tableName + " where form_id=? and user_name=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[] {new Integer(formId), userName});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                return rr.getInt(1);
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return -1;
    }

    public QuestionnaireItemDb getQuestionnaireItemDb(int id) {
        return (QuestionnaireItemDb)getObjectDb(new Integer(id));
    }

    public boolean create() throws ErrMsgException {
        id = (int) SequenceManager.nextID(SequenceManager.QUESTIONNAIRE_ITEM);
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setInt(1, questionnaireNum);
            ps.setInt(2, itemId);
            ps.setString(3, itemValue);
            fillDate = new java.sql.Date(System.currentTimeMillis());
            ps.setDate(4, fillDate);
            ps.setInt(5, id);
            ps.setString(6, userName);
            ps.setInt(7, formId);
            ps.setInt(8, weight);
            ps.setString(9, kind);
            re = conn.executePreUpdate()==1 ? true : false;
            if (re) {
                QuestionnaireItemCache qic = new QuestionnaireItemCache(this);
                qic.refreshCreate();
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
        return new QuestionnaireItemDb(pk.getIntValue());
    }

    public void onDelFormItem(int itemId) throws ErrMsgException {
        String sql = "select id from " + tableName + " where item_id=" + itemId;
        Iterator ir = list(sql).iterator();
        while (ir.hasNext()) {
            QuestionnaireItemDb qid = (QuestionnaireItemDb)ir.next();
            qid.del();
        }
        QuestionnaireSubitemDb qsd = new QuestionnaireSubitemDb();
        qsd.onDelFormItem(itemId);
    }

    public void delAnswer(int questionnaireNum) throws ErrMsgException {
        String sql = "select id from " + tableName + " where questionnaire_num=" + questionnaireNum;
        Iterator ir = list(sql).iterator();
        while (ir.hasNext()) {
            QuestionnaireItemDb qid = (QuestionnaireItemDb)ir.next();
            qid.del();
        }
        QuestionnaireSubitemDb qsd = new QuestionnaireSubitemDb();
        qsd.onDelAnswer(questionnaireNum);
    }

    public boolean del() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                QuestionnaireItemCache qic = new QuestionnaireItemCache(this);
                primaryKey.setValue(new Integer(id));
                qic.refreshDel(primaryKey);
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
                questionnaireNum = rs.getInt(1);
                itemId = rs.getInt(2);
                itemValue = rs.getString(3);
                fillDate = rs.getDate(4);
                id = rs.getInt(5);
                userName = rs.getString(6);
                formId = rs.getInt(7);
                weight = rs.getInt(8);
                kind = StrUtil.getNullStr(rs.getString(9));
                loaded = true;
                primaryKey.setValue(new Integer(id));
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
            ps.setString(1, itemValue);
            re = conn.executePreUpdate()==1 ? true : false;
            if (re) {
                QuestionnaireItemCache qic = new QuestionnaireItemCache(this);
                primaryKey.setValue(new Integer(id));
                qic.refreshSave(primaryKey);
                qic.refreshList();
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

    public java.sql.Date getFillDate() {
        return fillDate;
    }

    public int getId() {
        return id;
    }

    public int getItemId() {
        return itemId;
    }

    public String getItemValue() {
        return itemValue;
    }

    public int getQuestionnaireNum() {
        return questionnaireNum;
    }

    public String getUserName() {
        return userName;
    }

    public int getFormId() {
        return formId;
    }

    public void setFillDate(Date fillDate) {
        this.fillDate = fillDate;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }

    public void setQuestionnaireNum(int questionnaireNum) {
        this.questionnaireNum = questionnaireNum;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setFormId(int formId) {
        this.formId = formId;
    }

	/**
	 * @param kind the kind to set
	 */
	public void setKind(String kind) {
		this.kind = kind;
	}

	/**
	 * @return the kind
	 */
	public String getKind() {
		return kind;
	}
}
