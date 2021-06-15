<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import = "com.redmoon.oa.*"%>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.util.*" %>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.kaoqin.*"%>
<%@ page import = "com.redmoon.oa.worklog.*"%>
<%@ page import = "com.redmoon.oa.oacalendar.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style>
.workUser {
/*
width:220px;
float:left;
*/
border:1px solid #cccccc;
padding:3px;
margin-right:5px;
margin-top:10px;
}
.workUserHeader {
margin-bottom:5px;
background-color:#eeeecc;
height:20px;
padding:3px;
text-align:left;
}
.workItem {
padding-top:2px;
padding-left:2px;
border-bottom:1px dashed #cccccc;
margin:5px 0px;
}
.workItemContent div {
text-overflow:ellipsis;
overflow:hidden;
white-space:nowrap;
line-height:1.5;
}
.workUser div p {
padding:0px;
margin:0px;
float:left;
}
.workWrapper div {
line-height:1.5;
}
</style>
<script>
var isLeftMenuShow = true;
function closeLeftMenu() {
	if (isLeftMenuShow) {
		window.parent.setCols("0,*");
		isLeftMenuShow = false;
		btnName.innerHTML = "+&nbsp;打开菜单";
	}
	else {
		window.parent.setCols("200,*");
		isLeftMenuShow = true;
		btnName.innerHTML = "-&nbsp;关闭菜单";		
	}
}
</script>
<title>部门工作</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script>
function preview(userName,deptCode)
{
	window.parent.location.href='archive_user_modify.jsp?userName='+userName+'&deptCode='+deptCode;
}
</script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<% 
    String deptCode = ParamUtil.get(request, "deptCode");

	if (deptCode.equals("")) {
		out.print(SkinUtil.makeInfo(request, "请选择某个部门！"));
		return;
	}
	if (!privilege.canUserAdminDept(request, deptCode)) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	com.redmoon.oa.dept.DeptDb dd = new com.redmoon.oa.dept.DeptDb();
	dd = dd.getDeptDb(deptCode);
	if (dd==null || !dd.isLoaded()) {
		out.print(StrUtil.Alert("部门" + deptCode + "不存在！"));
		return;
	}
	
	String op = ParamUtil.get(request, "op");
