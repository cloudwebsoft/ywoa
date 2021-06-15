<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<title>查询管理</title>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	if (!privilege.isUserPrivValid(request, "admin.flow.query")) {
		out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "pvg_invalid"),"提示"));
		return;
	}

	FormQueryMgr aqm = new FormQueryMgr();
	boolean re = false;
	String op = ParamUtil.get(request, "op");

	if (op.equals("add")) {
		try {
			re = aqm.create(request);
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		}
		if (re) {
			out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "form_query_list.jsp"));
		}
		else {
			out.print(StrUtil.jAlert_Back("操作失败！","提示"));
		}
		return;			
	}
	else if (op.equals("modify")) {
		try {
			re = aqm.modify(request);
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		}
		if (re) {
			out.print(StrUtil.jAlert_Back("操作成功！","提示"));
		}
		else {
			out.print(StrUtil.jAlert_Back("操作失败！","提示"));
		}
		return;			
	}
	else if (op.equals("del")) {
		try {
			re = aqm.del(request);
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		}
		if (re) {
			String isSystem = ParamUtil.get(request, "isSystem");
			int CPages = ParamUtil.getInt(request, "CPages", 1);
			out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "form_query_list.jsp?isSystem=" + isSystem + "&CPages=" + CPages));
		}
		else {
			out.print(StrUtil.jAlert_Back("操作失败！","提示"));
		}
		return;	
	}
	else if (op.equals("modifyQueryPriv")){
	    FormQueryPrivilegeMgr apm = new FormQueryPrivilegeMgr();
		int id = ParamUtil.getInt(request, "id");
		try {
			re = apm.create(request);
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		}
		if (re) {
			out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "form_query_user.jsp?id=" + id));
		} else {
			out.print(StrUtil.jAlert_Back("请先选择！","提示"));
		}
		return;
	}
	
%>
</body>
</html>
