package com.redmoon.oa.android;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.StrUtil;

import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.flow.WorkflowMgr;
import com.redmoon.oa.flow.WorkflowPredefineDb;
import com.redmoon.oa.person.UserDb;

public class FlowReturnAction {
	String flowId = "";
	String myActionId = "";
	private String skey = "";
	private String result = "";

	public String getFlowId() {
		return flowId;
	}

	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}

	public String getMyActionId() {
		return myActionId;
	}

	public void setMyActionId(String myActionId) {
		this.myActionId = myActionId;
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
		int flowId = StrUtil.toInt(getFlowId());
		int actionId = StrUtil.toInt(getMyActionId());
		WorkflowMgr wfm = new WorkflowMgr();
		WorkflowDb wf = wfm.getWorkflowDb(flowId);

		WorkflowPredefineDb wfp = new WorkflowPredefineDb();
		wfp = wfp.getPredefineFlowOfFree(wf.getTypeCode());

		try {
			json.put("res", "0");
			json.put("msg", "操作成功");
			JSONArray users = new JSONArray();
			if (wfp.getReturnStyle() == WorkflowPredefineDb.RETURN_STYLE_FREE) {

				String sql = "select id from flow_my_action where flow_id="
						+ flowId + " and is_checked<>" + MyActionDb.CHECK_STATUS_NOT + " order by receive_date asc";
				MyActionDb mad = new MyActionDb();
				Vector v = mad.list(sql);
				Iterator ir = v.iterator();
				Map map = new HashMap();
				WorkflowActionDb wa = new WorkflowActionDb();
				while (ir.hasNext()) {
					mad = (MyActionDb) ir.next();
					
					// 防止用户重复
					if (map.get(mad.getUserName())==null) {
						map.put(mad.getUserName(), mad.getUserName());
					}
					else
						continue;					
					
					long aId = mad.getActionId();
					if (map.get("" + aId) != null)
						continue;
					map.put("" + aId, "" + aId);
					wa = wa.getWorkflowActionDb((int) aId);
					// 去除本节点
					if (actionId == aId)
						continue;

					JSONObject user = new JSONObject();
					user.put("id",String.valueOf(wa.getId()));
					user.put("name", wa.getUserRealName());
					user.put("actionTitle", wa.getTitle());					
					users.put(user);
				}
			} else {
				WorkflowActionDb wa = new WorkflowActionDb();
				MyActionDb mad = new MyActionDb();
				mad = mad.getMyActionDb(actionId);
				wa = wa.getWorkflowActionDb((int)mad.getActionId());
		
				Vector returnv = wa.getLinkReturnActions();

				Iterator returnir = returnv.iterator();
				while (returnir.hasNext()) {
					WorkflowActionDb returnwa = (WorkflowActionDb) returnir
							.next();
					if (returnwa.getStatus() != WorkflowActionDb.STATE_IGNORED) {
						JSONObject user = new JSONObject();
						user.put("id", String.valueOf(returnwa.getId()));
						user.put("name", returnwa.getUserRealName());
						user.put("actionTitle", returnwa.getTitle());
						users.put(user);
					}
				}
			}
			json.put("users", users);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setResult(json.toString());
		return "SUCCESS";
	}
}
