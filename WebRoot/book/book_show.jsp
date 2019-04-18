<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.book.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="Book";
if (!privilege.isUserPrivValid(request, priv)) {
	//out.println(fchar.makeErrMsg("对不起，您不具有发起流程的权限！"));
	//return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("show")) {
	BookMgr bm = new BookMgr();
	boolean re = false;
	try {
		re = bm.modify(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re)
		out.print(StrUtil.Alert("操作成功！"));
}

int id = ParamUtil.getInt(request, "id");
BookDb bd = new BookDb();
bd = bd.getBookDb(id);
String pubDate = DateUtil.format(bd.getPubDate(), "yyyy-MM-dd");
   
BookTypeDb btdb = new BookTypeDb();
int typeId = bd.getTypeId();
BookTypeDb btd = btdb.getBookTypeDb(typeId);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>图书信息</title>
</head>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<body>
<div class="spacerH"></div>
<table class="tabStyle_1 percent60">
  <tbody>
	  <tr>
	      <td colspan=4 align="center" class="tabStyle_1_title">《<%=bd.getBookName()%>》详细信息
	      </td>
	      </tr>
    <tr>
      <td nowrap="nowrap" align="right" width="15%">图书名称：</td>
      <td nowrap="nowrap" width="35%"><%=bd.getBookName()%></td>
      <td nowrap="nowrap" align="right" width="15%">图书编号：</td>
      <td nowrap="nowrap" width="35%"><%=bd.getBookNum()%></td>
    </tr>
    <tr>
      <td nowrap="nowrap" align="right">图书类别：</td>
      <td nowrap="nowrap"><%=btd.getName()%></td>
      <td nowrap="nowrap" align="right">作&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;者：</td>
      <td nowrap="nowrap"><%=bd.getAuthor()%></td>
    </tr>
    <tr>
      <td nowrap="nowrap" align="right">出版单位：</td>
      <td nowrap="nowrap"><%=bd.getPubHouse()%> </td>
      <td nowrap="nowrap" align="right">出版时间：</td>
      <td nowrap="nowrap"><%=pubDate%></td>
    </tr>
    <tr>
      <td nowrap="nowrap" align="right">价&nbsp;格(￥)：</td>
      <td nowrap="nowrap"><%=bd.getPrice()%></td>
      <td colspan="2"></td>
    </tr>
    <tr>
      <td align="right">内容简介：</td>
      <td colspan="3"><%=bd.getBrief()%></td>
    </tr>
  </tbody>
</table>
</body>
</html>
