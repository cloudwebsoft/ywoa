<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "sales.user";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
String formCode = "sales_contract";

String querystr = "";

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

String custerId = ParamUtil.get(request,"customerId");

// 防SQL注入	
if ("".equals(custerId) || !cn.js.fan.db.SQLFilter.isValidSqlParam(custerId)) {
	com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "SQL_INJ sales/customer_contract_list.jsp custerId=" + custerId);
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "param_invalid")));
	return;
}

String custerSql = "select customer from form_table_sales_customer where id="+custerId;
JdbcTemplate jt = new JdbcTemplate();		   
ResultIterator ri = jt.executeQuery(custerSql);	
ResultRecord rr = null;
String customer = "";
if (ri.hasNext()) {
	rr = (ResultRecord)ri.next();
	customer = rr.get(1).toString();
}	
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=fd.getName()%>列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%@ include file="../inc/nocache.jsp"%>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_js.jsp"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
</head>
<body>
<%@ include file="customer_inc_nav.jsp"%>
<script>
o("menu7").className="current"; 
</script>
<%
long customerId = ParamUtil.getLong(request, "customerId", -1);
String sql = "select id from " + fd.getTableNameByForm() + " where customer=" + StrUtil.sqlstr("" + customerId) + " order by id desc";		

querystr = "query=" + StrUtil.UrlEncode(sql) + "&customerId=" + customerId;
// out.print(sql);
int pagesize = ParamUtil.getInt(request, "pagesize", 10);
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
	
String code = "contract_type";
SelectMgr sm = new SelectMgr();

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
<table width="98%" border="0" cellpadding="0" cellspacing="0" id="grid">
	<thead>
  <tr>
    <th width=200>编码</th>
    <th width=200>名称</th>
    <th width=200>类型</th>
    <th width=200>客户</th>
    <th width=200>操作</th>
  </tr>
  </thead>
  <%	
		FormDb fdCustomer = new FormDb();
		fdCustomer = fdCustomer.getFormDb("sales_customer");
  
	  	int i = 0;
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long id = fdao.getId();
			
			com.redmoon.oa.visual.FormDAO fdaoCustomer = new com.redmoon.oa.visual.FormDAO(StrUtil.toInt(fdao.getFieldValue("customer")), fdCustomer);			
			if(!customer.equals(fdaoCustomer.getFieldValue("customer"))){
			    continue;
			}
		%>
		
  <tr align="center">
    <td width="19%"><a href="customer_contract_show.jsp?id=<%=id%>&amp;formCode=<%=formCode%>"><%=fdao.getFieldValue("contact_no")%></a></td>
    <td width="27%"><%=fdao.getFieldValue("contact_name")%></td>
    <td width="21%">
	<%
		String optName = "";
		SelectDb sd = sm.getSelect(code);
		if (sd.getType() == SelectDb.TYPE_LIST) {
			SelectOptionDb sod = new SelectOptionDb();
			optName = sod.getOptionName(code, fdao.getFieldValue("contract_type"));
		} else {
			TreeSelectDb tsd = new TreeSelectDb();
			tsd = tsd.getTreeSelectDb(fdao.getFieldValue("contract_type"));
			optName = tsd.getName();
		}
		out.print(optName);		
	%>	
	</td>
    <td width="16%"><%=fdaoCustomer.getFieldValue("customer")%></td>
    <td width="17%"><a href="customer_contract_show.jsp?id=<%=id%>&amp;formCode=<%=formCode%>&customerId=<%=custerId%>">查看</a></td>
  </tr>
  <%
		}
%>
</table>

</body>
<script>
	$(function(){
		flex = $("#grid").flexigrid
		(
			{
				buttons : [
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
	
}

function changePage(newp) {
	if (newp){
		window.location.href = "customer_contract_list.jsp?CPages=" + newp + "&pagesize=" + flex.getOptions().rp+"&customerId=<%=customerId%>&op=<%=op%>";
		}
}

function rpChange(pageSize) {
	window.location.href = "customer_contract_list.jsp?CPages=<%=curpage%>&pagesize=" + pageSize+"&customerId=<%=customerId%>&op=<%=op%>";
}

function onReload() {
	window.location.reload();
}
function actions(com, grid) {
}
</script>
</html>
