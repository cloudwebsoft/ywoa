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
<%@ page import = "com.redmoon.oa.sale.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String formCode = "sales_order";

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
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../inc/sortabletable.js"></script>
<script type="text/javascript" src="../inc/columnlist.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/jquery.raty.min.js"></script>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
<%@ include file="../inc/nocache.jsp"%>
</head>
<body>
<%@ include file="sales_user_inc_menu_top.jsp"%>
<script>
o("menu4").className="current"; 
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

FormDb customerfd = new FormDb();
customerfd = customerfd.getFormDb("sales_customer");

String customer = ParamUtil.get(request, "customer");
String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");
java.util.Date beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
java.util.Date endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");

String orderSource = ParamUtil.get(request, "orderSource");
String payType = ParamUtil.get(request, "payType");

try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "customer", customer, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "payType", payType, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "orderSource", orderSource, getClass().getName());
	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "userName", userName, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "payType", payType, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "customer", customer, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "strBeginDate", strBeginDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "strEndDate", strEndDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;	
}

String sql = "select o.id from " + fd.getTableNameByForm() + " o, form_table_sales_customer c where o.customer=c.id and c.sales_person=" + StrUtil.sqlstr(userName);		
if (op.equals("search")) {
	if (!customer.equals("")) {
		sql = "select o.id from " + fd.getTableNameByForm() + " o, form_table_sales_customer c where c.sales_person=" + StrUtil.sqlstr(userName) + " and o.customer=c.id and c.customer like " + StrUtil.sqlstr("%" + customer + "%");
	}
	if (!orderSource.equals("")) {
		sql += " and o.source=" + orderSource;
	}
	if (!payType.equals("")) {
		sql += " and o.pay_type=" + StrUtil.sqlstr(payType);
	}
	if (beginDate!=null) {
		sql += " and o.order_date>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
	}
	if (endDate!=null) {
		sql += " and o.order_date<" + SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
	}	
}

sql += " order by o.id desc";
// out.print(sql);

int pagesize = ParamUtil.getInt(request, "pagesize", 20);
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
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}
%>
  <table id="searchTable" width="100%" border="0" cellpadding="0" cellspacing="0">
    <tr>
      <td width="100%" align="center">
<form id="formSearch" name="formSearch" method="get" action="sales_user_order_list.jsp">
      	<input name="op" value="search" type="hidden" />
	  	<input name="userName" value="<%=userName%>" type="hidden" />
        &nbsp;客户
        <input type="text" id="customer" name="customer" size="10" value="<%=customer%>" />
        来源
        <select id="orderSource" name="orderSource">
          <option value="">不限</option>
          <%
SelectMgr sm = new SelectMgr();
SelectDb sd = sm.getSelect("sales_order_source");
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
        支付方式
        <select id="payType" name="payType">
          <option value="">不限</option>
          <%
sd = sm.getSelect("pay_type");
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
        促成日期从
        <input type="text" id="beginDate" name="beginDate" size="10" value="<%=strBeginDate%>" />
        至
        <input type="text" id="endDate" name="endDate" size="10" value="<%=strEndDate%>" />
        <script>
o("orderSource").value = "<%=orderSource%>";
o("payType").value = "<%=payType%>";
</script>
        &nbsp;
			<input class="tSearch" name="submit" type=submit value="搜索">
