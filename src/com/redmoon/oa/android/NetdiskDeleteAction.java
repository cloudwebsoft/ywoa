package com.redmoon.oa.android;

import org.json.JSONException;
import org.json.JSONObject;

import com.redmoon.oa.netdisk.Attachment;
import com.redmoon.oa.netdisk.DocContent;
import com.redmoon.oa.netdisk.Document;
import com.redmoon.oa.netdisk.LeafPriv;

public class NetdiskDeleteAction {
	private String skey = "";
	private String result = "";
	private int doc_id;
	private int attach_id;
	private int page_num;
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
	public int getDoc_id() {
		return doc_id;
	}
	public void setDoc_id(int docId) {
		doc_id = docId;
	}
	public int getAttach_id() {
		return attach_id;
	}
	public void setAttach_id(int attachId) {
		attach_id = attachId;
	}
	public int getPage_num() {
		return page_num;
	}
	public void setPage_num(int pageNum) {
		page_num = pageNum;
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
		    Document doc = new Document();
	        doc = doc.getDocument(getDoc_id());
	        LeafPriv lp = new LeafPriv(doc.getDirCode());
	        if (!lp.canUserDel(privilege.getUserName(getSkey()))) {
	        	try {
					json.put("res","-1");
					json.put("msg","权限非法");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
	        }
	        Attachment att = new Attachment(getAttach_id());
	        re = att.delAttLogic();
	        if(re){
	        	try {
					json.put("res","0");
					json.put("msg","操作成功");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
	        }
		setResult(json.toString());
		return "SUCCESS";
	}		
}
