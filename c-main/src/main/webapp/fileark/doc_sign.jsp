<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.fileark.*"%>
<%
com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
if (!privilege.isUserPrivValid(request, "read")) {
    out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();

//String userName = privilege.getUser(request);
int curpage = ParamUtil.getInt(request, "CPages", 1);
String op = ParamUtil.get(request, "op");

String type = ParamUtil.get(request, "type");
String doc_id = ParamUtil.get(request, "id");
if ("".equals(doc_id) || StrUtil.toInt(doc_id, -1)==-1) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id") + " id=" + doc_id));
	return;	
}
String title = ParamUtil.get(request, "title");
String name = ParamUtil.get(request, "name");
String beginDate = ParamUtil.get(request, "beginDate");
String endDate = ParamUtil.get(request, "endDate");

try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "id", doc_id, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "name", name, getClass().getName());
	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "type", type, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "title", title, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

DocLogDb dld = new DocLogDb();

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "id";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
String sql = "SELECT user_name,count(user_name) as countName FROM doc_log l where doc_id=" + doc_id;
if(!"".equals(name)){
	sql = "SELECT l.user_name,count(l.user_name) as countName FROM doc_log l, users u where l.user_name=u.name and l.doc_id=" + doc_id;

	sql += " and u.realname like "+ StrUtil.sqlstr("%" + name + "%");
}

if(!"".equals(beginDate)){
	sql += " and log_date >="+ SQLFilter.getDateStr(beginDate+" 00:00:00", "yyyy-MM-dd HH:mm:ss");
}
if(!"".equals(endDate)){
	sql += " and log_date <="+ SQLFilter.getDateStr(endDate+" 23:59:59", "yyyy-MM-dd HH:mm:ss");
}

sql += " GROUP BY l.user_name ORDER BY countName DESC";
int pagesize = ParamUtil.getInt(request, "pageSize", 20);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<title>签读</title>
<%@ include file="../inc/nocache.jsp"%>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
	<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
	<style>
		.search-form input,select {
			vertical-align:middle;
		}
		.search-form input:not([type="radio"]):not([type="button"]) {
			width: 80px;
			line-height: 20px; /*否则输入框的文字会偏下*/
		}
	</style>
<!-- 
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
-->
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
</head>
<body>

<%@ include file="doc_log_menu_top.jsp"%>
<script>
o("menu3").className="current";
</script>
<%
JdbcTemplate jt = new JdbcTemplate();
ResultIterator ri = jt.executeQuery(sql,curpage,pagesize);
long total = ri.getTotal();
Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}
%>
	<table id="searchTable" width="100%" border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td align="center">
			<form action="doc_sign.jsp?type=<%=type %>" class="search-form" method="get">
			<input id="op" name="op" value="search" type="hidden" />
			<input id="type" name="type" value="<%=type %>" type="hidden" />
			<input id="id" name="id" value="<%=doc_id %>" type="hidden" />
			<input id="title" name="title" value="<%=title %>" type="hidden" />
			&nbsp;用户名&nbsp;
			<input id="name" name="name" size="15" value="<%=name%>" />
			&nbsp;&nbsp;日期从&nbsp;<span class="TableData">
            <input type="text" id="beginDate" name="beginDate" size="10" value="<%=beginDate %>" />
            至
            <input type="text" id="endDate" name="endDate" size="10" value="<%=endDate %>" />
            </span>
			&nbsp;
          <input class="tSearch" name="submit" type=submit value="搜索">
			</form>
		  </td>
		</tr>
	</table>
      <table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
      <thead>
        <tr>
          <th width="291" name="name" >用户</th> 
          <th width="213" name="log_date" >次数</th> 
        </tr>
      </thead>
      <tbody>
<%
UserDb user = new UserDb();
while(ri.hasNext()) {
	ResultRecord rr = (ResultRecord)ri.next();
	String userName = rr.getString(1);
	int count = rr.getInt(2);
	
	user = user.getUserDb(userName);
%>
        <tr >
		  <td><a href="doc_log.jsp?id=<%=doc_id%>&op=search&name=<%=StrUtil.UrlEncode(user.getRealName())%>"><%=user.getRealName()%></a></td> 
          <td><%=count%></td>
        </tr>
<%}%>
	</tbody>
</table>  
<%
	String querystr = "op=" + op + "&name=" + StrUtil.UrlEncode(name) + "&beginDate=" + beginDate + "&endDate=" + endDate;
	// out.print(paginator.getPageBlock(request,"notice_list.jsp?"+querystr));
%>
<script>
function initCalendar() {
	$('#beginDate').datetimepicker({
     	lang:'ch',
     	timepicker:false,
     	format:'Y-m-d',
     	formatDate:'Y/m/d'
	});
	$('#endDate').datetimepicker({
		lang:'ch',
		timepicker:false,
        format:'Y-m-d',
		formatDate:'Y/m/d'
	});
}

function doOnToolbarInited() {
	initCalendar();
}

var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "doc_sign.jsp?id=<%=doc_id %>&type=<%=type %>&title=<%=title %>&<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "doc_sign.jsp?id=<%=doc_id %>&type=<%=type %>&title=<%=title %>&<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "doc_sign.jsp?id=<%=doc_id %>&type=<%=type %>&title=<%=title %>&<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}
$(document).ready(function() {
	flex = $("#grid").flexigrid
	(
		{
		buttons : [
	
			//{name: '添加', bclass: 'add', onpress : action},
			//{name: '修改', bclass: 'edit', onpress : action},
			//{name: '删除', bclass: 'delete', onpress : action},
	
			{name: '条件', bclass: '', type: 'include', id: 'searchTable'}
			],
		/*
		searchitems : [
			{display: 'ISO', name : 'iso'},
			{display: 'Name', name : 'name', isdefault: true}
			],
		sortname: "iso",
		sortorder: "asc",
		*/
		url: false,
		usepager: true,
	
		checkbox : false,
	
		page: <%=curpage%>,
		total: <%=total%>,
		useRp: true,
		rp: <%=pagesize%>,
		
		// title: "通知",
		singleSelect: true,
		resizable: false,
		showTableToggleBtn: true,
		showToggleBtn: true,
		
		onChangeSort: changeSort,
		
		onChangePage: changePage,
		onRpChange: rpChange,
		onReload: onReload,
		/*
		onRowDblclick: rowDbClick,
		onColSwitch: colSwitch,
		onColResize: colResize,
		onToggleCol: toggleCol,
		*/
		onToolbarInited: doOnToolbarInited,
		autoHeight: true,
		width: document.documentElement.clientWidth,
		height: document.documentElement.clientHeight - 84
		}
	);
});

function action(com, grid) {
}
</script>
</body>
</html>