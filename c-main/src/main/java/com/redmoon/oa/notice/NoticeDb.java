package com.redmoon.oa.notice;

import java.sql.*;
import java.util.*;
import java.util.Date;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.*;
import com.redmoon.oa.db.*;
import com.redmoon.oa.dept.*;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.UserGroupDb;

import javax.servlet.http.HttpServletRequest;

public class NoticeDb extends ObjectDb {
	public Vector attachs = new Vector();

	private long id;

	private String title;

	private String content;

	private String userName;

	private java.util.Date createDate;

	String connname = Global.getDefaultDB();

	private int isDeptNotice;

	private int isShow;
	
	private java.util.Date beginDate;
	
	private String unitCode;
	
	private int isall;
	
	private String userList;
	
	private int flowId;
	
	private int is_reply;
	
	private int is_forced_response;
	
	/**
	 * 鎵�鏈夌敤鎴�
	 */
	public static final int IS_ALL_WHOLE = 2;
	/**
	 * 閮ㄩ棬涓殑鐢ㄦ埛
	 */
	public static final int IS_ALL_DEPT = 1;
	/**
	 * 鎵�閫夌殑鐢ㄦ埛
	 */
	public static final int IS_ALL_SEL_USER = 0;

	/**
	 * @return the is_reply
	 */
	public int getIs_reply() {
		return is_reply;
	}

	/**
	 * @param isReply the is_reply to set
	 */
	public void setIs_reply(int isReply) {
		is_reply = isReply;
	}

	/**
	 * @return the is_forced_response
	 */
	public int getIs_forced_response() {
		return is_forced_response;
	}

	/**
	 * @param isForcedResponse the is_forced_response to set
	 */
	public void setIs_forced_response(int isForcedResponse) {
		is_forced_response = isForcedResponse;
	}

	public int getFlowId() {
		return flowId;
	}

	public void setFlowId(int flowId) {
		this.flowId = flowId;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public boolean isBold() {
		return bold;
	}

	public void setBold(boolean bold) {
		this.bold = bold;
	}

	private java.util.Date endDate;

	private String color = "";
	private boolean bold = false;

	public java.util.Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(java.util.Date beginDate) {
		this.beginDate = beginDate;
	}

	public java.util.Date getEndDate() {
		return endDate;
	}

	public void setEndDate(java.util.Date endDate) {
		this.endDate = endDate;
	}

	public int getIsShow() {
		return isShow;
	}

	public void setIsShow(int isShow) {
		this.isShow = isShow;
	}

	public NoticeDb() {
		super();
	}

	public NoticeDb(long id) {
		this.id = id;
		init();
		load();
	}

	public void initDB() {

		tableName = "oa_notice";
		primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_LONG);
		objectCache = new NoticeCache(this);
		isInitFromConfigDB = false;
		QUERY_CREATE = "insert into " + tableName + " (id,title,content,user_name,create_date,is_dept_notice,is_show,begin_date,end_date,color,is_bold,unit_code,notice_level,is_all,flowId,is_reply,is_forced_response) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		QUERY_SAVE = "update " + tableName + " set title=?,is_dept_notice=?,users_know=?,is_show=?,begin_date=?,end_date=?,color=?,is_bold=?,content=?,unit_code=?,notice_level=?,is_reply=?,is_forced_response=? where id=?";
		QUERY_LIST = "select id from " + tableName + " order by create_date desc";
		QUERY_DEL = "delete from " + tableName + " where id=?";
		QUERY_LOAD = "select title,content,user_name,create_date,is_dept_notice,users_know,is_show,begin_date,end_date,color,is_bold,unit_code,notice_level,is_all,flowId,is_reply,is_forced_response  from " + tableName + " where id=?";
	}

	public boolean isDeptNotice() {
		String sql = "select is_dept_notice from " + tableName + " where id=?";
		boolean re = false;
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setLong(1, id);
			rs = conn.executePreQuery();
			while (rs.next()) {
				int isDeptNoticeInt = rs.getInt(1);
				if (isDeptNoticeInt == 1)
					re = true;
			}
			return re;
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error("isDeptNotice: " + StrUtil.trace(e));
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return re;
	}

	public ObjectDb getObjectRaw(PrimaryKey pk) {
		return new NoticeDb(pk.getLongValue());
	}

