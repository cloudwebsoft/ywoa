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
<%@page import="cn.js.fan.web.Global"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String formCode = "sales_chance";

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

// 取得皮肤路径
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))
	skincode = UserSet.defaultSkin;

SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "ch.find_date";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String op = ParamUtil.get(request, "op");
String dept =ParamUtil.get(request, "dept");
String customer = ParamUtil.get(request, "customer");
int state = ParamUtil.getInt(request, "state", -1);
String status = ParamUtil.get(request, "status");
String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");
java.util.Date beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
java.util.Date endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");

try {	
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "orderBy", orderBy, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "customer", customer, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "status", status, getClass().getName());

	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "strBeginDate", strBeginDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "strEndDate", strEndDate, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=fd.getName()%>列表</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
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
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../inc/sortabletable.js"></script>
<script type="text/javascript" src="../inc/columnlist.js"></script>

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
			
	window.location.href = "sales_chance_list.jsp?op=<%=op%>&orderBy=" + orderBy + "&sort=" + sort + "&state=<%=state%>&status=<%=status%>&beginDate=<%=strBeginDate%>&endDate=<%=strEndDate%>";
}
</script>
</head>
<body>
<%
String unitCode = privilege.getUserUnitCode(request);

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

String userName = ParamUtil.get(request, "userName");
String[] deptArr1 = null;
if (userName.equals(""))
	userName = privilege.getUser(request);

String deptstrs = "";
if("".equals(dept) || dept.equals(DeptDb.ROOTCODE)){
	if (privilege.isUserPrivValid(request, "sales.manager")&&!privilege.isUserPrivValid(request, "sales")) {
		deptstrs = "'"+getDeptCode(userName)+"'";
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
	}
}

if (deptstrs.equals("") && !dept.equals("")) {
	deptstrs = StrUtil.sqlstr(dept);
}

if (!userName.equals(privilege.getUser(request))) {	
	// 检查是否有管理用户所在部门的权限
	DeptUserDb dud = new DeptUserDb();
	Vector vDept = dud.getDeptsOfUser(userName);
	Iterator vIr = vDept.iterator();
	boolean canAdminUser = false;
	while (vIr.hasNext()) {
		DeptDb dd = (DeptDb)vIr.next();
		if (com.redmoon.oa.pvg.Privilege.canUserAdminDept(request, dd.getCode())) {
			canAdminUser = true;
			break;
		}
	}
	if (!canAdminUser) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "您对该用户没有管理权限！"));
		return;
	}
}

FormDb customerfd = new FormDb();
customerfd = customerfd.getFormDb("sales_customer");

