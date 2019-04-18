<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.book.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "book.all";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");

int id = ParamUtil.getInt(request, "id");
BookTypeDb btd = new BookTypeDb();
btd = btd.getBookTypeDb(id);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>图书类别编辑</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
</head>
<body>
<%@ include file="book_inc_menu_top.jsp"%>
<%
if (op.equals("modify")) {
	BookTypeMgr btm = new BookTypeMgr();
	boolean re = false;
	try {
		re = btm.modify(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert(e.getMessage(),"提示"));
	}
	if (re) {
		 out.print(StrUtil.jAlert("操作成功！","提示"));	
	}
}
 %>
<table class="tabStyle_1 percent80">
  <tr> 
    <td class="tabStyle_1_title">图书类别编辑</td>
  </tr>
  <tr> 
	  <form action="?op=modify" method=post> 
        <td align="center">图书类别名称：
		  <input name="name" value="<%=btd.getName()%>">
		  <input name="id" value="<%=id%>" type=hidden>
		  &nbsp;
		  <input class="btn" name="submit" type=submit value="确定">&nbsp;</td>
	  </form>
  </tr>
</table>
<br>
<br>
</body>
</html>
