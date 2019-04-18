<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "com.redmoon.oa.worklog.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>点评工作报告</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>
<script language="JavaScript" type="text/JavaScript">
<!--
function MM_preloadImages() { //v3.0
  var d=document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
    var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
    if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
}
//-->
</script>
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
</head>
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
int id = ParamUtil.getInt(request, "id");

WorkLogMgr wlm = new WorkLogMgr();
WorkLogDb wld = wlm.getWorkLogDb(request, id);
String appraise="",mydate="";
int logType = WorkLogDb.TYPE_NORMAL;
if (wld!=null && wld.isLoaded()) {
	appraise = wld.getAppraise();
	mydate = DateUtil.format(wld.getMyDate(), "yyyy-MM-dd HH:mm:ss");
	logType = wld.getLogType();
}

String userName = wld.getUserName(); // ParamUtil.get(request, "userName");
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">点评工作报告</td>
    </tr>
  </tbody>
</table>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

if (!userName.equals(privilege.getUser(request))) {
	if (!(privilege.canAdminUser(request, userName))) {
		out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "pvg_invalid"),"提示"));
		return;
	}
}

String sql = "";

String op = request.getParameter("op");
if (op!=null)
{
	boolean re = false;
	try {
	%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		re = wlm.saveAppraise(request);
		%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	if (re)
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "mywork_appraise.jsp?id=" + id));
	return;
}
%>
<br />
<table width="80%" border="0" align="center" cellpadding="0" cellspacing="0" >
  <tr>
    <td><%=wld.getContent()%>
        <div style="border-bottom:1px dashed #cccccc"></div></td>
  </tr>
  <tr>
    <td style="padding-top:5px"><%=StrUtil.getNullStr(appraise)%></td>
  </tr>
</table>
<br />
<table width="98%" align="center" class="tabStyle_1 percent80">
  <form action="mywork_appraise.jsp?op=edit" method="post" name="form1" id="form1" onsubmit="return form1_onsubmit()">
    <tr>
      <td class="tabStyle_1_title">
      	  <%if (logType==WorkLogDb.TYPE_WEEK) {%>
          周报
		  <%}else if (logType==WorkLogDb.TYPE_MONTH) {%>
          月报
          <%}%>
          日期：<%=mydate%>
          <input type="hidden" name="id" value="<%=id%>" />
          </td>
    </tr>
    <tr>
      <td height="9" align="center">
          <textarea id="appraise" name="appraise" style="display:none"></textarea>
          <script>
			CKEDITOR.replace('appraise',
				{
					// skin : 'kama',
					toolbar : 'Middle'
				});
		  </script>
      </td>
    </tr>
    <tr>
      <td align="center">
      <input class="btn" name="submit" type="submit" value="确定" />
      &nbsp;&nbsp;
      <%
	  String url = "mywork.jsp";
	  if (logType==WorkLogDb.TYPE_WEEK)
	  	url = "mywork_list_week.jsp";
	  else if (logType==WorkLogDb.TYPE_MONTH)
	  	url = "mywork_list_month.jsp";
	  %>
      <input class="btn" type="button" value="返回" onclick="window.location.href='<%=url%>'" />
      </td>
    </tr>
  </form>
</table>
<br />
</body>
<script>
function form1_onsubmit() {
	if (CKEDITOR.instances.appraise.getData()=="") {
		jAlert("请填写内容！","提示");
		return false;
	}
}
</script>
</html>
