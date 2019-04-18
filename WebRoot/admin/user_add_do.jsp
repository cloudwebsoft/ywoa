<%@ page contentType="text/html;charset=utf-8"
import = "java.io.File"
import = "cn.js.fan.util.*"
%>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.integration.cwbbs.*"%>
<%@ page import="com.redmoon.forum.security.*"%>
<%@page import="com.redmoon.oa.video.VideoMgr"%>
<%@page import="com.cloudwebsoft.framework.util.LogUtil"%>
<%@page import="com.redmoon.oa.visual.FormDAO"%>
<%@page import="com.redmoon.oa.flow.FormDb"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Iterator"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>添加用户</title>
<%@ include file="../inc/nocache.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
</head>
<style>
	.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
	-moz-opacity: 0.20;
	opacity: 0.20;
	z-index: 1500;
	}
	.treeBackground {
	display: none;
	position: absolute;
	top: -2%;
	left: 0%;
	width: 100%;
	margin: auto;
	height: 200%;
	background-color: #EEEEEE;
	z-index: 1800;
	-moz-opacity: 0.8;
	opacity: .80;
	filter: alpha(opacity = 80);
	}
</style>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil" />
<jsp:useBean id="usermgr" scope="page" class="com.redmoon.oa.person.UserMgr" />
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div> 
<br>
<%
String priv="admin.user";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

boolean isSuccess = false;
try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	isSuccess = usermgr.add(application, request);
}
catch (ErrMsgException e) {
	out.println(fchar.jAlert_Back("增加用户失败："+e.getMessage(),"提示"));
}
if (isSuccess) {
	String userName = ParamUtil.get(request, "name");
	String deptCode = ParamUtil.get(request, "deptCode");
	VideoMgr vmgr = new VideoMgr();   
	UserDb userDb = new UserDb();
	userDb = userDb.getUserDb(userName);                     
	if(vmgr.validate()){                             //校验成功后，同步添加视频会议用户
		String password = userDb.getPwdRaw();
		String returnString = vmgr.createUser(userName,password);
		boolean isFinish = vmgr.getResultByParseXML(returnString);
		if (isFinish)
			LogUtil.getLog("同步视频会议用户:").info("添加用户成功。");
		else 
			LogUtil.getLog("同步视频会议用户:").info("添加用户失败。");
	}
	CWBBSConfig ccfg = CWBBSConfig.getInstance();
	boolean isUseCWBBS = ccfg.getBooleanProperty("isUse");
		
	String isCWBBS = ParamUtil.get(request, "isCWBBS");
	isUseCWBBS = isUseCWBBS && isCWBBS.equals("1");
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	if (!isUseCWBBS) {
		if (deptCode.equals("")) {
			out.println(fchar.waitJump("<p style='line-height:1.5'>增加用户成功！<BR /><a href='"+request.getContextPath()+"/admin/user_dept_modify.jsp?userName=" + StrUtil.UrlEncode(userName) + "'>点击安排部门</a></p>", 3, "user_list.jsp"));
		}
		else {
			out.print("<script type='text/javascript'>parent.parent.hiddenLoading();</script>");
			out.print("<script type='text/javascript'>parent.parent.page_refresh();</script>");  //添加成功能刷新父页面
			// out.print(StrUtil.jAlert_Redirect("操作成功！","提示", request.getContextPath()+"/admin/organize/organize.jsp?type=list"));
		}
	} else {
		PassportRemoteUtil pru = new PassportRemoteUtil();
		request.setAttribute("uid", userDb.getName());
		request.setAttribute("desc", userDb.getAddress());
		request.setAttribute("pwd", userDb.getPwdMD5());
		request.setAttribute("realname", userDb.getRealName());
		pru.remoteSuperRegist(request, response, ccfg.getProperty("url"), ccfg.getProperty("key"), Global.getFullRootPath(request) + "/admin/organize/user_list.jsp");
	}
} else {
	out.print("<script type='text/javascript'>parent.parent.hiddenLoading();</script>");
}
	
%>
</body>
</html>


