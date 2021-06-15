<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String userName = ParamUtil.get(request, "userName");
if (userName.equals("")) {
	userName = privilege.getUser(request);
}

if (!userName.equals(privilege.getUser(request))) {
	if (!(privilege.canAdminUser(request, userName))) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}
String mode = ParamUtil.get(request, "mode");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>日程列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />

<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
</head>
<body>
<%
if (!privilege.isUserLogin(request)) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int pagesize = ParamUtil.getInt(request, "pageSize", 20);
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();		
String action = ParamUtil.get(request, "action");
String what = ParamUtil.get(request, "what");

String op = ParamUtil.get(request, "op");
String preDate = ParamUtil.get(request, "preDate");
String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");
int actionType = ParamUtil.getInt(request, "actionType", -1);

java.util.Date beginDate = null;
java.util.Date endDate = null;

if (!preDate.equals("") && !preDate.equals("*")) {
	String[] ary = StrUtil.split(preDate, "\\|");
	strBeginDate = ary[0];
	strEndDate = ary[1];
	beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
	endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
}
else {
	if (preDate.equals("*")) {
		beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
		endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
		if (endDate!=null)
			endDate = DateUtil.addDate(endDate, 1);
	}
	else {
		strBeginDate = "";
		strEndDate = "";
	}
}

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "myDate";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String sql;
String myname = privilege.getUser(request);

sql = "select id from user_plan where username=" + fchar.sqlstr(userName) + " and is_closed=0";
if (mode.equals("iMake"))
	sql = "select id from user_plan where maker=" + fchar.sqlstr(userName) + " and is_closed=0";

String y = ParamUtil.get(request, "year");
String m = ParamUtil.get(request, "month");
String d = ParamUtil.get(request, "day");
if (!y.equals("")) {
	sql += " and " + SQLFilter.year("myDate") + "=" + y + " and " + SQLFilter.month("myDate") + "=" + m + " and " + SQLFilter.day("myDate") + "=" + d;
}

if (action.equals("search")) {
	if (!what.equals("")) {
		sql += " and title like " + StrUtil.sqlstr("%" + what + "%");
	}
	if (beginDate!=null) {
		sql += " and myDate>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
	}
	if (endDate!=null) {
		sql += " and myDate<" + SQLFilter.getDateStr(DateUtil.format(endDate, "yyyy-MM-dd"), "yyyy-MM-dd");
	}
	if (actionType!=-1) {
		if (actionType==0)
			sql += " and action_type=0";
		else
			sql += " and action_type<>0";
	}
}

sql += " order by " + orderBy + " " + sort;

// out.print(sql);
	
PlanDb pd = new PlanDb();

ListResult lr = pd.listResult(sql, curpage, pagesize);
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
  <table id="searchTable" width="98%" border="0" cellpadding="0" cellspacing="0">
  <tr>
	<td align="center">
        <form action="plan_todo_list.jsp" method="get" name="formSearch" id="formSearch">
        <select id="preDate" name="preDate" onchange="if (this.value=='*') o('dateSection').style.display=''; else o('dateSection').style.display='none'">
        <option selected="selected" value="">不限</option>
        <%
        java.util.Date[] ary = DateUtil.getDateSectOfToday();
        %>
        <option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">今天</option>
        <%
        ary = DateUtil.getDateSectOfYestoday();
        %>
        <option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">昨天</option>
        <%
        ary = DateUtil.getDateSectOfCurWeek();
        %>
        <option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">本周</option>
        <%
        ary = DateUtil.getDateSectOfLastWeek();
        %>
        <option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">上周</option>
        <%
        ary = DateUtil.getDateSectOfCurMonth();
        %>
        <option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">本月</option>
        <%
        ary = DateUtil.getDateSectOfLastMonth();
        %>
        <option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">上月</option>
        <%
        ary = DateUtil.getDateSectOfQuarter();
        %>
        <option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">本季度</option>
        <%
        ary = DateUtil.getDateSectOfCurYear();
        %>
        <option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">今年</option>
        <%
        ary = DateUtil.getDateSectOfLastYear();
        %>
        <option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">去年</option>
        <%
        ary = DateUtil.getDateSectOfLastLastYear();
        %>
        <option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">前年</option>
        <option value="*">自定义</option>
        </select>
		<script>
        o("preDate").value = "<%=preDate%>";
        </script>        
        <span id="dateSection" style="display:<%=preDate.equals("*")?"":"none"%>">
        从
        <input id="beginDate" name="beginDate" size="10" value="<%=strBeginDate%>" />
        <script type="text/javascript">
            Calendar.setup({
                inputField     :    "beginDate",      // id of the input field
                ifFormat       :    "%Y-%m-%d",       // format of the input field
                showsTime      :    false,            // will display a time selector
                singleClick    :    false,           // double-click mode
                align          :    "Tl",           // alignment (defaults to "Bl")		
                step           :    1                // show all years in drop-down boxes (instead of every other year as default)
            });
        </script>		
        至
        <input id="endDate" name="endDate" size="10" value="<%=strEndDate%>" />
        <script type="text/javascript">
            Calendar.setup({
                inputField     :    "endDate",      // id of the input field
                ifFormat       :    "%Y-%m-%d",       // format of the input field
                showsTime      :    false,            // will display a time selector
                singleClick    :    false,           // double-click mode
                align          :    "Tl",           // alignment (defaults to "Bl")		
                step           :    1                // show all years in drop-down boxes (instead of every other year as default)
            });
        </script>
        </span>
        <span style="display:none">
        类型&nbsp;&nbsp;
        <select id="actionType" name="actionType">
        <option value="-1">不限</option>
        <option value="0">非系统生成</option>
        <option value="1">系统生成</option>
        </select>        
        <script>
		o("actionType").value = "<%=actionType%>";
		</script>
        </span>
		&nbsp;&nbsp;标题&nbsp;&nbsp;
	  	<input name="what" value="<%=what%>" />           
		<input name="action" value="search" type="hidden" />
		<input class="btn" name="submit" type="submit" value="搜索" />
		<input name="userName" value="<%=userName%>" type="hidden" />	
    </form>
	</td>
