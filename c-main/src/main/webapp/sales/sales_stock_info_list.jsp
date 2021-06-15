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
String formCode = "sales_stock_info";

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
<title>出入库记录</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

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

String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");
java.util.Date beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
java.util.Date endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");

String stock = ParamUtil.get(request, "stock");
String opType = ParamUtil.get(request, "opType");

try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "stock", stock, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "opType", opType, getClass().getName());
	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "stock", stock, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "opType", opType, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "strBeginDate", strBeginDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "strEndDate", strEndDate, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "stock", stock, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "opType", opType, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

String unitCode = privilege.getUserUnitCode(request);

String sql = "select s.id from " + fd.getTableNameByForm() + " s where unit_code=" + StrUtil.sqlstr(unitCode);		
if (op.equals("search")) {
	if (!stock.equals("")) {
		sql = "select s.id from " + fd.getTableNameByForm() + " s, form_table_sales_stock c where s.stock=c.id and c.unit_code=" + StrUtil.sqlstr(unitCode) + " and c.id = " + StrUtil.sqlstr(stock);
	}
	if (!opType.equals("")) {
		sql += " and s.op_type=" + opType;
	}
	if (beginDate!=null) {
		sql += " and s.create_date>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
	}
	if (endDate!=null) {
		sql += " and s.create_date<=" + SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
	}	
}

sql += " order by s.id desc";
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
	<form id="formSearch" name="formSearch" method="get" action="sales_stock_info_list.jsp">
	<input name="op" value="search" type="hidden" />
        仓库
        <select id="stock" name="stock">
          <option value="">不限</option>
          <%
		  Iterator irstock = fdao.list("sales_stock", "select id from form_table_sales_stock order by id").iterator();
		  while (irstock.hasNext()) {
		  	fdao = (FormDAO)irstock.next();
			%>
			<option value="<%=fdao.getId()%>"><%=fdao.getFieldValue("name")%></option>
			<%
		  }
		  %>
        </select>
        类型
        <select id="opType" name="opType">
        <option value="">不限</option>
		<option value="1">入库</option>
		<option value="0">出库</option>
        </select>
        日期从
        <input id="beginDate" name="beginDate" size="10" value="<%=strBeginDate%>" />
        至
        <input id="endDate" name="endDate" size="10" value="<%=strEndDate%>" />
        <script>
o("stock").value = "<%=stock%>";
o("opType").value = "<%=opType%>";
</script>
        &nbsp;
          <input class="tSearch" name="submit" type=submit value="搜索">
</form></td>
    </tr>
  </table>
      <table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
      	<thead>
        <tr>
          <th width="130">仓库名称</th>
          <th width="130">操作类型</th>
          <th width="130">日期</th>
          <th width="130">操作人员</th>
          <th width="130">备注</th>
          <th width="130">操作</th>
        </tr>
        </thead>
      <%
	  	UserMgr um = new UserMgr();
	  	FormDAO fdaoStock = new FormDAO();
	  	int i = 0;
		SelectOptionDb sod = new SelectOptionDb();
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long id = fdao.getId();
			fdaoStock = fdaoStock.getFormDAO(StrUtil.toLong(fdao.getFieldValue("stock")), stockfd);
			
			String realName = "";
			UserDb user = um.getUserDb(fdao.getCreator());
			realName = user.getRealName();
			
			String opType2 = fdao.getFieldValue("op_type").equals("1")?"入库":"出库";
		%>
        <tr>
		  <td><%=fdaoStock.getFieldValue("name")%></td>
          <td><%=opType2%></td>
          <td><%=fdao.getFieldValue("create_date")%></td>
          <td><%=realName%></td>
          <td><%=StrUtil.getNullStr(fdao.getFieldValue("remark"))%>
          </td>
          <td align="center">
          <a href="javascript:;" onclick="addTab('<%=opType2%>单', '<%=request.getContextPath()%>/visual/module_show.jsp?id=<%=id%>&code=sales_stock_info')">查看</a>&nbsp;&nbsp;&nbsp;&nbsp;
          <a href="javascript:;" onclick="openWin('../visual/module_edit.jsp?id=<%=id%>&code=sales_stock_info&isShowNav=0', 800, 600)">编辑</a>
          &nbsp;&nbsp;&nbsp;<a onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{ window.location.href='../visual_del.jsp?id=<%=id%>&formCode=<%=formCode%>&amp;privurl=<%=StrUtil.getUrl(request)%>'}})" style="cursor:pointer">删除</a></span> </td>
        </tr>
      <%
		}
%>
      </table>
<%
String querystr = "op=" + op + "&stock=" + stock + "&opType=" + opType + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate;
//out.print(paginator.getCurPageBlock("sales_stock_info_list.jsp?" + querystr));
%>
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
	window.location.href = "sales_stock_info_list.jsp?<%=querystr%>&pagesize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp){
		window.location.href = "sales_stock_info_list.jsp?<%=querystr%>&CPages=" + newp + "&pagesize=" + flex.getOptions().rp;
		}
}

function rpChange(pagesize) {
	window.location.href = "sales_stock_info_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pagesize=" + pagesize;
}

function onReload() {
	window.location.reload();
}
</script>
</html>
