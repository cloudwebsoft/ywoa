<%@ page contentType="text/html;charset=utf-8"%><%@ page import="com.redmoon.oa.person.*"%><%@ page import="com.redmoon.forum.security.*"%><%@ page import="cn.js.fan.util.*" %><%@ page import="cn.js.fan.web.*" %><%@ page import="java.util.Properties" %><%
com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
boolean re = false;
try{
	String name = ParamUtil.get(request, "user");
	String pwd = ParamUtil.get(request, "pwd");
	System.out.println(getClass() + " name=" + name);
	System.out.println(getClass() + " pwd=" + pwd);
	String pwdMD5 = "";
	try {
		pwdMD5 = cn.js.fan.security.SecurityUtil.MD5(pwd);
	} catch (Exception e) {
		com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("login MD5 exception: " + e.getMessage());
	}
	re = privilege.Authenticate(name, pwdMD5);
	out.print(re);
}
catch (ErrMsgException e) {
	out.print("验证错误："+e.getMessage());
	return;
}
%>