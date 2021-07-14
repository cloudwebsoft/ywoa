<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ include file="../inc/nocache.jsp"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>在线排行</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">在线排行</td>
    </tr>
  </tbody>
</table>
<%
String sql = "select name from users where isValid=1 order by online_time desc";
UserDb user = new UserDb();

String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
if (strcurpage.equals(""))
	strcurpage = "1";
if (!StrUtil.isNumeric(strcurpage)) {
	out.print(StrUtil.makeErrMsg("标识非法！"));
	return;
}
int pagesize = 50;
int curpage = Integer.parseInt(strcurpage);

String op = ParamUtil.get(request, "op");

// out.print(sql);

ListResult lr = user.listResult(sql, curpage, pagesize);
long total = lr.getTotal();
Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}

Vector v = lr.getResult();
Iterator ir = null;
if (v!=null)
	ir = v.iterator();
%>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0" class="percent60">
  <tr>
    <td height="24" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b></td>
  </tr>
</table>
<table width="62%" class="tabStyle_1 percent60">
  <tbody>
    <tr>
      <td width="30%" class="tabStyle_1_title">用户</td>
      <td width="30%" class="tabStyle_1_title">等级</td>
      <td width="40%" class="tabStyle_1_title">在线时长&nbsp;(小时)</td>
    </tr>
<%
UserLevelDb uld = new UserLevelDb();
while (ir.hasNext()) {
 	user = (UserDb)ir.next();
	%>
    <tr class="highlight">
      <td align="center"><%=user.getRealName()%></td>
      <td align="center"><img src="<%=request.getContextPath()%>/<%=uld.getUserLevelDbByLevel(user.getOnlineTime()).getLevelPicPath()%>" /></td>
      <td align="center"><%=NumberUtil.round(user.getOnlineTime(), 1)%></td>
    </tr>
<%}%>
  </tbody>
</table>
<table width="98%"  border="0" align="center" cellpadding="0" cellspacing="0" class="percent60">
  <tr>
    <td align="right"><%
	String querystr = "op="+op;
    out.print(paginator.getCurPageBlock("?"+querystr));
%></td>
  </tr>
</table>
</body>
</html>