<%@ page contentType="text/html;charset=utf-8"%><%@ page import="com.redmoon.oa.person.*"%><%@ page import="com.redmoon.forum.security.*"%><%@ page import="cn.js.fan.util.*" %><%@ page import="cn.js.fan.web.*" %><%@ page import="cn.js.fan.security.*" %><%@ page import="java.util.Properties" %><%@ page import = "org.json.*"%><%
/**
* 用于spark登录时，同步从OA服务器获取key
*/
com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
boolean re = false;
try{
	String name = ParamUtil.get(request, "username");
	String pwdMD5 = ParamUtil.get(request, "password");
	
	System.out.println(getClass() + " name=" + name);
	System.out.println(getClass() + " pwdMD5=" + pwdMD5);
	
	JSONObject json = new JSONObject();
	re = privilege.Authenticate(name, pwdMD5);
	if (!re) {
		json.put("ret", 0);
		json.put("msg", StrUtil.GBToUnicode("登录失败！"));
		out.print(json);
		return;
	}

	com.redmoon.oa.sso.Config cfg = new com.redmoon.oa.sso.Config();
	String key = cfg.get("key");
	String authKey = "";
	try {
		authKey = ThreeDesUtil.encrypt2hex(key, name + "|" + DateUtil.toLongString(new java.util.Date()));
	} catch (Exception e) {
		com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("login MD5 exception: " + e.getMessage());
		e.printStackTrace();
	}
	
	com.redmoon.oa.Config oacfg = new com.redmoon.oa.Config();
	boolean isRecordLarkMsg = oacfg.get("isRecordLarkMsg").equals("true");
	
	json.put("ret", 1);
	json.put("authKey", authKey);
	
	if (isRecordLarkMsg)
		json.put("isRecordLarkMsg", 1);
	else
		json.put("isRecordLarkMsg", 0);
	
	out.print(json);
	return;
} catch (ErrMsgException e) {
	out.print("Authenticate error：" + e.getMessage());
}
%>