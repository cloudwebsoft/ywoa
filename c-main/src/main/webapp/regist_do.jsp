<%@ page contentType="text/html;charset=utf-8"
import = "java.io.File"
import = "cn.js.fan.util.*"
%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.util.Calendar"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.message.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="org.jdom.Element"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><lt:Label res="res.label.regist" key="regist"/> - <%=Global.AppName%></title>
<%@ include file="inc/nocache.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="forum/<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
</head>
<body>
<div id="wrapper">
<%@ include file="forum/inc/header.jsp"%>
<div id="main">
<br>
<br>
<jsp:useBean id="userservice" scope="page" class="com.redmoon.forum.person.userservice" />
<%
com.redmoon.forum.RegConfig cfg = new com.redmoon.forum.RegConfig();
boolean usevcode = cfg.getBooleanProperty("registUseValidateCode");
if (usevcode) {
	String sessionCode = StrUtil.getNullStr((String) session.getAttribute("validateCode"));
	String validateCode = ParamUtil.get(request, "validateCode");
	if (!validateCode.equals(sessionCode)) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "res.label.regist", "err_validate_code")));
		return;
	}
}

boolean isRegQuiz = cfg.getBooleanProperty("isRegQuiz");
if(isRegQuiz){
	String answer = ParamUtil.get(request,"quizAnswer");
	int id = ParamUtil.getInt(request,"qid",-1);
	if (id==-1) {
		out.print(StrUtil.Alert_Back("问题不存在，请联系管理员！"));
		return ;
	}
	QuizDb qd = new QuizDb();
	qd = qd.getQuizDb(id);
	if (qd==null) {
		out.print(StrUtil.Alert_Back("注册问题尚未初始化，请联系管理员！！"));
		return ;
	}	
	if (!qd.getString("answer").equals(answer)) {
		out.print(StrUtil.Alert_Back("答案不正确，请重新输入！"));
		return ;
	}
}

boolean cansubmit = false;
int interval = cfg.getIntProperty("registInterval");
int maxtimespan = interval;
try {
	cansubmit = cn.js.fan.security.Form.cansubmit(request, "regist", maxtimespan); // 防止重复刷新	
}
catch (ErrMsgException e) {
	out.println(StrUtil.Alert_Back(e.getMessage()));
	return;
}

boolean isSuccess = false;
com.redmoon.forum.ucenter.UCenterConfig myconfig = com.redmoon.forum.ucenter.UCenterConfig.getInstance();
boolean isUcActive = myconfig.getBooleanProperty("uc.isActive");
if(isUcActive) {//集成到UCenter
	String uid = "";
	com.redmoon.forum.ucenter.UC uCenter =  new com.redmoon.forum.ucenter.UC();
	try {
		String userName = ParamUtil.get(request, "RegName");
		String pwd = ParamUtil.get(request, "Password");
		String pwdRepeat = ParamUtil.get(request, "Password2");
		if(!pwd.equals(pwdRepeat)) {
			out.println(StrUtil.Alert_Back("两次输入的密码不一致！"));
			return;
		}
		String email = ParamUtil.get(request, "Email");
		uid = com.redmoon.forum.ucenter.UC.register(userName, pwd, email);
	} catch (ErrMsgException e) {
		out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
	try {
		isSuccess = uCenter.regist(request, response, uid);
	} catch (ErrMsgException e) {
		response.sendRedirect("info.jsp?info=" + StrUtil.UrlEncode(e.getMessage()));
		return;
	}
} else {
	try {
		isSuccess = userservice.regist(request);
	} catch (ErrMsgException e) {
		response.sendRedirect("info.jsp?info=" + StrUtil.UrlEncode(e.getMessage()));
		return;
	}
}

if (isSuccess) {
	String nick = ParamUtil.get(request, "RegName");
	UserDb ud = new UserDb();
	ud = ud.getUserDbByNick(nick);
	String info = "";
	if (ud.getCheckStatus()==UserDb.CHECK_STATUS_NOT) {
		RegConfig rc = new RegConfig();
        int regVerify = rc.getIntProperty("regVerify");
		String email = ParamUtil.get(request, "Email");
        if (regVerify==RegConfig.REGIST_VERIFY_EMAIL)
			info = SkinUtil.LoadString(request, "res.forum.Privilege", "info_need_check_email") + " " + email;
        else
			info = SkinUtil.LoadString(request, "res.forum.Privilege", "info_need_check_manual");
	}
	else {
		info = SkinUtil.LoadString(request, "res.label.regist", "regist_success");
	}
	out.print(StrUtil.waitJump(info, 3, "forum/index.jsp"));
}
else
	out.println(StrUtil.Alert_Back(SkinUtil.LoadString(request, "res.label.regist", "regist_fail")));
%><br>
<br>
<br>
<br>
<br>
</div>
<%@ include file="forum/inc/footer.jsp"%>
</div>
</body>
</html>


