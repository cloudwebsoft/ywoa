package com.redmoon.oa.notice;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.person.UserDb;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.Conn;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

public class NoticeReplyDb extends ObjectDb{

	private long id;
	private long noticeid;
	private String username;
	private String content;
	private Date replyTime;
	private String isReaded;
	private Date readTime;
	
	public void initDB() {
		tableName = "oa_notice_reply";
		primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_LONG);
		objectCache = new NoticeReplyCache(this);
		isInitFromConfigDB = false;
		QUERY_CREATE = "insert into " + tableName + " (id,notice_id,user_name) values(?,?,?)";

		QUERY_SAVE = "update " + tableName + " set content=?,reply_time=? where notice_id=? and user_name=?";
		QUERY_LIST = "select id from " + tableName + " order by create_date desc";
		QUERY_DEL = "delete from " + tableName + " where id=?";
		QUERY_LOAD = "select id,notice_id,user_name,content,reply_time,is_readed,read_time from " + tableName + " where id=?";
	}
	
	/**
	 * 批量生成回复
	 * @Description: 
	 * @param noticeId
	 * @param users
	 * @return
	 */
	public int createBatch(long noticeId, String[] users) {
		if (users==null)
			return 0;
		
		int len = users.length;
        JdbcTemplate jt = new JdbcTemplate(new com.cloudwebsoft.framework.db.
                Connection(cn.js.fan.web.Global.getDefaultDB()));
		try {
			for (int i=0; i<len; i++) {
				String userName = users[i];
				id = (long) SequenceManager.nextID(SequenceManager.OA_NOTICE);
				String sql = "insert into " + tableName + " (id,notice_id,user_name) values(" + id + "," + noticeId + "," + StrUtil.sqlstr(userName) + ")";
				jt.addBatch(sql);				
			}
			jt.executeBatch();
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
			return 0;
		}
		return len;
	}

	public boolean create() {
		Conn conn = new Conn(connname);
		boolean re = false;
		try {
			id = (long) SequenceManager.nextID(SequenceManager.OA_NOTICE);
			PreparedStatement pstmt = conn.prepareStatement(QUERY_CREATE);
			pstmt.setLong(1, id);
			pstmt.setLong(2, noticeid);
			pstmt.setString(3, username);
			//pstmt.setString(4, content);
			//pstmt.setTimestamp(5, new Timestamp(replyTime.getTime()));
			//pstmt.setString(6, isReaded);
			//pstmt.setTimestamp(7, new Timestamp(readTime.getTime()));
			
			re = conn.executePreUpdate() == 1 ? true : false;
			if (re) {
				NoticeReplyCache rc = new NoticeReplyCache(this);
				rc.refreshCreate();
			}
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
	
	
	
	@Override
	public boolean del() throws ErrMsgException, ResKeyException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ObjectDb getObjectRaw(PrimaryKey arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void load() throws ErrMsgException, ResKeyException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean save() throws ErrMsgException, ResKeyException {
		Conn conn = new Conn(connname);
		boolean re = false;
		try {
			PreparedStatement pstmt = conn.prepareStatement(QUERY_SAVE);
			pstmt.setString(1, content);
			pstmt.setTimestamp(2, new Timestamp(new Date().getTime()));
			pstmt.setLong(3, noticeid);
			pstmt.setString(4,username);
			
			re = conn.executePreUpdate() == 1 ? true : false;
			if (re) {
				NoticeReplyCache rc = new NoticeReplyCache(this);
				rc.refreshCreate();
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error("updateContent:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return re;
	}
	
	public NoticeReplyDb getReply(){
		String sql = "select user_name,content,reply_time from oa_notice_reply where notice_id=? and user_name=?";
		ResultSet rs = null;
		Vector<NoticeReplyDb> v = new Vector<NoticeReplyDb>();
		Conn conn = new Conn(connname);
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setLong(1, noticeid);
			ps.setString(2,username);
			rs = conn.executePreQuery();
			while (rs.next()) {
				NoticeReplyDb nrd = new NoticeReplyDb();
				nrd.setUsername(rs.getString(1));
				nrd.setContent(rs.getString(2));
				nrd.setReplyTime(rs.getTimestamp(3));
				return nrd;
			}
		
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error("getNoticeReply: " + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return null;
		
		
	}
	public Vector<NoticeReplyDb> getNoticeReply(long noticeid)
	{
		String sql = "select user_name,content,reply_time from oa_notice_reply where notice_id=? and content is not null order by reply_time desc";
		ResultSet rs = null;
		Vector<NoticeReplyDb> v = new Vector<NoticeReplyDb>();
		Conn conn = new Conn(connname);
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setLong(1, noticeid);
			rs = conn.executePreQuery();
			while (rs.next()) {
				NoticeReplyDb nrd = new NoticeReplyDb();
				nrd.setUsername(rs.getString(1));
				nrd.setContent(rs.getString(2));
				nrd.setReplyTime(rs.getTimestamp(3));
				
				v.addElement(nrd);
			}
			return v;
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error("getNoticeReply: " + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return v;
	}
	
	public void saveStatus(){
		Conn conn = new Conn(connname);
		boolean re = false;
		String sql = "update oa_notice_reply set is_readed=?,read_time=? where notice_id=? and user_name=?";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "1");
			pstmt.setTimestamp(2, new Timestamp(new Date().getTime()));
			pstmt.setLong(3, noticeid);
			pstmt.setString(4,username);
			
			re = conn.executePreUpdate() == 1;
			if (re) {
				NoticeReplyCache rc = new NoticeReplyCache(this);
				rc.refreshCreate();
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error("updateContent:" + e.getMessage());
		} finally {
			conn.close();
		}

	}
	
	public Vector getNoticeReadOrNot(long noticeId,int isReaded) {
		String sql = "select user_name from oa_notice_reply where notice_id=? and is_readed="+isReaded;
		ResultSet rs = null;
		Vector v = new Vector();
		Conn conn = new Conn(connname);
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setLong(1, noticeId);
			rs = conn.executePreQuery();
			while (rs.next()) {
				String uName = rs.getString(1);
				if (uName.equals("system")) {
					continue;
				}
				UserDb ud = new UserDb(uName);
				if (ud == null || !ud.isLoaded()) {
					continue;
				}
				v.addElement(uName);
			}
			return v;
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error("getDeptOfNotice: " + e.getMessage());
		} finally {
			conn.close();
		}
		return v;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getNoticeid() {
		return noticeid;
	}

	public void setNoticeid(long noticeid) {
		this.noticeid = noticeid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getReplyTime() {
		return replyTime;
	}

	public void setReplyTime(Date replyTime) {
		this.replyTime = replyTime;
	}

	public String getIsReaded() {
		return isReaded;
	}

	public void setIsReaded(String isReaded) {
		this.isReaded = isReaded;
	}

	public Date getReadTime() {
		return readTime;
	}

	public void setReadTime(Date readTime) {
		this.readTime = readTime;
	}

}
