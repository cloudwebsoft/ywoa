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
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
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
<title>库存明细</title>
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
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
<script src="../js/jquery.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../js/flexigrid.js"></script>
<%@ include file="../inc/nocache.jsp"%>
</head>
<body>
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

String priv = "sales.stock";
if (!privilege.isUserPrivValid(request, priv) && !privilege.isUserPrivValid(request, "sales")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

FormDb stockfd = new FormDb();
stockfd = stockfd.getFormDb("sales_stock");
FormDb productfd = new FormDb();
productfd = productfd.getFormDb("sales_product_info");

int pagesize = ParamUtil.getInt(request, "pagesize", 10);
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

String unitCode = privilege.getUserUnitCode(request);
String productName = ParamUtil.get(request, "productName");

try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "productName", productName, getClass().getName());	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

String sql = "select id from oa_sales_stock_product s where s.unit_code=" + StrUtil.sqlstr(unitCode);
long stock = ParamUtil.getLong(request, "stock", -1);
if (!productName.equals("")) {
	sql = "select s.id from oa_sales_stock_product s, form_table_sales_product_info p where s.product_id=p.id and s.unit_code=" + StrUtil.sqlstr(unitCode) + " and p.product_name like " + StrUtil.sqlstr("%" + productName + "%");
}
if (stock!=-1) {
	sql += " and s.stock_id=" + stock;
}
sql += " order by s.id desc";

// out.print(sql);

SalesStockProductDb sspd = new SalesStockProductDb();
ListResult lr = sspd.listResult(sql, curpage, pagesize);

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
		<form id="formSearch" name="formSearch" class="search-form" method="get" action="sales_stock_product_list.jsp">
		<input name="op" value="search" type="hidden" />
        &nbsp;仓库
        <select id="stock" name="stock" >
          <option value="-1">不限</option>
          <%
		  FormDAO fdao = new FormDAO();
		   sql = "select id from form_table_sales_stock where unit_code=" + StrUtil.sqlstr(unitCode) +  " order by id";
		  Iterator irstock = fdao.list("sales_stock", sql).iterator();
		  while (irstock.hasNext()) {
		  	fdao = (FormDAO)irstock.next();
			%>
			<option value="<%=fdao.getId()%>"><%=fdao.getFieldValue("name")%></option>
			<%
		  }
		  %>
        </select>
        <script>
		o("stock").value = "<%=stock%>";
		</script>
        产品
        <input name="productName" size="8" value="<%=productName%>" />
        &nbsp;
          <input class="tSearch" name="submit" type=submit value="搜索">
</form>
        </td>
    </tr>
  </table>
      <table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
      	<thead>
        <tr>
          <th width="200">仓库名称</th>
          <th width="200">产品</th>
          <th width="200">数量</th>
        </tr>
        </thead>
      <%
	  	UserMgr um = new UserMgr();
	  	FormDAO fdaoStock = new FormDAO();
		FormDAO fdaoProduct = new FormDAO();
	  	int i = 0;
		SelectOptionDb sod = new SelectOptionDb();
		while (ir!=null && ir.hasNext()) {
			sspd = (SalesStockProductDb)ir.next();
			i++;
			long id = sspd.getLong("id");
			fdaoStock = fdaoStock.getFormDAO(sspd.getLong("stock_id"), stockfd);
			fdaoProduct = fdaoProduct.getFormDAO(sspd.getLong("product_id"), productfd);
			
			String realName = "";
			UserDb user = um.getUserDb(fdao.getCreator());
			realName = user.getRealName();
		%>
        <tr>
		  <td><%=fdaoStock.getFieldValue("name")%></td>
          <td><a href="product_show.jsp?id=<%=sspd.getLong("product_id")%>&formCode=sales_product_info" target="_blank"><%=fdaoProduct.getFieldValue("product_name")%></a></td>
          <td><%=sspd.getInt("num")%></td>
        </tr>
      <%
		}
%>
      </table>
<%
String querystr = "op=" + op + "&stock=" + stock + "&productName=" + StrUtil.UrlEncode(productName);
//out.print(paginator.getCurPageBlock("sales_stock_product_list.jsp?" + querystr));
%>
</body>
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
	window.location.href = "sales_stock_product_list.jsp?<%=querystr%>&pagesize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp){
		window.location.href = "sales_stock_product_list.jsp?<%=querystr%>&CPages=" + newp + "&pagesize=" + flex.getOptions().rp;
		}
}

function rpChange(pagesize) {
	window.location.href = "sales_stock_product_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pagesize=" + pagesize;
}

function onReload() {
	window.location.reload();
}
</script>
</html>
