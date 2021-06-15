<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "com.redmoon.oa.meeting.*"%>
<html>
<head>
<title>会议室管理</title>
<%@ include file="../inc/nocache.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" href="common.css" type="text/css">
<script language="javascript">
<!--
//-->
</script>
</head>
<body bgcolor="#FFFFFF" text="#000000">
<%@ include file="../inc/inc.jsp"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="admin";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

BoardroomMgr bm = new BoardroomMgr();
String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	boolean re = false;
	try {
		re = bm.create(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back("添加失败：" + e.getMessage()));
	}
	if (re) {
		out.print(StrUtil.Alert_Redirect("添加成功！", "boardroom_m.jsp"));
	}
}

if (op.equals("del")) {
	boolean re = false;
	try {
		re = bm.del(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back("删除失败：" + e.getMessage()));
	}
	if (re) {
		out.print(StrUtil.Alert_Redirect("删除成功！", "boardroom_m.jsp"));
	}
}

if (op.equals("edit")) {
	boolean re = false;
	try {
		re = bm.modify(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back("修改失败：" + e.getMessage()));
	}
	if (re) {
		out.print(StrUtil.Alert_Redirect("修改成功！", "boardroom_m.jsp"));
	}
}
%>
</body>
</html>