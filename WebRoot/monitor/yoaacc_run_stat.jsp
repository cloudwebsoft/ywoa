<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@page import="com.redmoon.oa.flow.FormMgr"%>
<%@page import="com.redmoon.oa.flow.FormDb"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="com.redmoon.oa.monitor.RunStatusUtil"%>
<%@page import="cn.js.fan.util.DateUtil"%>
<%@page import="org.json.JSONObject"%>
<%
	RunStatusUtil statusUtil = new RunStatusUtil();
	
	String preDate = ParamUtil.get(request,RunStatusUtil.preDateParm);
	String beginDateS = ParamUtil.get(request,RunStatusUtil.beginDateParm);
	String endDateS = ParamUtil.get(request,RunStatusUtil.endDateParm);
	
	Map<String,Date> returnValue = statusUtil.getQueryDateCondition(request);
	Date startDate = returnValue.get(RunStatusUtil.beginDateParm);
	Date endDate = returnValue.get(RunStatusUtil.endDateParm);
	
	String formCode = ParamUtil.get(request, "formCode");
	int id = ParamUtil.getInt(request, "id");
	FormMgr fm = new FormMgr();
	FormDb fd = fm.getFormDb(formCode);
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(
			fd);
	com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(id);
	JSONObject json = statusUtil.getYOAStatus(fdao, startDate, endDate);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<title>云OA 使用情况</title>
		<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
		<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
		<style type="text/css">
