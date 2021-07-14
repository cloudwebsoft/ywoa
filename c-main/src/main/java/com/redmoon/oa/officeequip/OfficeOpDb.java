package com.redmoon.oa.officeequip;

import java.sql.*;
import java.util.*;
import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.person.UserDb;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class OfficeOpDb extends ObjectDb {
	private int id;
	public static final int TYPE_RECEIVE = 0; // 领用，如：耗材等
	public static final int TYPE_BORROW = 1; // 借用，如：U盘等
	public static final int TYPE_RETURN = 2; // 已归还

	public static final int RETURN_FLOWING = 1;
	public static final int RETURN_FLOWNONE = 0;
	public static final int RETURN_FLOWED = -1;
	
	private String operator;

	public OfficeOpDb() {
		init();
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public OfficeOpDb(int id) {
		this.id = id;
		init();
		load();
	}

	public int getId() {
		return id;
	}

	public String getOfficeCode() {
		return officeCode;
	}

	public int getCount() {
		return count;
	}

	public java.util.Date getOpDate() {
		return opDate;
	}

	public int getType() {
		return type;
	}

	public String getPerson() {
		return person;
	}

	public String getRemark() {
		return remark;
	}

	public java.util.Date getReturnDate() {
		return returnDate;
	}

	public void initDB() {
		tableName = "office_equipment_op";
		primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
		objectCache = new OfficeOpCache(this);
		isInitFromConfigDB = false;
		QUERY_CREATE = "insert into "
				+ tableName
				+ " (office_code, count,opDate,type,person,remark,returnDate,flowid,operator) values (?,?,?,?,?,?,?,?,?)";
		QUERY_SAVE = "update "
				+ tableName
				+ " set office_code=?, count=?,opDate=?,type=?,person=?,remark=?,returnDate=?,flowid=?,operator=? where id=?";
		QUERY_LIST = "select id from " + tableName;
		QUERY_DEL = "delete from " + tableName + " where id=?";
		QUERY_LOAD = "select office_code, count,opDate,type,person,remark,returnDate,flowid,operator from "
				+ tableName + " where id=?";
	}

	public OfficeOpDb getOfficeOpDb(int id) {
		return (OfficeOpDb) getObjectDb(new Integer(id));
	}

	public boolean create() throws ErrMsgException {
		Conn conn = new Conn(connname);
		boolean re = false;
		try {
			/**
			 * officeId,count,opDate,type,person,remark
			 */
			PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
			ps.setString(1, officeCode);
			ps.setInt(2, count);
			if (opDate != null) {
				ps.setDate(3, new java.sql.Date(opDate.getTime()));
			} else {
				ps.setDate(3, null);
			}
			ps.setInt(4, type);
			ps.setString(5, person);
			UserDb ud = new UserDb();
			ud = ud.getUserDb(person);
			if (ud == null || !ud.isLoaded()) {
				// throw new ErrMsgException("用户" + person + "不存在！");
			}
			ps.setString(6, remark);
			if (returnDate != null) {
				ps.setDate(7, new java.sql.Date(returnDate.getTime()));
			} else {
				ps.setDate(7, null);
			}
			ps.setInt(8, flowid);
			ps.setString(9, operator);

			re = conn.executePreUpdate() == 1 ? true : false;
			if (re) {
				OfficeOpCache rc = new OfficeOpCache(this);
				rc.refreshCreate();
			}
		} catch (SQLException e) {
			logger.error("create:" + e.getMessage());
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
				OfficeOpCache rc = new OfficeOpCache(this);
				primaryKey.setValue(new Integer(id));
				rc.refreshDel(primaryKey);
			}
		} catch (SQLException e) {
			logger.error("del: " + e.getMessage());
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
	 * @param pk
	 *            Object
	 * @return Object
	 * @todo Implement this cn.js.fan.base.ObjectDb method
	 */
	public ObjectDb getObjectRaw(PrimaryKey pk) {
		return new OfficeOpDb(pk.getIntValue());
	}

	/**
	 * 判断类型为officeId的类别是否已存在
	 * 
	 * @param officeId
	 *            int
	 * @return boolean
	 */
	public boolean hasOfficeOfType(int officeId) {
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		try {
			String sql = "select id from office_equipment_op where officeId=? and type="
					+ TYPE_BORROW;
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, officeId);
			rs = conn.executePreQuery();
			if (rs != null && rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			logger.error("hasOfficeOfType: " + e.getMessage());
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
		return false;
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
			/**
			 * officeId,count,opDate,type,person,remark
			 */

			PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
			ps.setInt(1, id);
			rs = conn.executePreQuery();
			if (rs != null && rs.next()) {
				officeCode = rs.getString(1);
				count = rs.getInt(2);
				try {
					opDate = rs.getDate(3);
				} catch (Exception e) {
					logger.error("load1:" + e.getMessage());
				}
				type = rs.getInt(4);
				person = rs.getString(5);
				remark = rs.getString(6);
				try {
					returnDate = rs.getDate(7);
				} catch (Exception e) {
					logger.error("load1:" + e.getMessage());
				}
				flowid = rs.getInt(8);
				operator = StrUtil.getNullStr(rs.getString(9));

				loaded = true;
				primaryKey.setValue(new Integer(id));
			}
		} catch (SQLException e) {
			logger.error("load: " + e.getMessage());
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
			/**
			 * officeId,count,opDate,type,person,remark
			 */
			PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
			ps.setString(1, officeCode);
			ps.setInt(2, count);
			if (opDate == null) {
				ps.setDate(3, null);
			} else {
				ps.setDate(3, new java.sql.Date(opDate.getTime()));
			}
			ps.setInt(4, type);
			ps.setString(5, person);
			UserDb ud = new UserDb();
			ud = ud.getUserDb(person);
			ps.setString(6, remark);
			if (returnDate == null) {
				ps.setDate(7, null);
			} else {
				ps.setDate(7, new java.sql.Date(returnDate.getTime()));
			}
			ps.setInt(8, flowid);
			ps.setString(9, operator);
			ps.setInt(10, id);
			re = conn.executePreUpdate() == 1 ? true : false;
			if (re) {
				OfficeOpCache rc = new OfficeOpCache(this);
				primaryKey.setValue(new Integer(id));
				rc.refreshSave(primaryKey);
			}
		} catch (SQLException e) {
			logger.error("save: " + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return re;
	}

	public ListResult listResult(String listsql, int curPage, int pageSize)
			throws ErrMsgException {
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

			if (total != 0) {
				conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用
			}

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
					OfficeOpDb ug = getOfficeOpDb(rs.getInt(1));
					result.addElement(ug);
				} while (rs.next());
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new ErrMsgException("数据库出错！");
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
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

	/**
	 * 判断是否存在该字段
	 */

	public boolean isExist(String tableName) {
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		try {
			rs = conn
					.executeQuery("select id from office_equipment where officeNum='"
							+ tableName + "'");
			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			logger.error("list:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return false;
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
					// System.out.println("OfficeOpDb list:" + rs.getInt(1));
					result.addElement(getOfficeOpDb(rs.getInt(1)));
				}
			}
		} catch (SQLException e) {
			logger.error("list:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return result;
	}

	/**
	 * 根据officecode查询数目
	 */

	public int queryNumByCode(String officecode, int iType) {
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		try {
			rs = conn
					.executeQuery("select sum(count) from office_equipment_op where office_code="
							+ StrUtil.sqlstr(officecode)
							+ " and type = "
							+ iType);
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("list:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return 0;
	}

	public int checkIsFlowing(String type) {
		Conn conn = new Conn(connname);
		String sql = "select flowid from form_table_" + type
				+ " where equip_id=?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int flowId;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, id);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				flowId = rs.getInt(1);
				WorkflowDb wf = new WorkflowDb();
				wf = wf.getWorkflowDb(flowId);
				if (wf.getStatus() == WorkflowDb.STATUS_STARTED) {
					return RETURN_FLOWING;
				} else if (wf.getStatus() == WorkflowDb.STATUS_FINISHED) {
					return RETURN_FLOWED;
				}
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return RETURN_FLOWNONE;
	}
	
	/**
	 * 删除办公用品时，先查询officecode是否存在入库借出记录
	 */

	public int isOfficeCodeExist(String officecode) {
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		try {
			rs = conn
					.executeQuery("select count(id) from office_equipment_op where office_code="
							+ StrUtil.sqlstr(officecode));
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("list:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return 0;
	}

	/**
	 * officeId,count,opDate,type,person,remark
	 */
	private String officeCode;
	private int count;
	private java.util.Date opDate;
	private int type;
	private String person;
	private String remark;
	private java.util.Date returnDate;

	private void jbInit() throws Exception {
	}

	public void setOfficeCode(String officeCode) {
		this.officeCode = officeCode;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setOpDate(java.util.Date opDate) {
		this.opDate = opDate;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setPerson(String person) {
		this.person = person;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setReturnDate(java.util.Date returnDate) {
		this.returnDate = returnDate;
	}

	private int flowid;

	public int getFlowid() {
		return flowid;
	}

	public void setFlowid(int flowid) {
		this.flowid = flowid;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getOperator() {
		return operator;
	}

}
