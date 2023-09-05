package com.redmoon.oa;

import java.sql.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;

import com.cloudweb.oa.utils.ConstUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;

public class LogDb extends ObjectDb {
    public static final int TYPE_LOGIN = 0;
    public static final int TYPE_LOGOUT = 1;
    public static final int TYPE_ACTION = 2;
    public static final int TYPE_WARN = 3;
    public static final int TYPE_ERROR = 4;

    /**
     * 权限
     */
    public static final int TYPE_PRIVILEGE = 5;
    /**
     * 攻击
     */
    public static final int TYPE_HACK = 100;

    public static final int DEVICE_PC = 0;//PC 端登录 默认
    public static final int DEVICE_MOBILE = 100;//移动 端登录    

    public LogDb() {
        init();
    }

    public LogDb(int id) {
        init();
        this.id = id;
        load();
    }

    @Override
    public void initDB() {
        tableName = "log";
        primaryKey = new PrimaryKey("ID", PrimaryKey.TYPE_INT);
        objectCache = new LogCache(this);
        isInitFromConfigDB = false;
        QUERY_CREATE = "insert into " + tableName + " (USER_NAME, IP, LOG_TYPE, ACTION, LOG_DATE, unit_code, remark,device) values (?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set USER_NAME=?, IP=?, LOG_TYPE=?, ACTION=?, LOG_DATE=? where ID=?";
        QUERY_LOAD =
                "select USER_NAME, IP, LOG_TYPE, ACTION, LOG_DATE, unit_code, remark ,device from " + tableName + " where ID=?";
        QUERY_DEL = "delete from " + tableName + " where ID=?";
        QUERY_LIST = "select ID from " + tableName + " order by LOG_DATE desc";
    }

    @Override
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new LogDb(pk.getIntValue());
    }

    public LogDb getLogDb(int id) {
        LogDb log = (LogDb)getObjectDb(new Integer(id));
        return log;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUserName() {
        return userName;
    }

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public String getAction() {
        return action;
    }

    public String getIp() {
        return ip;
    }

    public java.util.Date getDate() {
        return date;
    }

    @Override
    public boolean create() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_CREATE);
            pstmt.setString(1, userName);
            pstmt.setString(2, ip);
            pstmt.setInt(3, type);
            pstmt.setString(4, action);
            pstmt.setTimestamp(5, new java.sql.Timestamp(new java.util.Date().getTime()));
            pstmt.setString(6, unitCode);
            pstmt.setString(7, remark);
            pstmt.setInt(8, device);
            re = conn.executePreUpdate() == 1;
            if (re) {
                LogCache rc = new LogCache(this);
                rc.refreshCreate();
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return re;
    }

    @Override
    public boolean save() {
        Conn conn = new Conn(connname);
        boolean re = false;
        // QUERY_SAVE = "update " + tableName + " set USER_NAME=?, IP=?, TYPE=?, ACTION=?, LOG_DATE=? where ID=?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_SAVE);
            pstmt.setString(1, userName);
            pstmt.setString(2, ip);
            pstmt.setInt(3, type);
            pstmt.setString(4, action);
            pstmt.setTimestamp(5, new Timestamp(date.getTime()));
            pstmt.setInt(6, id);
            re = conn.executePreUpdate() == 1;
            if (re) {
                LogCache rc = new LogCache(this);
                primaryKey.setValue(id);
                rc.refreshSave(primaryKey);
                return true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return re;
    }

    @Override
    public void load() {
        Conn conn = new Conn(connname);
        ResultSet rs;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    userName = StrUtil.getNullStr(rs.getString(1));
                    ip = StrUtil.getNullStr(rs.getString(2));
                    type = rs.getInt(3);
                    action = rs.getString(4);
                    date = rs.getTimestamp(5);
                    unitCode = StrUtil.getNullStr(rs.getString(6));
                    remark = StrUtil.getNullStr(rs.getString(7));
                    device = rs.getInt(8);
                    loaded = true;
                    primaryKey.setValue(id);
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("load:" + e.getMessage());
        } finally {
            conn.close();
        }
    }

    @Override
    public boolean del() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_DEL);
            pstmt.setInt(1, id);
            re = conn.executePreUpdate() == 1;
            if (re) {
                re = conn.executePreUpdate() >= 0;
                LogCache rc = new LogCache(this);
                rc.refreshDel(primaryKey);
                return true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return re;
    }
    
    /**
     * 取得某单位在某个时间段内的登录次数
     * @param unitCode
     * @param beginDate
     * @param endDate
     * @return
     */
    public int getLoginCount(String unitCode, java.util.Date beginDate, java.util.Date endDate) {
        String sql = "select count(*) from " + tableName + " where unit_code=? and log_type=" + TYPE_LOGIN + " and log_date>=? and log_date<?";
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri;
		try {
			ri = jt.executeQuery(sql, new Object[]{unitCode, beginDate, endDate});
	        if (ri.hasNext()) {
	        	ResultRecord rr = (ResultRecord)ri.next();
	        	return rr.getInt(1);
	        }
		} catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
		}

        return 0;
    }
    
    /**
     * 取得用户的登录次数，用于积分
     * @param userName
     * @param year
     * @param month 从0开始
     * @return
     */
    public int getLoginCountOfUser(String userName, int year, int month) {
    	java.util.Date d = DateUtil.getDate(year, month, 1);
    	java.util.Date d2 = DateUtil.addMonthDate(d, 1);

		String sql = "select count(*) from " + tableName + " where user_name=? and log_type=? and log_date>=? and log_date<?";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri;
		try {
			ri = jt.executeQuery(sql, new Object[]{userName, TYPE_LOGIN, d, d2});
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				return rr.getInt(1);
			}			
		} catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
		}
		return 0;    	
    }    

    private String userName;
    private int id;
    private int type;
    private String action;
    private String ip;
    private java.util.Date date;
    
    private String unitCode = ConstUtil.DEPT_ROOT;
    
    private String remark;
    
    private int device = LogDb.DEVICE_PC;

	public int getDevice() {
		return device;
	}

	public void setDevice(int device) {
		this.device = device;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getUnitCode() {
		return unitCode;
	}

	public void setUnitCode(String unitCode) {
		this.unitCode = unitCode;
	}
}
