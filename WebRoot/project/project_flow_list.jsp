<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = StrUtil.getNullString(request.getParameter("op"));
String typeCode = ParamUtil.get(request, "typeCode");
String title = ParamUtil.get(request, "title");

try {	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "typeCode", typeCode, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
	
String myname = ParamUtil.get(request, "userName");
if(myname.equals("")){
	myname = privilege.getUser(request);
}
if (!myname.equals(privilege.getUser(request))) {
	if (!(privilege.canAdminUser(request, myname))) {
		out.print(StrUtil.Alert_Back(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

long projectId = ParamUtil.getLong(request, "projectId", -1);
if (projectId==-1) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id") + " projectId=" + projectId));
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>项目中的流程</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script src="../inc/common.js"></script>
<script>
function onTypeCodeChange(obj) {
	if(obj.options[obj.selectedIndex].value=='not'){
		alert(obj.options[obj.selectedIndex].text+' 不能被选择！'); 
		return false;
	}
	window.location.href = "project_flow_list.jsp?op=search&projectId=<%=projectId%>&typeCode=" + obj.options[obj.selectedIndex].value;
}
</script>
</head>
<body>
<%@ include file="prj_inc_menu_top.jsp"%>
<script>
$("menu4").className="current"; 
</script>
<div class="spacerH"></div>
<div align="center">
<table border="0" cellspacing="0" cellpadding="0">
<form name="formSearch" action="project_flow_list.jsp" method="get">
	<tr>
		<td>类型&nbsp;&nbsp;</td>
		<td>
			<select id="typeCode" name="typeCode" onChange="onTypeCodeChange(this)">
				<option value="">不限</option>
<%
Leaf lf = new Leaf();
lf = lf.getLeaf("root");
DirectoryView dv = new DirectoryView(lf);
dv.ShowDirectoryAsOptions(request, out, lf, 1);
%>
			</select>
			<input name="projectId" value="<%=projectId%>" type="hidden" />
		</td>
		<td>&nbsp;&nbsp;标题&nbsp;&nbsp;</td>
		<td>
			<input name="title" value="<%=title%>">
			<input name="userName" value="<%=myname%>" type="hidden">
			<input name="op" value="search" type="hidden">
			<input name="submit" class="btn" type=submit value="搜索">
			&nbsp;
			<input type="button" class="btn" onclick="window.location.href='../flow_initiate1.jsp?projectId=<%=projectId%>'" value="发起流程" />
<script>
$("typeCode").value = "<%=typeCode%>";
</script>
		</td>
	</tr>
</form>
</table>
</div>
<%
String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
if (strcurpage.equals(""))
	strcurpage = "1";
if (!StrUtil.isNumeric(strcurpage)) {
	out.print(StrUtil.makeErrMsg("标识非法！"));
	return;
}
int pagesize = 20;
int curpage = Integer.parseInt(strcurpage);

WorkflowDb wf = new WorkflowDb();

String sql = "select distinct id from flow where project_id=" + projectId + " and status<>" + WorkflowDb.STATUS_NONE; 
if (op.equals("search")) {
	sql = "select distinct id from flow where project_id=" + projectId + " and status<>" + WorkflowDb.STATUS_NONE;
	if (!typeCode.equals("")) {
		sql += " and type_code=" + StrUtil.sqlstr(typeCode);
	}
	if (!title.equals("")) {
		sql += " and title like " + StrUtil.sqlstr("%" + title + "%");
	}	
}
sql += " order by id desc";

ListResult lr = wf.listResult(sql, curpage, pagesize);

// ListResult lr = wf.listUserAttended(privilege.getUser(request), curpage, pagesize);
int total = lr.getTotal();
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
<table width="92%" border="0" align="center" class="p9">
  <tr>
    <td height="20" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b></td>
  </tr>
</table>
<table width="98%" align="center" class="tabStyle_1 percent98">
  <tbody>
    <tr>
      <td width="33%" class="tabStyle_1_title">标题</td>
      <td width="17%" class="tabStyle_1_title">类型</td>
      <td width="11%" class="tabStyle_1_title">开始时间</td>
      <td width="10%" class="tabStyle_1_title">发起人</td>
      <td width="10%" class="tabStyle_1_title">最后办理</td>
      <td width="9%" class="tabStyle_1_title">状态</td>
      <td width="10%" class="tabStyle_1_title">管理</td>
    </tr>
    <%
Leaf ft = new Leaf();
MyActionDb mad = new MyActionDb();
com.redmoon.oa.person.UserMgr um = new com.redmoon.oa.person.UserMgr();
while (ir.hasNext()) {
 	WorkflowDb wfd = (WorkflowDb)ir.next(); 
	UserDb user = null;
	if (wfd.getUserName()!=null)
		user = um.getUserDb(wfd.getUserName());
	String userRealName = "";
	if (user!=null)
		userRealName = user.getRealName();	
	%>
    <tr class="highlight">
      <td><a href="../flow_modify.jsp?flowId=<%=wfd.getId()%>" title="<%=wfd.getTitle()%>"><%=StrUtil.getLeft(wfd.getTitle(), 40)%></a></td>
      <td>
	  <%
	  lf = ft.getLeaf(wfd.getTypeCode());
	  %>
	  <%if (lf!=null) {%>
	  	<a href="project_flow_list.jsp?op=search&projectId=<%=projectId%>&typeCode=<%=StrUtil.UrlEncode(lf.getCode())%>"><%=lf.getName()%></a>
	  <%}%>	  </td>
      <td align="center"><%=DateUtil.format(wfd.getBeginDate(), "yy-MM-dd HH:mm")%></td>
      <td><%=userRealName%></td>
      <td>
	  <%
		sql = "select id from flow_my_action where flow_id=" + wfd.getId() + " order by receive_date desc";
		Iterator ir2 = mad.listResult(sql, 1, 1).getResult().iterator();
	  	if (ir2.hasNext()) {
			mad = (MyActionDb)ir2.next();
		%>
		<%=um.getUserDb(mad.getUserName()).getRealName()%>
		<%
		}
	  %>
	  </td>
      <td><%=wfd.getStatusDesc()%></td>
      <td align="center"><a href="../flow_modify.jsp?flowId=<%=wfd.getId()%>" title="查看流程进度、附言、修改流程标题等">查看详情</a></td>
    </tr>
<%}%>
  </tbody>
</table>
<table width="96%"  border="0" align="center" cellpadding="0" cellspacing="0">
  
  <tr>
    <td align="right"><%
	String querystr = "op="+op+"&projectId=" + projectId + "&userName=" + StrUtil.UrlEncode(myname) + "&typeCode=" + typeCode + "&title=" + StrUtil.UrlEncode(title);
    out.print(paginator.getCurPageBlock("project_flow_list.jsp?"+querystr));
%></td>
  </tr>
</table>
</body>
</html>