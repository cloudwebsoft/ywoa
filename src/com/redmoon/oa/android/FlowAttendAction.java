package com.redmoon.oa.android;

import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.redmoon.oa.flow.*;
import com.redmoon.oa.person.UserMgr;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

public class FlowAttendAction {
	private String skey = "";
	private String result = "";
	private String op = "";
	private String title = "";
	
	public String getOp() {
		return op;
	}
	public void setOp(String op) {
		this.op = op;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
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
		String myname = privilege.getUserName(getSkey());
		WorkflowDb wf = new WorkflowDb();
		String sql = "select distinct m.flow_id from flow_my_action m, flow f where m.flow_id=f.id and (m.user_name=" + StrUtil.sqlstr(myname) + " or proxy=" + StrUtil.sqlstr(myname) + ") and f.status<>" + WorkflowDb.STATUS_NONE + " and f.status<> " + WorkflowDb.STATUS_DELETED;
		if(getOp().equals("search")){
		   sql = "select distinct m.flow_id from flow_my_action m, flow f where m.flow_id=f.id and (m.user_name=" + StrUtil.sqlstr(myname) + " or m.proxy=" + StrUtil.sqlstr(myname) + ") and f.status<>" + WorkflowDb.STATUS_NONE + " and f.status<> " + WorkflowDb.STATUS_DELETED;
		   if (!getTitle().equals("")) {
		     sql += " and f.title like "+StrUtil.sqlstr("%"+getTitle()+"%");	
		   }
		}	
		sql += " order by flow_id desc";
		//System.out.println("sql = " + sql);
		int curpage = getPagenum();   //第几页
		int pagesize = getPagesize(); //每页显示多少条
		try {
			ListResult lr = wf.listResult(sql, curpage, pagesize);
			int total = lr.getTotal();
			json.put("res","0");
			json.put("msg","操作成功");
			json.put("total",String.valueOf(total));
			Vector v = lr.getResult();
			Iterator ir = null;
			if (v!=null)
				ir = v.iterator();
			JSONObject result = new JSONObject(); 
			result.put("count",String.valueOf(pagesize));
			MyActionDb mad = new MyActionDb();
			UserMgr um = new UserMgr();
			Leaf lf = new Leaf();
			JSONArray flows  = new JSONArray(); 	
			while (ir.hasNext()) {
				WorkflowDb wfd = (WorkflowDb)ir.next(); 
				
				lf = lf.getLeaf(wfd.getTypeCode());
				if (lf==null)
					lf = new Leaf();
				
				JSONObject flow = new JSONObject(); 
				flow.put("flowId", String.valueOf(wfd.getId()));				
				flow.put("name", StringEscapeUtils.unescapeHtml(wfd.getTitle()) );
				flow.put("status",wfd.getStatusDesc());
				flow.put("beginDate", DateUtil.format(wfd.getBeginDate(),"yyyy-MM-dd HH:mm:ss"));
				
				flow.put("type", lf.getCode());
				flow.put("typeName", lf.getName());
				
			  	String lastUser = "";
			  	
			    sql = "select id from flow_my_action where flow_id=" + wfd.getId() + " and is_checked<>" + MyActionDb.CHECK_STATUS_CHECKED + " order by id desc";

				Iterator ir2 = mad.listResult(sql, 1, 1).getResult().iterator();
			  	if (ir2.hasNext()) {
					mad = (MyActionDb)ir2.next();
					lastUser = um.getUserDb(mad.getUserName()).getRealName();
				}
			  	
			  	flow.put("lastUser", lastUser);
				
				flows.put(flow);
			}		
			result.put("flows",flows);		
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
