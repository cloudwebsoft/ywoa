package com.redmoon.oa.android.sales;

import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.visual.*;

public class CustomersListAction {
	private final static String SALES_CUSTOMER = "sales_customer";
	private int pageSize = 15;
	private String skey = "";
	private String result = "";
	private String name = "";
	private String kind = "";// 客户分级
	private String state = "";// 客户状态
	private String action;
	private String category = "";

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
			sqlSb.append("select id from form_table_").append(SALES_CUSTOMER)
					.append(" where 1 = 1 ");
			if (action != null && !action.trim().equals("")) {
				if (action.equals("manage")) {
					sqlSb.append(" and unit_code = ").append("'").append(
							priv.getUserUnitCode(request)).append("' ");
					if (!priv.isUserPrivValid(request, "admin")
							&& !priv.isUserPrivValid(request, "sales")
							&& !priv.isUserPrivValid(request, "sales.manager")) {
						String salesPerson = SalesModuleDao.getSalesPerson(priv
								.getUser(request));
						if (!salesPerson.equals("")) {
							sqlSb.append(" and sales_person in ( ").append(
									salesPerson).append(")");
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
								salesPerson += ","
										+ StrUtil.sqlstr(ud.getName());
							}
						}
						if (!salesPerson.equals("")) {
							sqlSb.append(" and sales_person in ( ").append(
									salesPerson).append(")");
						}
					}
				}

			} else {
				sqlSb.append(" and sales_person = ").append(
						StrUtil.sqlstr(privilege.getUserName(skey)));
			}
			if (name != null && !name.trim().equals("")) {
				sqlSb.append(" and  customer like '%").append(name)
						.append("%'");
			}
			if (kind != null && !kind.trim().equals("")) {// 客户分级
				sqlSb.append(" and customer_type in (").append(kind)
						.append(")");
			}
			if (state != null && !state.trim().equals("")) {// 客户状态
				sqlSb.append(" and kind in (").append(state).append(")");
			}
			if (category != null && !category.trim().equals("")) {
				sqlSb.append(" and category in (").append(category).append(")");
			}

			sqlSb.append(" order by id desc");
			if (pagenum <= 0) {
				pagenum = 1;
			}
			FormDAO fdao = new FormDAO();
			json.put("res", "0");
			json.put("msg", "操作成功");
			JSONObject result = new JSONObject();
			ListResult lr = fdao.listResult(SALES_CUSTOMER, sqlSb.toString(),
					pagenum, pageSize);
			int total = lr.getTotal();
			Vector v = lr.getResult();
			json.put("total", total);
			result.put("count", pageSize);
			Iterator ir = null;
			if (v != null)
				ir = v.iterator();
			JSONArray customers = new JSONArray();
			SelectOptionDb sod = new SelectOptionDb();
			while (ir != null && ir.hasNext()) {
				fdao = (FormDAO) ir.next();
				JSONObject customer = new JSONObject();
				customer.put("id", fdao.getId());
				customer.put("name", fdao.getFieldValue("customer"));// 客户名称
				String coustomerKindValue = fdao.getFieldValue("customer_type");
				String coustomerKindName = sod.getOptionName(
						"sales_customer_type", coustomerKindValue);
				JSONObject customerKindSelect = new JSONObject();
				customerKindSelect.put("name", coustomerKindName);
				customerKindSelect.put("value", coustomerKindValue);
				customer.put("customerKindSelect", customerKindSelect);
				String stateValue = fdao.getFieldValue("kind");
				String stateName = sod.getOptionName("khlb", stateValue);
				JSONObject stateSelect = new JSONObject();
				stateSelect.put("name", stateName);
				stateSelect.put("value", stateValue);
				customer.put("customerStateSelect", stateSelect);
				String userName = fdao.getFieldValue("sales_person");
				UserDb userDb = new UserDb(userName);
				JSONObject user = new JSONObject();
				user.put("userName", userName);
				user.put("realName", userDb.getRealName());
				customer.put("saleUser", user);
				customers.put(customer);
			}
			result.put("customers", customers);
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
