package com.redmoon.oa.officeequip;

import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.Conn;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

public class OfficeStocktakingDb extends ObjectDb {

	private int id;
	private String equipmentCode;
	private int stockNum;
	private int realNum;
	private Date modifyTime;
	private int isTookstock;
	private int flowid = 0;
	
	private String operator;

	public void initDB() {
		tableName = "office_stocktaking";
		primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
		objectCache = new OfficeStocktakingCache(this);
		isInitFromConfigDB = false;

		QUERY_CREATE = "insert into "
				+ tableName
				+ " (equipment_code, stock_num,real_num,is_tookstock,flowid, operator) values (?,?,?,?,?,?)";
		QUERY_SAVE = "update "
				+ tableName
				+ " set real_num=?,modify_time=?,is_tookstock=?,flowid=? where equipment_code=? and is_tookstock=0";
		QUERY_LIST = "select id from " + tableName;
		QUERY_DEL = "delete from " + tableName + " where id=?";
		QUERY_LOAD = "select equipment_code, stock_num,real_num,modify_time,is_tookstock,operator from "
				+ tableName + " where id=?";
	}
	
	public OfficeStocktakingDb() {
		init();
	}
	
	public OfficeStocktakingDb(int id) {
		this.id = id;
		init();
		load();
	}

	@Override
	public boolean del() throws ErrMsgException, ResKeyException {
		return false;
	}

	@Override
	public ObjectDb getObjectRaw(PrimaryKey pk) {
		return new OfficeStocktakingDb(pk.getIntValue());
	}

