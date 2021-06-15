package com.redmoon.oa.sale;

import java.sql.SQLException;
import java.util.Iterator;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.FormDAO;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-2-2下午10:50:00
 */
public class CustomerMgr {

	/**
	 * 取得三个月内未被访问的客户
	 * @Description: 
	 * @return
	 */
	public static int getCustomerNotVisitedCount(String userName) {
		String sql = "select count(id) from form_table_sales_customer where sales_person=? and last_visit_date is null";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		try {
			ri = jt.executeQuery(sql);
			return ri.size();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * 初始化客户的last_visit_date字段
	 * @Description: 
	 * @return
	 */
	public static int initLastVisitDate() {
		String sql = "select id from form_table_sales_customer";
		
		FormDAO fdao = new FormDAO();
		
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		try {
			ri = jt.executeQuery(sql);
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				
				String sql2 = "select d.id from form_table_day_lxr d, form_table_sales_linkman l where d.lxr=l.id and l.customer=" + rr.getInt(1) + " order by visit_date desc"; //  + " and d.is_visited='是'";
				Iterator ir2 = fdao.listResult("day_lxr", sql2, 1, 1).getResult().iterator();
				if (ir2.hasNext()) {
					FormDAO fdao2 = (FormDAO)ir2.next();
					String visitDate = fdao2.getFieldValue("visit_date");
					java.util.Date vd = DateUtil.parse(visitDate, "yyyy-MM-dd");
					String sql3 = "update form_table_sales_customer set last_visit_date=? where id=" + rr.getInt(1);
					jt.executeUpdate(sql3, new Object[]{vd});
					
					// System.out.println("id=" + rr.getInt(1) + " visitDate=" + visitDate);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("init over");
		return 0;		
	}
}
