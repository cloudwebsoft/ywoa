<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.kaoqin.*" %>
<%@ page import="com.redmoon.oa.worklog.*" %>
<%@ page import="com.redmoon.oa.oacalendar.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="cn.js.fan.db.SQLFilter" %>
<%@ page import="cn.js.fan.db.ListResult" %>
<%@ page import="java.util.Date" %>
<%@ page import="com.redmoon.oa.attendance.AttendanceMgr" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>用户当前工作详情</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
    <style type="text/css">
        .centerBox {
            margin: 0px auto;
        }
    </style>
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
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">工作详情</td>
    </tr>
  </tbody>
</table>
<%
	String userName = ParamUtil.get(request, "userName");
	if (!privilege.canAdminUser(request, userName)) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	com.redmoon.oa.person.UserMgr um = new com.redmoon.oa.person.UserMgr();
	UserDb user = um.getUserDb(userName);
	String op = ParamUtil.get(request, "op");
%>
  <table class="tabStyle_1 percent98" width="99%"  border="0" align="center" cellspacing="0">
    <tr>
      <td height="24" align="center" class="tabStyle_1_title"><%=user.getRealName()%></td>
    </tr>
    <tr>
      <td height="22" align="left" valign="top">
	  <!--<a href="../netdisk/netdisk_frame.jsp?op=showDirShare&amp;userName=<%=StrUtil.UrlEncode(user.getName())%>" title="网络硬盘共享" target="_blank">网盘共享</a>&nbsp;&nbsp;&nbsp;<a title="用户信息" href="../user_info.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>" target="_blank">个人信息</a>-->
          <div class="percent98 centerBox" style="margin-top: 10px"><a href="javascript:;" onclick="addTab('待办流程', '<%=request.getContextPath() %>/flow/flow_list.jsp?displayMode=1&userName=<%=StrUtil.UrlEncode(user.getName())%>')" title="待办流程"><strong>待办流程</strong></a>		</div>
		<table width="98%" align="center" class="tabStyle_1_subTab percent98">
		  <tbody>
		    <tr>
		      <td align="center" width="32%" class="tabStyle_1_subTab_title">标题</td>
        <td align="center" width="14%" class="tabStyle_1_subTab_title">类型</td>
        <td align="center" width="14%" class="tabStyle_1_subTab_title">开始时间</td>
        <td align="center" width="13%" class="tabStyle_1_subTab_title">到期时间</td>
        <td align="center" width="10%" class="tabStyle_1_subTab_title">发起人</td>
        </tr>
  <%
	MyActionDb mad = new MyActionDb();
	String sql = "select id from flow_my_action where (user_name=" + StrUtil.sqlstr(user.getName()) + " or proxy=" + StrUtil.sqlstr(user.getName()) + ") and is_checked=0 order by receive_date desc";
	int count = 3;
	Iterator ir = mad.listResult(sql, 1, count).getResult().iterator();
	Directory dir = new Directory();
	while (ir.hasNext()) {
		mad = (MyActionDb)ir.next();
		WorkflowDb wfd = new WorkflowDb();
		wfd = wfd.getWorkflowDb((int)mad.getFlowId());
		String userRealName = new UserDb(wfd.getUserName()).getRealName();
		Leaf ft = dir.getLeaf(wfd.getTypeCode());
%>
		<tr class="highlight">
		<td><a href="javascript:;" onclick="addTab('<%=wfd.getTitle() %>', '<%=Global.getFullRootPath(request) %>/flow_modify.jsp?flowId=<%=wfd.getId()%>')" title="<%=wfd.getTitle()%>"><%=StrUtil.getLeft(wfd.getTitle(), 40)%></a> </td>
        <td><%if (ft!=null) {%>
          <%=ft.getName()%>
          <%}%>      </td>
        <td align="center"><%=DateUtil.format(wfd.getBeginDate(), "yyyy-MM-dd HH:mm")%> </td>
        <td align="center"><%=DateUtil.format(mad.getExpireDate(), "yyyy-MM-dd HH:mm")%></td>
        <td align="center"><%=userRealName%></td>
        </tr>
		    <%}%>
	      </tbody>
        </table>
          <div class="percent98 centerBox"><a title="已办流程" href="javascript:;" onclick="addTab('<%=user.getRealName() %>的已办流程', '<%=Global.getFullRootPath(request) %>/flow_list_done.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>')"><strong>已办流程</strong></a></div>
		<table width="98%" align="center" class="tabStyle_1_subTab percent98">
          <tbody>
            <tr>
              <td align="center" width="23%" class="tabStyle_1_subTab_title">标题</td>
              <td align="center" width="14%" class="tabStyle_1_subTab_title">类型</td>
              <td align="center" width="12%" class="tabStyle_1_subTab_title">到达时间</td>
              <td align="center" width="11%" class="tabStyle_1_subTab_title">处理时间</td>
              <td align="center" width="11%" class="tabStyle_1_subTab_title">到期时间</td>
              <td align="center" style="display:none" width="6%" class="tabStyle_1_subTab_title">绩效</td>
              <td align="center" width="10%" class="tabStyle_1_subTab_title">发起人</td>
            </tr>
		<%
