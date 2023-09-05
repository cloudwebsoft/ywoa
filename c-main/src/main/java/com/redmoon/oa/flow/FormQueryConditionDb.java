package com.redmoon.oa.flow;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.*;

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
public class FormQueryConditionDb extends ObjectDb{
        /**
         * 比较类型，如某职级之上、之下
         */
        public static final int COMPARE_TYPE_NONE = 0;
    public static final int COMPARE_TYPE_ABOVE = 1;
    public static final int COMPARE_TYPE_UNDER = -1;

    public static final int COMPARE_TYPE_NOT_EQUALS = 2;

    public FormQueryConditionDb() {
    }

    public FormQueryConditionDb(int id) {
        this.id = id;
        init();
        load();
    }

    @Override
    public void initDB() {
        tableName = "FORM_QUERY_CONDITION";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_LONG);
        objectCache = new FormQueryConditionCache(this);
        isInitFromConfigDB = false;
        QUERY_CREATE = "insert into " + tableName +
                       " (id,query_id,condition_field_code,condition_sign,condition_value,condition_type,input_value,compare_type) values (?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName +
                     " set query_id=?,condition_field_code=?,condition_sign=?,condition_value=?,condition_type=?,input_value=?,compare_type=? where id=?";
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select id,query_id,condition_field_code,condition_sign,condition_value,condition_type,input_value,compare_type from " +
                     tableName + " where id=?";
    }

    @Override
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new FormQueryConditionDb(pk.getIntValue());
    }

    public FormQueryConditionDb getFormQueryConditionDb(int id) {
        return (FormQueryConditionDb) getObjectDb(new Integer(id));
    }

    @Override
    public boolean create() throws ErrMsgException {
        Conn conn = null;
        boolean re = false;
        try {
            id = SequenceManager.nextID(SequenceManager.OA_FORM_QUERY_CONDITION);
            conn = new Conn(connname);
            PreparedStatement pstmt = conn.prepareStatement(this.QUERY_CREATE);
            pstmt.setLong(1, id);
            pstmt.setInt(2, queryId);
            pstmt.setString(3, conditionFieldCode);
            pstmt.setString(4, conditionSign);
            pstmt.setString(5, conditionValue);
            pstmt.setString(6, conditionType);
            pstmt.setString(7, inputValue);
            pstmt.setInt(8, compareType);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                FormQueryConditionCache aqcc = new FormQueryConditionCache(this);
                aqcc.refreshCreate();
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
            throw new ErrMsgException("插入FormQueryCondition时出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    @Override
    public boolean save() throws ErrMsgException {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        boolean re = false;
        try {
            pstmt = conn.prepareStatement(QUERY_SAVE);
            pstmt.setInt(1, queryId);
            pstmt.setString(2, conditionFieldCode);
            pstmt.setString(3, conditionSign);
            pstmt.setString(4, conditionValue);
            pstmt.setString(5, conditionType);
            pstmt.setString(6, inputValue);
            pstmt.setInt(7, compareType);
            pstmt.setLong(8, id);
            re = conn.executePreUpdate() > 0 ? true : false;
            if (re) {
                FormQueryConditionCache aqcc = new FormQueryConditionCache(this);
                primaryKey.setValue(new Long(id));
                aqcc.refreshSave(primaryKey);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("save:" + e.getMessage());
            throw new ErrMsgException("更新FormQueryCondition时出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    @Override
    public void load() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(this.QUERY_LOAD);
            pstmt.setLong(1, id);
            rs = conn.executePreQuery();
            if (rs.next()) {
                this.id = rs.getInt(1);
                this.queryId = rs.getInt(2);
                this.conditionFieldCode = StrUtil.getNullStr(rs.getString(3));
                this.conditionSign = StrUtil.getNullStr(rs.getString(4));
                this.conditionValue = StrUtil.getNullStr(rs.getString(5));
                this.conditionType = StrUtil.getNullStr(rs.getString(6));
                this.inputValue = StrUtil.getNullStr(rs.getString(7));
                compareType = rs.getInt(8);
                loaded = true;
                primaryKey.setValue(new Long(id));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load:" + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    @Override
    public boolean del() throws ErrMsgException {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        boolean re = false;
        try {
            pstmt = conn.prepareStatement(QUERY_DEL);
            pstmt.setLong(1, id);
            re = conn.executePreUpdate() > 0 ? true : false;
            if (re) {
                FormQueryConditionCache aqcc = new FormQueryConditionCache(this);
                primaryKey.setValue(new Long(id));
                aqcc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del:" + e.getMessage());
            throw new ErrMsgException("删除FormQueryCondition时出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }


    @Override
    public ListResult listResult(String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();
        ListResult lr = new ListResult();
        lr.setTotal(total);
        lr.setResult(result);

        Conn conn = new Conn(connname);
        try {
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(listsql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            if (total != 0) {
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用
            }

            rs = conn.executeQuery(listsql);
            if (rs == null) {
                return lr;
            } else {
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (rs.absolute(absoluteLocation) == false) {
                    return lr;
                }
                do {
                    FormQueryConditionDb aqcd = getFormQueryConditionDb(rs.getInt(1));
                    result.addElement(aqcd);
                } while (rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            throw new ErrMsgException("数据库出错！");
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }

    public ResultIterator getResultIterator(String sqlOfQuery, int curPage,
                                            int pageSize) throws
            ErrMsgException {

        RMConn rmconn = new RMConn(connname);
        ResultIterator ri = null;
        try {
            ri = rmconn.executeQuery(sqlOfQuery, curPage, pageSize);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        return ri;
    }

    @Override
    public Vector list(String sql) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        try {
            rs = conn.executeQuery(sql);
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    result.addElement(getFormQueryConditionDb(rs.getInt(1)));
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("list:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }

    public List<String> listCondFieldByQueryId(long id) {
        List<String> list = new ArrayList<String>();
        String sql = "select distinct condition_field_code from form_query_condition where query_id=" + id;
        String fieldsSelected = "";
        ResultIterator ri = null;
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ri = jt.executeQuery(sql);
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                list.add(rr.getString(1));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return list;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }

    public void setConditionSign(String conditionSign) {
        this.conditionSign = conditionSign;
    }

    public void setConditionFieldCode(String conditionFieldCode) {
        this.conditionFieldCode = conditionFieldCode;
    }

    public void setQueryId(int queryId) {
        this.queryId = queryId;
    }

    public void setInputValue(String inputValue) {
        this.inputValue = inputValue;
    }

    public void setCompareType(int compareType) {
        this.compareType = compareType;
    }

    public long getId() {
        return id;
    }

    public String getConditionType() {
        return conditionType;
    }

    public String getConditionValue() {
        return conditionValue;
    }

    public String getConditionSign() {
        return conditionSign;
    }

    public String getConditionFieldCode() {
        return conditionFieldCode;
    }

    public int getQueryId() {
        return queryId;
    }

    public String getInputValue() {
        return inputValue;
    }

    public int getCompareType() {
        return compareType;
    }

    public String getFieldCode() {
        return conditionFieldCode;
    }

    private long id;
    private int queryId;
    private String conditionFieldCode;
    private String conditionSign;
    private int compareType = 0;
    /**
     * 条件值加''号、SQLFilter.getDateStr(toDate, "yyyy-MM-dd")、StrUtil.sqlstr("%" + inputValue + "%") 
     */
    private String conditionValue;
    private String conditionType;
    
    /**
     * 条件值
     */
    private String inputValue;

}
