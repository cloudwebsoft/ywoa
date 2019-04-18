<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.ParamUtil"%>
<%@ page import="cn.js.fan.web.SkinUtil"%>
<%@ page import="com.redmoon.forum.util.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.*"%>
<html>
<head>
<title>Check Regist Parameter</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<%@ include file="inc/nocache.jsp"%>
<link rel="stylesheet" href="common.css" type="text/css">
</head>
<body>
<%@ include file="inc/inc.jsp"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="userservice" scope="page" class="com.redmoon.forum.person.userservice"/>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("chkRegName")) {
	boolean re = false;
	try{
		String regName = ParamUtil.get(request, "RegName");
		re = ForumFilter.filterUserName(request, regName);
		re = userservice.isRegNameExist(request, regName);
	}
	catch(cn.js.fan.util.ErrMsgException e) {
		out.print(e.getMessage());
	%>
		<script>
		window.parent.showCheckResult("span_RegName", "<%=e.getMessage()%>");
		</script>
	<%	return;
	}
	if (!re) {
	%>
		<script>
		window.parent.showCheckResult("span_RegName", "<%=SkinUtil.LoadString(request, "res.label.forum.user", "user_name_ok")%>");
		</script>
	<%}else{%>
		<script>
		window.parent.showCheckResult("span_RegName", "<%=SkinUtil.LoadString(request, "res.label.forum.user", "user_name_exist")%>");
		</script>
	<%}
}

if (op.equals("chkEmail")) {
	String email = ParamUtil.get(request, "Email");
	int re = StrUtil.IsValidEmail(email)?0:1;
    RegConfig rcfg = new RegConfig();
    boolean allowERegUser = true;
    allowERegUser = rcfg.getBooleanProperty("allowERegUser");
    if(!allowERegUser){
        UserDb ud = new UserDb();
        if (!ud.validEmailOnly(email)) {
            re = 2;
        }
    }	
	if (re==0) {
	%>
		<script>
		window.parent.showCheckResult("span_email", "<%=SkinUtil.LoadString(request, "res.label.regist", "email_ok")%>");
		</script>
	<%}else if (re==1) {%>
		<script>
		window.parent.showCheckResult("span_email", "<%=SkinUtil.LoadString(request, "res.label.regist", "email_invalid")%>");
		</script>
	<%}else if (re==2) {%>
		<script>
		window.parent.showCheckResult("span_email", "<%=SkinUtil.LoadString(request, "res.label.forum.user", "allowereguser")%>");
		</script>
	<%}%>
}

if (!op.equals("")) {%>
	<script>
	window.self.history.go(-1); // 如果此处不返回，在注册未通过自动返回时，会致得再次检查时出错，原因可能是因为当form post后，父窗口再返回时IE并不会再显示该窗口内容造成
	</script>
<%}%>
</body>
</html>