sql = "select id from flow_my_action where (user_name=" + StrUtil.sqlstr(user.getName()) + " or proxy=" + StrUtil.sqlstr(user.getName()) + ") and is_checked=1 order by receive_date desc";
ir = mad.listResult(sql, 1, count).getResult().iterator();
WorkflowDb wfd2 = new WorkflowDb();
while (ir.hasNext()) {
 	mad = (MyActionDb)ir.next();
	WorkflowDb wfd = wfd2.getWorkflowDb((int)mad.getFlowId());
	String userRealName = new UserDb(wfd.getUserName()).getRealName();
	%>
            <tr class="highlight">
              <td><a href="javascript:;" onclick="addTab('<%=wfd.getTitle() %>', '<%=Global.getFullRootPath(request) %>/flow_modify.jsp?flowId=<%=wfd.getId()%>&amp;actionId=<%=mad.getActionId()%>')" title="<%=wfd.getTitle()%>"><%=StrUtil.getLeft(wfd.getTitle(), 40)%></a></td>
              <td align="left"><%
	  Leaf ft = dir.getLeaf(wfd.getTypeCode());
	  %>
                  <%if (ft!=null) {%>
                  <%=ft.getName()%>
                  <%}%>
			  </td>
              <td align="center"><%=DateUtil.format(mad.getReceiveDate(), "yyyy-MM-dd HH:mm")%> </td>
              <td align="center"><%=DateUtil.format(mad.getCheckDate(), "yyyy-MM-dd HH:mm")%> </td>
              <td align="center"><%=DateUtil.format(mad.getExpireDate(), "yyyy-MM-dd HH:mm")%></td>
              <td style="display:none" align="center"><%=NumberUtil.round(mad.getPerformance(), 2)%></td>
              <td align="center"><%=userRealName%></td>
            </tr>
            <%}%>
          </tbody>
        </table>
		<div class="percent98 centerBox"><a title="工作计划" href="javascript:;" onclick="addTab('<%=user.getRealName() %>的工作计划', '<%=Global.getFullRootPath(request) %>/workplan/workplan_list.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>')"><strong>工作计划</strong></a></div>
		<%
		sql = "select p.id from work_plan p, work_plan_user u where u.workPlanId=p.id and u.userName=" + StrUtil.sqlstr(user.getName()) + " order by beginDate desc";
		com.redmoon.oa.workplan.WorkPlanDb wpd = new com.redmoon.oa.workplan.WorkPlanDb();
		com.redmoon.oa.workplan.WorkPlanTypeDb wptd = new com.redmoon.oa.workplan.WorkPlanTypeDb();
		ir = wpd.listResult(sql, 1, count).getResult().iterator();
		%>
		<table width="98%" align="center" class="tabStyle_1_subTab percent98">
          <tr>
            <td align="center" width="34%" class="tabStyle_1_subTab_title">标题            </td>
            <td align="center" width="12%" class="tabStyle_1_subTab_title">类型 </td>
            <td align="center" width="12%" class="tabStyle_1_subTab_title">拟定者            </td>
            <td align="center" width="11%" class="tabStyle_1_subTab_title">进度            </td>
            <td align="center" width="16%" class="tabStyle_1_subTab_title">开始日期            </td>
            <td align="center" width="15%" class="tabStyle_1_subTab_title">结束日期            </td>
          </tr>
          <%
		while (ir!=null && ir.hasNext()) {
			wpd = (com.redmoon.oa.workplan.WorkPlanDb)ir.next();
			int id = wpd.getId();
			String sbeginDate = DateUtil.format(wpd.getBeginDate(), "yyyy-MM-dd");
			String sendDate = DateUtil.format(wpd.getEndDate(), "yyyy-MM-dd");
		%>
          <tr class="highlight">
            <td><a href="javascript:;" onclick="addTab('<%=wpd.getTitle() %>', '<%=Global.getFullRootPath(request) %>/workplan/workplan_show.jsp?id=<%=id%>')"><%=wpd.getTitle()%></a></td>
            <td align="center"><%=wptd.getWorkPlanTypeDb(wpd.getTypeId()).getName()%></td>
            <td align="center"><%=um.getUserDb(wpd.getAuthor()).getRealName()%></td>
            <td align="center"><%=wpd.getProgress()%>&nbsp;&nbsp;%</td>
            <td align="center"><%=sbeginDate%></td>
            <td align="center"><%=sendDate%></td>
          </tr>
          <%
		}
