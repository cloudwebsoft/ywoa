package com.redmoon.oa.android;

import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.address.AddressDb;
import com.redmoon.oa.address.AddressTypeDb;
import com.redmoon.oa.address.Leaf;
import com.redmoon.oa.dept.DeptDb;

public class AddressOrganizeAction {
	private String skey = "";
	private String result = "";
	private String dircode = "";
	

	public int getPagenum() {
		return pagenum;
	}

	private int pagenum;
	private int pagesize;
	
	public String getSkey() {
		return skey;
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
	
	public String getDircode() {
		return dircode;
	}
	public void setDircode(String dircode) {
		this.dircode = dircode;
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
		boolean re = privilege.Auth(getSkey());
		if(re){
			try {
				json.put("res","-2");
				json.put("msg","时间过期");
				setResult(json.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			json.put("res", "0");
			json.put("msg", "操作成功");
			
			JSONArray groupTypes = new JSONArray();
			DeptDb dd = new DeptDb();
			dd = dd.getDeptDb("root");
			Iterator ir = dd.getChildren().iterator();
			while (ir.hasNext()) {
			    DeptDb dept = (DeptDb)ir.next();
			  	JSONObject groupType = new JSONObject();
				groupType.put("grouptype",dept.getCode());
				groupType.put("name", dept.getName());
				groupType.put("mode", "show");
				groupType.put("type","-1"); //-1代表组织机构
				groupTypes.put(groupType);
				
			}
			
			json.put("groupTypes", groupTypes);
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    setResult(json.toString());
		return "SUCCESS";
	}
}
