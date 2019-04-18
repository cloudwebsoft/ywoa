<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.oacalendar.*" %>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	Calendar c = Calendar.getInstance();
	int year = ParamUtil.getInt(request, "year", c.get(Calendar.YEAR));
	int month = ParamUtil.getInt(request, "month", c.get(c.MONTH));
	
	int yearDlt = ParamUtil.getInt(request, "yearDlt", 0);
	int monthDlt = ParamUtil.getInt(request, "monthDlt", 0);
	
	if (yearDlt!=0) {
		java.util.Date d = DateUtil.getDate(year, month, 1);
		Calendar cal = Calendar.getInstance();		
        cal.setTime(d);
        cal.add(Calendar.YEAR, yearDlt);
		d = cal.getTime();
		year = DateUtil.getYear(d);
		month = DateUtil.getMonth(d);
	}	
	if (monthDlt!=0) {
		java.util.Date d = DateUtil.getDate(year, month, 1);
		Calendar cal = Calendar.getInstance();		
        cal.setTime(d);
        cal.add(Calendar.MONTH, monthDlt);
		d = cal.getTime();
		year = DateUtil.getYear(d);
		month = DateUtil.getMonth(d);
	}
	
	%>
	<script>
	year = "<%=year%>";
	month = "<%=month%>";
	</script>
	<%
	
	OACalendarDb oacdb = new OACalendarDb();
	int[][] monthlyCalendar = oacdb.initMonthlyCalendar(year, month);
	
	int id = ParamUtil.getInt(request, "id", -1);
	if (id==-1) {
		out.print(SkinUtil.makeErrMsg(request, "标识非法！"));
		return;
	}
%>
<div id="drag_<%=id%>_h" class="box" style="padding:0px; _padding-top:5px;">
<%
String rootPath = request.getContextPath();
%>
<span class="titletxt"><a title="上一年" href="javascript:void(0);" onclick="showCal(-1, 0)"><img style="vertical-align:middle" src="<%=rootPath%>/images/first.png" width="15" height="15"/></a>&nbsp;<a title="上一月" href="javascript:void(0);" onclick="showCal(0, -1)"><img style="vertical-align:middle" src="<%=rootPath%>/images/previous.png" width="15" height="15"/></a>&nbsp;&nbsp;&nbsp;<a href="plan/plan.jsp"><%=year%>&nbsp;年&nbsp;<%=month + 1%>&nbsp;月</a>&nbsp;&nbsp;&nbsp;<a title="下一月" href="javascript:void(0);" onclick="showCal(0, 1)"><img style="vertical-align:middle" src="<%=rootPath%>/images/next.png" width="15" height="15"/></a>&nbsp;<a title="下一年" href="javascript:void(0);" onclick="showCal(1, 0)"><img style="vertical-align:middle" src="<%=rootPath%>/images/last.png" width="15" height="15"/></a></span>
 <!--<div class="opbut-2"><img onclick="mini('<%=id%>')" title="最小化" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/minimization.png" align="absmiddle" /></div>-->
 <!--<div class="opbut-3"><img onclick="clo('<%=id%>')" title="关闭" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/close.png" align="absmiddle" /></div>-->
</div>


<div id="drag_<%=id%>_c">
<div id="desktop_plan">
	<ul>
		<li>
			<table id="calTable" width="100%" height="190px" border="0" cellpadding="0" cellspacing="0" style="margin-top:5px;">
				<tr>
					<td style="width:15%;color:#0093e9;font-size:12px;font-family:Arial,Helvetica,sans-serif;">Sun</td>
					<td style="width:14%;color:#0093e9;font-size:12px;font-family:Arial,Helvetica,sans-serif;">Mon</td>
					<td style="width:14%;color:#0093e9;font-size:12px;font-family:Arial,Helvetica,sans-serif;">Tue</td>
					<td style="width:14%;color:#0093e9;font-size:12px;font-family:Arial,Helvetica,sans-serif;">Wed</td>
					<td style="width:14%;color:#0093e9;font-size:12px;font-family:Arial,Helvetica,sans-serif;">Thu</td>
					<td style="width:14%;color:#0093e9;font-size:12px;font-family:Arial,Helvetica,sans-serif;">Fri</td>
					<td style="width:15%;color:#0093e9;font-size:12px;font-family:Arial,Helvetica,sans-serif;">Sat</td>
				</tr>
<%
	for(int i=0; i<monthlyCalendar.length; i++) {
%>
				<tr>
<%
		for (int j = 0; j < monthlyCalendar[i].length; j++) {
			if (monthlyCalendar[i][j] == -1) {
%>
					<td>&nbsp;</td>
<%
			} else {
				Calendar cal = Calendar.getInstance();
				cal.set(year, month, monthlyCalendar[i][j], 0, 0, 0);
				java.util.Date d1 = cal.getTime();
				boolean flag = false;
				if(DateUtil.format(d1, "yyyy-MM-dd").equals(DateUtil.format(c.getTime(), "yyyy-MM-dd"))) {
					flag = true;
				}
				String tdClass = "planNormal";
				if(flag) {
					tdClass = "planCurrent";
				}
				cal.set(year, month, monthlyCalendar[i][j], 23, 59, 59);
				java.util.Date d2 = cal.getTime();
				PlanDb pd = new PlanDb();
				String sql = "select id from user_plan where userName=" + StrUtil.sqlstr(privilege.getUser(request)) + " and mydate>=" + SQLFilter.getDateStr(DateUtil.format(d1, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss") + " and mydate<=" + SQLFilter.getDateStr(DateUtil.format(d2, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss") + "order by myDate";
				Vector v =  pd.list(sql);
				if(!v.isEmpty()) {
					tdClass = "planSome";
				}
				Iterator ii = v.iterator();
				String s = "";
				while(ii.hasNext()) {
					pd = (PlanDb) ii.next();
					s += DateUtil.format(pd.getMyDate(), "HH:mm:ss") + " " + StrUtil.getNullStr(pd.getTitle());
					if(ii.hasNext()) {
						s += "\r\n";
					}
				}
%>
					<td title="<%=s%>"><span class="<%=tdClass%>" onMouseOver="this.className='planOver <%=tdClass%>'" onMouseOut="this.className='<%=tdClass%>'"><a href="plan/plan.jsp?year=<%=year%>&month=<%=month+1%>&day=<%=monthlyCalendar[i][j]%>" style="color:#4d566e"><%=monthlyCalendar[i][j]%></a></span></td>
<%
			}
		}
%>					
				</tr>
<%
	}
%>
		  </table>
		</li>
	</ul>
</div>
</div>