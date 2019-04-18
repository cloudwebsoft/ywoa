package com.redmoon.oa.android.sales;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.visual.FormDAO;
public class SalesContractPayListAction {
	private final static String SALES_CONTRACT_PAY = "sales_contract_pay";
	private int pageSize = 10;
	private String skey = "";
	private String result = "";
	private long contractId = 0;
	
	public long getContractId() {
		return contractId;
	}
	public void setContractId(long contractId) {
		this.contractId = contractId;
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
	@SuppressWarnings("finally")
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
			sqlSb.append("select id from form_table_sales_contract_pay where 1 = 1 ");
			if(contractId != 0){
				sqlSb.append("  and cws_id =").append(contractId);
			}
			
			FormDAO fdao = new FormDAO();
			json.put("res", "0");
			json.put("msg", "操作成功");
			JSONObject result = new JSONObject();	
			if(pagenum <= 0){
				pagenum = 1;
			}
			ListResult lr = fdao.listResult(SALES_CONTRACT_PAY, sqlSb.toString(), pagenum, pageSize);
			int total = lr.getTotal();
			Vector v = lr.getResult();
			json.put("total", total);
			result.put("count", pageSize);
		    Iterator ir = null;
			if (v!=null)
				ir = v.iterator();
			JSONArray contractPays = new JSONArray();
			while (ir!=null && ir.hasNext()) {
				fdao = (FormDAO)ir.next();
				JSONObject payObj = new JSONObject();
				payObj.put("id",fdao.getId());
				payObj.put("pay_date",fdao.getFieldValue("pay_date"));//计划付款日期
				payObj.put("real_pay_date",fdao.getFieldValue("real_pay_date"));//实际付款日期
				payObj.put("sum", fdao.getFieldValue("sum"));//应收
				payObj.put("real_sum",fdao.getFieldValue("real_sum"));//实收
				payObj.put("isPay",fdao.getFieldValue("isPay"));//是否支付
				contractPays.put(payObj);
			}	
			result.put("contractPays", contractPays);
			json.put("result", result);
		} catch (JSONException e) {
			Logger.getLogger(SalesContractPayListAction.class).error("json error:"+e.getMessage());
			json.put("res", "-1");
			json.put("msg", "JSON解析错误");
		} catch (ErrMsgException e) {
			Logger.getLogger(SalesContractPayListAction.class).error("ErrMsgException error:"+e.getMessage());
			json.put("res", "-1");
			json.put("msg",e.getMessage());
		}finally{
			setResult(json.toString());
			return "SUCCESS";
		}	
		
	}
}
