package com.redmoon.oa.android;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.opensymphony.xwork2.ActionSupport;
import com.redmoon.oa.notice.NoticeAttachmentDb;
import com.redmoon.oa.notice.NoticeDb;
import com.redmoon.oa.notice.NoticeReplyDb;
import com.redmoon.oa.notice.NoticeReplyMgr;

public class NoticeReadFlagAction extends ActionSupport {
	private String result = "";
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	private String skey = "";
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
	private int id;
	
	
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
		
		String name = privilege.getUserName(getSkey());		
		NoticeDb nd = new NoticeDb();
		nd = nd.getNoticeDb(getId());
		String usersKnow = nd.getUsersKnow();
		if (usersKnow.equals("")) {
			usersKnow = name;
		}else{
			// 检查用户是否已被记录
			String[] ary = usersKnow.split(",");
			boolean isFound = false;
			int len = ary.length;
		
			for (int i=0; i<len; i++) {
				if (name.equals(ary[i])) {
					isFound = true;
					break;
				}
			}
			if (!isFound) {
				usersKnow += "," + name;
			}
		}
		nd.setUsersKnow(usersKnow);
		NoticeReplyDb nrdb = new NoticeReplyDb();
		NoticeReplyMgr replyMgr = new NoticeReplyMgr();
		if(!replyMgr.readStatusByReply(id, privilege.getUserName(skey))){
			nrdb.setIsReaded("1");
			nrdb.setReadTime(new java.util.Date());
			nrdb.setNoticeid((long) id);
			nrdb.setUsername(privilege.getUserName(skey));
			nrdb.saveStatus();
		}
		re = nd.save();		
		try {
			if(re){
				json.put("id", id);
				json.put("res","0");
				JSONArray files = new JSONArray();
				Iterator riAtt = nd.getAttachs().iterator();
				String downPath = "";
				while (riAtt.hasNext()) {
					NoticeAttachmentDb nad = (NoticeAttachmentDb) riAtt.next();
					JSONObject file = new JSONObject();
					file.put("name", nad.getName());
					downPath = "public/android/notice_getfile.jsp?attId="
							+ nad.getId();
					file.put("url", downPath);
					files.put(file);
				}
				json.put("files", files);
				json.put("msg","操作成功");
			}else{
				json.put("id", id);
				json.put("res","-1");
				json.put("msg","操作失败");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setResult(json.toString());
	    return "SUCCESS";
	}
}
