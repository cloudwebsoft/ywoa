package com.redmoon.oa.android.sales;

import java.util.Iterator;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.visual.FormDAO;

public class SalesChanceDetailAction {
	private final static String SALES_CUSTOMER = "sales_customer";
	private final static String SALES_CHANCE = "sales_chance";
	private long chanceId = 0;
	private String skey = "";
	private String result = "";
	public long getChanceId() {
		return chanceId;
	}

	public void setChanceId(long chanceId) {
		this.chanceId = chanceId;
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
			if (chanceId != 0) {
				StringBuilder sqlSb = new StringBuilder();
				sqlSb.append("select id from form_table_").append(SALES_CHANCE)
						.append(" where id =  ").append(chanceId);

				FormDAO fdao = new FormDAO();
				try {
					Vector vec = fdao.list(SALES_CHANCE, sqlSb.toString());
					Iterator ir = vec.iterator();
					SelectOptionDb sod = new SelectOptionDb();
					JSONObject result = new JSONObject();
					JSONObject salesChanceObj = new JSONObject();
					FormDAO fdaoCustomer = new FormDAO();
					FormDb customerfd = new FormDb(SALES_CUSTOMER);// FormDb
																	// 关联模块
					while (ir.hasNext()) {
						fdao = (FormDAO) ir.next();
						salesChanceObj.put("id", fdao.getId());
						salesChanceObj.put("chanceName", fdao
								.getFieldValue("chanceName"));
						salesChanceObj.put("expectPrice", fdao
								.getFieldValue("expectPrice"));
						JSONObject customer = new JSONObject();
						fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil
								.toLong(fdao.getFieldValue("customer")),
								customerfd);
						customer.put("id", fdaoCustomer.getId());
						customer.put("name", fdaoCustomer
								.getFieldValue("customer"));// 客户名称
						String userName = fdaoCustomer
								.getFieldValue("sales_person");
						UserDb userDb = new UserDb(userName);
						salesChanceObj.put("customer", customer);
						salesChanceObj.put("preDate", fdao
								.getFieldValue("pre_date"));
						String stateVal = fdao.getFieldValue("state");
						String stateText = sod.getOptionName(
								"sales_chance_state", stateVal);
						JSONObject stateObj = new JSONObject();
						stateObj.put("name", stateText);
						stateObj.put("value", stateVal);
						salesChanceObj.put("stateSelect", stateObj);
					}
					StringBuilder orderSb = new StringBuilder();
					orderSb
							.append(
									"select count(id) from form_table_sales_order where chance = ")
							.append(chanceId);
					int orderCount = SalesModuleDao.getCountInfoById(orderSb.toString());
					result.put("orderCount", orderCount);
					StringBuilder productSb = new StringBuilder();
					productSb
							.append(
									"select count(p.id) from form_table_sales_ord_product  p,form_table_sales_order o where o.id = p.cws_id and o.chance=")
							.append(chanceId);
					int productCount = SalesModuleDao.getCountInfoById(productSb.toString());
					result.put("productCount", productCount);
					StringBuilder attachAccountSb = new StringBuilder();
					attachAccountSb.append("select count(id)  from visual_attach where formCode = ").append("'").append(SALES_CHANCE).append("'").append( " and visualid = ").append(chanceId);
					result.put("attachCount",SalesModuleDao.getCountInfoById(attachAccountSb.toString()));
					result.put("chance", salesChanceObj);
					json.put("result", result);
					json.put("res", "0");
					json.put("msg", "操作成功");

				} catch (ErrMsgException e) {
					// TODO Auto-generated catch block
					LogUtil.getLog(SalesChanceDetailAction.class).error(
							"ErrMsgException:" + e.getMessage());
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(SalesChanceDetailAction.class).error(
					"JSONException:" + e.getMessage());
		}
		setResult(json.toString());
		return "SUCCESS";
	}



}
