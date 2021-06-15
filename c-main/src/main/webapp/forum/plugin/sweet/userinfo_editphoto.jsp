<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.plugin.sweet.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<%
	SweetUserInfoMgr suim = new SweetUserInfoMgr();
	boolean re = false;
	try {
		re = suim.editPhoto(application, request);
		if (re) {
			out.print(StrUtil.Alert_Redirect("编辑照片成功！", "userinfo_edit.jsp?boardcode=" + StrUtil.UrlEncode(suim.getBoardCode()) + "&userName=" + StrUtil.UrlEncode(suim.getUserName())));
		}
		else
			out.print(StrUtil.Alert_Back("编辑照片失败！"));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
%>
