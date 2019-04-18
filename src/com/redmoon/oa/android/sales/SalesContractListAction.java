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

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.visual.FormDAO;

public class SalesContractListAction {
	private final static String SALES_CONTRACT = "sales_contract";
	private int pageSize = 10;
	private String skey = "";
	private String result = "";
	private long customerId = 0;
	private boolean  lower = false;
	public boolean isLower() {
		return lower;
	}
	public void setLower(boolean lower) {
		this.lower = lower;
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
		if (re) {
			try {
				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
		StringBuilder sqlSb = new StringBuilder();
		sqlSb.append("select id from form_table_sales_contract where 1 = 1 ");
		FormDAO fdao = new FormDAO();
		try {
			json.put("res", "0");
			json.put("msg", "操作成功");
			JSONObject result = new JSONObject();	
			if(pagenum <= 0){
				pagenum = 1;
			}
			if (customerId != 0) {
				sqlSb.append(" and customer=").append(customerId);
			}else{
				if(!lower){
					sqlSb.append(" and seller_name =").append("'").append(privilege.getUserName(skey)).append("'");
					//sqlSb.append(" and customer in (").append("select id from form_table_sales_customer where sales_person = ").append(StrUtil.sqlstr(privilege.getUserName(skey))).append(")");
				}
			}
			ListResult lr = fdao.listResult(SALES_CONTRACT, sqlSb.toString(), pagenum, pageSize);
			int total = lr.getTotal();
			Vector v = lr.getResult();
			json.put("total", total);
			result.put("count", pageSize);
		    Iterator ir = null;
			if (v!=null)
				ir = v.iterator();
			JSONArray contracts = new JSONArray();
			//SelectOptionDb sod = new SelectOptionDb();
			while (ir!=null && ir.hasNext()) {
				fdao = (FormDAO)ir.next();
				JSONObject contractObj = new JSONObject();
				contractObj.put("id",fdao.getId());
				contractObj.put("name",fdao.getFieldValue("contact_name"));//客户名称
//				String contractTypeText = fdao.getFieldValue("contract_type");
//				String contractTypeVal = sod.getOptionName("contract_type", contractTypeText);
//				JSONObject contractTypeSelect = new JSONObject();
//				contractTypeSelect.put("name", contractTypeText);
//				contractTypeSelect.put("value", contractTypeVal);
//				contractObj.put("contractTypeSelect",contractTypeSelect);
				//实际付款金额总和
				String sql = "SELECT  sum(real_sum)  FROM  form_table_sales_contract_pay where isPay = 1 and cws_id = "+fdao.getId();
				contractObj.put("realPay", SalesModuleDao.getCountInfoById(sql));
				contracts.put(contractObj);
			}	
			result.put("contracts", contracts);
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
