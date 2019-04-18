<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.worklog.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.lark.*"%>
<%@ page import="com.redmoon.oa.workplan.*"%>
<%@ page import="com.redmoon.oa.task.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作报告 - 日报</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<style>
.workDiv p {
margin:0px;
line-height:1.5;
}
</style>
<script language="JavaScript" type="text/JavaScript">
<!--
function openWin(url,width,height){
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}
//-->
</script>
<script src="../inc/common.js"></script>
</head>
<body>
<%
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

boolean isShowDetail = ParamUtil.getInt(request, "isShowDetail", 0)==1;
%>
<%@ include file="mywork_nav.jsp"%>
<script>
o("menu1").className="current";
</script>
<%!
  int daysInMonth[] = {
      31, 28, 31, 30, 31, 30, 31, 31,
      30, 31, 30, 31};

  public int getDays(int month, int year) {
    //测试选择的年份是否是润年？
    if (1 == month)
      return ( (0 == year % 4) && (0 != (year % 100))) ||
          (0 == year % 400) ? 29 : 28;
        else
      return daysInMonth[month];
  }
%>
<%
	// 翻月
	int showyear,showmonth;
	Calendar cal = Calendar.getInstance();
	int curday = cal.get(cal.DAY_OF_MONTH);
	int curhour = cal.get(cal.HOUR_OF_DAY);
	int curminute = cal.get(cal.MINUTE);
	int curmonth = cal.get(cal.MONTH);
	int curyear = cal.get(cal.YEAR);
	
	String strshowyear = request.getParameter("showyear");
	String strshowmonth = request.getParameter("showmonth");
	if (strshowyear!=null)
		showyear = Integer.parseInt(strshowyear);
	else
		showyear = cal.get(cal.YEAR);
	if (strshowmonth!=null)
		showmonth = Integer.parseInt(strshowmonth);
	else
		showmonth = cal.get(cal.MONTH)+1;
%>
<%
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

%>
<br />
<table width="98%" border="0" align="center" height="40px" style="line-height:40px">
  <tr>
    <td width="50%" align="center">
	<!--<%=showmonth%>月-->
	<%if (!isShowDetail) {%>
&nbsp;&nbsp;<a href="mywork.jsp?userName=<%=StrUtil.UrlEncode(userName)%>&showyear=<%=showyear%>&showmonth=<%=showmonth%>&isShowDetail=1">按详细方式查看</a>
<%}else{%>
&nbsp;&nbsp;<a href="mywork.jsp?userName=<%=StrUtil.UrlEncode(userName)%>&showyear=<%=showyear%>&showmonth=<%=showmonth%>">按概要方式查看</a>
<%}%>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<select name="showyear" onChange="var y=this.options[this.selectedIndex].value; window.location.href='mywork.jsp?showyear=' + y + '&userName=<%=StrUtil.UrlEncode(userName)%>';">
      <%for (int y=curyear-60; y<=curyear; y++) {%>
      <option value="<%=y%>"><%=y%></option>
      <%}%>
    </select>
        <script>
		  showyear.value = "<%=showyear%>";
		  </script>
        <%
for (int i=1; i<=12; i++) {
	if (showmonth==i)
		out.print("<a href='mywork.jsp?userName="+StrUtil.UrlEncode(userName)+"&showyear="+showyear+"&showmonth="+i+"'><font color=red>"+i+"月</font></a>&nbsp;");
	else
		out.print("<a href='mywork.jsp?userName="+StrUtil.UrlEncode(userName)+"&showyear="+showyear+"&showmonth="+i+"'>"+i+"月</a>&nbsp;");

}
%>
    <a href="mywork_list.jsp?userName=<%=StrUtil.UrlEncode(userName)%>">全部</a></td>
    <form method="post" action="mywork_list.jsp?op=search&userName=<%=StrUtil.UrlEncode(userName)%>">
      <td width="50%" align="center">搜索&nbsp;
          <input name="what" value="">
        &nbsp;
        <input class="btn" name="submit" type="submit" value="确定">
	  </td>
    </form>
  </tr>
