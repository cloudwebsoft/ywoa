<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.officeequip.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "officeequip";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("modify")) {
	OfficeTypeMgr otm = new OfficeTypeMgr();
	boolean re = false;
	try {
		re = otm.modify(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re) {
		out.print(StrUtil.Alert("操作成功！"));	
	}
}

int id = ParamUtil.getInt(request, "id");
OfficeTypeDb btd = new OfficeTypeDb();
btd = btd.getOfficeTypeDb(id);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>办公类别编辑</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
<body>
<%@ include file="officeequip_inc_menu_top.jsp"%>
<div class="spacerH"></div>
<table align="center" class="tabStyle_1 percent60">
  <tr> 
    <td class="tabStyle_1_title">办公用品类别编辑</td>
  </tr>
  <tr> 
	  <form action="?op=modify" method=post> 
        <td align="center">办公类别名称：
		  <input name="name" value="<%=btd.getName()%>">
		  <input name="id" value="<%=id%>" type=hidden>
		  &nbsp;
		  <input class="btn" name="submit" type=submit value="确定"></td>
	  </form>
  </tr>
</table>
</body>
</html>
