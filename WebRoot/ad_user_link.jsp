<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.miniplugin.ad.AdUserLink"%>
<%
	AdUserLink aul = new AdUserLink();
	String msg = aul.ad(request);
	if (!msg.equals("")) {
		out.print(StrUtil.Alert_Redirect(msg, "forum/index.jsp"));
		return;
	}
	else {
		response.sendRedirect("forum/index.jsp");
		return;	
	}
%>