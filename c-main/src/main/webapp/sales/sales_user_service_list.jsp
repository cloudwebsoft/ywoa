<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "sales.user";
if (!privilege.isUserPrivValid(request, priv)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

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

String op = ParamUtil.get(request, "op");
String formCode = "sales_service";

String customer = ParamUtil.get(request, "customer");
String satisfy = ParamUtil.get(request, "satisfy");

try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "satisfy", satisfy, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;	
}

String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");
java.util.Date beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
java.util.Date endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");

try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "customer", customer, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "satisfy", satisfy, getClass().getName());
	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "userName", userName, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "customer", customer, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "strBeginDate", strBeginDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "strEndDate", strEndDate, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;	
}

String querystr = "";

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=fd.getName()%>列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<%@ include file="../inc/nocache.jsp"%>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script src="<%=Global.getRootPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_js.jsp"></script>

    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
</head>
<body>
<%@ include file="sales_user_inc_menu_top.jsp"%>
<script>
o("menu8").className="current"; 
</script>
<%
String sql = "select s.id from " + fd.getTableNameByForm() + " s, form_table_sales_customer c where s.customer=c.id and c.sales_person=" + StrUtil.sqlstr(userName);

if (op.equals("search")) {
	if (!customer.equals("")) {
		sql = "select s.id from " + fd.getTableNameByForm() + " s, form_table_sales_customer c where c.sales_person=" + StrUtil.sqlstr(userName) + " and s.customer=c.id and c.customer like " + StrUtil.sqlstr("%" + customer + "%");
	}
	if (!satisfy.equals("")) {
		sql += " and s.customer_myd=" + satisfy;
	}
	if (beginDate!=null) {
		sql += " and s.contact_date>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
	}
	if (endDate!=null) {
		sql += " and s.contact_date<" + SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
	}
}

sql += " order by s.id desc";		

querystr = "op=" + op + "&customer=" + StrUtil.UrlEncode(customer) + "&userName=" + StrUtil.UrlEncode(userName) + "&satisfy=" + satisfy + "&beginDate=" + strBeginDate + "&enDate=" + strEndDate;

int pagesize = ParamUtil.getInt(request, "pagesize", 10);
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
	
FormDAO fdao = new FormDAO();

ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
long total = lr.getTotal();
Vector v = lr.getResult();
Iterator ir = null;
if (v!=null)
	ir = v.iterator();
paginator.init(total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}
%>
  <table id="searchTable" width="100%" border="0" cellpadding="0" cellspacing="0">
    <tr>
      <td align="center">
<form id="form2" name="form2" action="sales_user_service_list.jsp" method="get">
      	<input name="op" value="search" type="hidden" />
      	<input name="userName" value="<%=userName%>" type="hidden" />      
        &nbsp;客户
        <input type="text" id="customer" name="customer" size="10" value="<%=customer%>" />
        客户满意度
        <select id="satisfy" name="satisfy">
          <option value="">不限</option>
          <%
SelectMgr sm = new SelectMgr();
SelectDb sd = sm.getSelect("customer_myd");
Vector vsd = sd.getOptions();
Iterator irsd = vsd.iterator();
while (irsd.hasNext()) {
	SelectOptionDb sod = (SelectOptionDb)irsd.next();
	%>
          <option value="<%=sod.getValue()%>"><%=sod.getName()%></option>
          <%	
}
%>
        </select>
        <script>
o("satisfy").value = "<%=satisfy%>";
</script>
        服务时间从
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
        <tr align="center"> 
          <th width="120" style="cursor:pointer">客户名称</th>
          <th width="120" style="cursor:pointer">联系人</th>
          <th width="120" style="cursor:pointer">客户满意度</th>
          <th width="120" style="cursor:pointer">服务日期</th>
          <th width="120" style="cursor:pointer">操作</th>
        </tr>
      </thead>
      <%
		FormDb fdCustomer = new FormDb();
		fdCustomer = fdCustomer.getFormDb("sales_customer");
		SelectOptionDb sod = new SelectOptionDb();
		
		FormDb fdLinkman = new FormDb();
		fdLinkman = fdLinkman.getFormDb("sales_linkman");
		
		com.redmoon.oa.visual.FormDAO fdaoLinkman = new com.redmoon.oa.visual.FormDAO();
		com.redmoon.oa.visual.FormDAO fdaoCustomer = new com.redmoon.oa.visual.FormDAO();

	  	int i = 0;
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long id = fdao.getId();
			fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toLong(fdao.getFieldValue("customer")), fdCustomer);			
			fdaoLinkman = fdaoLinkman.getFormDAO(StrUtil.toLong(fdao.getFieldValue("lxr")), fdLinkman);			
		%>
        <tr align="center"> 
          <td width="26%"><a href="javascript:;" onclick="addTab('<%=fdaoCustomer.getFieldValue("customer")%>', '<%=request.getContextPath()%>/sales/customer_service_show.jsp?customerId=<%=fdao.getFieldValue("customer")%>&parentId=<%=fdao.getFieldValue("customer")%>&id=<%=id%>&formCodeRelated=sales_service&formCode=sales_customer&isShowNav=1')"><%=fdaoCustomer.getFieldValue("customer")%></a></td>
          <td width="20%"><%=fdaoLinkman.getFieldValue("linkmanName")%></td>
          <td width="21%"><%=sod.getOptionName("customer_myd", fdao.getFieldValue("customer_myd"))%></td>
          <td width="16%"><%=fdao.getFieldValue("contact_date")%></td>
          <td width="17%">
          <a href="javascript:;" onclick="addTab('<%=fdaoCustomer.getFieldValue("customer")%>', '<%=request.getContextPath()%>/sales/customer_service_show.jsp?customerId=<%=fdao.getFieldValue("customer")%>&parentId=<%=fdao.getFieldValue("customer")%>&id=<%=id%>&formCodeRelated=sales_service&formCode=sales_customer&isShowNav=1')">
          查看</a>&nbsp;&nbsp;&nbsp;<a href="customer_service_edit.jsp?customerId=<%=fdao.getFieldValue("customer")%>&parentId=<%=fdao.getFieldValue("customer")%>&id=<%=fdao.getId()%>&menuItem=&formCodeRelated=sales_service&formCode=sales_customer&isShowNav=1">编辑</a>&nbsp;&nbsp;&nbsp;
          <a onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){event.returnValue=false;}else{ window.location.href='../visual_del.jsp?id=<%=id%>&formCode=<%=formCode%>&privurl=<%=StrUtil.getUrl(request)%>'}}) " >删除</a>&nbsp;&nbsp;		  </td>
        </tr>
      <%
		}
%>
      </table>
</body>
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
	window.location.href = "sales_user_service_list.jsp?<%=querystr%>&pagesize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp){
		window.location.href = "sales_user_service_list.jsp?<%=querystr%>&CPages=" + newp + "&pagesize=" + flex.getOptions().rp;
		}
}

function rpChange(pagesize) {
	window.location.href = "sales_user_service_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pagesize=" + pagesize;
}

function onReload() {
	window.location.reload();
}
</script>
</html>
