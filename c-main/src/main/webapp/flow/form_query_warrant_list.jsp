<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>授权查询</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1"><lt:Label res="res.flow.Flow" key="authorizationQuery"/></td>
    </tr>
  </tbody>
</table>
<%
/*
String isNav = ParamUtil.get(request, "isNav");
if (!"false".equals(isNav)) {
%>
<%@ include file="form_query_nav.jsp"%>
<script>
o("menu1").className="current"; 
</script>
<div class="spacerH"></div>
<%}%>
<%
*/
// 被授权查询的人，不需要再拥有“查询设计”的权限
if (!privilege.isUserPrivValid(request, "read")) {
// if (!privilege.isUserPrivValid(request, "admin.flow.query")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String sql;

UserDb user = new UserDb();
user = user.getUserDb(privilege.getUser(request));
RoleDb[] rgs = user.getRoles();			
int len = rgs.length;

String roles = "";
for (int i=0; i<len; i++) {
	if (roles.equals("")) {
		roles = StrUtil.sqlstr(rgs[i].getCode());
	}
	else
		roles += "," + StrUtil.sqlstr(rgs[i].getCode());
}
	
DeptUserDb dud = new DeptUserDb();
Iterator ir = dud.getDeptsOfUser(privilege.getUser(request)).iterator();
String depts = "";
while (ir.hasNext()) {
	DeptDb dd = (DeptDb)ir.next();
	if (depts.equals(""))
		depts = StrUtil.sqlstr(dd.getCode());
	else
		depts += "," + StrUtil.sqlstr(dd.getCode());
}

if ("".equals(depts)) {
	sql = "select distinct query_id from form_query_privilege where (priv_type=" + FormQueryPrivilegeDb.TYPE_USER + " and user_name=" + StrUtil.sqlstr(privilege.getUser(request)) + ") or (priv_type=" + FormQueryPrivilegeDb.TYPE_ROLE + " and user_name in (" + roles + ")) order by query_id desc";
}
else {
	sql = "select distinct query_id from form_query_privilege where (priv_type=" + FormQueryPrivilegeDb.TYPE_USER + " and user_name=" + StrUtil.sqlstr(privilege.getUser(request)) + ") or (priv_type=" + FormQueryPrivilegeDb.TYPE_ROLE + " and user_name in (" + roles + ")) or (priv_type=" + FormQueryPrivilegeDb.TYPE_DEPT + " and user_name in (" + depts + ")) order by query_id desc";
}
// out.print(sql);

String querystr = "";	
int pagesize = 10;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
		
FormQueryDb aqd = new FormQueryDb();			
ListResult lr = aqd.listResult(sql, curpage, pagesize);
long total = lr.getTotal();
Vector v = lr.getResult();
ir = v.iterator();
	
paginator.init(total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}
%>
      <table width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr> 
          <td width="47">&nbsp;</td>
          <td align="right" backgroun="images/title1-back.gif"><lt:Label res="res.flow.Flow" key="matchRecord"/> <b><%=paginator.getTotal() %></b> <lt:Label res="res.flow.Flow" key="article"/>　<lt:Label res="res.flow.Flow" key="perPage"/> <b><%=paginator.getPageSize() %></b> <lt:Label res="res.flow.Flow" key="article"/>　<lt:Label res="res.flow.Flow" key="page"/> <b><%=curpage %>/<%=totalpages %></td>
        </tr>
      </table>
	    <table class="tabStyle_1 percent98" width="97%" border="0" align="center" cellpadding="2" cellspacing="0" >
          <tr>
            <td width="47%" height="24" class="tabStyle_1_title" ><lt:Label res="res.flow.Flow" key="queryName"/></td>
            <td width="16%" class="tabStyle_1_title" ><lt:Label res="res.flow.Flow" key="time"/></td>
			<td width="17%" class="tabStyle_1_title" ><lt:Label res="res.flow.Flow" key="creator"/></td>
            <td width="20%" class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="operate"/></td>
          </tr>
	    <%
		UserDb ud = new UserDb();
		while (ir!=null && ir.hasNext()) {
			aqd = (FormQueryDb)ir.next();	
		%>
        <tr align="center" >
          <td align="left"><%=aqd.getQueryName()%></td> 
          <td><%=DateUtil.format(aqd.getTimePoint(), "yyyy-MM-dd")%></td>
		  <td>
<%
	String userName = aqd.getUserName();
	ud = ud.getUserDb(userName);
	out.print(ud.getRealName());
%>
		  </td>
          <td>
          <%if (!aqd.isScript()) {%>          
          	<a href="javascript:;" onclick="addTab('<%=aqd.getQueryName()%>', '<%=request.getContextPath()%>/flow/form_query_list_do.jsp?id=<%=aqd.getId()%>&op=query')"><lt:Label res="res.flow.Flow" key="query"/></a>
          <%}else{%>
            <a href="javascript:;" onClick="addTab('<%=aqd.getQueryName()%>', '<%=request.getContextPath()%>/flow/form_query_script_list_do.jsp?id=<%=aqd.getId()%>&op=query')"><lt:Label res="res.flow.Flow" key="query"/></a>        
          <%}%>
		  <%if (privilege.isUserPrivValid(request, "admin.flow.query")) {%>
		  &nbsp;&nbsp;&nbsp;<a href="javascript:;" onclick="top.mainFrame.addTab('<lt:Label res="res.flow.Flow" key="designer"/>', '<%=request.getContextPath()%>/flow/designer/designer.jsp?id=<%=aqd.getId()%>')"><lt:Label res="res.flow.Flow" key="designer"/></a>          
          <%}%>
          </td>
          </tr>
      <%}%>	 
      </table>
      <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="percent98">
        <tr> 
          <td height="23" align="right"> 
             <%
			   out.print(paginator.getCurPageBlock("?"+querystr));
			 %>
		  </td>
        </tr>
      </table>
</body>
</html>