</form></td>
    </tr>
  </table>
	   <!-- 该处无法关联客户而表单中需要提取与客户关联的商机，将来可用表单与宏控件解决此问题 -->
	  <!-- <td width="7%" height="30" align="left" backgroun="images/title1-back.gif"><input type="button" class="btn" value="添加" onclick="window.location.href='sales_user_order_add.jsp?formCode=sales_order'" /></td> -->

      <table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
      	<thead>
        <tr>
          <th width="120" style="cursor:pointer">客户</td>
          <th width="120" style="cursor:pointer">订单来源</td>
          <th width="120" style="cursor:pointer">订单状态</td>
          <th width="120" style="cursor:pointer">支付方式</td>
          <th width="120" style="cursor:pointer">订单标值</td>
          <th width="120" style="cursor:pointer">促成日期</td>
          <th width="120" style="cursor:pointer">操作</td>
        </tr>
        </thead>
      <%
	  	UserMgr um = new UserMgr();
	  	FormDAO fdaoCustomer = new FormDAO();
	  	ArrayList<String> list = new ArrayList<String>();
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
			String starLevel = RatyCtl.render(request, fdao.getFormField("order_value"), true);
			list.add(starLevel);
		%>
        <tr>
		  <td><a href="customer_show.jsp?id=<%=fdao.getFieldValue("customer")%>&action=<%=StrUtil.UrlEncode(action)%>&formCode=sales_customer" target="_blank"><%=fdaoCustomer.getFieldValue("customer")%></a></td>
          <td><%=sod.getOptionName("sales_order_source", fdao.getFieldValue("source"))%></td>
          <td><%=sod.getOptionName("sales_order_state", fdao.getFieldValue("status"))%></td>
          <td><%=sod.getOptionName("pay_type", fdao.getFieldValue("pay_type"))%></td>
          <td><%=starLevel%>
          </td>
          <td><%=fdao.getFieldValue("order_date")%></td>
          <td align="center">
          <a href="javascript:;" onclick="addTab('<%=fdaoCustomer.getFieldValue("customer")%>订单', '<%=request.getContextPath()%>/visual/module_mode1_show.jsp?parentId=<%=id%>&id=<%=id%>&formCode=sales_order')">查看</a>&nbsp;&nbsp;&nbsp;&nbsp;
          <%if (SalePrivilege.canUserManageCustomer(request, StrUtil.toLong(fdao.getCwsId()))) {%>
            <a href="javascript:;" onclick="addTab('<%=fdaoCustomer.getFieldValue("customer")%>订单', '<%=request.getContextPath()%>/sales/customer_sales_order_edit.jsp?customerId=<%=fdao.getCwsId()%>&parentId=<%=fdao.getCwsId()%>&id=<%=id%>&formCodeRelated=sales_order&formCode=sales_customer&isShowNav=1')">编辑</a>
            &nbsp;&nbsp;&nbsp;<a onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{ window.location.href='../visual_del.jsp?id=<%=id%>&amp;action=<%=action%>&formCode=<%=formCode%>&amp;privurl=<%=StrUtil.getUrl(request)%>';}})" style="cursor:pointer">删除</a></span> </td>
          <%}%>
          </td>
        </tr>
      <%
		}
%>
      </table>
<%
String querystr = "op=" + op + "&userName=" + StrUtil.UrlEncode(userName) + "&customer=" + StrUtil.UrlEncode(customer) + "&orderSource=" + orderSource + "&payType=" + payType + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate;
%>
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
		<%for (int j = 0; j < list.size(); j++) {
			int index1 = list.get(j).indexOf("order_value_raty");
			String rand="",scpt="";
			if(index1 > 0){
				rand = list.get(j).substring(index1, index1 + 21);
				index1 = list.get(j).indexOf("<script>");
				int index2 = list.get(j).indexOf("</script>");
				if (index2 > 0)
					scpt = list.get(j).substring(index1 + 8, index2);
			}
		%>
		$('#<%=rand%>').html('');
		<%=scpt%>
		<%}%>
});

function changeSort(sortname, sortorder) {
	window.location.href = "sales_user_order_list.jsp?<%=querystr%>&pagesize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp){
		window.location.href = "sales_user_order_list.jsp?<%=querystr%>&CPages=" + newp + "&pagesize=" + flex.getOptions().rp;
		}
}

function rpChange(pagesize) {
	window.location.href = "sales_user_order_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pagesize=" + pagesize;
}

function onReload() {
	window.location.reload();
}
</script>
</html>
