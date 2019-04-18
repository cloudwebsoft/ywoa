<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.lark.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>即时消息</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />

<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../js/flexigrid.js"></script>

<script src="../js/jquery-alerts/jquery.alerts.js"
	type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"
	type="text/css" media="screen" />
</head>
<%
String priv = "admin";
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

String querystr = "action=" + action + "&what=" + StrUtil.UrlEncode(what) + "&kind=" + kind + "&beginDate=" + beginDate + "&endDate=" + endDate;

String op = ParamUtil.get(request, "op");
if (op.equals("del")) {
	LarkMsgDb lmd = new LarkMsgDb();
	String ids = ParamUtil.get(request, "ids");
	String[] ary = StrUtil.split(ids, ",");
	if (ary==null) {
		out.print(StrUtil.jAlert_Back("请选择记录！", "提示"));
		return;
	}
	for (int i=0; i<ary.length; i++) {
		lmd = (LarkMsgDb)lmd.getQObjectDb(new Long(StrUtil.toLong(ary[i])));
		lmd.del();
	}
	out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "lark_msg_list.jsp?" + querystr));
	return;
}
else if (op.equals("delBatch")) {
	String sql = "delete from oa_lark_msg where 1=1";
	boolean isBlank = true;
	if (kind.equals("sender")) {
		sql += " and from_user like " + StrUtil.sqlstr("%" + what + "%");
		isBlank = false;
	}
	else if (kind.equals("receiver")) {
		sql += " and to_user like " + StrUtil.sqlstr("%" + what + "%");
		isBlank = false;
	}
	else {
		sql += " and content like " + StrUtil.sqlstr("%" + what + "%");
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
		out.print(StrUtil.jAlert_Back("请设定批量删除的条件！", "提示"));
		return;
	}
	
	JdbcTemplate jt = new JdbcTemplate();
	int row = jt.executeUpdate(sql);
	out.print(StrUtil.jAlert_Redirect("操作成功，共删除" + row + "条！", "提示", "lark_msg_list.jsp"));
	return;	
}
%>
<body>
		<table id="searchTable"><tr><td>
          <form name="formSearch" action="lark_msg_list.jsp" method="get">
			<input name="action" value="search" type="hidden" />
       	    	从
                <input id="beginDate" name="beginDate" value="<%=beginDate%>" size=15>		 
             	至 
             	<input id="endDate" name="endDate" value="<%=endDate%>" size=15>
             	<select name="kind">
                  <option value="sender" <%=kind.equals("title")?"selected":""%>>发送者</option>
                  <option value="receiver" <%=kind.equals("receiver")?"selected":""%>>接收者</option>
                  <option value="content" <%=kind.equals("content")?"selected":""%>>内容</option>
                </select>
              <input name=what size=20 value="<%=what%>">
              &nbsp;
              <input class="tSearch" name="submit" type=submit value="搜索">
          </form>
        </td></tr></table>
<%

		String myname = privilege.getUser(request);

		String sql = "select id from oa_lark_msg where 1=1";

		if (action.equals("search")) {
			if (kind.equals("sender"))
				sql += " and from_user like " + StrUtil.sqlstr("%" + what + "%");
			else if (kind.equals("receiver"))
				sql += " and to_user like " + StrUtil.sqlstr("%" + what + "%");
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
          <th width="20"><input type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else clearAllCheckBox('ids')" /></th>
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
        <tr id="<%=id%>">
          <td><input type="checkbox" name="ids" value="<%=id%>" /></td>
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
	window.location.href = "lark_msg_list.jsp?<%=sortquerystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "lark_msg_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "lark_msg_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

flex = jQuery("#grid").flexigrid
(
	{
	buttons : [
		{name: '删除', bclass: 'delete', onpress : action},
		{name: '批量删除', bclass: 'delete', onpress : action},
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

function action(com, grid) {
	if (com=="删除") {
		var ids = getCheckboxValue("ids");
		if (ids=="") {
			jAlert("请选择一条记录！","提示");
			return;
		}
		
		jConfirm("您确定要删除么？","提示",function(r){
			if(!r){return}
			else{
				window.location.href = "lark_msg_list.jsp?<%=querystr%>&op=del&ids=" + ids;
			}
		});	
	}
	else if (com=="批量删除") {
		jConfirm("您确定要删除么？","提示",function(r){
			if(!r){return}
			else{
				window.location.href = "lark_msg_list.jsp?<%=querystr%>&op=delBatch&<%=querystr%>";	
			}
		});	
	}
}
</script>
</html>
