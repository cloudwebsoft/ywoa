package com.redmoon.oa.android.sales;

import java.util.Iterator;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.flow.FormDb;

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
public class SalesOrderProdouctListAction {
	private final static String SALES_ORD_PRODUCT = "sales_ord_product";
	private final static String SALES_PRODUCT_INFO = "sales_product_info";
	private int pageSize = 10;
	private String skey = "";
	private String result = "";
	private long orderId = 0;
	private long chanceId = 0;
	private long customerId = 0;
	
	
	public long getCustomerId() {
		return customerId;
	}
	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}
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
	public long getOrderId() {
		return orderId;
	}
	public void setOrderId(long orderId) {
		this.orderId = orderId;
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
			sqlSb.append("select p.id from form_table_sales_order o,form_table_sales_ord_product p where  o.id = p.cws_id  ");
			if(orderId != 0){
				sqlSb.append(" and o.id = ").append(orderId);
			}
			if(chanceId != 0){
				sqlSb.append(" and o.chance =").append(chanceId);
			}
			if(customerId != 0){
				sqlSb.append(" and o.customer =").append(customerId);
			}
			FormDAO fdao = new FormDAO();
			json.put("res", "0");
			json.put("msg", "操作成功");
			JSONObject result = new JSONObject();	
			if(pagenum <= 0){
				pagenum = 1;
			}
			ListResult lr = fdao.listResult(SALES_ORD_PRODUCT, sqlSb.toString(), pagenum, pageSize);
			int total = lr.getTotal();
			Vector v = lr.getResult();
			json.put("total", total);
			result.put("count", pageSize);
		    Iterator ir = null;
			if (v!=null)
				ir = v.iterator();
			JSONArray orderProducts = new JSONArray();
			FormDAO fdaoPro = new FormDAO();
			FormDb productFd = new FormDb(SALES_PRODUCT_INFO);// FormDb 关联模块
			while (ir!=null && ir.hasNext()) {
				fdao = (FormDAO)ir.next();
				JSONObject orderProductsObj = new JSONObject();
				orderProductsObj.put("id",fdao.getId());
				fdaoPro = fdaoPro.getFormDAO(StrUtil.toLong(fdao
						.getFieldValue("product")), productFd);
				JSONObject proObj = new JSONObject();
				proObj.put("id",fdaoPro.getId());//产品名称
				proObj.put("proName",fdaoPro.getFieldValue("product_name"));//产品名称
				proObj.put("standardPrice",fdaoPro.getFieldValue("standard_price"));//销售价格
				proObj.put("unit", fdaoPro.getFieldValue("measure_unit"));//单位
				orderProductsObj.put("product", proObj);
				orderProductsObj.put("num",fdao.getFieldValue("num"));//数量
				orderProductsObj.put("price",fdao.getFieldValue("price"));//单价
				orderProductsObj.put("totalPrice", fdao.getFieldValue("zj"));//总价
				orderProductsObj.put("realSum", fdao.getFieldValue("real_sum"));//实际销售额
				
				orderProducts.put(orderProductsObj);
			}	
			result.put("orderProducts", orderProducts);
			json.put("result", result);
		} catch (JSONException e) {
			Logger.getLogger(SalesOrderProdouctListAction.class).error("json error:"+e.getMessage());
		} catch (ErrMsgException e) {
			Logger.getLogger(SalesOrderProdouctListAction.class).error("ErrMsgException error:"+e.getMessage());
		}
		setResult(json.toString());
		return "SUCCESS";
	}
}
