package com.redmoon.oa.questionnaire;

import java.sql.*;
import java.util.Vector;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.redmoon.oa.db.SequenceManager;

public class QuestionnaireFormDb extends ObjectDb {
        /**
         * 表单编号
         */
        private int formId;

    private String formName,description;//表单名称，表单说明
    private java.sql.Date createDate;//表单生成日期

    public QuestionnaireFormDb() {
        init();
    }

    public QuestionnaireFormDb(int formId) {
        this.formId = formId;
        init();
        load();
    }

    public void initDB() {
        tableName = "oa_questionnaire_form";
        primaryKey = new PrimaryKey("formId", PrimaryKey.TYPE_INT);
        objectCache = new QuestionnaireFormCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE = "insert into " + tableName + " (form_id,form_name,description,create_date,is_open,unit_code,begin_date,end_date,is_public) values (?,?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set form_name=?,description=?,is_open=?,begin_date=?,end_date=?,is_public=? where form_id=?";
        QUERY_LIST = "select form_id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where form_id=?";
        QUERY_LOAD = "select form_id,form_name,description,create_date,is_open,unit_code,begin_date,end_date,is_public from " + tableName + " where form_id=?";
    }

    public QuestionnaireFormDb getQuestionnaireFormDb(int formId) {
        return (QuestionnaireFormDb)getObjectDb(new Integer(formId));
    }

    public boolean create() throws ErrMsgException {
        formId = (int) SequenceManager.nextID(SequenceManager.QUESTIONNAIRE_FORM);
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setInt(1, formId);
            ps.setString(2, formName);
            ps.setString(3, description);
            createDate = new java.sql.Date(System.currentTimeMillis());
            ps.setDate(4, createDate);
            ps.setInt(5, open?1:0);
            ps.setString(6, unitCode);
            ps.setTimestamp(7, new Timestamp(beginDate.getTime()));
            ps.setTimestamp(8, new Timestamp(endDate.getTime()));
            ps.setInt(9, pub?1:0);
            re = conn.executePreUpdate()==1 ? true : false;
            if (re) {
                QuestionnaireFormCache qfc = new QuestionnaireFormCache(this);
                qfc.refreshCreate();
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
        return new QuestionnaireFormDb(pk.getIntValue());
    }

    public boolean del() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, formId);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                QuestionnaireFormCache qfc = new QuestionnaireFormCache(this);
                primaryKey.setValue(new Integer(formId));
                qfc.refreshDel(primaryKey);
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
            ps.setInt(1, formId);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                formId = rs.getInt(1);
                formName = rs.getString(2);
                description = rs.getString(3);
                createDate = rs.getDate(4);
                open = rs.getInt(5)==1;
                unitCode = rs.getString(6);
                beginDate = rs.getTimestamp(7);
                endDate = rs.getTimestamp(8);
                pub = rs.getInt(9)==1;
                loaded = true;
                primaryKey.setValue(new Integer(formId));
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
            ps.setString(1, formName);
            ps.setString(2, description);
            ps.setInt(3, open?1:0);
            ps.setTimestamp(4, new Timestamp(beginDate.getTime()));
            ps.setTimestamp(5, new Timestamp(endDate.getTime()));
            ps.setInt(6, pub?1:0);
            ps.setInt(7, formId);
            re = conn.executePreUpdate()==1 ? true : false;
            if (re) {
                QuestionnaireFormCache qfc = new QuestionnaireFormCache(this);
                primaryKey.setValue(new Integer(formId));
                qfc.refreshSave(primaryKey);
                qfc.refreshList();
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

    public java.sql.Date getCreateDate() {
        return createDate;
    }

    public String getDescription() {
        return description;
    }

    public int getFormId() {
        return formId;
    }

    public String getFormName() {
        return formName;
    }

    public boolean isOpen() {
        return open;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFormId(int formId) {
        this.formId = formId;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    private boolean open = true;
    
    public java.util.Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(java.util.Date beginDate) {
		this.beginDate = beginDate;
	}

	public java.util.Date getEndDate() {
		return endDate;
	}

	public void setEndDate(java.util.Date endDate) {
		this.endDate = endDate;
	}

	private String unitCode;
    
    java.util.Date beginDate;
    java.util.Date endDate;
    
    private boolean pub;

	public String getUnitCode() {
		return unitCode;
	}

	public void setUnitCode(String unitCode) {
		this.unitCode = unitCode;
	}

	public void setPublic(boolean pub) {
		this.pub = pub;
	}

	public boolean isPublic() {
		return pub;
	}
}
