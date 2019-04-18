<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.flow.WorkflowDb"%>
<%@ page import="com.redmoon.oa.flow.Render"%>
<%@ page import="com.redmoon.oa.flow.WorkflowMgr"%>
<%@ page import="com.redmoon.oa.flow.WorkflowActionDb"%>
<script>
</script>
<link href="common.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="js/jquery.js"></script>
<script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
WorkflowMgr wfm = new WorkflowMgr();
try {
	boolean re = wfm.saveDocumentArchive(request);
	if (re) {
		// 重定向至文件的列表页
		String dirCode = ParamUtil.get(request, "dirCode");
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "fileark/document_list_m.jsp?dir_code=" + StrUtil.UrlEncode(dirCode)));
		out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
	}
}
catch (ErrMsgException e) {
	out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
	e.printStackTrace();
}
%>