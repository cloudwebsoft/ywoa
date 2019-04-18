<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.officeequip.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="officeequip";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	OfficeTypeMgr btm = new OfficeTypeMgr();
	boolean re = false;
	try {
		  re = btm.create(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re)
		out.print(StrUtil.Alert_Redirect("操作成功！", "officeequip_type_list.jsp"));
}

if (op.equals("del")) {
	OfficeTypeMgr btm = new OfficeTypeMgr();
	boolean re = false;
	try {
		re = btm.del(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re)
		out.print(StrUtil.Alert("操作成功！"));
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>添加办公用品类别</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script language="JavaScript" type="text/JavaScript">
<!--
function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=150,left=220,width="+width+",height="+height);
}
//-->
</script>
<script src="../inc/common.js"></script>
</head>
<body>
<%@ include file="officeequip_inc_menu_top.jsp"%>
<script>
$("menu1").className="current";
</script>
<div class="spacerH"></div>
<table width="98%" align="center" class="percent98">
<tr>
<td align="right">
<input type="button" class="btn" onClick="openWin('officeequip_type_add.jsp', 494, 123)" value="添加">
</td>
</tr>
</table>
<table width="98%" align="center" class="tabStyle_1 percent98">
  <tr>
    <td width="34%" class="tabStyle_1_title">用品类别名称</td>
    <td width="23%" class="tabStyle_1_title">参考单位</td>
    <td width="24%" class="tabStyle_1_title">备 注</td>
    <td width="19%" class="tabStyle_1_title">操 作</td>
  </tr>
<%
	OfficeTypeDb otd = new OfficeTypeDb();
	String sql = "select id from office_equipment_type order by id desc";
	Iterator ir = otd.list(sql).iterator();
	while (ir.hasNext()) {
		otd = (OfficeTypeDb)ir.next();
%>
  <tr>
    <td><a href="officeequip_all_list.jsp?op=search&typeId=<%=otd.getId()%>"><%=otd.getName()%></a></td>
    <td><%=otd.getUnit()%></td>
    <td><% 
					String abstracts = "";
					if (otd.getAbstracts() == null)
					       abstracts = "";
					else	   
					       abstracts = otd.getAbstracts();
					%>
    <%=abstracts%> </td>
    <td align="center"><a href="officeequip_type_edit.jsp?id=<%=otd.getId()%>">编辑</a>&nbsp;&nbsp;&nbsp;<a href="#" onClick="if (confirm('您确定要删除<%=otd.getName()%>吗？')) window.location.href='?op=del&id=<%=otd.getId()%>'">删除</a></td>
  </tr>
<%}%>
</table>
</body>
</html>

