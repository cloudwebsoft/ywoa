<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@page import="com.redmoon.oa.dept.DeptDb"%>
<%@page import="com.redmoon.oa.person.UserDb"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
//String priv = "sales";
//if (!privilege.isUserPrivValid(request, priv)) {
	//out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	//return;
//}
String action = ParamUtil.get(request, "action"); // action为manage时表示为销售总管理员方式
if (action.equals("manage")) {
	if (!privilege.isUserPrivValid(request, "sales")&&!privilege.isUserPrivValid(request, "sales.manager")) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

String priv = "sales.user";
if (!privilege.isUserPrivValid(request, priv) && !privilege.isUserPrivValid(request, "sales")&&!privilege.isUserPrivValid(request, "sales.manager")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
String formCode = "sales_service";

String customer = ParamUtil.get(request, "customer");
String satisfy = ParamUtil.get(request, "satisfy");

String dept =ParamUtil.get(request, "dept");
String userName = privilege.getUser(request);
String[] deptArr1 = null;
if("".equals(dept)){
	if (privilege.isUserPrivValid(request, "sales.manager")&&!privilege.isUserPrivValid(request, "sales")) {
		String deptstrs = "'"+getDeptCode(userName)+"'";
    	UserDb udb1 = new UserDb();
		udb1 = udb1.getUserDb(userName);
		deptArr1 = udb1.getAdminDepts();
		if(deptArr1.length>0){
			for(int t=0;t<deptArr1.length;t++){
				if("".equals(deptstrs)){
					deptstrs = "'"+deptArr1[t]+"'";
				}else{
					deptstrs += ",'"+deptArr1[t]+"'";
				}
			}
		}
		dept = deptstrs;
	}
}

try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "customer", customer, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
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
	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "satisfy", satisfy, getClass().getName());
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

String unitCode = privilege.getUserUnitCode(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=fd.getName()%>列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
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
<%@ include file="../inc/nocache.jsp"%>
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script src="<%=Global.getRootPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_js.jsp"></script>

<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../js/flexigrid.js"></script>
</head>
<body>
<%
		String sql = "select s.id from " + fd.getTableNameByForm() + " s where s.unit_code=" + StrUtil.sqlstr(unitCode);

		if (op.equals("search")) {
			sql = "select s.id from " + fd.getTableNameByForm() + " s, form_table_sales_customer c where s.customer=c.id and c.unit_code=" + StrUtil.sqlstr(unitCode);
			if(!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "sales") && !privilege.isUserPrivValid(request, "sales.manager") && privilege.isUserPrivValid(request, "sales.user") ){
				String salesPerson = privilege.getUser(request);
				if(!salesPerson.equals("")){
					sql += " and s.cws_creator in ("+StrUtil.sqlstr(salesPerson)+")";
				}
			}
			if (!customer.equals("")) {
				sql += " and c.customer like " + StrUtil.sqlstr("%" + customer + "%");
			}
			if (!"".equals(dept)) {
				sql += " and s.customer in (select id from form_table_sales_customer where dept_code in ("+dept+"))";
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
		}else{

			if(!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "sales") && !privilege.isUserPrivValid(request, "sales.manager") && privilege.isUserPrivValid(request, "sales.user") ){
				String salesPerson = privilege.getUser(request);
				if(!salesPerson.equals("")){
					sql += " and s.cws_creator in ("+StrUtil.sqlstr(salesPerson)+")";
				}
			}
			else if(!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "sales") && privilege.isUserPrivValid(request, "sales.manager")){
				sql += " and s.customer in (select id from form_table_sales_customer where dept_code in("+dept+"))";
			}
		}

		sql += " order by s.id desc";

		querystr = "customer=" + StrUtil.UrlEncode(customer) + "&satisfy=" + satisfy + "&beginDate=" + strBeginDate + "&enDate=" + strEndDate;

		int pagesize = ParamUtil.getInt(request, "pagesize", 10);
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
		<form id="form2" name="form2" class="search-form" action="sales_service_list.jsp" method="get">
      <input name="op" value="search" type="hidden" />
        &nbsp;客户
        <input type="text" id="customer" name="customer" size="10" value="<%=customer%>" />
   <%if (privilege.isUserPrivValid(request, "sales")) {%>
&nbsp;所属部 门&nbsp;<select id="dept" name="dept" style="height:24px"><option value="" selected="selected">不限</option>

<%
	String sqldept = "select name,code from department where rootCode='root' and parentCode<>'-1'";
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = jt.executeQuery(sqldept);
	ResultRecord rd = null;
	while(ri.hasNext()){
		rd = (ResultRecord)ri.next();
	
%>
	<option value="<%=rd.getString(2) %>"><%=rd.getString(1) %></option>
	<%} %>
</select>&nbsp;&nbsp;
<script>

o("dept").value = "<%=dept %>";
</script>
<%} %>

