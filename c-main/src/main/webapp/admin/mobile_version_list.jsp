<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.account.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.BasicDataMgr"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.android.SystemUpMgr"%>
<%@page import="cn.js.fan.db.Paginator"%>
<%@page import="cn.js.fan.db.ListResult"%>
<%@page import="com.redmoon.oa.basic.SelectMgr"%>
<%@page import="com.redmoon.oa.basic.SelectDb"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="com.redmoon.oa.basic.SelectOptionDb"%>
<%@ page import="com.redmoon.oa.android.SystemUpDb"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<% 
	if (!privilege.isUserPrivValid(request, "admin")) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	String op = ParamUtil.get(request, "op");
	
	int pagesize = ParamUtil.getInt(request, "pageSize", 25);	
    String CPages  = ParamUtil.get(request,"CPages");
    
		
	String what = ParamUtil.get(request, "what");	
	String querystr = "op=" + op + "&what=" + StrUtil.UrlEncode(what);
	Paginator paginator = new Paginator(request);
	int curpage = paginator.getCurPage();
	String sql = "select id from oa_version where 1=1 ";
	if(op.equals("search"))	{
		if (!what.equals("")) {
	   		sql += " and version_name like " + StrUtil.sqlstr("%" +what+ "%")+"";
	 	}
	}
	
	sql += " order by id desc";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<title>信息列表</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
	<style>
		.search-form input, select {
			vertical-align: middle;
		}
	</style>
	<script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	<script type="text/javascript" src="../js/flexigrid.js"></script>
	<script>
		function selAllCheckBox(checkboxname) {
			var checkboxboxs = document.getElementsByName(checkboxname);
			if (checkboxboxs != null) {
				// 如果只有一个元素
				if (checkboxboxs.length == null) {
					checkboxboxs.checked = true;
				}
				for (i = 0; i < checkboxboxs.length; i++) {
					checkboxboxs[i].checked = true;
				}
			}
		}

		function deSelAllCheckBox(checkboxname) {
			var checkboxboxs = document.getElementsByName(checkboxname);
			if (checkboxboxs != null) {
				if (checkboxboxs.length == null) {
					checkboxboxs.checked = false;
				}
				for (i = 0; i < checkboxboxs.length; i++) {
					checkboxboxs[i].checked = false;
				}
			}
		}

		function getIds() {
			var checkedboxs = 0;
			var checkboxboxs = document.getElementsByName("ids");
			var id = "";
			if (checkboxboxs != null) {
				// 如果只有一个元素
				if (checkboxboxs.length == null) {
					if (checkboxboxs.checked) {
						checkedboxs = 1;
						id = checkboxboxs.value;
					}
				}
				for (i = 0; i < checkboxboxs.length; i++) {
					if (checkboxboxs[i].checked) {
						checkedboxs = 1;
						if (id == "")
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
	if (op.equals("delBatch")) {
	    try {
			SystemUpMgr er = new SystemUpMgr();
			er.delBatch(request);
			out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "mobile_version_list.jsp?CPages="+CPages+"&pageSize="+pagesize));
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
			return;
		}
   } else if(op.equals("del")){
		int ids  = Integer.parseInt(ParamUtil.get(request, "id"));
		try {
			SystemUpMgr dm = new SystemUpMgr();
			dm.del(ids);
			out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "mobile_version_list.jsp?CPages="+CPages+"&pageSize="+pagesize));
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
			return;
		}
	}
	SystemUpDb dd1 = new SystemUpDb(); 
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
			<form class="search-form" action="mobile_version_list.jsp" method="get">
				<input name="op" value="search" type="hidden"/>
				&nbsp;&nbsp;版本信息&nbsp;
				<input name="what" size="15" value="<%=what%>"/>
				<input name="submit" type="submit" class="tSearch" value="搜索"/>
			</form>
		</td>
	</tr>
</table>
<table width="815" border="0" id="grid">
  <thead>
    <tr>
      <th width="40"><input name="checkbox" type="checkbox" onClick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')"></th>
      <th width="92" abbr="money_type">版本号</th>
      <th width="541" >版本信息</th>
      <th width="92">客户端</th>
      <th width="124">操作</th>
    </tr>
  </thead>
  <tbody>
<%
	 String version_num = "";
	 String version_name ="";
	 String client = "";
	 int id = 0;
	 float rate=0.0f;
	 while(ir.hasNext()){
		dd1 = (SystemUpDb) ir.next();
		id = dd1.getInt("id");
 	    version_num = dd1.getString("version_num");
		version_name = dd1.getString("version_name");
		client = dd1.getString("client");
		
%>
    <tr align="center">
      <td><input type="checkbox" id="ids"  name="ids" value="<%=id%>"/></td>
      <td><%=version_num%></td>
      <td><%=StrUtil.toHtml(version_name)%></td>
         <td><%=StrUtil.getNullStr(client)%></td>
      <td><a href="mobile_version_edit.jsp?id=<%=id%>&CPages=<%=curpage%>&pageSize=<%=pagesize%>">修改</a> &nbsp;&nbsp;&nbsp;&nbsp;<a onclick="del(<%=id%>)" href="javascript:void(0)">删除</a></td>
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
		window.location.href = "mobile_version_list.jsp?pageSize=" + flex.getOptions().rp ;
	}
	
	function changePage(newp) {
		if (newp)
			window.location.href = "mobile_version_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
	}
	
	function rpChange(pageSize) {
		window.location.href = "mobile_version_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
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
			{name: '条件', bclass: 'fbutton', type: 'include', id: 'searchTable'}
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
			jAlert("请选择记录！","提示");
			return;
		}
		jConfirm("您确定要删除吗？","提示",function(r){
			if(!r){return;}
			else{
				window.location.href ="mobile_version_list.jsp?op=delBatch&id="+ids+"&ids=" + ids;
			}
		})
	}
	function doAdd() {
	  window.location.href = "mobile_version_add.jsp";
	}
	function del(id) {
	    var pageSize = <%=pagesize%>;
		var CPages = <%=curpage%>;
	
		jConfirm("您确定要删除吗？","提示",function(r){
			if(!r){return;}
			else{
				window.location.href = "mobile_version_list.jsp?op=del&id="+id+"&pageSize="+pageSize+"&CPages="+CPages;
			}
		})
	}

</script>
</html>