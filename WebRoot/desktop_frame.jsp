<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.ui.menu.*"%>
<%@ page import="cn.js.fan.security.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
UserSetupDb usd = new UserSetupDb();
usd = usd.getUserSetupDb(privilege.getUser(request));
int w = 400;
if (!usd.isShowSidebar()) {
	w = 0;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>桌面</title>
</head>
<frameset cols="*,<%=w%>" frameborder="no" border="0" framespacing="0">
  <frame src="desktop_left.jsp" name="mainFrame" id="mainFrame" title="mainFrame" />
  <frame src="desktop_right.jsp" name="rightFrame" scrolling="yes" noresize="noresize" id="rightFrame" title="rightFrame" />
</frameset>
<noframes><body>
</body></noframes>
</html>
