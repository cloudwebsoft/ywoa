<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@page import="com.redmoon.oa.pvg.Privilege"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));

String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action"); // action为manage时表示为销售总管理员方式
if (action.equals("manage")) {
	if (!privilege.isUserPrivValid(request, "sales")) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

long linkmanId = ParamUtil.getLong(request, "linkmanId");

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "visit_date";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>行动记录</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script>
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "linkman_visit_list.jsp?op=<%=op%>&action=<%=action%>&linkmanId=<%=linkmanId%>&orderBy=" + orderBy + "&sort=" + sort;
}
</script>
</head>
<body>
<%
String priv = "sales.user";
if (!privilege.isUserPrivValid(request, priv) && !privilege.isUserPrivValid(request, "sales")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

request.setAttribute("isShowVisitTag", "true");
%>
<%if (op.equals("listOfCustomer")) {%>
<%@ include file="customer_inc_nav.jsp"%>
<script>
o("menu3").className="current"; 
</script>
<%}else{%>
<%@ include file="linkman_inc_menu_top.jsp"%>
<script>
o("menu4").className="current"; 
</script>
<%}%>
<div class="spacerH"></div>
<%
String formCode = "day_lxr";
FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
String sql = "select id from " + fd.getTableNameByForm() + " where lxr=" + linkmanId; //  + " and is_visited='是'";
sql += " order by " + orderBy + " " + sort;

int pagesize = 30;
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
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}
%>
<table class="percent98" width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td width="47" height="28">
    <%if (op.equals("listOfCustomer")) {%>
    <input type="button" class="btn" value="添加" onclick="window.location.href='linkman_visit_add.jsp?op=<%=op%>&action=<%=StrUtil.UrlEncode(action)%>&formCode=<%=StrUtil.UrlEncode(formCode)%>&customerId=<%=ParamUtil.getLong(request, "customerId")%>&linkmanId=<%=linkmanId%>'" />
    <%}else{%>
    <input type="button" class="btn" value="添加" onclick="window.location.href='linkman_visit_add.jsp?op=<%=op%>&action=<%=StrUtil.UrlEncode(action)%>&formCode=<%=StrUtil.UrlEncode(formCode)%>&linkmanId=<%=linkmanId%>'" />
    <%}%>
    </td>
    <td align="right" backgroun="images/title1-back.gif">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b></td>
  </tr>
</table>
<table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
  <tr align="center">
    <td class="tabStyle_1_title" width="14%">联系人	</td>
    <td class="tabStyle_1_title" width="13%">电话</td>
    <td class="tabStyle_1_title" width="11%" style="cursor:pointer" onClick="doSort('contact_type')">方式
      <%if (orderBy.equals("contact_type")) {
		if (sort.equals("asc")) 
			out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
		else
			out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
	}%>		</td>
    <td class="tabStyle_1_title" width="20%" style="cursor:pointer" onClick="doSort('visit_date')">拜访日期
    <%if (orderBy.equals("visit_date")) {
		if (sort.equals("asc")) 
			out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
		else
			out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
	}%>	</td>
    <td class="tabStyle_1_title" width="20%">联系结果</td>
    <td class="tabStyle_1_title" width="9%" style="cursor:pointer" onClick="doSort('is_visited')">已联系
    <%if (orderBy.equals("is_visited")) {
		if (sort.equals("asc")) 
			out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
		else
			out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
	}%>
    </td>
    <td class="tabStyle_1_title" width="13%">操作</td>
  </tr>
  <%	
	  	int i = 0;
		FormDb fdLinkman = new FormDb();
		fdLinkman = fdLinkman.getFormDb("sales_linkman");
		FormDAO fdaoLinkman = new FormDAO();
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long myid = fdao.getId();
			fdaoLinkman = fdaoLinkman.getFormDAO(StrUtil.toInt(fdao.getFieldValue("lxr")), fdLinkman);
		%>
  <tr align="center">
    <td width="14%" align="left"><a target="_blank" href="../visual/module_show.jsp?id=<%=myid%>&amp;action=<%=action%>&formCode=<%=formCode%>&code=<%=formCode%>&isShowNav=0"><%=fdaoLinkman.getFieldValue("linkmanName")%></a></td>
    <td width="13%" align="left">&nbsp;</td>
    <td width="11%" align="left"><%=fdao.getFieldValue("contact_type")%></td>
    <td width="20%" align="left"><%=fdao.getFieldValue("visit_date")%></td>
    <td width="20%" align="left"><%=fdao.getFieldValue("contact_result")%></td>
    <td width="9%" align="left"><%=fdao.getFieldValue("is_visited")%></td>
    <td width="13%"><a href="javascript:;" onclick="addTab('<%=fdaoLinkman.getFieldValue("linkmanName")%>', '<%=request.getContextPath()%>/sales/linkman_visit_edit.jsp?id=<%=myid%>&amp;action=<%=action%>&amp;formCode=<%=StrUtil.UrlEncode(formCode)%>&isShowNav=0')">编辑</a>
    <%
    ModulePrivDb mpd = new ModulePrivDb(formCode);
    if(mpd.canUserManage(new Privilege().getUser(request))) { %>
    &nbsp;&nbsp;&nbsp;<a onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{window.location.href='../visual_del.jsp?id=<%=myid%>&amp;action=<%=action%>&formCode=<%=formCode%>&amp;privurl=<%=StrUtil.getUrl(request)%>'}})" style="cursor:pointer">删除</a></span> </td>
  	<%} %>
  </tr>
  <%
		}
%>
</table>
<table class="percent98" width="98%"  border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td width="48%" height="30" align="left">&nbsp;</td>
    <td width="52%" align="right"><%
			out.print(paginator.getCurPageBlock("?action=" + action));
			%></td>
  </tr>
</table>
<br />
</body>
</html>
