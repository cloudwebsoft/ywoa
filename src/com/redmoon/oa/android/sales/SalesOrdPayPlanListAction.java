package com.redmoon.oa.android.sales;

import java.util.Iterator;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.redmoon.oa.basic.SelectOptionDb;
import cn.js.fan.db.ListResult;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.visual.FormDAO;
/**
 * 订单回款计划
 * @author Administrator
 *
 */
public class SalesOrdPayPlanListAction {
	private final static String SALES_ORD_PAY_PLAN = "sales_ord_pay_plan";
	private int pageSize = 10;
	private String skey = "";
	private String result = "";
	private long orderId = 0;
	
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
		try {
			if (re) {
				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			}	
			StringBuilder sqlSb = new StringBuilder();
			sqlSb.append("select id from form_table_").append(SALES_ORD_PAY_PLAN).append(" where 1 = 1 ");
			if(orderId != 0){
				sqlSb.append("  and cws_id =").append(orderId);
			}
			FormDAO fdao = new FormDAO();
			json.put("res", "0");
			json.put("msg", "操作成功");
			JSONObject result = new JSONObject();	
			if(pagenum <= 0){
				pagenum = 1;
			}
			ListResult lr = fdao.listResult(SALES_ORD_PAY_PLAN, sqlSb.toString(), pagenum, pageSize);
			int total = lr.getTotal();
			Vector v = lr.getResult();
			json.put("total", total);
			result.put("count", pageSize);
		    Iterator ir = null;
			if (v!=null)
				ir = v.iterator();
			JSONArray ordPayPlans = new JSONArray();
			SelectOptionDb sod = new SelectOptionDb();
			while (ir!=null && ir.hasNext()) {
				fdao = (FormDAO)ir.next();
				JSONObject planObj = new JSONObject();
				planObj.put("id",fdao.getId());
				planObj.put("receivePrice",fdao.getFieldValue("jhhkje"));//应收金额
				planObj.put("planPayDate",fdao.getFieldValue("jhhkrq"));//计划还款日期
				String stageValue = fdao.getFieldValue("qici");
				String stageText = sod.getOptionName("sales_time",
						stageValue);
				JSONObject  stageSelect = new JSONObject();
				stageSelect.put("name", stageText);
				stageSelect.put("value", stageValue);
				planObj.put("stageSelect",stageSelect);//支付类别
				//是否回款
				String whetherPaidStr = fdao.getFieldValue("is_hk");
				boolean whetherPaid = false;
				if(whetherPaidStr!=null && !whetherPaidStr.trim().equals("")){
					if(whetherPaidStr.equals("是")){
						whetherPaid = true;
					}
				}
				planObj.put("whetherPaid",whetherPaid);//支付类别
				ordPayPlans.put(planObj);
			}	
			result.put("ordPayPlans", ordPayPlans);
			json.put("result", result);
		} catch (JSONException e) {
			Logger.getLogger(SalesOrdPayPlanListAction.class).error("json error:"+e.getMessage());
		} catch (ErrMsgException e) {
			Logger.getLogger(SalesOrdPayPlanListAction.class).error("ErrMsgException error:"+e.getMessage());
		}
		setResult(json.toString());
		return "SUCCESS";
	}
}
