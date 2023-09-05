package com.redmoon.oa.kaoqin;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.oacalendar.OACalendarDb;

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
public class KaoqinDb extends ObjectDb {
	private int id;
	
	/**
	 * 1上午上班 2上午下班 3下午上班 4下午下班 5晚上上班 6晚上下班 0其它
	 */
	private int kind = 0;
	
	/**
	 * 迟到或早退时长，值大于0
	 */
	private int timeMin = 0;
	
	/**
	 * 标志，默认为0，迟到为1，早退为2；
	 */
	private int flag = 0;

	public static final int TYPE_SYSTEM = 1;
	public static final int TYPE_USER = 0;

	public KaoqinDb() {
		init();
	}

	public KaoqinDb(int id) {
		this.id = id;
		init();
		load();
	}

	public int getId() {
		return id;
	}

	public void initDB() {
		tableName = "kaoqin";
		primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
		objectCache = new KaoqinCache(this);
		isInitFromConfigDB = false;

		QUERY_CREATE = "insert into " + tableName + " (name,reason,direction,type,myDate,kind,time_min,flag) values (?,?,?,?,?,?,?,?)";
		QUERY_SAVE = "update " + tableName + " set name=?,reason=?,direction=?,type=?,myDate=?,time_min=?,flag=? where id=?";
		QUERY_LIST = "select id from " + tableName + " order by mydate desc";
		QUERY_DEL = "delete from " + tableName + " where id=?";
		QUERY_LOAD = "select name,reason,direction,type,myDate,kind,time_min,flag from " + tableName + " where id=?";
	}

	public KaoqinDb getKaoqinDb(int id) {
		return (KaoqinDb) getObjectDb(new Integer(id));
	}
	
	public KaoqinDb getKaoqinDb(String userName, java.util.Date dt, int kind) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		int y = cal.get(Calendar.YEAR);
		int m = cal.get(Calendar.MONTH) + 1;
		int d = cal.get(Calendar.DAY_OF_MONTH);
		String str = SQLFilter.year("mydate") + "=" + y + " and " + SQLFilter.month("mydate") + "=" + m + " and " + SQLFilter.day("mydate") + "=" + d;
		String sql = "select id from kaoqin where name=" + StrUtil.sqlstr(userName) + " and " + str + " and kind=" + kind;
		try {
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(sql);
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				int id = rr.getInt(1);
				return getKaoqinDb(id);
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		return null;
	}

