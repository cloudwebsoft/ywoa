<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="java.util.Calendar" %>
<jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<%
Privilege privilege = new Privilege();

String op = ParamUtil.get(request, "op");
if (op.equals("del")) {
	int docId = ParamUtil.getInt(request, "docId");
	int pageNum = ParamUtil.getInt(request, "pageNum");
	Document doc = new Document();
	doc = doc.getDocument(docId);
	DocContent dc = doc.getDocContent(pageNum);
	if (dc.del()) {
%>
<link href="common.css" rel="stylesheet" type="text/css">
<link href="admin/default.css" rel="stylesheet" type="text/css">
<table align="left" width="200"><tr><td align="left">
<%	
		out.print(StrUtil.waitJump("删除成功！<br><BR><a href='fwebedit.jsp?op=edit&dir_code=" + StrUtil.UrlEncode(doc.getDirCode()) + "&id=" + doc.getID() + "'>点击此处返回</a>", 3, "fwebedit.jsp?op=edit&dir_code=" + StrUtil.UrlEncode(doc.getDirCode()) + "&id=" + doc.getID()));
%>
</td></tr></table>
<%		
	}
	else {
		out.print(StrUtil.Alert_Back("删除失败！"));
	}
	return;
}

boolean re = false;
try {
	re = docmanager.OperatePage(application, request, privilege);
}
catch(ErrMsgException e) {
	out.print(e.getMessage());
}
if (re) {
	out.print("操作成功！");
}
%>