<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.sale.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));

String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action"); // action为manage时表示为销售总管理员方式
if (action.equals("manage")) {
	if (!privilege.isUserPrivValid(request, "sales")) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"), true));
		return;
	}
}

long customerId = ParamUtil.getLong(request, "customerId", -1);
if (customerId==-1) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id") + " customerId=" + customerId));
	return;
}

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "visit_date";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
	
try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "orderBy", orderBy, getClass().getName());
	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "orderBy", orderBy, getClass().getName());
} catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}	
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>行动记录</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script type="text/javascript" src="../js/flexigrid.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script>
function openWin(url,width,height) {
	var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}

var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "customer_visit_list.jsp?op=<%=op%>&customerId=<%=customerId%>&orderBy=" + orderBy + "&sort=" + sort;
}
</script>
</head>
<body>
<%
String priv = "sales.user";
if (!privilege.isUserPrivValid(request, priv) && !privilege.isUserPrivValid(request, "sales")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"), true));
	return;
}

// 判断有无浏览的权限
if (!SalePrivilege.canUserSeeCustomer(request, customerId)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"), true));
	return;
}

%>
<%@ include file="customer_inc_nav.jsp"%>
<script>
o("menu3").className="current";
</script>
<%
String formCode = "day_lxr";
FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
String sql = "select d.id from " + fd.getTableNameByForm() + " d, form_table_sales_linkman l where d.lxr=l.id and l.customer=" + customerId; //  + " and d.is_visited='是'";
sql += " order by " + orderBy + " " + sort;

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
<table width="98%" border="0" cellpadding="0" cellspacing="0" id="grid">
	<thead>
  <tr align="center">
    <th width="120" style="cursor:pointer">联系人</th>
    <th width="90" style="cursor:pointer">拜访者</th>
    <th width="90" style="cursor:pointer">方式</th>
    <th width="500" style="cursor:pointer">联系结果</th>
    <th width="90" style="cursor:pointer">拜访日期</th>
    <th width="60" style="cursor:pointer">已联系</th>
    <th width="70" style="cursor:pointer">定位签到</th>
    <th width="120" style="cursor:pointer">操作</th>
  </tr>
  </thead>
  <%	
	  	int i = 0;
		FormDb fdLinkman = new FormDb();
		fdLinkman = fdLinkman.getFormDb("sales_linkman");
		FormDAO fdaoLinkman = new FormDAO();
		UserMgr um = new UserMgr();
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long myid = fdao.getId();
			fdaoLinkman = fdaoLinkman.getFormDAO(StrUtil.toInt(fdao.getFieldValue("lxr")), fdLinkman);
		%>
  <tr align="center">
    <td width="13%" align="left"><a href="javascript:;" onclick="addTab('行动', '<%=request.getContextPath()%>/visual/module_show.jsp?code=<%=formCode%>&id=<%=myid%>&amp;action=<%=action%>&formCode=<%=formCode%>&isShowNav=0')"><%=fdaoLinkman.getFieldValue("linkmanName")%></a></td>
    <td width="9%" align="left">
      <%
	UserDb user = um.getUserDb(fdao.getCreator());
	%>
      <a href="../user_info.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>" target="_blank"><%=user.getRealName()%></a>
    </td>
    <td width="9%" align="left"><%=fdao.getFieldValue("contact_type")%></td>
    <td width="35%" align="left"><%=fdao.getFieldValue("contact_result")%></td>
    <td width="10%" align="center"><%=fdao.getFieldValue("visit_date")%></td>
    <td width="5%" align="center"><%=fdao.getFieldValue("is_visited")%></td>
    <td width="7%">
    <%
	String locationId = StrUtil.getNullStr(fdao.getFieldValue("location"));
	if (!"".equals(locationId)) {
		%>
		<a href="javascript:;" onclick="addTab('定位签到', '<%=request.getContextPath()%>/map/location_map_new.jsp?locationMaps=<%=locationId%>')">查看</a>
		<%
	}
	%>
    </td>
    <td width="12%">
<a href="javascript:;" onclick="addTab('行动', '<%=request.getContextPath()%>/visual/module_show.jsp?code=<%=formCode%>&id=<%=myid%>&amp;action=<%=action%>&formCode=<%=formCode%>&isShowNav=0')">查看</a>&nbsp;&nbsp;
    <a href="javascript:;" onclick="openWin('linkman_visit_edit.jsp?id=<%=myid%>&amp;action=<%=action%>&amp;formCode=<%=StrUtil.UrlEncode(formCode)%>&isShowNav=0', 800, 600, true)">编辑</a>&nbsp;&nbsp;
    <a href="javascript:;" onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{ window.location.href='../visual_del.jsp?id=<%=myid%>&amp;action=<%=action%>&formCode=<%=formCode%>&amp;privurl=<%=StrUtil.getUrl(request)%>'}}) " style="cursor:pointer">删除</a></span> </td>
  </tr>
  <%
		}
%>
</table>

<script>
	$(function(){
		flex = $("#grid").flexigrid
		(
			{
				buttons : [
							],
			/*buttons : [
			{name: '添加', bclass: 'add', onpress : actions}
			],
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
			autoHeight: true,
			width: document.documentElement.clientWidth,
			height: document.documentElement.clientHeight - 84
			}
		);
});

function changeSort(sortname, sortorder) {
	window.location.href = "customer_visit_list.jsp?pagesize=" + flex.getOptions().rp + "&customerId=<%=customerId%>";
}

function changePage(newp) {
	if (newp){
		window.location.href = "customer_visit_list.jsp?CPages=" + newp + "&pagesize=" + flex.getOptions().rp + "&customerId=<%=customerId%>";
		}
}

function rpChange(pageSize) {
	window.location.href = "customer_visit_list.jsp?CPages=<%=curpage%>&pagesize=" + pageSize + "&customerId=<%=customerId%>";
}

function onReload() {
	window.location.reload();
}
function actions(com, grid) {
	if(com=='添加'){
		window.location.href='linkman_add.jsp?op=listOfCustomer&formCode=sales_linkman&customerId=<%=customerId%>';
	}
  else if (com=='全选') {
  	selAllCheckBox('mobiles');
	}
	else if (com=='群发短信')
	{
		sendSms();
	}
}
</script>
</body>
</html>
