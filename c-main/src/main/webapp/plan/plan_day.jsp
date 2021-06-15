<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.io.File"%>
<%@ page import = "java.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "java.text.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="plan" scope="page" class="com.redmoon.oa.person.PlanMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>日程安排</title>
<meta http-equiv=Content-Type content="text/html; charset=utf-8" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="main.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script type="text/javascript" src="../js/jquery.toaster.js"></script>
<%
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = ParamUtil.get(request, "userName");
if (userName.equals("")) {
	userName = privilege.getUser(request);
}

if (!userName.equals(privilege.getUser(request))) {
	if (!(privilege.canAdminUser(request, userName))) {
		out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "pvg_invalid"),"提示"));
		return;
	}
}

boolean isShared = ParamUtil.getBoolean(request, "isShared", false);
%>
<style>
.menuskin{
position: absolute;
background-color: #f4f3f3;
background-repeat:repeat-y;
border:1px solid black;
font: normal 12px;
line-height: 22px;
z-index: 100;
visibility: hidden;
padding:5;
padding-left:10px;
padding-right:10px;
filter: alpha(opacity=90);
width:50px;
}
.menuskin a{
text-decoration: none;
padding-left: 6px;
color: black;
display: block;
padding-left:15px;
}

.divsty {
  margin-top: 5px;
  padding-top: 5px;
  padding-right: 5px;
  padding-bottom: 5px;
  padding-left: 5px;
  background-color:#C8E1FF;
  text-align:left;
}
</style>
</head>
<body>
<%@ include file="plan_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<script language="javascript" type="text/javascript">
var y;
var m;
var d;
function onTypeCodeChange(obj){
     y = obj.options[obj.options.selectedIndex].value;	
	 document.form1.y.value = y;
	 form1.submit();
}
function onTypeCodeChange1(obj){
     m = obj.options[obj.options.selectedIndex].value;	
	 document.form1.m.value = m;
	 form1.submit();
}
function onTypeCodeChange2(obj){
     d = obj.options[obj.options.selectedIndex].value;	
	 document.form1.d.value = d;
	 form1.submit();
}
function yearChange(obj,isChange){
	if(isChange == true){
		o("clickAction").value = "y-";
		form1.submit();
	}else{
		o("clickAction").value = "y+";
		form1.submit();
	}
}
function monthChange(obj,isChange){
	if(isChange == true){
		o("clickAction").value = "m-";
		form1.submit();
	}else{
		o("clickAction").value = "m+";
		form1.submit();
	}
}
function dayChange(obj,isChange){
	if(isChange == true){
		o("clickAction").value = "d-";
		form1.submit();
	}else{
		o("clickAction").value = "d+";
		form1.submit();
	}
}
</script>
<form name="form1" action="plan_day.jsp" method="get">
<%
	int y = 0;
	int m = 0;
	int d = 0;
	String showDate = ""; 
	showDate = ParamUtil.get(request,"showDate");
	if(showDate.length()!=0){
		java.util.Date dt = DateUtil.parse(showDate, "yyyy-MM-dd");
		if (dt==null) {
			out.print(SkinUtil.makeErrMsg(request, "格式非法！"));
			return;
		}
		y = DateUtil.getYear(dt);
		m = DateUtil.getMonth(dt) + 1;
		d = DateUtil.getDay(dt);
	}else{
		y = ParamUtil.getInt(request, "year", -1);
		m = ParamUtil.getInt(request, "month", -1);
		d = ParamUtil.getInt(request, "day", -1);
		
		String clickAction = ParamUtil.get(request, "clickAction");
		java.util.Date dt = DateUtil.getDate(y, m-1, d);
		if (clickAction.equals("y+"))
			y += 1;	
		else if (clickAction.equals("y-"))
			y -= 1;
		else if (clickAction.equals("m+")) {
			dt = DateUtil.addMonthDate(dt, 1);
			m = DateUtil.getMonth(dt) + 1;
			d = DateUtil.getDay(dt);
		}
		else if (clickAction.equals("m-")) {
			dt = DateUtil.addMonthDate(dt, -1);
			m = DateUtil.getMonth(dt) + 1;
			d = DateUtil.getDay(dt);
		}
		else if (clickAction.equals("d+")) {
			dt = DateUtil.addDate(dt, 1);
			m = DateUtil.getMonth(dt) + 1;
			d = DateUtil.getDay(dt);
		}
		else if (clickAction.equals("d-")) {
			dt = DateUtil.addDate(dt, -1);
			m = DateUtil.getMonth(dt) + 1;
			d = DateUtil.getDay(dt);
		}
	}
	String sj="";
	Calendar c1=Calendar.getInstance();
	int year = c1.get(Calendar.YEAR);
	int curY = year;
	int curM = c1.get(Calendar.MONTH)+1;
	int curD = c1.get(Calendar.DAY_OF_MONTH);
	if(y == -1){
	  y = year;
	}
	if(m == -1){
	  m=c1.get(Calendar.MONTH)+1;
	}
	if(d == -1){
	  d = c1.get(Calendar.DAY_OF_MONTH);
	}
	if(m < 10){
		if(d < 10 ){
			sj=y+"-0"+m+"-0"+d;
		}else{
			sj=y+"-0"+m+"-"+d;
		}
	}else{
		if(d < 10){
			sj=y+"-"+m+"-0"+d;
		}else{
			sj=y+"-"+m+"-"+d;
		}
	}
	
	java.util.Date dt = DateUtil.parse(sj, "yyyy-MM-dd");
	java.util.Date dt2 = DateUtil.addDate(dt, 1);
 %>
