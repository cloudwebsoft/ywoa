package com.redmoon.oa.emailpop3;

import java.sql.*;
import cn.js.fan.util.*;
import cn.js.fan.db.Conn;
import org.apache.log4j.Logger;
import cn.js.fan.web.Global;

public class UserPop3Setup {
	String mailserver, email_user, email_pwd;
	int port;
	Logger logger = Logger.getLogger(UserPop3Setup.class.getName());

	public UserPop3Setup() {
	}

	public String getMailServer() {
		return mailserver;
	}

	public int getPort() {
		return port;
	}

	public int getSmtpPort() {
		return smtpPort;
	}

	public String getUser() {
		return email_user;
	}

	public String getPwd() {
		return email_pwd;
	}

	public String[] getUserEmails(String user) {
		boolean re = false;
		String sql = "select email from email_pop3 where userName="
				+ StrUtil.sqlstr(user);
		ResultSet rs = null;
		String[] list = null;
		Conn conn = new Conn(Global.getDefaultDB());
		try {
			rs = conn.executeQuery(sql);
			int count = conn.getRows();

			if (count > 0)
				list = new String[count];
			int i = 0;
			while (rs.next()) {
				list[i] = rs.getString(1);
				i++;
			}
		} catch (SQLException e) {
			logger.error("getUserPop3Setup error: " + e.getMessage());
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
			}
		}
		return list;
	}

	public boolean getUserPop3Setup(String user, String email) {
		boolean re = false;
		String sql = "select server,port,emailUser,emailPwd,smtpPort,is_ssl from email_pop3 where userName="
				+ StrUtil.sqlstr(user) + " and email=" + StrUtil.sqlstr(email);
		ResultSet rs = null;
		Conn conn = new Conn(Global.getDefaultDB());
		try {
			rs = conn.executeQuery(sql);
			if (rs != null && rs.next()) {
				re = true;
				mailserver = rs.getString(1);
				port = rs.getInt(2);
				email_user = StrUtil.getNullString(rs.getString(3));
				email_pwd = StrUtil.getNullString(rs.getString(4));
				smtpPort = rs.getInt(5);
				setSsl(rs.getInt(6) == 1);
			}
		} catch (SQLException e) {
			logger.error("getUserPop3Setup error: " + e.getMessage());
		} finally {
			try {
				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
			}
		}
		return re;
	}
	
	public boolean getUserPop3Setup(String email) {
		boolean re = false;
		String sql = "select server,port,emailUser,emailPwd,smtpPort,is_ssl from email_pop3 where  email=" + StrUtil.sqlstr(email);
		ResultSet rs = null;
		Conn conn = new Conn(Global.getDefaultDB());
		try {
			rs = conn.executeQuery(sql);
			if (rs != null && rs.next()) {
				re = true;
				mailserver = rs.getString(1);
				port = rs.getInt(2);
				email_user = StrUtil.getNullString(rs.getString(3));
				email_pwd = StrUtil.getNullString(rs.getString(4));
				smtpPort = rs.getInt(5);
				setSsl(rs.getInt(6) == 1);
			}
		} catch (SQLException e) {
			logger.error("getUserPop3Setup error: " + e.getMessage());
		} finally {
			try {
				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
			}
		}
		return re;
	}

	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public boolean isSsl() {
		return ssl;
	}

	private int smtpPort;

	private boolean ssl = false;

}