	/**
	 * 判断当前是否已经考勤过了
	 * @param name String
	 * @param direction String
	 * @return boolean
	 */
	public boolean isKaoqinDone(String name, String direction, String type) {
		boolean re = false;
		// 如果是考勤则验证是否该考勤项目已签，即不允许重复签
		Calendar cal = Calendar.getInstance();

		java.util.Date d1 = DateUtil.parse(DateUtil.format(cal.getTime(), "yyyy-MM-dd") + " 00:00", "yyyy-MM-dd HH:mm");
		java.util.Date d2 = DateUtil.parse(DateUtil.format(cal.getTime(), "yyyy-MM-dd") + " 23:59", "yyyy-MM-dd HH:mm");

		/*
		 int myhour = cal.get(cal.HOUR_OF_DAY);
		 OACalendarDb oad = new OACalendarDb();
		 oad = (OACalendarDb)oad.getQObjectDb(cal.getTime());
		 if (myhour<12) {
		 String ta1 = StrUtil.getNullStr(oad.getString("work_time_begin_a"));
		 String ta2 = StrUtil.getNullStr(oad.getString("work_time_end_a"));

		 d1 = DateUtil.parse(DateUtil.format(cal.getTime(), "yyyy-MM-dd") + " " +
		 ta1, "yyyy-MM-dd HH:mm");
		 d2 = DateUtil.parse(DateUtil.format(cal.getTime(), "yyyy-MM-dd") + " " +
		 ta2, "yyyy-MM-dd HH:mm");
		 }
		 else {
		 String tb1 = StrUtil.getNullStr(oad.getString("work_time_begin_b"));
		 String tb2 = StrUtil.getNullStr(oad.getString("work_time_end_b"));
		 d1 = DateUtil.parse(DateUtil.format(cal.getTime(), "yyyy-MM-dd") + " " +
		 tb1, "yyyy-MM-dd HH:mm");
		 d2 = DateUtil.parse(DateUtil.format(cal.getTime(), "yyyy-MM-dd") + " " +
		 tb2, "yyyy-MM-dd HH:mm");
		 }
		 */

		String sql = "select id from kaoqin where name=? and direction=? and type=?";
		sql += " and myDate>=" + SQLFilter.getDateStr(DateUtil.format(d1, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss") + " and myDate<" + SQLFilter.getDateStr(DateUtil.format(d2, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss");
		LogUtil.getLog(getClass()).info("sql:" + sql);

		Conn conn = new Conn(connname);
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, name);
			ps.setString(2, direction);
			ps.setString(3, type);
			ResultSet rs = conn.executePreQuery();
			if (rs != null && rs.next()) {
				// LogUtil.getLog(getClass()).info("isKaoqinDone:" + " is done.");
				re = true;
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error("isKaoqinDone:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return re;
	}

	public boolean create() throws ErrMsgException {
		Conn conn = new Conn(connname);
		boolean re = false;
		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
			ps.setString(1, name);
			ps.setString(2, reason);
			ps.setString(3, direction);
			ps.setString(4, type);
			if (myDate==null) {
				throw new ErrMsgException("考勤时间不能为空！");
			}
			ps.setTimestamp(5, new Timestamp(myDate.getTime()));
			ps.setInt(6, kind);
			ps.setInt(7, timeMin);
			ps.setInt(8, flag);
			re = conn.executePreUpdate() == 1 ? true : false;
			if (re) {
				KaoqinCache rc = new KaoqinCache(this);
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

	/**
	 * del
	 *
	 * @return boolean
	 * @throws ErrMsgException
	 * @throws ResKeyException
	 * @todo Implement this cn.js.fan.base.ObjectDb method
	 */
	public boolean del() throws ErrMsgException {
		Conn conn = new Conn(connname);
		boolean re = false;
		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
			ps.setInt(1, id);
			re = conn.executePreUpdate() == 1 ? true : false;
			if (re) {
				KaoqinCache rc = new KaoqinCache(this);
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

	/**
	 *
	 * @param pk Object
	 * @return Object
	 * @todo Implement this cn.js.fan.base.ObjectDb method
	 */
	public ObjectDb getObjectRaw(PrimaryKey pk) {
		return new KaoqinDb(pk.getIntValue());
	}

	/**
	 * load
	 *
	 * @throws ErrMsgException
	 * @throws ResKeyException
	 * @todo Implement this cn.js.fan.base.ObjectDb method
	 */
	public void load() {
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		try {
			// QUERY_LOAD = "select name,reason,direction,type,myDate from " + tableName + " where id=?";
			PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
			ps.setInt(1, id);
			rs = conn.executePreQuery();
			if (rs != null && rs.next()) {
				name = rs.getString(1);
				reason = StrUtil.getNullStr(rs.getString(2));
				direction = rs.getString(3);
				type = rs.getString(4);
				myDate = rs.getTimestamp(5);
				kind = rs.getInt(6);
				timeMin = rs.getInt(7);
				flag = rs.getInt(8);
				loaded = true;
				primaryKey.setValue(new Integer(id));
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error("load: " + e.getMessage());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
				rs = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
	}

	/**
	 * save
	 *
	 * @return boolean
	 * @throws ErrMsgException
	 * @throws ResKeyException
	 * @todo Implement this cn.js.fan.base.ObjectDb method
	 */
	public boolean save() throws ErrMsgException {
		Conn conn = new Conn(connname);
		boolean re = false;
		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
			ps.setString(1, name);
			ps.setString(2, reason);
			ps.setString(3, direction);
			ps.setString(4, type);
			if (myDate==null) {
				throw new ErrMsgException("考勤时间不能为空！");
			}			
			ps.setTimestamp(5, new Timestamp(myDate.getTime()));
			ps.setInt(6, timeMin);
			ps.setInt(7, flag);
			ps.setInt(8, id);
			re = conn.executePreUpdate() == 1 ? true : false;

			if (re) {
				KaoqinCache rc = new KaoqinCache(this);
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

	/**
	 * 取出全部信息置于result中
	 */
	public Vector list(String sql) {
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		Vector result = new Vector();
		try {
			rs = conn.executeQuery(sql);
			if (rs == null) {
				return null;
			} else {
				while (rs.next()) {
					result.addElement(getKaoqinDb(rs.getInt(1)));
				}
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error("list:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return result;
	}

	@Override
    public ListResult listResult(String listsql, int curPage, int pageSize) throws ErrMsgException {
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
					KaoqinDb ug = getKaoqinDb(rs.getInt(1));
					result.addElement(ug);
				} while (rs.next());
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e.getMessage());
			throw new ErrMsgException("数据库出错！");
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
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

	public String getName() {
		return name;
	}

	public String getReason() {
		return reason;
	}

	public java.util.Date getMyDate() {
		return myDate;
	}

	public String getDirection() {
		return direction;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public void setMyDate(java.util.Date myDate) {
		this.myDate = myDate;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}


	private String name;

	private String reason;

	private java.util.Date myDate;

	private String type;

	private String direction;

	public int getKind() {
		return kind;
	}

	public void setKind(int kind) {
		this.kind = kind;
	}

	public int getTimeMin() {
		return timeMin;
	}

	public void setTimeMin(int timeMin) {
		this.timeMin = timeMin;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

}
