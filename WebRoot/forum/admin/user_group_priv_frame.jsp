<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>管理用户组权限</title>
</head>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String groupCode = ParamUtil.get(request, "groupCode");
com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();	
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "groupCode", groupCode, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

%>
<frameset rows="*" cols="200,*" framespacing="0" frameborder="no" border="0">
  <frame src="user_group_priv_left.jsp?groupCode=<%=StrUtil.UrlEncode(groupCode)%>" name="userGroupLeftFrame" id="userGroupLeftFrame" title="userGroupLeftFrame" />
  <frame src="user_group_priv.jsp?groupCode=<%=StrUtil.UrlEncode(groupCode)%>" name="userGroupMainFrame" id="userGroupMainFrame" title="userGroupMainFrame" />
</frameset>
<noframes><body>
</body>
</noframes></html>
