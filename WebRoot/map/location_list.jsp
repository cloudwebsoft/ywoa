<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.map.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "admin.location";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String action = ParamUtil.get(request, "action");
String kind = ParamUtil.get(request, "kind");
String what = ParamUtil.get(request, "what");
String beginDate = ParamUtil.get(request, "beginDate");
String endDate = ParamUtil.get(request, "endDate");
String dept = ParamUtil.get(request, "dept");

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "l.create_date";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String querystr = "action=" + action + "&what=" + StrUtil.UrlEncode(what) + "&kind=" + kind + "&beginDate=" + beginDate + "&endDate=" + endDate + "&dept=" + StrUtil.UrlEncode(dept);

String op = ParamUtil.get(request, "op");

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>地理位置</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
	<style>
		.span-cond input {
			width: 80px;
		}
		.search-form input, select {
			vertical-align: middle;
		}
	</style>
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<style>
	.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
	-moz-opacity: 0.20;
	opacity: 0.20;
	z-index: 1500;
	}
	.treeBackground {
	display: none;
	position: absolute;
	top: -2%;
	left: 0%;
	width: 100%;
	margin: auto;
	height: 200%;
	background-color: #EEEEEE;
	z-index: 1800;
	-moz-opacity: 0.8;
	opacity: .80;
	filter: alpha(opacity = 80);
	}
</style>
</head>
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
if (op.equals("del")) {
	LocationDb ld = new LocationDb();
	String ids = ParamUtil.get(request, "ids");
	String[] ary = StrUtil.split(ids, ",");
	if (ary==null) {
		out.print(StrUtil.jAlert_Back("请选择记录！","提示"));
		return;
	}
	%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	for (int i=0; i<ary.length; i++) {
		ld = (LocationDb)ld.getQObjectDb(new Long(StrUtil.toLong(ary[i])));
		ld.del();
	}
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "location_list.jsp?" + querystr));
	return;
}
else if (op.equals("delBatch")) {
	String sql = "delete from oa_location where 1=1";
	boolean isBlank = true;
	if (kind.equals("user")) {
		sql += " and user_name like " + StrUtil.sqlstr("%" + what + "%");
		isBlank = false;
	}
	else if (kind.equals("address")) {
		sql += " and address like " + StrUtil.sqlstr("%" + what + "%");
		isBlank = false;
	}

	if (!beginDate.equals("")) {
		if (DateUtil.parse(beginDate, "yyyy-MM-dd")!=null) {
			sql += " and create_date>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
			isBlank = false;
		}
	}
	if (!endDate.equals("")) {
		java.util.Date d = DateUtil.parse(endDate, "yyyy-MM-dd");
		if (d!=null) {
			d = DateUtil.addDate(d, 1);
			sql += " and create_date<=" + SQLFilter.getDateStr(DateUtil.format(d, "yyyy-MM-dd"), "yyyy-MM-dd");
			isBlank = false;
		}
	}
	
	if (isBlank) {
		out.print(StrUtil.jAlert_Back("请设定批量删除的条件！","提示"));
		return;
	}
	
	JdbcTemplate jt = new JdbcTemplate();
	int row = jt.executeUpdate(sql);
	out.print(StrUtil.jAlert_Redirect("操作成功，共删除" + row + "条！","提示", "location_list.jsp"));
	return;	
}
 %>
        <table id="searchTable" width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr><td>
        <form name="formSearch" class="search-form" action="location_list.jsp" method="get">
			<input name="action" value="search" type="hidden" />&nbsp;&nbsp;
            部门&nbsp;<select id="dept" name="dept">
              <option value="">请选择</option>
              <%
                DeptMgr dm = new DeptMgr();
                DeptDb lf = dm.getDeptDb(DeptDb.ROOTCODE);
                DeptView dv = new DeptView(lf);
                dv.ShowDeptAsOptions(out, lf, lf.getLayer()); 
               %>
                </select>
       	    	从
                <input id="beginDate" name="beginDate" value="<%=beginDate%>" size=15 >
             	<select name="kind">
                  <option value="user" <%=kind.equals("user")?"selected":""%>>用户</option>
                  <option value="address" <%=kind.equals("address")?"selected":""%>>地点</option>
                </select>
              <input name=what size=10 value="<%=what%>" />
              &nbsp;
              <input class="tSearch" name="submit" type=submit value="搜索" />
          </form>
        </td></tr>
        </table>