</table>
<%
cal.set(showyear,showmonth-1,1,0,0,0);
java.util.Date d1 = cal.getTime();
cal.set(showyear,showmonth-1,getDays(showmonth-1, showyear),23,59,59);
java.util.Date d2 = cal.getTime();

String sql = "select id from work_log where myDate>=" + SQLFilter.getDateStr(DateUtil.format(d1, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss") + " and myDate<=" +SQLFilter.getDateStr(DateUtil.format(d2, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss") + " and userName="+StrUtil.sqlstr(userName)+" and log_type=" + WorkLogDb.TYPE_NORMAL + " order by myDate asc";

int i = 1;
String content="",mydate="",strweekday="";
int id = -1;
int weekday=0;
Date dt = null;
int monthday = -1;
int monthdaycount = getDays(showmonth-1,showyear);//当前显示月份的天数
String[] wday = {"","日","一","二","三","四","五","六"};
boolean rsnotend = true;
Calendar cld = Calendar.getInstance();

com.redmoon.oa.person.UserMgr um = new com.redmoon.oa.person.UserMgr();

WorkLogDb wld = new WorkLogDb();
Iterator ir = wld.list(sql).iterator();

int totalWorkflow = 0;
int totalWorkplan = 0;
int totalDoc = 0;
int totalTask = 0;
%>
<table width="98%" class="tabStyle_1 percent98">
  <tr>
    <td width="10%" class="tabStyle_1_title">星期</td>
    <td width="10%" class="tabStyle_1_title">日期</td>
    <td width="55%" class="tabStyle_1_title">内 容</td>
    <td width="15%" class="tabStyle_1_title">记录时间</td>
    <td width="10%" class="tabStyle_1_title">操作</td>
  </tr>
<%
		if (ir.hasNext()) {
			wld = (WorkLogDb)ir.next();
			mydate = DateUtil.format(wld.getMyDate(), "yyyy-MM-dd");
			cld.setTime(wld.getMyDate());
			monthday = cld.get(cld.DAY_OF_MONTH);
		}
		boolean canEditPreviousWorklog = com.redmoon.oa.worklog.Config.getInstance().getBooleanProperty("canEditPreviousWorklog");			
		while (i<=monthdaycount) {
			boolean isCurDayWorkWritten = false;
			Calendar showCal = Calendar.getInstance();
			showCal.set(showyear, showmonth-1, i);
			weekday = showCal.get(cld.DAY_OF_WEEK);
			strweekday = wday[weekday];
			mydate = "";
			if (monthday==i) {
				if (wld.isLoaded()) {
					isCurDayWorkWritten = true;
					id = wld.getId();
					content = wld.getContent();
					mydate = DateUtil.format(wld.getMyDate(), "yyyy-MM-dd");
					cld.setTime(wld.getMyDate());
				}
			}				
		%>
  <tr class="highlight">
    <td align="center">
	<%
	String clr = "";
	if (weekday==1 || weekday==7) {
		clr = "red";
	}
	%>
	<span style="color:<%=clr%>"><%=strweekday%></span>	</td>
    <td align="center"><%=i%></td>
    <td style='word-break:break-all'>
    <%
	String bdate = SQLFilter.getDateStr(DateUtil.format(showCal, "yyyy-MM-dd 00:00:00"), "yyyy-MM-dd HH:mm:ss");
    Calendar nextCal = Calendar.getInstance();
	nextCal.set(showyear, showmonth-1, i+1);
	String edate = SQLFilter.getDateStr(DateUtil.format(nextCal, "yyyy-MM-dd 00:00:00"), "yyyy-MM-dd HH:mm:ss");
	
	sql = "select id from flow_my_action where (user_name=" + StrUtil.sqlstr(userName) + " or proxy=" + StrUtil.sqlstr(userName) + ") and is_checked=1 and check_date>=" + bdate + " and check_date<" + edate + " order by receive_date asc";
	MyActionDb mad = new MyActionDb();
	Vector actionV = mad.list(sql);
	totalWorkflow += actionV.size();
	if (isShowDetail) {
		if (actionV.size()>0) {
	%>
		<table width="98%" align="center" class="tabStyle_1 percent98">
		  <tbody>
			<tr>
			  <td width="59%" class="tabStyle_1_title">流程</td>
			  <td width="26%" class="tabStyle_1_title">到期时间</td>
			  <td width="15%" class="tabStyle_1_title">绩效</td>
		    </tr>
			<%
			Iterator iraction = actionV.iterator();	
			Directory dir = new Directory();	
			WorkflowDb wfd2 = new WorkflowDb();
			while (iraction.hasNext()) {
				mad = (MyActionDb)iraction.next();
				WorkflowDb wfd = wfd2.getWorkflowDb((int)mad.getFlowId());
				String starter = wfd.getUserName();
				String userRealName = "";
				if (starter!=null) {
					userRealName = um.getUserDb(wfd.getUserName()).getRealName();
				}
			%>
			<tr class="highlight">
			  <td>&nbsp;&nbsp;<a title="<%=wfd.getTitle()%>" href="../flow_modify.jsp?flowId=<%=wfd.getId()%>&actionId=<%=mad.getActionId()%>"><%=StrUtil.getLeft(wfd.getTitle(), 40)%></a></td>
			  <td align="center"><%=DateUtil.format(mad.getExpireDate(), "yy-MM-dd HH:mm")%></td>
			  <td align="center"><%=NumberUtil.round(mad.getPerformance(), 2)%></td>
		    </tr>
			<%}%>
		  </tbody>
	  </table>
<%	}
	}else{
		if (DateUtil.compare(new java.util.Date(), showCal.getTime())==1) {	
	%>
		办理流程：<%=actionV.size()%>&nbsp;个
		<%}
	}%>
	<%
	sql = "select id from document where nick=" + StrUtil.sqlstr(userName) + " and createDate>=" + bdate + " and createDate<" + edate + " order by createDate asc";
	com.redmoon.oa.fileark.Document doc = new com.redmoon.oa.fileark.Document();
	Vector docV = doc.list(sql, 100);
	totalDoc += docV.size();
	if (isShowDetail) {
	if (docV.size()>0) {
	%>
	<table class="tabStyle_1 percent98" cellspacing="0" cellpadding="0" width="98%" align="center">
      <tbody>
        <tr>
          <td class="tabStyle_1_title" width="59%" height="28" nowrap="nowrap">文件</td>
          <td class="tabStyle_1_title" width="26%" nowrap="nowrap">创建时间</td>
          <td class="tabStyle_1_title" width="15%" nowrap="nowrap">审核状态</td>
          </tr>
        <%
		Iterator irdoc = docV.iterator();		
		while (irdoc.hasNext()) {
			doc = (com.redmoon.oa.fileark.Document)irdoc.next(); 
			String color = doc.getColor();
			boolean isBold = doc.isBold();
			java.util.Date expireDate = doc.getExpireDate();
	%>
        <tr onmouseover="this.className='tbg1sel'" onmouseout="this.className='tbg1'" class="tbg1">
          <td height="24"><%if (doc.getType()==1) {%>
              <img height="15" alt="" src="forum/skin/bluedream/images/f_poll.gif" width="17" border="0" />&nbsp;
              <%}%>
              <%if (DateUtil.compare(new java.util.Date(), expireDate)==2) {%>
              <a href="doc_show.jsp?id=<%=doc.getID()%>" title="<%=doc.getTitle()%>">
              <%
		if (isBold)
			out.print("<B>");
		if (!color.equals("")) {
		%>
              <font color="<%=color%>">
              <%}%>
              <%=doc.getTitle()%>
              <%if (!color.equals("")) {%>
              </font>
              <%}%>
              <%
		if (isBold)
			out.print("</B>");
		%>
              </a>
              <%}else{%>
              <a href="../doc_show.jsp?id=<%=doc.getID()%>" title="<%=doc.getTitle()%>"><%=doc.getTitle()%></a>
            <%}%></td>
          <td align="center"><%
	  java.util.Date d = doc.getCreateDate();
	  if (d!=null)
	  	out.print(DateUtil.format(d, "yy-MM-dd HH:mm"));
	  %></td>
          <td align="center"><%
	  int examine = doc.getExamine();
	  if (examine==0)
	  	out.print("<font color='blue'>未审核</font>");
	  else if (examine==1)
	  	out.print("<font color='red'>未通过</font>");
	  else
	  	out.print("已通过");
	  %>          </td>
          </tr>
        <%}%>
      </tbody>
    </table>
<%}
	}else{
		if (DateUtil.compare(new java.util.Date(), showCal.getTime())==1) {
	%>
		文件柜：<%=docV.size()%>&nbsp;个
		<%}
	}%>
<%
		sql = "select id from work_plan_annex where user_name=" + StrUtil.sqlstr(userName) + " and add_date>=" + bdate + " and add_date<" + edate + " order by add_date asc";
		WorkPlanAnnexDb wad = new WorkPlanAnnexDb();
		Vector wplanV = wad.list(sql);		
		totalWorkplan += wplanV.size();
		if (isShowDetail) {
			if (wplanV.size()>0) {
			%>
			<table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
			  <tr>
				<td width="42%" height="22" class="tabStyle_1_title">计划</td>
				<td width="31%" class="tabStyle_1_title">内容</td>
				<td width="27%" class="tabStyle_1_title">进度</td>
				</tr>
			<%
			Iterator irwplan = wplanV.iterator();
			WorkPlanMgr wpm = new WorkPlanMgr();
			while (irwplan.hasNext()) {
				wad = (WorkPlanAnnexDb)irwplan.next();
			%>
			  <tr>
				<td><%=wpm.getWorkPlanDb(wad.getInt("workplan_id")).getTitle()%></td>
				<td><%=HtmlUtil.getAbstract(request, wad.getString("content"), 200)%></td>
				<td align="center">
                  <div class="progressBar">
                    <div class="progressBarFore" style="width:<%=wad.getInt("progress")%>%;">
                      </div>
                    <div class="progressText">
                      <%=wad.getInt("progress")%>%
                      </div>
                    </div>
                </td>
			  </tr>
			<%}%>
	  </table>
<%}
		}else{
			if (DateUtil.compare(new java.util.Date(), showCal.getTime())==1) {		
		%>
			计划进度：<%=wplanV.size()%>&nbsp;个
			<%}
		}%>
		
<%
		sql = "select id from task where person=" + StrUtil.sqlstr(userName) + " and mydate>=" + bdate + " and mydate<=" + edate;
		TaskDb td = new TaskDb();
		Vector taskV = td.list(sql);		
		totalTask += taskV.size();
		if (isShowDetail) {
			if (taskV.size()>0) {
			%>
			<table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
			  <tr>
				<td width="46%" height="22" class="tabStyle_1_title">任务</td>
				<td width="39%" class="tabStyle_1_title">进度</td>
				<td width="15%" class="tabStyle_1_title">状态</td>
				</tr>
			<%
			Iterator irTask = taskV.iterator();
			while (irTask.hasNext()) {
				td = (TaskDb)irTask.next();
			%>
			  <tr>
				<td><a href="../task_show.jsp?rootid=<%=td.getRootId()%>&showid=<%=td.getId()%>&projectId=<%=td.getProjectId()%>"><%=td.getTitle()%></a></td>
				<td><%=td.getProgress()%></td>
				<td align="center"><%=TaskDb.getTaskStatusDesc(td.getStatus())%></td>
			  </tr>
			<%}%>
	  </table>
<%}
		}else{
			if (DateUtil.compare(new java.util.Date(), showCal.getTime())==1) {		
		%>
			<!--任务督办：<%=taskV.size()%>&nbsp;个-->
			<%}
		}%>
		
<%
		sql = "select id from oa_lark_msg where (from_user=" + StrUtil.sqlstr(userName) + " || to_user=" + StrUtil.sqlstr(userName) + ") and create_date>=" + bdate + " and create_date<=" + edate + " order by create_date asc";
		
		LarkMsgDb lmd = new LarkMsgDb();
		Vector msgV = lmd.list(sql);		
		if (isShowDetail) {
			if (msgV.size()>0) {
			%>
			<table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
            <thead>
			  <tr>
			    <td>即时消息</td>
		      </tr>
            </thead>
			<%
			Iterator irMsg = msgV.iterator();
			while (irMsg.hasNext()) {
				lmd = (LarkMsgDb)irMsg.next();
			%>
			  <tr>
				<td width="46%">
				<%=DateUtil.format(lmd.getDate("create_date"), "yy-MM-dd HH:mm:ss")%>&nbsp;<%=um.getUserDb(lmd.getString("from_user")).getRealName()%>：<%=StrUtil.toHtml(lmd.getString("content"))%>
                </td>
			  </tr>
			<%}%>
	  		</table>
		<%}
		}%>
        
		<%if (isCurDayWorkWritten) {%>
            <div class="workDiv">
			<a title="查看" href="javascript:;" onclick="addTab('<%=mydate%>', '<%=request.getContextPath()%>/mywork/mywork_show.jsp?id=<%=id%>&userName=<%=StrUtil.UrlEncode(userName)%>')"><%=StrUtil.getAbstract(request, wld.getContent(), 320)%></a>
			<p><font color="#6666FF"><%=StrUtil.getNullStr(wld.getAppraise())%></font></p>
			</div>
			<% 
				
				WorkLogDb workLogDb = new WorkLogDb();
				workLogDb = workLogDb.getWorkLogDb(wld.getId());
				
				Iterator ir1 = workLogDb.getAttachs().iterator();
				
				while (ir1.hasNext()) {
                    WorkLogAttachmentDb workLogAttachmentDb = (WorkLogAttachmentDb)ir1.next();
			%>
			<div><img src="../images/attach2.gif" width="17" height="17" />
		    	<a target="_blank" href="mywork_getfile.jsp?attachId=<%=workLogAttachmentDb.getId()%>"><%=workLogAttachmentDb.getName()%></a>
		    </div>
		    <%} %>
		<%}%>
	</td>
    <td align="center"><%=mydate%></td>
    <td align="center">
    <%if (isCurDayWorkWritten) {%>
    	<%
		if (canEditPreviousWorklog || (curyear==showyear && curmonth+1==showmonth && curday==i)) { %>
        <a title="修改" href="mywork_edit.jsp?id=<%=id%>&userName=<%=StrUtil.UrlEncode(userName)%>">修改</a>
        <%}%>
  	  	<%if (userName.equals(privilege.getUser(request)) || privilege.canAdminUser(request, userName)) {%>
      	&nbsp; <a title="点评" href="mywork_appraise.jsp?id=<%=id%>&userName=<%=StrUtil.UrlEncode(userName)%>">点评</a>
	  	<%}%>
	 <%}%>
     </td>
  </tr>
<%
			if (monthday==i) {
				if (ir.hasNext()) {
					wld = (WorkLogDb)ir.next();
					cld.setTime(wld.getMyDate());
					monthday = cld.get(cld.DAY_OF_MONTH);
				}			
			}

			i++;
		}
%>
  <tr class="highlight">
    <td align="center">合计</td>
    <td align="center">&nbsp;</td>
    <td>办理流程：<%=totalWorkflow%>个&nbsp;&nbsp;文件柜：<%=totalDoc%>个&nbsp;&nbsp;计划进度：<%=totalWorkplan%>个&nbsp;&nbsp;任务督办：<%=totalTask%>个</td>
    <td align="center">&nbsp;</td>
    <td align="center">&nbsp;</td>
  </tr>
</table>
<br>
</body>
<script>
function form1_onsubmit() {
	form1.content.value = getHtml();
}
</script>
</html>