String sql = "select id from " + fd.getTableNameByForm() + " ch where unit_code=" + StrUtil.sqlstr(unitCode);
//String sql = "select id from " + fd.getTableNameByForm() + " ch where 1=1";
if (op.equals("search")) {
	sql = "select ch.id from " + fd.getTableNameByForm() + " ch, form_table_sales_customer c where c.unit_code=" + StrUtil.sqlstr(unitCode)+" and ch.customer=c.id";
	if(!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "sales") && !privilege.isUserPrivValid(request, "sales.manager") && privilege.isUserPrivValid(request, "sales.user") ){
		String salesPerson = privilege.getUser(request);
		if(!salesPerson.equals("")){
			sql += " and ch.provider in ("+StrUtil.sqlstr(salesPerson)+")";
		}
	}
	if(!customer.equals("")){
		sql += " and c.customer like " + StrUtil.sqlstr("%" + customer + "%");
	}
	if (!"".equals(dept) && !dept.equals(DeptDb.ROOTCODE)) {
		sql += " and c.id in (select id from form_table_sales_customer where dept_code in ("+deptstrs+"))";
	}
	/**
	if (!customer.equals("") || !"".equals(dept)) {
		sql = "select ch.id from " + fd.getTableNameByForm() + " ch, form_table_sales_customer c where c.unit_code=" + StrUtil.sqlstr(unitCode) + " and ch.customer=c.id";
		if (!customer.equals("")) {
			sql += " and c.customer like " + StrUtil.sqlstr("%" + customer + "%");
		}
		if (!"".equals(dept)) {
			sql += " and c.id in (select id from form_table_sales_customer where dept_code in ("+dept+"))";
		}
	}*/
	if (state!=-1) {
		sql += " and ch.state=" + state;
	}
	if (!status.equals("")) {
		sql += " and ch.sjzt=" + status;
	}
	if (beginDate!=null) {
		sql += " and ch.find_date>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
	}
	if (endDate!=null) {
		sql += " and ch.find_date<" + SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
	}
}else{

	if(!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "sales") && !privilege.isUserPrivValid(request, "sales.manager") && privilege.isUserPrivValid(request, "sales.user") ){
		String salesPerson = privilege.getUser(request);
		if(!salesPerson.equals("")){
			sql += " and provider in ("+StrUtil.sqlstr(salesPerson)+")";
		}
	}
	else if(!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "sales") && privilege.isUserPrivValid(request, "sales.manager")){
		sql += " and customer in (select id from form_table_sales_customer where dept_code in("+dept+"))";
	}
}
sql += " and ch.customer in (select id from form_table_sales_customer) order by " + orderBy + " " + sort;

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
    <td align="center">
    <form action="sales_chance_list.jsp" class="search-form">
    <input name="op" value="search" type="hidden" />
      &nbsp;客户
        <input type="text" id="customer" name="customer" size="10" value="<%=customer%>" />
        <%if (privilege.isUserPrivValid(request, "sales")) {%>
&nbsp;所属部门&nbsp;<select id="dept" name="dept" style="height:24px">
<%
DeptDb lf = new DeptDb(DeptDb.ROOTCODE);
DeptView dv = new DeptView(lf);
dv.ShowDeptAsOptions(out, lf, lf.getLayer()); 
%>
</select>&nbsp;&nbsp;
<script>
$("#dept").find("option[value='<%=dept%>']").attr("selected", true);
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
      阶段
<select id="state" name="state">
<option value="-1">不限</option>
<%
SelectMgr sm = new SelectMgr();
SelectDb sd = sm.getSelect("sales_chance_state");
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
状态
<select id="status" name="status">
<option value="">不限</option>
<%
sd = sm.getSelect("sales_chance_status");
vsd = sd.getOptions();
irsd = vsd.iterator();
while (irsd.hasNext()) {
	SelectOptionDb sod = (SelectOptionDb)irsd.next();
	%>
	<option value="<%=sod.getValue()%>"><%=sod.getName()%></option>
	<%	
}
%>
</select>
<script>
o("state").value = "<%=state%>";
o("status").value = "<%=status%>";
</script>      
      发现日期从
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
          <th width="220" style="cursor:pointer">客户</th>
          <th width="120" style="cursor:pointer">商机建立人</th>
          <th width="120" style="cursor:pointer">所属部门</th>
          <th width="120" style="cursor:pointer">商机阶段</th>
          <th width="120" style="cursor:pointer">商机状态</th>
          <th width="120" style="cursor:pointer">发现日期</th>
          <th width="120" style="cursor:pointer">可能性</th>
          <th width="120" style="cursor:pointer">预计金额</th>
          <th width="120" style="cursor:pointer">操作</th>
        </tr>
        </thead>
      <%
	  	UserMgr um = new UserMgr();
	  	FormDAO fdaoCustomer = new FormDAO();
	  	int i = 0;
		SelectOptionDb sod = new SelectOptionDb();
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long id = fdao.getId();
			fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toLong(fdao.getFieldValue("customer")), customerfd);
			
			String realName = "";
			UserDb user = um.getUserDb(fdao.getCreator());
			realName = user.getRealName();
			
			sql = "select sum(zj) from form_table_sales_chance c, form_table_sales_cha_product p where c.id=p.cws_id and c.id=" + id;
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(sql);
			double sum = 0.0;
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				sum = rr.getDouble(1);
			}
		%>
        <tr>
		  <td><a href="javascript:;" onclick="addTab('<%=realName %>', '<%=Global.getFullRootPath(request) %>/sales/customer_show.jsp?id=<%=fdao.getFieldValue("customer")%>&action=<%=StrUtil.UrlEncode(action)%>&formCode=sales_customer')"><%=fdaoCustomer.getFieldValue("customer")%></a></td>
          <td><%=realName%></td>
          <td><%=getDeptName(getDeptCode(user.getName())) %></td>
          <td><%=sod.getOptionName("sales_chance_state", fdao.getFieldValue("state"))%></td>
          <td><%=sod.getOptionName("sales_chance_status", fdao.getFieldValue("sjzt"))%></td>
          <td><%=fdao.getFieldValue("find_date")%></td>
          <td>
		  <div class="progressBar" style="">
        <%
		  int w = StrUtil.toInt(fdao.getFieldValue("possibility"), 0);
		  %>
              <div class="progressBarFore" style="width:<%=w%>%;">
              </div>
              <div class="progressText">
              <%=fdao.getFieldValue("possibility")%>%
              </div>
          </div>
          </td>
          <td><%=sum%></td>
          <td align="center"><a href="javascript:;" onclick="addTab('<%=realName %>', '<%=Global.getFullRootPath(request) %>/sales/customer_sales_chance_show.jsp?customerId=<%=fdao.getCwsId()%>&parentId=<%=fdao.getCwsId()%>&id=<%=id%>&formCodeRelated=sales_chance&formCode=sales_customer&isShowNav=1')">查看</a></td>
        </tr>
      <%
		}
%>
      </table> 
<%
String querystr = "op=" + op + "&orderBy=" + orderBy + "&sort=" + sort + "&state=" + state + "&status=" + status + "&customer=" + StrUtil.UrlEncode(customer) + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate;
//out.print(paginator.getCurPageBlock("sales_chance_list.jsp?action=" + action + "&" + querystr));
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
	window.location.href = "sales_chance_list.jsp?action=<%=action%>&<%=querystr%>&pagesize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp){
		window.location.href = "sales_chance_list.jsp?action=<%=action%>&<%=querystr%>&CPages=" + newp + "&pagesize=" + flex.getOptions().rp;
		}
}

function rpChange(pageSize) {
	window.location.href = "sales_chance_list.jsp?action=<%=action%>&<%=querystr%>&CPages=<%=curpage%>&pagesize=" + pageSize;
}

function onReload() {
	window.location.reload();
}
</script>
<%!
	String getSalesPerson(String userName){
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(StrUtil.sqlstr(userName));
		
		UserDb ud = new UserDb();
		ud = ud.getUserDb(userName);
		String[] depts = ud.getAdminDepts();
		DeptUserDb dud = null;
		Vector v = null;
		Iterator ir = null;
		
		if(depts!=null){
			for(String dept : depts){
				dud = new DeptUserDb();
				v = dud.list(dept);
				if(v!=null){
					ir = v.iterator();
					while(ir.hasNext()){
						dud = (DeptUserDb)ir.next();
						buffer.append(",");
						buffer.append(StrUtil.sqlstr(dud.getUserName()));
					}
				}
			}
		}
		
		return buffer.toString();
	}

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
