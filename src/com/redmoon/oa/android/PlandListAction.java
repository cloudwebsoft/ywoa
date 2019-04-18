package com.redmoon.oa.android;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.netdisk.PublicLeaf;
import com.redmoon.oa.notice.NoticeDb;
import com.redmoon.oa.person.PlanDb;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

public class PlandListAction {
	
	private String skey = "";
	private String result = "";
	private String title = "";

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

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
	public String execute() {
		JSONObject json = new JSONObject();
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(getSkey());
		String sql = "";
		try {
			if (re) {
				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			}
			
			sql = "select id from user_plan where is_closed=0 and userName="
						+ StrUtil.sqlstr(privilege.getUserName(skey))
						+ " order by mydate desc, enddate desc limit 3";		
					
			PlanDb pd = new PlanDb();
			Vector v = null;
			int total = 0;
			v = pd.list(sql);
			Iterator ir = null;
			if(v!=null && v.size()>0)
			ir = v.iterator(); 
			JSONArray result = new JSONArray();
			while (ir.hasNext()) {
				   pd = (PlanDb)ir.next();
				   int action_type = pd.getActionType();
				   JSONObject plan = new JSONObject();
				   plan.put("id", String.valueOf(pd.getId()));	
				   plan.put("title", pd.getTitle());				   
				   plan.put("content", pd.getContent());				   
				   plan.put("startDate", DateUtil.format(pd.getMyDate(),"yyyy-MM-dd HH:mm:ss"));				   
				   plan.put("endDate", DateUtil.format(pd.getEndDate(),"yyyy-MM-dd HH:mm:ss"));				   
				   plan.put("is_closed",String.valueOf(pd.isClosed()));	
				   plan.put("is_remind",String.valueOf(pd.isRemind()));		
				   plan.put("remindDate",String.valueOf(pd.getRemindDate()));
				   plan.put("isToMobile",String.valueOf(pd.isRemindBySMS()));
				   plan.put("action_type",String.valueOf(action_type));
				   if(action_type!=0){
					   long action_id = Long.parseLong(pd.getActionData());
					   MyActionDb actionDb = new MyActionDb(action_id);
					   int flow_id = (int) actionDb.getFlowId();
					   WorkflowDb wfd = new WorkflowDb(flow_id);
						Logger.getLogger(PlandListAction.class.getName()).info("action_id="+action_id+",flow_id="+flow_id);
					   if(wfd!=null){
						   Leaf lf = new Leaf();
						   lf = lf.getLeaf(wfd.getTypeCode());
						   Logger.getLogger(PlandListAction.class.getName()).info("leafCode="+wfd.getTypeCode()); 
						   if(lf!=null){
							  plan.put("name", StrUtil.getNullStr(wfd.getTitle()));
							  plan.put("myActionId",String.valueOf(action_id));
							  plan.put("type", String.valueOf(lf.getType()));
						   }
							
					   }
						
				   }
				   result.put(plan);
			}
		
			json.put("res", "0");
			json.put("msg", "操作成功");
			json.put("result", result);
		
			//System.out.println(json.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setResult(json.toString());
		return "SUCCESS";
	}
}
