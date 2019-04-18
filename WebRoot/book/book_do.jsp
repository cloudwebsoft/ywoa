<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "com.redmoon.oa.book.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<html>
<head>
<title></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<%
	String priv="book.all";
	if (!privilege.isUserPrivValid(request, priv))
	{
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	
	BookMgr bm = new BookMgr();
	boolean re = false;

	String op = ParamUtil.get(request, "op");
	if (op.equals("add")) {
		try {
			re = bm.create(request);
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
			out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
		}
		if (re) {
			out.print(StrUtil.jAlert_Redirect("添加成功！","提示", "book_add.jsp"));
			//out.print("OK!");
		}
		out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
	}
	if (op.equals("modify")) {
		try {
			re = bm.modify(request);
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
			out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
		}
		if (re) {
			out.print(StrUtil.jAlert_Back("修改成功！","提示"));
			out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
		}
	}
		if (op.equals("borrow")) {
		try {
			re = bm.borrow(request);
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
			out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
		}
		if (re) {
			out.print(StrUtil.jAlert_Back("借书成功！","提示"));
			out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
		}
	}

	if (op.equals("del")) {
		try {
			re = bm.del(request);
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
			out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
		}
		if (re) {
			out.print(StrUtil.jAlert_Redirect("删除成功！","提示", "book_list.jsp"));
			out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
		}
	}	
%>
</body>
</html>