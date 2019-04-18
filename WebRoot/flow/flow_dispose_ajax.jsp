<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@page import="com.redmoon.oa.ui.LocalUtil"%>
<%
String op = ParamUtil.get(request, "op");
long myActionId = ParamUtil.getLong(request, "myActionId");
MyActionDb myActionDb = new MyActionDb();
myActionDb = myActionDb.getMyActionDb(myActionId);
// System.out.println(getClass() + " myActionId=" + myActionId + " " + myActionDb.getCheckStatus() + " " + MyActionDb.CHECK_STATUS_PASS);
if (myActionDb.getCheckStatus()==MyActionDb.CHECK_STATUS_PASS) {
	JSONObject json = new JSONObject();
	json.put("ret", "0");
	String str = LocalUtil.LoadString(request,"res.flow.Flow","nodeByOtherPersonal");
	json.put("msg", str);
	out.print(json);
	return;
}

WorkflowActionDb wa = new WorkflowActionDb();
int actionId = (int)myActionDb.getActionId();
wa = wa.getWorkflowActionDb(actionId);
if ( wa==null || !wa.isLoaded()) {
	JSONObject json = new JSONObject();
	json.put("ret", "0");
	String str = LocalUtil.LoadString(request,"res.flow.Flow","actionNotExist");
	json.put("msg", str);
	out.print(json);
	return;
}
int flowId = wa.getFlowId();
WorkflowMgr wfm = new WorkflowMgr();
WorkflowDb wf = wfm.getWorkflowDb(flowId);

WorkflowPredefineDb wfp = new WorkflowPredefineDb();
wfp = wfp.getPredefineFlowOfFree(wf.getTypeCode());

if (wa.getStatus()==wa.STATE_DOING || wa.getStatus()==wa.STATE_RETURN)
	;
else {
	// 有可能会是重激活的情况
	if (!wfp.isReactive()) {
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		String str = LocalUtil.LoadString(request,"res.flow.Flow","processStatus");
		String str1 = LocalUtil.LoadString(request,"res.flow.Flow","mayHaveBeenProcess");
		json.put("msg", str + wa.getStatus() + "，"+str1);
		out.print(json);
		return;
	}
}

if (op.equals("transfer")) {
	String toUserName = request.getParameter("toUserName");//ParamUtil.get(request, "toUserName");
	JSONObject json = new JSONObject();
	try {
		wfm.transfer(request, myActionId, toUserName);
	}
	catch (ErrMsgException e) {
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		out.print(json);
		return;
	}
	json.put("ret", "1");
	String str = LocalUtil.LoadString(request,"res.common","info_op_success");
	json.put("msg", str);
	out.print(json);
}
else if (op.equals("suspend")) {
	JSONObject json = new JSONObject();
	try {
		wfm.suspend(request, myActionId);
	}
	catch (ErrMsgException e) {
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		out.print(json);
		return;
	}
	json.put("ret", "1");
	String str = LocalUtil.LoadString(request,"res.common","info_op_success");
	json.put("msg", str);
	out.print(json);
}
else if (op.equals("resume")) {
	JSONObject json = new JSONObject();
	long id = -1;
	try {
		id = wfm.resume(request, myActionId);
	}
	catch (ErrMsgException e) {
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		out.print(json);
		return;
	}
	json.put("ret", "1");
	String str = LocalUtil.LoadString(request,"res.common","info_op_success");
	json.put("msg", str);
	json.put("myActionId", "" + id);
	out.print(json);
}
else if (op.equals("plus")) {
	int type = ParamUtil.getInt(request, "type", WorkflowActionDb.PLUS_TYPE_BEFORE);
		
	boolean re = false;
	JSONObject json = new JSONObject();
	try {
		WorkflowActionMgr wam = new WorkflowActionMgr();
		re = wam.addPlus(request);
	}
	catch (ErrMsgException e) {
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		out.print(json);
		return;
	}
	
	if (re) {
		json.put("ret", "1");
		json.put("type", "" + type);
		String str = LocalUtil.LoadString(request,"res.common","info_op_success");
		json.put("msg", str);
	}
	else {
		json.put("ret", "0");
		String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
		json.put("msg", str);
	}
	out.print(json);
}
%>