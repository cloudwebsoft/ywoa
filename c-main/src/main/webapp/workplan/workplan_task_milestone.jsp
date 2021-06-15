<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.oacalendar.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
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
String action = ParamUtil.get(request, "action");
String kind = ParamUtil.get(request, "kind");
String what = ParamUtil.get(request, "what");
String beginDate = ParamUtil.get(request, "beginDate");
String endDate = ParamUtil.get(request, "endDate");
int progressFlag = ParamUtil.getInt(request, "progressFlag", -1);

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "orders";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "asc";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>里程碑</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>

<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

<script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>

<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>

</head>
<body>
<%
int workplanId = ParamUtil.getInt(request, "id");
WorkPlanDb wpd = new WorkPlanDb();
wpd = wpd.getWorkPlanDb(workplanId);
if (!wpd.isLoaded()) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id")));
	return;
}
%>
<%@ include file="workplan_show_inc_menu_top.jsp"%>
<script>
o("menu10").className="current";
</script>
<div class="spacerH"></div>
<%
		WorkPlanTaskDb wptd = new WorkPlanTaskDb();
	
		String sql;
		String myname = privilege.getUser(request);
		String querystr = "";
		sql = "select id from work_plan_task where work_plan_id=" + workplanId + " and (startIsMilestone=1 or endIsMilestone=1)";
		sql += " order by " + orderBy + " " + sort;

		String urlStr = "op=" + op + "&userName=" + StrUtil.UrlEncode(userName) + "&action=" + action + "&what=" + StrUtil.UrlEncode(what) + "&beginDate=" + beginDate + "&endDate=" + endDate + "&progressFlag=" + progressFlag + "&id=" + ParamUtil.getInt(request, "id");
		
		querystr = urlStr + "&orderBy=" + orderBy + "&sort=" + sort;
		
		int pagesize = ParamUtil.getInt(request, "pageSize", 100);
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
					
		ListResult lr = wptd.listResult(sql, curpage, pagesize);
		long total = lr.getTotal();
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
<table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
        <tr>
          <td class="tabStyle_1_title" width="23" abbr="title">&nbsp;</td>
          <td class="tabStyle_1_title" width="20"></td>          
          <td class="tabStyle_1_title" width="36" abbr="ID">ID</td>          
          <td class="tabStyle_1_title" width="237" abbr="name">标题</td>		  
          <td class="tabStyle_1_title" width="82" abbr="progress">进度</td>
          <td class="tabStyle_1_title" width="50" abbr="progress">责任人</td>
          <td class="tabStyle_1_title" width="70" abbr="start_date">开始日期</td>
          <td class="tabStyle_1_title" width="70" abbr="end_date">结束日期</td>
          <td class="tabStyle_1_title" width="70" abbr="end_date">实际结束</td>
          <td class="tabStyle_1_title" width="65" abbr="duration">计划工作日</td>
          <td class="tabStyle_1_title" width="60">天数</td>
        </tr>
      <%	
	  	int i = 0;
		SelectOptionDb sod = new SelectOptionDb();
		WorkPlanAnnexDb wpad = new WorkPlanAnnexDb();
		UserMgr um = new UserMgr(); 
		com.redmoon.oa.workplan.Privilege workplanPvg = new com.redmoon.oa.workplan.Privilege();
		while (ir!=null && ir.hasNext()) {
			wptd = (WorkPlanTaskDb)ir.next();
			i++;
			String sbeginDate = DateUtil.format(wptd.getDate("start_date"), "yyyy-MM-dd");
			String sendDate = DateUtil.format(wptd.getDate("end_date"), "yyyy-MM-dd");
		%>
        <tr>
          <td align="center">
			<%
            int nowDays = DateUtil.datediff(wptd.getDate("end_date"), new Date());
            if(nowDays<0) {
                // nowDays = 0;
            }
            int sumDays = DateUtil.datediff(wptd.getDate("end_date"), wptd.getDate("start_date"));
            float progress =(float)nowDays/sumDays;

            float r23 = (float)2/3;
            if(progress>r23) {
            %>
                <img src="../images/green.jpg" width="16" height="18" border="0" title="时间大于2/3" />
            <%}else if(progress<r23 && progress>((float)1/3)){%>
                <img src="../images/yel.jpg" width="16" height="18" border="0" title="时间介于1/3与2/3之间" />
            <%}else if(progress<((float)1/3) && progress>=0){%>
                <img src="../images/red.jpg" width="16" height="18" border="0" title="时间小于1/3" />
            <%}else {%>
                <img src="../images/red_hot.jpg" width="16" height="18" border="0" title="时间超期" />
            <%}%>
          </td>
          <td align="center" style="align:center">
          <%
		  String clr = "#ffffff";
		  int status = wptd.getInt("status");
		  if (status==WorkPlanTaskDb.STATUS_ACTIVE)
		  	clr = "#0099FF";
		  else if (status==WorkPlanTaskDb.STATUS_DONE)
		  	clr = "#66FF99";
		  else if (status==WorkPlanTaskDb.STATUS_FAILED)
		  	clr = "#660066";
		  else if (status==WorkPlanTaskDb.STATUS_SUSPENDED)
		  	clr = "#fbb11e";
		  else if (status==WorkPlanTaskDb.STATUS_UNDEFINED)
		  	clr = "#ffffff"; 
		  %>
          <div title="<%=WorkPlanTaskDb.getStatusDesc(request, status)%>" style="width:6px; height:6px; background-color:<%=clr%>; border:1px solid #a0a0a0;"></div>
          </td>
          <td align="center"><%=wptd.getLong("id")%></td>
          <td>
		  <%
		  for (int k=0; k<wptd.getInt("task_level"); k++) {
		  	out.print("&nbsp;&nbsp;");
		  }
		  if (wptd.getInt("task_level")!=0) {
		  %>
          <img src='../images/i_plus-2-3.gif' align='absmiddle' />
          <%}%>
		  <%=wptd.getString("name")%></td>
          <td align="center">
		  <div class="progressBar" style="padding:0px; margin:0px; height:20px">
              <div class="progressBarFore" style="width:<%=wptd.getInt("progress")%>%;">
              </div>
              <div class="progressText">
              <%=wptd.getInt("progress")%>%
              </div>
          </div>          
          </td>
          <td align="center">
		  <%
		  if (!StrUtil.getNullStr(wptd.getString("task_resource")).equals("")) {
			  UserDb user = um.getUserDb(wptd.getString("task_resource"));
		  %>
		  <a href="javascript:;" onclick="addTab('消息', 'message_oa/message_frame.jsp?op=send&receiver=<%=StrUtil.UrlEncode(user.getName())%>')"><%=user.getRealName()%></a>
          <%}%>
          </td>
          <td align="left"><%=sbeginDate%>&nbsp;<%=wptd.getInt("startIsMilestone")==1?"<img title='里程碑' align='absmiddle' src='../images/workplan/milestone.png'>":""%></td>
          <td align="left"><%=sendDate%>&nbsp;<%=wptd.getInt("endIsMilestone")==1?"<img title='里程碑' align='absmiddle' src='../images/workplan/milestone.png'>":""%></td>
          <td align="center"><%=DateUtil.format(wpad.getRealCompleteDate(wptd.getLong("id")), "yyyy-MM-dd")%></td>
          <td align="center">
          <%=wptd.getInt("duration")%>
          </td>
          <td align="center">
            <%if (wptd.getInt("progress")<100) {
				if (nowDays<0) {%>
					<font color="red">过期<%=-nowDays%>天</font>
				<%} else {%>
					剩余<%=nowDays%>天
				<%}
			}%>
          </td>
        </tr>
      <%
		}
