package com.redmoon.oa.android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.ErrMsgException;
import java.util.*;
import com.redmoon.oa.visual.*;

public class CustomersListAction {
	private String skey = "";
	private String result = "";

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
	public int getPagesize() {
		return pagesize;
	}
	public void setPagesize(int pagesize) {
		this.pagesize = pagesize;
	}
	private int pagenum;
	private int pagesize;
	
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
		String sql = "select id from form_table_sales_customer";
		int curpage = getPagenum();
		int pagesize = getPagesize();
		FormDAO fdao = new FormDAO();
		String formCode = "sales_customer";
		try {
			json.put("res", "0");
			json.put("msg", "操作成功");
			JSONObject result = new JSONObject();			
			ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
			int total = lr.getTotal();
			Vector v = lr.getResult();
			json.put("total", String.valueOf(total));
			result.put("count", String.valueOf(pagesize));
		    Iterator ir = null;
			if (v!=null)
				ir = v.iterator();
			JSONArray customers = new JSONArray();
			while (ir!=null && ir.hasNext()) {
				fdao = (FormDAO)ir.next();
				JSONObject customer = new JSONObject();
				customer.put("id",fdao.getId());
				customer.put("name",fdao.getFieldValue("customer"));
				customer.put("tel",fdao.getFieldValue("tel"));
				customers.put(customer);
			}	
			result.put("customers", customers);
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
