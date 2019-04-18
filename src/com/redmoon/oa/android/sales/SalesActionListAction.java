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

/**
 * 销售行动
 * 
 * @author Administrator
 * 
 */
public class SalesActionListAction {
	private final static String FORM_CODE = "day_lxr";
	private final static String SALES_LINKMAN = "sales_linkman";
	private final static String SALES_CUSTOMER = "sales_customer";
	private int pageSize = 10;
	private String skey = "";
	private String result = "";
	private int pagenum;

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
					&& !priv.isUserPrivValid(request, "sales.manager")){
				json.put("res", "-1");
				json.put("msg", "权限非法");
				setResult(json.toString());
				return "SUCCESS";
			}
			StringBuilder sqlSb = new StringBuilder();
			sqlSb
			.append(" select a.id from form_table_sales_linkman l, form_table_sales_customer b,form_table_day_lxr a where l.customer=b.id and l.id = a.lxr");
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
			sqlSb.append(" order by id desc");
			if (pagenum <= 0) {
				pagenum = 1;
			}
			FormDAO fdao = new FormDAO();
			json.put("res", "0");
			json.put("msg", "操作成功");
			JSONObject result = new JSONObject();
		//	Vector v = fdao.list(FORM_CODE, sqlSb.toString());
			ListResult lr = fdao.listResult(FORM_CODE, sqlSb.toString(),
				pagenum, pageSize);
			Vector v = lr.getResult();
			int total = lr.getTotal();
			json.put("total", total);
			result.put("count", pageSize);
			Iterator ir = null;
			if (v != null)
				ir = v.iterator();
			JSONArray salesActionArr = new JSONArray();
			FormDb linkMansFdb = new FormDb(SALES_LINKMAN);
			FormDAO linkManFdao = new FormDAO();
			FormDb customerfd = new FormDb(SALES_CUSTOMER);// FormDb 关联模块
			FormDAO customerDao = new FormDAO();
			while (ir != null && ir.hasNext()) {
				fdao = (FormDAO) ir.next();
				JSONObject salesActionObj = new JSONObject();
				salesActionObj.put("id", fdao.getId());
				JSONObject linkManObj = new JSONObject();
				String linkManId = fdao.getFieldValue("lxr");
				linkManFdao = linkManFdao.getFormDAO(StrUtil.toLong(linkManId),
						linkMansFdb);
				linkManObj.put("id", linkManFdao.getId());
				linkManObj
						.put("name", linkManFdao.getFieldValue("linkmanName"));
				long customerId = Long.parseLong(linkManFdao
						.getFieldValue("customer"));
				customerDao = customerDao.getFormDAO(customerId, customerfd);
				JSONObject customerObj = new JSONObject();
				customerObj.put("id", customerDao.getId());
				customerObj.put("name", customerDao.getFieldValue("customer"));
				salesActionObj.put("customer", customerObj);
				salesActionObj.put("contact", linkManObj);
				String contactResult = fdao.getFieldValue("contact_result");
				salesActionObj.put("contactResult", contactResult);
				UserDb userDb = new UserDb(customerDao.getFieldValue("sales_person"));
				JSONObject user = new JSONObject();
				user.put("userName", customerDao.getFieldValue("sales_person"));
				user.put("realName", userDb.getRealName());
				salesActionObj.put("visitUser", user);
				String visitDate = fdao.getFieldValue("visit_date");
				salesActionObj.put("visitDate", visitDate);
				String location = fdao.getFieldValue("location");
				if (location != null && !location.trim().equals("")) {
					String[] locationArr = location.split(",");
					if (locationArr != null && locationArr.length == 3) {
						salesActionObj.put("latitude", locationArr[1]);
						salesActionObj.put("lontitude", locationArr[0]);
						salesActionObj.put("address", locationArr[2]);
					}
				}
				salesActionArr.put(salesActionObj);

			}
		
			result.put("saleActions", salesActionArr);
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
