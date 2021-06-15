package com.redmoon.oa.officeequip;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;

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
public class OfficeDb extends ObjectDb {
	private int id;
	public static final int TYPE_PUBLIC = 1;
	public static final int TYPE_USER = 0;

	private String unitCode;
	
	/**
	 * 操作员
	 */
	private String operator;

	public OfficeDb() {
		init();
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public OfficeDb(int id) {
		this.id = id;
		init();
		load();
	}

	public int getId() {
		return id;
	}

	public int getTypeId() {
		return typeId;
	}

	public String getOfficeName() {
		return officeName;
	}

	public double getPrice() {
		return price;
	}

	public String getBuyPerson() {
		return buyPerson;
	}

	public int getStorageCount() {
		return storageCount;
	}

	public String getMeasureUnit() {
		return measureUnit;
	}

	public java.util.Date getBuyDate() {
		return buyDate;
	}

	public String getAbstracts() {
		return abstracts;
	}

	public void initDB() {
		tableName = "office_equipment";
		primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
		objectCache = new OfficeCache(this);
		isInitFromConfigDB = false;

		QUERY_CREATE = "insert into "
				+ tableName
				+ " (typeId, officeName,price,buyPerson,storageCount,measureUnit,buyDate,abstracts,unit_code,flowid,operator) values (?,?,?,?,?,?,?,?,?,?,?)";
		QUERY_SAVE = "update "
				+ tableName
				+ " set typeId=?, officeName=?,price=?,buyPerson=?,storageCount=?,measureUnit=?,buyDate=?,abstracts=?,unit_code=? where id=?";
		QUERY_LIST = "select id from " + tableName;
		QUERY_DEL = "delete from " + tableName + " where id=?";
		QUERY_LOAD = "select typeId, officeName,price,buyPerson,storageCount,measureUnit,buyDate,abstracts,unit_code,flowid,operator from "
				+ tableName + " where id=?";
	}

	public OfficeDb getOfficeDb(int id) {
		return (OfficeDb) getObjectDb(new Integer(id));
	}

	public boolean create() throws ErrMsgException {
		Conn conn = new Conn(connname);
		boolean re = false;
		try {
			/**
			 * typeId,
			 * officeName,price,buyPerson,storageCount,measureUnit,buyDate
			 */
			PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
			ps.setInt(1, typeId);
			ps.setString(2, officeName);
			ps.setDouble(3, price);
			ps.setString(4, buyPerson);
			ps.setInt(5, storageCount);
			ps.setString(6, measureUnit);
			if (buyDate != null)
				ps.setDate(7, new java.sql.Date(buyDate.getTime()));
			else
				ps.setDate(7, null);
			ps.setString(8, abstracts);
			ps.setString(9, unitCode);
			ps.setInt(10, flowid);
			ps.setString(11, operator);
			re = conn.executePreUpdate() == 1 ? true : false;
			if (re) {
				OfficeCache rc = new OfficeCache(this);
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
				OfficeCache rc = new OfficeCache(this);
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
		return new OfficeDb(pk.getIntValue());
	}

	/**
	 * 判断类型为typeId的书是否有被借出的
	 * 
	 * @param typeId
	 *            int
	 * @return boolean
	 */
	public boolean hasOfficeOfType(int typeId) {
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		try {
			String sql = "select id from office_equipment where typeId=?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, typeId);
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
			 * typeId,
			 * officeName,price,buyPerson,storageCount,measureUnit,buyDate
			 */

			PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
			ps.setInt(1, id);
			rs = conn.executePreQuery();
			if (rs != null && rs.next()) {
				typeId = rs.getInt(1);
				officeName = rs.getString(2);
				price = rs.getDouble(3);
				buyPerson = rs.getString(4);
				storageCount = rs.getInt(5);
				measureUnit = rs.getString(6);
				try {
					buyDate = rs.getDate(7);
				} catch (Exception e) {
					logger.error("load1:" + e.getMessage());
				}
				abstracts = rs.getString(8);
				unitCode = rs.getString(9);
				flowid = rs.getInt(10);
				operator = StrUtil.getNullStr(rs.getString(11));
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
			 * typeId,
			 * officeName,price,buyPerson,storageCount,measureUnit,buyDate
			 */
			PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
			ps.setInt(1, typeId);
			ps.setString(2, officeName);
			ps.setDouble(3, price);
			ps.setString(4, buyPerson);
			ps.setInt(5, storageCount);
			ps.setString(6, measureUnit);
			if (buyDate == null)
				ps.setDate(7, null);
			else
				ps.setDate(7, new java.sql.Date(buyDate.getTime()));
			ps.setString(8, abstracts);
			ps.setString(9, unitCode);
			ps.setInt(10, id);
			re = conn.executePreUpdate() == 1 ? true : false;
			if (re) {
				OfficeCache rc = new OfficeCache(this);
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
					OfficeDb ug = getOfficeDb(rs.getInt(1));
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
					.executeQuery("select id from office_equipment where officeName="
							+ StrUtil.sqlstr(tableName));
			if (rs.next())
				return true;
		} catch (SQLException e) {
			logger.error("isExist:" + e.getMessage());
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
					// System.out.println("OfficeDb list:" + rs.getInt(1));
					result.addElement(getOfficeDb(rs.getInt(1)));
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
	 * 根据商品code取出入库总数
	 */

	public int querySumByCode(String officeCode) {
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		try {
			rs = conn
					.executeQuery("select sum(storageCount) from office_equipment where officeName='"
							+ officeCode + "'");
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
	 * 取得某办公用品最后加入的记录
	 * @Description: 
	 * @param officeName
	 * @return
	 */
	public OfficeDb getOfficeDbLastAdded(String officeName) {
		String sql = "select id from " + tableName + " where officeName=" + StrUtil.sqlstr(officeName) + " order by id desc";
		try {
			ListResult lr = listResult(sql, 1, 1);
			Iterator ir = lr.getResult().iterator();
			if (ir.hasNext()) {
				return (OfficeDb)ir.next();
			}
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	// 根据officeCode查询
	public List<OfficeDb> queryDataByOfficeCode(String officeCode) {
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		ArrayList<OfficeDb> arr = new ArrayList<OfficeDb>();
		try {
			/**
			 * typeId,
			 * officeName,price,buyPerson,storageCount,measureUnit,buyDate
			 */

			String sql = "select typeId, officeName,price,buyPerson,storageCount,measureUnit,buyDate,abstracts,unit_code,flowid from "
				+ tableName + " where officeName=?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, officeCode);
			rs = conn.executePreQuery();
			while (rs.next()) {
				OfficeDb od = new OfficeDb();
				od.setTypeId(rs.getInt(1));
				od.setOfficeName(rs.getString(2));
				od.setPrice(rs.getDouble(3));
				od.setBuyPerson(rs.getString(4));
				od.setStorageCount(rs.getInt(5));
				od.setMeasureUnit(rs.getString(6));
				try {
					od.setBuyDate(rs.getDate(7));
				} catch (Exception e) {
					logger.error("queryDataByOfficeCode:" + e.getMessage());
				}
				od.setAbstracts(rs.getString(8));
				od.setUnitCode(rs.getString(9));
				od.setFlowid(rs.getInt(10));
				arr.add(od);
			}
		} catch (SQLException e) {
			logger.error("queryDataByOfficeCode: " + e.getMessage());
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
		return arr;
	}

	/**
	 * typeId, officeName,price,buyPerson,storageCount,measureUnit,buyDate
	 */
	private int typeId;
	private String officeName;
	private double price;
	private String buyPerson;
	private int storageCount;
	private String measureUnit;
	private java.util.Date buyDate;
	private String abstracts;

	private void jbInit() throws Exception {
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public void setOfficeName(String officeName) {
		this.officeName = officeName;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public void setBuyPerson(String buyPerson) {
		this.buyPerson = buyPerson;
	}

	public void setStorageCount(int storageCount) {
		this.storageCount = storageCount;
	}

	public void setMeasureUnit(String measureUnit) {
		this.measureUnit = measureUnit;
	}

	public void setBuyDate(java.util.Date buyDate) {
		this.buyDate = buyDate;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setAbstracts(String abstracts) {
		this.abstracts = abstracts;
	}

	public void setUnitCode(String unitCode) {
		this.unitCode = unitCode;
	}

	public String getUnitCode() {
		return unitCode;
	}

	private int flowid = 0;

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
