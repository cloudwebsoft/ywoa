<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "read";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = ParamUtil.get(request, "userName");
if(userName.equals("")){
	userName = privilege.getUser(request);
}
if (!userName.equals(privilege.getUser(request))) {
	if (!(privilege.canAdminUser(request, userName))) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

String op = ParamUtil.get(request, "op");

try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "userName", userName, getClass().getName());

	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "userName", userName, getClass().getName());
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
<title>工作计划参与者</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>

<script src="../js/jquery-ui/jquery-ui.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />

<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

<script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>

</head>
<body>
<%@ include file="workplan_show_inc_menu_top.jsp"%>
<script>
o("menu6").className="current";
</script>
<%
String sql;
String myname = privilege.getUser(request);
String querystr = "";
int workplanId = ParamUtil.getInt(request, "id", -1);
if (workplanId==-1) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id") + " workplanId=" + workplanId));
	return;
}

sql = "select u.id from work_plan_task_user u, work_plan_task t where u.task_id=t.id and t.work_plan_id=" + workplanId;
sql += " order by t.orders asc, u.orders asc";

String urlStr = "op=" + op + "&id=" + workplanId;

querystr = urlStr;

int pagesize = ParamUtil.getInt(request, "pageSize", 100);
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
	
WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();

ListResult lr = wptud.listResult(sql, curpage, pagesize);
int total = lr.getTotal();
Vector v = lr.getResult();
Iterator ir = v.iterator();
paginator.init(total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}
%>
<table id="grid">
  <thead>
        <tr>
          <th width="36" style="cursor:pointer">ID</th>
          <th width="351" style="cursor:pointer">任务</th>          
          <th width="155" style="cursor:pointer">参与者</th>
          <th width="129" style="cursor:pointer">使用率</th>
          <th width="141" style="cursor:pointer">工作日</th>
        </tr>
   </thead>
   <tbody>
      <%	
	  	int i = 0;
		WorkPlanDb wpd = new WorkPlanDb();
		wpd = wpd.getWorkPlanDb(workplanId);
		UserMgr um = new UserMgr();
		WorkPlanTaskDb wptd = new WorkPlanTaskDb();
		com.redmoon.oa.workplan.Privilege workplanPvg = new com.redmoon.oa.workplan.Privilege();
		while (ir!=null && ir.hasNext()) {
			wptud = (WorkPlanTaskUserDb)ir.next();
			wptd = (WorkPlanTaskDb)wptd.getQObjectDb(new Long(wptud.getLong("task_id")));
			if (wptd==null) {
				wptd = new WorkPlanTaskDb();
				continue;
			}
			i++;
		%>
        <tr>
          <td align="center"><%=wptud.getLong("id")%></td>
          <td><%=wptd.getString("name")%></td>
          <td>
		  <%
			UserDb user = um.getUserDb(wptud.getString("user_name"));
		  %>
		  <a href="javascript:;" onclick="addTab('消息', 'message_oa/send.jsp?receiver=<%=StrUtil.UrlEncode(user.getName())%>')"><%=user.getRealName()%></a>
          </td>
          <td align="center"><%=wptud.getInt("percent")%>&nbsp;%</td>
          <td align="center"><%=NumberUtil.round(wptud.getDouble("duration"), 1)%></td>
        </tr>
      <%
		}
%>
	</tbody>
</table>
</body>
<script type="text/javascript">
function doOnToolbarInited() {
}

var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "workplan_user.jsp?<%=urlStr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "workplan_user.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "workplan_user.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

flex = $("#grid").flexigrid
(
	{
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
</script>
</html>