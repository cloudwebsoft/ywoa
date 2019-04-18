package com.redmoon.oa.android.sales;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.BrowserTool;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.visual.FormDAO;

/**
 * 联系人Action 1,某个客户下的联系人 2，所有联系人
 * 
 * @author Administrator
 * 
 */
public class LinkmanListAction {
	private final static String FORM_CODE = "sales_linkman";
	private final static String RELATION_FORM_CODE = "sales_customer";
	private int pageSize = 10;
	private String skey = "";
	private String result = "";
	private long customerId = 0;
	// 搜索
	private String name;
	private String action;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
			// 封装 sql语句
			StringBuilder sqlSb = new StringBuilder();
			sqlSb
					.append(" select a.id from form_table_sales_linkman a, form_table_sales_customer b where a.customer=b.id  ");
			if (customerId != 0) {
				sqlSb.append(" and a.customer=").append(customerId);
			} else {
				if (action != null && !action.trim().equals("")) {
					sqlSb.append("  and a.unit_code = ").append("'").append(
							priv.getUserUnitCode(request)).append("' ");
					if (action.equals("manage")) {
						if (!priv.isUserPrivValid(request, "admin")
								&& !priv.isUserPrivValid(request, "sales")
								&& !priv.isUserPrivValid(request,
										"sales.manager")) {
							String salesPerson = SalesModuleDao
									.getSalesPerson(priv.getUser(request));
							if (!salesPerson.equals("")) {
								sqlSb.append(" and b.sales_person in ( ")
										.append(salesPerson).append(")");
							}
						} else if (priv.isUserPrivValid(request,
								"sales.manager")
								&& !priv.isUserPrivValid(request, "admin")
								&& !priv.isUserPrivValid(request, "sales")) {
							DeptUserDb dud = new DeptUserDb(priv
									.getUser(request));
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
								sqlSb.append(" and b.sales_person in ( ")
										.append(salesPerson).append(")");
							}
						}
					}
				} else {
					sqlSb.append(" and  b.sales_person = ").append("'").append(
							privilege.getUserName(skey)).append("'");
				}
			}
			if (name != null && !name.trim().equals("")) {
				sqlSb.append(" and a.linkManName like ").append("'")
						.append("%").append(name).append("%").append("'");
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
			Vector v = lr.getResult();
			int total = lr.getTotal();
			json.put("total", total);
			result.put("count", pageSize);
			Iterator ir = null;
			if (v != null)
				ir = v.iterator();
			JSONArray linkmanArr = new JSONArray();
			while (ir != null && ir.hasNext()) {
				fdao = (FormDAO) ir.next();
				JSONObject linkManObj = new JSONObject();
				linkManObj.put("id", fdao.getId());
				linkManObj.put("name", fdao.getFieldValue("linkmanName"));// 联系人
				// customer信息 -客户ID 客户名称 销售员
				JSONObject customer = new JSONObject();
				fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toLong(fdao
						.getFieldValue("customer")), customerfd);
				customer.put("id", fdaoCustomer.getId());
				customer.put("name", fdaoCustomer.getFieldValue("customer"));// 客户名称
				String userName = fdaoCustomer.getFieldValue("sales_person");
				UserDb userDb = new UserDb(userName);
				JSONObject user = new JSONObject();
				user.put("userName", userName);
				user.put("realName", userDb.getRealName());
				customer.put("saleUser", user);
				linkManObj.put("customer", customer);
				// 部门
				linkManObj.put("dept", fdao.getFieldValue("dept"));
				// 职位
				linkManObj.put("postPriv", fdao.getFieldValue("postPriv"));
				// 工作号码
				linkManObj.put("telNoWork", fdao.getFieldValue("telNoWork"));
				// 家庭号码
				linkManObj.put("telNoHome", fdao.getFieldValue("telNoHome"));
				// 手机号码
				linkManObj.put("mobile", fdao.getFieldValue("mobile"));
				linkmanArr.put(linkManObj);
			}
			result.put("linkmans", linkmanArr);
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
