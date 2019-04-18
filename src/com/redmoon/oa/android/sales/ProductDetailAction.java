package com.redmoon.oa.android.sales;

import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.ErrMsgException;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.FormDAO;

public class ProductDetailAction {
	private final static String SALES_PRODUCT_INFO = "sales_product_info";
	private long proId = 0;
	private String skey = "";
	private String result = "";
	private long customerId = 0;

	public long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}

	public long getProId() {
		return proId;
	}

	public void setProId(long proId) {
		this.proId = proId;
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
			if (proId != 0) {
				FormDb formDb = new FormDb(SALES_PRODUCT_INFO);
				com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(
						formDb);
				com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(proId);
				Iterator itFiles = fdao.getAttachments().iterator();
				JSONArray filesArr = new JSONArray();
				while (itFiles.hasNext()) {
					JSONObject fileObj = new JSONObject();
					com.redmoon.oa.visual.Attachment am = (com.redmoon.oa.visual.Attachment) itFiles
							.next();
					String name = am.getName();
					String downUrl = "/public/visual/visual_getfile.jsp";
					int attId = am.getId();
					fileObj.put("name", name);
					fileObj.put("downloadUrl", downUrl);
					fileObj.put("id", attId);
					filesArr.put(fileObj);
				}
				json.put("files", filesArr);
				StringBuilder sqlSb = new StringBuilder();
				sqlSb.append("select id from form_table_").append(
						SALES_PRODUCT_INFO).append(" where id =  ").append(
						proId);
				FormDAO fdaoPro = new FormDAO();
				try {
					Vector vec = fdaoPro
							.list(SALES_PRODUCT_INFO, sqlSb.toString());
					Iterator ir = vec.iterator();
					SelectOptionDb sod = new SelectOptionDb();
					JSONObject result = new JSONObject();
					JSONObject proObj = new JSONObject();
					while (ir.hasNext()) {
						fdao = (FormDAO) ir.next();
						proObj.put("id", fdao.getId());
						proObj.put("proName", fdao
								.getFieldValue("product_name"));// 产品名称
						proObj.put("standardPrice", fdao
								.getFieldValue("standard_price"));// 销售价格
						proObj.put("unit", fdao.getFieldValue("measure_unit"));// 单位
					}
					result.put("product", proObj);
					StringBuilder attachAccountSb = new StringBuilder();
					attachAccountSb
							.append(
									"select count(id)  from visual_attach where formCode = ")
							.append("'").append(SALES_PRODUCT_INFO).append("'")
							.append(" and visualid = ").append(proId);
					result.put("attachCount", SalesModuleDao
							.getCountInfoById(attachAccountSb.toString()));
					json.put("result", result);
					json.put("res", "0");
					json.put("msg", "操作成功");

				} catch (ErrMsgException e) {
					// TODO Auto-generated catch block
					LogUtil.getLog(ProductDetailAction.class).error(
							"ErrMsgException:" + e.getMessage());
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(ProductDetailAction.class).error(
					"JSONException:" + e.getMessage());
		}
		setResult(json.toString());
		return "SUCCESS";
	}

}
