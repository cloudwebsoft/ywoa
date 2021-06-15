<%@ page contentType="text/html;charset=utf-8"
import = "cn.js.fan.util.ErrMsgException"
%><%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="java.util.Calendar" %><%
// 刷新在位时间
OnlineUserMgr.refreshStayTime(request,response);

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
boolean canMultiLogin = cfg.getBooleanProperty("canMultiLogin");
if (!canMultiLogin) {
	String loginOnOtherPlace = (String)session.getAttribute("loginOnOtherPlace");
	// System.out.println(getClass() + " loginOnOtherPlace=" + loginOnOtherPlace);
	if (loginOnOtherPlace!=null) {
		Privilege privilege = new Privilege();
		privilege.logout(request, response);
		out.print("multiLogin");
	}
}
else {
	out.print(OnlineUserMgr.getJSOrganization());
}
%>