package com.cloudweb.oa.controller;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.android.MessageInnerBoxOrSysBoxAction;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.message.MessageMgr;
import com.redmoon.oa.notice.NoticeDb;

@Controller
@RequestMapping("/public/message")
public class MessageController {
	@Autowired  
	private HttpServletRequest request;
	
	/**
	 * 列出消息
	 * @param skey
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/listNewMsg", method = RequestMethod.POST, produces={"text/html;","application/json;charset=UTF-8;"})		
	public String listNewMsg(String skey) {
		JSONObject json = new JSONObject(); 
		
		Privilege privilege = new Privilege();
		boolean re = privilege.auth(request);
		if(!re){
			try {
				json.put("res","-2");
				json.put("msg","时间过期");
				return json.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			String op = ParamUtil.get(request, "op");
			String cond = ParamUtil.get(request, "cond");
			String what = request.getParameter("what");
			what = StrUtil.UnicodeToUTF8(what);
			MessageDb md = new MessageDb();
			String sql = "select id from oa_message where is_sent=1 and receiver="+StrUtil.sqlstr(privilege.getUserName())+" and box=" + MessageDb.INBOX + " and is_dustbin=0";// and isreaded=0";
			if (op.equals("search")) {
				if (cond.equals("title")) {
					sql += " and box=" + MessageDb.INBOX + " and title like " + StrUtil.sqlstr("%" + what + "%");
				} else if(cond.equals("content")) {
					sql += " and box=" + MessageDb.INBOX + " and content like " + StrUtil.sqlstr("%" + what + "%");
				} else if(cond.equals("sender")) {
					sql += " and sender in (select name from users where realname like " + StrUtil.sqlstr("%" + what + "%") + ")";
				} else if(cond.equals("sys")) {
					sql += " and sender='系统'" + " and (content like " + StrUtil.sqlstr("%" + what + "%") + "or title like " + StrUtil.sqlstr("%" + what + "%") + ")";
				}
			}
			
			sql += " order by isreaded asc, rq desc";
			
			int curpage = ParamUtil.getInt(request, "pagenum", 1);
			int pagesize = ParamUtil.getInt(request, "pagesize", 20);
			ListResult lr = md.listResult(sql, curpage, pagesize);
			Vector vt = lr.getResult();
			Iterator ri = vt.iterator();

			json.put("res","0");
			json.put("msg","操作成功");
			json.put("total",String.valueOf(lr.getTotal()));
			
			JSONObject result = new JSONObject(); 
			result.put("count",String.valueOf(pagesize));
			
			JSONArray messages = new JSONArray(); 		
			
			while (ri.hasNext()) {
				MessageDb rr = (MessageDb)ri.next();	
				JSONObject message = new JSONObject();
				message.put("id",String.valueOf(rr.getId()));
				message.put("title",rr.getTitle());
				message.put("sender",rr.getSenderRealName());
				message.put("haveread",String.valueOf(rr.isReaded()));
				message.put("createdate",String.valueOf(rr.getSendTime()));
				String action = rr.getAction();
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
								message.put("flow", flow);
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
							message.put("flow", flow);
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
							message.put("notice", jo);
						}						
					}
				}
				messages.put(message);
			}	
			result.put("messages",messages);
			json.put("result",result);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(MessageInnerBoxOrSysBoxAction.class).error(e.getMessage());
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(MessageInnerBoxOrSysBoxAction.class).error(e.getMessage());
		}
		return json.toString();		
	}
	
	@ResponseBody
	@RequestMapping(value = "/delToDustbin", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;", "application/json;"})		
	public String delToDustbin() {
		JSONObject json = new JSONObject();
		com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
		boolean re = privilege.isUserPrivValid(request, "read");
		if(!re){
			try {
				json.put("res", "0");
				json.put("msg", "权限非法");
				return json.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		MessageMgr mm = new MessageMgr();
		try {
			try {
				re = mm.doDustbin(request, true);
				if (re) {
					if (re) {
						json.put("ret", 1);
						json.put("msg", "操作成功！");
					} else {
						json.put("ret", 0);
						json.put("msg", "操作失败");
					}			
				}				
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				json.put("res", 0);
				json.put("msg", e.getMessage());
				return json.toString();
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}

		return json.toString();
	}
	
	@ResponseBody
	@RequestMapping(value = "/restore", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;", "application/json;"})		
	public String restore() {
		JSONObject json = new JSONObject();
		com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
		boolean re = privilege.isUserPrivValid(request, "read");
		if(!re){
			try {
				json.put("res", "0");
				json.put("msg", "权限非法");
				return json.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		MessageMgr mm = new MessageMgr();
		try {
			try {
				re = mm.doDustbin(request, false);
				if (re) {
					if (re) {
						json.put("ret", 1);
						json.put("msg", "操作成功！");
					} else {
						json.put("ret", 0);
						json.put("msg", "操作失败");
					}			
				}				
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				json.put("res", 0);
				json.put("msg", e.getMessage());
				return json.toString();
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}

		return json.toString();
	}	
	
	@ResponseBody
	@RequestMapping(value = "/del", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;", "application/json;"})		
	public String del() {
		JSONObject json = new JSONObject();
		com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
		boolean re = privilege.isUserPrivValid(request, "read");
		if(!re){
			try {
				json.put("res", "0");
				json.put("msg", "权限非法");
				return json.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		MessageMgr mm = new MessageMgr();
		try {
			try {
				re = mm.delMsg(request);
				if (re) {
					if (re) {
						json.put("ret", 1);
						json.put("msg", "操作成功！");
					} else {
						json.put("ret", 0);
						json.put("msg", "操作失败");
					}			
				}				
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				json.put("res", 0);
				json.put("msg", e.getMessage());
				return json.toString();
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}

		return json.toString();
	}	
	
	@ResponseBody
	@RequestMapping(value = "/setReaded", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;", "application/json;"})	
	public String setReaded() {
		JSONObject json = new JSONObject();
		com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
		boolean re = privilege.isUserPrivValid(request, "read");
		if(!re){
			try {
				json.put("res", "0");
				json.put("msg", "权限非法");
				return json.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		re = true;
		try {
			String ids = ParamUtil.get(request, "ids");
			MessageDb md = new MessageDb();
			String[] ary = StrUtil.split(ids, ",");
			for (int i=0; i<ary.length; i++) {
				int id = StrUtil.toInt(ary[i]);
				md = (MessageDb)md.getMessageDb(id);
				md.setReaded(true);
				md.save();						
			}
			if (re) {
				if (re) {
					json.put("ret", 1);
					json.put("msg", "操作成功！");
				} else {
					json.put("ret", 0);
					json.put("msg", "操作失败");
				}			
			}				
		}
		catch (JSONException e) {
			e.printStackTrace();
		}

		return json.toString();
	}
	
	@ResponseBody
	@RequestMapping(value = "/setAllReaded", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;", "application/json;"})		
	public String setAllReaded() {
		JSONObject json = new JSONObject();
		com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
		boolean re = privilege.isUserPrivValid(request, "read");
		if(!re){
			try {
				json.put("res", "0");
				json.put("msg", "权限非法");
				return json.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		re = true;
		try {
			String sql = "select id from oa_message where isreaded=0 and box=0 and is_dustbin=0 and receiver="+StrUtil.sqlstr(privilege.getUser(request));
			MessageDb md = new MessageDb();
			Iterator ir = md.list(sql).iterator();
			while (ir.hasNext()){
				md = (MessageDb)ir.next();
				md.setReaded(true);
				md.save();	
			}
			if (re) {
				if (re) {
					json.put("ret", 1);
					json.put("msg", "操作成功！");
				} else {
					json.put("ret", 0);
					json.put("msg", "操作失败");
				}			
			}				
		}
		catch (JSONException e) {
			e.printStackTrace();
		}

		return json.toString();						
	}
	
	
	@ResponseBody
	@RequestMapping(value = "/setUnReaded", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;", "application/json;"})	
	public String setUnReaded() {
		JSONObject json = new JSONObject();
		com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
		boolean re = privilege.isUserPrivValid(request, "read");
		if(!re){
			try {
				json.put("res", "0");
				json.put("msg", "权限非法");
				return json.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		re = true;
		try {
			String ids = ParamUtil.get(request, "ids");
			MessageDb md = new MessageDb();
			String[] ary = StrUtil.split(ids, ",");
			for (int i=0; i<ary.length; i++) {
				int id = StrUtil.toInt(ary[i]);
				md = (MessageDb)md.getMessageDb(id);
				md.setReaded(false);
				md.save();						
			}
			if (re) {
				if (re) {
					json.put("ret", 1);
					json.put("msg", "操作成功！");
				} else {
					json.put("ret", 0);
					json.put("msg", "操作失败");
				}			
			}				
		}
		catch (JSONException e) {
			e.printStackTrace();
		}

		return json.toString();		
	}	
}