<input name="isShared" value="<%=isShared%>" type="hidden" />
<table width="98%" align="center" class="tabStyle_1 percent98" >
  <tr>
    <td width="17%" class="tabStyle_1_title"><input class="btn" onclick="window.location.href='plan_day.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>'" type="button" value="今天" /></td>
    <td class="tabStyle_1_title">
	<a href="javascript:;" onclick="yearChange(<%=y%>,true)"><img title="上一年" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="images/4.gif" /></a>
	&nbsp;
	<select id="year" name="year" onChange="onTypeCodeChange(this)" >	
	<%
	   c1=Calendar.getInstance();
	   for(int i=-15;i<15;i++){
		   int k = year + i;
	%>
		 <option value="<%=k%>"><%=k%>年</option>
	<%}%>
    </select> 
    <script>
	o("year").value = "<%=y%>";
	</script>
	&nbsp;
	<a href="javascript:;" onclick="yearChange(<%=y%>,false)"><img title="下一年" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="images/3.gif" /></a> 
	&nbsp;
	<a href="javascript:;" onclick="monthChange(<%=m%>,true)"><img title="上一月" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="images/4.gif" /></a> 
	<select id="month" name="month" onchange="onTypeCodeChange1(this)">
	<%
	for(int i=1;i<=12;i++){
	%>
	<option value="<%=i%>"><%=i%>月</option>
	<%}%>
	</select>
    <script>
	o("month").value = "<%=m%>";
	</script>
	&nbsp;
	<a href="javascript:;" onclick="monthChange(<%=m%>,false)"><img title="下一月" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="images/3.gif" /></a> 
    &nbsp;
    <a href="javascript:;" onclick="dayChange(<%=d%>,true)"><img title="上一日" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="images/4.gif" /></a>
	<%
	   int dayCount = DateUtil.getDayCount(y, m-1);
	%>
	 <select id="day" name="day" onchange="onTypeCodeChange2(this)">
     <% for(int j=1;j<=dayCount;j++){
			if(d == j){%>
				<option value="<%=j%>" selected="selected"><%=j%>日</option>
			<%}else{%>
				<option value="<%=j%>"><%=j%>日</option>
			<%}
	  }
	%>
	</select>
    <script>
	o("day").value = "<%=d%>";
	</script>
	&nbsp;
	<a href="javascript:;" onclick="dayChange(<%=d%>,false)"><img title="下一日" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="images/3.gif" /></a> 
	<input name="userName" value="<%=userName%>" type="hidden" />
	<input name="clickAction" type="hidden" />
	</td>
    <td width="17%" class="tabStyle_1_title" ><a style="color:#666; font-weight: normal" href="plan_day.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>"><img src="<%=SkinMgr.getSkinPath(request)%>/icons/OAimg28.gif" align="absmiddle" />&nbsp;<b style="color:#4e96f0">日</b></a>
	&nbsp;<a style="color:#666; font-weight: normal" href="plan.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>"><img src="<%=SkinMgr.getSkinPath(request)%>/icons/OAimg28.gif" align="absmiddle" />&nbsp;周</a>
	&nbsp;<a style="color:#666; font-weight: normal" href="plan_month.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>"><img src="<%=SkinMgr.getSkinPath(request)%>/icons/OAimg28.gif" align="absmiddle" />&nbsp;月</a>	
	</td>
  </tr>
