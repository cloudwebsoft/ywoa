<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "com.redmoon.oa.book.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String priv="read";
	if (!privilege.isUserPrivValid(request, priv))
	{
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	
	BookMgr bm = new BookMgr();
	boolean re = false;
	String op = ParamUtil.get(request, "op");
%>
<html>
<head>
<title>图书归还处理</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
</head>
<body>
<%
if (op.equals("return")) {
		try {
			re = bm.returnBook(request);
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
			out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
		}
		if (re) {
			out.print(StrUtil.jAlert_Redirect("还书成功！","提示","book_list.jsp"));
			out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
		}
 }
 %>
</body>
</html>