	@Override
	public void load() {
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
			ps.setInt(1, id);
			rs = conn.executePreQuery();
			if (rs != null && rs.next()) {
				equipmentCode = rs.getString(1);
				stockNum = rs.getInt(2);
				realNum = rs.getInt(3);
				modifyTime = rs.getTimestamp(4);
				isTookstock = rs.getInt(5);
				operator = StrUtil.getNullStr(rs.getString(6));
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

	@Override
	public boolean save() throws ErrMsgException, ResKeyException {

		// 已有商品code即update商品数量
		int iStock = hasEquipCode(equipmentCode);
		if (iStock > -1) {
			String sqlupdate = "update "
					+ tableName
					+ " set stock_num=? where equipment_code=? and is_tookstock=0";
			Conn conn = new Conn(connname);
			boolean re = false;
			try {

				PreparedStatement ps = conn.prepareStatement(sqlupdate);
				ps.setInt(1, stockNum + iStock);
				ps.setString(2, equipmentCode);

				re = conn.executePreUpdate() == 1 ? true : false;
				if (re) {
					OfficeStocktakingCache rc = new OfficeStocktakingCache(this);
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
		// 没有商品code即insert商品记录
		else {
			Conn conn = new Conn(connname);
			boolean re = false;
			try {
				PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
				ps.setString(1, equipmentCode);
				ps.setInt(2, stockNum);
				ps.setInt(3, -1);// 该条记录未盘点过
				ps.setInt(4, 0);
				ps.setInt(5, flowid);
				ps.setString(6, operator);				

				re = conn.executePreUpdate() == 1 ? true : false;
				if (re) {
					OfficeStocktakingCache rc = new OfficeStocktakingCache(this);
					rc.refreshCreate();
				}
			} catch (SQLException e) {
				logger.error("save:" + e.getMessage());
				throw new ErrMsgException("数据库操作失败！");
			} finally {
				if (conn != null) {
					conn.close();
					conn = null;
				}
			}
			return re;
		}
	}
	
	public boolean saveForImport() {
		int iStock = hasEquipCode(equipmentCode);
		if (iStock > -1) {
			String sqlupdate = "update "
					+ tableName
					+ " set stock_num=? where equipment_code=? and is_tookstock=0";
			Conn conn = new Conn(connname);
			boolean re = false;
			try {

				PreparedStatement ps = conn.prepareStatement(sqlupdate);
				ps.setInt(1, stockNum);
				ps.setString(2, equipmentCode);

				re = conn.executePreUpdate() == 1 ? true : false;
				if (re) {
					OfficeStocktakingCache rc = new OfficeStocktakingCache(this);
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
		// 没有商品code即insert商品记录
		else {
			Conn conn = new Conn(connname);
			boolean re = false;
			try {
				PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
				ps.setString(1, equipmentCode);
				ps.setInt(2, stockNum);
				ps.setInt(3, -1);// 该条记录未盘点过
				ps.setInt(4, 0);
				ps.setInt(5, flowid);
				ps.setString(6, operator);

				re = conn.executePreUpdate() == 1 ? true : false;
				if (re) {
					OfficeStocktakingCache rc = new OfficeStocktakingCache(this);
					rc.refreshCreate();
				}
			} catch (SQLException e) {
				logger.error("save:" + e.getMessage());
			} finally {
				if (conn != null) {
					conn.close();
					conn = null;
				}
			}
			return re;
		}
	}

	// 领用
	public boolean changeStocknum(int ily, String equipmentCode)
			throws ErrMsgException {
		int iStock = hasEquipCode(equipmentCode);
		if (iStock > 0) {
			if (ily > iStock)
				throw new ErrMsgException("库存不足！");
			String sqlupdate = "update "
					+ tableName
					+ " set stock_num=? where equipment_code=? and is_tookstock=0";
			Conn conn = new Conn(connname);
			boolean re = false;
			try {

				PreparedStatement ps = conn.prepareStatement(sqlupdate);
				ps.setInt(1, iStock - ily);
				ps.setString(2, equipmentCode);

				re = conn.executePreUpdate() == 1 ? true : false;
				if (re) {
					OfficeStocktakingCache rc = new OfficeStocktakingCache(this);
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
		} else {
			throw new ErrMsgException("该商品还未入库！");
		}
	}

	// 归还
	public boolean returnChangeStocknum(int ily, String equipmentCode)
			throws ErrMsgException {
		int iStock = hasEquipCode(equipmentCode);
		if (iStock >= 0) {

			String sqlupdate = "update "
					+ tableName
					+ " set stock_num=? where equipment_code=? and is_tookstock=0";
			Conn conn = new Conn(connname);
			boolean re = false;
			try {

				PreparedStatement ps = conn.prepareStatement(sqlupdate);
				ps.setInt(1, iStock + ily);
				ps.setString(2, equipmentCode);

				re = conn.executePreUpdate() == 1 ? true : false;
				if (re) {
					OfficeStocktakingCache rc = new OfficeStocktakingCache(this);
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
		} else {
			throw new ErrMsgException("该商品还未入库！");
		}
	}

	// 查询库存表是否存在该商品code
	public int hasEquipCode(String equipCode) {
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		try {
			String sql = "select stock_num,id from office_stocktaking where equipment_code=? and is_tookstock=0";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, equipCode);
			rs = conn.executePreQuery();
			if (rs != null && rs.next()) {
				id = rs.getInt(2);
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("hasEquipCode: " + e.getMessage());
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
		return -1;
	}

	// 根据code查询库存
	public int queryNumByCode(String officecode) {
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		try {
			rs = conn
					.executeQuery("select stock_num from office_stocktaking where equipment_code='"
							+ officecode + "' and is_tookstock = 0");
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
	
	// 查询officecode是否盘点过
	public int isOfficeCodeExist(String officecode) {
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		try {
			rs = conn
					.executeQuery("select count(id) from office_stocktaking where equipment_code='"
							+ officecode + "' and is_tookstock = 0");
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

	// 执行盘点操作,先更新成历史盘点记录
	public boolean updateStock() {
		Conn conn = new Conn(connname);
		boolean re = false;
		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
			ps.setInt(1, realNum);
			ps.setTimestamp(2, new Timestamp((new Date()).getTime()));
			ps.setInt(3, 1);
			ps.setInt(4, flowid);
			ps.setString(5, equipmentCode);
			re = conn.executePreUpdate() == 1 ? true : false;
			if (re) {
				OfficeStocktakingCache rc = new OfficeStocktakingCache(this);
				primaryKey.setValue(new Integer(id));
				rc.refreshSave(primaryKey);
			}
		} catch (SQLException e) {
			logger.error("list:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return re;
	}

	// 执行盘点操作,再插入一条新盘点记录
	public boolean insertStock() {
		Conn conn = new Conn(connname);
		boolean re = false;
		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
			ps.setString(1, equipmentCode);
			ps.setInt(2, realNum);// 将用户输入的实际库存数存为盘点数
			ps.setInt(3, -1);
			ps.setInt(4, 0);
			ps.setInt(5, flowid);
			ps.setString(6, operator);

			re = conn.executePreUpdate() == 1 ? true : false;
			if (re) {
				OfficeStocktakingCache rc = new OfficeStocktakingCache(this);
				rc.refreshCreate();
			}
		} catch (SQLException e) {
			logger.error("list:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return re;
	}

	public String getEquipmentCode() {
		return equipmentCode;
	}

	public void setEquipmentCode(String equipmentCode) {
		this.equipmentCode = equipmentCode;
	}

	public int getStockNum() {
		return stockNum;
	}

	public void setStockNum(int stockNum) {
		this.stockNum = stockNum;
	}

	public int getRealNum() {
		return realNum;
	}

	public void setRealNum(int realNum) {
		this.realNum = realNum;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public int getIsTookstock() {
		return isTookstock;
	}

	public void setIsTookstock(int isTookstock) {
		this.isTookstock = isTookstock;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

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
