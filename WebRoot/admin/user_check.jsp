<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.account.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="com.cloudwebsoft.framework.util.LogUtil"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<%
String op = ParamUtil.get(request, "op");

if (op.equals("name")) {
	boolean re = false;
	try {
		String name = ParamUtil.get(request,"name");
		if (name.length()>20){
			out.print("<font color='red'>帐号长度不能大于20个字符</font>");
			return;
		}
		UserCheck uc = new UserCheck();
		re = uc.chkNameAjax(request);
	}
	catch (ErrMsgException e) {
		out.print("<font color='red'>" + e.getMessage() + "</font>");
		return;
	}
	%>
	<font color="green">帐号合法</font>
<%
} else if (op.equals("personNo")) {
	String personNo = ParamUtil.get(request, "personNo");
	if ("".equals(personNo)) {
		return;
	}
	JSONObject json = new JSONObject();
	UserDb user = new UserDb();
	if (user.isPersonNoExist(personNo)) {
		json.put("ret", "0");
		json.put("msg", "编号已存在！");
		out.print(json);
	}
	else {
		json.put("ret", "1");
		json.put("msg", "");
		out.print(json);
	}
	return;
} else if (op.equals("personNoExcept")) {
	JSONObject json = new JSONObject();
	UserDb user = new UserDb();
	String personNo = ParamUtil.get(request, "personNo");
	String userName = ParamUtil.get(request, "userName");
	System.out.println(getClass() + " userName=" + userName);
	if (user.isPersonNoExistExceptUser(userName, personNo)) {
		json.put("ret", "0");
		json.put("msg", "编号已存在！");
		out.print(json);
	}
	else {
		json.put("ret", "1");
		json.put("msg", "");
		out.print(json);
	}
	return;
} else if ("mobile".equals(op)){
	try {
		String mobile = ParamUtil.get(request,"mobile");
		if (mobile==null||"".equals(mobile))
			return;
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = jt.executeQuery("select name from users where mobile =  "+StrUtil.sqlstr(mobile));
		while(ri.hasNext()){
			out.print("<font color='red'>手机号已注册</font>");
			break;
		}
	}
	catch (Exception e) {
		LogUtil.getLog("组织架构--校验手机号：").error(e.getMessage());
	}
	return;
} else {
	String account = ParamUtil.get(request, "account");
	AccountDb adb = new AccountDb();
	if (adb.isExist(account)) {
	%>
	<font color="red">工号已被使用</font>
	<%
	}
	else {
	%>
	<font color="green">工号合法</font>
	<%
	}
}
%>