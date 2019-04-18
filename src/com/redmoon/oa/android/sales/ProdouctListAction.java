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
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.visual.FormDAO;
/**
 * 产品列表
 * @author Administrator
 *
 */
public class ProdouctListAction {
	private final static String SALES_PRODUCT_INFO = "sales_product_info";
	private int pageSize = 10;
	private String skey = "";
	private String result = "";
	private String name ;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
			sqlSb.append("select id from form_table_").append(SALES_PRODUCT_INFO).append(" where 1 = 1 ");
			if(name != null && !name.trim().equals("")){
				sqlSb.append("  and  product_name like").append("'%").append(name).append("%'");
			}
			FormDAO fdao = new FormDAO();
			json.put("res", "0");
			json.put("msg", "操作成功");
			JSONObject result = new JSONObject();	
			if(pagenum <= 0){
				pagenum = 1;
			}
			ListResult lr = fdao.listResult(SALES_PRODUCT_INFO, sqlSb.toString(), pagenum, pageSize);
			int total = lr.getTotal();
			Vector v = lr.getResult();
			json.put("total", total);
			result.put("count", pageSize);
		    Iterator ir = null;
			if (v!=null)
				ir = v.iterator();
			JSONArray products = new JSONArray();
			while (ir!=null && ir.hasNext()) {
				fdao = (FormDAO)ir.next();
				JSONObject proObj = new JSONObject();
				proObj.put("id",fdao.getId());
				proObj.put("proName",fdao.getFieldValue("product_name"));//产品名称
				proObj.put("standardPrice",fdao.getFieldValue("standard_price"));//销售价格
				proObj.put("unit", fdao.getFieldValue("measure_unit"));//单位
				products.put(proObj);
			}	
			result.put("products", products);
			json.put("result", result);
		} catch (JSONException e) {
			Logger.getLogger(ProdouctListAction.class).error("json error:"+e.getMessage());
		} catch (ErrMsgException e) {
			Logger.getLogger(ProdouctListAction.class).error("ErrMsgException error:"+e.getMessage());
		}
		setResult(json.toString());
		return "SUCCESS";
	}
}
