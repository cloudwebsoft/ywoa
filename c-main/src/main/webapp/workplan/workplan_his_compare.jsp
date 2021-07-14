<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "org.json.*"%>
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

String[] ids = StrUtil.split(ParamUtil.get(request, "ids"), ",");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作计划历史对比</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>

<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

<script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>

<style>
.newTask {
	background-color:#D7EBFF;
}
.deletedTask {
	background-color:#FFEADF;
}
.differentTask {
	background-color:#FEFBB4;
}
</style>
</head>
<body>
<%
int workplanId = ParamUtil.getInt(request, "id");

com.redmoon.oa.workplan.Privilege wppvg = new com.redmoon.oa.workplan.Privilege();
boolean canSee = wppvg.canUserSeeWorkPlan(request, (int)workplanId);
if (!canSee) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

WorkPlanDb wpd = new WorkPlanDb();
wpd = wpd.getWorkPlanDb(workplanId);

if (!wpd.isLoaded()) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id")));
	return;
}

%>
<%@ include file="workplan_show_inc_menu_top.jsp"%>
<script>
o("menu7").className="current";
</script>
<div class="spacerH"></div>
<%
UserMgr um = new UserMgr();
WorkPlanLogDb wpld0 = new WorkPlanLogDb();
wpld0 = (WorkPlanLogDb)wpld0.getQObjectDb(new Long(StrUtil.toLong(ids[0])));
WorkPlanLogDb wpld1 = new WorkPlanLogDb();
wpld1 = (WorkPlanLogDb)wpld1.getQObjectDb(new Long(StrUtil.toLong(ids[1])));
%>
<table width="100%" border="0">
  <tr>
  <td>
  &nbsp;&nbsp;<%=um.getUserDb(wpld0.getString("user_name")).getRealName()%>&nbsp;&nbsp;<%=DateUtil.format(wpld0.getDate("create_date"), "yyyy-MM-dd HH:mm")%>
  </td>
  <td>
  &nbsp;&nbsp;<%=um.getUserDb(wpld1.getString("user_name")).getRealName()%>&nbsp;&nbsp;<%=DateUtil.format(wpld1.getDate("create_date"), "yyyy-MM-dd HH:mm")%>
  </td>
  </tr>
  <tr>
    <td width="50%" valign="top">
    <%
	SelectOptionDb sod = new SelectOptionDb();
	String data0 = wpld0.getString("gantt");
	JSONObject json0 = new JSONObject(data0);
	JSONArray jsonAry0 = (JSONArray) json0.get("tasks");
	
	String data1 = wpld1.getString("gantt");
	JSONObject json1 = new JSONObject(data1);
	JSONArray jsonAry1 = (JSONArray) json1.get("tasks");

	if (true) {%>
        <table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
          <thead>
                <tr>
                  <td class="tabStyle_1_title" width="15" style="cursor:pointer"></td>          
                  <td class="tabStyle_1_title" width="192" style="cursor:pointer" abbr="name">标题</td>		  
                  <td class="tabStyle_1_title" width="82" style="cursor:pointer" abbr="progress">进度</td>
                  <td class="tabStyle_1_title" width="50" style="cursor:pointer" abbr="progress">责任人</td>
                  <td class="tabStyle_1_title" width="95" style="cursor:pointer" abbr="start_date">开始日期</td>
                  <td class="tabStyle_1_title" width="95" style="cursor:pointer" abbr="end_date">结束日期</td>
                  <td class="tabStyle_1_title" width="60" style="cursor:pointer" abbr="duration">工作日</td>
                  <td class="tabStyle_1_title" width="35" style="cursor:pointer" abbr="assess">评价</td>
                  <td class="tabStyle_1_title" width="70" style="cursor:pointer">关联计划</td>
                </tr>
           </thead>
           <tbody>
             <%
                for (int i = 0; i < jsonAry0.length(); i++) {
                    JSONObject obj = (JSONObject) jsonAry0.get(i);
                    
                    String sbeginDate = "";
            		try {
            			sbeginDate = obj.getString("start");
            		} catch (JSONException e) {
            			sbeginDate = DateUtil.format(new Date(obj.getLong("start")), "yy-MM-dd");
            		}
            		String sendDate = "";
            		try {
            			sendDate = obj.getString("end");
            		} catch (JSONException e) {
            			sendDate = DateUtil.format(new Date(obj.getLong("end")), "yy-MM-dd");
            		}
					String cls = "";
					// 检查是否存在于之前的日志里
					boolean isFound = false;
					boolean isSame = true;
					for (int j=0; j<jsonAry1.length(); j++) {
						JSONObject obj1 = (JSONObject)jsonAry1.get(j);
						String cd = obj1.getString("code");
						if (cd.equals(obj.getString("code"))) {
							isFound = true;
							
							isSame = WorkPlanTaskMgr.compareTask(obj, obj1);
							break;
						}
					}
					if (!isFound)
						cls = " class='newTask'";
					else {
						if (!isSame)
							cls = " class='differentTask'";
					}
                %>
                <tr <%=cls%>>
                  <td align="center" style="align:center">
                  <%
                  String clr = "#ffffff";
                  int status = StrUtil.toInt(obj.getString("status"));
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
                  <td>
                  <%
                  int level = 0;
	          	  try {
	          	  	level = StrUtil.toInt(obj.getString("level"));
	          	  } catch (JSONException e) {
	          		level = obj.getInt("level");
	          	  }
                  for (int k=0; k<level; k++) {
                    out.print("&nbsp;&nbsp;");
                  }
                  if (level!=0) {
                  %>
                  <img src='../images/i_plus-2-3.gif' align='absmiddle' />
                  <%}%>
                  <%=obj.getString("name")%></td>
                  <td align="center">
                  <%
                  int progress = 0;
					try {
						progress = StrUtil.toInt(obj.getString("progress"));
					} catch (JSONException e) {
						progress = obj.getInt("progress");
					}
					boolean startIsMilestone = obj.getBoolean("startIsMilestone");
					boolean endIsMilestone = obj.getBoolean("endIsMilestone");
					int duration = obj.getInt("duration");
					int assess = 0;
					try {
						assess = StrUtil.toInt(obj.getString("assess"));
					} catch (JSONException e) {
						assess = obj.getInt("assess");
					}
                  %>
                  <div class="progressBar" style="padding:0px; margin:0px; height:20px">
                      <div class="progressBarFore" style="width:<%=progress%>%;">
                      </div>
                      <div class="progressText">
                      <%=progress%>%
                      </div>
                  </div>          
                  </td>
                  <td>
                  <%
                  if (!StrUtil.getNullStr(obj.getString("resource")).equals("")) {
                      UserDb user = um.getUserDb(obj.getString("resource"));
                  %>
                  <a href="javascript:;" onclick="addTab('消息', 'message_oa/message_frame.jsp?op=send&receiver=<%=StrUtil.UrlEncode(user.getName())%>')"><%=user.getRealName()%></a>
                  <%}%>
                  </td>
                  <td align="center"><%=sbeginDate%>&nbsp;<%=startIsMilestone?"<font color='red'>！</font>":""%></td>
                  <td align="center"><%=sendDate%>&nbsp;<%=endIsMilestone?"<font color='red'>！</font>":""%></td>
                  <td align="center">
                  <%=duration%>
                  </td>
                  <td>
                  <%=sod.getOptionName("workplan_assess", String.valueOf(assess))%>
                  </td>
                  <td align="center">
                  <%
                    if(!obj.getString("workplanRelated").equals("-1")){
                        WorkPlanDb wprel = wpd.getWorkPlanDb(StrUtil.toInt(obj.getString("workplanRelated")));
                        if (wprel.isLoaded()) {
                  %>
                            <a href="javascript:;" onclick="addTab('<%=wprel.getTitle()%>', 'workplan/workplan_show.jsp?id=<%=wprel.getId()%>')"><%=wprel.getTitle()%></a>
                  <%	}
                    }else{%>
                    无
                    <%}%>
                  </td>
             </tr>
              <%
                }
        %>
          </tbody>
        </table>
        &nbsp;&nbsp;备注：<%=wpld0.getString("remark")%>
        <%}%>
    </td>
    <td valign="top">
    <%	
	if (true) {%>
        <table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
          <thead>
                <tr>
                  <td class="tabStyle_1_title" width="15" style="cursor:pointer"></td>          
                  <td class="tabStyle_1_title" width="192" style="cursor:pointer" abbr="name">标题</td>		  
                  <td class="tabStyle_1_title" width="82" style="cursor:pointer" abbr="progress">进度</td>
                  <td class="tabStyle_1_title" width="50" style="cursor:pointer" abbr="progress">责任人</td>
                  <td class="tabStyle_1_title" width="95" style="cursor:pointer" abbr="start_date">开始日期</td>
                  <td class="tabStyle_1_title" width="95" style="cursor:pointer" abbr="end_date">结束日期</td>
                  <td class="tabStyle_1_title" width="60" style="cursor:pointer" abbr="duration">工作日</td>
                  <td class="tabStyle_1_title" width="35" style="cursor:pointer" abbr="assess">评价</td>
                  <td class="tabStyle_1_title" width="70" style="cursor:pointer">关联计划</td>
                </tr>
           </thead>
           <tbody>
             <%				
                for (int i = 0; i < jsonAry1.length(); i++) {
                    JSONObject obj = (JSONObject) jsonAry1.get(i);
                    
                    String sbeginDate = "";
            		try {
            			sbeginDate = obj.getString("start");
            		} catch (JSONException e) {
            			sbeginDate = DateUtil.format(new Date(obj.getLong("start")), "yy-MM-dd");
            		}
            		String sendDate = "";
            		try {
            			sendDate = obj.getString("end");
            		} catch (JSONException e) {
            			sendDate = DateUtil.format(new Date(obj.getLong("end")), "yy-MM-dd");
            		}
					
					// 检查是否存在于之后的日志里
					boolean isFound = false;
					for (int j=0; j<jsonAry0.length(); j++) {
						String cd = ((JSONObject)jsonAry0.get(j)).getString("code");
						if (cd.equals(obj.getString("code"))) {
							isFound = true;
							break;
						}
					}
					String cls = "";
					if (!isFound)
						cls = " class='deletedTask'";	
					
					int progress = 0;
					try {
						progress = StrUtil.toInt(obj.getString("progress"));
					} catch (JSONException e) {
						progress = obj.getInt("progress");
					}
					boolean startIsMilestone = obj.getBoolean("startIsMilestone");
					boolean endIsMilestone = obj.getBoolean("endIsMilestone");
					int duration = obj.getInt("duration");
					int assess = 0;
					try {
						assess = StrUtil.toInt(obj.getString("assess"));
					} catch (JSONException e) {
						assess = obj.getInt("assess");
					}
                %>
                <tr <%=cls%>>
                  <td align="center" style="align:center">
                  <%
                  String clr = "#ffffff";
                  int status = StrUtil.toInt(obj.getString("status"));
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
                  <td>
                  <%
                  int level = 0;
	          	  try {
	          	  	level = StrUtil.toInt(obj.getString("level"));
	          	  } catch (JSONException e) {
	          		level = obj.getInt("level");
	          	  }
                  for (int k=0; k<level; k++) {
                    out.print("&nbsp;&nbsp;");
                  }
                  if (level!=0) {
                  %>
                  <img src='../images/i_plus-2-3.gif' align='absmiddle' />
                  <%}%>
                  <%=obj.getString("name")%></td>
                  <td align="center">
                  <div class="progressBar" style="padding:0px; margin:0px; height:20px">
                      <div class="progressBarFore" style="width:<%=progress%>%;">
                      </div>
                      <div class="progressText">
                      <%=progress%>%
                      </div>
                  </div>          
                  </td>
                  <td>
                  <%
                  if (!StrUtil.getNullStr(obj.getString("resource")).equals("")) {
                      UserDb user = um.getUserDb(obj.getString("resource"));
                  %>
                  <a href="javascript:;" onclick="addTab('消息', 'message_oa/message_frame.jsp?op=send&receiver=<%=StrUtil.UrlEncode(user.getName())%>')"><%=user.getRealName()%></a>
                  <%}%>
                  </td>
                  <td align="center"><%=sbeginDate%>&nbsp;<%=startIsMilestone?"<font color='red'>！</font>":""%></td>
                  <td align="center"><%=sendDate%>&nbsp;<%=endIsMilestone?"<font color='red'>！</font>":""%></td>
                  <td align="center">
                  <%=duration%>
                  </td>
                  <td>
                  <%=sod.getOptionName("workplan_assess", String.valueOf(assess))%>
                  </td>
                  <td align="center">
                  <%
                    if(!obj.getString("workplanRelated").equals("-1")){
                        WorkPlanDb wprel = wpd.getWorkPlanDb(StrUtil.toInt(obj.getString("workplanRelated")));
                        if (wprel.isLoaded()) {
                  %>
                            <a href="javascript:;" onclick="addTab('<%=wprel.getTitle()%>', 'workplan/workplan_show.jsp?id=<%=wprel.getId()%>')"><%=wprel.getTitle()%></a>
                  <%	}
                    }else{%>
                    无
                    <%}%>
                  </td>
             </tr>
              <%
                }
        %>
          </tbody>
        </table>
        &nbsp;&nbsp;备注：<%=wpld1.getString("remark")%>        
    <%}%>
    </td>
  </tr>
  <tr>
    <td colspan="2" valign="top">
      &nbsp;&nbsp;说明：<span class="newTask">&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;新增&nbsp;&nbsp;<span class="deletedTask">&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;删除&nbsp;&nbsp;<span class="differentTask">&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;不同
    </td>
  </tr>
</table>

</body>
</html>