<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@page import="com.redmoon.oa.flow.FormMgr"%>
<%@page import="com.redmoon.oa.flow.FormDb"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="com.redmoon.oa.monitor.RunStatusUtil"%>
<%@page import="cn.js.fan.util.DateUtil"%>
<%@page import="org.json.JSONObject"%>
<jsp:useBean id="myconfig" scope="page" class="com.redmoon.oa.Config"/>
<%
	RunStatusUtil statusUtil = new RunStatusUtil();
	
	String preDate = ParamUtil.get(request,RunStatusUtil.preDateParm);
	String beginDateS = ParamUtil.get(request,RunStatusUtil.beginDateParm);
	String endDateS = ParamUtil.get(request,RunStatusUtil.endDateParm);
	
	Map<String,Date> returnValue = statusUtil.getQueryDateCondition(request);
	Date startDate = returnValue.get(RunStatusUtil.beginDateParm);
	Date endDate = returnValue.get(RunStatusUtil.endDateParm);
	
	String enterprise = myconfig.get("enterprise");
	
	Map<String,Object> map = statusUtil.statUsedStatusToMap(startDate, endDate, "admin");
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<title>OA 使用情况</title>
		<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
		<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
		<script src="../inc/common.js"></script>
		<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
		<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
		<script src="../js/datepicker/jquery.datetimepicker.js"></script>
		<style type="text/css">
		.tSearch {
			background: url(<%=SkinMgr.getSkinPath(request)%>/flexigrid/images/magnifier.png) no-repeat left;
			border:0px;
			padding-left:12px;
			cursor:pointer;
			height:20px;
		}
		</style>
	</head>

	<body>
		<table cellSpacing="0" cellPadding="0" width="100%">
		  <tbody>
		    <tr>
		      <td class="tdStyle_1">使用统计</td>
	        </tr>
	      </tbody>
    </table>
		<%
			boolean errorValue = (Boolean)map.get(RunStatusUtil.error);
			if (errorValue) {
		%>
			<div><%=map.get(RunStatusUtil.errorMessage).toString() %></div>
<%
			} else {
		%>
		<div class="spacerH"></div>
		<table id="searchTable" width="80%" border="0" cellspacing="1"
			cellpadding="3" align="center">
			<tr >
			<td height="30" align="left">
			<form id='formSearch' action="oa_run_stat.jsp" method="post">
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
				<span id="dateSection" height="25" style="display:<%=preDate.equals("*")?"":"none"%>"> 从 
				<input id="beginDate" name="beginDate" size="15" height="25" value="<%=beginDateS%>" /></span>
				<input name="submit" type=submit value="查询" class="tSearch"/>
			</form>
			</td>
			</tr>
			</table>
		<div id="divContent" style="text-align: center;">
		<table class="tabStyle_1 percent80" border=0 cellSpacing=0 cellPadding=0>
			<tr>
				<td colspan="4" class="tabStyle_1_title"><%=enterprise%>
				</td>
			</tr>
			<tr>
			
			</tr>
			<tr>
				<td width="22%" align="right">
					登录次数：
				</td>
				<td width="28%"><%=map.get(RunStatusUtil.loginCount)%></td>
				<td width="22%" align="right">
					网络硬盘文件数：
				</td>
				<td width="28%"><%=map.get(RunStatusUtil.netDiskFileCount)%></td>
			</tr>
			<tr>
				<td align="right">
					磁盘份额：
				</td>
				<td><%=map.get(RunStatusUtil.DiskSpace)%></td>
				<td align="right">
					已使用磁盘空间：
				</td>
				<td><%=map.get(RunStatusUtil.DiskUseredSpace)%></td>
			</tr>
			<tr>
				<td align="right">
					云盘份额：
				</td>
				<td><%=map.get(RunStatusUtil.netDiskSpace)%></td>
				<td align="right">
					已使用云盘空间：
				</td>
				<td><%=map.get(RunStatusUtil.netDiskUserSpace)%></td>
			</tr>
			<tr>
				<td align="right">
					文件柜数：
				</td>
				<td><%=map.get(RunStatusUtil.documentCount)%></td>
				<td align="right">
					流程数：
				</td>
				<td><%=map.get(RunStatusUtil.flowCount)%></td>
			</tr>
			<tr>
				<td align="right">
					短消息数：
				</td>
				<td><%=map.get(RunStatusUtil.messageCount)%></td>
				<td align="right">
					通知数：
				</td>
				<td><%=map.get(RunStatusUtil.noticeCount)%></td>
			</tr>
			<tr>
				<td align="right">
					手机登录数：
				</td>
				<td><%=map.get(RunStatusUtil.mobileAccessCount)%></td>
				<td align="right">
					用户数：
				</td>
				<td><%=map.get(RunStatusUtil.userCount)%></td>
			</tr>
			<tr>
				<td align="right">
					论坛帖数：
				</td>
				<td><%=map.get(RunStatusUtil.forumCount)%></td>
				<td align="right">
					公共共享文件数：
				</td>
				<td><%=map.get(RunStatusUtil.shareFileCount)%></td>
			</tr>
			<tr>
				<td align="right">
					日程安排数：
				</td>
				<td><%=map.get(RunStatusUtil.scheduleCount)%></td>
				<td align="right">
					项目数：
				</td>
				<td><%=map.get(RunStatusUtil.projectCount)%></td>
			</tr>
			<tr>
				<td align="right">
					工作计划数：
				</td>
				<td><%=map.get(RunStatusUtil.workPlanCount)%></td>
				<td align="right">
					工作报告数：
				</td>
				<td><%=map.get(RunStatusUtil.workNoteCount)%></td>
			</tr>
			<tr>
				<td align="right">
					部门数：
				</td>
				<td><%=map.get(RunStatusUtil.departmentCount)%></td>
				<td align="right">
					最后登录时间：
				</td>
				<td><%=DateUtil.format((Date)map.get(RunStatusUtil.lastLoginDate),RunStatusUtil.dateTimeFormatString) %></td>
			</tr>
		  </table>
	</div>
		<%
			}
		%>
	</body>
<script type="text/javascript">
$(function() {
	o("preDate").value = "<%=preDate%>";
	$('#beginDate').datetimepicker({
     	lang:'ch',
     	timepicker:false,
     	format:'Y-m-d',
     	formatDate:'Y/m/d'
	});
	$('#endDate').datetimepicker({
		lang:'ch',
		timepicker:false,
        format:'Y-m-d',
		formatDate:'Y/m/d'
	});
});

</script>
</html>
