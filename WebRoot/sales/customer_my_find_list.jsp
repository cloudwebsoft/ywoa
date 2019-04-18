<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.module.sales.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = ParamUtil.get(request, "op");
String customer = ParamUtil.get(request, "customer");
try {	
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "customer", customer, getClass().getName());

	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "customer", customer, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
String userName = ParamUtil.get(request, "userName");
if ("".equals(userName)) {
	userName = privilege.getUser(request);
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>我发现的客户</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%@ include file="../inc/nocache.jsp"%>
<script src="../inc/common.js"></script>
<script src="<%=request.getContextPath()%>/js/jquery.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script language="JavaScript" type="text/JavaScript">
<!--
function openWin(url,width,height) {
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=150,left=220,width="+width+",height="+height);
}
//-->
</script>
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<%
int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);
if (isShowNav==1) {
%>
<%@ include file="customer_inc_menu_top.jsp"%>
<script>
o("menu5").className="current"; 
</script>
<%}%>

<table id="searchTable" width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td align="center">
    <form action="?" method="get">
	&nbsp;客户名称
	<input name="customer" value="<%=customer%>" size="8" />
	&nbsp;<input class="tSearch" type="submit" value="查询" />
	<input type="hidden" name="op" value="search" />
    <input name="userName" value="<%=userName%>" type="hidden" />
    </form>
	</td>
  </tr>
</table>
<%
FormMgr fm = new FormMgr();
String formCode = "sales_customer";
FormDb fd = fm.getFormDb(formCode);
%>
<table width="98%" border="0" cellpadding="0" cellspacing="0" id="grid">
	<thead>
  <tr>
    <th width="150" style="cursor:pointer">客户名称</th>
    <th width="150" style="cursor:pointer">销售人员</th>
    <th width="150" style="cursor:pointer">发现者</th>
    <th width="150" style="cursor:pointer">电话</th>
    <th width="150" style="cursor:pointer">网址</th>
    <th width="150" style="cursor:pointer">操作</th>
  </tr>
  </thead>
<%
FormDb fdCustomer = new FormDb();
fdCustomer = fdCustomer.getFormDb("sales_customer");

com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO(fd);
String sql = "select id from form_table_sales_customer where founder=" + StrUtil.sqlstr(userName);
if (op.equals("search")) {
	sql += " and customer like " + StrUtil.sqlstr("%"+customer+"%");
}

sql += " order by id desc";

int pagesize = ParamUtil.getInt(request, "pagesize", 20);
int curpage = StrUtil.toInt(ParamUtil.get(request, "CPages"), 1);

ListResult lr = fdao.listResult("sales_customer", sql, curpage, pagesize);

int total = lr.getTotal();
Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}

UserMgr um = new UserMgr();

Iterator ir = lr.getResult().iterator();
while (ir.hasNext()) {
	fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
	long id = fdao.getId();
	String founderRealName = "";
	String founder = StrUtil.getNullStr(fdao.getFieldValue("founder"));
	if (!founder.equals("")) {
		UserDb user = um.getUserDb(founder);
		if (user.isLoaded())
			founderRealName = user.getRealName();
	}	
%>
  <tr align="center">
    <td width="19%"><a href="customer_show.jsp?id=<%=id%>&amp;formCode=<%=formCode%>"><%=fdao.getFieldValue("customer")%></a></td>
    <td width="17%"><%=fdao.getFieldValue("sales_person")%></td>
    <td width="16%"><%=founderRealName%></td>
    <td width="15%"><%=fdao.getFieldValue("tel")%></td>
    <td width="19%"><%=fdao.getFieldValue("web")%></td>
    <td width="14%"><a href="customer_visit_list.jsp?customerId=<%=id%>">拜访</a></td>
  </tr>
  <%}%>
</table>
<script>
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
			autoHeight: true,
			width: document.documentElement.clientWidth,
			height: document.documentElement.clientHeight - 84
			}
		);
});

function changeSort(sortname, sortorder) {
	window.location.href = "customer_my_find_list.jsp?pagesize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp){
		window.location.href = "customer_my_find_list.jsp?CPages=" + newp + "&pagesize=" + flex.getOptions().rp;
		}
}

function rpChange(pageSize) {
	window.location.href = "customer_my_find_list.jsp?CPages=<%=curpage%>&pagesize=" + pageSize;
}

function onReload() {
	window.location.reload();
}
</script>
</body>
</html>

