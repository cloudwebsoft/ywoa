package com.redmoon.oa.android;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.ErrMsgException;

import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.flow.WorkflowMgr;

public class TestFlow {
	private String skey = "";
	private String result = "";
	private int myActionId;
	private String op = "";

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
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

	public int getMyActionId() {
		return myActionId;
	}

	public void setMyActionId(int myActionId) {
		this.myActionId = myActionId;
	}

	public int getActionId() {
		return actionId;
	}

	public void setActionId(int actionId) {
		this.actionId = actionId;
	}

	private int actionId;

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
		HttpServletRequest request = ServletActionContext.getRequest();
		MyActionDb mad = new MyActionDb();
		mad = mad.getMyActionDb(getMyActionId());
		long flowId = mad.getFlowId();
		WorkflowDb wf = new WorkflowDb();
		wf = wf.getWorkflowDb((int) flowId);
		WorkflowMgr wfm = new WorkflowMgr();

		WorkflowActionDb wa = new WorkflowActionDb();
		wa = wa.getWorkflowActionDb((int) actionId);
		if (getOp().equals("finish")) {
			try {
				re = wfm.FinishAction(request, wf, wa, myActionId);
				if (re) {
					json.put("res", "0");
					json.put("msg", "操作成功");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(getOp().equals("suspend")){
			WorkflowMgr wfm1 = new WorkflowMgr();
			try {
				re = wfm1.suspend(request, myActionId);
				if (re) {
					json.put("res", "0");
					json.put("msg", "挂起成功");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(getOp().equals("return")){
			
		}

		setResult(json.toString());
		return "SUCCESS";
	}
}
