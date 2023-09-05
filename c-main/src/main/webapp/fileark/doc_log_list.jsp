<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.fileark.*"%>
<%
com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
if (!privilege.isUserPrivValid(request, "admin")) {
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
if (type.equals(""))
	type = "doc";
String name = ParamUtil.get(request, "name");
String beginDate = ParamUtil.get(request, "beginDate");
String endDate = ParamUtil.get(request, "endDate");
String title = ParamUtil.get(request, "title");
String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "id";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "name", name, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "title", title, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "orderBy", orderBy, getClass().getName());
	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "name", name, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "title", title, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "beginDate", beginDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "endDate", endDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "type", type, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "orderBy", orderBy, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

DocLogDb dld = new DocLogDb();
DocAttachmentLogDb dd = new DocAttachmentLogDb();

%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>查看日志</title>
	<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
	<style>
		.search-form input,select {
			vertical-align:middle;
		}
		.search-form input:not([type="radio"]):not([type="button"]):not([type="checkbox"]) {
			width: 80px;
			line-height: 20px; /*否则输入框的文字会偏下*/
		}
		.cond-title {
			margin: 0 5px;
		}
	</style>
	<%@ include file="../inc/nocache.jsp" %>
	<script type="text/javascript" src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
	<script src="../js/datepicker/jquery.datetimepicker.js"></script>
	<script type="text/javascript" src="../js/flexigrid.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css"/>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
</head>
<body>
<%@ include file="doc_log_list_menu_top.jsp"%>
<%
if (op.equals("del")) {
	long id = ParamUtil.getLong(request, "id");
	if (type.equals("doc")) {
		dld = dld.getDocLogDb(id);
		if (dld.del()) {
			out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "doc_log_list.jsp?type=" + type + "&CPages=" + curpage));
		}
		else {
			out.print(StrUtil.jAlert_Back("操作失败！","提示"));
		}
	}
	else {
		dd = dd.getDocAttachmentLogDb(id);		
		if (dd.del()) {
			out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "doc_log_list.jsp?type=" + type + "&CPages=" + curpage));
		}
		else {
			out.print(StrUtil.jAlert_Back("操作失败！","提示"));
		}
	}
	return;
}
else if (op.equals("delBatch")) {
	if (type.equals("doc")) {
		String strIds = ParamUtil.get(request, "ids");
		String[] ids = StrUtil.split(strIds, ",");
		for (int i=0; i<ids.length; i++) {
			dld = dld.getDocLogDb(StrUtil.toInt(ids[i]));
			dld.del();
		}
		out.print(StrUtil.jAlert_Redirect("操作成功！", "提示",  "doc_log_list.jsp?type=" + type + "&CPages=" + curpage));
	}
	else {
		String strIds = ParamUtil.get(request, "ids");
		String[] ids = StrUtil.split(strIds, ",");
		for (int i=0; i<ids.length; i++) {
			dd = dd.getDocAttachmentLogDb(StrUtil.toInt(ids[i]));
			dd.del();
		}
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "doc_log_list.jsp?type=" + type + "&CPages=" + curpage));
	}
	return;
}

String sql="";

if("doc".equals(type) || "".equals(type)){
	sql = "select id from doc_log where 1=1";
	if (!title.equals(""))
		sql = "select l.id from doc_log l, document d where l.doc_id=d.id and d.title like " + StrUtil.sqlstr("%" + title + "%");
}
if("att".equals(type)){
	sql = "select id from doc_attachment_log where 1=1";
	if (!title.equals(""))
		sql = "select l.id from doc_attachment_log l, document d where l.doc_id=d.id and d.title like " + StrUtil.sqlstr("%" + title + "%");
}

if(!"".equals(name)){
	if("doc".equals(type) || "".equals(type)) {
		if (!title.equals(""))
			sql = "select l.id from doc_log l, document d, users u where l.doc_id=d.id and l.user_name=u.name and d.title like " + StrUtil.sqlstr("%" + title + "%");
		else
			sql = "select l.id from doc_log l, users u where l.user_name=u.name";
	}
	if("att".equals(type)){
		if (!title.equals(""))
			sql = "select l.id from doc_attachment_log l, document d, users u where l.doc_id=d.id and l.user_name=u.name and d.title like " + StrUtil.sqlstr("%" + title + "%");
		else
			sql = "select a.id from doc_attachment_log a, users u where a.user_name=u.name";
	}
	sql += " and u.realname like " + StrUtil.sqlstr("%" + name + "%");
}
if(!"".equals(beginDate)){
	sql += " and log_date >="+ SQLFilter.getDateStr(beginDate+" 00:00:00", "yyyy-MM-dd HH:mm:ss");
}
if(!"".equals(endDate)){
	sql += " and log_date <="+ SQLFilter.getDateStr(endDate+" 23:59:59", "yyyy-MM-dd HH:mm:ss");
}

sql += " order by " + orderBy + " " + sort;
int pagesize = ParamUtil.getInt(request, "pageSize", 20);