%>
<%@ include file="admin_dept_user_inc_menu_top.jsp"%>
<script>
$("menu1").className="current";
</script>
<div class="spacerH"></div>
<div>&nbsp;&nbsp;<a href="admin_dept_user.jsp?deptCode=<%=StrUtil.UrlEncode(deptCode)%>">- 概要显示</a>
&nbsp;&nbsp;<a href="admin_dept_user_detail.jsp?deptCode=<%=StrUtil.UrlEncode(deptCode)%>">- 纵向显示</a>
<script>
if (typeof(window.parent.leftFrame)=="object"){
	if (window.parent.getCols()=="200,*")
		;
		//document.write("&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"javascript:closeLeftMenu()\"><span id=\"btnName\">-&nbsp;关闭菜单</span></a>");
	else {
		isLeftMenuShow = false;
		//document.write("&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"javascript:closeLeftMenu()\"><span id=\"btnName\">+&nbsp;打开菜单</span></a>");
	}
}
</script>
</div>
<div class="workWrapper" style="padding:5px">
<%
	DeptMgr dm = new DeptMgr();
	DeptUserDb du = new DeptUserDb();
	
	DeptDb deptDb = new DeptDb();
	deptDb = deptDb.getDeptDb(deptCode);
	Vector dv = new Vector();
	deptDb.getAllChild(dv, deptDb);
	String depts = StrUtil.sqlstr(deptCode);
	Iterator ird = dv.iterator();
	while (ird.hasNext()) {
		deptDb = (DeptDb)ird.next();
		depts += "," + StrUtil.sqlstr(deptDb.getCode());
	}

	DeptUserDb jd = new DeptUserDb();
	UserDb ud = new UserDb();

	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");
	String orderField = showByDeptSort ? "du.orders" : "u.orders";

   	String sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and du.DEPT_CODE in (" + depts + ") order by du.DEPT_CODE asc, " + orderField + " asc";
	Vector v = jd.listBySQL(sql);
	Iterator irdu = v.iterator();
	while (irdu.hasNext()) {
		DeptUserDb pu = (DeptUserDb)irdu.next();
		if (pu.getUserName().equals("admin"))
			continue;
		if (!pu.getUserName().equals(""))
			ud = ud.getUserDb(pu.getUserName());	
%>
	<div class="workUser">
    <div class="workUserHeader tabStyle_1_title"><strong><%=ud.getRealName()%></strong>：</div>
    <div><a href="../netdisk/netdisk_frame.jsp?op=showDirShare&amp;userName=<%=StrUtil.UrlEncode(ud.getName())%>" title="网络硬盘共享" target="_blank">网盘共享</a>&nbsp;&nbsp;&nbsp; <a title="用户信息" href="../user_info.jsp?userName=<%=StrUtil.UrlEncode(ud.getName())%>" target="_blank">个人信息</a></div>
	<div class="workItem"><strong><a href="../flow/flow_list.jsp?displayMode=1&userName=<%=StrUtil.UrlEncode(ud.getName())%>" title="待办流程" target="_blank">待办流程：</a></strong></div>
	<div class="workItemContent">
<%
MyActionDb mad = new MyActionDb();
sql = "select id from flow_my_action where (user_name=" + StrUtil.sqlstr(pu.getUserName()) + " or proxy=" + StrUtil.sqlstr(pu.getUserName()) + ") and is_checked=0 order by receive_date desc";
int count = 3;
Iterator ir = mad.listResult(sql, 1, count).getResult().iterator();
com.redmoon.oa.person.UserMgr um = new com.redmoon.oa.person.UserMgr();
Directory dir = new Directory();
while (ir.hasNext()) {
	mad = (MyActionDb)ir.next();
	WorkflowDb wfd = new WorkflowDb();
	wfd = wfd.getWorkflowDb((int)mad.getFlowId());
	String userName = wfd.getUserName();
	String userRealName = "";
	if (userName!=null) {
		UserDb user = um.getUserDb(wfd.getUserName());
		userRealName = user.getRealName();
	}
	Leaf ft = dir.getLeaf(wfd.getTypeCode());
%>
  	<div><a href="../flow_modify.jsp?flowId=<%=wfd.getId()%>" title="<%=wfd.getTitle()%> 类型：<%if (ft!=null) {%><%=ft.getName()%><%}%> 开始：<%=DateUtil.format(wfd.getBeginDate(), "yy-MM-dd HH:mm:ss")%> 到期：<%=DateUtil.format(mad.getExpireDate(), "yy-MM-dd HH:mm:ss")%> 发起人：<%=userRealName%>" target="_blank"><%=wfd.getTitle()%></a></div>
<%}%>
	</div>
<div class="workItem"><strong><a title="已办流程" target="_blank" href="../flow_list_done.jsp?userName=<%=StrUtil.UrlEncode(ud.getName())%>">已办流程：</a></strong></div>
	<div class="workItemContent">
<%
sql = "select id from flow_my_action where (user_name=" + StrUtil.sqlstr(pu.getUserName()) + " or proxy=" + StrUtil.sqlstr(pu.getUserName()) + ") and is_checked=1 order by receive_date desc";
ir = mad.listResult(sql, 1, count).getResult().iterator();
WorkflowDb wfd2 = new WorkflowDb();
while (ir.hasNext()) {
 	mad = (MyActionDb)ir.next();
	WorkflowDb wfd = wfd2.getWorkflowDb((int)mad.getFlowId());
	String userName = wfd.getUserName();
	String userRealName = "";
	if (userName!=null) {
		UserDb user = um.getUserDb(wfd.getUserName());
		userRealName = user.getRealName();
	}
	Leaf ft = dir.getLeaf(wfd.getTypeCode());
	%>
            <div>
			  <a href="../flow_modify.jsp?flowId=<%=wfd.getId()%>&amp;actionId=<%=mad.getActionId()%>" title="<%=wfd.getTitle()%> 类型：<%if (ft!=null) {%><%=ft.getName()%><%}%> 到达：<%=DateUtil.format(mad.getReceiveDate(), "yy-MM-dd HH:mm:ss")%> 处理：<%=DateUtil.format(mad.getCheckDate(), "yy-MM-dd HH:mm:ss")%> 到期：<%=DateUtil.format(mad.getExpireDate(), "yy-MM-dd HH:mm:ss")%> 绩效：<%=NumberUtil.round(mad.getPerformance(), 2)%> 发起人：<%=userRealName%>" target="_blank"><%=wfd.getTitle()%></a>
            </div>
     <%}%>
	</div>
	<div class="workItem"><strong><a title="工作报告" target="_blank" href="../mywork/mywork.jsp?userName=<%=StrUtil.UrlEncode(ud.getName())%>">工作报告：</a></strong></div>
	<div class="workItemContentLog">
<%
sql = "select id from work_log where userName=" + StrUtil.sqlstr(pu.getUserName()) + " order by myDate desc";
WorkLogDb wld = new WorkLogDb();
ir = wld.listResult(sql, 1, count).getResult().iterator();
BasicDataMgr bdm = new BasicDataMgr("kaoqin");
	while (ir.hasNext()) {
		wld = (WorkLogDb)ir.next();
%>
        <div><%=DateUtil.format(wld.getMyDate(), "yy-MM-dd HH:mm:ss")%>&nbsp;<a href="../mywork/mywork_show.jsp?id=<%=wld.getId()%>&userName=<%=StrUtil.UrlEncode(pu.getUserName())%>" title="工作报告" target="_blank"><%=StrUtil.getAbstract(request, wld.getContent(), 38)%></a></div>
    <%}%>
	</div>
	<div class="workItem"><strong><a title="日程安排" target="_blank" href="../plan/plan.jsp?userName=<%=StrUtil.UrlEncode(ud.getName())%>">日程安排：</a></strong></div>
	<div class="workItemContent">
		<%
		sql = "select id from user_plan where username=" + StrUtil.sqlstr(pu.getUserName()) + " order by myDate desc";
		PlanDb pd = new PlanDb();
		ir = pd.listResult(sql, 1, count).getResult().iterator();
		String  mydate, endDate;
		while (ir!=null && ir.hasNext()) {
			pd = (PlanDb)ir.next();
			mydate = DateUtil.format(pd.getMyDate(), "yy-MM-dd HH:mm");
			endDate = DateUtil.format(pd.getEndDate(),"yy-MM-dd HH:mm");		
		%>
	<div><%=mydate%>&nbsp;~&nbsp;<%=endDate%><a target="_blank" href="../plan/plan_show.jsp?id=<%=pd.getId()%>">&nbsp;<%=pd.getTitle()%></a></div>
<%}%>
	</div>    
	<div class="workItem"><strong><a title="工作计划" target="_blank" href="../workplan/workplan_list.jsp?userName=<%=StrUtil.UrlEncode(ud.getName())%>">工作计划：</a></strong></div>
	<div class="workItemContent">	
	<%
		sql = "select p.id from work_plan p, work_plan_user u where u.workPlanId=p.id and u.userName=" + StrUtil.sqlstr(pu.getUserName()) + " order by beginDate desc";
		com.redmoon.oa.workplan.WorkPlanDb wpd = new com.redmoon.oa.workplan.WorkPlanDb();
		com.redmoon.oa.workplan.WorkPlanTypeDb wptd = new com.redmoon.oa.workplan.WorkPlanTypeDb();
		ir = wpd.listResult(sql, 1, count).getResult().iterator();
		while (ir!=null && ir.hasNext()) {
			wpd = (com.redmoon.oa.workplan.WorkPlanDb)ir.next();
			int id = wpd.getId();
			String sbeginDate = DateUtil.format(wpd.getBeginDate(), "yyyy-MM-dd");
			String sendDate = DateUtil.format(wpd.getEndDate(), "yyyy-MM-dd");
		%>
          <div>
			<a title="<%=wpd.getTitle()%> 类型：<%=wptd.getWorkPlanTypeDb(wpd.getTypeId()).getName()%> 拟定者：<%=um.getUserDb(wpd.getAuthor()).getRealName()%> 进度：<%=wpd.getProgress()%>% 开始：<%=sbeginDate%> 结束：<%=sendDate%>" href="workplan_show.jsp?id=<%=id%>" target="_blank"><%=wpd.getTitle()%></a>
          </div>
          <%
		}
%>
	</div>
    
	<div class="workItem"><strong><a title="即时消息" target="_blank" href="../lark/lark_msg_my_list.jsp?userName=<%=StrUtil.UrlEncode(ud.getName())%>">即时消息：</a></strong></div>
	<div class="workItemContent">	
	<%
		sql = "select id from oa_lark_msg where (from_user=" + StrUtil.sqlstr(pu.getUserName()) + " || to_user=" + StrUtil.sqlstr(pu.getUserName()) + ") order by create_date asc";
		com.redmoon.oa.lark.LarkMsgDb lmd = new com.redmoon.oa.lark.LarkMsgDb();
		ir = lmd.listResult(sql, 1, 20).getResult().iterator();
		while (ir!=null && ir.hasNext()) {
			lmd = (com.redmoon.oa.lark.LarkMsgDb)ir.next();
		%>
          <div>
				<%=DateUtil.format(lmd.getDate("create_date"), "yy-MM-dd HH:mm:ss")%>&nbsp;<%=um.getUserDb(lmd.getString("from_user")).getRealName()%>：<%=StrUtil.toHtml(lmd.getString("content"))%>			
          </div>
          <%
		}
	%>
	</div>    

	<div class="workItem"><strong><a href="../kaoqin.jsp?userName=<%=StrUtil.UrlEncode(ud.getName())%>" target="_blank">考勤：</a></strong></div>
	<div class="workItemContentKaoqin">
		<%
		sql = "select id from kaoqin where name=" + StrUtil.sqlstr(pu.getUserName()) + " order by mydate desc";	
		KaoqinDb kd = new KaoqinDb();
		ir = kd.listResult(sql, 1, 4).getResult().iterator();
		String direction="",type="",reason="",strweekday="";
		Calendar cld = Calendar.getInstance();
		String[] wday = {"","日","一","二","三","四","五","六"};
		OACalendarDb oad = new OACalendarDb();
		String COLOR_LATE = "#ffeeee";
		String COLOR_BEFORE = "#ffff00";		
 		while (ir.hasNext()) {
			kd = (KaoqinDb)ir.next();
			direction = kd.getDirection();
			type = kd.getType();
			reason = kd.getReason();
			
			mydate = DateUtil.format(kd.getMyDate(), "yyyy-MM-dd HH:mm:ss");
			String strTempDate = DateUtil.format(kd.getMyDate(), "yyyy-MM-dd 00:00:00");
			java.util.Date dt = DateUtil.parse(strTempDate, "yyyy-MM-dd HH:mm:ss");
			cld.setTime(dt);
			
			mydate = mydate.substring(11,19);
			int weekday = cld.get(cld.DAY_OF_WEEK);
			strweekday = wday[weekday];
			// 计算是否迟到
			int myhour = cld.get(cld.HOUR_OF_DAY);
			int myminute = cld.get(cld.MINUTE);
			String backcolor = "";
			if (type.equals("考勤"))	{
				OACalendarDb oad2 = (OACalendarDb)oad.getQObjectDb(dt);
				if (oad2.getInt("date_type") == OACalendarDb.DATE_TYPE_WORK) {
					if (kd.getFlag()==1) {
						backcolor = COLOR_LATE;					
					}
					else if (kd.getFlag()==2) {
						backcolor = COLOR_BEFORE;								
					}
				}
			}					
			String directionDesc = bdm.getItemText("direction", direction);
		%>
          <div title="星期<%=strweekday%><%=DateUtil.format(kd.getMyDate(), "yyyy-MM-dd")%> <%=reason%>"><%=mydate%><%=directionDesc%><%=type%>迟到或早退<%=kd.getTimeMin()%>分钟</div>
          <%
		}%>
	</div>
	<div class="workItem"><strong><a href="../attendance/leave_list_user.jsp?userName=<%=StrUtil.UrlEncode(ud.getName())%>" target="_blank">请假：</a></strong></div>
	<div class="workItemContent">
<%
	sql = "select f.flowId from form_table_qjsqd f, flow fl where f.flowId=fl.id and f.flowTypeCode='qj' and (fl.status=" + WorkflowDb.STATUS_STARTED + " or fl.status=" + WorkflowDb.STATUS_FINISHED + ")";
	sql += " and f.applier=" + StrUtil.sqlstr(pu.getUserName()) + " order by fl.mydate desc";
	WorkflowDb wf = new WorkflowDb();
	ir = wf.listResult(sql, 1, count).getResult().iterator();
	com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
	
	FormDb fd = new FormDb();
	fd = fd.getFormDb("qjsqd");

	while (ir.hasNext()) {
		WorkflowDb wfd = (WorkflowDb)ir.next();
		fdao = fdao.getFormDAO(wfd.getId(), fd);
		String strBeginDate = fdao.getFieldValue("qjkssj");
		String strEndDate = fdao.getFieldValue("qjjssj");
		String xjrq = fdao.getFieldValue("xjrq");
		dd = dm.getDeptDb(fdao.getFieldValue("dept"));
		String deptName = "";
		if (dd!=null)
			deptName = dd.getName();
		String checker = fdao.getFieldValue("checker");
		if (!checker.equals(""))
			checker = um.getUserDb(checker).getRealName();
			
		java.util.Date ksDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
		java.util.Date jsDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
		java.util.Date xjDate = DateUtil.parse(xjrq, "yyyy-MM-dd");
		boolean isExpire = false;
		if (DateUtil.compare(xjDate, jsDate)==1) {
			isExpire = true;
		}
		String jqlb = fdao.getFieldValue("jqlb");
	%>
		<div title="审批：<%=fdao.getFieldValue("result").equals("1")?"通过":"不通过"%> 流程：<%=wfd.getStatusDesc()%> 审批：<%=checker%> 销假：<%=DateUtil.format(xjDate, "yy-MM-dd")%>">
		<%=jqlb%>从<%=DateUtil.format(ksDate, "yy-MM-dd")%>至<%=DateUtil.format(jsDate, "yy-MM-dd")%>&nbsp;	<%if (isExpire) {%><span style="color:red">超假</span><%}%>
		</div>
    <%}%>
	</div>
	</div>
<%}%>
</div>
</body>
</html>