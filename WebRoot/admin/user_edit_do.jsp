<%@ page contentType="text/html;charset=utf-8"
import = "cn.js.fan.util.*"
import = "java.io.File"
%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="com.redmoon.oa.video.VideoMgr"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@page import="com.cloudwebsoft.framework.util.LogUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>更改用户信息</title>
<%@ include file="../inc/nocache.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
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
<jsp:useBean id="userMgr" scope="page" class="com.redmoon.oa.person.UserMgr" />
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'/></div>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

boolean isSuccess = false;
String name = ParamUtil.get(request, "name");
String selectDeptCode = ParamUtil.get(request, "selectDeptCode");  //  当前列表的部门
try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "name", name, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;	
}

try {
	isSuccess = userMgr.modify(application, request);
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
}
catch (ErrMsgException e) {
	// out.print("<script type='text/javascript'>parent.parent.hiddenLoading();</script>");
	out.println(fchar.jAlert_Back(e.getMessage(),"提示"));
}
if (isSuccess)
{
	VideoMgr vmgr = new VideoMgr();                        
	if(vmgr.validate()){                             //校验成功后，同步添加视频会议用户
		UserDb userDb = new UserDb();
		userDb = userDb.getUserDb(name);
		String password = userDb.getPwdRaw();
		String returnString = vmgr.createUser(name,password);              //考虑到存在老用户，故编辑时，采取创建用户
		boolean isFinish = vmgr.getResultByParseXML(returnString);
		if (isFinish)
			LogUtil.getLog("同步视频会议用户:").info("编辑用户成功。");
		else 
			LogUtil.getLog("同步视频会议用户:").info("编辑用户失败。");
	}
	// out.print("<script type='text/javascript'>parent.parent.hiddenLoading();</script>");
	//out.print("<script type='text/javascript'>parent.parent.page_refresh();</script>");  //添加成功能刷新父页面
	if (!"".equals(selectDeptCode)) {
		// out.print(StrUtil.jAlert_Redirect("操作成功！","提示", request.getContextPath()+"/admin/organize/user_list.jsp?deptCode="+selectDeptCode));
	}
	else {
		// out.print(StrUtil.jAlert_Redirect("操作成功！","提示", request.getContextPath()+"/admin/organize/user_edit.jsp?name="+StrUtil.UrlEncode(name)));
	}
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", request.getContextPath()+"/admin/organize/user_edit.jsp?name="+StrUtil.UrlEncode(name) + "&selectDeptCode=" + StrUtil.UrlEncode(selectDeptCode)));
}
%>
</body>
</html>