if(type.equals("doc")){%>
<script>
o("menu1").className="current";
</script>
<%}else if(type.equals("att")){%>
<script>
o("menu2").className="current";
</script>
<%}
%>
<script>
function selAllCheckBox(checkboxname) {
	var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = true;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = true;
		}
	}
}

function clearAllCheckBox(checkboxname) {
	var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = false;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = false;
		}
	}
}
</script>

<% 
ListResult lr = null;
if(type.equals("doc")){
	lr = dld.listResult(sql, curpage, pagesize);
}
if("att".equals(type)){
	lr = dd.listResult(sql, curpage, pagesize);
}
Iterator iterator = lr.getResult().iterator();
long total = lr.getTotal();
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
			<td width="48%" height="30" align="left">
			<form action="doc_log_list.jsp?type=<%=type %>" class="search-form" method="get">
			<input id="op" name="op" value="search" type="hidden" />
			<input id="type" name="type" value="<%=type %>" type="hidden" />
			&nbsp;&nbsp;
            标题&nbsp;
			<input id="title" name="title" size="15" value="<%=title%>" />
            用户&nbsp;
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
        		<th width="30"><input type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else clearAllCheckBox('ids')" /></th>
            <th width="347" name="title" >标题</th> 
            <th width="122" name="name" >用户</th> 
            <th width="128" name="ip" >IP</th>
            <th width="145" name="log_date" >日期</th>
            <th width="100" name="log_date" >操作</th> 
        </tr>
      </thead>
      <tbody>
<%
Document doc = new Document();
UserDb user = new UserDb();
while(iterator.hasNext()) {
	DocLogDb dldb = null;
	DocAttachmentLogDb dad = null;
	if(type.equals("doc")||"".equals(type)){
		dldb = (DocLogDb)iterator.next();
		user = user.getUserDb(dldb.getUserName());
		doc = doc.getDocument(dldb.getDoc_id());
		if (doc==null)
			doc = new Document();
	}
	if("att".equals(type)){
		dad = (DocAttachmentLogDb)iterator.next();
		user = user.getUserDb(dad.getUserName());
		doc = doc.getDocument(dad.getDoc_id());
		if (doc==null)
			doc = new Document();
	}
	if(type.equals("doc")||"".equals(type)){
%>
        <tr id="<%=dldb.getId()%>">
        	<td><input type="checkbox" name="ids" value="<%=dldb.getId()%>" /></td>
		  <td>
		   <%=doc.getTitle()%>
		  </td>
          <td><%=user.getRealName()%></td> 
          <td><%=dldb.getIp() %></td>
          <td><%=DateUtil.format(dldb.getLogDate(),"yyyy-MM-dd HH:mm:ss") %></td>
          <td><a href="javascript:;" onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{window.location.href='doc_log_list.jsp?op=del&id=<%=dldb.getId()%>&type=doc&CPages=<%=curpage%>'}}) " style="cursor:pointer">删除</a></td>
        </tr>
<%} else if("att".equals(type)){ %>
		<tr id="<%=dad.getId()%>">
			<td><input type="checkbox" name="ids" value="<%=dad.getId()%>" /></td>
		  <td>
		   <%=doc.getTitle()%>
		  </td>
          <td><%=user.getRealName()%></td> 
          <td><%=dad.getIp() %></td>
          <td><%=DateUtil.format(dad.getLogDate(),"yyyy-MM-dd HH:mm:ss") %></td>
          <td><a href="javascript:;" onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{window.location.href='doc_log_list.jsp?op=del&id=<%=dad.getId()%>&type=att&CPages=<%=curpage%>'}}) " style="cursor:pointer">删除</a></td>          
        </tr>
<%
}
}
%>
	</tbody>
</table>  
<%
	String querystr = "op=" + op + "&name=" + StrUtil.UrlEncode(name) + "&beginDate=" + beginDate + "&endDate=" + endDate + "&title=" + StrUtil.UrlEncode(title);
	// out.print(paginator.getPageBlock(request,"notice_list.jsp?"+querystr));
%>
<script>
function initCalendar() {
	$('#beginDate').datetimepicker({
		lang:'ch',
		timepicker:false,
		format:'Y-m-d'
	});
	$('#endDate').datetimepicker({
	    lang:'ch',
	    timepicker:false,
	    format:'Y-m-d'
	});
}

function doOnToolbarInited() {
	initCalendar();
}

var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "doc_log_list.jsp?type=<%=type %>&<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "doc_log_list.jsp?type=<%=type %>&<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "doc_log_list.jsp?type=<%=type %>&<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
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
			{name: '删除', bclass: 'delete', onpress : action},
			{separator: true},	
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
	if (com=='删除') {
		var ids = getCheckboxValue("ids");
		if (ids=="") {
			jAlert('请选择一条记录!','提示');
			return;
		}

		jConfirm("您确定要删除么？","提示",function(r){
			if(!r){return;}
			else{
				window.location.href='doc_log_list.jsp?op=delBatch&type=<%=type%>&CPages=<%=curpage%>&ids=' + ids;
			}
		});
	}
}
</script>
</body>
</html>