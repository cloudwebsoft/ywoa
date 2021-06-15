<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.account.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.BasicDataMgr"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="com.redmoon.oa.util.ExchangeRateDb"%>
<%@page import="cn.js.fan.db.Paginator"%>
<%@page import="cn.js.fan.db.ListResult"%>
<%@page import="com.redmoon.oa.basic.SelectMgr"%>
<%@page import="com.redmoon.oa.basic.SelectDb"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="com.redmoon.oa.basic.SelectOptionDb"%>
<%@page import="com.redmoon.oa.util.ExchangeRateMgr"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<% 
	if (!privilege.isUserPrivValid(request, "admin")) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	String op = ParamUtil.get(request, "op");
	
	int pagesize = ParamUtil.getInt(request, "pageSize", 25);	
    String CPages  = ParamUtil.get(request,"CPages");
    if (op.equals("delBatch")) {
	    try {
			ExchangeRateMgr er = new ExchangeRateMgr();
			er.delBatch(request);
			out.print(StrUtil.Alert_Redirect("操作成功！", "exchange_rate_list.jsp?CPages="+CPages+"&pageSize="+pagesize));
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
			return;
		}
   } else if(op.equals("del")){
		int ids  = Integer.parseInt(ParamUtil.get(request, "id"));
		try {
			ExchangeRateMgr dm = new ExchangeRateMgr();
			dm.del(ids);
			out.print(StrUtil.Alert_Redirect("操作成功！", "exchange_rate_list.jsp?CPages="+CPages+"&pageSize="+pagesize));
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
			return;
		}
	}
		
	String what = ParamUtil.get(request, "what");	
	String querystr = "op=" + op + "&what=" + StrUtil.UrlEncode(what);
	Paginator paginator = new Paginator(request);
	int curpage = paginator.getCurPage();
	String sql = "select id, money_type, rate, modify_date from oa_exchange_rate where 1=1 ";
	if(op.equals("search"))	{
		if (!what.equals("")) {
	   		sql += " and money_type like " + StrUtil.sqlstr("%" +what+ "%")+"";
	 	}
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>汇率信息列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
<script>
	function selAllCheckBox(checkboxname){
	var checkboxboxs = document.getElementsByName(checkboxname);
		if (checkboxboxs!=null)
		{
			// 如果只有一个元素
			if (checkboxboxs.length==null) {
			if(checkboxboxs.checked==false)
			{
			  checkboxboxs.checked = true;
			}else{ checkboxboxs.checked = false;}
				
			}
			for (i=0; i<checkboxboxs.length; i++)
			{
			 if(checkboxboxs[i].checked==false)
			{
			  checkboxboxs[i].checked = true;
			}else{ checkboxboxs[i].checked = false;}
			}
		}
	}
	
	function getIds() {
	    var checkedboxs = 0;
		var checkboxboxs = document.getElementsByName("ids");
		var id = "";
		if (checkboxboxs!=null){
			// 如果只有一个元素
			if (checkboxboxs.length==null) {
				if (checkboxboxs.checked){
				   checkedboxs = 1;
				   id = checkboxboxs.value;
				}
			}
			for (i=0; i<checkboxboxs.length; i++)
			{
				if (checkboxboxs[i].checked){
				   checkedboxs = 1;
				   if (id=="")
					   id = checkboxboxs[i].value;
				   else
					   id += "," + checkboxboxs[i].value;
				}
			}
		}
		return id;
	}
</script>
</head>
<body>
<% 
	ExchangeRateDb dd1 = new ExchangeRateDb(); 
	ListResult lr = dd1.listResult(sql, curpage, pagesize); 
	long total = lr.getTotal();
	Iterator ir = lr.getResult().iterator(); 
	paginator.init(total, pagesize); 
	// 设置当前页数和总页数 
	int totalpages = paginator.getTotalPages(); 
	if (totalpages==0) 
	{ 
		curpage = 1; 
		totalpages = 1; 
	} 
%>
<table id="searchTable" width="98%" border="0" align="center" cellpadding="0" cellspacing="0" style=" margin-left:5px;">
  <tr>
    <td width="48%" height="30" align="left">
    	<form action="exchange_rate_list.jsp" method="get">
	        <input name="op" value="search" type="hidden" />
    &nbsp;&nbsp;币种&nbsp;<input name="what" size="15" value="" />
	        <input name="submit" type="submit" class="tSearch" value="搜索" />
      	</form>
	</td>
  </tr>
</table>
<table id="grid" border="0">
  <thead>
    <tr>
      <th width="30"><input name="ids" type="checkbox" onClick="selAllCheckBox('ids')"></th>
      <th width="128" abbr="money_type">币种</th>
      <th width="128" >汇率</th>
      <th width="180" abbr="create_date">更新日期</th>
      <th width="316">操作</th>
    </tr>
  </thead>
  <tbody>
<%
	 int id=0;
	 String moneytype ="";
	 float rate=0.0f;
	 SelectOptionDb sod = new SelectOptionDb();
	 while(ir.hasNext()){
		dd1 = (ExchangeRateDb) ir.next();
		id = dd1.getInt("id");
		moneytype = dd1.getString("money_type");
		rate=dd1.getFloat("rate");
		String bzName = sod.getOptionName("bz", moneytype);
%>
    <tr align="center">
      <td><input type="checkbox" id="ids"  name="ids" value="<%=id%>"/></td>
      <td><%=bzName%></td>
      <td><%=rate%></td>
      <td><%=DateUtil.format((Date)dd1.get("modify_date"), "yyyy-MM-dd")%></td>
      <td><a href="exchange_rate_edit.jsp?id=<%=id%>&CPages=<%=curpage%>&pageSize=<%=pagesize%>">修改</a> &nbsp;&nbsp;&nbsp;&nbsp;<a onclick="del(<%=id%>)" href="javascript:void(0)">删除</a></td>
    </tr>
<%}%>
  </tbody>
</table>
</body>
<script>
	function doOnToolbarInited() {
	}
	
	var flex;
	
	function changeSort(sortname, sortorder) {
		window.location.href = "exchange_rate_list.jsp?pageSize=" + flex.getOptions().rp ;
	}
	
	function changePage(newp) {
		if (newp)
			window.location.href = "exchange_rate_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
	}
	
	function rpChange(pageSize) {
		window.location.href = "exchange_rate_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
	}
	
	function onReload() {
		window.location.reload();
	}
	
	flex = $("#grid").flexigrid
	(
		{
		buttons : [
			{name: '添加', bclass: 'add', onpress : action},
			{name: '删除', bclass: 'delete', onpress : action},
			//{name: '导入住户', bclass: 'export', onpress : action},
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
	
	function action(com, grid) {
		if (com=="删除") {
			doDel();
		}else if(com=="添加")
		{
		  doAdd();
		//}else if(com=="导入住户")
		//{
		 //doImport();
		}
		
	}
	
	function doDel() {
		var ids = getCheckboxValue("ids");
		if (ids=="") {
			alert("请选择币种！");
			return;
		}
		if(confirm("您确定要删除吗？"))	{
			window.location.href ="exchange_rate_list.jsp?op=delBatch&id="+ids+"&ids=" + ids;
		}
	}
	function doAdd() {
	  window.location.href = "exchange_rate_add.jsp";
	}
	function del(id) {
	    var pageSize = <%=pagesize%>;
		var CPages = <%=curpage%>;
	
	
		if(confirm("您确定要删除吗？"))
		{
		  window.location.href = "exchange_rate_list.jsp?op=del&id="+id+"&pageSize="+pageSize+"&CPages="+CPages;
		}
	}

</script>
</html>