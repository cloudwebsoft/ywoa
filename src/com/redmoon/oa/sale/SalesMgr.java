package com.redmoon.oa.sale;

import java.sql.SQLException;
import java.util.Date;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.FormDAO;

public class SalesMgr {

	public boolean isOrderOutOfStock(long orderId) {
		String sql = "select id from form_table_sales_stock_info where sales_order=?";
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql, new Object[] { new Long(
					orderId) });
			if (ri.hasNext()) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	
	public void createHistory(int cId,int hid,String creator,String action){
		String sql = "insert into sales_chance_history (chanceId,hid,update_date,creator,action) values (?,?,?,?,?)";
		Date date = new Date();
		JdbcTemplate jt = new JdbcTemplate();
		try{
			jt.executeUpdate(sql,new Object[]{cId,hid,date,creator,action});
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public int getSid(){
		String sql = "select max(id) from form_table_sales_chance";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		ResultRecord rd = null;
		int sid = 0;
		try{
			ri = jt.executeQuery(sql);
			if(ri.hasNext()){
				rd = (ResultRecord)ri.next();
				sid = rd.getInt(1);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return sid;
	}
	
	public boolean createHistoryRecord(long cid){
		FormDb fDb = new FormDb();
		fDb = fDb.getFormDb("sales_chance");
		FormDAO fdao = new FormDAO();
		fdao = fdao.getFormDAO(cid, fDb);
		String code = fdao.getFieldValue("code");
		String chanceName = fdao.getFieldValue("chanceName");
		String expectPrice = fdao.getFieldValue("expectPrice");
		String customer = fdao.getFieldValue("customer");
		String find_date = fdao.getFieldValue("find_date");
		String provider = fdao.getFieldValue("provider");
		String pre_date = fdao.getFieldValue("pre_date");
		String state = fdao.getFieldValue("state");
		String sjzt = fdao.getFieldValue("sjzt");
		String sjly = fdao.getFieldValue("sjly");
		String possibility = fdao.getFieldValue("possibility");
		String description = fdao.getFieldValue("description");
		String sql = "insert into form_table_sales_chance_bak (code,chanceName,expectPrice," +
				"customer,find_date,provider,pre_date,state,sjzt,sjly,possibility,description) values (?,?,?,?,?,?,?,?,?,?,?,?)";
		JdbcTemplate jt = new JdbcTemplate();
		try {
			return jt.executeUpdate(sql, new Object[]{code,chanceName,expectPrice,customer,find_date,provider,pre_date,state,sjzt,sjly,possibility,description})>0?true:false;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}
	
	public int getHid(){
		String sql = "select max(id) from form_table_sales_chance_bak";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		ResultRecord rd = null;
		int hid = 0;
		try{
			ri = jt.executeQuery(sql);
			if(ri.hasNext()){
				rd = (ResultRecord)ri.next();
				hid = rd.getInt(1);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return hid;
	}
	
}
