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

public class SalesContactDetailAction {
	private final static String SALES_CUSTOMER = "sales_customer";
	private final static String DAY_LXR = "day_lxr";
	private final static String SALES_LINKMAN = "sales_linkman";
	private long contactId = 0;
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


	public long getContactId() {
		return contactId;
	}

	public void setContactId(long contactId) {
		this.contactId = contactId;
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
			
			if(contactId != 0){
				StringBuilder sqlSb = new StringBuilder();
				sqlSb.append(" select a.id from form_table_sales_linkman a, form_table_sales_customer b where a.customer=b.id ").append(" and a.id=").append(contactId);
				FormDAO fdao = new FormDAO();
				try {
					Vector vec = fdao.list(SALES_LINKMAN, sqlSb.toString());
					Iterator ir = vec.iterator();
					SelectOptionDb sod = new SelectOptionDb();
					JSONObject result = new JSONObject();	
					JSONObject linkManObj = new JSONObject();
					FormDb customerfd = new FormDb(SALES_CUSTOMER);// FormDb 关联模块
					FormDAO fdaoCustomer = new FormDAO();
					JSONObject customer = new JSONObject();
					while(ir.hasNext()){
						fdao = (FormDAO)ir.next();
						linkManObj.put("id", fdao.getId());
						linkManObj.put("name", fdao.getFieldValue("linkmanName"));// 联系人
						fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toLong(fdao
								.getFieldValue("customer")), customerfd);
						customer.put("id", fdaoCustomer.getId());
						customer.put("name", fdaoCustomer.getFieldValue("customer"));// 客户名称
					}
					result.put("contact",linkManObj);
					StringBuilder attachAccountSb = new StringBuilder();
					attachAccountSb.append("select count(id)  from visual_attach where formCode = ").append("'").append(SALES_LINKMAN).append("'").append(" and visualid = ").append(contactId);
					result.put("attachCount",SalesModuleDao.getCountInfoById(attachAccountSb.toString()));
					//销售行动
					StringBuilder actionSb2 = new StringBuilder();
					actionSb2.append("select d.id from form_table_day_lxr d, form_table_sales_linkman l where d.lxr=l.id and l.id=").append(contactId);
					StringBuilder actionCountSb = new StringBuilder();
					actionCountSb.append("select count(d.id) from form_table_day_lxr d, form_table_sales_linkman l where d.lxr=l.id and l.id=").append(contactId);
					result.put("actionCount",SalesModuleDao.getCountInfoById(actionCountSb.toString()));
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
					while (irAction!=null && irAction.hasNext()) {
						fdao = (FormDAO)irAction.next();
						JSONObject salesActionObj = new JSONObject();
						salesActionObj.put("id",fdao.getId());
						salesActionObj.put("contact",linkManObj);
						String contactResult = fdao.getFieldValue("contact_result");
						salesActionObj.put("contactResult", contactResult);
						String visitDate = fdao.getFieldValue("visit_date");
						salesActionObj.put("visitDate", visitDate);
						String contactType = fdao.getFieldValue("contact_type");
						salesActionObj.put("contactType", contactType);
						salesActionObj.put("customer", customer);
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
						salesActionObj.put("visitDate", visitDate);
						salesActionArr.put(salesActionObj);
					}	
					result.put("actions", salesActionArr);
					json.put("result", result);
					json.put("res", "0");
					json.put("msg", "操作成功");
					
				} catch (ErrMsgException e) {
					// TODO Auto-generated catch block
					LogUtil.getLog(SalesContactDetailAction.class).error("ErrMsgException:"+e.getMessage());
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(SalesContactDetailAction.class).error("JSONException:"+e.getMessage());
		}
		setResult(json.toString());
		return "SUCCESS";
	}
	

}
