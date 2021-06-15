<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="java.lang.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.module.cms.plugin.wiki.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>得分排行</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../../../../inc/common.js"></script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.fileark.Directory"/>
<%
LeafPriv lp = new LeafPriv(Leaf.CODE_WIKI);
if (!lp.canUserExamine(privilege.getUser(request))) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String dir_code = ParamUtil.get(request, "dir_code");
if (dir_code.equals(""))
	dir_code = Leaf.CODE_WIKI;
String dir_name = ParamUtil.get(request, "dir_name");
Leaf leaf = dir.getLeaf(dir_code);
%>
<%@ include file="wiki_rank_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<div class="spacerH"></div>
<%
String sDate = ParamUtil.get(request, "date");
java.util.Date date = DateUtil.parse(sDate, "yyyy-MM-dd");
if (date==null)
	date = new java.util.Date();
	
int row = ParamUtil.getInt(request, "row", 20);

String op = ParamUtil.get(request, "op");
String realName = ParamUtil.get(request, "realName");
String sql = "select name from sq_user_prop order by wiki_score desc";
if (op.equals("search")) {
	if (!realName.equals("")) {
		sql = "select p.name from sq_user_prop p, users u where p.name=u.name and u.realName like " + StrUtil.sqlstr("%" + realName + "%") + " order by wiki_score desc";
	}
}

UserPropDb upd = new UserPropDb();
int pagesize = 20;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
	
ListResult lr = upd.listResult(sql, curpage, pagesize);
long total = lr.getTotal();
Vector v = lr.getResult();
Iterator ir = null;
if (v!=null)
	ir = v.iterator();
paginator.init(total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}
%>
<form action="wiki_score_rank.jsp" method="get">
<table class="percent60" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td align="center" backgroun="images/title1-back.gif">
    用户&nbsp;
    <input name="op" value="search" type="hidden" />
    <input name="realName" size="10" value="<%=realName%>" />
	&nbsp;<input type="button" value="搜索" class="btn" />
    </td>
  </tr>
</table>
</form>
<br />
<table class="percent60" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td align="right" backgroun="images/title1-back.gif">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></td>
  </tr>
</table>
<table class="tabStyle_1 percent60"cellSpacing="0" cellPadding="3" width="95%" align="center">
  <thead>
    <tr>
      <td noWrap width="49%">用户</td>
      <td noWrap width="51%">得分</td>
    </tr>
  </thead>
  <tbody>
<%
com.redmoon.oa.person.UserMgr um = new com.redmoon.oa.person.UserMgr();
while (ir.hasNext()) {
	upd = (UserPropDb)ir.next();
	com.redmoon.oa.person.UserDb user = um.getUserDb(upd.getString("name"));
%>
    <tr onMouseOver="this.className='tbg1sel'" onMouseOut="this.className='tbg1'" class="tbg1">
      <td>
      <a href="<%=request.getContextPath()%>/user_info.jsp?userName=<%=StrUtil.UrlEncode(upd.getString("name"))%>" target="_blank"><%=user.getRealName()%></a></td>
      <td align="center"><%=upd.getDouble("wiki_score")%></td>
    </tr>
<%}%>
  </tbody>
</table>
<table width="98%" class="percent60" border="0" align="center" cellpadding="0" cellspacing="0">
<tr>
	<td align="right"><%
String querystr = "op=" + op + "&realName=" + StrUtil.UrlEncode(realName);
out.print(paginator.getCurPageBlock("wiki_score_rank.jsp?" + querystr));
%></td>
  </tr>
</table>
</body>
</html>