%>
        </table>
          <div class="percent98 centerBox"><a title="工作报告" href="javascript:;" onclick="addTab('<%=user.getRealName() %>的工作报告', '<%=Global.getFullRootPath(request) %>/ymoa/showWorkLogInfo.action?userName=<%=StrUtil.UrlEncode(user.getName())%>')"><strong>工作报告</strong></a></div>
		<%
sql = "select id from work_log where userName=" + StrUtil.sqlstr(userName) + " order by myDate desc";
WorkLogDb wld = new WorkLogDb();
ir = wld.listResult(sql, 1, count).getResult().iterator();
		%>
		<table width="98%" align="center" class="tabStyle_1_subTab percent98">
          <tbody>
            <tr>
              <td align="center" width="15%" class="tabStyle_1_subTab_title">时间</td>
              <td align="center" width="5%" class="tabStyle_1_subTab_title">类型</td>
              <td align="center" width="80%" class="tabStyle_1_subTab_title">记事</td>
            </tr>
            <%
	while (ir.hasNext()) {
		wld = (WorkLogDb)ir.next();
%>
            <tr class="highlight">
            <td align="center"><%=DateUtil.format(wld.getMyDate(), "yyyy-MM-dd HH:mm")%> </td>
            <td align="center"><%=wld.getLogType() == WorkLogDb.TYPE_NORMAL ? "日报" : (wld.getLogType() == WorkLogDb.TYPE_WEEK ? "周报" : "月报")%> </td>
              <td>
			  <a href="javascript:;" onclick="addTab('查看工作报告', '<%=Global.getFullRootPath(request) %>/ymoa/showWorkLogById.action?workLogId=<%=wld.getId()%>')" title="查看"><%=wld.getContent()%></a> </td>
            </tr>
            <%}%>
          </tbody>
        </table>

          <div class="percent98 centerBox"><a title="日程安排" href="javascript:;" onclick="addTab('<%=user.getRealName() %>的日程安排', '<%=Global.getFullRootPath(request) %>/plan/plan.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>')"><strong>日程安排</strong></a></div>
		<%
		sql = "select id from user_plan where username=" + StrUtil.sqlstr(user.getName()) + " order by myDate desc";
		PlanDb pd = new PlanDb();
		ir = pd.listResult(sql, 1, count).getResult().iterator();

		%>
		<table width="98%" align="center" class="tabStyle_1_subTab percent98">
          <tr>
            <td align="center" width="25%" class="tabStyle_1_subTab_title">安排日期</td>
            <td align="center" class="tabStyle_1_subTab_title">标题</td>
          </tr>
          <%
		String  mydate, endDate;
		while (ir!=null && ir.hasNext()) {
			pd = (PlanDb)ir.next();
			mydate = DateUtil.format(pd.getMyDate(), "yyyy-MM-dd HH:mm");
			endDate = DateUtil.format(pd.getEndDate(),"yyyy-MM-dd HH:mm");
		%>
          <tr>
            <td width="19%" align="left"><%=mydate%>&nbsp;~&nbsp;<%=endDate%></td>
            <td width="81%"><a href="javascript:;" onclick="addTab('<%=pd.getTitle() %>', '<%=Global.getFullRootPath(request) %>/plan/plan_show.jsp?id=<%=pd.getId()%>')"><%=pd.getTitle()%></a></td>
          </tr>
          <%}%>
        </table>
          <div class="percent98 centerBox"><a href="javascript:;" onclick="addTab('<%=user.getRealName() %>的考勤', '<%=Global.getFullRootPath(request) %>/kaoqin.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>')"><strong>考勤</strong></a></div>
		<%
            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            boolean isUseKqj = cfg.getBooleanProperty("isUseKqj");
            if (isUseKqj) {
                sql = "select s.id from form_table_kaoqin_time_sign s, users u where s.number = u.person_no";
            } else {
                sql = "select s.id from form_table_kaoqin_time_sign s, users u where s.name = u.name";
            }
            sql += " and u.name=" + SQLFilter.sqlstr(user.getName()) + " order by id desc";
            com.redmoon.oa.visual.FormDAO fdaoSign = new com.redmoon.oa.visual.FormDAO();
            ListResult lr = fdaoSign.listResult("kaoqin_time_sign", sql, 1, 6);
            ir = lr.getResult().iterator();
		%>
		<table width="98%" align="center" class="tabStyle_1_subTab percent98">
            <tr>
                <td align="center" width="8%" class="tabStyle_1_subTab_title">星期</td>
                <td align="center" width="14%" class="tabStyle_1_subTab_title">日期</td>
                <td align="center" width="14%" class="tabStyle_1_subTab_title">时间</td>
                <td align="center" width="12%" class="tabStyle_1_subTab_title">去向</td>
                <td align="center" width="11%" class="tabStyle_1_subTab_title">结果</td>
                <td align="center" width="8%" class="tabStyle_1_subTab_title">位置异常</td>
                <td align="center" width="13%" class="tabStyle_1_subTab_title">位置</td>
                <td align="center" width="20%" class="tabStyle_1_subTab_title">事由</td>
            </tr>
            <%
		String type="",reason="",strweekday="",location="";
		Calendar cld = Calendar.getInstance();
		String[] wday = {"","日","一","二","三","四","五","六"};
		OACalendarDb oad = new OACalendarDb();
		String COLOR_LATE = "#ffeeee";
		String COLOR_BEFORE = "#ffff00";
		BasicDataMgr bdm = new BasicDataMgr("kaoqin");
 		while (ir.hasNext()) {
            fdaoSign = (com.redmoon.oa.visual.FormDAO)ir.next();
            int signType = StrUtil.toInt(fdaoSign.getFieldValue("sign_type"), AttendanceMgr.TYPE_ON_DUTY_1);
            int signResult = StrUtil.toInt(fdaoSign.getFieldValue("sign_result"), -100);
            boolean isLocAbnormal = "1".equals(fdaoSign.getFieldValue("is_loc_abnormal"));
            location = StrUtil.getNullStr(fdaoSign.getFieldValue("location"));

            String strSignTime = fdaoSign.getFieldValue("sign_time");
            Date d = DateUtil.parse(strSignTime, "yyyy-MM-dd HH:mm:ss");

			String strTempDate = DateUtil.format(d, "yyyy-MM-dd 00:00:00");
			java.util.Date dt = DateUtil.parse(strTempDate, "yyyy-MM-dd HH:mm:ss");
			cld.setTime(dt);

            strSignTime = strSignTime.substring(11,19);
			int weekday = cld.get(cld.DAY_OF_WEEK);
			strweekday = wday[weekday];

            // 如果是导入的数据，则无考勤结果
            if (signResult == -100) {
                long[] aryCheck = AttendanceMgr.check(userName, d, signType, isLocAbnormal);
                signResult = (int)aryCheck[0];
            }

            String strSignType = bdm.getItemText("punch_type", String.valueOf(signType));
            String strSignResult = bdm.getItemText("punch_result", String.valueOf(signResult));
            String isLocAbnormalDesc = bdm.getItemText("yesorno", String.valueOf(isLocAbnormal?1:0));
		%>
          <tr class="highlight">
              <td align="center"><%=strweekday%></td>
              <td align="center"><%=DateUtil.format(dt, "yyyy-MM-dd")%></td>
              <td align="center"><%=strSignTime%></td>
              <td align="center"><%=strSignType%></td>
              <td align="center"><%=strSignResult%></td>
              <td align="center"><%=isLocAbnormalDesc%></td>
              <td align="center"><%=location%></td>
            <td><%=reason%></td>
          </tr>
          <%
		}%>
        </table>
          <div class="percent98 centerBox"><a href="javascript:;" onclick="addTab('<%=user.getRealName() %>的请假', '<%=Global.getFullRootPath(request) %>/attendance/leave_list_user.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>')"><strong>请假</strong></a></div>
		<%
		sql = "select f.flowId from form_table_qjsqd f, flow fl where f.flowId=fl.id and f.flowTypeCode='qj' and (fl.status=" + WorkflowDb.STATUS_STARTED + " or fl.status=" + WorkflowDb.STATUS_FINISHED + ")";
		sql += " and f.applier=" + StrUtil.sqlstr(user.getName()) + " order by fl.mydate desc";
		WorkflowDb wf = new WorkflowDb();
		ir = wf.listResult(sql, 1, count).getResult().iterator();
		%>
		<table width="98%" align="center" class="tabStyle_1_subTab percent98">
          <tbody>
          <tr>
              <td align="center" width="9%" class="tabStyle_1_subTab_title">假期类别</td>
              <td align="center" width="11%" class="tabStyle_1_subTab_title">开始日期</td>
              <td align="center" width="9%" class="tabStyle_1_subTab_title">结束日期</td>
              <td align="center" width="11%" class="tabStyle_1_subTab_title">销假日期</td>
              <td align="center" width="11%" class="tabStyle_1_subTab_title">请假时间</td>
              <td align="center" width="9%" class="tabStyle_1_subTab_title">审批结果</td>
              <td align="center" width="10%" class="tabStyle_1_subTab_title">流程状态</td>
          </tr>
            <%
