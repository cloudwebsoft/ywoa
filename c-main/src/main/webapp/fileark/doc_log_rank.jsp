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
if (skincode==null || skincode.equals("")) {
	skincode = UserSet.defaultSkin;
}
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();

//String userName = privilege.getUser(request);
int curpage = ParamUtil.getInt(request, "CPages", 1);
String op = ParamUtil.get(request, "op");

String beginDate = ParamUtil.get(request, "beginDate");  
String endDate = ParamUtil.get(request, "endDate");  

DocLogDb dld = new DocLogDb();

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals("")) {
	orderBy = "id";
}
String sort = ParamUtil.get(request, "sort");
if (sort.equals("")) {
	sort = "desc";
}
	
String sql = "SELECT doc_id,count(doc_id) as countName FROM doc_log l where 1=1";

if(!"".equals(beginDate)){
	sql += " and log_date >="+ SQLFilter.getDateStr(beginDate+" 00:00:00", "yyyy-MM-dd HH:mm:ss");
}
if(!"".equals(endDate)){
	sql += " and log_date <="+ SQLFilter.getDateStr(endDate+" 23:59:59", "yyyy-MM-dd HH:mm:ss");
}

sql += " GROUP BY l.user_name ORDER BY countName DESC";
// System.out.println(getClass() + " sql=" + sql);
int pagesize = ParamUtil.getInt(request, "pageSize", 20);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>文件查看排行</title>
	<%@ include file="../inc/nocache.jsp" %>
	<script type="text/javascript" src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
	<script src="../js/datepicker/jquery.datetimepicker.js"></script>
	<script type="text/javascript" src="../js/flexigrid.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css"/>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
</head>
<body>

<%@ include file="doc_log_list_menu_top.jsp"%>
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
			<td width="48%" height="30" align="left">
			<form action="doc_log_rank.jsp" method="get">
			<input id="op" name="op" value="search" type="hidden" />
&nbsp;日期从&nbsp;<span class="TableData">
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
          <th width="291" name="name" >标题</th> 
          <th width="213" name="log_date" >次数</th> 
        </tr>
      </thead>
      <tbody>
<%
Document doc = new Document();
while(ri.hasNext()) {
	ResultRecord rr = (ResultRecord)ri.next();
	int docId = rr.getInt(1);
	int count = rr.getInt(2);
	
	doc = doc.getDocument(docId);
	if (doc==null) {
		doc = new Document();
		continue;
	}
%>
        <tr >
		  <td><a href="javascript:;" onclick="addTab('查看日志', '<%=request.getContextPath()%>/fileark/doc_log.jsp?id=<%=doc.getId()%>&title=<%=StrUtil.UrlEncode(doc.getTitle())%>')"><%=doc.getTitle()%></a></td> 
          <td><%=count%></td>
        </tr>
<%}%>
	</tbody>
</table>  
<%
	String querystr = "op=" + op + "&beginDate=" + beginDate + "&endDate=" + endDate;
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
	window.location.href = "doc_log_rank.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "doc_log_rank.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "doc_log_rank.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
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