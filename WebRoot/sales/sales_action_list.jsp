<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
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

String linkman = ParamUtil.get(request, "linkman");
String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");
java.util.Date beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
java.util.Date endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");

String orderBy = ParamUtil.get(request, "orderBy");
try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "orderBy", orderBy, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "linkman", linkman, getClass().getName());

	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "linkman", linkman, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "beginDate", strBeginDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "endDate", strEndDate, getClass().getName());
	
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
if (orderBy.equals(""))
	orderBy = "visit_date";
	
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
	
String unitCode = privilege.getUserUnitCode(request);

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>行动记录</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
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
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
<script>
function openWin(url,width,height) {
	var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}

var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "sales_action_list.jsp?op=<%=op%>&orderBy=" + orderBy + "&sort=" + sort + "&linkman=<%=StrUtil.UrlEncode(linkman)%>&beginDate=<%=strBeginDate%>&endDate=<%=strEndDate%>";
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
<%@ include file="sales_action_inc_menu_top.jsp"%>
<script>
o("menu1").className="current"; 
</script>
<%
String formCode = "day_lxr";
FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
String sql = "select id from " + fd.getTableNameByForm() + " d where d.is_visited='是' and d.unit_code=" + StrUtil.sqlstr(unitCode);

if (op.equals("search")) {
	if (!linkman.equals("")) {
		sql = "select d.id from " + fd.getTableNameByForm() + " d, form_table_sales_linkman l where d.lxr=l.id and l.unit_code=" + StrUtil.sqlstr(unitCode) + " and l.linkmanName like " + StrUtil.sqlstr("%" + linkman + "%");
	}
	if (beginDate!=null) {
		sql += " and d.visit_date>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
	}
	if (endDate!=null) {
		sql += " and d.visit_date<" + SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
	}
}

sql += " order by " + orderBy + " " + sort;

// out.print("sql=" + sql);

int pagesize = ParamUtil.getInt(request, "pagesize", 30);
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
	
FormDAO fdao = new FormDAO();

ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
int total = lr.getTotal();
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
<table id="searchTable" width="100%" border="0" cellpadding="0" cellspacing="0">
	<tr>
		<td align="center">
			<form action="sales_action_list.jsp" class="search-form">
				<input name="op" value="search" type="hidden"/>
				&nbsp;联系人
				<input type="text" id="linkman" name="linkman" size="10" value="<%=linkman%>"/>
				日期从
				<input type="text" id="beginDate" name="beginDate" size="10" value="<%=strBeginDate%>"/>
				至
				<input type="text" id="endDate" name="endDate" size="10" value="<%=strEndDate%>"/>
				&nbsp;<input class="tSearch" type="submit" value="搜索"/>
			</form>
		</td>
	</tr>