<%
        if (privilege.isUserPrivValid(request, "sales.manager")&&!privilege.isUserPrivValid(request, "sales")) {
        	//String deptCodes = getManageDepts(userName);
        	//String deptArr[] = deptCodes.split(",");
        	DeptDb ddb = new DeptDb();
        	UserDb udb = new UserDb();
			udb = udb.getUserDb(userName);
			String[] deptArr = udb.getAdminDepts();
        	if(deptArr.length>1){
        %>
        &nbsp;所属部门&nbsp;&nbsp;<select id="dept" name="dept" style="height:24px"><option value="" selected="selected">不限</option>

        <%
        	for(int t = 0 ;t<deptArr.length;t++){
        		ddb = ddb.getDeptDb(deptArr[t]);
        %>
        	<option value="<%=deptArr[t] %>"><%=ddb.getName() %></option>
        	<%} %>
        </select>&nbsp;&nbsp;
        <script>
o("dept").value = "<%=dept %>";
</script>
        <%}} %>
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
          <th width="220" >客户名称</th>
          <th width="120" >所属部门</th>
          <th width="160" >联系人</th>
          <th width="170">客户满意度</th>
          <th width="160" >服务日期</th>
          <th width="170" >操作</th>
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
          <td ><a href="javascript:;" onclick="addTab('<%=fdaoCustomer.getFieldValue("customer")%>', '<%=request.getContextPath()%>/sales/customer_service_show.jsp?customerId=<%=fdao.getFieldValue("customer")%>&amp;parentId=<%=fdao.getFieldValue("customer")%>&amp;id=<%=id%>&amp;formCodeRelated=sales_service&amp;formCode=sales_customer&amp;isShowNav=1')"><%=fdaoCustomer.getFieldValue("customer")%></a></td>
          <td><%=getDeptName(getDeptCode(fdao.getCreator())) %></td>
          <td ><%=fdaoLinkman.getFieldValue("linkmanName")%></td>
          <td ><%=sod.getOptionName("customer_myd", fdao.getFieldValue("customer_myd"))%></td>
          <td ><%=fdao.getFieldValue("contact_date")%></td>
          <td ><a href="javascript:;" onclick="addTab('<%=fdaoCustomer.getFieldValue("customer")%>', '<%=request.getContextPath()%>/sales/customer_service_show.jsp?customerId=<%=fdao.getFieldValue("customer")%>&amp;parentId=<%=fdao.getFieldValue("customer")%>&amp;id=<%=id%>&amp;formCodeRelated=sales_service&amp;formCode=sales_customer&amp;isShowNav=1')">查看</a>&nbsp;&nbsp;&nbsp;<a target="_blank" href="customer_service_edit.jsp?customerId=<%=fdao.getFieldValue("customer")%>&parentId=<%=fdao.getFieldValue("customer")%>&id=<%=fdao.getId()%>&menuItem=&formCodeRelated=sales_service&formCode=sales_customer&isShowNav=1">编辑</a>&nbsp;&nbsp;&nbsp;<a onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){event.returnValue=false;}else{window.location.href='../visual_del.jsp?id=<%=id%>&formCode=<%=formCode%>&privurl=<%=StrUtil.getUrl(request)%>'}}) " style="cursor:pointer">删除</a>&nbsp;&nbsp;		  </td>
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

$(document).ready(function() {
	flex = $("#grid").flexigrid
	(
		{
		buttons : [
			{name: '添加', bclass: 'add', onpress : action},
			{name: '条件', bclass: 'btnseparator', type: 'include', id: 'searchTable'}
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
});

function changeSort(sortname, sortorder) {
	window.location.href = "sales_service_list.jsp?<%=querystr%>&pagesize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "sales_service_list.jsp?<%=querystr%>&CPages=" + newp + "&pagesize=" + flex.getOptions().rp;
}

function rpChange(pagesize) {
	window.location.href = "sales_service_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pagesize=" + pagesize;
}

function onReload() {
	window.location.reload();
}
function action(com, grid) {
		if (com=='添加')	{
		window.location.href='sales_user_service_add.jsp?formCode=<%=formCode%>';
	}
}
</script>
<%!
public String getDeptCode(String uName){
	String sql = "select dept_code from dept_user where user_name=?";
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = null;
	ResultRecord rd = null;
	String deptCode = "";
	String deptCodes = "";
	try{
		ri = jt.executeQuery(sql,new Object[]{uName});
		if(ri.hasNext()){
			rd = (ResultRecord)ri.next();
			deptCodes = rd.getString(1);
			String codeAry[] = deptCodes.split(",");
			if(codeAry.length>0){
				deptCode = codeAry[0];
			}
		}
	}catch(Exception e){
		e.printStackTrace();
	}
	return deptCode;
}

public String getDeptName(String code){
	String sql = "select name from department where code=?";
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = null;
	ResultRecord rd = null;
	String name = "";
	try{
		ri = jt.executeQuery(sql,new Object[]{code});
		if(ri.hasNext()){
			rd = (ResultRecord)ri.next();
			name = rd.getString(1);
		}
	}catch(Exception e){
		e.printStackTrace();
	}
	return name;
}
%>
</html>
