package com.redmoon.oa.notice;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.Conn;
import cn.js.fan.db.KeyUnit;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.dept.DeptDb;

public class NoticeDeptDb extends ObjectDb{

    private long noticeId;
    private String deptCode;
    String connname= Global.getDefaultDB();

    public NoticeDeptDb() {
        super();
    }

    public NoticeDeptDb(String deptCode, long noticeId) {
        this.deptCode = deptCode;
        this.noticeId = noticeId;
        init();
        load();
    }

    public void initDB() {
        tableName = "oa_notice_dept";

        HashMap key = new HashMap();
        // 注意key的顺序一定要设置
        //key.put("dept_code", new KeyUnit(primaryKey.TYPE_STRING, 0));
        //key.put("notice_id", new KeyUnit(primaryKey.TYPE_LONG, 1));
        key.put("notice_id", new KeyUnit(primaryKey.TYPE_LONG, 0));
        key.put("dept_code", new KeyUnit(primaryKey.TYPE_STRING, 1));
        primaryKey = new PrimaryKey(key);

        objectCache = new NoticeDeptCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE =
                "insert into " + tableName + " (notice_id, dept_code) values(?,?)";
        QUERY_SAVE = "update " + tableName + " where dept_code=?,notice_id=?";
        QUERY_LIST =
                "select id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where dept_code=?, notice_id=?";
        QUERY_LOAD = "select dept_code,notice_id from " +
                     tableName + " where dept_code=?,notice_id=?";
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new NoticeDb(pk.getIntValue());
    }

    public boolean delOfNotice(long noticeId) {
        String sql = "delete from " + tableName + " where notice_id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, noticeId);
            re = conn.executePreUpdate() >= 0 ? true : false;
            if (re) {
                NoticeDeptCache rc = new NoticeDeptCache(this);
                rc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public Vector getDeptOfNotice(long noticeId) {
        String sql = "select dept_code from " + tableName + " where notice_id=?";
        ResultSet rs = null;
        DeptDb dd = new DeptDb();
        Vector v = new Vector();
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, noticeId);
            rs = conn.executePreQuery();
            while (rs.next()) {
                String dCode = rs.getString(1);
                v.addElement(dd.getDeptDb(dCode));
            }
            return v;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getDeptOfNotice: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    public boolean del() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            //ps.setString(1, deptCode);
            //ps.setLong(2, noticeId);
            ps.setLong(1, noticeId);
            ps.setString(2, deptCode);
            re = conn.executePreUpdate() >= 1 ? true : false;
            if (re) {
                NoticeDeptCache rc = new NoticeDeptCache(this);
                rc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public boolean save() throws ErrMsgException {
        return true;
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            //name, type,  number, typeId , addId, department, buyMan,
            //keeper, inputMan,startDate, buyDate, regDate,abstracts
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            //ps.setString(1, deptCode);
            //ps.setLong(2, noticeId);
            ps.setLong(1, noticeId);
            ps.setString(2, deptCode);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                //deptCode = rs.getString(1);
                //noticeId = rs.getLong(2);
                noticeId = rs.getLong(1);
                deptCode = rs.getString(2);
                loaded = true;
                //primaryKey.setKeyValue("dept_code", deptCode);
                //primaryKey.setKeyValue("notice_id", new Long(noticeId));
                primaryKey.setKeyValue("notice_id", new Long(noticeId));
                primaryKey.setKeyValue("dept_code", deptCode);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }


    public boolean create() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_CREATE);
            //pstmt.setString(1, deptCode);
            //pstmt.setLong(2, noticeId);
            pstmt.setLong(1, noticeId);
            pstmt.setString(2, deptCode);
            re = conn.executePreUpdate() >= 1 ? true : false;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public void setNoticeId(long noticeId) {
        this.noticeId = noticeId;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public long getNoticeId() {
        return noticeId;
    }

    public String getDeptCode() {
        return deptCode;
    }


}
