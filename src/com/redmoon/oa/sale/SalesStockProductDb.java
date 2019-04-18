package com.redmoon.oa.sale;

import java.sql.SQLException;

import cn.js.fan.db.*;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;

public class SalesStockProductDb extends QObjectDb {

	/**
	 * 根据仓库及产品获得库存记录
	 * @param stockId
	 * @param productId
	 * @return
	 */
	public SalesStockProductDb getSalesStockProductDb(long stockId, long productId) {
		String sql = "select id from " + getTable().getName() + " where stock_id=? and product_id=?";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri;
		try {
			ri = jt.executeQuery(sql, new Object[]{new Long(stockId), new Long(productId)});
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				return (SalesStockProductDb)getQObjectDb(new Long(rr.getLong(1)));
			}			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public synchronized boolean updateNum(int alt) throws ResKeyException {
		set("num", new Integer(getInt("num") + alt));
		return save();
	}
	
	/**
	 * 取得某产品的库存
	 * @param productId
	 * @return
	 */
	public int getNumOfProduct(long productId) {
		String sql = "select sum(num) from " + getTable().getName() + " where product_id=?";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri;
		try {
			ri = jt.executeQuery(sql, new Object[]{new Long(productId)});
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				return rr.getInt(1);
			}			
		} catch (SQLException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}

		return 0;
	}
}
