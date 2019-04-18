package com.redmoon.oa.android;

import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.redmoon.oa.netdisk.Leaf;


public class NetdiskDirCodeAction {
	private String skey = "";
	private String result = "";
	private String dircode = "";
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
	public String getDircode() {
		return dircode;
	}
	public void setDircode(String dircode) {
		this.dircode = dircode;
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
		
		if(getDircode().equals("")){		
			setDircode(privilege.getUserName(getSkey()));
		}
		
		try {
			json.put("res","0");
			json.put("msg","操作成功");
			json.put("dircode",getDircode());
			
			JSONArray childrens  = new JSONArray(); 	
			
			Leaf lf = new Leaf();
			lf = lf.getLeaf(getDircode());
			if(lf!=null){
				Vector vector = lf.getChildren();
				Iterator ri = vector.iterator();
				while(ri.hasNext()){
					Leaf lf_c = (Leaf)ri.next();
					JSONObject children = new JSONObject();
					children.put("dircode",lf_c.getCode());
					children.put("name",lf_c.getName());
					childrens.put(children);
				}	
			}
			json.put("childrens",childrens);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		setResult(json.toString());
		return "SUCCESS";
	}
}
