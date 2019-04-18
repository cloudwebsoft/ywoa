package com.redmoon.oa.android;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import com.redmoon.oa.kernel.License;
public class SysUpgradeAction {
	String version = "";
	private String result = "";
	private String client = "";
	
	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	/**
	 * 
	 * @return
	 */
	public String execute() {
		JSONObject json = new JSONObject(); 
		try {
		if(client==""){
			client = "ios";
		}
		String sql = "select id from oa_version where client=? order by id desc";
		SystemUpDb sd = new SystemUpDb();
		Vector v = sd.list(sql,new Object[]{client});
		Iterator ir = v.iterator();
		String ver_sql = "";
		if(ir.hasNext()){
			sd =(SystemUpDb) ir.next();
			ver_sql = sd.getString("version_num");
			if(ver_sql!=null && !ver_sql.equals("")){
				if(SysUpgradeAction.getDoubleVersion(ver_sql) > SysUpgradeAction.getDoubleVersion(version)){
					json.put("res","0");
					json.put("msg",sd.getString("version_name"));
					json.put("version", ver_sql);
					String type = License.getInstance().getType();
					String path = "";
					if(type.equals(License.TYPE_OEM)){
						path = "activex/oa.apk";
					}else{
						path = "activex/yimioa.apk";
					}
					json.put("url",path);
				}else{
					json.put("res","-1");
					json.put("msg","无更新");
				}
			}else{
				json.put("res","-1");
				json.put("msg","无更新");
			}
		
		}else{
			json.put("res","-1");
			json.put("msg","无更新");
		}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setResult(json.toString());
		return "SUCCESS";
	}
	public static double getDoubleVersion(String version) {
		double ver = 0.0;
		String versionRes = "";
		double total = 0;
		try {
			ver = Double.parseDouble(version);
		} catch (java.lang.NumberFormatException e) {
			versionRes = version;
		}finally{
			if(!versionRes.trim().equals("")){
				version = version.replace(".", ",");
				String[] result = version.split(",");
				int len = result.length;
				if (len > 0) {
					total = Double.valueOf(result[0])+Double.valueOf(result[1])*0.1;
					for (int i = 2; i < result.length; i++) {
						double currentVal = Double.valueOf(result[i]);
						double tempValue = currentVal * 0.01;
						BigDecimal b1 = new BigDecimal(Double.toString(tempValue));
						BigDecimal b2 = new BigDecimal(Double.toString(total));
						total = b1.add(b2).doubleValue();
					}
				}
			}else{
				total = ver;
			}
		}
		return total;
	}
}
