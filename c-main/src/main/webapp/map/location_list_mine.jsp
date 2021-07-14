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
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "read";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String action = ParamUtil.get(request, "action");
String kind = ParamUtil.get(request, "kind");
String what = ParamUtil.get(request, "what");
String beginDate = ParamUtil.get(request, "beginDate");
String endDate = ParamUtil.get(request, "endDate");

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "create_date";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String op = ParamUtil.get(request, "op");
String isForm = ParamUtil.get(request, "isForm");

String querystr = "op=" + op + "&isForm=" + isForm + "&action=" + action + "&what=" + StrUtil.UrlEncode(what) + "&kind=" + kind + "&beginDate=" + beginDate + "&endDate=" + endDate;
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
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
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
</head>
<body>
		<table id="searchTable" width="100%" border="0" cellpadding="0" cellspacing="0"><tr><td align="center">
          <form name="formSearch" class="search-form" action="location_list_mine.jsp" method="get">
			<input name="action" value="search" type="hidden" />
			<input name="op" value="<%=op%>" type="hidden" />
			<input name="isForm" value="<%=isForm%>" type="hidden" />
       	    	&nbsp;&nbsp;从
                <input id="beginDate" name="beginDate" value="<%=beginDate%>" size=10 />
             	至 
             	<input id="endDate" name="endDate" value="<%=endDate%>" size=10 />
             	<select name="kind">
                  <option value="remark" <%=kind.equals("user")?"selected":""%>>信息</option>
                  <option value="address" <%=kind.equals("address")?"selected":""%>>地点</option>
                </select>
              <input name=what size=10 value="<%=what%>" />
              &nbsp;
              <input class="tSearch" name="submit" type=submit value="搜索" />
          </form>
        </td></tr></table>
<%

		String myname = privilege.getUser(request);

		String sql = "select id from oa_location where user_name=" + StrUtil.sqlstr(myname);

		if (action.equals("search")) {
			if (kind.equals("remark"))
				sql += " and remark like " + StrUtil.sqlstr("%" + what + "%");
			else if (kind.equals("address"))
				sql += " and address like " + StrUtil.sqlstr("%" + what + "%");

			if (!beginDate.equals("")) {
				if (DateUtil.parse(beginDate, "yyyy-MM-dd")!=null)
					sql += " and create_date>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
			}
			if (!endDate.equals("")) {
				java.util.Date d = DateUtil.parse(endDate, "yyyy-MM-dd");
				if (d!=null) {
					d = DateUtil.addDate(d, 1);
					sql += " and create_date<=" + SQLFilter.getDateStr(DateUtil.format(d, "yyyy-MM-dd"), "yyyy-MM-dd");
				}
			}
		}
		sql += " order by " + orderBy + " " + sort;
		
		// out.print(sql);
		
		String sortquerystr = querystr;
		querystr += "&orderBy=" + orderBy + "&sort=" + sort;
		
		int pagesize = 60;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
			
		LocationDb ld = new LocationDb();
		
		ListResult lr = ld.listResult(sql, curpage, pagesize);
		long total = lr.getTotal();
		Vector v = lr.getResult();

		paginator.init(total, pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0) {
			curpage = 1;
			totalpages = 1;
		}
%>
    <table width="811" id="grid">    
		<thead>
        <tr>
          <th width="42" style="cursor:pointer" abbr="id">ID</th>                  
          <th width="200" style="cursor:pointer" abbr="address">信息</th>
          <th width="400" style="cursor:pointer" abbr="address">附近位置</th>
          <th width="150" style="cursor:pointer" abbr="create_date" align="center">日期</th>
          <th width="100" style="cursor:pointer" align="center">操作</th>
          </tr>
   </thead>
   <tbody>
      <%	
	  	int i = 0;
		UserMgr um = new UserMgr();
		Iterator ir = lr.getResult().iterator(); 
		while (ir!=null && ir.hasNext()) {
			ld = (LocationDb)ir.next();
			i++;
			long id = ld.getLong("id");
		%>
        <tr id="<%=id%>">
          <td><%=id%></td>
          <td align="center"><%=StrUtil.getNullStr(ld.getString("remark"))%></td>
          <td align="center"><%=StrUtil.getNullStr(ld.getString("address"))%></td>
          <td align="center"><%=DateUtil.format(ld.getDate("create_date"), "yyyy-MM-dd HH:mm:ss")%></td>
          <td align="center">
          <a href="javascript:;" onclick="addTab('地理位置', '<%=request.getContextPath()%>/map/location_map_bd.jsp?id=<%=ld.getLong("id")%>')">查看</a>
          <%if (op.equals("sel")) {%>
          &nbsp;&nbsp;
          <a href="javascript:;" onclick="selLocation('<%=id%>')">选择</a>
          <%}%>
          </td>
        </tr>
      <%
		}
%>   
	</tbody>
</table>
</body>
<script type="text/javascript">
function initCalendar() {
	$('#beginDate').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d',
    	formatDate:'Y/m/d'
    });
 	$('#endDate').datetimepicker({
 		lang:'ch',
 		timepicker:false,
 		format:'Y-m-d',
 		formatDate:'Y/m/d'
 	});
}

function doOnToolbarInited() {
	initCalendar();
}

var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "location_list_mine.jsp?<%=sortquerystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "location_list_mine.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "location_list_mine.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

flex = jQuery("#grid").flexigrid
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
}

function selLocation(id) {
  	var dlg = window.opener ? window.opener : dialogArguments;

	<%
	// 不能直接调用dlg.setPerson，因为有可能是从表单中UserSelectWinCtl调用
	if (isForm.equals("true")) {
		%>
		dlg.setIntpuObjValue(id, id);
		<%
	}
	else {
	%>
		dlg.setLocation(id);
	<%}%>
	
	window.close();
}

</script>
</html>
