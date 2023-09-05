package com.redmoon.oa.emailpop3;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.Conn;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import com.cloudwebsoft.framework.util.LogUtil;

public class EmailAddrDb extends ObjectDb {

	public int id;
	public String emailAddr;
	public String userName;
	public Date addDate;
	public boolean isDelete = false;

	public EmailAddrDb() {
		init();
	}

	public EmailAddrDb(int id) {
		this.id = id;
		init();
		load();
	}

	public void initDB() {
		tableName = "email_recently_addr";
		primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
		objectCache = new EmailAddrCache(this);
        isInitFromConfigDB = false;
        
		QUERY_CREATE = "insert into email_recently_addr (email_addr,user_name,send_date,is_delete) values (?,?,?,?)";
		QUERY_SAVE = "update email_recently_addr set email_addr=?,user_name=?,send_date=?,is_delete=? where id=?";
		QUERY_LIST = "select id from email_recently_addr order by send_date desc";

		QUERY_DEL = "delete from email_recently_addr where id=?";
		QUERY_LOAD = "select email_addr,user_name,send_date,is_delete from email_recently_addr where id=?";
	}

	public synchronized void load() {
		Conn conn = new Conn(connname);
		ResultSet rs = null;
		try {
			PreparedStatement pstmt = conn.prepareStatement(QUERY_LOAD);
			pstmt.setInt(1, id);
			rs = conn.executePreQuery();
			if (rs != null && rs.next()) {
				emailAddr = rs.getString(1);
				userName = rs.getString(2);
				addDate = rs.getTimestamp(3);
				isDelete = rs.getInt(4) == 1;
				loaded = true;
			}
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error("load: " + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
	}

	public EmailAddrDb getEmailAddrDb(int id) {
		return (EmailAddrDb) getObjectDb(new Integer(id));
	}

	public boolean create() throws ErrMsgException {
		Conn conn = new Conn(connname);
		boolean re = false;
		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
			ps.setString(1, emailAddr);
			ps.setString(2, userName);
			ps.setTimestamp(3, new Timestamp(new java.util.Date().getTime()));
			ps.setInt(4, isDelete?1:0);

			re = conn.executePreUpdate() == 1 ? true : false;
			if (re) {
				EmailAddrCache rc = new EmailAddrCache(this);
				rc.refreshCreate();
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error("create:" + e.getMessage());
			throw new ErrMsgException("数据库操作失败！");
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return re;
	}
	
	
	
	public boolean del() throws ErrMsgException, ResKeyException {
		Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
            	EmailAddrCache rc = new EmailAddrCache(this);
                primaryKey.setValue(new Integer(id));
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

	
	public ObjectDb getObjectRaw(PrimaryKey pk) {
		 return new EmailAddrDb(pk.getIntValue());
	}

	
	public boolean save() throws ErrMsgException, ResKeyException {
		Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1, emailAddr);
            ps.setString(2, userName);
            if(addDate == null){
            	ps.setTimestamp(3, null);
            }
            else {
            	ps.setTimestamp(3, new Timestamp(addDate.getTime()));
			}
            ps.setInt(4, isDelete?1:0);
            ps.setInt(5, id);
            re = conn.executePreUpdate()==1?true:false;

            if (re) {
            	EmailAddrCache rc = new EmailAddrCache(this);
                primaryKey.setValue(new Integer(id));
                rc.refreshSave(primaryKey);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("save: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
       return re;
	}
	
	
	public EmailAddrDb getEmailAddrDb(String email,String userName){
		String sql = "select id from email_recently_addr where  (LOCATE(email_addr,'"+email+"') or LOCATE('"+email+"',email_addr)) and user_name = '"+userName+"'";
		Conn conn = null;
		EmailAddrDb emailAddrDb = null;
        try {
            conn = new Conn(connname);
            ResultSet rs = conn.executeQuery(sql);
            if (rs!=null) {
                while (rs.next()) {
                	emailAddrDb = getEmailAddrDb(rs.getInt(1));
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("delMsg:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        
        return emailAddrDb;
	}
	

	public String getEmailAddr() {
		return emailAddr;
	}

	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
	}

	public Date getAddDate() {
		return addDate;
	}

	public void setAddDate(Date addDate) {
		this.addDate = addDate;
	}

	public int getId() {
		return id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean isDelete() {
		return isDelete;
	}

	public void setDelete(boolean isDelete) {
		this.isDelete = isDelete;
	}

}