<%

		String myname = privilege.getUser(request);

		String sql = "select l.id from oa_location l, dept_user du where l.user_name=du.user_name";

		if (action.equals("search")) {
			if (kind.equals("user")) {
				if (!"".equals(what))
					sql += " and l.user_name like " + StrUtil.sqlstr("%" + what + "%");
			}
			else if (kind.equals("address")) {
				if (!"".equals(what))
					sql += " and l.address like " + StrUtil.sqlstr("%" + what + "%");
			}

			if (!beginDate.equals("")) {
				if (DateUtil.parse(beginDate, "yyyy-MM-dd")!=null)
					sql += " and l.create_date>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
			}
			if (!endDate.equals("")) {
				java.util.Date d = DateUtil.parse(endDate, "yyyy-MM-dd");
				if (d!=null) {
					d = DateUtil.addDate(d, 1);
					sql += " and l.create_date<=" + SQLFilter.getDateStr(DateUtil.format(d, "yyyy-MM-dd"), "yyyy-MM-dd");
				}
			}
			if (!dept.equals("")) {
				sql += " and du.dept_code=" + SQLFilter.sqlstr(dept);
			}		
		}
		sql += " order by " + orderBy + " " + sort;
		
		// out.print(sql);
		
		String sortquerystr = querystr;
		querystr += "&orderBy=" + orderBy + "&sort=" + sort;
		
		int pagesize = ParamUtil.getInt(request, "pageSize", 30);
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
			
		LocationDb ld = new LocationDb();
		
		ListResult lr = ld.listResult(sql, curpage, pagesize);
		int total = lr.getTotal();
		Vector v = lr.getResult();

		paginator.init(total, pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0) {
			curpage = 1;
			totalpages = 1;
		}
%>
<table width="900" id="grid">    
		<thead>
        <tr>
        	<th width="30"><input type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else clearAllCheckBox('ids')" /></th>
          <th width="37" style="cursor:pointer" abbr="id">ID</th>                  
          <th width="61" style="cursor:pointer" abbr="l.user_name">用户</th>
          <th width="97" style="cursor:pointer" abbr="du.dept_code">部门</th>		  
          <th width="143" style="cursor:pointer" abbr="remark">信息</th>
          <th width="306" style="cursor:pointer" abbr="address">附近位置</th>
          <th width="148" style="cursor:pointer" abbr="l.create_date">日期</th>
          <th width="78" style="cursor:pointer" align="center">操作</th>
          </tr>
   </thead>
   <tbody>
      <%	
	  	int i = 0;
		UserMgr um = new UserMgr();
		DeptUserDb dud = new DeptUserDb();
		Iterator ir = lr.getResult().iterator();
		while (ir!=null && ir.hasNext()) {
			ld = (LocationDb)ir.next();
			i++;
			long id = ld.getLong("id");
		%>
        <tr id="<%=id%>">
        	<td><input type="checkbox" name="ids" value="<%=id%>" /></td>
          <td><%=id%></td>
          <td><%=um.getUserDb(ld.getString("user_name")).getRealName()%></td>
          <td><%
          		Iterator ir2 = dud.getDeptsOfUser(ld.getString("user_name")).iterator();
          		int k = 0;
          		while (ir2.hasNext()) {
          			DeptDb dd = (DeptDb) ir2.next();
          			String deptName = "";
          			if (!dd.getParentCode().equals(DeptDb.ROOTCODE)
          					&& !dd.getCode().equals(DeptDb.ROOTCODE)) {
          				deptName = dm.getDeptDb(dd.getParentCode()).getName()
          						+ "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>"
          						+ dd.getName();
          			} else
          				deptName = dd.getName();
          			if (k == 0) {
          				out.print(deptName);
          			} else {
          				out.print("，&nbsp;" + deptName);
          			}
          			k++;
          		}
          %></td>
          <td align="center"><%=StrUtil.getNullStr(ld.getString("remark"))%></td>
          <td align="center"><%=StrUtil.getNullStr(ld.getString("address"))%></td>
          <td align="center"><%=DateUtil.format(ld.getDate("create_date"), "yyyy-MM-dd HH:mm:ss")%></td>
          <td align="center"><a href="javascript:;" onclick="addTab('地理位置', '<%=request.getContextPath()%>/map/location_map_bd.jsp?id=<%=ld.getLong("id")%>')">查看</a></td>
        </tr>
      <%
		}
%>   
	</tbody>
</table>
</body>
<script type="text/javascript"><!--
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

var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "location_list.jsp?<%=sortquerystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "location_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "location_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

flex = jQuery("#grid").flexigrid
(
	{
	buttons : [
		{name: '删除', bclass: 'delete', onpress : action},
		// {name: '批量删除', bclass: 'delete', onpress : action},
		{name: '导出', bclass: 'export', onpress : action},
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

$(document).ready(function () {
	o("dept").value = "<%=dept%>";
});

function action(com, grid) {
	if (com=="删除") {
		var ids = getCheckboxValue("ids");
		if (ids=="") {
			jAlert('请选择记录!','提示');
			return;
		}
		
		jConfirm("您确定要删除么？","提示",function(r){
			if(!r){return;}
			else{	
				window.location.href = "location_list.jsp?<%=querystr%>&op=del&ids=" + ids;	
			}
		})	
	}
	/*
	else if (com=="批量删除") {
		jConfirm("您确定要批量删除全部查询结果么？","提示",function(r){
			if(!r){return;}
			else{
				window.location.href = "location_list.jsp?<%=querystr%>&op=delBatch&<%=querystr%>";	
			}
		})	
	}
	*/
	else if(com=="下载"){
		window.location.href = "location_excel.jsp?<%=querystr%>";
	}
}
function selAllCheckBox(checkboxname) {
	var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = true;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = true;
		}
	}
}

function clearAllCheckBox(checkboxname) {
	var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = false;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = false;
		}
	}
}
--></script>
</html>
