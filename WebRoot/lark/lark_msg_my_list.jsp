<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.lark.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "read";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = ParamUtil.get(request, "userName");
if (userName.equals("")) {
	userName = privilege.getUser(request);
}
else {
	if (!privilege.canAdminUser(request, userName)) {
		// 检查用户能否管理该单位
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

String op = ParamUtil.get(request, "op");
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
try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "what", what, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "orderBy", orderBy, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "what", what, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>即时消息</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />

<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

<!-- 
<style type="text/css"> 
@import url("<%=request.getContextPath()%>/util/jscalendar/calendar-win2k-2.css"); 
</style>

<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar-setup.js"></script>
 -->

<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../js/flexigrid.js"></script>
</head>
<body>
		<table id="searchTable"><tr><td>
          <form name="formSearch" action="lark_msg_my_list.jsp" method="get">
			<input name="action" value="search" type="hidden" />
       	    	从
                <input id="beginDate" name="beginDate" value="<%=beginDate%>" size=15>		 
             	至 
             	<input id="endDate" name="endDate" value="<%=endDate%>" size=15>
             	<select name="kind">
                  <option value="sender" <%=kind.equals("sender")?"selected":""%>>姓名</option>
                  <option value="content" <%=kind.equals("content")?"selected":""%>>内容</option>
                </select>
              <input name=what size=20 value="<%=what%>">
              &nbsp;
              <input class="tSearch" name="submit" type=submit value="搜索">
              <input name="op" type="hidden" value="<%=op%>">
              <input name="userName" type="hidden" value="<%=userName%>">
          </form>
        </td></tr></table>
<%
		String myname = privilege.getUser(request);
		String querystr = "";

		String sql = "select id from oa_lark_msg where (to_user=" + StrUtil.sqlstr(userName) + " or from_user=" + StrUtil.sqlstr(userName) + ")";

		if (action.equals("search")) {
			if (kind.equals("sender"))
				sql += " and from_user like " + StrUtil.sqlstr("%" + what + "%");
			else
				sql += " and content like " + StrUtil.sqlstr("%" + what + "%");
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
		
		querystr += "op=" + op + "&userName=" + StrUtil.UrlEncode(userName) + "&action=search&what=" + StrUtil.UrlEncode(what) + "&kind=" + kind + "&beginDate=" + beginDate + "&endDate=" + endDate;
		
		String sortquerystr = querystr;
		querystr += "&orderBy=" + orderBy + "&sort=" + sort;
		
		int pagesize = 60;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
			
		LarkMsgDb lmd = new LarkMsgDb();
		
		ListResult lr = lmd.listResult(sql, curpage, pagesize);
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
<table width="944" id="grid">    
		<thead>
        <tr>
          <th width="49" style="cursor:pointer" abbr="id">ID</th>                  
          <th width="105" style="cursor:pointer" abbr="from_user">发送者</th>		  
          <th width="109" style="cursor:pointer" abbr="to_user">接收者</th>
          <th width="512" style="cursor:pointer" abbr="content">内容</th>
          <th width="145" style="cursor:pointer" abbr="create_date">日期</th>
          </tr>
   </thead>
   <tbody>
      <%	
	  	int i = 0;
		UserMgr um = new UserMgr();
		Iterator ir = lr.getResult().iterator(); 
		while (ir!=null && ir.hasNext()) {
			lmd = (LarkMsgDb)ir.next();
			i++;
			long id = lmd.getLong("id");
		%>
        <tr>
          <td><%=id%></td>
          <td><%=um.getUserDb(lmd.getString("from_user")).getRealName()%></td>
          <td align="center"><%=um.getUserDb(lmd.getString("to_user")).getRealName()%></td>
          <td align="center"><%=StrUtil.toHtml(lmd.getString("content"))%></td>
          <td align="center"><%=DateUtil.format(lmd.getDate("create_date"), "yyyy-MM-dd HH:mm:ss")%></td>
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
	window.location.href = "lark_msg_my_list.jsp?<%=sortquerystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}


function changePage(newp) {
	if (newp)
		window.location.href = "lark_msg_my_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "lark_msg_my_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}
$(function(){
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
})

function action(com, grid) {
}
</script>
</html>
