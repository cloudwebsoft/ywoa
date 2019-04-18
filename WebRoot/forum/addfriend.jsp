<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.Privilege"%>
<%
    Privilege pvg = new Privilege();
    if (!pvg.isUserLogin(request)) {
        response.sendRedirect("../info.jsp?info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "err_not_login")));
        return;
    }
	
	userservice us = new userservice();
	boolean re = false;
	try {
		re = us.AddFriend(request);
	}
	catch (ErrMsgException e) {
		out.println(StrUtil.Alert_Back(e.getMessage()));
	}
	if (re) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_operate_success") + SkinUtil.LoadString(request, "res.label.forum.myfriend", "appling")));
	}
	else {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_operate_fail")));
	}
%>	