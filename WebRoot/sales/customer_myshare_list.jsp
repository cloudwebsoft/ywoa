<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.module.sales.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
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
<title>共享人员列表</title>
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
<%@ include file="customer_inc_menu_top.jsp"%>
<script>
o("menu4").className="current"; 
</script>

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

<br>
<%
CustomerShareDb csd = new CustomerShareDb();
FormMgr fm = new FormMgr();
String formCode = "sales_customer";
FormDb fd = fm.getFormDb(formCode);
%>
<table width="98%" border="0" cellpadding="0" cellspacing="0" id="grid">
	<thead>
  <tr>
    <th width="200" style="cursor:pointer">客户名称</th>
    <th width="200" style="cursor:pointer">销售人员</th>
    <th width="200" style="cursor:pointer">电话</th>
    <th width="200" style="cursor:pointer">传真</th>
    <th width="200" style="cursor:pointer">网址</th>
  </tr>
  </thead>
<%
FormDb fdCustomer = new FormDb();
fdCustomer = fdCustomer.getFormDb("sales_customer");

com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
String sql = "select cs.id from customer_share cs, form_table_sales_customer c where cs.customerId=c.id and cs.sharePerson=" + StrUtil.sqlstr(userName);
if (op.equals("search")) {
	sql += " and c.customer like " + StrUtil.sqlstr("%"+customer+"%");
}

int pagesize = ParamUtil.getInt(request, "pagesize", 20);
int curpage = StrUtil.toInt(ParamUtil.get(request, "CPages"), 1);

ListResult lr = csd.listResult(sql, curpage, pagesize);

int total = lr.getTotal();
Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}

Iterator ir = lr.getResult().iterator();
while (ir.hasNext()) {
	csd = (CustomerShareDb)ir.next();
	long id = csd.getCustomerId();
	com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(id);
%>
  <tr align="center">
    <td ><a href="javascript:;" onclick="addTab('<%=fdao.getFieldValue("customer")%>', '<%=request.getContextPath()%>/sales/customer_show.jsp?id=<%=id%>&amp;formCode=<%=formCode%>')"><%=fdao.getFieldValue("customer")%></a></td>
    <td ><%=fdao.getFieldValue("sales_person")%></td>
    <td ><a href="../visual_show.jsp?id=<%=id%>&amp;formCode=<%=formCode%>"><%=fdao.getFieldValue("tel")%></a></td>
    <td ><a href="../visual_show.jsp?id=<%=id%>&amp;formCode=<%=formCode%>"><%=fdao.getFieldValue("fax")%></a></td>
    <td ><a href="../visual_show.jsp?id=<%=id%>&amp;formCode=<%=formCode%>"><%=fdao.getFieldValue("web")%></a></td>
  </tr>
  <%}%>
</table>
<table width="98%" border="0" align="center">
  <tr>
    <td align="right">
	<%
	String querystr = "op="+op+"&customer=" + StrUtil.UrlEncode(customer) + "&userName=" + StrUtil.UrlEncode(userName);
    //out.print(paginator.getCurPageBlock("customer_myshare_list.jsp?"+querystr));	
	%>
    </td>
  </tr>
</table>
<script>
$(function(){
		flex = $("#grid").flexigrid
		(
			{
			buttons : [
			//{separator: true},
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
	window.location.href = "customer_myshare_list.jsp?pagesize=" + flex.getOptions().rp ;
}

function changePage(newp) {
	if (newp){
		window.location.href = "customer_myshare_list.jsp?CPages=" + newp + "&pagesize=" + flex.getOptions().rp;
		}
}

function rpChange(pageSize) {
	window.location.href = "customer_myshare_list.jsp?CPages=<%=curpage%>&pagesize=" + pageSize;
}

function onReload() {
	window.location.reload();
}
</script>
</body>
</html>

