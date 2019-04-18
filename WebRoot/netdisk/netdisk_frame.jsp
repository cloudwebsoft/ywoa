<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.netdisk.*" %>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"><head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>网络硬盘</title>
</head>
<frameset rows="*" cols="180,*" framespacing="2" border="0" frameborder="1">
<%
String op = ParamUtil.get(request, "op");
String userName = ParamUtil.get(request, "userName");
String mode = ParamUtil.get(request, "mode");
String name = ParamUtil.get(request, "name");
String dir_code = ParamUtil.get(request, "dir_code");
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "mode", mode, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

if (op.equals("showDirShare")) {
%>
  <frame src="netdisk_dir_share.jsp?userName=<%=StrUtil.UrlEncode(userName)%>" name="leftFileFrame" >
<%}
else {
%>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.netdisk.Directory"/>
<%
	String root_code = privilege.getUser(request);
	boolean flag = true;
	Leaf leaf = dir.getLeaf(root_code);
	if (leaf==null || !leaf.isLoaded()) {
		// 为用户初始化网盘
		leaf = new Leaf();
		leaf.initRootOfUser(root_code);
		leaf = leaf.getLeaf(root_code);
		RoleTemplateMgr roleTemplateMgr = new RoleTemplateMgr();
		try {
			flag = roleTemplateMgr.copyDirsAndAttToNewUser(root_code);
			if(!flag){
				out.print(StrUtil.Alert("角色模板初始化失败！"));
			}
		} catch (ErrMsgException e1) {
			// TODO Auto-generated catch block
			out.print(StrUtil.Alert("角色模板初始化失败！"));
			e1.printStackTrace();
		}
	}
	
%>
  <frame src="netdisk_left.jsp?mode=<%=mode%>&dir_code=<%=dir_code %>" name="leftFileFrame" >
<%}%>
  <frame src="netdisk_main.jsp?op=<%=op%>&mode=<%=mode%>&dir_code=<%=dir_code %>&name=<%=name %>" name="mainFileFrame">
</frameset>
<noframes><body>
</body></noframes>
</html>
