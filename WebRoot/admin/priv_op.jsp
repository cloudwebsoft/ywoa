<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>管理权限-添加/编辑</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>

<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>

</head>
<body>
<%
String op = ParamUtil.get(request, "op");
PrivDb pvg = null;
boolean isEdit = false;
if (op.equals("edit")) {
	isEdit = true;
	String priv = ParamUtil.get(request, "priv");
	if (priv.equals("")) {
		StrUtil.Alert_Back("编码不能为空！");
		return;
	}
	pvg = new PrivDb(priv);
}
if (op.equals("editdo")) {
	isEdit = true;
	PrivMgr privmgr = new PrivMgr();
	try {
		if (privmgr.update(request))
			out.print(StrUtil.Alert("修改成功！"));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}	
	String priv = ParamUtil.get(request, "priv");
	if (priv.equals("")) {
		StrUtil.Alert_Back("编码不能为空！");
		return;
	}
	pvg = privmgr.getPriv(priv);
}
%>
<%@ include file="priv_inc_menu_top.jsp"%>
<script>
$("menu2").className="current";
</script>
<div class="spacerH"></div>
<form action="<%=isEdit?"priv_op.jsp?op=editdo":"priv_m.jsp?op=add"%>" method="post" name="form1" id="form1">
<table class="tabStyle_1 percent80" width="71%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
      <td class="tabStyle_1_title" height="31" colspan="2" align="center"><%if (isEdit) {%>
        修改权限
        <%}else{%>
        添加权限
        <%}%>
      </td>
    </tr>
    <tr>
      <td width="91" height="31" align="center">编 码</td>
      <td align="left"><input name="priv" value="<%=isEdit?pvg.getPriv():""%>" <%=isEdit?"readonly":""%> /></td>
    </tr>
    <tr>
      <td height="32" align="center">描 述</td>
      <td align="left"><input name="desc" value="<%=isEdit?pvg.getDesc():""%>" /></td>
    </tr>
    <tr>
      <td height="32" align="center">层级</td>
      <td align="left">
      <select id="layer" name="layer">
      <option value="1">大类</option>
      <option value="2">小类</option>
      </select>
      <%
      if (isEdit) {
      %>
      <script>
      $('#layer').val('<%=pvg.getLayer()%>');
      </script>
      <%
      }
      %>
      </td>
    </tr>    
    <tr>
      <td height="43" colspan="2" align="center"><input name="Submit" type="submit" class="btn" value="确定" />
        &nbsp;&nbsp;&nbsp;
        <input name="Submit" type="reset" class="btn" value="重置" /></td>
    </tr>
</table>
</form>
<br>
<br>
</body>
</html>