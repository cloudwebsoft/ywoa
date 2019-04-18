<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.account.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工号修改</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%@ include file="../inc/nocache.jsp"%>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("modify")) {
	AccountMgr am = new AccountMgr();
	boolean re = false;
	try {
		  re = am.modify(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re)	{  
	     out.print(StrUtil.Alert("操作成功"));
%>
<%	}
}%>
<script>
function setPerson(deptCode, deptName, user, userRealName)
{
	form1.userRealName.value = userRealName;
	form1.userName.value = user;
}
</script>
<script src="../inc/common.js"></script>
<style type="text/css">
<!--
.STYLE1 {color: #FF0000}
-->
</style>
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.user")) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%@ include file="account_inc_menu_top.jsp"%>
<div class="spacerH"></div>
<%
    String name = ParamUtil.get(request, "name");
	AccountDb adb = new AccountDb();
	adb = adb.getAccountDb(name);
	String userName = StrUtil.getNullString(adb.getUserName());
	String userRealName = "";
	if (!userName.equals("")) {
		UserMgr um = new UserMgr();
		UserDb user = um.getUserDb(userName);
		if (user==null || !user.isLoaded()) {
			out.print(StrUtil.Alert("该用户已不存在！"));
		}
		else
			userRealName = user.getRealName();
	}
%>
<form action="?op=modify" method="post" name="form1" id="form1" onSubmit="">
<TABLE width="80%" border="0" align="center" cellspacing="0" class="tabStyle_1 percent80">
    <TBODY>
      <TR>
        <TD colspan="2" align="left" class="tabStyle_1_title">&nbsp;工号修改  </TD>
      </TR>
      <TR>
        <TD width="204" align="right">工号：</TD>
        <TD><INPUT name="name" id="name" value="<%=adb.getName()%>" maxLength="255" readonly="">
        <span class="STYLE1"> *</span></TD>
      </TR>
      <TR>
        <TD align="right">姓名：</TD>
        <td><input readonly name="userRealName" type="text" id="userRealName" value="<%=userRealName%>" size="20" >          
          <input type=hidden name="userName" size=20 value="<%=userName%>">
        <a href="#" onClick="javascript:showModalDialog('../user_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>',window.self,'dialogWidth:800px;dialogHeight:600px;status:no;help:no;')">选择用户</a></td>
      </TR>
      <TR>
        <TD colspan="2" align="center"><input name="button" type="submit" class="btn"  value="确定"></TD>
      </TR>
    </TBODY>
</TABLE>
</FORM>
</body>
</html>
