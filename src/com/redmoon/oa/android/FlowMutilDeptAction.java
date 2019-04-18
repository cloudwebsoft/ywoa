package com.redmoon.oa.android;

import java.util.Iterator;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.redmoon.oa.flow.MatchUserException;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowRuler;
import com.redmoon.oa.person.UserDb;

import cn.js.fan.util.ErrMsgException;

public class FlowMutilDeptAction {
	private String skey = "";
	private String result = "";
	private String deptCode = "";
	public String getDeptCode() {
		return deptCode;
	}

	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}

	private int myActionId;

	public int getMyActionId() {
		return myActionId;
	}

	public void setMyActionId(int myActionId) {
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
		String res = "0";
		String msg = "操作成功";
		Privilege privilege = new Privilege();
		HttpServletRequest request = ServletActionContext.getRequest();
		JSONObject result = new JSONObject();
		boolean re = privilege.Auth(getSkey());
		try {
			if (re) {
				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			}
			
			privilege.doLogin(request, getSkey());
			
			// 这段用来验证字段是否可写
			MyActionDb mad = new MyActionDb();
			mad = mad.getMyActionDb(getMyActionId());
			WorkflowActionDb wa = new WorkflowActionDb();
			int actionId = (int) mad.getActionId();
			wa = wa.getWorkflowActionDb(actionId);
			if (wa == null || !wa.isLoaded()) {
				res = "-1";
				msg = "流程中的相应动作不存在";
				json.put("res", res);
				json.put("msg", msg);
				setResult(json.toString());
				return "SUCCESS";
			}
			// 取得下一步提交的用户
			JSONArray users = new JSONArray();
			Vector vto = wa.getLinkToActions();
			Iterator toir = vto.iterator();
			Iterator userir = null;
			WorkflowRuler wr = new WorkflowRuler();
			while (toir.hasNext()) {
				WorkflowActionDb towa = (WorkflowActionDb) toir.next();
				if (towa.getJobCode().equals(
						WorkflowActionDb.PRE_TYPE_USER_SELECT)
						|| towa
								.getJobCode()
								.equals(
										WorkflowActionDb.PRE_TYPE_USER_SELECT_IN_ADMIN_DEPT)) {
					JSONObject user = new JSONObject();
					user.put("actionTitle", towa.getTitle());
					user.put("roleName", towa.getJobName());
					user.put("internalname", towa.getInternalName());
					user.put("name", "WorkflowAction_" + towa.getId());
					// 手机客户端还不能区分是否在所管理的部门范围内
					user.put("value", WorkflowActionDb.PRE_TYPE_USER_SELECT);
					user.put("realName", "自选用户");
					user.put("isSelectable", "true");

					// 如果节点上曾经选过人，则在手机客户端默认选中
					user.put("actionUserName", towa.getUserName());
					user.put("actionUserRealName", towa.getUserRealName());

					// 标志位，能否选择用户
					boolean canSelUser = wr.canUserSelUser(request, towa);
					// System.out.println(getClass() + " actionUserRealName=" +
					// towa.getUserRealName() + " canSelUser=" + canSelUser);
					user.put("canSelUser", String.valueOf(canSelUser));

					users.put(user);
				} else {
					boolean isStrategySelectable = towa.isStrategySelectable();
					if (deptCode!=null) {
						deptCode = deptCode.trim(); // @android存在bug，多了一个空格
					}
					Vector vuser = towa.matchActionUser(request, towa, wa, false,deptCode);
					userir = vuser.iterator();
					while (userir != null && userir.hasNext()) {
						UserDb ud = (UserDb) userir.next();
						JSONObject user = new JSONObject();
						user.put("actionTitle", towa.getTitle());
						user.put("roleName", towa.getJobName());
						user.put("internalname", towa.getInternalName());
						user.put("name", "WorkflowAction_" + towa.getId());
						user.put("value", ud.getName());
						user.put("realName", ud.getRealName());
						user.put("isSelectable", String
								.valueOf(isStrategySelectable));

						// 标志位，能否选择用户
						boolean canSelUser = wr.canUserSelUser(request, towa);
						user.put("canSelUser", String.valueOf(canSelUser));
						users.put(user);
					}
				}
			}
			result.put("users", users);
		} catch (ErrMsgException e) {
			res = "-1";
			msg = "服务器端异常";
			Logger.getLogger(FlowMutilDeptAction.class).error(e.getMessage());
		} catch (JSONException e) {
			res = "-1";
			msg = "JSON解析异常";
			Logger.getLogger(FlowMutilDeptAction.class).error(e.getMessage());
		} catch (MatchUserException e) {
			res = "-1";
			msg = "下一步处理用户获取失败";
			Logger.getLogger(FlowMutilDeptAction.class).error(e.getMessage());
		}finally{
			try {
				json.put("result", result);
				json.put("res",res);
				json.put("msg",msg);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Logger.getLogger(FlowMutilDeptAction.class).error(e.getMessage());
			}
		}
		setResult(json.toString());
		return "SUCCESS";
	}
}
