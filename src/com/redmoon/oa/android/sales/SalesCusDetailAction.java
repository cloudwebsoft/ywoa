package com.redmoon.oa.android.sales;

import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.visual.FormDAO;

public class SalesCusDetailAction {
	private final static String SALES_CUSTOMER = "sales_customer";
	private final static String DAY_LXR = "day_lxr";
	private final static String SALES_LINKMAN = "sales_linkman";
	private long customerId = 0;
	private String skey = "";
	private String result = "";
	private int pageSize = 15;
	private int pagenum;
	

	public int getPagenum() {
		return pagenum;
	}

	public void setPagenum(int pagenum) {
		this.pagenum = pagenum;
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

	public String execute() {
		JSONObject json = new JSONObject();
		Privilege privilege = new Privilege();
		try {
			boolean re = privilege.Auth(getSkey());
			if (re) {
				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			}
			
			if(customerId != 0){
				StringBuilder sqlSb = new StringBuilder();
				sqlSb.append("select id from form_table_").append(SALES_CUSTOMER).append(" where id =  ").append(customerId);
				FormDAO fdao = new FormDAO();
				try {
					Vector vec = fdao.list(SALES_CUSTOMER, sqlSb.toString());
					Iterator ir = vec.iterator();
					SelectOptionDb sod = new SelectOptionDb();
					JSONObject result = new JSONObject();	
					JSONObject customer = new JSONObject();
					while(ir.hasNext()){
						fdao = (FormDAO)ir.next();
						customer.put("id",fdao.getId());
						customer.put("name",fdao.getFieldValue("customer"));//客户名称
						String coustomerKindValue = fdao.getFieldValue("customer_type");
						String coustomerKindName = sod.getOptionName("sales_customer_type", coustomerKindValue);
						JSONObject customerKindSelect = new JSONObject();
						customerKindSelect.put("name", coustomerKindName);
						customerKindSelect.put("value", coustomerKindValue);
						String userName =fdao.getFieldValue("sales_person");
						UserDb userDb = new UserDb(userName);
						JSONObject user = new JSONObject();
						user.put("userName",userName);
						user.put("realName",userDb.getRealName());
						customer.put("saleUser",user);
						customer.put("customerKindSelect",customerKindSelect);
					}
					result.put("customer",customer);
					StringBuilder contactSb = new StringBuilder();
					contactSb.append(" select count(id) from form_table_sales_linkman where customer = ").append(customerId);
					int contactCount = SalesModuleDao.getCountInfoById(contactSb.toString());
					result.put("contactCount", contactCount);
					StringBuilder chanceSb = new StringBuilder();
					chanceSb.append("select count(id) from form_table_sales_chance where cws_id = ").append(customerId);
					int chanceCount = SalesModuleDao.getCountInfoById(chanceSb.toString());
					result.put("chanceCount", chanceCount);
					StringBuilder orderSb = new StringBuilder();
					orderSb.append("select count(id) from form_table_sales_order where cws_id = ").append(customerId);
					int orderCount = SalesModuleDao.getCountInfoById(orderSb.toString());
					result.put("orderCount", orderCount);
					StringBuilder actionSb = new StringBuilder();
					actionSb.append("select count(d.id) from form_table_day_lxr d, form_table_sales_linkman l where d.lxr=l.id and l.customer=").append(customerId);
					int actionCount = SalesModuleDao.getCountInfoById(actionSb.toString());
					result.put("actionCount", actionCount);
					StringBuilder attachAccountSb = new StringBuilder();
					attachAccountSb.append("select count(id)  from visual_attach where formCode = ").append("'").append(SALES_CUSTOMER).append("'").append(" and visualid = ").append(customerId);
					result.put("attachCount",SalesModuleDao.getCountInfoById(attachAccountSb.toString()));
					StringBuilder proCountSb = new StringBuilder();
					proCountSb.append("select count(id) from form_table_sales_order where cws_id in(select  id from form_table_sales_order where cws_id = ").append(customerId).append(" )");
					result.put("proCount",SalesModuleDao.getCountInfoById(proCountSb.toString()));
					//销售行动
					StringBuilder actionSb2 = new StringBuilder();
					actionSb2.append("select d.id from form_table_day_lxr d, form_table_sales_linkman l where d.lxr=l.id and l.customer=").append(customerId);
					if(pagenum<=0){
						pagenum = 1;
					}
					FormDAO fdaoAction = new FormDAO();
					ListResult lr = fdaoAction.listResult(DAY_LXR, actionSb2.toString(), pagenum, pageSize);
					int total = lr.getTotal();
					Vector v = lr.getResult();
					json.put("total", total);
					result.put("count", pageSize);
				    Iterator irAction = null;
					if (v!=null)
						irAction = v.iterator();
					JSONArray salesActionArr = new JSONArray();
					FormDb linkMansFdb = new FormDb(SALES_LINKMAN);
					FormDAO linkManFdao = new FormDAO();
					FormDb customerfd = new FormDb(SALES_CUSTOMER);// FormDb 关联模块
					FormDAO customerDao = new FormDAO();
					while (irAction!=null && irAction.hasNext()) {
						fdao = (FormDAO)irAction.next();
						JSONObject salesActionObj = new JSONObject();
						salesActionObj.put("id",fdao.getId());
						JSONObject linkManObj = new JSONObject();
						String linkManId = fdao.getFieldValue("lxr");
						linkManFdao = linkManFdao.getFormDAO(StrUtil.toLong(linkManId),linkMansFdb);
						linkManObj.put("id", linkManFdao.getId());
						linkManObj.put("name", linkManFdao.getFieldValue("linkmanName"));
						salesActionObj.put("contact",linkManObj);
						String contactResult = fdao.getFieldValue("contact_result");
						salesActionObj.put("contactResult", contactResult);
						String visitDate = fdao.getFieldValue("visit_date");
						salesActionObj.put("visitDate", visitDate);
						String contactType = fdao.getFieldValue("contact_type");
						salesActionObj.put("contactType", contactType);
						long customerId = Long.parseLong(linkManFdao.getFieldValue("customer"));
						customerDao = customerDao.getFormDAO(customerId,customerfd);
						JSONObject customerObj = new JSONObject();
						customerObj.put("id", customerDao.getId());
						customerObj.put("name", customerDao.getFieldValue("customer"));
						salesActionObj.put("customer",customerObj);
						String location = fdao.getFieldValue("location");
						if(location != null && !location.trim().equals("")){
							String[] locationArr = location.split(",");
							if(locationArr!=null && locationArr.length==3){
								salesActionObj.put("latitude",locationArr[1]);
								salesActionObj.put("lontitude",locationArr[0]);
								salesActionObj.put("address",locationArr[2]);
							}
						}
						UserDb userDb = new UserDb(fdao.getCreator());
						JSONObject user = new JSONObject();
						user.put("userName", fdao.getCreator());
						user.put("realName", userDb.getRealName());
						salesActionObj.put("visitUser", user);
						salesActionArr.put(salesActionObj);
					}	
					result.put("actions", salesActionArr);
					json.put("result", result);
					json.put("res", "0");
					json.put("msg", "操作成功");
					
				} catch (ErrMsgException e) {
					// TODO Auto-generated catch block
					LogUtil.getLog(SalesCusDetailAction.class).error("ErrMsgException:"+e.getMessage());
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(SalesCusDetailAction.class).error("JSONException:"+e.getMessage());
		}
		setResult(json.toString());
		return "SUCCESS";
	}
	

}
