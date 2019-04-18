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

String formCode = "sales_ord_pay_plan";

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

String op = ParamUtil.get(request, "op");

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "h.id";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String customer = ParamUtil.get(request, "customer");
String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");
java.util.Date beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
java.util.Date endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");

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
	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "strBeginDate", strBeginDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "strEndDate", strEndDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "customer", customer, getClass().getName());
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
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../inc/sortabletable.js"></script>
<script type="text/javascript" src="../inc/columnlist.js"></script>

<script src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
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
			
	window.location.href = "sales_pay_plan_list.jsp?op=<%=op%>&customer=<%=StrUtil.UrlEncode(customer)%>&beginDate=<%=strBeginDate%>&endDate=<%=strEndDate%>&orderBy=" + orderBy + "&sort=" + sort;
}
</script>
</head>
<body>
<%
FormDb customerfd = new FormDb();
customerfd = customerfd.getFormDb("sales_customer");

FormDb orderfd = new FormDb();
orderfd = orderfd.getFormDb("sales_order");

String unitCode = privilege.getUserUnitCode(request);

String sql = "select p.id from " + fd.getTableNameByForm() + " p, form_table_sales_order o, form_table_sales_customer c where p.cws_id=o.id and o.cws_id=c.id and p.unit_code=" + StrUtil.sqlstr(unitCode);
if (op.equals("search")) {
	sql = "select p.id from " + fd.getTableNameByForm() + " p, form_table_sales_order o, form_table_sales_customer c where p.cws_id=o.id and o.cws_id=c.id and c.unit_code=" + StrUtil.sqlstr(unitCode);
	if(!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "sales") && !privilege.isUserPrivValid(request, "sales.manager") && privilege.isUserPrivValid(request, "sales.user") ){
		String salesPerson = privilege.getUser(request);
		if(!salesPerson.equals("")){
			sql += " and p.cws_creator in ("+StrUtil.sqlstr(salesPerson)+")";
		}
	}
	if (!customer.equals("")) {
		sql += " and c.customer like " + StrUtil.sqlstr("%" + customer + "%");
	}
	if (!"".equals(dept)) {
		sql += " and c.id in (select id from form_table_sales_customer where dept_code in ("+dept+"))";
	}
	if (beginDate!=null) {
		sql += " and p.jhhkrq>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
	}
	if (endDate!=null) {
		sql += " and p.jhhkrq<" + SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
	}	
}else{

	if(!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "sales") && !privilege.isUserPrivValid(request, "sales.manager") && privilege.isUserPrivValid(request, "sales.user") ){
		String salesPerson = privilege.getUser(request);
		if(!salesPerson.equals("")){
			sql += " and p.cws_creator in ("+StrUtil.sqlstr(salesPerson)+")";
		}
	}
	else if(!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "sales") && privilege.isUserPrivValid(request, "sales.manager")){
		sql += " and o.customer in (select id from form_table_sales_customer where dept_code in("+dept+"))";
	}
}

sql += " order by p.id desc";

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
      	<form action="sales_pay_plan_list.jsp" class="search-form">
        	&nbsp;客户
        	<input type="text" id="customer" name="customer" size="10" value="<%=customer%>" />
			<input name="op" value="search" type="hidden" />
    <%if (privilege.isUserPrivValid(request, "sales")) {%>
&nbsp;所属部门&nbsp;<select id="dept" name="dept" style="height:24px"><option value="" selected="selected">不限</option>

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
        计划回款日期从
        <input type="text" id="beginDate" name="beginDate" size="10" value="<%=strBeginDate%>" />
        至
        <input type="text" id="endDate" name="endDate" size="10" value="<%=strEndDate%>" />
        &nbsp;<input class="tSearch" type="submit" value="搜索" />
        </form>
        </td>
    </tr>
  </table>


      <table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
      	<thead>
        <tr>
          <th width="120">客户</th>
          <th width="120">所属部门</th>
          <th width="120">金额</th>
          <th width="120">计划回款日期</th>
          <th width="120">订单</th>
          <th width="120">操作</th>
        </tr>
        </thead>
      <%
	  	UserMgr um = new UserMgr();
	  	FormDAO fdaoCustomer = new FormDAO();
	  	FormDAO fdaoOrder = new FormDAO();
	  	int i = 0;
		SelectOptionDb sod = new SelectOptionDb();
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long id = fdao.getId();
			fdaoOrder = fdaoOrder.getFormDAO(StrUtil.toLong(fdao.getCwsId()), orderfd);
			fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toLong(fdaoOrder.getCwsId()), customerfd);
			
			String realName = "";
			UserDb user = um.getUserDb(fdao.getFieldValue("ywy"));
			realName = user.getRealName();
			%>
        <tr>
		  <td><a href="customer_show.jsp?id=<%=fdao.getCwsId()%>&formCode=sales_customer" target="_blank"><%=fdaoCustomer.getFieldValue("customer")%></a></td>
          <td><%=getDeptName(getDeptCode(fdao.getCreator())) %></td>
          <td><%=fdao.getFieldValue("jhhkje")%></td>
          <td><%=fdao.getFieldValue("jhhkrq")%>
          </td>
          <td align="center"><a href="javascript:;" onclick="addTab('<%=fdaoCustomer.getFieldValue("customer")%>订单', '<%=request.getContextPath()%>/visual/module_mode1_show.jsp?parentId=<%=fdao.getCwsId()%>&id=<%=fdao.getCwsId()%>&formCode=sales_order')">查看</a></td>
          <td align="center">
          <a href="../visual/module_mode1_show_relate.jsp?parentId=<%=fdao.getCwsId()%>&id=<%=fdao.getId()%>&formCodeRelated=sales_ord_pay_plan&formCode=sales_order&isShowNav=1">查看</a>
          </td>
        </tr>
      <%
		}
%>
      </table>
<%
String querystr = "op=" + op + "&customer=" + StrUtil.UrlEncode(customer) + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate;
//out.print(paginator.getCurPageBlock("sales_pay_plan_list.jsp?" + querystr));
%>
</body>
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
	window.location.href = "sales_pay_plan_list.jsp?<%=querystr%>&pagesize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp){
		window.location.href = "sales_pay_plan_list.jsp?<%=querystr%>&CPages=" + newp + "&pagesize=" + flex.getOptions().rp;
		}
}

function rpChange(pagesize) {
	window.location.href = "sales_pay_plan_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pagesize=" + pagesize;
}

function onReload() {
	window.location.reload();
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
