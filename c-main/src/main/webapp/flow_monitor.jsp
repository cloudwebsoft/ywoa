<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>发起流程</title>
<link href="common.css" rel="stylesheet" type="text/css">
<%@ include file="inc/nocache.jsp"%>
<script language="JavaScript" type="text/JavaScript">
<!--
function MM_preloadImages() { //v3.0
  var d=document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
    var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
    if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
}

function setPerson(deptCode, deptName, userName, userRealName)
{
	form1.userName.value = userName;
	form1.userRealName.value = userRealName;
}
//-->
</script>
<style type="text/css">
<!--
.style2 {font-size: 14px}
-->
</style>
</head>
<body background="" leftmargin="0" topmargin="5" marginwidth="0" marginheight="0">
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="flow.init";
if (!privilege.isUserPrivValid(request, priv))
{
	out.println(fchar.makeErrMsg("对不起，您不具有发起流程的权限！"));
	return;
}

WorkflowMgr wfm = new WorkflowMgr();

String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	boolean re = false;
	try {
		re = wfm.addMonitor(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re) {
		out.print(StrUtil.Alert("操作成功！"));
	}
}
if (op.equals("del")) {
	boolean re = false;
	try {
		re = wfm.delMonitor(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re) {
		out.print(StrUtil.Alert("操作成功！"));
	}
}

int flowId = ParamUtil.getInt(request, "flowId");
WorkflowDb wfd = wfm.getWorkflowDb(flowId);
String[] monitors = wfd.getMonitors();
UserDb ud = new UserDb();
%>
<table width="494" height="89" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe">
  <tr> 
    <td height="23" background="images/top-right.gif" class="right-title">&nbsp;&nbsp;<span>流程监控人员设定 (<%=wfd.getTitle()%>) </span></td>
  </tr>
  <tr> 
    <td valign="top">
	<table width="100%"  border="0" cellspacing="0" cellpadding="0">
	<form id=form1 name="form1" action="?op=add" method=post onSubmit="return form1_onsubmit()">
      <tr>
        <td height="100" align="center" class="p14"><table width="88%" height="81"  border="0" cellpadding="0" cellspacing="0" class="p14">
            <tr>
              <td class="p14">
			  <%
			  int len = monitors.length;
			  for (int i=0; i<len; i++) {
			  	ud = ud.getUserDb(monitors[i]);
			  %>
			  	<table width="100%">
			  	  <tr><td width="83%"><%=ud.getRealName()%></td>
			  	    <td width="17%"><a href="?op=del&flowId=<%=flowId%>&userName=<%=StrUtil.UrlEncode(monitors[i])%>">删除</a></td>
			  	  </tr></table>
			  <%
			  }
			  %>			  </td>
            </tr>
            <tr>
              <td class="p14"><input type="hidden" id="userName" name="userName" value="" >
                  <input type="hidden" id="flowId" name="flowId" value="<%=flowId%>" >
                    <input id="userRealName" name="userRealName" value="">
				<input type="submit" value="添加监控人员">
&nbsp;&nbsp;				<a href="#" onClick="javascript:showModalDialog('post_sel.jsp',window.self,'dialogWidth:480px;dialogHeight:320px;status:no;help:no;')">选择用户</a>				 </td>
            </tr>
          </table>
          <br>          </td>
      </tr>
      </form>
    </table></td>
  </tr>
</table>
<br>
<br>
</body>
<script>
function form1_onsubmit() {
	if (form1.userName.value=="") {
		alert("请选择流程监控人员！");
		return false;
	}
}
</script>
</html>