%>
</table>

<div id="taskDlg" style="display:none">
<form id="formAdd">
<table>
<tr>
<td width="96">标题</td>
<td width="290"><input type="text" name="name" id="name" class="formElements" style="width:200px" /></td>
</tr>
<tr>
  <td>上级任务</td>
  <td><input id="parentTaskId" name="parentTaskId" type="text" readonly class="formElements" />
  </td>
</tr>
<tr>
  <td>进度</td>
  <td><input type="text" name="progress" id="progress" value="0" size="3" class="formElements" />&nbsp;%
  </td>
</tr>
<tr>
  <td>开始日期</td>
  <td>
    <input readonly type="text" id="start_date" name="start_date" size="10">
    <script type="text/javascript">
        Calendar.setup({
            inputField     :    "start_date",      // id of the input field
            ifFormat       :    "%Y-%m-%d",       // format of the input field
            showsTime      :    false,            // will display a time selector
            singleClick    :    false,           // double-click mode
            align          :    "Tl",           // alignment (defaults to "Bl")		
            step           :    1                // show all years in drop-down boxes (instead of every other year as default)
        });
    </script>
    <input id="startIsMilestone" name="startIsMilestone" type="checkbox" value="1" />
    里程碑
  </td>
</tr>
<tr>
  <td>结束日期</td>
  <td>
    <input readonly type="text" id="end_date" name="end_date" size="10">
    <script type="text/javascript">
        Calendar.setup({
            inputField     :    "end_date",      // id of the input field
            ifFormat       :    "%Y-%m-%d",       // format of the input field
            showsTime      :    false,            // will display a time selector
            singleClick    :    false,           // double-click mode
            align          :    "Tl",           // alignment (defaults to "Bl")		
            step           :    1                // show all years in drop-down boxes (instead of every other year as default)
        });
    </script>
    <input id="endIsMilestone" name="endIsMilestone" type="checkbox" value="1" />
