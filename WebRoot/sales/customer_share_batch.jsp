<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.module.sales.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>共享人员列表</title>
<link href="../common.css" rel="stylesheet" type="text/css">
<%@ include file="../inc/nocache.jsp"%>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script language="JavaScript" type="text/JavaScript">
<!--
function openWin(url,width,height){
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=150,left=220,width="+width+",height="+height);
}
//-->
</script>
<style type="text/css">
<!--
.style2 {font-size: 14px}
.STYLE3 {color: #FFFFFF}
.STYLE4 {
	color: #000000;
	font-weight: bold;
}
.STYLE5 {color: #FF0000}
.STYLE6 {color: #000000}
-->
</style>
</head>
<body background="" leftmargin="0" topmargin="5" marginwidth="0" marginheight="0">
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>

<%
String ids = ParamUtil.get(request, "ids");
String op = ParamUtil.get(request, "op");
if (op.equals("addBatch")) {
	CustomerShareMgr csm = new CustomerShareMgr();
	try {
		csm.addBatch(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert(e.getMessage(),"提示"));
		return;
	}
	out.print(StrUtil.jAlert("操作成功！","提示"));
	%>
	<script>
	window.close();
	window.opener.focus();
	window.opener.location.reload();
	</script>
	<%
	return;
}
%>
<script language="javascript">
function setPerson(deptCode, deptName, user, userRealName){
	form1.sharePerson.value = user;
	form1.sharePersonRealName.value = userRealName;
}
function form1_onsubmit(){
	if (form1.sharePerson.value=="") {
		jAlert("请输入用户名","提示");
		return false;
	}
}
</script>
<table width="100%" border="0" cellpadding="0" cellspacing="0" class="tableframe_gray">
  <tr>
    <td class="right-title">&nbsp;共享人员列表</td>
  </tr>
  <form id=form1 name="form1" action="?op=addBatch" method=post onSubmit="return form1_onsubmit()">
  <tr>
    <td align="center"><span class="STYLE6">共享给</span>:
      <input name="sharePerson" id="sharePerson" type=hidden>
      <input name="sharePersonRealName" id="sharePersonRealName" style="width:50px">
      <input name="ids" id="ids" type="hidden" value="<%=ids%>" />
      <a href="#" onClick="javascript:showModalDialog('../user_sel.jsp',window.self,'dialogWidth:800px;dialogHeight:600px;status:no;help:no;')">选择用户</a></span>
      &nbsp;&nbsp;
      <input name="submit" type=submit class="btn" value="添  加"></td>
  </tr>
  </form>
</table>
</body>
</html>

