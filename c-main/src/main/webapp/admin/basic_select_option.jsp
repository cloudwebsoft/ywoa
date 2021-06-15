<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.SkinUtil"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>管理选择项</title>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<%
int kind = ParamUtil.getInt(request, "kind", -1);
String op = ParamUtil.get(request, "op");
String code = ParamUtil.get(request, "code");
SelectDb sd = new SelectDb();
SelectMgr sm = new SelectMgr();
sd = sm.getSelect(code);
if (op.equals("add")) {
	boolean re = false;
	try {
		  re = sm.createOption(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "basic_select_option.jsp?kind=" + kind + "&code=" + code));
	}
	else {
		out.print(StrUtil.jAlert_Redirect("操作失败！","提示", "basic_select_option.jsp?kind=" + kind + "&code=" + code));
	}
	return;
}
else if (op.equals("modify")) {
	boolean re = false;
	try {
		  re = sm.modifyOption(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "basic_select_option.jsp?kind=" + kind + "&code=" + code));
		return;
	}
}
else if (op.equals("del")) {
	boolean re = false;
	int id = ParamUtil.getInt(request, "id");
	re = sm.delOption(id);
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "basic_select_option.jsp?kind=" + kind + "&code=" + code));
		return;
	}
}
%>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String userName = privilege.getUser(request);
SelectKindPriv skp = new SelectKindPriv();
if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
	if (skp.canUserAppend(userName, kind) || skp.canUserModify(userName, kind) || skp.canUserDel(userName, kind)) {
	}
	else {	  
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

boolean canAdd = true;
boolean canModify = true;
boolean canDel = true;
if (kind!=-1) {
	canAdd = skp.canUserAppend(userName, kind);
	canModify = skp.canUserModify(userName, kind);
	canDel = skp.canUserDel(userName, kind);
}

String key = ParamUtil.get(request, "key");
%>
<%-- <%@ include file="basic_select_inc_menu_top.jsp"%> --%>
<div class="spacerH"></div>
<form id="formSearch" action="basic_select_option.jsp?kind=<%=kind %>&code=<%=code %>" method="post">
<table width="100%">
<tr>
	<td align="center">
	名称&nbsp;<input id="key" name="key" value="<%=key%>"/>
	<input type="submit" class="btn btn-default" value="查询"/>
	</td>
</tr>
</table>
</form>
<table width="100%" border="0" align="center" cellspacing="0" class="tabStyle_1 percent80">
  <tr align="center">
    <td width="25%" class="tabStyle_1_title">名称</td>
    <td width="25%" class="tabStyle_1_title">值</td>
    <td width="12%" class="tabStyle_1_title">默认</td>
    <td width="12%" class="tabStyle_1_title">启用</td>
    <td width="13%" class="tabStyle_1_title">序号</td>
    <td width="25%" class="tabStyle_1_title">操作</td>
  </tr>
    <%
		Iterator ir = sd.getOptions(new JdbcTemplate(), key).iterator();
		while (ir.hasNext()) {
		  SelectOptionDb sod = (SelectOptionDb)ir.next();
		  String name = sod.getName();
		  if (!sod.getColor().equals(""))
		  	name = "<font color='" + sod.getColor() + "'>" + name + "</font>";
	%>
  <tr align="center">
    <td><%=name%></td>
    <td><%=sod.getValue()%></td>
    <td><%=sod.isDefault()?"是":"否"%></td>
    <td><%=sod.isOpen()?"是":"否"%></td>
    <td><%=sod.getOrders()%></td>
    <td>
    <%if (canModify) { %>
    <a href="javascript:;" onclick="modify('<%=sod.getId()%>', '<%=sod.getName()%>','<%=sod.getValue()%>','<%=sod.getOrders()%>', '<%=sod.isDefault()?"true":"false"%>', '<%=sod.getColor()%>', '<%=sod.isOpen()?1:0%>')">修改</a>
    <%} %>
    <%if (canDel) { %>
    &nbsp;&nbsp;&nbsp;
    <a href="javascript:;" onclick="jConfirm('您确定要删除<%=StrUtil.toHtml(sod.getName())%>吗？','提示',function(r){if(!r){return;}else{window.location.href='?op=del&kind=<%=kind %>&id=<%=sod.getId()%>&code=<%=StrUtil.UrlEncode(sod.getCode())%>'}}) ">删除</a>
    <%} %>
    </td>
  </tr>
  <%}%>
</table>
<br />
<form action="basic_select_option.jsp" style="display:<%=canAdd?"":"none" %>" method="post" name="form1" id="form1">
<table width="54%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent60">
    <tbody>
      <tr>
        <td colspan="2" align="left" class="tabStyle_1_title">&nbsp;<%=sd.getName()%>选择项</td>
      </tr>
      <tr>
        <td height="26" align="right">名称</td>
        <td align="left"><input name="name" id="name" />
          &nbsp;
          <input type="hidden" id="code" name="code" value="<%=code%>" />
          <input type="hidden" id="op" name="op" value="add" />
          <input type="hidden" id="id" name="id" value="id" />
          <input type="hidden" id="kind" name="kind" value="<%=kind %>"/>
        </td>
      </tr>
      <tr>
        <td height="26" align="right">值</td>
        <td align="left"><input name="value" id="value" onfocus="this.select()" /></td>
      </tr>
      <tr>
        <td height="26" align="right">默认</td>
        <td align="left"><select id="isDefault" name="isDefault">
          <option value="true">是</option>
          <option value="false" selected>否</option>
        </select>
        </td>
      </tr>
      <tr>
        <td height="26" align="right">颜色</td>
        <td align="left">
        <select id="color" name="color">
          <option value="" style="COLOR: black" selected>无</option>
          <option style="BACKGROUND: #000088" value="#000088"></option>
          <option style="BACKGROUND: #0000ff" value="#0000ff"></option>
          <option style="BACKGROUND: #008800" value="#008800"></option>
          <option style="BACKGROUND: #008888" value="#008888"></option>
          <option style="BACKGROUND: #0088ff" value="#0088ff"></option>
          <option style="BACKGROUND: #00a010" value="#00a010"></option>
          <option style="BACKGROUND: #1100ff" value="#1100ff"></option>
          <option style="BACKGROUND: #111111" value="#111111"></option>
          <option style="BACKGROUND: #333333" value="#333333"></option>
          <option style="BACKGROUND: #50b000" value="#50b000"></option>
          <option style="BACKGROUND: #880000" value="#880000"></option>
          <option style="BACKGROUND: #8800ff" value="#8800ff"></option>
          <option style="BACKGROUND: #888800" value="#888800"></option>
          <option style="BACKGROUND: #888888" value="#888888"></option>
          <option style="BACKGROUND: #8888ff" value="#8888ff"></option>
          <option style="BACKGROUND: #aa00cc" value="#aa00cc"></option>
          <option style="BACKGROUND: #aaaa00" value="#aaaa00"></option>
          <option style="BACKGROUND: #ccaa00" value="#ccaa00"></option>
          <option style="BACKGROUND: #ff0000" value="#ff0000"></option>
          <option style="BACKGROUND: #ff0088" value="#ff0088"></option>
          <option style="BACKGROUND: #ff00ff" value="#ff00ff"></option>
          <option style="BACKGROUND: #ff8800" value="#ff8800"></option>
          <option style="BACKGROUND: #ff0005" value="#ff0005"></option>
          <option style="BACKGROUND: #ff88ff" value="#ff88ff"></option>
          <option style="BACKGROUND: #ee0005" value="#ee0005"></option>
          <option style="BACKGROUND: #ee01ff" value="#ee01ff"></option>
          <option style="BACKGROUND: #3388aa" value="#3388aa"></option>
          <option style="BACKGROUND: #000000" value="#000000"></option>
        </select>        
        </td>
      </tr>
      <tr>
        <td height="26" align="right">序号</td>
        <td align="left"><input name="orders" id="orders" size="10" value="0" /></td>
      </tr>
      <tr>
        <td height="26" align="right">启用</td>
        <td align="left">
        <select id="isOpen" name="isOpen">
        <option value="1">是</option>
        <option value="0">否</option>
        </select>
        </td>
      </tr>
      <tr>
        <td height="30" colspan="2" align="center"><input name="button" type="submit" class="btn"  value="确定 " />
          &nbsp;&nbsp;&nbsp;</td>
      </tr>
    </tbody>
</table>
</form>
</body>
<script>
function modify(id, name, value, orders, isDefault, color, isOpen) {
	o("id").value = id;
	o("name").value = name;
	o("value").value = value;
	o("orders").value = orders;
	o("op").value = "modify";
	o("isDefault").value = isDefault;
	o("color").value = color;
	o("isOpen").value = isOpen;
}
</script>
</html>
