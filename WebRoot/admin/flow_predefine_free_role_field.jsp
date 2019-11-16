<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%
String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
String field = ParamUtil.get(request, "field");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>角色可写表单域</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%@ include file="../inc/nocache.jsp"%>
<script language="JavaScript" type="text/JavaScript">
<!--
function openWin(url,width,height)
{
	var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}

function OpenFormFieldSelWin() {
	openWin("flow_predefine_form_field_nest_sel.jsp?formView=0&flowTypeCode=<%=flowTypeCode%>&fields=" + getFieldWriteValue(), 500, 465);
}

function getFieldWriteValue() {
	return fieldWrite.value;
}

function setFieldValue(v) {
	fieldWrite.value = v;
}

function setFieldText(v) {
	fieldWriteText.value = v;
}

function setRoleField() {
	window.opener.setRoleField("<%=field%>", getFieldWriteValue(), fieldWriteText.value);
	window.close();
}

function window_onload() {
	setFieldValue(window.opener.getRoleFieldValue("<%=field%>"));
	setFieldText(window.opener.getRoleFieldText("<%=field%>"));
}
//-->
</script>
</head>
<%
String op = ParamUtil.get(request, "op");
%>
<body onLoad="window_onload()">
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, com.redmoon.oa.pvg.PrivDb.PRIV_ADMIN)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%
    String roleCode = ParamUtil.get(request, "roleCode");
	RoleDb rd = new RoleDb();
	rd = rd.getRoleDb(roleCode);
	String roleName = rd.getDesc();
	if (roleCode.equals("starter"))
		roleName = "发起人";
%>
<TABLE class="tabStyle_1" width="100%" height="240" border="0" align="center" cellpadding="0" cellspacing="0">
    <TBODY>
      <TR>
        <TD colspan="2" align="center" class="tabStyle_1_title"><%=roleName%>的可写表单域</TD>
      </TR>
      <TR>
        <td width="69" align="left">可写表单域</td>
        <td width="532"><textarea name="fieldWriteText" cols="70" rows="8" readonly="" id="fieldWriteText" style="background-color:#eeeeee"></textarea>
          <br>
          &nbsp;<a href="javascript:OpenFormFieldSelWin()">选择表单域 </a>
        <input name="fieldWrite" type="hidden" id="fieldWrite" value=""></td>
      </TR>
      <TR>
        <TD height="28" colspan="2" align="center"><input name="button" type="submit" class="btn"  value="确定" onClick="setRoleField()">
        <input type="hidden" name="roleCode" value="<%=roleCode%>">
        <iframe id=hiddenframe name=hiddenframe style="display:none" src="flow_predefine_action_modify_getfieldtitle.jsp" width=0 height=0></iframe></TD>
      </TR>
</TABLE>
</body>
</html>