</tr>
</table>
<table id="grid">
  <thead>
  <tr>
    <th width="479">标题</th>
    <th width="115">日期</th>
    <th width="79">操作</th>
  </tr>
  </thead>
  <tbody>
<%	
  UserDb user = new UserDb();
  int id;
  String title, mydate, sEndDate;
  while (ir!=null && ir.hasNext()) {
	  pd = (PlanDb)ir.next();
	  id = pd.getId();
	  title = pd.getTitle();
	  mydate = DateUtil.format(pd.getMyDate(), "yy-MM-dd HH:mm");
	  sEndDate = DateUtil.format(pd.getEndDate(),"yy-MM-dd HH:mm");		
%>
  <tr>
    <td><%=title%></td>
    <td align="center"><%=mydate%></td>
    <td align="center">
    <%
	String tabTitle = "";
	String tabUrl = "";
	if (PlanDb.ACTION_TYPE_SALES_VISIT == pd.getActionType()) {
		com.redmoon.oa.flow.FormDb fd = new com.redmoon.oa.flow.FormDb();
		String formCode = "day_lxr";
		fd = fd.getFormDb(formCode);
		com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
		long visitId = StrUtil.toLong(pd.getActionData(), -1);
		if (visitId!=-1) {
			fdao = fdao.getFormDAO(visitId, fd);
			String lxrId = fdao.getFieldValue("lxr");
			fd = fd.getFormDb("sales_linkman");
			fdao = fdao.getFormDAO(StrUtil.toLong(lxrId), fd);
			tabTitle = "行动";
			tabUrl = request.getContextPath() + "/sales/customer_visit_list.jsp?customerId=" + fdao.getFieldValue("customer");
		}
	}
	else if (PlanDb.ACTION_TYPE_FLOW == pd.getActionType()) {
		MyActionDb mad = new MyActionDb();
		mad = mad.getMyActionDb(StrUtil.toLong(pd.getActionData()));
		WorkflowMgr wfm = new WorkflowMgr();
		WorkflowDb wf = wfm.getWorkflowDb((int)mad.getFlowId());
		com.redmoon.oa.flow.Leaf lf = new com.redmoon.oa.flow.Leaf();
		lf = lf.getLeaf(wf.getTypeCode());

		tabTitle = wf.getTitle();
		if (lf!=null && lf.getType()==com.redmoon.oa.flow.Leaf.TYPE_LIST) {
			tabUrl = request.getContextPath() + "/flow_dispose.jsp?myActionId=" + pd.getActionData();
		}
		else {
			tabUrl = request.getContextPath() + "/flow_dispose_free.jsp?myActionId=" + pd.getActionData();
		}
	}	
	else if (PlanDb.ACTION_TYPE_PAPER_DISTRIBUTE == pd.getActionType()) {
		tabTitle = "收文";
		tabUrl = request.getContextPath() + "/paper/paper_show.jsp?paperId=" + pd.getActionData();
	}                
	else {
		tabTitle = "日程安排";
		tabUrl = request.getContextPath() + "/user/plan_show.jsp?id=" + pd.getId();
	}
	%>
    <a href="javascript:;" onclick="addTab('<%=tabTitle%>', '<%=tabUrl%>')">查看</a>
    </td>
  </tr>
<%}%>
  </tbody>
</table>
<%
String querystr = "mode=" + mode + "&userName=" + StrUtil.UrlEncode(userName) + "&action=" + action + "&what=" + StrUtil.UrlEncode(what) + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate + "&actionType=" + actionType;
%>
</body>
<script>
var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "plan_todo_list.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "plan_todo_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "plan_todo_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}
$(document).ready(function() {
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
		sortname: "<%=orderBy%>",
		sortorder: "<%=sort%>",
		url: false,
		usepager: true,
		page: <%=curpage%>,
		total: <%=total%>,
		useRp: true,
		rp: <%=pagesize%>,
		
		// title: "通知",
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
		autoHeight: true,
		width: document.documentElement.clientWidth,
		height: document.documentElement.clientHeight - 84
		}
	);
});
</script>
</html>