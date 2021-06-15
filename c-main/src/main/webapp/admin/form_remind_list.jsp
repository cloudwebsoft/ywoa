<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "admin";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String action = ParamUtil.get(request, "action");
String what = ParamUtil.get(request, "what");
String code = ParamUtil.get(request, "code"); // 表单编码

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "id";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String querystr = "action=" + action + "&what=" + StrUtil.UrlEncode(what) + "&code=" + code;

String op = ParamUtil.get(request, "op");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>到期列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script type="text/javascript" src="../js/flexigrid.js"></script>
<script src="../inc/common.js"></script>
<style>
	.search-form input,select {
		vertical-align:middle;
	}
	.search-form input:not([type="radio"]):not([type="button"]) {
		width: 80px;
		line-height: 20px; /*否则输入框的文字会偏下*/
	}
</style>
<style>
	.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
	-moz-opacity: 0.20;
	opacity: 0.20;
	z-index: 1500;
	}
	.treeBackground {
	display: none;
	position: absolute;
	top: -2%;
	left: 0%;
	width: 100%;
	margin: auto;
	height: 200%;
	background-color: #EEEEEE;
	z-index: 1800;
	-moz-opacity: 0.8;
	opacity: .80;
	filter: alpha(opacity = 80);
	}
</style>
</head>
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
if (op.equals("del")) {
	FormRemindDb frd = new FormRemindDb();
	String ids = ParamUtil.get(request, "ids");
	String[] ary = StrUtil.split(ids, ",");
	if (ary==null) {
		out.print(StrUtil.jAlert_Back("请选择记录！","提示"));
		return;
	}
	%>
	<script>
		jQuery("#treeBackground").addClass("SD_overlayBG2");
		jQuery("#treeBackground").css({"display":"block"});
		jQuery("#loading").css({"display":"block"});
	</script>
	<%
	for (int i=0; i<ary.length; i++) {
		frd = (FormRemindDb)frd.getQObjectDb(new Long(StrUtil.toLong(ary[i])));
		frd.del();
	}
	%>
	<script>
		jQuery("#loading").css({"display":"none"});
		jQuery("#treeBackground").css({"display":"none"});
		jQuery("#treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "form_remind_list.jsp?" + querystr));
	return;
}
if (!code.equals("")) {
%>
<%@ include file="form_edit_inc_menu_top.jsp"%>
<script>
o("menu4").className="current";
</script>
<%}%>
		<table id="searchTable"><tr><td>
          <form name="formSearch" class="search-form" action="form_remind_list.jsp" method="get">
			<input name="action" value="search" type="hidden" />
			  名称&nbsp;
              <input name=what size=10 value="<%=what%>">
              <input name="code" value="<%=code%>" type="hidden" />
              &nbsp;
              <input class="tSearch" name="submit" type=submit value="搜索">
          </form>
        </td></tr></table>
<%

		
		FormRemindDb frd = new FormRemindDb();

		String sql = "select id from " + frd.getTable().getName() + " where 1=1";
		if (!code.equals("")) {
			sql += " and table_name=" + StrUtil.sqlstr(FormDb.getTableName(code));
		}

		if (action.equals("search")) {
			sql += " and name like " + StrUtil.sqlstr("%" + what + "%");
		}
		sql += " order by " + orderBy + " " + sort;
		
		// out.print(sql);
		// System.out.println(getClass() + " sql=" + sql);
		
		String sortquerystr = querystr;
		querystr += "&orderBy=" + orderBy + "&sort=" + sort;
		
		int pagesize = 60;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
					
		ListResult lr = frd.listResult(sql, curpage, pagesize);
		long total = lr.getTotal();
		Vector v = lr.getResult();

		paginator.init(total, pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0) {
			curpage = 1;
			totalpages = 1;
		}
%>
<table width="944" id="grid">    
		<thead>
        <tr>
          <th width="49" style="cursor:pointer" abbr="id">ID</th>                  
          <th width="105" style="cursor:pointer" abbr="from_user">名称</th>		  
          <th width="109" style="cursor:pointer" abbr="to_user">类型</th>
          <th width="112" style="cursor:pointer" abbr="content">日期字段</th>
          <th width="145" style="cursor:pointer" abbr="create_date">提前</th>
          </tr>
   </thead>
   <tbody>
      <%	
	  	int i = 0;
		Iterator ir = lr.getResult().iterator(); 
		while (ir!=null && ir.hasNext()) {
			frd = (FormRemindDb)ir.next();
			i++;
			int id = frd.getInt("id");
		%>
        <tr id="<%=id%>">
          <td><%=id%></td>
          <td><%=frd.getString("name")%></td>
          <td align="center"><%=frd.getInt("kind")==FormRemindDb.KIND_EXPIRE?"到期":"周年"%></td>
          <td align="center"><%=frd.getString("date_field")%></td>
          <td align="center">
		  <%=frd.getInt("ahead_day")%>天
		  <%=frd.getInt("ahead_hour")%>小时
		  <%=frd.getInt("ahead_minute")%>分钟
          </td>
        </tr>
      <%
		}
%>   
	</tbody>
</table>
</body>
<script type="text/javascript">
function doOnToolbarInited() {
}

var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "form_remind_list.jsp?<%=sortquerystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "form_remind_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "form_remind_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

flex = jQuery("#grid").flexigrid
(
	{
	buttons : [
		{name: '添加', bclass: 'add', onpress : action},
		{name: '编辑', bclass: 'edit', onpress : action},		
		{name: '删除', bclass: 'delete', onpress : action},
		{separator: true},		
		{name: '条件', bclass: '', type: 'include', id: 'searchTable'}
		],
	/*
	searchitems : [
		{display: 'ISO', name : 'iso'},
		{display: 'Name', name : 'name', isdefault: true}
		],
	*/
	sortname: "<%=orderBy%>",
	sortorder: "<%=sort%>",
	url: false,
	usepager: true,
	checkbox : true,
	page: <%=curpage%>,
	total: <%=total%>,
	useRp: true,
	rp: <%=pagesize%>,
	
	//title: "通知",
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

function action(com, grid) {
	if (com=="删除") {
		var selectedCount = jQuery(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
		if (selectedCount == 0) {
			jAlert('请选择一条记录!','提示');
			return;
		}
		
		jConfirm("您确定要删除么？","提示",function(r){
			if(!r){return;}
			else{
				var ids = "";
				jQuery(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function(i) {
					if (ids=="")
						ids = jQuery(this).val();
					else
						ids += "," + jQuery(this).val();
				});			
		
				window.location.href = "form_remind_list.jsp?<%=querystr%>&op=del&ids=" + ids;		
			}
		})
	}
	else if (com=="添加") {
		window.location.href = "form_remind_add.jsp?code=<%=code%>";		
	}
	else if (com=="编辑") {
		var selectedCount = jQuery(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
		if (selectedCount == 0) {
			jAlert('请选择一条记录!','提示');
			return;
		}
		if (selectedCount > 1) {
			jAlert('只能选择一条记录!','提示');
			return;
		}
		
		var id = jQuery(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).val();
		// window.location.href = "form_remind_edit.jsp?id=" + id;		
		addTab("编辑提醒", "<%=request.getContextPath()%>/admin/form_remind_edit.jsp?id=" + id);
	}
}
</script>
</html>
