package com.redmoon.oa.meeting;

import java.sql.*;
import java.util.*;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.visual.FormDAO;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;

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

public class BoardroomApplyDb extends ObjectDb {

    public BoardroomApplyDb() {
    }

    public BoardroomApplyDb(int id) {
        this.id = id;
        initDB();
        load();
    }

    public BoardroomApplyDb getBoardroomApplyDb(int id) {
        return (BoardroomApplyDb) getObjectDb(new Integer(id));
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new BoardroomApplyDb(pk.getIntValue());
    }

    public void initDB() {
        this.tableName = "ft_hysqd";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new BoardroomApplyCache(this);

        this.QUERY_LOAD =
                "SELECT id, flowId, meeting_title,sqren,hycontent,chrenyuan,bzwpyq,hyshi,apply_date,start_date,end_date,chrs,spyjian,myresult FROM " +
                tableName + " WHERE id=?";
        this.QUERY_LIST = "SELECT id FROM " + tableName;
        isInitFromConfigDB = false;
    }

    public boolean create() throws ErrMsgException {
        return false;
    }

    public boolean save() throws ErrMsgException {
        return false;
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
                try {
                	this.flowId = rs.getInt("flowId");
                    this.meeting_title = StrUtil.getNullStr(rs.getString(
                            "meeting_title"));
                    this.sqren = StrUtil.getNullStr(rs.getString("sqren"));
                    this.hycontent = StrUtil.getNullStr(rs.getString(
                            "hycontent"));
                    this.chrenyuan = StrUtil.getNullStr(rs.getString(
                            "chrenyuan"));
                    this.bzwpyq = StrUtil.getNullStr(rs.getString("bzwpyq"));
                    this.hyshi = StrUtil.getNullStr(rs.getString("hyshi"));
                    Timestamp ts = rs.getTimestamp("apply_date");
                    if (ts == null)
                        this.apply_date = null;
                    else {
                        this.apply_date = ts;
                    }
                    ts = rs.getTimestamp("start_date");
                    if (ts == null)
                        this.start_date = null;
                    else {
                        this.start_date = ts;
                    }
                    ts = rs.getTimestamp("end_date");
                    if (ts == null) {
                        this.end_date = null;
                    } else {
                        this.end_date = ts;
                    }
                } catch (SQLException e) {
                    LogUtil.getLog(getClass()).error("load1:" + e.getMessage());
                }
                this.chrs = StrUtil.getNullStr(rs.getString("chrs"));
                this.spyjian = StrUtil.getNullStr(rs.getString("spyjian"));
                this.result = StrUtil.getNullStr(rs.getString("myresult"));
                loaded = true;
                primaryKey.setValue(new Integer(id));

            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
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

    public boolean del() throws ErrMsgException {
        return false;
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
                    BoardroomApplyDb vd = getBoardroomApplyDb(rs.getInt(1));
                    result.addElement(vd);
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
    
	public boolean isRoomInUse(int id, String startDate, String endDate) throws ErrMsgException{
		FormDAO fdao = new FormDAO();
		// 此前的会议申请结束时间晚于当前申请的开始时间且此前的会议申请开始时间早于当前申请的结束时间,且审批状态为,未审批,已通过,正在使用
		String sql = "select id from ft_hysqd where hyshi="
				+ StrUtil.sqlstr("" + id) + " and end_date>"
				+ SQLFilter.getDateStr(startDate, "yyyy-MM-dd HH:mm:ss")
				+ " and start_date<"
				+ SQLFilter.getDateStr(endDate, "yyyy-MM-dd HH:mm:ss")
				+ " and myresult in ("
				+ StrUtil.sqlstr(BoardroomSQLBuilder.RESULT_APPLY) + ","
				+ StrUtil.sqlstr(BoardroomSQLBuilder.RESULT_AGREE) + ","
				+ StrUtil.sqlstr(BoardroomSQLBuilder.RESULT_USED) + ")";
		try {
			Vector vec = fdao.list("hysqd", sql);
			Iterator it = vec.iterator();
			while (it.hasNext()) {
				FormDAO dao = (FormDAO) it.next();
				WorkflowDb wfDb = new WorkflowDb((int) dao.getFlowId());
				if (wfDb.isStarted()) {
					return true;
				}
			}
		} catch (ErrMsgException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			throw e;
		}
		return false;
	}

    public void setSqren(String sqren) {
        this.sqren = sqren;
    }

    public void setFlowId(int flowId) {
        this.flowId = flowId;
    }

    public void setHycontent(String hycontent) {
        this.hycontent = hycontent;
    }

    public void setChrenyuan(String chrenyuan) {
        this.chrenyuan = chrenyuan;
    }

    public void setBzwpyq(String bzwpyq) {
        this.bzwpyq = bzwpyq;
    }

    public void setHyshi(String hyshi) {
        this.hyshi = hyshi;
    }

    public void setApply_date(java.util.Date apply_date) {
        this.apply_date = apply_date;
    }

    public void setStart_date(java.util.Date start_date) {
        this.start_date = start_date;
    }

    public void setEnd_date(java.util.Date end_date) {
        this.end_date = end_date;
    }

    public void setMeetingTitle(String meeting_title) {
        this.meeting_title = meeting_title;
    }

    public void setChrs(String chrs) {
        this.chrs = chrs;
    }

    public void setSpyjian(String spyjian) {
        this.spyjian = spyjian;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getSqren() {
        return sqren;
    }

    public int getFlowId() {
        return flowId;
    }

    public String getHycontent() {
        return hycontent;
    }

    public String getChrenyuan() {
        return chrenyuan;
    }

    public String getBzwpyq() {
        return bzwpyq;
    }

    public String getHyshi() {
        return hyshi;
    }

    public java.util.Date getApply_date() {
        return apply_date;
    }

    public java.util.Date getStart_date() {
        return start_date;
    }

    public java.util.Date getEnd_date() {
        return end_date;
    }

    public String getMeetingTitle() {
        return meeting_title;
    }

    public String getChrs() {
        return chrs;
    }

    public String getSpyjian() {
        return spyjian;
    }

    public String getResult() {
        return result;
    }

    private int flowId;
    private String hycontent;
    private String chrenyuan;
    private String bzwpyq;
    private String hyshi;
    private java.util.Date apply_date;
    private java.util.Date start_date;
    private java.util.Date end_date;
    private String meeting_title;
    private String sqren;
    private String chrs;
    private String spyjian;
    private String result;
    
    private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