	public NoticeDb getNoticeDb(long id) {
		return (NoticeDb) getObjectDb(new Long(id));
	}

	/**
	 * 鐢ㄤ簬鎵嬫満绔垱寤鸿褰�
	 */
	public boolean create() {
		Conn conn = new Conn(connname);
		boolean re = false;
		try {
			id = (long) SequenceManager.nextID(SequenceManager.OA_NOTICE);
			PreparedStatement pstmt = conn.prepareStatement(QUERY_CREATE);
			pstmt.setLong(1, id);
			pstmt.setString(2, title);
			pstmt.setString(3, content);
			pstmt.setString(4, userName);
			pstmt.setTimestamp(5, new Timestamp(new java.util.Date().getTime()));
			pstmt.setInt(6, isDeptNotice);
			pstmt.setInt(7, isShow);
			pstmt.setTimestamp(8, new java.sql.Timestamp(beginDate.getTime()));
			if (endDate==null){
				pstmt.setTimestamp(9, null);
			}
			else{
				pstmt.setTimestamp(9, new java.sql.Timestamp(endDate.getTime()));
			}
			pstmt.setString(10, color);
			pstmt.setInt(11, bold?1:0);
			pstmt.setString(12, unitCode);
			pstmt.setInt(13, level);
			pstmt.setInt(14, isall);
			pstmt.setInt(15, flowId);
			pstmt.setInt(16, is_reply);
			pstmt.setInt(17,is_forced_response);
			re = conn.executePreUpdate() == 1 ? true : false;
			if (re) {
				NoticeCache rc = new NoticeCache(this);
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

	public void load() {
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
			ps.setLong(1, id);
			rs = conn.executePreQuery();
			if (rs != null && rs.next()) {
				title = rs.getString(1);
				content = rs.getString(2);
				userName = rs.getString(3);
				createDate = rs.getTimestamp(4);
				isDeptNotice = rs.getInt(5);
				usersKnow = StrUtil.getNullStr(rs.getString(6));
				isShow = rs.getInt(7);
				beginDate = rs.getTimestamp(8);
				endDate = rs.getTimestamp(9);
				color = StrUtil.getNullStr(rs.getString(10));
				bold = rs.getInt(11)==1;
				unitCode = rs.getString(12);
				level = rs.getInt(13);
				isall = rs.getInt(14);
				flowId = rs.getInt(15);
				is_reply = rs.getInt(16);
				is_forced_response = rs.getInt(17);
				loaded = true;
				primaryKey.setValue(new Long(id));
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error("load: " + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		NoticeAttachmentDb nad = new NoticeAttachmentDb();
		attachs = nad.getAttachsOfNotice(id);
	}

	public Vector getAttachs() {
		return attachs;
	}

	public boolean del() {
		boolean re = false;
		Conn conn = new Conn(connname);
		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
			ps.setLong(1, id);
			re = conn.executePreUpdate() == 1 ? true : false;
			if (re) {
				NoticeDeptDb ndd = new NoticeDeptDb();
				re = ndd.delOfNotice(id);
				Iterator ir = attachs.iterator();
				while (ir.hasNext()) {
					NoticeAttachmentDb nad = (NoticeAttachmentDb) ir.next();
					re = nad.del();
				}
				if (re) {
					NoticeCache rc = new NoticeCache(this);
					primaryKey.setValue(new Long(id));
					rc.refreshDel(primaryKey);
				}
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

	public boolean delAttach(long attachId) {
		NoticeAttachmentDb nad = new NoticeAttachmentDb(attachId);
		boolean re = nad.del();
		if (re) {
			NoticeCache rc = new NoticeCache(this);
			rc.refreshSave(primaryKey);
		}
		return re;
	}

	public boolean save() {
		Conn conn = new Conn(connname);
		boolean re = false;
		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
			ps.setString(1, title);
			ps.setInt(2, isDeptNotice);
			ps.setString(3, usersKnow);
			ps.setInt(4, isShow);
			if (beginDate==null)
				ps.setTimestamp(5, null);
			else
				ps.setTimestamp(5, new java.sql.Timestamp(beginDate.getTime()));
			if (endDate==null)
				ps.setTimestamp(6, null);
			else
				ps.setTimestamp(6, new java.sql.Timestamp(endDate.getTime()));

			ps.setString(7, color);
			ps.setInt(8, bold?1:0);

			ps.setString(9, content);
			ps.setString(10, unitCode);

			ps.setInt(11, level);
			ps.setInt(12,is_reply);
			ps.setInt(13,is_forced_response);


			ps.setLong(14, id);
			re = conn.executePreUpdate() == 1 ? true : false;
			if (re) {
				NoticeCache rc = new NoticeCache(this);
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

	public Vector getDeptOfNotice(long noticeId) {
		String sql = "select dept_code from oa_notice_dept where notice_id=? order by notice_id";
		ResultSet rs = null;
		Vector v = new Vector();
		Conn conn = new Conn(connname);
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setLong(1, noticeId);
			rs = conn.executePreQuery();
			while (rs.next()) {
				String dCode = rs.getString(1);
				v.addElement(new DeptDb(dCode));
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

	public void setId(long id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public void setIsDeptNotice(int isDeptNotice) {
		this.isDeptNotice = isDeptNotice;
	}

	public void setUsersKnow(String usersKnow) {
		this.usersKnow = usersKnow;
	}

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public String getUserName() {
		return userName;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public int getIsDeptNotice() {
		return isDeptNotice;
	}

	public String getUsersKnow() {
		return usersKnow;
	}

	/**
	 * 閸欐牕绶辨稉搴㈡拱闁氨鐓￠惄绋垮彠閻ㄥ嫮鏁ら幋锟�
	 * @return Vector
	 */
	public Vector getUsersRelated() {
		Vector vret = new Vector();
		String[] usernames = null;
		if (0 == isall) {// 闁瀚ㄩ悧鐟扮暰閻€劍鍩涢惃鍕剰閸愶拷閸欐牕鍤璾serlist娑擃厾娈戦悽銊﹀煕
			usernames = StrUtil.split(getUserList(), ",");
		}
		if (1 == isall) {// 閸楁洑缍呯粻锛勬倞閸涙﹢锟介幏鈺佸弿闁劋姹夐崨锟介崣鏍у毉閸楁洑缍呮稉瀣弿闁劎鏁ら幋锟�
			String userName = getUserName();
			DeptUserDb dud = new DeptUserDb();
			Vector v1 = dud.getDeptsOfUser(userName); // 閸欐牕绶遍悽銊﹀煕閹碉拷婀柈銊╂，閻ㄥ垼eptcode
			// ArrayList<String> v2 = new ArrayList<String>();
			// v2.add(userName);
			if (v1.size() > 0) {
				for (int j = 0; j < v1.size(); j++) {

					DeptDb deptDb = (DeptDb) v1.get(j);
					Vector va = new Vector();
					try {
						va = deptDb.getAllChild(va, deptDb);
					} catch (ErrMsgException e) {
						LogUtil.getLog(getClass()).error(e);
					}
					Vector va2 = new Vector();
					va2.add(deptDb);
					va2.addAll(va);
					Iterator it = va2.iterator();
					String sql = "";
					while (it.hasNext()) {
						DeptDb dept = (DeptDb) it.next();
						String deptCode = dept.getCode();
						sql += (sql.equals("") ? "" : ",")
								+ StrUtil.sqlstr(deptCode);
						/*
						 * Vector v = dud.list(deptCode);// 閺嶈宓乨eptcode閸欐牕绶遍柈銊╂，娑撳鏁ら幋锟絠f
						 * (v.size() > 0) { //usernames = new String[v.size()];
						 * for (int i=0;i<v.size();i++) { DeptUserDb ud =
						 * (DeptUserDb)v.get(i); v2.add(ud.getUserName()); } }
						 */
					}
					if (!sql.equals("")) {
						sql = "select distinct(u.name) from dept_user du, users u where du.user_name=u.name and u.isValid=1 and du.DEPT_CODE in ("
								+ sql
								+ ") order by u.orders desc";
						JdbcTemplate jt = new JdbcTemplate();
						int i = 0;
						try {
							ResultIterator ri = jt.executeQuery(sql);
							usernames = new String[ri.getRows()];
							while (ri.hasNext()) {
								ResultRecord rr = (ResultRecord) ri.next();
								// v2.add(rr.getString(1));
								usernames[i++] = rr.getString(1);
							}
						} catch (SQLException e) {
							LogUtil.getLog(getClass()).error(
									"createNoticeReply:" + StrUtil.trace(e));
							LogUtil.getLog(getClass()).error(e);
						} finally {
							jt.close();
						}
					}
				}
			}
			// removeDuplicateWithOrder(v2);
			// usernames = v2.toArray(new String[]{});

		} else if (2 == isall) // 缁崵绮虹粻锛勬倞閸涙﹢锟介幏鈺佸弿闁劋姹夐崨锟介崣鏍у毉瑜版挸澧犻崡鏇氱秴閸欏﹤鍙剧�涙劕宕熸担宥囨畱閸忋劑鍎撮悽銊﹀煕
		{
			/*
			 * UserGroupDb ugd = new UserGroupDb(); ugd =
			 * ugd.getUserGroupDb(UserGroupDb.EVERYONE); Vector v =
			 * ugd.getAllUserOfGroup(); if (v.size() > 0) { usernames = new
			 * String[v.size()]; for (int i=0;i<v.size();i++) { UserDb ud =
			 * (UserDb)v.get(i); usernames[i] = ud.getName(); } }
			 */
			DeptDb dd = new DeptDb(getUnitCode());
			Vector v = new Vector();
			try {
				v = dd.getAllChild(v, dd);
			} catch (ErrMsgException e) {
				LogUtil.getLog(getClass()).error(e);
			}
			Vector va2 = new Vector();
			va2.add(dd);
			va2.addAll(v);
			Iterator it = va2.iterator();
			// Vector uva = new Vector();
			String sql = "";
			while (it.hasNext()) {
				DeptDb dept = (DeptDb) it.next();
				if (dept.getType() == DeptDb.TYPE_UNIT) {
					String deptCode = dept.getCode();
					sql += (sql.equals("") ? "" : ",")
							+ StrUtil.sqlstr(deptCode);
				}
				// UserDb ud = new UserDb();
				// Vector uv = ud.listUserOfUnit(dept.getCode());
				// uva.addAll(uv);
			}
			if (!sql.equals("")) {
				sql = "select distinct(name) from users where isValid=1 and unit_code in ("
						+ sql + ") order by orders desc";
				JdbcTemplate jt = new JdbcTemplate();
				int i = 0;
				try {
					ResultIterator ri = jt.executeQuery(sql);
					usernames = new String[ri.getRows()];
					while (ri.hasNext()) {
						ResultRecord rr = (ResultRecord) ri.next();
						// v2.add(rr.getString(1));
						usernames[i++] = rr.getString(1);
					}
				} catch (SQLException e) {
					LogUtil.getLog(getClass()).error(e);
				} finally {
					jt.close();
				}
			}
			/*
			 * if (uva.size() > 0) { usernames = new String[uva.size()];
			 * Iterator uit = uva.iterator(); int i = 0; while (uit.hasNext()) {
			 * UserDb ud = (UserDb) uit.next(); usernames[i++] = ud.getName(); }
			 * }
			 */
		} else {
			return vret;
		}

		if (usernames == null || usernames.length == 0) {
			return vret;
		}
		for (String userName : usernames) {
			vret.addElement(new UserDb(userName));
		}
		return vret;
	}

	public Vector getUsersKnown() {
		Vector v = new Vector();
		UserMgr um = new UserMgr();
		String[] ary = StrUtil.split(usersKnow, ",");
		if (ary != null) {
			int len = ary.length;
			for (int i = 0; i < len; i++) {
				v.addElement(um.getUserDb(ary[i]));
			}
		}
		return v;
	}

	public Vector getUsersUnknown() {
		Vector v = getUsersRelated();
		Vector v2 = getUsersKnown();
		int c2 = v2.size();
		for (int i = 0; i < c2; i++) {
			UserDb ud2 = (UserDb) v2.elementAt(i);
			int c = v.size();
			for (int j = 0; j < c; j++) {
				UserDb ud = (UserDb) v.elementAt(j);
				if (ud.getName().equals(ud2.getName())) {
					v.remove(j);
					break;
				}
			}
		}
		return v;
	}

	public boolean isUserReaded(String userName) {
		String sql = "select id from oa_notice_reply where notice_id=? and user_name=? and is_readed=1";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		try {
			ri = jt.executeQuery(sql, new Object[]{id, userName});
			if (ri.hasNext()) {
				return true;
			}
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error("isUserReaded: " + e.getMessage());
		} finally {
			jt.close();
		}
		return false;
	}

	public void setUnitCode(String unitCode) {
		this.unitCode = unitCode;
	}

	public String getUnitCode() {
		return unitCode;
	}

	public String getSqlList(HttpServletRequest request, Privilege privilege, boolean isNoticeAll, String fromDate, String toDate, boolean isSearch, String what, String cond, String orderBy, String sort) {
		String strCurDay = DateUtil.format(new java.util.Date(), "yyyy-MM-dd");
		String userName = privilege.getUser(request);

		String sql = "select id from oa_notice where 1=1";
		if (!privilege.isUserPrivValid(request, "admin") && !isNoticeAll) {
			sql += " and begin_date<=" + SQLFilter.getDateStr(strCurDay, "yyyy-MM-dd") + " and (end_date is null or end_date>=" + SQLFilter.getDateStr(strCurDay, "yyyy-MM-dd") + ")";
		}
		if (isSearch) {
			if (!"".equals(what)) {
				sql += " and " + cond + " like '%" + what + "%'";
			}

			if (!fromDate.equals("") && !toDate.equals("")) {
				java.util.Date d = DateUtil.parse(toDate, "yyyy-MM-dd");
				d = DateUtil.addDate(d, 1);
				String toDate2 = DateUtil.format(d, "yyyy-MM-dd");
				sql += " and (create_date>=" + SQLFilter.getDateStr(fromDate, "yyyy-MM-dd") + " and create_date<" + SQLFilter.getDateStr(toDate2, "yyyy-MM-dd") + ")";
			} else if (!fromDate.equals("")) {
				sql += " and create_date>=" + SQLFilter.getDateStr(fromDate, "yyyy-MM-dd");
			} else if (fromDate.equals("") && !toDate.equals("")) {
				sql += " and create_date<=" + SQLFilter.getDateStr(toDate, "yyyy-MM-dd");
			}
		}

		if (!privilege.isUserPrivValid(request, "admin") && !isNoticeAll) {
			sql += " and ((id in (select notice_id from oa_notice_reply where user_name = " + StrUtil.sqlstr(userName) + ")) or user_name=" + StrUtil.sqlstr(userName) + ")";
		}

		sql += " order by " + orderBy + " " + sort;
		return sql;
	}

	public boolean createNoticeForFlow() {
		Conn conn = new Conn(connname);
		boolean re = false;
		try {
			id = (long) SequenceManager.nextID(SequenceManager.OA_NOTICE);
			PreparedStatement pstmt = conn.prepareStatement(QUERY_CREATE);
			pstmt.setLong(1, id);
			pstmt.setString(2, title);
			pstmt.setString(3, content);
			pstmt.setString(4, userName);
			pstmt.setTimestamp(5, new Timestamp(createDate.getTime()));
			pstmt.setInt(6, isDeptNotice);
			pstmt.setInt(7, isShow);
			if (beginDate==null)
				pstmt.setTimestamp(8, null);
			else
				pstmt.setTimestamp(8, new java.sql.Timestamp(beginDate.getTime()));
			if (endDate==null)
				pstmt.setTimestamp(9, null);
			else
				pstmt.setTimestamp(9, new java.sql.Timestamp(endDate.getTime()));
			pstmt.setString(10, color);
			pstmt.setInt(11, bold?1:0);
			pstmt.setString(12, unitCode);
			pstmt.setInt(13, level);
			pstmt.setInt(14, isall);
			pstmt.setInt(15, flowId);
			pstmt.setInt(16, is_reply);
			pstmt.setInt(17, is_forced_response);

			re = conn.executePreUpdate() == 1 ? true : false;
			if (re) {
				NoticeCache rc = new NoticeCache(this);
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

	/**
	 * 
	 */
	private String usersKnow;
	
	public static final int LEVEL_NONE = 0;
	
	private int level = LEVEL_NONE;

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getIsall() {
		return isall;
	}

	public void setIsall(int isall) {
		this.isall = isall;
	}

	public String getUserList() {
		return userList;
	}

	public void setUserList(String userList) {
		this.userList = userList;
	}
	

}