</table>
<%
	String modify = "修改";
	String see = "查看";
	String mstr = "";
	// String sql = "select id,title,mydate,enddate from user_plan where ('"+sj+"' >=mydate and '"+sj+"' <=enddate) or (mydate like '"+sj+"%' or enddate like '"+sj+"%') order by mydate,enddate";
	String sql = "select id,title,mydate,enddate,is_closed,userName from user_plan where ((" + SQLFilter.getDateStr(sj, "yyyy-MM-dd") + "<=mydate and " + SQLFilter.getDateStr(DateUtil.format(dt2, "yyyy-MM-dd"), "yyyy-MM-dd") + ">enddate) or (mydate<=" + SQLFilter.getDateStr(sj, "yyyy-MM-dd") + " and enddate>" + SQLFilter.getDateStr(sj, "yyyy-MM-dd") + "))";
	if (isShared) {
		sql += " and is_shared=1";
	}
	else {
		sql += " and userName=" + StrUtil.sqlstr(userName);
	}
	sql += " order by mydate,enddate";
	// out.print(sql);
	JdbcTemplate rmconn = new JdbcTemplate();
	ResultIterator ri = rmconn.executeQuery(sql, 0, 0);
	ResultRecord rr = null;
	Vector hour_of_day = new Vector();
	Vector hour_of_hour = new Vector();
	Vector vid = new Vector();
	Vector overId = new Vector();
	Vector overDayTitle = new Vector();
	Vector vclosed = new Vector();
	int[] num = new int[24];
	UserDb ud = new UserDb();
	String temp = "";String title = "";
	String startDate = "",endDate = "",endHour = "",startDay = "",endDay = "";
	String startHour = "";
	while (ri.hasNext()) {
		rr = (ResultRecord)ri.next();
		int id = rr.getInt(1);
		title = rr.getString(2);
		
		startDate = DateUtil.format(rr.getDate(3), "yyyy-MM-dd HH:mm:ss");
		endDate = DateUtil.format(rr.getDate(4), "yyyy-MM-dd HH:mm:ss");
		if (endDate.equals(""))
			endDate = startDate;
	
		startHour = startDate.substring(11,13);
		endHour = endDate.substring(11,13);
		startDay = startDate.substring(8,10);
		endDay = endDate.substring(8,10);
		
		String uName = rr.getString(6);
		String tempHour = "";
		if(startDay.equals(endDay)){
			title = startDate.substring(10,16) + " ~ " +endDate.substring(10,16) + "&nbsp;&nbsp;"+ title + "<br/>";
			if (isShared) {
				ud = ud.getUserDb(uName);
				title = ud.getRealName() + "：" + title;
			}
			hour_of_day.add(title);
			hour_of_hour.add(startHour);
			vid.add(String.valueOf(id));
			vclosed.add("" + (rr.getInt(5)==1));
		}else{
			title = startDate.substring(0,16) + " ~ " +endDate.substring(0,16)+ "&nbsp;&nbsp;" + title + "<br/>";
			if (isShared) {
				ud = ud.getUserDb(uName);
				title = ud.getRealName() + "：" + title;
			}			
			overId.add(String.valueOf(id));
			overDayTitle.add(title);
		}
	}
