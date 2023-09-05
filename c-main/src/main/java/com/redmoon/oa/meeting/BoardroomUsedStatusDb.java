package com.redmoon.oa.meeting;

import java.sql.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.util.LogUtil;

import java.util.Vector;

public class BoardroomUsedStatusDb extends ObjectDb {
    String connname;

    public BoardroomUsedStatusDb() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).info("FlowTypeDb:默认数据库名为空！");
        isInitFromConfigDB = false;
        init();
    }

    public BoardroomUsedStatusDb(int id){
        isInitFromConfigDB = false;
        this.id = id;
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).info("FlowTypeDb:默认数据库名为空！");
        load();
        init();
    }

    public void initDB() {
        objectCache = new BoardroomUsedStatusCache(this);
        tableName = "boardroom_used_status";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);

        QUERY_LOAD =
            "SELECT boardroomId,flowId,applyUserName,checkUserName,beginDate,endDate,topic FROM " + tableName + " WHERE id=?";
        QUERY_SAVE =
            "update " + tableName + " set boardroomId=?, flowId=?, applyUserName=?, checkUserName=?, beginDate=?, endDate=?, topic=? where id=?";
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_CREATE = "insert into " + tableName + " (boardroomId,flowId,applyUserName,checkUserName,beginDate,endDate,topic) values (?,?,?,?,?,?,?)";
        QUERY_LIST = "select id from " + tableName + " order by beginDate desc";
        isInitFromConfigDB = false;
    }

    public boolean create() throws ErrMsgException {
        Conn conn = null;
        boolean re = false;
        try {
            conn = new Conn(connname);
            PreparedStatement pstmt = conn.prepareStatement(this.QUERY_CREATE);
            pstmt.setInt(1, boardroomId);
            pstmt.setInt(2, flowId);
            pstmt.setString(3, applyUserName);
            pstmt.setString(4, checkUserName);
            pstmt.setString(5, DateUtil.format(beginDate, "yyyy-MM-dd HH:mm:ss"));
            pstmt.setString(6, DateUtil.format(endDate, "yyyy-MM-dd HH:mm:ss"));
            pstmt.setString(7, topic);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                BoardroomUsedStatusCache mc = new BoardroomUsedStatusCache(this);
                mc.refreshCreate();
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
            throw new ErrMsgException("插入时出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new BoardroomUsedStatusDb(pk.getIntValue());
    }

    public boolean del() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        boolean re = false;
        try {
            pstmt = conn.prepareStatement(QUERY_DEL);
            pstmt.setInt(1, id);
            re = conn.executePreUpdate() > 0 ? true : false;
            if (re) {
                BoardroomUsedStatusCache bc = new BoardroomUsedStatusCache(this);
                primaryKey.setValue(new Integer(id));
                bc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public boolean save() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        boolean re = false;
        try {
            // "update " + tableName + " set name=?, personNum, description=?, address=?, equipment=? where id=?";
            pstmt = conn.prepareStatement(QUERY_SAVE);
            // "update " + tableName + " set boardroomId=?, flowId=?, applyUserName=?, checkUserName=?, beginDate=?, endDate=? where id=?";
            pstmt.setInt(1, boardroomId);
            pstmt.setInt(2, flowId);
            pstmt.setString(3, applyUserName);
            pstmt.setString(4, checkUserName);
            pstmt.setString(5, DateUtil.format(beginDate, "yyyy-MM-dd HH:mm:ss"));
            pstmt.setString(6, DateUtil.format(endDate, "yyyy-MM-dd HH:mm:ss"));
            pstmt.setString(7, topic);
            pstmt.setInt(8, id);
            re = conn.executePreUpdate()>0?true:false;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("save:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public void load() {
        // Based on the id in the object, get the message data from the database.
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // "SELECT boardroomId,flowId,applyUserName,checkUserName,beginDate,endDate FROM " + tableName + " WHERE id=?";
            pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            if (!rs.next()) {
                LogUtil.getLog(getClass()).error("load:流程类型 " + id +
                             " 在数据库中未找到.");
            } else {
                boardroomId = rs.getInt(1);
                flowId = rs.getInt(2);
                applyUserName = rs.getString(3);
                checkUserName = rs.getString(4);
                beginDate = rs.getTimestamp(5);
                endDate = rs.getTimestamp(6);
                topic = rs.getString(7);
                loaded = true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load:" + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    @Override
    public ListResult listResult(String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();
        ListResult lr = new ListResult();
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
                conn.setMaxRows(curPage * pageSize); //尽量减少内存的使用

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
                    BoardroomUsedStatusDb lp = getBoardroomUsedStatusDb(rs.getInt(1));
                    result.addElement(lp);
                } while (rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            throw new ErrMsgException("数据库出错！");
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
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

    public BoardroomUsedStatusDb getBoardroomUsedStatusDb(int id) {
        return (BoardroomUsedStatusDb)getObjectDb(new Integer(id));
    }

    public boolean isBoardroomUsing(int boardroomId, java.util.Date beginTime, java.util.Date endTime) {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // "SELECT boardroomId,flowId,applyUserName,checkUserName,beginDate,endDate FROM " + tableName + " WHERE id=?";
            String sql = "select id from " + tableName + " where boardroomId=" + boardroomId + "and beginDate>? and beginDate<?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, boardroomId);
            pstmt.setString(2, DateUtil.format(beginTime, "yyyy-MM-dd HH:mm:ss"));
            pstmt.setString(3, DateUtil.format(endDate, "yyyy-MM-dd HH:mm:ss"));
            rs = conn.executePreQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("isBoardroomUsing:" + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return false;
    }

    public int getId() {
        return id;
    }

    public String getApplyUserName() {
        return applyUserName;
    }

    public String getCheckUserName() {
        return checkUserName;
    }

    public int getFlowId() {
        return flowId;
    }

    public int getBoardroomId() {
        return boardroomId;
    }

    public java.util.Date getBeginDate() {
        return beginDate;
    }

    public java.util.Date getEndDate() {
        return endDate;
    }

    public String getTopic() {
        return topic;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setApplyUserName(String applyUserName) {
        this.applyUserName = applyUserName;
    }

    public void setCheckUserName(String checkUserName) {
        this.checkUserName = checkUserName;
    }

    public void setFlowId(int flowId) {
        this.flowId = flowId;
    }

    public void setBoardroomId(int boardroomId) {
        this.boardroomId = boardroomId;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setBeginDate(java.util.Date beginDate) {
        this.beginDate = beginDate;
    }

    public void setEndDate(java.util.Date endDate) {
        this.endDate = endDate;
    }

    private int id;
    private String applyUserName;
    private String checkUserName;
    private int flowId;
    private int boardroomId;
    private java.util.Date beginDate;
    private java.util.Date endDate;
    private String topic;

}
