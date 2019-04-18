package com.redmoon.oa.android.sales;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.basic.SelectMgr;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.visual.FormDAO;

public class SalesOrderDetailAction {
	private final static String SALES_CUSTOMER = "sales_customer";
	private final static String SALES_ORDER = "sales_order";
	private long orderId = 0;
	private String skey = "";
	private String result = "";
	private long customerId = 0;

	public long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}

	public long getOrderId() {
		return orderId;
	}

	public void setOrderId(long orderId) {
		this.orderId = orderId;
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
			if (orderId != 0) {
				StringBuilder sqlSb = new StringBuilder();
				sqlSb.append("select id from form_table_").append(SALES_ORDER)
						.append(" where id =  ").append(orderId);

				FormDAO fdao = new FormDAO();
				try {
					Vector vec = fdao.list(SALES_ORDER, sqlSb.toString());
					Iterator ir = vec.iterator();
					SelectOptionDb sod = new SelectOptionDb();
					JSONObject result = new JSONObject();
					JSONObject orderObj = new JSONObject();
					while (ir.hasNext()) {
						fdao = (FormDAO) ir.next();
						long id = fdao.getId();
						orderObj.put("id", id);
						orderObj.put("orderTitle", fdao.getFieldValue("orderTitle"));
						String totalPrice = fdao.getFieldValue("totalPrice");
						double totalPri = 0;
						if(totalPrice != null && !totalPrice.trim().equals("")){
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
						sqlRealPay.append("select sum(hkje) from form_table_sales_ord_huikuan where cws_id = ").append(id);
						orderObj.put("realPay",SalesModuleDao.getCountInfoById(sqlRealPay.toString()));
					}
					result.put("order", orderObj);
					StringBuilder productCountSb = new StringBuilder();
					productCountSb.append("select count(id) from form_table_sales_ord_product where cws_id = ").append(orderId);
					result.put("productCount",SalesModuleDao.getCountInfoById(productCountSb.toString()));
					StringBuilder payRecordCountSb = new StringBuilder();
					payRecordCountSb.append("select count(id) from form_table_sales_ord_huikuan where cws_id = ").append(orderId);
					result.put("payRecordCount",SalesModuleDao.getCountInfoById(payRecordCountSb.toString()));
					StringBuilder attachAccountSb = new StringBuilder();
					attachAccountSb.append("select count(id)  from visual_attach where formCode = ").append("'").append(SALES_ORDER).append("'").append(" and visualid = ").append(orderId);
					result.put("attachCount",SalesModuleDao.getCountInfoById(attachAccountSb.toString()));
					json.put("result", result);
					json.put("res", "0");
					json.put("msg", "操作成功");

				} catch (ErrMsgException e) {
					// TODO Auto-generated catch block
					LogUtil.getLog(SalesOrderDetailAction.class).error(
							"ErrMsgException:" + e.getMessage());
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(SalesOrderDetailAction.class).error(
					"JSONException:" + e.getMessage());
		}
		setResult(json.toString());
		return "SUCCESS";
	}


}
