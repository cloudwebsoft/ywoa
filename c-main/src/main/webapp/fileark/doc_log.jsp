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
int doc_id = ParamUtil.getInt(request, "id", -1);
if (doc_id==-1) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id") + " doc_id=" + doc_id));
	return;
}
String title = ParamUtil.get(request, "title");
String name = ParamUtil.get(request, "name");
String beginDate = ParamUtil.get(request, "beginDate");  
String endDate = ParamUtil.get(request, "endDate");  

DocLogDb dld = new DocLogDb();
DocAttachmentLogDb dd = new DocAttachmentLogDb();

String sql="";

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "id";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
	
try {	
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "name", name, getClass().getName());

	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "name", name, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "title", title, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}	
	
if("chakan".equals(type) || "".equals(type)){
	sql = "select id from doc_log where doc_id = "+ doc_id;
}
if("xiazai".equals(type)){
	sql = "select id from doc_attachment_log where doc_id = "+ doc_id;
}
if(!"".equals(name)){
	if("chakan".equals(type) || "".equals(type)){
		sql = "select l.id from doc_log l, users u where l.user_name=u.name and l.doc_id = " + doc_id;
	}
	if("xiazai".equals(type)){
		sql = "select a.id from doc_attachment_log a, users u where a.user_name=u.name and a.doc_id = " + doc_id;
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
%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>查看日志</title>
	<%@ include file="../inc/nocache.jsp" %>
	<script type="text/javascript" src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css"/>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
	<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
	<style>
		.search-form input, select {
			vertical-align: middle;
		}

		.search-form input:not([type="radio"]):not([type="button"]) {
			width: 80px;
			line-height: 20px; /*否则输入框的文字会偏下*/
		}
	</style>
	<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
	<script src="../js/datepicker/jquery.datetimepicker.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	<script type="text/javascript" src="../js/flexigrid.js"></script>
</head>
<body>
<%@ include file="doc_log_menu_top.jsp"%>
<script>
<%
if(type.equals("chakan")||"".equals(type)){%>
o("menu1").className="current";
<%}else if(type.equals("xiazai")){%>
o("menu2").className="current";
<%}
%>
</script>
<%
ListResult lr = null;
if(type.equals("chakan")||"".equals(type)){
	lr = dld.listResult(sql,curpage,pagesize);
}
if("xiazai".equals(type)){
	lr = dd.listResult(sql,curpage,pagesize);
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
			<td align="center">
			<form action="doc_log.jsp?type=<%=type %>" class="search-form" method="get">
			<input id="op" name="op" value="search" type="hidden" />
			<input id="type" name="type" value="<%=type %>" type="hidden" />
			<input id="id" name="id" value="<%=doc_id %>" type="hidden" />
			<input id="title" name="title" value="<%=title %>" type="hidden" />
			&nbsp;用户&nbsp;
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
        <% 
        if(type.equals("chakan")||"".equals(type)){%>
			  <th width="347" name="title" >标题</th> 
	          <th width="122" name="name" >操作用户</th> 
	          <th width="128" name="ip" >用户IP</th>
	          <th width="145" name="log_date" >查看日期</th> 
         <% }
         if("xiazai".equals(type)){%>
         	  <th width="347" name="title" >标题</th> 
	          <th width="122" name="name" >操作用户</th> 
	          <th width="128" name="ip" >用户IP</th>
	          <th width="145" name="log_date" >下载日期</th>
         <% }%>
        </tr>
      </thead>
      <tbody>
<%
while(iterator.hasNext()) {
	DocLogDb dldb = null;
	DocAttachmentLogDb dad = null;
	if(type.equals("chakan")||"".equals(type)){
		dldb = (DocLogDb)iterator.next();
	}
	if("xiazai".equals(type)){
		dad = (DocAttachmentLogDb)iterator.next();
	}
	if("chakan".equals(type) ||"".equals(type)){
%>
        <tr >
		  <td>
		   <%=title %>
		  </td>
		  <%
		  	UserDb udb = new UserDb();
		  	udb =udb.getUserDb(dldb.getUserName());
		  %>
          <td><%=udb.getRealName()%></td> 
          <td><%=dldb.getIp() %></td>
          <td><%=DateUtil.format(dldb.getLogDate(),"yyyy-MM-dd HH:mm:ss") %></td>
        </tr>
<%}if("xiazai".equals(type)){ %>
		<tr >
		  <td>
		   <%=title %>
		  </td>
          <td><%=dad.getUserName()%></td> 
          <td><%=dad.getIp() %></td>
          <td><%=DateUtil.format(dad.getLogDate(),"yyyy-MM-dd HH:mm:ss") %></td>
        </tr>
<%
}
}
%>
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
	window.location.href = "doc_log.jsp?id=<%=doc_id %>&type=<%=type %>&title=<%=title %>&<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "doc_log.jsp?id=<%=doc_id %>&type=<%=type %>&title=<%=title %>&<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "doc_log.jsp?id=<%=doc_id %>&type=<%=type %>&title=<%=title %>&<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
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
			// {name: '删除', bclass: 'delete', onpress : action},
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
		checkbox: false,
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
	if (com=='添加')	{
		window.location.href = "info_add.jsp?type=<%=type%>";
	}
	else if (com=='修改') {
		selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
		if (selectedCount == 0) {
			jAlert('请选择一条记录!','提示');
			return;
		}
		if (selectedCount > 1) {
			jAlert('只能选择一条记录!','提示');
			return;
		}
		
		var id = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).val();
		window.location.href = "info_edit.jsp?type=<%=type%>&id=" + id;
	}
	else if (com=='删除') {
		selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
		if (selectedCount == 0) {
			jAlert('请选择一条记录!','提示');
			return;
		}

		jConfirm("您确定要删除么？","提示",function(r){
			if(r) {
				var ids = "";
				$(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function(i) {
					if (ids=="")
						ids = $(this).val();
					else
						ids += "," + $(this).val();
				});	
				window.location.href='doc_log.jsp?op=del&CPages=<%=curpage%>&id=<%=doc_id%>&ids=' + ids;
			}
		})
	}
}
</script>
</body>
</html>