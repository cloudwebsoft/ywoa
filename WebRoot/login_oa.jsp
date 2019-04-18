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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>登录</title>
<%@ include file="inc/nocache.jsp"%>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script language=javascript>
<!--
function openWin(url,width,height) {
	var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}
function openMyWin(target) {
	var newwin = window.open("","","toolbar=yes, menubar=yes, scrollbars=no, resizable=no,status=yes")
	if (document.all){
		newwin.moveTo(0,0)
		newwin.resizeTo(800,575)
		newwin.location=target
		opener=null;//用'popo'也可以不弹出提示关闭窗口;
		window.close();
	}
}
//-->
</script>
</head>
<body>
<jsp:useBean id="login" scope="page" class="cn.js.fan.security.Login"/>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
boolean re = false;
com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
if (scfg.isDefendBruteforceCracking()) {
	try {
		login.canlogin(request, "redmoonoa");
	}
	catch (ErrMsgException e) {
		out.print(SkinUtil.makeErrMsg(request, e.getMessage(), true));
		return;
	}
}

try{
	re = privilege.login(request, response);
	if (scfg.isDefendBruteforceCracking()) {
		login.afterlogin(request,re,"redmoonoa",true);
	}
}
catch(WrongPasswordException e){
	out.print(SkinUtil.makeErrMsg(request, "登录错误："+e.getMessage()));
	return;
}
catch (InvalidNameException e) {
	out.print(SkinUtil.makeErrMsg(request, "登录错误："+e.getMessage()));
	return;
}
catch (ErrMsgException e) {
	String str = e.getMessage();
	if (str.startsWith("-RTX")) {
		// 传参数reverseRTXLogin=false防止重新自动反向登录
		response.sendRedirect("index.jsp?reverseRTXLogin=false");
	}
	else
		out.print(SkinUtil.makeErrMsg(request, "登录错误："+str, true));
	return;
}
if (re) {
	String serverName = request.getServerName();
	// System.out.println(getClass() + " serverName=" + serverName);
	ServerIPPriv sip = new ServerIPPriv(serverName);
	if (!sip.canUserLogin(ParamUtil.get(request, "name"))) {
		out.print(SkinUtil.makeErrMsg(request, "禁止登录！"));
		return;
	}
	
    com.redmoon.oa.android.CloudConfig cfg = com.redmoon.oa.android.CloudConfig.getInstance();
	try {
		re = cfg.canUserLogin(request);
	}
	catch (ErrMsgException e) {
		out.print(SkinUtil.makeErrMsg(request, "登录错误："+e.getMessage(), true));
		return;
	}
}
%>
</body>
<%
String op = ParamUtil.get(request, "op");
if (re) {
	if (scfg.isForceChangeInitPassword()) {
		// 判断是否初始密码
		String pwd = ParamUtil.get(request, "pwd");
		if (pwd.equals(scfg.getInitPassword())) {
			response.sendRedirect("oa_change_initpwd.jsp");
			return;
		}
	}
	
	String name = ParamUtil.get(request, "name");
	UserDb user = new UserDb();
	user = user.getUserDb(name);
	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	boolean isRTXUsed = cfg.get("isRTXUsed").equals("true");
	if (isRTXUsed) {%>
		<script>
		// openWin("rtx/rtx.jsp?name=<%=StrUtil.UrlEncode(RTXUserProxy.getRTXName(user))%>", 1, 1);
		</script>
	<%}
	/*
	<script language="JavaScript">
	<!--
	//newwin = window.open('2.htm', '', 'height=475, width=800, top=0, left=0, toolbar=yes, menubar=yes, scrollbars=no, resizable=no,location=no, status=yes');
	//openMyWin("oa.jsp",800,600);
	//newwin = window.open('2.htm', 'd', 'height=475, width=800, top=0, left=0, toolbar=yes, menubar=yes, scrollbars=no, resizable=no,location=no, status=yes');
	//newwin.moveTo(0,0)
	//newwin.resizeTo(800,575)
	//opener=null;//用'popo'也可以不弹出提示关闭窗口;
	//window.close();
	//-->
	</script>
	*/
	CWBBSConfig ccfg = CWBBSConfig.getInstance();
	if (ccfg.getBooleanProperty("isUse")) {
		PassportRemoteUtil pru = new PassportRemoteUtil();
		request.setAttribute("uid", user.getName());
		request.setAttribute("desc", user.getAddress());
		request.setAttribute("pwd", user.getPwdMD5());
		request.setAttribute("realname", user.getRealName());
		pru.remoteSuperLogin(request, response, ccfg.getProperty("url"), ccfg.getProperty("key"), Global.getFullRootPath(request) + "/oa.jsp");
	}
	else {
		String mainTitle = ParamUtil.get(request, "mainTitle");
		String mainPage = ParamUtil.get(request, "mainPage");
		
		String queryStr = "";
		if (!mainPage.equals(""))
			queryStr = "?mainTitle=" + StrUtil.UrlEncode(mainTitle) + "&mainPage=" + mainPage;
		
		UserSetupDb usd = new UserSetupDb();
		// 注意不能用name作为参数，因为可能是用工号登录的
		// usd = usd.getUserSetupDb(name);
		usd = usd.getUserSetupDb(privilege.getUser(request));
		
		boolean isSpecified = cfg.get("styleMode").equals("2"); 
		// 指定风格
		if (isSpecified) {
			int styleSpecified = StrUtil.toInt(cfg.get("styleSpecified"), -1);
			if (styleSpecified!=-1) {
				if (styleSpecified==UserSetupDb.UI_MODE_PROFESSION)
					response.sendRedirect("oa.jsp" + queryStr);
				else if (styleSpecified==UserSetupDb.UI_MODE_FLOWERINESS) {
					response.sendRedirect("mydesktop.jsp" + queryStr);
				}
				else
					response.sendRedirect("main.jsp" + queryStr);				
				return;
			}
		}
		
		String os = ParamUtil.get(request, "os");
		// Safari
		if (os.equals("4")) {
			response.sendRedirect("main.jsp" + queryStr);
			return;
		}
		
		if (usd.getUiMode()==UserSetupDb.UI_MODE_NONE) {
			com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();    
			if(license.isVip()) {
				response.sendRedirect("ui_mode_guide.jsp"+ queryStr);
			}else{
				response.sendRedirect("oa.jsp"+ queryStr);
			}
		}
		else if (usd.getUiMode()==UserSetupDb.UI_MODE_PROFESSION)
			response.sendRedirect("oa.jsp" + queryStr);
		else if (usd.getUiMode()==UserSetupDb.UI_MODE_FLOWERINESS) {
			response.sendRedirect("mydesktop.jsp" + queryStr);
		}
		else
			response.sendRedirect("main.jsp" + queryStr);
	}
}
else {
	out.print(StrUtil.Alert_Back("登录失败，请检查用户名或密码是否正确！"));
}
%>
</html>