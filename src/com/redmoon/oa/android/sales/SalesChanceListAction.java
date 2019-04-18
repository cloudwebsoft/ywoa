package com.redmoon.oa.android.sales;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.visual.FormDAO;

public class SalesChanceListAction {
	private final static String FORM_CODE = "sales_chance";
	private final static String RELATION_FORM_CODE = "sales_customer";
	private long customerId = 0;
	private int pageSize = 10;
	private String skey = "";
	private String result = "";
	private int pagenum;

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

	public String execute() {
		JSONObject json = new JSONObject();
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(getSkey());
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
			StringBuilder sqlSb = new StringBuilder();
			sqlSb.append("select id from form_table_sales_chance where 1 = 1");
			if (customerId != 0) {
				sqlSb.append(" and cws_id=").append(customerId);
			} else {
				sqlSb.append(" and unit_code = ").append("'").append(
						priv.getUserUnitCode(request)).append("' ");
				if (!priv.isUserPrivValid(request, "admin")
						&& !priv.isUserPrivValid(request, "sales")
						&& !priv.isUserPrivValid(request, "sales.manager")) {
					String salesPerson = SalesModuleDao.getSalesPerson(priv
							.getUser(request));
					if (!salesPerson.equals("")) {
						sqlSb.append(" and provider in ( ").append(salesPerson)
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
						sqlSb.append(" and provider in ( ").append(salesPerson)
								.append(")");
					}
				}
			}
			sqlSb.append(" order by id desc");

			if (pagenum <= 0) {
				pagenum = 1;
			}
			FormDAO fdao = new FormDAO();
			FormDb customerfd = new FormDb(RELATION_FORM_CODE);// FormDb 关联模块
			FormDAO fdaoCustomer = new FormDAO();
			json.put("res", "0");
			json.put("msg", "操作成功");
			JSONObject result = new JSONObject();
			ListResult lr = fdao.listResult(FORM_CODE, sqlSb.toString(),
					pagenum, pageSize);
			int total = lr.getTotal();
			Vector v = lr.getResult();
			json.put("total", total);
			result.put("count", pageSize);
			Iterator ir = null;
			if (v != null)
				ir = v.iterator();
			JSONArray salesChanceArr = new JSONArray();
			SelectOptionDb sod = new SelectOptionDb();
			while (ir != null && ir.hasNext()) {
				fdao = (FormDAO) ir.next();
				JSONObject salesChanceObj = new JSONObject();
				salesChanceObj.put("id", fdao.getId());
				salesChanceObj.put("chanceName", fdao
						.getFieldValue("chanceName"));
				String expectPrice = StrUtil.getNullStr(fdao
						.getFieldValue("expectPrice"));
				if(expectPrice.equals("")){
					salesChanceObj.put("expectPrice","0" );
				}else{
					salesChanceObj.put("expectPrice",expectPrice);
				}
				
				// customer信息 -客户ID 客户名称 销售员
				JSONObject customer = new JSONObject();
				fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toLong(fdao
						.getFieldValue("customer")), customerfd);
				customer.put("id", fdaoCustomer.getId());
				customer.put("name", fdaoCustomer.getFieldValue("customer"));// 客户名称
				String userName = fdaoCustomer.getFieldValue("sales_person");
				UserDb userDb = new UserDb(userName);
				// JSONObject user = new JSONObject();
				// user.put("userName", userName);
				// user.put("realName", userDb.getRealName());
				// customer.put("user", user);
				salesChanceObj.put("customer", customer);
				salesChanceObj.put("preDate", fdao.getFieldValue("pre_date"));
				// 百分比
				// salesChanceObj.put("possibility",
				// fdao.getFieldValue("possibility"));
				// 商机阶段
				String stateVal = fdao.getFieldValue("state");
				String stateText = sod.getOptionName("sales_chance_state",
						stateVal);
				JSONObject stateObj = new JSONObject();
				stateObj.put("name", stateText);
				stateObj.put("value", stateVal);
				salesChanceObj.put("stateSelect", stateObj);
				// 商机状态
				// String statusVal = fdao.getFieldValue("sjzt");
				// String statusText = sod.getOptionName("sales_chance_status",
				// statusVal);
				// JSONObject statusObj = new JSONObject();
				// statusObj.put("name",statusText);
				// statusObj.put("value",statusVal);
				// salesChanceObj.put("statusSelect",statusObj);
				salesChanceArr.put(salesChanceObj);
			}
			result.put("salesChances", salesChanceArr);
			json.put("result", result);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setResult(json.toString());
		return "SUCCESS";
	}
}
