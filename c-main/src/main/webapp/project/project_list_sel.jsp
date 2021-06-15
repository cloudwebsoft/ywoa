<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "read";
if (!privilege.isUserPrivValid(request, priv))
{
	// out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	// return;
}

String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action");
String formCode = "project";

String name = ParamUtil.get(request, "name");
try {	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "action", action, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "name", name, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

String querystr = "";

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=fd.getName()%><lt:Label res="res.flow.Flow" key="list"/></title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%@ include file="../inc/nocache.jsp"%>
<script>
function sel(id, prjName) {
	<%if (action.equals("linkProject")) {%>
	window.opener.doLinkProject(id, prjName);
	<%}else{%>
	window.opener.setIntpuObjValue(id, prjName);
	// IE10为function，IE8、9为object
	if (typeof(window.opener.doLinkProject)=="function" ||  typeof(window.opener.doLinkProject)=="object") {
		window.opener.doLinkProject(id, prjName);	
	}
	<%}%>
	window.close();
}
</script>
</head>
<body>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td class="tdStyle_1" height="23" valign="middle"><lt:Label res="res.flow.Flow" key="choose"/><%=fd.getName()%></td>
  </tr>
</table>
<%
String sql = "select id from " + fd.getTableNameByForm();

if (op.equals("search")) {
	if (!name.equals(""))
		sql = "select id from " + fd.getTableNameByForm() + " where name like " + StrUtil.sqlstr("%" + name + "%");
}

sql += " order by id desc";

querystr = "op=" + op + "&name=" + StrUtil.UrlEncode(name);
%>
<form id="form2" name="form2" action="project_list_sel.jsp" method="get">
  <table cellspacing="0" cellpadding="2" style="margin-bottom:5px;" width="100%" border="0">
    <tbody>
      <tr>
        <td align="center"><lt:Label res="res.flow.Flow" key="name"/>:
        <input name="provider_cond" value="0" type="hidden" />
        <input name="name" size="20" value="<%=name%>" />
        <input class="btn"  type="submit" value="<lt:Label res='res.flow.Flow' key='query'/>" />
        <input name="op" value="search" type="hidden" />
        <input name="action" value="<%=action%>" type="hidden" />
        </td>
      </tr>
    </tbody>
  </table>
</form>
<%
int pagesize = 10;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
	
FormDAO fdao = new FormDAO();

ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
long total = lr.getTotal();
Vector v = lr.getResult();
Iterator ir = null;
if (v!=null)
	ir = v.iterator();
paginator.init(total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}
%>
<table class="percent98" width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td align="right"><lt:Label res='res.flow.Flow' key='matchRecord'/> <b><%=paginator.getTotal() %></b> <lt:Label res='res.flow.Flow' key='article'/>　<lt:Label res='res.flow.Flow' key='perPage'/> <b><%=paginator.getPageSize() %></b> <lt:Label res='res.flow.Flow' key='article'/>　<lt:Label res='res.flow.Flow' key='page'/> <b><%=curpage %>/<%=totalpages %></b></td>
  </tr>
</table>
<table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
  <tr align="center">
    <td class="tabStyle_1_title" width="39%"><lt:Label res='res.flow.Flow' key='name'/></td>
    <td class="tabStyle_1_title" width="14%"><lt:Label res='res.flow.Flow' key='creator'/></td>
    <td class="tabStyle_1_title" width="13%"><lt:Label res='res.flow.Flow' key='type'/></td>
    <td class="tabStyle_1_title" width="16%"><lt:Label res='res.flow.Flow' key='startTime'/></td>
    <td class="tabStyle_1_title" width="18%"><lt:Label res='res.flow.Flow' key='operate'/></td>
  </tr>
  <%	
	  	int i = 0;
		UserDb user = new UserDb();
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long id = fdao.getId();
		%>
  <tr align="center">
    <td width="39%" align="left"><a target="_blank" href="project_show.jsp?projectId=<%=id%>&id=<%=id%>&formCode=<%=formCode%>"><%=fdao.getFieldValue("name")%></a></td>
    <td width="14%" align="center">
    <%
	user = user.getUserDb(fdao.getCreator());
	out.print(user.getRealName());
	%>
    </td>
    <td width="13%" align="center"><%=fdao.getFieldValue("prj_type")%></td>
    <td width="16%" align="center"><%=fdao.getFieldValue("begin_date")%></td>
    <td width="18%"><a href="javascript:sel('<%=fdao.getId()%>', '<%=fdao.getFieldValue("name")%>')"><lt:Label res='res.flow.Flow' key='choose'/></a>&nbsp;&nbsp; </td>
  </tr>
  <%
		}
%>
</table>
<table class="percent98" width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
  <tr>
    <td height="23" align="right">
    <%
	out.print(paginator.getCurPageBlock("?"+querystr));
	%>    </td>
  </tr>
</table>
<br>
</body>
</html>
