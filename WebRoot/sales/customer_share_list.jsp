<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.module.sales.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>共享人员列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<%@ include file="../inc/nocache.jsp"%>
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
.STYLE6 {color: #000000}
-->
</style>
</head>
<body background="" leftmargin="0" topmargin="5" marginwidth="0" marginheight="0">
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	CustomerShareMgr csm = new CustomerShareMgr();
	boolean re = false;
	try {
		  re = csm.create(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert(e.getMessage(),"提示"));
	}
	if (re)
		out.print(StrUtil.jAlert("操作成功！","提示"));
}
else if (op.equals("del")) {
	CustomerShareMgr csm = new CustomerShareMgr();
	boolean re = false;
	try {
		re = csm.del(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert(e.getMessage(),"提示"));
	}
	if (re)
		out.print(StrUtil.jAlert("操作成功！","提示"));
}

CustomerShareDb csd = new CustomerShareDb();
int id = ParamUtil.getInt(request,"id");
%>
<script language="javascript">
function setPerson(deptCode, deptName, user, userRealName)
{
	form1.sharePerson.value = user;
	form1.sharePersonRealName.value = userRealName;
}
</script>
<table width="100%" border="0" cellpadding="0" cellspacing="0" class="tableframe_gray">
  <tr>
    <td class="tdStyle_1">&nbsp;共享人员列表</td>
  </tr>
  <form id=form1 name="form1" action="?op=add&id=<%=id%>" method=post onSubmit="return form1_onsubmit()">
  <tr>
    <td align="center"><span class="STYLE6">共享给</span>：
      <input name="sharePerson" id="sharePerson" type=hidden>
      <input name="sharePersonRealName" id="sharePersonRealName" style="width:80px" readonly="readonly">
      <input name="customerId" id="customerId" type="hidden" value="<%=id%>" />
      &nbsp;
      <input type="button" class="btn" onclick="showModalDialog('../user_sel.jsp',window.self,'dialogWidth:640px;dialogHeight:480px;status:no;help:no;')" value="选择用户" />
      </span>
      &nbsp;
      <input name="submit" type=submit class="btn" value="添  加"></td>
  </tr>
  <tr>
    <td height="400" valign="top"><br>
    <table class="tabStyle_1" width="81%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
          <td align="center" class="tabStyle_1_title"><strong>已共享用户</strong></td>
          <td align="center" class="tabStyle_1_title"><strong>操作</strong></td>
        </tr>
      <%
	  String sql = "select id from customer_share where customerId=" + id;
	  Iterator ir = csd.list(sql).iterator();
	  UserDb ud = new UserDb();
	  while (ir.hasNext()) {
		csd = (CustomerShareDb)ir.next();%>
        <tr>
          <td width="59%"><%
					ud = ud.getUserDb(csd.getSharePerson());
					%>
              <%=ud.getRealName()%> </td>
          <td width="11%" align="center"><a href="javascript:;" onClick="jConfirm('您确定要删除<%=csd.getSharePerson()%>的共享吗？','提示',function(r){ if(!r){return;}else{ window.location.href='?op=del&id=<%=id%>&delId=<%=csd.getId()%>'}}) " style="cursor:pointer">删除</a></td>
        </tr>
      <%}%>
      </table>
</td></tr>
  </form>
</table>
</body>
<script>
function form1_onsubmit() {
	if (form1.sharePerson.value=="") {
		jAlert("请输入用户名","提示");
		return false;
	}
}
</script>
</html>

