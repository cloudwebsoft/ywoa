<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.book.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>查询图书</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
</head>
<body>
<%@ include file="book_inc_menu_top.jsp"%>
<div class="spacerH"></div>
<table align="center" class="tabStyle_1 percent60">
<form action="book_list.jsp?op=search" name="form1" method="post">
<tr>
  <td colspan="2" class="tabStyle_1_title">图书查询</td>
</tr>
<tr>
  <td>图书类别</td>
  <td><%
	  BookTypeDb btd = new BookTypeDb();
	  String opts = "";
	  Iterator ir = btd.list().iterator();
	  while (ir.hasNext()) {
	  	 btd = (BookTypeDb)ir.next();
	  	 opts += "<option value='" + btd.getId() + "'>" + btd.getName() + "</option>";
	  }
	  %>
    <select name="typeId" id="typId" >
      <option value="all">全部</option>
      <%=opts%>
    </select></td>
</tr>
<tr>
  <td>图书名称 </td>
  <td><input type="text" name="bookName" id="bookName" value="" maxlength="100"></td>
</tr>
<tr>
  <td>图书编号  </td>
  <td><input type="text" name="bookNum" id="bookNum" value="" maxlength="100" ></td>
</tr>
<tr>
  <td>作者</td>
  <td><input type="text" name="author" id="author" value="" maxlength="100"></td>
</tr>
<tr>
  <td>出版社</td>
  <td><input type="text" name="pubHouse" id="pubHouse"value="" maxlength="100"></td>
</tr>
<tr>
  <td align="center" colspan="2"><input name="submit" type="submit" class="btn"  value="确定" ></td>
</tr>
</form>
</table>
</body>
</html>
