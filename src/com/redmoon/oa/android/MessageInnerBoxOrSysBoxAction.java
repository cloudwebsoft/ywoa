package com.redmoon.oa.android;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.opensymphony.xwork2.ActionSupport;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.message.MessageDb;

public class MessageInnerBoxOrSysBoxAction {
	private String skey = "";
	private String result = "";
	private String op = "";
	private String cond = ""; //查询列表值
	private String what = "";
	private boolean type = false;
	
	public boolean getType() {
		return type;
	}
	public void setType(boolean type) {
		this.type = type;
	}

	private boolean dustbin = false;
	
	public boolean isDustbin() {
		return dustbin;
	}
	public void setDustbin(boolean dustbin) {
		this.dustbin = dustbin;
	}
	public String getOp() {
		return op;
	}
	public void setOp(String op) {
		this.op = op;
	}
	public String getCond() {
		return cond;
	}
	public void setCond(String cond) {
		this.cond = cond;
	}
	public String getWhat() {
		return what;
	}
	public void setWhat(String what) {
		this.what = what;
	}

	private int pagenum;
	private int pagesize;
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
	
	public int getPagenum() {
		return pagenum;
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
			MessageDb md = new MessageDb();
			String sql = "";
			if(type){
				// 系统消息
				sql = "select id from oa_message where is_sent=1 and receiver="+StrUtil.sqlstr(privilege.getUserName(getSkey()))+" and box=" + MessageDb.INBOX + " and is_dustbin=" + (dustbin?1:0)+" and type="+MessageDb.TYPE_SYSTEM ;
			}else{
				if(dustbin){
					sql = "select id from oa_message where ( (is_sent=1 and receiver="+StrUtil.sqlstr(privilege.getUserName(getSkey()))+" and box=" + MessageDb.INBOX + " and is_dustbin=1 and type <> "+MessageDb.TYPE_SYSTEM+" ) OR ( box ="+MessageDb.OUTBOX+" and sender ="+StrUtil.sqlstr(privilege.getUserName(getSkey()))+" and is_sender_dustbin=1  and type <> "+MessageDb.TYPE_SYSTEM+") ) ";
				}else{
					sql = "select id from oa_message where is_sent=1 and receiver="+StrUtil.sqlstr(privilege.getUserName(getSkey()))+" and box=" + MessageDb.INBOX + " and is_dustbin=" + (dustbin?1:0)+" and type<>10";
				}
			}
			//String sql = "select id from oa_message where is_sent=1 and receiver="+StrUtil.sqlstr(privilege.getUserName(getSkey()))+" and box=" + MessageDb.INBOX + " and is_dustbin=" + (dustbin?1:0) +" and type="+(isSys?10:0);
			if (getOp().equals("search")) {
				if (getCond().equals("title")) {
					sql += " and box=" + MessageDb.INBOX + " and title like " + StrUtil.sqlstr("%" + getWhat() + "%");
				} else if(getCond().equals("content")) {
					sql += " and box=" + MessageDb.INBOX + " and content like " + StrUtil.sqlstr("%" + getWhat() + "%");
				} else if(getCond().equals("sender")) {
					sql += " and sender in (select name from users where realname like " + StrUtil.sqlstr("%" + getWhat() + "%") + ")";
				} else if(getCond().equals("sys")) {
					sql += " and sender='系统'" + " and (content like " + StrUtil.sqlstr("%" + getWhat() + "%") + "or title like " + StrUtil.sqlstr("%" + getWhat() + "%") + ")";
				}
			}
			
			sql += " order by isreaded asc,rq desc";
	

			int curpage = getPagenum();
			int pagesize = getPagesize();
			ListResult lr = md.listResult(sql, curpage, pagesize);
			Vector vt = lr.getResult();
			Iterator ri = vt.iterator();
			//JdbcTemplate jt = new JdbcTemplate();
			//HttpServletRequest request = ServletActionContext.getRequest();
			//Paginator paginator = new Paginator(request);
			//ResultIterator ri = jt.executeQuery(sql, curpage, pagesize);
			//long total = ri.getTotal();
			//paginator.init(total, pagesize);
			// 设置当前页数和总页数
			//int totalpages = paginator.getTotalPages();
			
					
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
				if(action != null && !action.trim().equals("") && action.contains("action=flow_dispose")){
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
								if(lf.isLoaded()){
									flow.put("type", String.valueOf(lf.getType()));
									flow.put("typeName", lf.getName());
								}
							}
							message.put("flow",flow);
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
		setResult(json.toString());
		return "SUCCESS";
	}
}
