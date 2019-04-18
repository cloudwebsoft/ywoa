package com.redmoon.oa.android;

import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.oa.person.*;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.ui.*;
import com.redmoon.oa.kernel.*;

public class FlowListAction {
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
/*
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
		HttpServletRequest request = ServletActionContext.getRequest();

		Directory dir = new Directory();
		Leaf rootlf = dir.getLeaf(Leaf.CODE_ROOT);
		DirectoryView dv = new DirectoryView(rootlf);
		Vector children;
		try {
			json.put("res", "0");
			json.put("msg", "操作成功");
			json.put("root", rootlf.getName());
			children = dir.getChildren(Leaf.CODE_ROOT);  
			Iterator ri = children.iterator();  
			JSONObject result = new JSONObject(); 
			JSONArray parentNames = new JSONArray(); 
			while (ri.hasNext()) {
				Leaf childlf = (Leaf) ri.next();

				JSONObject parentName = new JSONObject(); 
				parentName.put("parentName", childlf.getName());
				if(childlf.getType()!=0){
					parentName.put("parentCode", childlf.getCode());
					parentName.put("parentType", childlf.getType());
				}
				parentNames.put(parentName);
				JSONArray childNames  = new JSONArray(); 

				if (dv.canUserSeeWhenInitFlow(request, childlf)) {
					Iterator ir = dir.getChildren(childlf.getCode()).iterator();
					while (ir.hasNext()) {
						Leaf chlf = (Leaf) ir.next();
						if (dv.canUserSeeWhenInitFlow(request, chlf)) {
							JSONObject childName = new JSONObject();
							childName.put("code", chlf.getCode());
							childName.put("name", chlf.getName());
							childName.put("type", chlf.getType());
							childNames.put(childName);
						}
					}
				}
				parentName.put("childNames",childNames);
			}
			result.put("parentNames",parentNames);		
			json.put("result",result);
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
	*/
	
	
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
		HttpServletRequest request = ServletActionContext.getRequest();

		privilege.doLogin(request, getSkey());
		
		Directory dir = new Directory();
		Leaf rootlf = dir.getLeaf(Leaf.CODE_ROOT);
		DirectoryView dv = new DirectoryView(rootlf);
		Vector children;
		try {
			json.put("res", "0");
			json.put("msg", "操作成功");
			json.put("root", rootlf.getName());
			children = dir.getChildren(Leaf.CODE_ROOT);
			Iterator ri = children.iterator();
			JSONObject result = new JSONObject(); 
			JSONArray parentNames  = new JSONArray(); 
			while (ri.hasNext()) {
				Leaf childlf = (Leaf) ri.next();

				JSONArray childNames  = new JSONArray(); 

				if (childlf.isOpen() && dv.canUserSeeWhenInitFlow(request, childlf)) {
					Iterator ir = dir.getChildren(childlf.getCode()).iterator();
					while (ir.hasNext()) {
						Leaf chlf = (Leaf) ir.next();
						if (chlf.isOpen() && chlf.isMobileStart() && dv.canUserSeeWhenInitFlow(request, chlf)) {
							JSONObject parentName = new JSONObject();							
							parentName.put("parentName", chlf.getName());
							if(chlf.getType()!=Leaf.TYPE_NONE){
								parentName.put("parentCode", chlf.getCode());
								parentName.put("parentType", chlf.getType());
							}
							parentName.put("childNames",childNames);							
							parentNames.put(parentName);
						}
					}
				}
			}
			result.put("parentNames",parentNames);		
			json.put("result",result);
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
