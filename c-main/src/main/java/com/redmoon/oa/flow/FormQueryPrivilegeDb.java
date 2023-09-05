package com.redmoon.oa.flow;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
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
public class FormQueryPrivilegeDb extends ObjectDb{
    public static final int TYPE_USER = 0;
    public static final int TYPE_DEPT = 1;
    public static final int TYPE_ROLE = 2;

    public FormQueryPrivilegeDb() {
    }

    public FormQueryPrivilegeDb(int id) {
        this.id = id;
        init();
        load();
    }

    public void initDB() {
        tableName = "FORM_QUERY_PRIVILEGE";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new FormQueryPrivilegeCache(this);
        isInitFromConfigDB = false;
        QUERY_CREATE = "insert into " + tableName +
                       " (user_name,priv_type,add_date,query_id) values (?,?,?,?)";
        QUERY_SAVE = "update " + tableName +
                     " set user_name=?,priv_type=? where id=?";
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select id,user_name,priv_type,add_date,query_id from " +
                     tableName + " where id=?";
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new FormQueryPrivilegeDb(pk.getIntValue());
    }

    public FormQueryPrivilegeDb getFormQueryPrivilegeDb(int id) {
        return (FormQueryPrivilegeDb) getObjectDb(new Integer(id));
    }

    public boolean create() throws ErrMsgException {
        Conn conn = null;
        boolean re = false;
        try {
            conn = new Conn(connname);
            PreparedStatement pstmt = conn.prepareStatement(this.QUERY_CREATE);
            pstmt.setString(1, userName);
            pstmt.setInt(2, type);
            pstmt.setTimestamp(3, new Timestamp(new java.util.Date().getTime()));
            pstmt.setInt(4, queryId);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                FormQueryPrivilegeCache apc = new FormQueryPrivilegeCache(this);
                apc.refreshCreate();
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public boolean save() throws ErrMsgException {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        boolean re = false;
        try {
            pstmt = conn.prepareStatement(QUERY_SAVE);
            pstmt.setString(1, userName);
            pstmt.setInt(2, type);
            pstmt.setInt(3, id);
            re = conn.executePreUpdate() > 0 ? true : false;
            if (re) {
                FormQueryPrivilegeCache apc = new FormQueryPrivilegeCache(this);
                primaryKey.setValue(new Integer(id));
                apc.refreshSave(primaryKey);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;

    }

    public boolean delOfQuery(int queryId) throws ErrMsgException {
        boolean re = false;
        Vector vt = list(FormSQLBuilder.getQueryPrivilege(queryId));
        Iterator ir = null;
        ir = vt.iterator();
        while (ir != null && ir.hasNext()) {
            FormQueryPrivilegeDb apd = (FormQueryPrivilegeDb) ir.next();
            re = apd.del();
        }
        return re;
    }

    public void load() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(this.QUERY_LOAD);
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs.next()) {
                id = rs.getInt(1);
                userName = StrUtil.getNullStr(rs.getString(2));
                type = rs.getInt(3);
                addDate = rs.getTimestamp(4);
                loaded = true;
                primaryKey.setValue(new Integer(id));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean del() throws ErrMsgException {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        boolean re = false;
        try {
            pstmt = conn.prepareStatement(QUERY_DEL);
            pstmt.setInt(1, id);
            re = conn.executePreUpdate() > 0 ? true : false;
            if (re) {
                FormQueryPrivilegeCache apc = new FormQueryPrivilegeCache(this);
                primaryKey.setValue(new Integer(id));
                apc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
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

            if (total != 0)
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用

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
                    FormQueryPrivilegeDb apd = getFormQueryPrivilegeDb(rs.getInt(1));
                    result.addElement(apd);
                } while (rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
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
                    result.addElement(getFormQueryPrivilegeDb(rs.getInt(1)));
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setAddDate(java.util.Date addDate) {
        this.addDate = addDate;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setQueryId(int queryId) {
        this.queryId = queryId;
    }

    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public java.util.Date getAddDate() {
        return addDate;
    }

    public int getType() {
        return type;
    }

    public int getQueryId() {
        return queryId;
    }

    private int id;
    private String userName;
    private int type;
    private java.util.Date addDate;
    private int queryId;

}
