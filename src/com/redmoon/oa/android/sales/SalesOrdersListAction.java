package com.redmoon.oa.android.sales;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.visual.FormDAO;

public class SalesOrdersListAction {
	private final static String SALES_ORDER = "sales_order";
	private int pageSize = 10;
	private String skey = "";
	private String result = "";
	private long customerId = 0;
	private long chanceId = 0;// 商机

	public long getChanceId() {
		return chanceId;
	}

	public void setChanceId(long chanceId) {
		this.chanceId = chanceId;
	}

	public long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}

	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public int getPagenum() {
		return pagenum;
	}

	public void setPagenum(int pagenum) {
		this.pagenum = pagenum;
	}

	private int pagenum;

	public String execute() {
		JSONObject json = new JSONObject();
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(getSkey());
		StringBuilder sqlSb = new StringBuilder();
		FormDAO fdao = new FormDAO();
		HttpServletRequest request = ServletActionContext.getRequest();
		privilege.doLogin(request, skey);
		com.redmoon.oa.pvg.Privilege priv = new com.redmoon.oa.pvg.Privilege();
		String privSaleUser = "sales.user";
		try {
			if (re) {
				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			}
			if (!priv.isUserPrivValid(request, privSaleUser)
					&& !priv.isUserPrivValid(request, "sales")
					&& !priv.isUserPrivValid(request, "sales.manager")) {
				json.put("res", "-1");
				json.put("msg", "权限非法");
				setResult(json.toString());
				return "SUCCESS";
			}
			sqlSb.append("select id from form_table_").append(SALES_ORDER)
					.append(" where 1= 1");
			json.put("res", "0");
			json.put("msg", "操作成功");
			JSONObject result = new JSONObject();
			if (pagenum <= 0) {
				pagenum = 1;
			}
			if (customerId != 0) {
				sqlSb.append(" and customer = ").append(customerId);
			}
			if (chanceId != 0) {
				sqlSb.append(" and chance = ").append(chanceId);
			}
			if (chanceId == 0 && customerId == 0) {
				sqlSb.append(" and unit_code = ").append("'").append(
						priv.getUserUnitCode(request)).append("' ");
				if (!priv.isUserPrivValid(request, "admin")
						&& !priv.isUserPrivValid(request, "sales")
						&& !priv.isUserPrivValid(request, "sales.manager")) {
					String salesPerson = SalesModuleDao.getSalesPerson(priv
							.getUser(request));
					if (!salesPerson.equals("")) {
						sqlSb.append(" and sales_user in ( ").append(salesPerson)
								.append(")");
					}
				} else if (priv.isUserPrivValid(request, "sales.manager")
						&& !priv.isUserPrivValid(request, "admin")
						&& !priv.isUserPrivValid(request, "sales")) {
					// 根据部门管理权限，查看所属部门的客户
					DeptUserDb dud = new DeptUserDb(priv.getUser(request));
					String dept = dud.getDeptCode();
					Vector vec = dud.getAllUsersOfUnit(dept);
					Iterator it = vec.iterator();
					String salesPerson = "";
					while (it.hasNext()) {
						UserDb ud = (UserDb) it.next();
						if (salesPerson.equals("")) {
							salesPerson = StrUtil.sqlstr(ud.getName());
						} else {
							salesPerson += "," + StrUtil.sqlstr(ud.getName());
						}
					}
					if (!salesPerson.equals("")) {
						sqlSb.append(" and sales_user in ( ").append(salesPerson)
								.append(")");
					}
				}
			}
			ListResult lr = fdao.listResult(SALES_ORDER, sqlSb.toString(),
					pagenum, pageSize);
			int total = lr.getTotal();
			Vector v = lr.getResult();
			json.put("total", total);
			result.put("count", pageSize);
			Iterator ir = null;
			if (v != null)
				ir = v.iterator();
			JSONArray orders = new JSONArray();
			SelectOptionDb sod = new SelectOptionDb();
			while (ir != null && ir.hasNext()) {
				fdao = (FormDAO) ir.next();
				JSONObject orderObj = new JSONObject();
				long id = fdao.getId();
				orderObj.put("id", fdao.getId());
				orderObj.put("orderTitle", fdao.getFieldValue("orderTitle"));
				String totalPrice = fdao.getFieldValue("totalPrice");
				double totalPri = 0;
				if (totalPrice != null && !totalPrice.trim().equals("")) {
					totalPri = Double.parseDouble(totalPrice);
				}
				orderObj.put("totalPrice", totalPri);
				String orderStatusVal = fdao.getFieldValue("status");
				String orderStatusText = sod.getOptionName("sales_order_state",
						orderStatusVal);
				JSONObject corderStatusSelect = new JSONObject();
				corderStatusSelect.put("name", orderStatusText);
				corderStatusSelect.put("value", orderStatusVal);
				orderObj.put("orderStatusSelect", corderStatusSelect);
				StringBuilder sqlRealPay = new StringBuilder();
				sqlRealPay
						.append(
								"select sum(hkje) from form_table_sales_ord_huikuan where cws_id = ")
						.append(id);
				orderObj.put("realPay", SalesModuleDao
						.getCountInfoById(sqlRealPay.toString()));
				orders.put(orderObj);
			}
			result.put("orders", orders);
			json.put("result", result);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(SalesOrdersListAction.class).error(
					"JSONException:" + e.getMessage());
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(SalesOrdersListAction.class).error(
					"ErrMsgException:" + e.getMessage());
		}
		setResult(json.toString());
		return "SUCCESS";
	}

	/**
	 * 根据 订单下的产品金额 统计 订单总金额 根据还款 统计 实付金额
	 * 
	 * @param tableName
	 *            表名
	 * @param cws_id
	 *            外键
	 * @param coloumName
	 *            列名
	 * @return
	 */
	public double sumPriceByOrders(String formCode, long cws_id,
			String coloumName) {
		double sum = 0;
		StringBuilder sqlSb = new StringBuilder();
		sqlSb.append("SELECT SUM(").append(coloumName).append(")").append(
				" FROM form_table_").append(formCode)
				.append(" WHERE cws_id = ").append(cws_id);
		JdbcTemplate jt = null;
		try {
			jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(sqlSb.toString());
			while (ri.hasNext()) {
				ResultRecord record = (ResultRecord) ri.next();
				sum = record.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(SalesContractListAction.class).error(
					"ReakSumByContact SQLException:" + e.getMessage());
		}
		return sum;
	}

}