@import url("../util/jscalendar/calendar-win2k-2.css");
</style>
		<script src="../inc/common.js"></script>
		<script type="text/javascript" src="../js/jquery.js"></script>
		<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
		<script type="text/javascript"
			src="../util/jscalendar/lang/calendar-zh.js"></script>
		<script type="text/javascript"
			src="../util/jscalendar/calendar-setup.js"></script>
		<style type="text/css">
		.tSearch {
			background: url(<%=SkinMgr.getSkinPath(request)%>/flexigrid/images/magnifier.png) no-repeat left;
			border:0px;
			padding-left:12px;
			cursor:pointer;
			margin-top:3px;
			height:16px;
		}
		</style>
	</head>

	<body>
		<%
			boolean errorValue = json.optBoolean(RunStatusUtil.error,true);
			if (errorValue) {
		%>
			<div><%=json.optString(RunStatusUtil.errorMessage,"") %></div>
	<%
			} else {
		%>
		<div class="spacerH"></div>
		<table id="searchTable" width="60%" border="0" cellspacing="1"
			cellpadding="3" align="center">
			<tr >
			<td height="23" align="left">
			<form id='formSearch' action="yoaacc_run_stat.jsp?parentId=<%=id%>&id=<%=id%>&formCode=<%=formCode%>" method="post">
				<select id="preDate" name="preDate"
					onchange="if (this.value=='*') o('dateSection').style.display=''; else o('dateSection').style.display='none'">
					<option selected="selected" value="">
						不限
					</option>
					<%
        java.util.Date[] aryDate = DateUtil.getDateSectOfToday();
        %>
					<option
						value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">
						今天
					</option>
					<%
        aryDate = DateUtil.getDateSectOfYestoday();
        %>
					<option
						value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">
						昨天
					</option>
					<%
        aryDate = DateUtil.getDateSectOfCurWeek();
        %>
					<option
						value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">
						本周
					</option>
					<%
        aryDate = DateUtil.getDateSectOfLastWeek();
        %>
					<option
						value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">
						上周
					</option>
					<%
        aryDate = DateUtil.getDateSectOfCurMonth();
        %>
					<option
						value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">
						本月
					</option>
					<%
        aryDate = DateUtil.getDateSectOfLastMonth();
        %>
					<option
						value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">
						上月
					</option>
					<%
        aryDate = DateUtil.getDateSectOfQuarter();
        %>
					<option
						value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">
						本季度
					</option>
					<%
        aryDate = DateUtil.getDateSectOfCurYear();
        %>
					<option
						value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">
						今年
					</option>
					<%
        aryDate = DateUtil.getDateSectOfLastYear();
        %>
					<option
						value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">
						去年
					</option>
					<%
        aryDate = DateUtil.getDateSectOfLastLastYear();
        %>
					<option
						value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">
						前年
					</option>
					<option value="*">
						自定义
					</option>
				</select>
				<script>
		        o("preDate").value = "<%=preDate%>";
		        </script>
				<span id="dateSection" style="display:<%=preDate.equals("*")?"":"none"%>"> 从 <input
						id="beginDate" name="beginDate" size="10" value="<%=beginDateS%>" /> <script
						type="text/javascript">
            Calendar.setup({
                inputField     :    "beginDate",      // id of the input field
                ifFormat       :    "%Y-%m-%d",       // format of the input field
                showsTime      :    false,            // will display a time selector
                singleClick    :    false,           // double-click mode
                align          :    "Tl",           // alignment (defaults to "Bl")		
                step           :    1                // show all years in drop-down boxes (instead of every other year as default)
            });
        </script> 至 <input id="endDate" name="endDate" size="10" value="<%=endDateS%>" /> <script
						type="text/javascript">
            Calendar.setup({
                inputField     :    "endDate",      // id of the input field
                ifFormat       :    "%Y-%m-%d",       // format of the input field
                showsTime      :    false,            // will display a time selector
                singleClick    :    false,           // double-click mode
                align          :    "Tl",           // alignment (defaults to "Bl")		
                step           :    1                // show all years in drop-down boxes (instead of every other year as default)
            });
        </script> </span>
				<input name="submit" type=submit value="查询" class="tSearch"/>
			</form>
		
			</td>
			</tr>
		</table>

		<div id="divContent" style="text-align: center;">
		<table class="tabStyle_1 percent60" border=0 cellSpacing=0 cellPadding=0>
			<tr>
				<td colspan="4" class="tabStyle_1_title">
					使用单位：<%=fdao.getFieldValue("used_unit") %>
					&nbsp;&nbsp;
					访问地址：<%=fdao.getFieldValue("access_path") %>
				</td>
			</tr>
			
			<tr>
				<td width="22%" align="right">
					登录次数：
				</td>
				<td width="28%"><%=json.optString(RunStatusUtil.loginCount,"-1")%></td>
				<td width="22%" align="right">
					网络硬盘文件数：
				</td>
				<td width="28%"><%=json.optString(RunStatusUtil.netDiskFileCount,"-1")%></td>
			</tr>
			<tr>
				<td align="right">
					磁盘份额：
				</td>
				<td><%=json.optString(RunStatusUtil.DiskSpace,"")%></td>
				<td align="right">
					已使用磁盘空间：
				</td>
				<td><%=json.optString(RunStatusUtil.DiskUseredSpace,"")%></td>
			</tr>
			
			<tr>
				<td align="right">
					网络硬盘份额：
				</td>
				<td><%=json.optString(RunStatusUtil.netDiskSpace,"")%></td>
				<td align="right">
					网络硬盘已使用空间：
				</td>
				<td><%=json.optString(RunStatusUtil.netDiskUserSpace,"")%></td>
			</tr>
			<tr>
				<td align="right">
					文件柜数：
				</td>
				<td><%=json.optString(RunStatusUtil.documentCount,"-1")%></td>
				<td align="right">
					流程数：
				</td>
				<td><%=json.optString(RunStatusUtil.flowCount,"-1")%></td>
			</tr>
			<tr>
				<td align="right">
					短消息数：
				</td>
				<td><%=json.optString(RunStatusUtil.messageCount,"-1")%></td>
				<td align="right">
					通知数：
				</td>
				<td><%=json.optString(RunStatusUtil.noticeCount,"-1")%></td>
			</tr>
			<tr>
				<td align="right">
					手机登录数：
				</td>
				<td><%=json.optString(RunStatusUtil.mobileAccessCount,"-1")%></td>
				<td align="right">
					用户数：
				</td>
				<td><%=json.optString(RunStatusUtil.userCount,"-1")%></td>
			</tr>
			<tr>
				<td align="right">
					论坛帖数：
				</td>
				<td><%=json.optString(RunStatusUtil.forumCount,"")%></td>
				<td align="right">
					公共共享文件数：
				</td>
				<td><%=json.optString(RunStatusUtil.shareFileCount,"")%></td>
			</tr>
			<tr>
				<td align="right">
					日程安排数：
				</td>
				<td><%=json.optString(RunStatusUtil.scheduleCount,"")%></td>
				<td align="right">
					项目数：
				</td>
				<td><%=json.optString(RunStatusUtil.projectCount,"")%></td>
			</tr>
			<tr>
				<td align="right">
					工作计划数：
				</td>
				<td><%=json.optString(RunStatusUtil.workPlanCount,"")%></td>
				<td align="right">
					工作报告数：
				</td>
				<td><%=json.optString(RunStatusUtil.workNoteCount,"")%></td>
			</tr>
			<tr>
				<td align="right">
					部门数：
				</td>
				<td><%=json.optString(RunStatusUtil.departmentCount,"")%></td>
				<td align="right">
					最后登录时间：
				</td>
				<td><%=json.optString(RunStatusUtil.lastLoginDate,"")%></td>
			</tr>
			<tr>
			  <td align="right">定位签到数：</td>
			  <td><%=json.optString(RunStatusUtil.locationCount,"")%></td>
			  <td align="right">活跃用户数：</td>
			  <td><%=json.optString(RunStatusUtil.activeUserCount,"")%></td>
		  </tr>
			<tr>
				<td colspan="4" align="center">
					<input type="button" class="btn" value=" 返 回 " onClick="window.history.go(-1);">
				</td>
			</tr>
		</table>
	</div>
		<%
			}
		%>
	</body>
</html>
