package com.redmoon.oa.android;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.fileark.Attachment;
import com.redmoon.oa.fileark.DocPriv;
import com.redmoon.oa.fileark.Document;
import com.redmoon.oa.fileark.LeafPriv;
import com.redmoon.oa.android.*;

public class FilecaseDetailAction {
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
		HttpServletRequest request = ServletActionContext.getRequest();
		JSONObject json = new JSONObject();
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(getSkey());
		if(re){
			privilege.doLogin(request, skey);
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
			Document doc = new Document();
			doc = doc.getDocument(getId());
            if(doc!=null){
				json.put("res","0");
				json.put("msg","操作成功");
				json.put("id",String.valueOf(getId()));
				json.put("title",doc.getTitle());
				json.put("createdate",StrUtil.getNullStr(DateUtil.format(doc.getCreateDate(),"yyyy-MM-dd HH:mm")));
				// json.put("content",privilege.delHTMLTag(StrUtil.getAbstract(request, doc.getContent(1), 50000, "\r\n")));
				json.put("content", StrUtil.getAbstract(request, doc.getContent(1), 50000, "\r\n"));
				
				json.put("canComment", "" + doc.isCanComment());
				
				LeafPriv lp = new LeafPriv();
				lp.setDirCode(doc.getDirCode());
				DocPriv dp = new DocPriv();

				boolean canDownload = lp.canUserDownLoad(privilege.getUserName(skey)) && dp.canUserDownload(request, id);
				
				// 文件附件
				JSONArray files = new JSONArray();
				if (canDownload) {
					String downPath = "";
					Vector attachments = doc.getAttachments(1);
					Iterator ri = attachments.iterator();
					while (ri.hasNext()) {
					  	Attachment am = (Attachment) ri.next();
						JSONObject file = new JSONObject();
						file.put("name",am.getName());
						downPath = "public/android/doc_getfile.jsp?"+"id="+am.getDocId()+"&attachId="+am.getId();
						file.put("url",downPath);
						file.put("size",String.valueOf(am.getSize()));
						files.put(file);
					}			
					json.put("files", files);
				}
          }else{
        	  json.put("res","-1");
			  json.put("msg","文档不存在");
          }
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 			
		setResult(json.toString());
		return "SUCCESS";
	}
}
