<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.privCenter.*"%>
<%@ page import = "org.json.*"%>
<%
String op = ParamUtil.get(request, "op");
if(op.equals("add")) {
	String flag = ParamUtil.get(request, "flag");
	String myPriv = ParamUtil.get(request, "myPriv");
	String module_code = ParamUtil.get(request, "module_code");
	String privs = ParamUtil.get(request, "privs");
	String userOrRoles = null;
	PrivCenterMgr pcm = new PrivCenterMgr();
	if("1".equals(flag)){//flag=1表示授权给用户
		userOrRoles = ParamUtil.get(request, "users");
		String userRealNames = ParamUtil.get(request, "userRealNames");
		try {
			pcm.addUsers(userOrRoles,myPriv,module_code,privs);
		}catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
		}
	}else if("2".equals(flag)){//flag=2表示授权给角色
		userOrRoles = ParamUtil.get(request, "roleCodes");
		String roleDescriptions = ParamUtil.get(request, "roleDescriptions");
		try {
			pcm.addRoles(userOrRoles,myPriv,module_code,privs);
		}catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
		}
	}
	return;	
}else if(op.equals("delete")) {
	String flag = ParamUtil.get(request, "flag");
	String myPriv = ParamUtil.get(request, "myPriv");
	String module_code = ParamUtil.get(request, "module_code");
	String privs = ParamUtil.get(request, "privs");
	String userOrRole = null;
	PrivCenterMgr pcm = new PrivCenterMgr();
	if("1".equals(flag)){//flag=1表示删除用户
		userOrRole = ParamUtil.get(request, "user");
		try {
			pcm.deleteUser(userOrRole,myPriv,module_code,privs);
		}catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
		}
	}else if("2".equals(flag)){//flag=2表示删除角色
		userOrRole = ParamUtil.get(request, "roleCode");
		try {
			pcm.deleteRole(userOrRole,myPriv,module_code,privs);
		}catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
		}	
	}
	return;	
}else if(op.equals("clear")) {
	String flag = ParamUtil.get(request, "flag");
	String myPriv = ParamUtil.get(request, "myPriv");
	String module_code = ParamUtil.get(request, "module_code");
	String privs = ParamUtil.get(request, "privs");
	PrivCenterMgr pcm = new PrivCenterMgr();
	if("1".equals(flag)){//flag=1表示清空用户
		try {
			pcm.clearUsers(null,myPriv,module_code,privs);
		}catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
		}	
	}else if("2".equals(flag)){//flag=2表示清空角色
		try {
			pcm.clearRoles(null,myPriv,module_code,privs);
		}catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
		}	
	}
	return;	
}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>权限选择处理</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=8">
</head>
<body>
</body>
</html>
