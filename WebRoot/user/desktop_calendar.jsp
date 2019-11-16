<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
/*
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
*/

int id = ParamUtil.getInt(request, "id");
UserDesktopSetupDb udsd = new UserDesktopSetupDb();
udsd = udsd.getUserDesktopSetupDb(id);
int count = udsd.getCount();

UserSetupDb usd = new UserSetupDb();
usd = usd.getUserSetupDb(privilege.getUser(request));
String calendarCode = usd.getCalendarCode();
if (calendarCode.equals("")) {
	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	calendarCode = cfg.get("calendarCode");
}
%>
<div id="drag_<%=id%>" class="portlet drag_div bor ibox" style="padding:0px;" >
  <div id="drag_<%=id%>_h" style="height:3px;padding:0px;margin:0px; font-size:1px"></div>
  <div class="portlet_content ibox-content" style="height:141px;padding:0px;margin:0px">
		<div style="text-align:center"><%=calendarCode%></div>
  </div>
</div>