</table>
<table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
	<thead>
  <tr align="center">
    <th width="80" style="cursor:pointer">联系人</th>
    <th width="220" style="cursor:pointer">客户</th>
    <th width="120" style="cursor:pointer">拜访者</th>
    <th width="120" style="cursor:pointer">方式</th>
    <th width="120" style="cursor:pointer">日期</th>
    <th width="120" style="cursor:pointer">成本</th>
    <th width="350" style="cursor:pointer">联系结果</th>
    <th width="120" style="cursor:pointer">定位签到</th>
    <th width="120" style="cursor:pointer">操作</th>
  </tr>
  </thead>
  <%	
	  	int i = 0;
		FormDb fdLinkman = new FormDb();
		fdLinkman = fdLinkman.getFormDb("sales_linkman");
		FormDb fdCustomer = new FormDb();
		fdCustomer = fdCustomer.getFormDb("sales_customer");
		FormDAO fdaoLinkman = new FormDAO();
		FormDAO fdaoCustomer = new FormDAO();
		SelectOptionDb sod = new SelectOptionDb();
		UserMgr um = new UserMgr();		
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long myid = fdao.getId();
			fdaoLinkman = fdaoLinkman.getFormDAO(StrUtil.toInt(fdao.getFieldValue("lxr")), fdLinkman);
			fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toInt(fdaoLinkman.getFieldValue("customer")), fdCustomer);
		%>
  <tr align="center">
    <td width="7%" align="center"><a href="javascript:;" onclick="addTab('<%=fdaoLinkman.getFieldValue("linkmanName")%>', '<%=request.getContextPath()%>/visual/module_show.jsp?id=<%=myid%>&amp;action=<%=action%>&formCode=<%=formCode%>&code=<%=formCode%>&isShowNav=0')"><%=fdaoLinkman.getFieldValue("linkmanName")%></a></td>
    <td width="16%" align="left"><a href="javascript:;" onclick="addTab('<%=fdaoCustomer.getFieldValue("customer")%>', '<%=request.getContextPath()%>/sales/customer_show.jsp?id=<%=fdaoCustomer.getId()%>&action=&formCode=sales_customer')"><%=fdaoCustomer.getFieldValue("customer")%></a></td>
    <td width="8%" align="center"><%
	UserDb user = um.getUserDb(fdao.getCreator());
	%>
      <a href="javascript:;" onclick="addTab('<%=user.getRealName()%>', '<%=request.getContextPath()%>/user_info.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>')"><%=user.getRealName()%></a></td>
    <td width="7%" align="center"><%=fdao.getFieldValue("contact_type")%></td>
    <td width="9%" align="center"><%=fdao.getFieldValue("visit_date")%></td>
    <td width="7%" align="left"><%=sod.getOptionName("cost_type", fdao.getFieldValue("cost_type"))%>&nbsp;<%=fdao.getFieldValue("cost_sum")%></td>
    <td width="27%" align="left"><%=StrUtil.toHtml(fdao.getFieldValue("contact_result"))%></td>
    <td width="8%"><%
	String locationId = StrUtil.getNullStr(fdao.getFieldValue("location"));
	if (!"".equals(locationId)) {
		%>
      <a href="javascript:;" onclick="addTab('定位签到', '<%=request.getContextPath()%>/map/location_map_bd.jsp?id=<%=locationId%>')">查看</a>
      <%
	}
	%></td>
    <td width="11%">
	<a href="javascript:;" onclick="addTab('<%=user.getRealName()%>行动', '<%=request.getContextPath()%>/visual/module_show.jsp?id=<%=myid%>&amp;action=<%=action%>&formCode=<%=formCode%>&code=<%=formCode%>&isShowNav=0')">查看</a>&nbsp;&nbsp;&nbsp;    
    <a href="javascript:;" onclick="addTab('<%=user.getRealName()%>行动', '<%=request.getContextPath()%>/visual/module_edit.jsp?id=<%=myid%>&amp;action=<%=action%>&amp;code=<%=StrUtil.UrlEncode(formCode)%>&amp;formCode=<%=StrUtil.UrlEncode(formCode)%>&isShowNav=0')">编辑</a>&nbsp;&nbsp;&nbsp;
    <a onclick=" jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{ window.location.href='../visual_del.jsp?id=<%=myid%>&amp;action=<%=action%>&formCode=<%=formCode%>&amp;privurl=<%=StrUtil.getUrl(request)%>'}})" style="cursor:pointer">删除</a></span> </td>
  </tr>
  <%
		}
%>
</table>
<%
			//out.print(paginator.getCurPageBlock("sales_action_list.jsp?action=" + action + "&orderBy=" + orderBy + "&sort=" + sort + "&op=" + op + "&linkman=" + StrUtil.UrlEncode(linkman) + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate));
			String querystr = "action=" + action + "&orderBy=" + orderBy + "&sort=" + sort + "&op=" + op + "&linkman=" + StrUtil.UrlEncode(linkman) + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate;
			%>
			<script>
			function initCalendar() {
				$('#beginDate').datetimepicker({
					lang:'ch',
					datepicker:true,
					timepicker:false,
					format:'Y-m-d'
				});
				$('#endDate').datetimepicker({
					lang:'ch',
					datepicker:true,
					timepicker:false,
					format:'Y-m-d'
				});
			}
			function doOnToolbarInited() {
				initCalendar();
			}
	$(function(){
		flex = $("#grid").flexigrid
		(
			{
			buttons : [
			{name: '条件', bclass: '', type: 'include', id: 'searchTable'}
			],
			/*
			searchitems : [
				{display: 'ISO', name : 'iso'},
				{display: 'Name', name : 'name', isdefault: true}
				],
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
			showToggleBtn: false,
			
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

function changeSort(sortname, sortorder) {
	window.location.href = "sales_action_list.jsp?action=<%=action%>&<%=querystr%>&pagesize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp){
		window.location.href = "sales_action_list.jsp?action=<%=action%>&<%=querystr%>&CPages=" + newp + "&pagesize=" + flex.getOptions().rp;
		}
}

function rpChange(pageSize) {
	window.location.href = "sales_action_list.jsp?action=<%=action%>&<%=querystr%>&CPages=<%=curpage%>&pagesize=" + pageSize;
}

function onReload() {
	window.location.reload();
}
</script>
</body>
</html>