%>
<table width="98%" align="center" class="tabStyle_1 percent98">
  <tr>
    <td class="tabStyle_1_title" width="10%">&nbsp;</td>
	<td class="tabStyle_1_title" style="text-align:center">
		<%
			String[] weekday = {"(星期一)","(星期二)","(星期三)","(星期四)","(星期五)","(星期六)","(星期日)"};
			int[] dateWeek = new int[7];
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); 
			c1=Calendar.getInstance();
			c1.setTime(df.parse(sj));
			int date = c1.get(Calendar.DAY_OF_WEEK)-1;
			for(int j = 0;j< dateWeek.length;j++){
				if((j+1)==date){%>
					<%=sj+weekday[j]%>
				<%}
			}
		%>
	</td>
  </tr>
  <tr>
    <td>跨天</td>
	<td>
	  <%
	  	int dayBegin = 0;
		int dayEnd = 0;
		int dayTemp = 0;
	  	String overDay = "";
	 	df = new SimpleDateFormat("yyyy-MM-dd"); 
		Calendar c = Calendar.getInstance();
		for(int j=0; j<overDayTitle.size(); j++){
			overDay = overDayTitle.get(j).toString();
			c.setTime(df.parse(overDay.substring(0,10)));
			dayBegin = c.get(Calendar.DAY_OF_YEAR);
			c.setTime(df.parse(overDay.substring(19,30)));
			dayEnd = c.get(Calendar.DAY_OF_YEAR);
			c.setTime(df.parse(sj));
			dayTemp = c.get(Calendar.DAY_OF_YEAR);
			mstr = "<a href='javascript:;' onclick='show(" + overId.get(j) + ")'>"+ see +"</a>";
			mstr += "<a href='javascript:;' onclick='edit(" + overId.get(j) + ")'>"+ modify +"</a>";						
			mstr += "<a onclick='del(" + overId.get(j) + ")' style='cursor:pointer;color:#666'>删除</a>";						
			if((dayBegin < dayTemp) && (dayEnd > dayTemp)){%>
				<div id="plan<%=overId.get(j) %>" class="divsty" style="float:left; clear:left; width:98%">
				<a href="plan_day.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>&year=<%=y%>&month=<%=m%>&day=<%=d-1%>">
				<img src="images/4.gif" style="float:left;onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" title="上一日" /></a>
				<a href='javascript:;' onMouseOver="showmenu(event, &quot;<%=mstr%>&quot;, 0)" style="float:left"><%=overDay%></a>
				<a href="plan_day.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>&year=<%=y%>&month=<%=m%>&day=<%=d+1%>" >
				<img src="images/3.gif" style="float:right;onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" title="下一日" /></a>
				</div>
			<%}else if(dayBegin == dayTemp){%>
				<div id="plan<%=overId.get(j) %>" class="divsty" style="clear:both; width:98%" >
				<div style="float:left">
				<a class="nav" href='javascript:;' onMouseOver="showmenu(event, &quot;<%=mstr%>&quot;, 0)">
				<%=overDay%></a>
				</div>
				<div style="float:left">
				<a href="plan_day.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>&year=<%=y%>&month=<%=m%>&day=<%=d+1%>" >
				<img src="images/3.gif" style=" float:right;onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" title="下一日"/></a>
				</div>
				</div>
			<%}else if(dayEnd == dayTemp){%>
				<div id="plan<%=overId.get(j) %>" class="divsty" style="float:left">
				<a href="plan_day.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>&year=<%=y%>&month=<%=m%>&day=<%=d-1%>" style="float:left">
				<img src="images/4.gif" title="上一日" style="float:left;onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" /></a>
				&nbsp;<a class="nav" href='javascript:;' onMouseOver="showmenu(event, &quot;<%=mstr%>&quot;, 0)">
				<%=overDay%></a>	
				</div>
			<%}	
	}%>
	</td>
  </tr>
<%
	for(int k=1;k<=23;k++){%>
	<tr>
		<td><%=k%>: 00</td>
		<td>
  	<%for(int j=0; j<hour_of_day.size(); j++) {
		String tempHour = hour_of_hour.get(j).toString();
		if(StrUtil.toInt(tempHour) == k){ 
			mstr = "<a href='javascript:;' onclick='show(" + vid.get(j).toString() + ")'>"+ see +"</a>";
			mstr += "<a href='javascript:;' onclick='edit(" + vid.get(j).toString() + ")'>"+ modify +"</a>";			
			mstr += "<a onclick='del(" + vid.get(j) + ")' style='cursor:pointer;color:#666'>删除</a>";						
			%>
			<div id="plan<%=vid.get(j) %>" class="<%=vclosed.get(j).equals("true")?"plan_closed":"plan_not_closed"%>" style="margin-bottom:2px"><a class="nav" href='javascript:;' onMouseOver="showmenu(event, &quot;<%=mstr%>&quot;, 0)"><%=hour_of_day.get(j).toString()%></a></div>
		<%}
	}%>
		</td>
	</tr>
<%  }
%>
</table>
<div class=menuskin id=popmenu onmouseover="clearhidemenu();highlightmenu(event,'on')" 
      onmouseout="highlightmenu(event,'off');dynamichide(event)" style="Z-index:100"></div>
</form>
</body>
<script>
function show(id) {
	addTab("查看日程", "plan/plan_show.jsp?id=" + id);
}
function edit(id) {
	addTab("修改日程", "plan/plan_edit.jsp?id=" + id);
}
function del(id) {
	jConfirm('确定要删除吗？','提示',function(r){
		if(!r){
			return;
		}
		$.ajax({
			type: "post",
			url: "../public/plan/delPlan.do",
			data: {
				id : id
			},
			dataType: "html",
			contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
			beforeSend: function(XMLHttpRequest) {
			},
			success: function(data, status) {
				data = $.parseJSON(data);
				if (data.ret=="1") {
					$('#plan' + id).remove();
				}
				$.toaster({
					"priority" : "info", 
					"message" : data.msg
				});
			},
			error: function(XMLHttpRequest, textStatus) {
				alert(XMLHttpRequest.responseText);
			}
		});			
	});
}
</script>
</html>