里程碑 </td>
</tr>
<tr>
  <td>关联计划 </td>
  <td><input type="text" name="workplan_related" id="workplan_related" value="" size="3" class="formElements" /></td>
</tr>
<tr>
  <td>任务状态</td>
  <td>
    <select id="status" name="status">
    <option value="<%=WorkPlanTaskDb.STATUS_ACTIVE%>">活动</option>
    <option value="<%=WorkPlanTaskDb.STATUS_DONE%>">完成</option>
    <option value="<%=WorkPlanTaskDb.STATUS_FAILED%>">失败</option>
    <option value="<%=WorkPlanTaskDb.STATUS_SUSPENDED%>">挂起</option>
    <option value="<%=WorkPlanTaskDb.STATUS_UNDEFINED%>">未定义</option>
    </select>  
  </td>
</tr>
<tr>
  <td>参与者</td>
  <td>
<select id="resource" name="resource">
<%
String[] principalAry = wpd.getPrincipals();
int len = principalAry==null?0:principalAry.length;
for (i=0; i<len; i++) {
  if (principalAry[i].equals(""))
      continue;
  UserDb user = um.getUserDb(principalAry[i]);
  %>
  <option value="<%=user.getName()%>"><%=user.getRealName()%></option>
  <%
}

String[] userAry = wpd.getUsers();
len = userAry==null?0:userAry.length;
for (i=0; i<len; i++) {
  if (userAry[i].equals(""))
      continue;
  // 过滤掉负责人
  boolean isFound = false;
  for (int j=0; j<principalAry.length; j++) {
      if (principalAry[j].equals(userAry[i])) {
          isFound = true;
          break;
      }
  }
  if (isFound)
      continue;
  UserDb user = um.getUserDb(userAry[i]);
  %>
  <option value="<%=user.getName()%>"><%=user.getRealName()%></option>
  <%
}
%>
  </select>
  <input id="work_plan_id" name="work_plan_id" value="<%=workplanId%>" type="hidden" />
  </td>
</tr>
</table>
</form>
</div>

<div id="dlg" style="display:none">
<form id="form1">
结果&nbsp;
<select id="assess" name="assess">
<%
SelectMgr sm = new SelectMgr();
SelectDb sd = sm.getSelect("workplan_assess");
Vector vsd = sd.getOptions();
Iterator irsd = vsd.iterator();
while (irsd.hasNext()) {
	sod = (SelectOptionDb)irsd.next();
	%>
	<option value="<%=sod.getValue()%>"><%=sod.getName()%></option>
	<%
}
%>        
</select>
</form>
</div>

</body>
</html>