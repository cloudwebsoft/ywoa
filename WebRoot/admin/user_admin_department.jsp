<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.pvg.Privilege"%>
<HTML><HEAD><TITLE>管理部门图</TITLE>
<link rel="stylesheet" href="../common.css">
<script language="JavaScript">
function openWinDepts() {
	var ret = showModalDialog('../dept_multi_sel.jsp',window.self,'dialogWidth:480px;dialogHeight:320px;status:no;help:no;')
	if (ret==null)
		return;
	form1.deptNames.value = "";
	form1.depts.value = "";
	for (var i=0; i<ret.length; i++) {
		if (form1.deptNames.value=="") {
			form1.depts.value += ret[i][0];
			form1.deptNames.value += ret[i][1];
		}
		else {
			form1.depts.value += "," + ret[i][0];
			form1.deptNames.value += "," + ret[i][1];
		}
	}
	if (form1.depts.value.indexOf("<%=DeptDb.ROOTCODE%>")!=-1) {
		form1.depts.value = "<%=DeptDb.ROOTCODE%>";
		form1.deptNames.value = "全部";
	}
}

function getDepts() {
	return form1.depts.value;
}
</script>
<META content="Microsoft FrontPage 4.0" name=GENERATOR><meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</HEAD>
<BODY bgColor=#FBFAF0 leftMargin=4 topMargin=8 rightMargin=0 class=menubar>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="admin";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String name = fchar.UnicodeToGB(request.getParameter("name"));
if (name==null)
{
	out.print(fchar.Alert("用户名不能为空！"));
	return;
}

UserDb ud = new UserDb();
String op = ParamUtil.get(request, "op");
if (op.equals("modify")) {
	boolean re = false;
	try {
		ud = ud.getUserDb(name);
		re = ud.setAdminDepts(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re)
		out.print(StrUtil.Alert("操作成功！"));
}

ud = ud.getUserDb(name);
String[] depts = ud.getAdminDepts();
String strdepts = "";
String strDeptNames = "";
int len = 0;
if (depts!=null)
	len = depts.length;
DeptDb dd = new DeptDb();	
for (int i=0; i<len; i++) {
	if (strdepts.equals("")) {
		strdepts = depts[i];
		strDeptNames = dd.getDeptDb(depts[i]).getName();
	}
	else {
		strdepts += "," + depts[i];
		strDeptNames += "," + dd.getDeptDb(depts[i]).getName();
	}
}
%>
<table width="426" border="0" align="center" cellpadding="0" cellspacing="1" class="tableframe">
<form action="?op=modify" name="form1" method="post">
  <tr>
    <td colspan="3" align="center" class="right-title"><%=name%>管理的部门</td>
    </tr>
  <tr>
    <td width="47">&nbsp;</td>
    <td width="317"> <span class="TableData">
      <input type="hidden" name="depts" value="<%=strdepts%>">
      <input type="hidden" name="name" value="<%=name%>">
      <textarea name="deptNames" cols="45" rows="3" readOnly wrap="yes" id="deptNames"><%=strDeptNames%></textarea>
&nbsp;</span><br> 
    <div></div>    </td>
    <td width="488" align="left"><span class="TableData">
      <input class="SmallButton" title="添加部门" onClick="openWinDepts()" type="button" value="选择部门" name="button">
    </span> </td>
  </tr>
  
  <tr>
    <td>&nbsp;</td>
    <td align="center"><input type="submit" value="确 定">
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </td>
    <td align="center">&nbsp;</td>
  </tr>
  </form>
</table>
</BODY></HTML>
