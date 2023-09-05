package com.redmoon.oa.visual;

import cn.js.fan.db.Conn;
import cn.js.fan.db.ListResult;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IAttachment;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

public class FormDAOLog implements IFormDAO {

	public static final int LOG_TYPE_CREATE = 0;
	FormDb fd;
	Vector<FormField> fields;
	long id;
	
	/**
	 * 保留历史记录类型：修改记录
	 */
	public static final int LOG_TYPE_EDIT = 1;
	/**
	 * 保留历史记录类型：删除记录
	 */	
	public static final int LOG_TYPE_DEL = 2;

    /**
     * 浏览类型：模块
     */
	public static final int READ_TYPE_MODULE = 0;
    /**
     * 浏览类型：报表
     */
	public static final int READ_TYPE_REPORT = 1;

    private String cwsId;
    private int cwsOrder = 0;
    private String unitCode;
    private String creator;
    
    private int flowId;
    
    private String flowTypeCode;

    @Override
    public long getIdentifier() {
        return id;
    }

    @Override
    public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
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

	@Override
    public int getFlowId() {
		return flowId;
	}

    @Override
    public int getCwsFlag() {
        return -1000;
    }

    @Override
    public int getCwsStatus() {
        return -1000;
    }

    @Override
    public FormField getFormField(String fieldName) {
        for (FormField ff : fields) {
            // LogUtil.getLog(getClass()).info(getClass() + " " + ff.getName() + " " + ff.getValue());
            if (ff.getName().equals(fieldName)) {
                return ff;
            }
        }
        return null;
    }

    public void setFlowId(int flowId) {
		this.flowId = flowId;
	}

    @Override
    public Vector<FormField> getFields() {
        return fields;
    }

    @Override
    public String getVisualPath() {
        return "";
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
    
    @Override
    public FormDb getFormDb() {
    	return fd;
    }
    
    /**
     * 根据SQL语句列出表单编码为formCode的分页记录
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
        Vector<FormDAOLog> result = new Vector<>();

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

            if (total != 0) {
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用
            }

            rs = conn.executeQuery(listsql);
            if (rs == null) {
                return lr;
            } else {
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (!rs.absolute(absoluteLocation)) {
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
            conn.close();
        }

        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }

    /**
     * 取得到达时间之前的最后一条历史记录
     * @param flowId
     * @param receiveDate
     * @return
     */
    public FormDAOLog getLastBeforeReceiveDate(int flowId, Date receiveDate) {
        String logTable = FormDb.getTableNameForLog(fd.getCode());
        String sql = "select id from " + logTable + " where flowId=" + flowId + " and cws_log_date < " + SQLFilter.getDateStr(DateUtil.format(receiveDate, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss");
        ListResult lr = listResult(sql, 1, 1);
        if (lr.getResult().size() > 0) {
            return (FormDAOLog)lr.getResult().elementAt(0);
        } else {
            return null;
        }
    }

    public void load() {
        String fds = "";
        for (FormField ff : fields) {
            if ("".equals(fds)) {
                fds = ff.getName();
            } else {
                fds += "," + ff.getName();
            }
        }
        Conn conn = new Conn(Global.getDefaultDB());
        String sql = "select " + fds + ",cws_creator,cws_id,cws_order,unit_code,flowId,cws_log_user,cws_log_type,cws_log_date,cws_log_id,flowTypeCode from " + FormDb.getTableNameForLog(fd.getCode()) + " where id=?";
        ResultSet rs;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    int k = 1;
                    for (FormField ff : fields) {
                        try {
                            if (ff.getType().equals(FormField.TYPE_DATE)) {
                                java.sql.Date dt = rs.getDate(k);
                                ff.setValue(DateUtil.format(dt,
                                        "yyyy-MM-dd"));
                            } else if (ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                                Timestamp ts = rs.getTimestamp(k);
                                String d = "";
                                if (ts != null) {
                                    d = DateUtil.format(new java.util.Date(ts.
                                            getTime()), "yyyy-MM-dd HH:mm:ss");
                                }
                                ff.setValue(d);
                                // ff.setValue(rs.getString(k).substring(0, 19));
                            } else {
                                // logger.info("load: id=" + id + " rs.getString(" + k + ")=" + rs.getString(k));
                                ff.setValue(rs.getString(k));
                            }
                        } catch (SQLException e) {
                            // 以免出现如下问题：load:Value '0000-00-00' can not be represented as java.sql.Timestamp
                            LogUtil.getLog(getClass()).error("load1:" + e.getMessage());
                            LogUtil.getLog(getClass()).error(e);
                        }
                        k++;
                    }

                    creator = StrUtil.getNullStr(rs.getString(k));
                    cwsId = rs.getString(k+1);
                    cwsOrder = rs.getInt(k+2);
                    unitCode = rs.getString(k+3);
                    flowId = rs.getInt(k+4);
                    
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
            LogUtil.getLog(getClass()).error(e);
        }
        finally {
            conn.close();
        }
    }    
    
    @Override
    public String getFieldValue(String fieldName) {
        for (FormField ff : fields) {
            if (ff.getName().equals(fieldName)) {
                return StrUtil.getNullStr(ff.getValue());
            }
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
            re = conn.executePreUpdate() == 1;
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del:" + e.getMessage());
        }
        finally {
            conn.close();
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
	
	@Override
    public boolean isLoaded() {
		return loaded;
	}
	
	@Override
    public String getFlowTypeCode() {
		return flowTypeCode;
	}

	@Override
    public void setFieldValue(String fieldName, String value) {
        for (FormField ff : fields) {
            if (ff.getName().equalsIgnoreCase(fieldName)) {
                ff.setValue(value);
                break;
            }
        }
    }

    @Override
    public boolean save() {
        return true;
    }

    @Override
    public void setFlowTypeCode(String flowTypeCode) {
        this.flowTypeCode = flowTypeCode;
    }

    @Override
    public long getCwsQuoteId() {
        return -1;
    }

    @Override
    public String getCwsQuoteForm() {
        return "";
    }

    @Override
    public String getFormCode() {
        if (fd != null) {
            return fd.getCode();
        }
        else {
            return null;
        }
    }

    @Override
    public Vector<IAttachment> getAttachments() {
        return null;
    }
}
