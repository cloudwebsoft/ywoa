<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.oacalendar.*" %>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ include file="../inc/nocache.jsp"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	Calendar c = Calendar.getInstance();
	int year = ParamUtil.getInt(request, "year", c.get(Calendar.YEAR));
	int month = ParamUtil.getInt(request, "month", c.get(c.MONTH));
	
	int monthDlt = ParamUtil.getInt(request, "monthDlt", 0);
	
	if (monthDlt!=0) {
		java.util.Date d = DateUtil.getDate(year, month, 1);
		Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.MONTH, monthDlt);
		d = cal.getTime();
		year = DateUtil.getYear(d);
		month = DateUtil.getMonth(d);
	}

	OACalendarDb oacdb = new OACalendarDb();
	int[][] monthlyCalendar = oacdb.initMonthlyCalendar(year, month);

	int id = ParamUtil.getInt(request, "id");
%>

<script>
function doShowCal(response){
	var rsp = response.responseText.trim();
	o("drag_<%=id%>").innerHTML = rsp;
}
var errFunc = function(response) {
	// alert('Error ' + response.status + ' - ' + response.statusText);
	alert(response.responseText);
}



</script>
<div id="drag_<%=id%>" class="portlet drag_div bor" >
<div id="drag_<%=id%>_h" class="box" style="padding:0px; _padding-top:5px;">
<%
String rootPath = request.getContextPath();
%>
<span class="titletxt"><a title="上一年" href="javascript:void(0);" onclick="showCal(-1, 0)"><img style="vertical-align:middle" src="<%=rootPath%>/images/first.png" width="15" height="15"/></a>&nbsp;<a title="上一月" href="javascript:void(0);" onclick="showCal(0, -1)"><img style="vertical-align:middle" src="<%=rootPath%>/images/previous.png" width="15" height="15"/></a>&nbsp;&nbsp;&nbsp;<a href="plan/plan.jsp"><%=year%>&nbsp;年&nbsp;<%=month + 1%>&nbsp;月</a>&nbsp;&nbsp;&nbsp;<a title="下一月" href="javascript:void(0);" onclick="showCal(0, 1)"><img style="vertical-align:middle" src="<%=rootPath%>/images/next.png" width="15" height="15"/></a>&nbsp;<a title="下一年" href="javascript:void(0);" onclick="showCal(1, 0)"><img style="vertical-align:middle" src="<%=rootPath%>/images/last.png" width="15" height="15"/></a></span>
 <!-- <div class="opbut-2"><img onclick="mini('<%=id%>')" title="最小化" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/minimization.png" align="absmiddle" /></div> -->
 <!-- <div class="opbut-3"><img onclick="clo('<%=id%>')" title="关闭" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/close.png" align="absmiddle" /></div> -->
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
					<td title="<%=s%>"><span class="<%=tdClass%>" onMouseOver="this.className='planOver <%=tdClass%>'" onMouseOut="this.className='<%=tdClass%>'"><a href="plan/plan.jsp?year=<%=year%>&month=<%=month+1%>&day=<%=monthlyCalendar[i][j]%>" style="color:#4d566e;"><span><%=monthlyCalendar[i][j]%></span></a></span></td>
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
</div>
<style>
#desktop_plan {
    /*min-height:120px;*/
    margin:0px 0px;
}

#desktop_plan ul {
    width:100%;
    /*height:120px;*/
    margin:0;
    padding:0;
    float:left;
    list-style:none;
}
#desktop_plan ul li {
    width:100%;
    /*height:16px;*/
    margin:0;
    padding:0;
    float:left;
}

#desktop_plan ul td {
    width:auto;
    text-align:center;
    cursor:pointer;
    padding-top:2px;
    padding-bottom:0px;
}
.planNormal{
    border:1px solid white;
    display:-moz-inline-box;
    display:inline-block;
    width:15px;
}
.planOver {
    background-color:#4286d3;
    display:-moz-inline-box;
    display:inline-block;
    width:30px;
    color:#FFF;
}
.planOver span { color:#FFF; }

.planCurrent {
    background-color:#6ec4f5;
    display:-moz-inline-box;
    display:inline-block;
    width:30px;
    color:#FFF;
}
.planCurrent span{color:#FFF;}
.planSome {
    border:1px solid #92C3F8;
    background-color:#DFEFFF;
    display:-moz-inline-box;
    display:inline-block;
    width:30px;
}
</style>
<script>
function adjust() {
	// IE8下需调整
	if (isIE8) {
		// 傲游或IE7中calTable的offsetHeight为0
		if (o("calTable").offsetHeight==0)
			;
		else
			o("desktop_plan").style.height = (o("calTable").offsetHeight + 10) + "px";
	}
}
var year = "<%=year%>";
var month = "<%=month%>";

function showCal(yearDlt, monthDlt) {
    try
    {
    jQuery.ajax({
        url: "<%=request.getContextPath()%>/plan/plan_desktop_ajax.jsp",
        type:"post",
        data: "id=<%=id%>&year=" + year + "&month=" + month + "&yearDlt=" + yearDlt + "&monthDlt=" + monthDlt,
        cache: false,
        success: function(html) {
            jQuery("#drag_<%=id%>").html(html);
            initDrag();
            adjust();
        }
    });
    }
    catch(e)
    {
        alert(e);
    }
   
}
adjust();
</script>
