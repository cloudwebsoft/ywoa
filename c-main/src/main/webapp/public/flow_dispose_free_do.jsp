<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="org.json.JSONObject"%>
<%@ page import="org.json.JSONException"%>
<%@page import="com.redmoon.oa.db.SequenceManager"%>
<%@page import="java.util.Date"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
WorkflowMgr wfm = new WorkflowMgr();

String action = ParamUtil.get(request, "action");
if (action.equals("recall")) {
	int flowId = ParamUtil.getInt(request, "flowId");
	long myActionId = ParamUtil.getLong(request, "myActionId");
	try {
		boolean re = wfm.recallMyAction(request, myActionId);
		if (re)
			out.print(StrUtil.Alert_Redirect("操作成功！", "flow_modify.jsp?flowId=" + flowId));
		else {
			out.print(StrUtil.Alert_Back("操作失败，没有节点能被撤回，请检查后继节点是否已处理，或者是否存在后继节点！"));
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}	
	return;
}
else if (action.equals("deliver")) {
	boolean re = false;
	int flowId = ParamUtil.getInt(request, "flowId", -1);
	try {
		re = wfm.deliverFree(request, flowId);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	if (re)
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "flow_modify.jsp?flowId=" + flowId));
	else
		out.print(StrUtil.Alert_Back("操作失败！"));
	return;
}
wfm.doUpload(application, request);

String skey = wfm.getFieldValue("skey");
com.redmoon.oa.android.Privilege prl = new com.redmoon.oa.android.Privilege();
//再次赋值覆盖上面的固定赋值，原因是因为上面需要doUpload
prl.doLogin(request,skey);
//session.setAttribute(com,prl.getUserName(skey));
//session.setAttribute("oa.unitCode",prl.getUserUnitCode(skey));

String op = wfm.getFieldValue("op");
String strFlowId = wfm.getFieldValue("flowId");
int flowId = Integer.parseInt(strFlowId);
String strActionId = wfm.getFieldValue("actionId");
int actionId = Integer.parseInt(strActionId);
String strMyActionId = wfm.getFieldValue("myActionId");
long myActionId = Long.parseLong(strMyActionId);

WorkflowDb wf = wfm.getWorkflowDb(flowId);

String myname = privilege.getUser( request );
WorkflowActionDb wa = new WorkflowActionDb();
wa = wa.getWorkflowActionDb(actionId);
if (!wa.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "没有正在办理的节点！"));
	return;
}

MyActionDb myActionDb = new MyActionDb();
myActionDb = myActionDb.getMyActionDb(myActionId);

String result = wfm.getFieldValue("cwsWorkflowResult");
myActionDb.setResult(result);
//myActionDb.setChecked(true);
myActionDb.save();


if (op.equals("return")) {
	try {
		boolean re = wfm.ReturnAction(request, wf, wa, myActionId);
		if (re) {
			JSONObject json = new JSONObject();
			json.put("res", "0");
			json.put("op", op);
			json.put("msg", "操作成功！");
			out.print(json);
		}
		else {
			JSONObject json = new JSONObject();
			json.put("res", "-1");
			json.put("msg", "操作失败！");
			json.put("op", op);
			out.print(json);			
		}
	}
	catch (ErrMsgException e) {
		// out.print(StrUtil.Alert_Redirect(e.getMessage(), "flow_dispose_free.jsp?myActionId=" + myActionId));
		JSONObject json = new JSONObject();
		json.put("res", "-1");
		json.put("msg", e.getMessage());
		json.put("op", op);
		out.print(json);		
	}
}
else if (op.equals("finish")) {
	try {
		boolean re = wfm.FinishActionFree(request, wf, wa, myActionId);
		if (re) {
			// 如果后继节点中有一个节点是由本人继续处理，且已处于激活状态，则继续处理这个节点
			MyActionDb mad = wa.getNextActionDoingWillBeCheckedByUserSelf(privilege.getUser(request));
			if (mad!=null) {
				// out.print(StrUtil.Alert_Redirect("操作成功！请点击确定，继续处理下一节点！", "flow_dispose_free.jsp?myActionId=" + mad.getId()));
				JSONObject json = new JSONObject();
				json.put("res", "0");
				json.put("op", op);
				json.put("nextMyActionId", "" + mad.getId());
				json.put("msg", "操作成功！请点击确定，继续处理下一节点！");
				out.print(json);				
			}
			else {
				JSONObject json = new JSONObject();
				json.put("res", "0");
				json.put("op", op);
				json.put("nextMyActionId", "");
				json.put("msg", "操作成功！");
				out.print(json);				
			}
			return;
		}
		else {
			// out.print(StrUtil.Alert_Redirect("操作失败！", "flow_dispose_free.jsp?myActionId=" + myActionId));
			JSONObject json = new JSONObject();
			json.put("res", "-1");
			json.put("msg", "操作失败！");
			json.put("op", op);
			out.print(json);
		}
	}
	catch (ErrMsgException e) {
		// out.print(StrUtil.Alert_Redirect(e.getMessage(), "flow_dispose_free.jsp?myActionId=" + myActionId));
		e.printStackTrace();
		JSONObject json = new JSONObject();
		json.put("res", "-1");
		json.put("msg", "操作失败！");
		json.put("op", op);
		out.print(json);		
		return;
	}
}
else if (op.equals("manualFinish")) {
	boolean re = false;
	try {
		re = wfm.saveFormValue(request, wf, wa);
		re = wfm.ManualFinish(request, flowId, myActionId);
		if (re) {
			myActionDb.setResultValue(WorkflowActionDb.RESULT_VALUE_DISAGGREE);
			myActionDb.save();
		}		
	}
	catch (ErrMsgException e) {
		// out.print(StrUtil.Alert_Back(e.getMessage()));
		JSONObject json = new JSONObject();
		json.put("res", "-1");
		json.put("msg", e.getMessage());
		json.put("op", op);
		out.print(json);		
		return;
	}
	if (re) {
		JSONObject json = new JSONObject();
		json.put("res", "0");
		json.put("op", op);
		json.put("msg", "操作成功！");
		out.print(json);		
	}
	else {
		// out.print(StrUtil.Alert_Back("操作失败！"));
		JSONObject json = new JSONObject();
		json.put("res", "-1");
		json.put("msg", "操作失败！");
		json.put("op", op);
		out.print(json);		
	}
	return;
}

// 自动存档前先保存数据，然后获取flow_displose.jsp中iframe中的report表单数据在 办理完毕 时存档
if (op.equals("saveformvalue") || op.equals("AutoSaveArchiveNodeCommit")) {
	boolean re = false;
	try {
		re = wfm.saveFormValue(request, wf, wa);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	// afterXorCondNodeCommit通知flow_dispose.jsp页面，已保存完毕，匹配条件后，自动重定向
	if (re) {
		if (op.equals("saveformvalue")) {
			JSONObject json = new JSONObject();
			json.put("res", "5");
			json.put("op", op);
			json.put("msg", "保存草稿成功！");
			out.print(json);
			return;
		}
		else if (op.equals("AutoSaveArchiveNodeCommit")) {
			re = wfm.autoSaveArchive(request, wf, wa);
			JSONObject json = new JSONObject();
			if (re) {
				json.put("res", "0");
				json.put("op", op);
				json.put("msg", "保存存档成功！");
			}
			else {
				json.put("res", "-1");
				json.put("op", op);
				json.put("msg", "保存存档失败！");
			}
			out.print(json);
			return;
		}
	}
}
%>