com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();

FormDb fd = new FormDb();
fd = fd.getFormDb("qjsqd");
DeptMgr dm = new DeptMgr();
while (ir.hasNext()) {
 	WorkflowDb wfd = (WorkflowDb)ir.next();
	fdao = fdao.getFormDAO(wfd.getId(), fd);
	String strBeginDate = fdao.getFieldValue("qjkssj");
	String strEndDate = fdao.getFieldValue("qjjssj");
	String xjrq = fdao.getFieldValue("xjrq");
	DeptDb dd = dm.getDeptDb(fdao.getFieldValue("dept"));
	String deptName = "";
	if (dd != null)
		deptName = dd.getName();

	java.util.Date ksDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
	java.util.Date jsDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
	java.util.Date xjDate = DateUtil.parse(xjrq, "yyyy-MM-dd");
	boolean isExpire = false;
	if (DateUtil.compare(xjDate, jsDate)==1) {
		isExpire = true;
	}
	%>
            <tr class="highlight">
              <td align="center"><%
	  String jqlb = fdao.getFieldValue("jqlb");
	  %>
              <%=jqlb%>			  </td>
              <td align="center"><%=strBeginDate%></td>
              <td align="center"><%=strEndDate%></td>
              <td align="center"><%if (isExpire) {%>
                  <span style="color:red"><%=xjrq%></span>
                  <%}else{%>
                  <%=xjrq%>
              <%}%>              </td>
              <td align="center"><%=DateUtil.format(wfd.getMydate(), "yyyy-MM-dd HH:mm")%> </td>
              <td align="center"><%=fdao.getFieldValue("result").equals("1")?"通过":"不通过"%></td>
              <td align="center"><%=wfd.getStatusDesc()%></td>
            </tr>
            <%}%>
          </tbody>
      </table></td>
    </tr>
  </table>
</body>
</html>