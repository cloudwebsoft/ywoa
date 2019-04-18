package com.redmoon.oa.sale;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.visual.IModuleChecker;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import com.redmoon.oa.visual.FormDAO;

import java.sql.SQLException;
import java.util.Vector;
import com.redmoon.kit.util.FileUpload;

/**
 * <p>
 * Title: 订单的有效性验证
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class SalesDailyReportChecker implements IModuleChecker {
	public SalesDailyReportChecker() {
		super();
	}

	public boolean validateUpdate(HttpServletRequest request, FileUpload fu,
			FormDAO fdaoBeforeUpdate, Vector fields) throws ErrMsgException {

		return true;
	}

	public boolean validateCreate(HttpServletRequest request, FileUpload fu,
			Vector fields) throws ErrMsgException {

		return true;
	}

	public boolean validateDel(HttpServletRequest request, FormDAO fdao)
			throws ErrMsgException {

		return true;
	}

	public boolean onDel(HttpServletRequest request, FormDAO fdao)
			throws ErrMsgException {
		return true;
	}

	public boolean onCreate(HttpServletRequest request, FormDAO fdao)
			throws ErrMsgException {
		String sql = "select * from form_table_sales_daily_lxr where cws_id="
				+ fdao.getId();
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri;
		try {
			ri = jt.executeQuery(sql);
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				String lxr = rr.getString("lxr");
				String customer = rr.getString("customer");
				String contact_purpose = rr.getString("contact_purpose");
				String solution = rr.getString("solution");
				String contact_type = rr.getString("contact_type");
				String sales_person = rr.getString("sales_person");
				// String contact_result = rr.getString("contact_result");

				// sql =
				// "insert into form_table_day_lxr (lxr,customer,contact_purpose,solution,contact_type,cws_id,unit_code, cws_creator, cws_status, flowId,contact_result) values (?,?,?,?,?,?,?,?,?,-1,?)";
				// jt.executeUpdate(sql, new
				// Object[]{lxr,customer,contact_purpose,solution,contact_type,lxr,
				// wf.getUnitCode(), wf.getUserName(), FormDAO.STATUS_DONE,
				// contact_result}); //

				sql = "insert into form_table_day_lxr (lxr,customer,contact_purpose,solution,contact_type,cws_id,unit_code, cws_creator, cws_status, flowId, sales_person) values (?,?,?,?,?,?,?,?,?,-1,?)";
				jt
						.executeUpdate(sql, new Object[] { lxr, customer,
								contact_purpose, solution, contact_type, lxr,
								fdao.getUnitCode(), fdao.getCreator(),
								com.redmoon.oa.flow.FormDAO.STATUS_DONE,
								sales_person }); //
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public boolean onNestTableCtlAdd(HttpServletRequest request,
			HttpServletResponse response, JspWriter out) {
		return false;
	}

	public boolean onUpdate(HttpServletRequest request, FormDAO fdao)
			throws ErrMsgException {
		return true;
	}
}
