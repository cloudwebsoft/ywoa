<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<% 
	if (!privilege.isUserPrivValid(request, "admin")) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	String parentId = ParamUtil.get(request, "parentId");

	String op = ParamUtil.get(request, "op");
	String cond = ParamUtil.get(request,"cond");
	if (op.equals("delBatch")) {
		try {
			RegionMgr rm = new RegionMgr();
			rm.delBatch(request);
			out.print(StrUtil.Alert_Redirect("操作成功！", "region_list.jsp?parentId=" + parentId));
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
			return;
		}
   	} else if(op.equals("del")){
		String id  = ParamUtil.get(request, "id");
		try {
			RegionMgr dm = new RegionMgr();
			dm.del(id);
			out.print(StrUtil.Alert_Redirect("操作成功！", "region_list.jsp?parentId=" + parentId));
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
			return;
		}
	}
		
	String orderBy = ParamUtil.get(request, "orderBy");
	if (orderBy.equals(""))
		orderBy = "region_id";
	String sort = ParamUtil.get(request, "sort");
	if (sort.equals(""))
		sort = "asc";	
	String what = ParamUtil.get(request, "what");
	String querystr = "op=" + op + "&what=" + StrUtil.UrlEncode(what);
	int pagesize = ParamUtil.getInt(request, "pageSize", 25);
	Paginator paginator = new Paginator(request);
	int curpage = paginator.getCurPage();
		
	String sql = "select region_id from oa_china_region where 1=1 and region_type = '1'";		
	if(op.equals("search") || !what.equals("")) {
	    sql = "select region_id from oa_china_region where 1=1";
		if (!what.equals("")){
			sql += " and region_name like " + StrUtil.sqlstr("%" + what + "%");
		}
		if (!cond.equals("")){
			sql += " and region_type=" + cond;
		}
	}
	sql += " order by " + orderBy + " " + sort;	
	if(!parentId.equals("")){  
	  sql = "select region_id from oa_china_region where parent_id = "+StrUtil.toInt(parentId);
	}
	
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>地域信息列表</title>
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
<table id="searchTable" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
	<tr>
		<td width="48%" height="30" align="left">
		<form action="region_list.jsp" method="get">
		<input name="op" value="search" type="hidden" />
		<select id="cond" name="cond">
		<option value="">全部</option>
        <option value="1" selected="selected">省</option>
        </select>
        <%if (op.equals("search")) {%>
		<script>
		o("cond").value = "<%=cond%>";
		</script>
        <%}%>
        名称
        <input name="what" size="15" value="<%=what%>" />
		<input class="tSearch" value="搜索" type="submit" />
		</form>
	  </td>
	</tr>
</table>
<%
RegionDb rd = new RegionDb();           		
ListResult lr = rd.listResult(sql, curpage, pagesize);
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
		
//ModuleSetupDb msd = new ModuleSetupDb();
//MacroCtlMgr mm = new MacroCtlMgr();
%>
<table id="grid" border="0">
	<thead>
	<tr>
		<th width="30"><input name="idsa" type="checkbox" onClick="selAllCheckBox('ids')"></th>
		<th width="107" >上级</th>
		<th width="140" abbr="region_name">名称</th>
		<th width="136" abbr="region_type">类型</th>
        <%if (privilege.isUserPrivValid(request, "admin")) {%>        
		<th width="280">操作</th>
        <%}%>
	</tr>
	</thead>
   <tbody>	
<%
	 int region_id=0;
	 while(ir.hasNext()){
		rd = (RegionDb) ir.next();
		region_id = rd.getInt("region_id");	
		int parent_id = rd.getInt("parent_id");
		int region_type = rd.getInt("region_type");
		String type ="";
		if(region_type==1){
		  type ="省";
		}else if( region_type==2){
		  type = "市";
		}else if(region_type==3) {
		  type = "区/县";
		}
		String parent_name = rd.getRegionName(parent_id);
%>
	<tr align="center">
		<td><input type="checkbox" name="ids" value="<%=region_id%>" /></td>
		<td><%=parent_name%></td>
		<td>
		<%
		    boolean re = rd.isRegionType(region_id);
			if(re){
		%>
		   <a href="region_list.jsp?parentId=<%=region_id%>"><%=rd.get("region_name")%></a>
		<%}else{%>
		   <%=rd.get("region_name")%>
		<%}%>
		</td>
		<td><%=type%></td>
        <%if (privilege.isUserPrivValid(request, "admin")) {%>
		<td>
         <a href="region_edit.jsp?id=<%=region_id%>">修改</a>
         &nbsp;&nbsp;&nbsp;&nbsp;<a onclick="doDelbyOne(<%=region_id%>)" href="javascript:void(0)">删除</a></td>
        <%}%>
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
	window.location.href = "region_list.jsp?pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "region_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "region_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
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
        {separator: true},
		{name: '条件', bclass: '', type: 'include', id: 'searchTable'}
		],
	/*
	searchitems : [
		{display: 'ISO', name : 'iso'},
		{display: 'Name', name : 'name', isdefault: true}
		],
	*/
	sortname: "<%=orderBy%>",
	sortorder: "<%=sort%>",
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
	}
	else if(com=="添加")
	{
	  doAdd();
	}
}
function doDel() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请选择删除的内容！");
		return;
	}
	if(confirm("您确定要删除吗？"))
	{
	   window.location.href = "?op=delBatch&parentId=<%=parentId%>&ids=" + ids;
	}	
}
function doDelbyOne(id) {
	if(confirm("您确定要删除吗？"))
	{
	  window.location.href = "?op=del&parentId=<%=parentId%>&id="+id;
	}
}

function doAdd() {
<%
if(!parentId.equals("")){
%>
window.location.href="region_add.jsp?parentId=<%=parentId%>";
<%}else{%>
window.location.href="region_add.jsp";
<%}%>
}
</script>
</html>
