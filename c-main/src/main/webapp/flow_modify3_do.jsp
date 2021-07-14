<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int flowId = ParamUtil.getInt(request, "flowId");
WorkflowMgr wfm = new WorkflowMgr();
WorkflowDb wf = wfm.getWorkflowDb(flowId);
String flowstring = ParamUtil.get(request, "flowstring");
try {
	if (wf.modifyFlowString(request, flowstring)) {
		// response.sendRedirect("flow_modify.jsp?flowId=" + flowId);
		out.print(StrUtil.Alert_Redirect("修改成功！", "flow_modify3.jsp?flowId=" + flowId));
	}
	else {
		out.print(StrUtil.Alert_Back("更新流程时出错！"));
	}
}
catch (ErrMsgException e) {
	out.print(StrUtil.Alert_Back(e.getMessage()));
}
%>
