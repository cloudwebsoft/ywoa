<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.sale.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormMgr"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
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
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script type="text/javascript" src="../js/flexigrid.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

<%@ include file="../inc/nocache.jsp"%>
</head>
<body>
<%@ include file="sales_user_inc_menu_top.jsp"%>
<script>
o("menu2").className="current"; 
</script>
<%
String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action"); // action为manage时表示为销售总管理员方式
if (action.equals("manage")) {
	if (!privilege.isUserPrivValid(request, "sales"))
	{
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

String priv = "sales.user";
if (!privilege.isUserPrivValid(request, priv) && !privilege.isUserPrivValid(request, "sales")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
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

if (op.equals("del")) {
	int parentId = ParamUtil.getInt(request, "parentId", -1);
	if (parentId==-1) {
		out.print(SkinUtil.makeErrMsg(request, "缺少父模块记录的ID！"));
		return;
	}	
	if (!SalePrivilege.canUserManageCustomer(request, parentId)) {
		%>
		<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
		<%
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"), true));
		return;		
	}
	FormMgr fm = new FormMgr();
	FormDb fdRelated = fm.getFormDb(formCode);
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fdRelated);
	try {
		if (fdm.del(request)) {
			out.print(StrUtil.jAlert_Redirect("删除成功！","提示", "sales_user_chance_list.jsp?action=" + action + "&userName=" + StrUtil.UrlEncode(userName)));
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	return;
}

FormDb customerfd = new FormDb();
customerfd = customerfd.getFormDb("sales_customer");

String sql = "select ch.id from " + fd.getTableNameByForm() + " ch, form_table_sales_customer c where ch.cws_id=c.id and c.sales_person=" + StrUtil.sqlstr(userName);

String customer = ParamUtil.get(request, "customer");
String state = ParamUtil.get(request, "state");
String status = ParamUtil.get(request, "status");

String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");
java.util.Date beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
java.util.Date endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");

try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "state", state, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "status", status, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "customer", customer, getClass().getName());
	
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

if (op.equals("search")) {
	sql = "select ch.id from " + fd.getTableNameByForm() + " ch, form_table_sales_customer c where ch.cws_id=c.id and c.sales_person=" + StrUtil.sqlstr(userName) + " and ch.customer=c.id ";
	if (!customer.equals("")) {
		sql+="and c.customer like " + StrUtil.sqlstr("%" + customer + "%");
	}
	if (!state.equals("")) {
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
}

sql += " order by ch.find_date desc" ;		

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
<table id="searchTable" width="100%" border="0" cellpadding="0" cellspacing="0"><tr><td align="center">
<form id="formSearch" name="formSearch" action="sales_user_chance_list.jsp" method="get">
<input name="op" value="search" type="hidden" />
<input name="userName" value="<%=userName%>" type="hidden" />
&nbsp;客户
<input type="text" id="customer" name="customer" size="10" value="<%=customer%>" />
阶段
<select id="state" name="state">
<option value="">不限</option>
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
发现时间从
<input type="text" id="beginDate" name="beginDate" size="10" value="<%=strBeginDate%>" />
至
<input type="text" id="endDate" name="endDate" size="10" value="<%=strEndDate%>" />
&nbsp;
<input class="tSearch" name="submit" type=submit value="搜索">
</form>
</td></tr></table>

      <table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
      	<thead>
        <tr>
          <th width="120" style="cursor:pointer">编号</th>
          <th width="300" style="cursor:pointer">客户</th>
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
          <td><%=StrUtil.getNullStr(fdao.getFieldValue("code"))%></td>
		  <td><a href="customer_show.jsp?id=<%=fdao.getFieldValue("customer")%>&action=<%=StrUtil.UrlEncode(action)%>&formCode=sales_customer" target="_blank"><%=fdaoCustomer.getFieldValue("customer")%></a></td>
          <td><%=sod.getOptionName("sales_chance_state", fdao.getFieldValue("state"))%></td>
          <td><%=sod.getOptionName("sales_chance_status", fdao.getFieldValue("sjzt"))%></td>
          <td><%=fdao.getFieldValue("find_date")%></td>
          <td>
		  <div class="progressBar" style="">
              <div class="progressBarFore" style="width:<%=fdao.getFieldValue("possibility")%>%;">
              </div>
              <div class="progressText">
              <%=fdao.getFieldValue("possibility")%>%
              </div>
          </div>
          </td>
          <td><%=sum%></td>
          <td align="center"><a href="javascript:;" onclick="addTab('商机<%=fdao.getFieldValue("code")%>', '<%=request.getContextPath()%>/sales/customer_sales_chance_show.jsp?customerId=<%=fdao.getCwsId()%>&parentId=<%=fdao.getCwsId()%>&id=<%=id%>&formCodeRelated=sales_chance&formCode=sales_customer&isShowNav=1')">查看</a>&nbsp;&nbsp;
          <%if (SalePrivilege.canUserManageCustomer(request, StrUtil.toLong(fdao.getCwsId()))) {%>
            <a href="javascript:;" onclick="addTab('商机<%=fdao.getFieldValue("code")%>', '<%=request.getContextPath()%>/sales/customer_sales_chance_edit.jsp?customerId=<%=fdao.getCwsId()%>&amp;parentId=<%=fdao.getCwsId()%>&amp;id=<%=id%>&amp;formCodeRelated=sales_chance&amp;formCode=sales_customer')">编辑</a>&nbsp;&nbsp;
			<a onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{ window.location.href='sales_user_chance_list.jsp?op=del&customerId=<%=fdao.getCwsId()%>&id=<%=id%>&parentId=<%=fdao.getCwsId()%>';}})" style="cursor:pointer">删除</a>
          <%}%>
          </td>
        </tr>
      <%
		}
%>
      </table>
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
		
		o("state").value = "<%=state%>";
		o("status").value = "<%=status%>";		
});

function changeSort(sortname, sortorder) {
	window.location.href = "sales_user_chance_list.jsp?pagesize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp){
		window.location.href = "sales_user_chance_list.jsp?CPages=" + newp + "&pagesize=" + flex.getOptions().rp;
		}
}

function rpChange(pageSize) {
	window.location.href = "sales_user_chance_list.jsp?CPages=<%=curpage%>&pagesize=" + pageSize;
}

function onReload() {
	window.location.reload();
}
</script>
</body>
</html>
