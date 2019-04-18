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
 * 订单回款记录
 * @author Administrator
 *
 */
public class SalesOrdPayRecordListAction {
	private final static String SALES_ORD_HUIKUAN = "sales_ord_huikuan";
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
			sqlSb.append("select id from form_table_").append(SALES_ORD_HUIKUAN).append(" where 1 = 1 ");
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
			ListResult lr = fdao.listResult(SALES_ORD_HUIKUAN, sqlSb.toString(), pagenum, pageSize);
			int total = lr.getTotal();
			Vector v = lr.getResult();
			json.put("total", total);
			result.put("count", pageSize);
		    Iterator ir = null;
			if (v!=null)
				ir = v.iterator();
			JSONArray ordPayRecords = new JSONArray();
			SelectOptionDb sod = new SelectOptionDb();
			while (ir!=null && ir.hasNext()) {
				fdao = (FormDAO)ir.next();
				JSONObject recordObj = new JSONObject();
				recordObj.put("id",fdao.getId());
				recordObj.put("realPay",fdao.getFieldValue("hkje"));//还款金额
				String isFp = fdao.getFieldValue("is_fp");
				boolean isInvoice = false;
				if(isFp!=null && !isFp.trim().equals("")){
					if(isFp.equals("是")){
						isInvoice = true;
					}
				}
				recordObj.put("isInvoice",isInvoice);//是否开发票
				recordObj.put("repaymentDate", fdao.getFieldValue("hkrq"));//还款日期
				String payTypeValue = fdao.getFieldValue("fklx");
				String payTypeText = sod.getOptionName("pay_type",
						payTypeValue);
				JSONObject payTypeSelect = new JSONObject();
				payTypeSelect.put("name", payTypeText);
				payTypeSelect.put("value", payTypeValue);
				recordObj.put("payTypeSelect",payTypeSelect);//支付类别
				ordPayRecords.put(recordObj);
			}	
			result.put("ordPayRecords", ordPayRecords);
			json.put("result", result);
		} catch (JSONException e) {
			Logger.getLogger(SalesOrdPayRecordListAction.class).error("json error:"+e.getMessage());
		} catch (ErrMsgException e) {
			Logger.getLogger(SalesOrdPayRecordListAction.class).error("ErrMsgException error:"+e.getMessage());
		}
		setResult(json.toString());
		return "SUCCESS";
	}
}
