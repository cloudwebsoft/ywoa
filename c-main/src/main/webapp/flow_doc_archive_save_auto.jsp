<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.flow.WorkflowPredefineDb"%>
<%@ page import="com.redmoon.oa.flow.WorkflowDb"%>
<%@ page import="com.redmoon.oa.flow.Render"%>
<%@ page import="com.redmoon.oa.flow.WorkflowMgr"%>
<%@ page import="com.redmoon.oa.flow.WorkflowActionDb"%>
<%@ page import="com.redmoon.oa.fileark.DirView"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
int actionId = ParamUtil.getInt(request, "actionId");
long myActionId = ParamUtil.getLong(request, "myActionId", -1);
WorkflowActionDb wa = new WorkflowActionDb();
wa = wa.getWorkflowActionDb(actionId);
int flowId = wa.getFlowId();
WorkflowMgr wfm = new WorkflowMgr();
WorkflowDb wf = wfm.getWorkflowDb(flowId);

String op = ParamUtil.get(request, "op");
if (op.equals("autoSave")) {
	boolean re = false;
	try {
		re = wfm.autoSaveArchive(request, wf, wa); 
		if (re) {
			if (myActionId==-1) {
				out.print(StrUtil.Alert_Redirect("自动存档成功！", "flow/flow_list.jsp?displayMode=1"));
			}
			else {
				out.print(StrUtil.Alert_Redirect("自动存档成功！", "flow_dispose.jsp?myActionId=" + myActionId));
			}
		}
		else {
			if (myActionId==-1) {
				out.print(StrUtil.Alert_Redirect("自动存档失败！", "flow/flow_list.jsp?displayMode=1"));
			}
			else {
				out.print(StrUtil.Alert_Redirect("自动存档失败！", "flow_dispose.jsp?myActionId=" + myActionId));
			}
		}
	}
	catch (ErrMsgException e) {
		if (myActionId==-1) {
			out.print(StrUtil.Alert_Redirect("自动存档失败：" + e.getMessage(), "flow/flow_list.jsp?displayMode=1"));
		}
		else {
			out.print(StrUtil.Alert_Redirect("自动存档失败：" + e.getMessage(), "flow_dispose.jsp?myActionId=" + myActionId));
		}
	}
	return;
}

// 置嵌套表需要用到的cwsId
request.setAttribute("cwsId", "" + flowId);
// System.out.println(getClass() + " cws_id=" + flowId);
request.setAttribute("pageType", "flow");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>自动存档</title>
<script src="inc/common.js"></script>
<script src="inc/flow_dispose_js.jsp"></script>
</head>
<body>
<%
out.println(SkinUtil.makeInfo(request, "请稍侯，正在存档......，请勿关闭窗口！"));
%>
<table style="display:none" width="98%" border="0" cellpadding="5" cellspacing="0" class="tableframe">
  <form id="flowForm" name="flowForm" action="flow_doc_archive_save_auto.jsp?op=autoSave&actionId=<%=actionId%>&myActionId=<%=myActionId%>" method="post" onsubmit="return flowForm_onsubmit()" target="_self">
    <tr>
      <td><%
				  int doc_id = wf.getDocId();
				  com.redmoon.oa.flow.DocumentMgr dm = new com.redmoon.oa.flow.DocumentMgr();
				  com.redmoon.oa.flow.Document doc = dm.getDocument(doc_id);
				  Render rd = new Render(request, wf, doc);
				  out.print(rd.report());
			%>
			<textarea id="formReportContent" name="formReportContent" style="display:none"></textarea>
	   </td>
    </tr>
  </form>
</table>
</body>
<script>
	flowForm.formReportContent.value = formDiv.innerHTML;
	flowForm.submit();
</script>
</html>
