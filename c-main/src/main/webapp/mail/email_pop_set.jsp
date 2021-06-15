<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.redmoon.oa.emailpop3.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@page import="cn.js.fan.security.ThreeDesUtil"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>

<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>新增邮箱</title>
<link href="../skin/pop.css" type="text/css" rel="stylesheet" />
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script>
	function sure(){
		form1.submit();
	}
</script>
</head>
<body>
<div class="pop-warp">
  <div class="pop-warp-title">新增邮箱</div>
  <form name="form1" action="?op=add" method="post">
	  <div>
	    <div class="pop-warp-inbox">
	      <p>邮箱：<input type="text" name="emailName" id="emailName" maxlength="50"></p>       
	      <p>密码：<input type="password" name="emailPass" id="emailPass" maxlength="50"></p>
	     </div>
	    <div class="pop-warp-btn" onclick="sure()">确认</div>
	  </div>
  </form>
</div>
</body>
</html>
<%
	if (!privilege.isUserLogin(request)){
		out.print(StrUtil.jAlert_Back("请先登录！","提示"));
	}
	String op = ParamUtil.get(request,"op");
	EmailPop3Mgr epm = new EmailPop3Mgr();
	boolean re = false;
	String errmsg = "";
	String email = ParamUtil.get(request,"emailName");
	String emailPwd = ParamUtil.get(request,"emailPass");
	String name = privilege.getUser(request);
	if(op.equals("add")){
		if (email.equals("")){
	        errmsg += "请输入EMAIL！\\n";
		}
	    if (!StrUtil.IsValidEmail(email)){
	         errmsg += "Email的格式错误！\\n";
	    }
	    if (emailPwd.equals("")){
	          errmsg += "请输入密码！\\n";
	    }
	    if (!errmsg.equals("")){
	    	out.print(StrUtil.jAlert_Back(errmsg,"提示"));
	    }
		
	    if(errmsg.equals("")){
	    	EmailPop3Db ep = new EmailPop3Db();
	        ep.setUserName(name);
	        ep.setEmail(email);
	        ep.setEmailUser(email.split("@")[0]);
	        
	        emailPwd = ThreeDesUtil.encrypt2hex("cloudwebcloudwebcloudweb",emailPwd);
	        
	        ep.setEmailPwd(emailPwd);
	        ep.setServer("");
	        ep.setPort(0);
	        ep.setSmtpPort(0);
	        ep.setServerPop3("");
	        ep.setDelete(false);
	        ep.setSsl(false);
	        ep.setDefault(true);
	        re = ep.create();
	    }
	    if(re){
	    	response.sendRedirect("set_email_frame.jsp");
	    }
	    
		
	}

%>
