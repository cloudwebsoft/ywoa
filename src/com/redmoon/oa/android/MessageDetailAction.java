package com.redmoon.oa.android;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.message.Attachment;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.message.MessageMgr;
import com.redmoon.oa.notice.NoticeDb;
import com.redmoon.oa.person.UserDb;

public class MessageDetailAction {
	private String skey = "";
	private String result = "";
	private int  id;
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
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
		String uName = privilege.getUserName(skey);
		HttpServletRequest request = ServletActionContext.getRequest();
		MessageMgr msg = new MessageMgr();
		UserDb user = new UserDb();
		try {
			MessageDb md = msg.getMessageDb(getId());
			md.setReaded(true);//设置短消息状态为已读
			md.save();
			if (md==null || !md.isLoaded()) {
				json.put("res","-1");
				json.put("msg","消息不存在");
				setResult(json.toString());
				return "SUCCESS";
			}		
			json.put("res", "0");
			json.put("msg", "操作成功");
			json.put("id", String.valueOf(md.getId()));
			json.put("title", md.getTitle());
			json.put("haveread",md.isReaded());
			json.put("sender", md.getSenderRealName());
			String sender = md.getSender();
			String realNamejs = "";
			String receiverscs = md.getReceiverscs();
			String receiversms = md.getReceiversms();
			String receiversjs = md.getReceiversjs();
    		if(receiversjs != null && !"".equals(receiversjs)){
    			String[] recjsArr = receiversjs.split(",");
    			UserDb u1 = new UserDb();
    			for(int j=0;j<recjsArr.length;j++){
    	    		u1 = u1.getUserDb(recjsArr[j]);
    	    		if("".equals(realNamejs)){
    	    			realNamejs = u1.getRealName();
    	    		}else{
    	    			realNamejs += "," + u1.getRealName();
    	    		}
    			}
    			json.put("receiver", realNamejs);
    		}
			if(receiverscs != null && !"".equals(receiverscs)){ 
		    	String[] recArry = receiverscs.split(",");
		    	String realNameAll = "";
		    	for(int t = 0;t<recArry.length;t++){
		    		UserDb udb1 = new UserDb();
		    		udb1 = udb1.getUserDb(recArry[t]);
		    		if("".equals(realNameAll)){
		    			realNameAll = udb1.getRealName();
		    		}else{
		    			realNameAll += "," + udb1.getRealName();
		    		}
		    	}
		    	json.put("cs",realNameAll);
			}
			if(sender.equals(uName) && receiversms != null && !"".equals(receiversms)){ 
		    	String[] recArry = receiversms.split(",");
		    	String realNameAll2 = "";
		    	for(int t = 0;t<recArry.length;t++){
		    		UserDb udb1 = new UserDb();
		    		udb1 = udb1.getUserDb(recArry[t]);
		    		if("".equals(realNameAll2)){
		    			realNameAll2 = udb1.getRealName();
		    		}else{
		    			realNameAll2 += "," + udb1.getRealName();
		    		}
		    	}
		    	json.put("ms",realNameAll2);
			}
			
			if(!sender.equals(uName) && receiversms != null && receiversms.contains(uName) && (receiversjs == null || !receiversjs.contains(uName)) && (receiverscs == null || !receiverscs.contains(uName))){
				json.put("isTip", true);
			}else{
				json.put("isTip", false);
			}
			String cont = md.getContent();
			int p = cont.indexOf("<table ");
			if (p!=-1) {
				cont = cont.substring(0, p);
			}
			
			json.put("content", StrUtil.getAbstract(request, cont, 50000, "\r\n"));
			
			json.put("createdate", md.getSendTime());
			
			String action = md.getAction();
			if(action != null && !action.trim().equals("")) {
				if (action.contains("action=flow_dispose")) {
					int index = action.indexOf("|");
					String idInfo = action.substring(index+1,action.length());
					String[] idArr = idInfo.split("=");
					if(idArr != null && idArr.length == 2){
						String actionName = idArr[0];
						int id = Integer.parseInt(idArr[1]);
						JSONObject flow = new JSONObject();
						if(actionName!=null && !actionName.trim().equals("") && id !=0){
							long myActionId = 0;
							long flowId = 0;
							MyActionDb mad = new MyActionDb();
							if(actionName.equals("flowId")){
								flowId = id;
							}else{
								myActionId = id;
								flow.put("myActionId", String.valueOf(myActionId));
							}
							if(flowId == 0 && myActionId != 0){
								mad = mad.getMyActionDb(myActionId);
								flowId = mad.getFlowId();
							}
							WorkflowDb wf = new WorkflowDb((int) flowId);
							flow.put("flowId", String.valueOf(flowId));
							Leaf lf = new Leaf();
							lf = lf.getLeaf(wf.getTypeCode());
							flow.put("status",wf.getStatus());
							flow.put("name", wf.getTitle());
							if(lf != null){
								if(lf.isLoaded()) {
									flow.put("type", String.valueOf(lf.getType()));
									flow.put("typeName", lf.getName());
								}
							}
							json.put("flow", flow);
						}
					}
				}
				else if (action.contains("action=flow_show")) {
					// action=flow_show|flowId=155
					int index = action.indexOf("|");
					String idInfo = action.substring(index+1,action.length());
					String[] idArr = idInfo.split("=");
					if(idArr != null && idArr.length == 2) {
						int flowId = Integer.parseInt(idArr[1]);
						WorkflowDb wf = new WorkflowDb((int) flowId);
						JSONObject flow = new JSONObject();
						flow.put("flowId", String.valueOf(flowId));
						flow.put("name", wf.getTitle());
						json.put("flow", flow);
					}
				}
				else if (action.contains("noticeId=")) {
					// noticeId=254
					String[] idArr = action.split("=");
					if(idArr != null && idArr.length == 2) {
						int id = Integer.parseInt(idArr[1]);
						JSONObject jo = new JSONObject();
						NoticeDb nd = new NoticeDb();
						nd = nd.getNoticeDb(id);
						jo.put("noticeId", String.valueOf(id));
						jo.put("name", nd.getTitle());
						json.put("notice", jo);
					}						
				}
			}
			
			JSONArray attachments = new JSONArray();
			
			Iterator ir = md.getAttachments().iterator();
			String downPath = "";
			while (ir.hasNext()) {
				Attachment att = (Attachment)ir.next();				
				JSONObject attachment = new JSONObject();			
				attachment.put("id", String.valueOf(att.getId()));
				attachment.put("name", att.getName());
				downPath = "public/android/message_getfile.jsp?"+"attachId="+att.getId()+"&msgId="+md.getId();
				attachment.put("url", downPath);			
				attachment.put("size", String.valueOf(att.getSize()));
				attachments.put(attachment);
			}
			json.put("attachments", attachments);
			
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
