<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.officeequip.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>办公类别添加</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
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
	if (re) {
		//out.print(StrUtil.Alert_Redirect("操作成功！", "officeequip_type_list.jsp"));
		%>
	<script>
	window.opener.location.reload();
	window.close();
	</script>
<%
	return;
	}
}%><form id=form1 name="form1" action="?op=add" method=post>	  
	  <table width="100%" border="0" align="center" cellpadding="3" class="tabStyle_1">
        <tr align="left">
          <td width="150">用品类别名称</td>
          <td width="202"><input name="name" width="200"></td>
        </tr>
        <tr align="left">
          <td>参考单位<span class="STYLE6">(<span class="STYLE5">*</span>)</span></td>
          <td><input name="unit" width="200"></td>
        </tr>
        <tr align="left">
          <td>备注</td>
          <td><input name="abstracts" width="200"></td>
        </tr>
        <tr align="left">
          <td colspan="2" align="center"><input name="submit" type=submit class="btn" value="确定" /></td>
        </tr>
  </table>
  </form>
</body>
</html>
