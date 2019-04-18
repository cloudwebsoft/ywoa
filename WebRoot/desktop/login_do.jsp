<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.oa.integration.cwbbs.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.security.*"%>
<%@ page import="com.redmoon.forum.security.*"%>
<%@ page import="com.redmoon.chat.ChatClient"%>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.util.Properties" %>
<%@ page import="rtx.*" %>
<%@ page import = "org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
boolean re = false;
JSONObject json = new JSONObject();
try{
	re = privilege.login(request, response);
	if (re) {
		json.put("ret", "1");
		json.put("msg", "");
	}
	else {
		json.put("ret", "0");
		json.put("msg", "帐户或密码错误！");
	}
	out.print(json);
}
catch(WrongPasswordException e){
	json.put("ret", "0");
	json.put("msg", e.getMessage());
	out.print(json);
	return;
}
catch (InvalidNameException e) {
	json.put("ret", "0");
	json.put("msg", e.getMessage());
	out.print(json);
	return;
}
catch (ErrMsgException e) {
	json.put("ret", "0");
	json.put("msg", e.getMessage());
	out.print(json);
	return;
}
%>