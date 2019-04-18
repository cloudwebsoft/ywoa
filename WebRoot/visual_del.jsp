<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<link href="common.css" rel="stylesheet" type="text/css">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<script type="text/javascript" src="js/jquery1.7.2.min.js"></script>
<script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	// out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	// return;
}
String myname = privilege.getUser( request );

String op = ParamUtil.get(request, "op");
String privurl = ParamUtil.get(request, "privurl");
String formCode = ParamUtil.get(request, "formCode");
if (formCode.equals("")) {
	out.print(SkinUtil.makeErrMsg(request, "编码不能为空！"));
	return;
}

// int id = ParamUtil.getInt(request, "id");
FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCode);

com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
try {
	if (fdm.del(request)) {
		out.print(StrUtil.jAlert_Redirect("删除成功！","提示", privurl));
		out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
		return;
	}
}
catch (ErrMsgException e) {
	out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
}
%>