<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String userName = ParamUtil.get(request, "userName");
if (userName.equals(""))
	userName = privilege.getUser(request);

if (!userName.equals(privilege.getUser(request))) {
	if (!privilege.isUserPrivValid(request, "sales")) {
		if (!privilege.canAdminUser(request, userName)) {
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "您对该用户没有管理权限！"));
			return;
		}
	}
}

String formCode = "sales_ord_invoice";

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

String op = ParamUtil.get(request, "op");

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "i.id";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String customer = ParamUtil.get(request, "customer");
String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");
java.util.Date beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
java.util.Date endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");

try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "userName", userName, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "customer", customer, getClass().getName());
	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "sort", sort, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "orderBy", orderBy, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "userName", userName, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "customer", customer, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "strBeginDate", strBeginDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "strEndDate", strEndDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;	
}

// 取得皮肤路径
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))
	skincode = UserSet.defaultSkin;

SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=fd.getName()%>列表</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../inc/sortabletable.js"></script>
<script type="text/javascript" src="../inc/columnlist.js"></script>

<script src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
<%@ include file="../inc/nocache.jsp"%>
<script>
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "sales_user_invoice_list.jsp?op=<%=op%>&userName=<%=StrUtil.UrlEncode(userName)%>&customer=<%=StrUtil.UrlEncode(customer)%>&beginDate=<%=strBeginDate%>&endDate=<%=strEndDate%>&orderBy=" + orderBy + "&sort=" + sort;
}
</script>
</head>
<body>
<%@ include file="sales_user_inc_menu_top.jsp"%>
<script>
o("menu7").className="current"; 
</script>
<%
String priv = "sales.user";
if (!privilege.isUserPrivValid(request, priv) && !privilege.isUserPrivValid(request, "sales")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

FormDb customerfd = new FormDb();
customerfd = customerfd.getFormDb("sales_customer");

FormDb orderfd = new FormDb();
orderfd = orderfd.getFormDb("sales_order");

String sql = "select i.id from " + fd.getTableNameByForm() + " i, form_table_sales_order o, form_table_sales_customer c where c.sales_person=" + StrUtil.sqlstr(userName) + " and i.cws_id=o.id and o.cws_id=c.id";
if (op.equals("search")) {
	if (!customer.equals("")) {
		sql = "select i.id from " + fd.getTableNameByForm() + " i, form_table_sales_order o, form_table_sales_customer c where c.sales_person=" + StrUtil.sqlstr(userName) + " and i.cws_id=o.id and o.cws_id=c.id and c.customer like " + StrUtil.sqlstr("%" + customer + "%");
	}
	if (beginDate!=null) {
		sql += " and i.kprq>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
	}
	if (endDate!=null) {
		sql += " and i.kprq<" + SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
	}	
}

sql += " order by i.id desc";

// out.print(sql);

int pagesize = ParamUtil.getInt(request, "pagesize", 20);
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
      <td width="100%" align="center">
<form id="formSearch" name="formSearch" method="get" action="sales_user_invoice_list.jsp">
      <input name="op" value="search" type="hidden" />
      <input name="userName" value="<%=userName%>" type="hidden" />      
        &nbsp;客户
        <input type="text" id="customer" name="customer" size="10" value="<%=customer%>" />
        开票日期从
        <input type="text" id="beginDate" name="beginDate" size="10" value="<%=strBeginDate%>" />
        至
        <input type="text" id="endDate" name="endDate" size="10" value="<%=strEndDate%>" />
        &nbsp;
			<input class="tSearch" name="submit" type=submit value="搜索">
</form></td>
    </tr>
  </table>

      <table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
      	<thead>
        <tr>
          <th width="120" style="cursor:pointer">客户</th>
          <th width="120" style="cursor:pointer">金额</th>
          <th width="120" style="cursor:pointer">发票类型</th>
          <th width="120" style="cursor:pointer">计划回款日期</th>
          <th width="120" style="cursor:pointer">订单</th>
          <th width="120" style="cursor:pointer">操作</th>
        </tr>
        </thead>
      <%
	  	UserMgr um = new UserMgr();
	  	FormDAO fdaoCustomer = new FormDAO();
	  	FormDAO fdaoOrder = new FormDAO();
		SelectOptionDb sod = new SelectOptionDb();
		
	  	int i = 0;
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long id = fdao.getId();
			fdaoOrder = fdaoOrder.getFormDAO(StrUtil.toLong(fdao.getCwsId()), orderfd);
			fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toLong(fdaoOrder.getCwsId()), customerfd);
			
			%>
        <tr>
		  <td><a href="customer_show.jsp?id=<%=fdao.getCwsId()%>&formCode=sales_customer" target="_blank"><%=fdaoCustomer.getFieldValue("customer")%></a></td>
          <td><%=fdao.getFieldValue("fpje")%></td>
          <td><%=sod.getOptionName("sales_tax", fdao.getFieldValue("fplx"))%></td>
          <td><%=fdao.getFieldValue("kprq")%>
          </td>
          <td align="center"><a href="javascript:;" onclick="addTab('<%=fdaoCustomer.getFieldValue("customer")%>订单', '<%=request.getContextPath()%>/visual/module_mode1_show.jsp?parentId=<%=fdao.getCwsId()%>&id=<%=fdao.getCwsId()%>&formCode=sales_order')">查看</a></td>
          <td align="center">
          <a href="../visual/module_mode1_show_relate.jsp?parentId=<%=fdao.getCwsId()%>&id=<%=fdao.getId()%>&formCodeRelated=sales_ord_invoice&formCode=sales_order&isShowNav=1&menuItem=7">查看</a>
          </td>
        </tr>
      <%
		}
%>
      </table>
<%
String querystr = "op=" + op + "&userName=" + StrUtil.UrlEncode(userName) + "&customer=" + StrUtil.UrlEncode(customer) + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate;
%>
<script>
function initCalendar() {
	$('#beginDate').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d',
    	formatDate:'Y/m/d',
    });
    $('#endDate').datetimepicker({
        lang:'ch',
        timepicker:false,
        format:'Y-m-d',
        formatDate:'Y/m/d',
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
	window.location.href = "sales_user_invoice_list.jsp?<%=querystr%>&pagesize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp){
		window.location.href = "sales_user_invoice_list.jsp?<%=querystr%>&CPages=" + newp + "&pagesize=" + flex.getOptions().rp;
		}
}

function rpChange(pagesize) {
	window.location.href = "sales_user_invoice_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pagesize=" + pagesize;
}

function onReload() {
	window.location.reload();
}
</script>
</body>
</html>
