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
long customerId = ParamUtil.getLong(request, "customerId");

String op = ParamUtil.get(request, "op");
String formCode = "sales_service";

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
<%@ include file="../inc/nocache.jsp"%>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script src="<%=Global.getRootPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_js.jsp"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
</head>
<body>
<%@ include file="customer_inc_nav.jsp"%>
<script>
o("menu6").className="current"; 
</script>
<%
		String sql = "select id from " + fd.getTableNameByForm() + " where cws_id=" + customerId + " order by id desc";		

		querystr = "";

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
      <table width="98%" border="0" cellpadding="0" cellspacing="0" id="grid">
      <thead>
        <tr align="center"> 
          <th width=200>客户名称</th>
          <th width=150>联系人</th>
          <th width=150>客户满意度</th>
          <th width=130>服务日期</th>
          <th width=130>操作</th>
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
          <td width="26%"><a href="customer_service_show.jsp?customerId=<%=customerId%>&parentId=<%=customerId%>&id=<%=id%>&formCodeRelated=<%=formCode%>&formCode=sales_customer&isShowNav=1"><%=fdaoCustomer.getFieldValue("customer")%></a></td>
          <td width="20%"><%=fdaoLinkman.getFieldValue("linkmanName")%></td>
          <td width="21%"><%=sod.getOptionName("customer_myd", fdao.getFieldValue("customer_myd"))%></td>
          <td width="16%"><%=fdao.getFieldValue("contact_date")%></td>
          <td width="17%">
			<a href="customer_service_show.jsp?customerId=<%=customerId%>&parentId=<%=customerId%>&id=<%=id%>&formCodeRelated=<%=formCode%>&formCode=sales_customer&isShowNav=1">查看</a>          
          	&nbsp;&nbsp;<a href="customer_service_edit.jsp?customerId=<%=customerId%>&parentId=<%=customerId%>&id=<%=fdao.getId()%>&menuItem=&formCodeRelated=sales_service&formCode=sales_customer&isShowNav=1">编辑</a>&nbsp;&nbsp;&nbsp;<a onclick="jConfirm('确定要删除？','提示',function(r){ if(!r){return;}else{ window.location.href='../visual_del.jsp?id=<%=id%>&formCode=<%=formCode%>&privurl=<%=StrUtil.getUrl(request)%>'}})" style="cursor:pointer">删除</a>&nbsp;&nbsp;		  </td>
        </tr>
      <%
		}
%>
      </table>
      <script>
	$(function(){
		flex = $("#grid").flexigrid
		(
			{
			buttons : [
			{name: '添加', bclass: 'add', onpress : actions}
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
		window.location.href = "customer_service_list.jsp?CPages=" + newp + "&pagesize=" + flex.getOptions().rp+"&customerId=<%=customerId%>&op=<%=op%>";
		}
}

function rpChange(pageSize) {
	window.location.href = "customer_service_list.jsp?CPages=<%=curpage%>&pagesize=" + pageSize+"&customerId=<%=customerId%>&op=<%=op%>";
}

function onReload() {
	window.location.reload();
}
function actions(com, grid) {
	if(com=='添加'){
	window.location.href='customer_service_add.jsp?customerId=<%=customerId%>&amp;parentId=<%=customerId%>&amp;formCode=sales_customer&amp;formCodeRelated=<%=formCode%>';
	}
}
</script>
</body>
</html>
