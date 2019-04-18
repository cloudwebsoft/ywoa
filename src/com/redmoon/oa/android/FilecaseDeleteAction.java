package com.redmoon.oa.android;

import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.ErrMsgException;

import com.redmoon.oa.fileark.Document;
import com.redmoon.oa.fileark.LeafPriv;
import com.redmoon.oa.pvg.Privilege;

public class FilecaseDeleteAction {
	private String result = "";
	private String skey = "";
	private int id;
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getSkey() {
		return skey;
	}
	public void setSkey(String skey) {
		this.skey = skey;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public String execute() {
		JSONObject json = new JSONObject(); 
		
		com.redmoon.oa.android.Privilege privilege = new com.redmoon.oa.android.Privilege();
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
        doc = doc.getDocument(id);
        if (doc==null || !doc.isLoaded()) {
        	try {
				json.put("res","-1");
				json.put("msg","文件不存在");
				setResult(json.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        LeafPriv lp = new LeafPriv(doc.getDirCode());
        if (lp.canUserDel(privilege.getUserName(getSkey()))) {
            if (true) {
                try {
					re = doc.UpdateExamine(Document.EXAMINE_DUSTBIN);
					if(re){
						json.put("res","0");
						json.put("msg","操作成功");
					}
				} catch (ErrMsgException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
            }
            else {        	
            	try {
					re = doc.del();
					if(re){
						json.put("res","0");
						json.put("msg","操作成功");
	            	}
				} catch (ErrMsgException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	      	
            }
        }
        else{
        	try {
				json.put("res","-1");
				json.put("msg",Privilege.MSG_INVALID);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
        }
		setResult(json.toString());
		return "SUCCESS";
	}	
}
