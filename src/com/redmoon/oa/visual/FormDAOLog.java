package com.redmoon.oa.visual;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Vector;

import cn.js.fan.db.Conn;
import cn.js.fan.db.ListResult;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;

public class FormDAOLog {
	public static final int LOG_TYPE_CREATE = 0;
	FormDb fd;
	Vector fields;
	long id;
	
	/**
	 * 保留历史记录类型：修改记录
	 */
	public static final int LOG_TYPE_EDIT = 1;
	/**
	 * 保留历史记录类型：删除记录
	 */	
	public static final int LOG_TYPE_DEL = 2;

    private String cwsId;
    private int cwsOrder = 0;
    private String unitCode;
    private String creator;
    
    private long flowId;
    
    private String flowTypeCode;
    
    public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCwsId() {
		return cwsId;
	}

	public void setCwsId(String cwsId) {
		this.cwsId = cwsId;
	}

	public int getCwsOrder() {
		return cwsOrder;
	}

	public void setCwsOrder(int cwsOrder) {
		this.cwsOrder = cwsOrder;
	}

	public String getUnitCode() {
		return unitCode;
	}

	public void setUnitCode(String unitCode) {
		this.unitCode = unitCode;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public long getFlowId() {
		return flowId;
	}

	public void setFlowId(long flowId) {
		this.flowId = flowId;
	}

	boolean loaded = false;
	private String logUser;
	private int logType;
	private java.util.Date logDate;
	private long logId;
    
    public FormDAOLog(FormDb fd) {
    	this.fd = fd;
        fields = fd.getFields();
    }  
    
    public FormDAOLog getFormDAOLog(long id) {
        FormDAOLog fdl = new FormDAOLog(fd);
        fdl.setId(id);
        fdl.load();
        return fdl;
    }    
    
    public FormDb getFormDb() {
    	return fd;
    }
    
    /**
     * 根据SQL语句列出表单编码为formCode的分页记录
     * @param formCode String 表单编码
     * @param listsql String SQL语句
     * @param curPage int 当前页码
     * @param pageSize int 每页记录数
     * @return ListResult
     * @throws ErrMsgException
     */
    public ListResult listResult(String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();

        ListResult lr = new ListResult();
        lr.setTotal(total);
        lr.setResult(result);

        Conn conn = new Conn(Global.getDefaultDB());
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
                    // logger.info("listResult: id=" + rs.getInt(1));
                    FormDAOLog fdaoLog = getFormDAOLog(rs.getLong(1));
                    result.addElement(fdaoLog);
                } while (rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("listResult:" + StrUtil.trace(e));
            throw new ErrMsgException("数据库出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }	
    
    public void load() {
        String fds = "";
        Iterator ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField)ir.next();
            if (fds.equals("")) {
                fds = ff.getName();
            }
            else {
                fds += "," + ff.getName();
            }
        }
        Conn conn = new Conn(Global.getDefaultDB());
        String sql = "select " + fds + ",cws_creator,cws_id,cws_order,unit_code,flowId,cws_log_user,cws_log_type,cws_log_date,cws_log_id,flowTypeCode from " + FormDb.getTableNameForLog(fd.getCode()) + " where id=?";
        ResultSet rs = null;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    ir = fields.iterator();
                    int k = 1;
                    while (ir.hasNext()) {
                        FormField ff = (FormField) ir.next();
                        try {
                            if (ff.getType().equals(ff.TYPE_DATE)) {
                                java.sql.Date dt = rs.getDate(k);
                                ff.setValue(DateUtil.format(dt,
                                        "yyyy-MM-dd"));
                            } else if (ff.getType().equals(ff.TYPE_DATE_TIME)) {
                                Timestamp ts = rs.getTimestamp(k);
                                String d = "";
                                if (ts != null)
                                    d = DateUtil.format(new java.util.Date(ts.
                                            getTime()), "yyyy-MM-dd HH:mm:ss");
                                ff.setValue(d);
                                // ff.setValue(rs.getString(k).substring(0, 19));
                            } else {
                                // logger.info("load: id=" + id + " rs.getString(" + k + ")=" + rs.getString(k));
                                ff.setValue(rs.getString(k));
                            }
                        } catch (SQLException e) {
                            // 以免出现如下问题：load:Value '0000-00-00' can not be represented as java.sql.Timestamp
                            LogUtil.getLog(getClass()).error("load1:" + e.getMessage());
                        }
                        k++;
                    }

                    creator = StrUtil.getNullStr(rs.getString(k));
                    cwsId = StrUtil.getNullStr(rs.getString(k+1));
                    cwsOrder = rs.getInt(k+2);
                    unitCode = rs.getString(k+3);
                    flowId = rs.getLong(k+4);
                    
                    // cws_log_user,cws_log_type,cws_log_date,cws_log_id
                    setLogUser(rs.getString(k+5));
                    setLogType(rs.getInt(k+6));
                    setLogDate(rs.getTimestamp(k+7));
                    setLogId(rs.getLong(k+8));
                    flowTypeCode = StrUtil.getNullStr(rs.getString(k+9));
                    loaded = true;
                }
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load:" + StrUtil.trace(e));
        }
        finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }    
    
    public String getFieldValue(String fieldName) {
        Iterator ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField)ir.next();
            // System.out.println(getClass() + " " + ff.getName() + " " + ff.getValue());
            if (ff.getName().equals(fieldName))
                return StrUtil.getNullStr(ff.getValue());
        }
        return null;
    }
    
    public boolean del() {
        Conn conn = new Conn(Global.getDefaultDB());
        String sql = "delete from " + FormDb.getTableNameForLog(fd.getCode()) + " where id=?";
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            re = conn.executePreUpdate()==1?true:false;
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }    

	public void setLogUser(String logUser) {
		this.logUser = logUser;
	}

	public String getLogUser() {
		return logUser;
	}

	public void setLogType(int logType) {
		this.logType = logType;
	}

	public int getLogType() {
		return logType;
	}

	public void setLogDate(java.util.Date logDate) {
		this.logDate = logDate;
	}

	public java.util.Date getLogDate() {
		return logDate;
	}

	public void setLogId(long logId) {
		this.logId = logId;
	}

	public long getLogId() {
		return logId;
	}    
	
	public boolean isLoaded() {
		return loaded;
	}
	
	public String getFlowTypeCode() {
		return flowTypeCode;
	}
}
