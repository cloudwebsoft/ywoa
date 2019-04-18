<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.io.File"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "java.text.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
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
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
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
.tabStyle_1_title a {color:#000}
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
padding-left:15px;
filter: alpha(opacity=90);
width:40px;
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
<script language="javascript" type="text/javascript">
var y;
var w;
function onTypeCodeChange(obj){
     y = obj.options[obj.options.selectedIndex].value;	
	 document.form1.y.value = y;
	 form1.submit();
}
function onTypeCodeChange1(obj){
     w = obj.options[obj.options.selectedIndex].value;	
	 document.form1.w.value = w;
	 form1.submit();
}
function yearChange(obj,isChange){
	y = document.form1.year.value;
	if(isChange == true){
		y--;
		document.form1.year.value = y ;
		form1.submit();
	}else{
		y++;
		document.form1.year.value = y ;
		form1.submit();
	}
}
function weekChange(obj,isChange){
	w = document.form1.week.value;
	if(isChange == true){
		w--;
		document.form1.week.value = w ;
		form1.submit();
	}else{
		w++;
		document.form1.week.value = w ;
		form1.submit();
	}
}
</script>
</HEAD>
<BODY>
<%@ include file="plan_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<form name="form1" action="plan.jsp" >
<table width="98%" align="center" class="tabStyle_1 percent98">
  <tr>
    <td width="17%" class="tabStyle_1_title"><input class="btn" type="button" value="今天" onclick="window.location.href = 'plan_day.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>'" /></td>
	<%
	int y = ParamUtil.getInt(request, "year", -1);
	int m = ParamUtil.getInt(request, "month", -1);
	int d = ParamUtil.getInt(request, "day", -1);
	
	int w = ParamUtil.getInt(request, "week", -1);
	
	Calendar cc = Calendar.getInstance();
	int year = cc.get(Calendar.YEAR);
	if(y == -1){
		y = year;
	}
	else {
		if (m!=-1 && d!=-1) {
			cc.setTime(DateUtil.getDate(year, m-1, d));
		}
	}
	
	cc.setFirstDayOfWeek(Calendar.MONDAY);
	
	if(w == -1){
		// w = cc.getActualMaximum(Calendar.WEEK_OF_YEAR);
		w = cc.get(Calendar.WEEK_OF_YEAR);
		if (w==1) {
			cc.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(y+"-12-24"));	
			w = cc.get(Calendar.WEEK_OF_YEAR);
		}
	}
%>
    <td class="tabStyle_1_title" style="text-align:center">
	<input type="hidden" name="y" value="<%=y%>" />
	<input type="hidden" name="w" value="<%=w%>" />
    <input name="isShared" value="<%=isShared%>" type="hidden" />    
	<a href="#" onclick="yearChange(<%=year%>,true)">
	<img title="上一年" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="images/1.gif" /></a>
	&nbsp;
	<a href="#" onclick="weekChange(<%=w%>,true)"><img title="上一周" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="images/4.gif" /></a>
	&nbsp;
	<span class="tabStyle_1_title" style="text-align:center">
	<%
	  Calendar c = Calendar.getInstance();
	  int y1 = y;
	  c.setTime(new SimpleDateFormat("yyyy/MM/dd").parse(y+"/12/31"));
	  c.setFirstDayOfWeek(Calendar.MONDAY);
	  c.setMinimalDaysInFirstWeek(7);
	  int week1 = c.get(Calendar.WEEK_OF_YEAR);
	  
		if (week1==1) {
			c.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(y+"-12-24"));	
			week1 = c.get(Calendar.WEEK_OF_YEAR);
		}
		  
	  //out.print(week1+"====="+y);
	  //out.print(w);
	%>
	</span>
	<select name="year" onchange="onTypeCodeChange(this)" >	
	<%
	  //out.print(y);
	  year += 1;
	  Calendar c1=Calendar.getInstance();
	  for(int i=0;i<30;i++){
	     if(y == year){
		 %>
		 <option value="<%=year%>" selected="selected"><%=year%></option>
	<%}else{%>
		 <option value="<%=year%>"><%=year%></option>
	<%
	   	}
		year--;
	   }
	%>
    </select>
	<select name="week" onchange="onTypeCodeChange1(this)">
	  <%for(int i=1;i<=(week1+1);i++) {
	     if(i == w) {
	  %>
	    <option value="<%=i%>" selected="selected"> 第<%=i%>周</option>
	  <% }else{%>
	  	<option value="<%=i%>"> 第<%=i%>周</option>
	  <%
	     }
	  }
	  %>
	</select>
	&nbsp;
	<a href="#" onclick="weekChange(<%=w%>,false)"><img title="下一周" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="images/3.gif" /></a>
	&nbsp;
	<a href="#" onclick="yearChange(<%=year%>,false)"><img title="下一年" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="images/2.gif" /></a>
	<input name="userName" value="<%=userName%>" type="hidden" />
    </td>
    <td width="17%" class="tabStyle_1_title"><a style="color:#666; font-weight: normal" href="plan_day.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>"><img src="<%=SkinMgr.getSkinPath(request)%>/icons/OAimg28.gif" align="absmiddle" />&nbsp;日</a>
	&nbsp;<a style="color:#666; font-weight: normal" href="plan.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>"><img src="<%=SkinMgr.getSkinPath(request)%>/icons/OAimg28.gif" align="absmiddle" />&nbsp;<b style="color:#4e96f0">周</b></a>
	&nbsp;<a style="color:#666; font-weight: normal" href="plan_month.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>"><img src="<%=SkinMgr.getSkinPath(request)%>/icons/OAimg28.gif" align="absmiddle" />&nbsp;月</a>	</td>
  </tr>
</table>
<%
	int yy = y;
	int ww = w;
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");   
	Calendar cal=Calendar.getInstance(); 
	cal.setFirstDayOfWeek(Calendar.MONDAY);
  	cal.set(Calendar.YEAR,yy); 
  	cal.set(Calendar.WEEK_OF_YEAR,ww); 
  	cal.set(Calendar.DAY_OF_WEEK,   Calendar.MONDAY); 
	String[] weekday = {"(星期二)","(星期三)","(星期四)","(星期五)","(星期六)","(星期日)"};
	String[] date_week = new String[7];
	date_week[0] = format.format(cal.getTime());
%>
<table align="center" class="tabStyle_1 percent98">
 <tr>
 <td class="tabStyle_1_title" width="7%">0-23点</td>
 <td class="tabStyle_1_title" width="13%"><a href="plan_day.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>&showDate=<%=date_week[0]%>">
<%out.print(format.format(cal.getTime()));%>
<br />
(星期一)</a></td>
 <%for(int i=1;i<7;i++){
     long temp = cal.getTimeInMillis()+24*60*60*1000;
     cal.setTimeInMillis(temp); 
	 date_week[i] = format.format(cal.getTime());
  %> 
	<td class="tabStyle_1_title" width="13%">
	<a href="plan_day.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>&showDate=<%=date_week[i]%>"><% out.print(date_week[i] + "<BR>" + weekday[i-1]);%></a></td>
	<%}%>
 </tr>
  <%
    String date1 = date_week[0] + " 00:00:00";
	String date2 = date_week[6] + " 23:59:59";
    // String sql = "select id from user_plan where userName=" + StrUtil.sqlstr(userName) + " and ((mydate>="+SQLFilter.getDateStr(date1, "yyyy-MM-dd HH:mm:ss")+" and mydate<="+SQLFilter.getDateStr(date2,"yyyy-MM-dd HH:mm:ss")+ ") or (enddate>="+SQLFilter.getDateStr(date1, "yyyy-MM-dd HH:mm:ss")+" and enddate<="+SQLFilter.getDateStr(date2,"yyyy-MM-dd HH:mm:ss")+") or (mydate<="+SQLFilter.getDateStr(date1, "yyyy-MM-dd HH:mm:ss")+" and enddate>="+SQLFilter.getDateStr(date2,"yyyy-MM-dd HH:mm:ss") +")) order by mydate,enddate" ;
    String sql = "select id from user_plan where ((mydate>="+SQLFilter.getDateStr(date1, "yyyy-MM-dd HH:mm:ss")+" and enddate<="+SQLFilter.getDateStr(date2,"yyyy-MM-dd HH:mm:ss")+ ") or (mydate<="+SQLFilter.getDateStr(date1, "yyyy-MM-dd HH:mm:ss")+" and enddate>="+SQLFilter.getDateStr(date1,"yyyy-MM-dd HH:mm:ss")+") or (mydate<="+SQLFilter.getDateStr(date2, "yyyy-MM-dd HH:mm:ss")+" and enddate>="+SQLFilter.getDateStr(date2,"yyyy-MM-dd HH:mm:ss")+") or (mydate<="+SQLFilter.getDateStr(date1, "yyyy-MM-dd HH:mm:ss")+" and enddate>="+SQLFilter.getDateStr(date2,"yyyy-MM-dd HH:mm:ss") +"))";
	if (isShared) {
		sql += " and is_shared=1";
	}
	else {
		sql += " and userName=" + StrUtil.sqlstr(userName);
	}
	sql += " order by mydate,enddate" ;
	
	// out.println(getClass() + " " + sql);
	
	PlanDb pd = new PlanDb();
	Vector v = pd.list(sql);
	Iterator ir = null;
	if(v!=null)
		ir = v.iterator(); 
	int[] dayofweek = new int[32];
	String[][] hour_day = new String[32][24];
	for(int i=0;i<32;i++){
		for(int j=0;j<24;j++){
			hour_day[i][j]="";
		}
	}
	v = new Vector();
	String title ="";
	String startDate = "";
	String endDate = "";
	String startDay = "";
	String endDay = "";
	String startHour = "";
	String startMonth = "";
	String endMonth = "";
	UserDb ud = new UserDb();	
	Vector content = new Vector();
	Vector hour_of_day = new Vector();
	Vector vid = new Vector();
	// 记录日期
	Vector date = new Vector();
	Vector overId = new Vector();
	Vector vclosed = new Vector();
	while (ir!=null && ir.hasNext()) {
	   pd = (PlanDb)ir.next();
	   int id = pd.getId();
	   title = pd.getTitle();   
	   startDate = DateUtil.format(pd.getMyDate(), "yyyy-MM-dd HH:mm:ss");
	   endDate = DateUtil.format(pd.getEndDate(), "yyyy-MM-dd HH:mm:ss");
	   startDay = startDate.substring(8,10);
	   if (!endDate.equals("")) {
		   endDay = endDate.substring(8,10);
		   endMonth = endDate.substring(5,7);
	   }
	   startMonth = startDate.substring(5,7);
	   startHour = startDate.substring(11,13);
	   // 如果日程的开始与结束日期是在同一天
	   if(startDay.equals(endDay) && startMonth.equals(endMonth)){
	   		title = startDate.substring(10,16) + " ~ " + endDate.substring(10,16) + "<br />" + title + "<br />";
		   	if (isShared) {
			  ud = ud.getUserDb(pd.getUserName());
			  title = ud.getRealName() + "：" + title;
		   	}				
			content.add(title);
			// 记录起始小时
			hour_of_day.add(startHour);
			vid.add(String.valueOf(id));
			date.add(startDay);
			vclosed.add(new Boolean(pd.isClosed()));
	   }else{
	   		title = startDate.substring(0,16) + " ~ " + endDate.substring(0,16) + "&nbsp;&nbsp;" +title + "<br />";			
			overId.add(String.valueOf(id));
	   		v.add(title);
	   }   
	}

	int size = hour_of_day.size();
	int[] sortHour = new int[size];
	String[] sortDay = new String[size];
	String[] sortTitle = new String[size]; 
	String[] sortId = new String[size];
	String[] sortClosed = new String[size];
	int sortTemp = 0;
	String sort = "";
	for(int i=0;i<size;i++){
		sortHour[i] = StrUtil.toInt(hour_of_day.get(i).toString());
		sortDay[i] = date.elementAt(i).toString();
		sortTitle[i] = content.elementAt(i).toString();
		sortId[i] = vid.elementAt(i).toString();
		sortClosed[i] = vclosed.elementAt(i).toString();
	}
	for(int i=0;i<size;i++){
		for(int j=0;j<size-i-1;j++){
			if(sortHour[j]>sortHour[j+1]){
				sortTemp = sortHour[j];
				sortHour[j] = sortHour[j+1];
				sortHour[j+1] = sortTemp;
				
				sort = sortDay[j];
				sortDay[j] = sortDay[j+1];
				sortDay[j+1] = sort;
				
				sort = sortTitle[j];
				sortTitle[j] = sortTitle[j+1];
				sortTitle[j+1] = sort;
				
				sort = sortId[j];
				sortId[j] = sortId[j+1];
				sortId[j+1] = sort;
				
				sort = sortClosed[j];
				sortClosed[j] = sortClosed[j+1];
				sortClosed[j+1] = sort;				
			}
		}	
	}
  %>
  <tr>
    <td style="text-align:center">跨天</td> 
	<%
		/*
		for(int i = 0;i<v.size();i++){
			out.print(v.get(i).toString());
		}*/
	%>
	<td colspan="7" style="text-align:center; width:98%">
     <%	
	 	int bMonth = 0;
	 	String modify = "修改";
		String see = "查看";
		String mstr = "";
		int day1 = 0;
		int day2 = 0;
		int eMonth = 0;
		int[] num = new int[7];
		int width = 150;
	 	int dayBegin = 0 , dayEnd = 0,dd = 0, tempNum = 0;
	 	String overDay = "";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); 
		c = Calendar.getInstance();   
     	int[] dayOfWeek = new int[2];//每月的第一天是星期几
		int[] tempMonth = new int[2];
		for(int i = 0; i<v.size(); i++){
			num[0] = Integer.parseInt(date_week[0].substring(8,10));
			num[6] = Integer.parseInt(date_week[6].substring(8,10));
			//out.print(num[0]+ "===" +num[6]);
			overDay = v.get(i).toString();//取出一个跨天的数据
			dayBegin = StrUtil.toInt(overDay.substring(8,10));//取出跨天日程开始的天
			dayEnd = StrUtil.toInt(overDay.substring(27,29));//取出跨天日程结束的天
			c.setTime(df.parse(overDay.substring(0,10)));
			bMonth = c.get(Calendar.MONTH)+1;
			dayOfWeek[0] = c.get(Calendar.DAY_OF_WEEK)-1;
			c.setTime(df.parse(overDay.substring(19,30)));
			eMonth = c.get(Calendar.MONTH)+1;
			dayOfWeek[1] = c.get(Calendar.DAY_OF_WEEK)-1;
			if(bMonth != eMonth){
				dd = DateUtil.getDayCount(y, bMonth-1);
				dayEnd = dd + dayEnd;
				//out.print(dayEnd);
			}
			c.setTime(df.parse(date_week[0]));
			
			tempMonth[0] = c.get(Calendar.MONTH)+1;
			c.setTime(df.parse(date_week[6]));
			
			tempMonth[1] = c.get(Calendar.MONTH)+1;
			if(dayOfWeek[1] == 0){
				dayOfWeek[1] = 7;
			}
			if(dayOfWeek[0] == 0){
				dayOfWeek[0] = 7;
			}
			if(bMonth < tempMonth[0]){
				num[0] = dd + num[0]; 
				num[6] = dd + num[6];
			}
			
			if (isShared) {
			  ud = ud.getUserDb(pd.getUserName());
			  overDay = ud.getRealName() + "：" + overDay;
		   	}	
		   				
			mstr = "<a href='javascript:;' onclick='show(" + overId.get(i) + ")'>"+ see +"</a>";
			mstr += "<a href='javascript:;' onclick='edit(" + overId.get(i) + ")'>"+ modify +"</a>";
			mstr += "<a onclick='del(" + overId.get(i) + ")' style='cursor:pointer;color:#666'>删除</a>";						
			
			if(tempMonth[0] != tempMonth[1]){
				dd = DateUtil.getDayCount(y, tempMonth[0]-1);
				tempNum = dd + num[6];
				if(dayBegin < num[0] && (dayEnd >= num[0] && dayEnd < tempNum)){%>
						<div id="plan<%=overId.get(i) %>" class="divsty" style="float:left; clear:left; width:<%=(14*dayOfWeek[1])%>%">
						<a href="plan.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>&week=<%=w-1%>"><img src="images/4.gif" style="float:left;" /></a>
						<a class="nav" href='javascript:;' onMouseOver="showmenu(event, &quot;<%=mstr%>&quot;, 0)"><%=overDay%></a>
						</div>
					<%}else if(dayBegin > num[0] && dayEnd < tempNum){%>
						<div style="float:left; clear:left; width:<%=(14*(dayOfWeek[0]-1))%>%"></div>
						<div id="plan<%=overId.get(i) %>" class="divsty" style="float:left; width:<%=((dayOfWeek[1]-dayOfWeek[0]+1)*14)%>%">
						<a class="nav" href='javascript:;' onMouseOver="showmenu(event, &quot;<%=mstr%>&quot;, 0)"><%=overDay%></a></div>
						<div>
						</div>
					<%}else if(dayBegin == num[0] && dayEnd < tempNum ){%>
						<div id="plan<%=overId.get(i) %>" class="divsty" style="float:left; clear:left; width:<%=(14*dayOfWeek[1])%>%">
						<a class="nav" href='javascript:;' onMouseOver="showmenu(event, &quot;<%=mstr%>&quot;, 0)"><%=overDay%></a>
						</div>
						<div></div>
					<%}else if(dayBegin > num[0] && dayEnd == tempNum){%>
						<div style="float:left; clear:left; width:<%=(14*(dayOfWeek[0]-1))%>%"></div>
						<div id="plan<%=overId.get(i) %>" class="divsty" style="float:left; width:<%=((dayOfWeek[1]-dayOfWeek[0]+1)*14)%>%">
						<a class="nav" href='javascript:;' onMouseOver="showmenu(event, &quot;<%=mstr%>&quot;, 0)"><%=overDay%></a></div>
					<%}else if((dayBegin > num[0] && dayBegin < tempNum)&& dayEnd > tempNum){%>
						<div style="float:left; clear:left; width:<%=(14*(dayOfWeek[0]-1))%>%"></div>
						<div id="plan<%=overId.get(i) %>" class="divsty" style="float:left; width:<%=(14*(8-dayOfWeek[0]))%>%">
						<a class="nav" href='javascript:;' onMouseOver="showmenu(event, &quot;<%=mstr%>&quot;, 0)"><%=overDay%></a>
						<a href="plan.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>&week=<%=w+1%>"><img src="images/3.gif" style="float:right;" /></a></div>
					<%}else{%>
						<div id="plan<%=overId.get(i) %>" class="divsty" style="float:left; clear:left;  width:<%=98%>%">
						<a href="plan.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>&week=<%=w-1%>"><img src="images/4.gif" style="float:left;" /></a>
						<a class="nav" href='javascript:;' onMouseOver="showmenu(event, &quot;<%=mstr%>&quot;, 0)" style="float:left"><%=overDay%></a>
						<a href="plan.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>&week=<%=w+1%>"><img src="images/3.gif" style="float:right;" /></a></div>
					<%}
				}else{
					if(dayBegin < num[0] && (dayEnd >= num[0] && dayEnd < num[6])){
					%>
						<div id="plan<%=overId.get(i) %>" class="divsty" style="float:left;  width:<%=(14*dayOfWeek[1])%>%">
						<a href="plan.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>&week=<%=w-1%>" style="clear:left"><img src="images/4.gif" style="float:left;" /></a>
						<a class="nav" href='javascript:;' onMouseOver="showmenu(event, &quot;<%=mstr%>&quot;, 0)" onmouseout="highlightmenu(event,'off')">
						<%
						if(dayOfWeek[1] == 1){%>
							<%=overDay.substring(0,18)%><br/><%=overDay.substring(18)%>
						<%}else{%>
							<%=overDay%>
						<%}%>
						</a>
						</div>
						<div style="clear:left"></div>
					<%}else if(dayBegin > num[0] && dayEnd < num[6]){%>
						<div style="float:left; clear:left; width:<%=(14*(dayOfWeek[0]-1))%>%"></div>
						<div id="plan<%=overId.get(i) %>" class="divsty" style="float:left; width:<%=((dayOfWeek[1]-dayOfWeek[0]+1)*14)%>%">
						<a class="nav" href='javascript:;' onMouseOver="showmenu(event, &quot;<%=mstr%>&quot;, 0)"><%=overDay%></a></div><br />
					<%}else if(dayBegin == num[0] && dayEnd < num[6] ){%>
						<div id="plan<%=overId.get(i) %>" class="divsty" style="float:left; width:<%=(14*dayOfWeek[1])%>%">
						<a class="nav" href='javascript:;' onMouseOver="showmenu(event, &quot;<%=mstr%>&quot;, 0)"><%=overDay%></a>
						</div>
					<%}else if(dayBegin > num[0] && dayEnd == num[6]){%>
						<div style="float:left; clear:left; width:<%=(14*(dayOfWeek[0]-1))%>%"></div>
						<div id="plan<%=overId.get(i) %>" class="divsty" style="float:left; width:<%=((dayOfWeek[1]-dayOfWeek[0]+1)*14)%>%">
						<a class="nav" href='javascript:;' onMouseOver="showmenu(event, &quot;<%=mstr%>&quot;, 0)"><%=overDay%></a></div>
					<%}else if((dayBegin > num[0] && dayBegin <= num[6])&& dayEnd > num[6]){%>
						<div style="float:left; clear:left; width:<%=(14.2*(dayOfWeek[0]-1))%>%"></div>
						<div id="plan<%=overId.get(i) %>" class="divsty" style="float:left;  width:<%=(13.9*(8-dayOfWeek[0]))%>%">
						<a class="nav" href='javascript:;' onMouseOver="showmenu(event, &quot;<%=mstr%>&quot;, 0)" style="float:left">
						<%
						if(dayOfWeek[0] == 7){%>
							<%=overDay.substring(0,18)%><br/><%=overDay.substring(18)%>
						<%}else{%>
							<%=overDay%>
						<%}%>
						</a>
						<a href="plan.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>&week=<%=w+1%>"><img src="images/3.gif" style="float:right" /></a></div>
					<%}else{%>
						<div id="plan<%=overId.get(i) %>" class="divsty" style="float:left; clear:left;  width:<%=98%>%">
						<a href="plan.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>&week=<%=w-1%>"><img src="images/4.gif" style="float:left;" /></a>
						<a class="nav" href='javascript:;' onMouseOver="showmenu(event, &quot;<%=mstr%>&quot;, 0)" style="float:left"><%=overDay%></a>
						<a href="plan.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>&week=<%=w+1%>"><img src="images/3.gif" style="float:right; clear:right" /></a>
						</div>
					<%}
				}
			}
	 	%>
	 </td>
    </tr>
  <%
  	int count = 0;int t = 0;boolean b = false;
	Calendar current=Calendar.getInstance();
	int currentYear = current.get(Calendar.YEAR);     
	int currentWeek = current.get(Calendar.WEEK_OF_YEAR);   
	int currentDay = current.get(Calendar.DATE);
	//int tempYe
	for(int i=0;i<7;i++){
		num[i] = Integer.parseInt(date_week[i].substring(8,10));
	}
	// 写出各时间段
    for(int i=1;i<=23;i++){%>
		<tr>
		<td style="height:40px; text-align:center;"><%= i%> :00</td>
		<%for(int j=0;j<7;j++){
			if(currentDay == num[j] && currentYear == y){
		%>
			<td style="background-color:#C8E1FF ">
				<%
				for(int k=0; k<hour_of_day.size(); k++){
					int tempHour = sortHour[k];
					String tempDay = sortDay[k];
					if(tempHour == i && StrUtil.toInt(tempDay) == num[j]){
						mstr = "<a href='javascript:;' onclick='show(" + sortId[k] + ")'>"+ see +"</a>";
						mstr += "<a href='javascript:;' onclick='edit(" + sortId[k] + ")'>"+ modify +"</a>";
						mstr += "<a onclick='del(" + sortId[k] + ")' style='cursor:pointer;color:#666'>删除</a>";						
						%>
						<div id="plan<%=sortId[k] %>" class="<%=sortClosed[k].equals("true")?"plan_closed":"plan_not_closed"%>">
						<script>
						var mstr<%=k%> = "<%=mstr%>";
						</script>
						<a class="nav" href='javascript:;' onmouseover="showmenu(event, mstr<%=k%>, 0)"><font color="#000000"><%=sortTitle[k]%></font></a></div><br>
					<%}
				}%>
			</td>
			<%}else{%>
				<td>
				<%
				for(int k=0;k<hour_of_day.size();k++){
					int tempHour = sortHour[k];
					String tempDay = sortDay[k];
					if(tempHour == i && StrUtil.toInt(tempDay) == num[j]){//onclick="openWin('plan_periodicity_new.jsp',494,400)"
						mstr = "<a href='javascript:;' onclick='show(" + sortId[k] + ")'>"+ see +"</a>";
						mstr += "<a href='javascript:;' onclick='edit(" + sortId[k] + ")'>"+ modify +"</a>";						
						mstr += "<a onclick='del(" + sortId[k] + ")' style='cursor:pointer;color:#666'>删除</a>";						
						%>
						<div id="plan<%=sortId[k] %>" class="<%=sortClosed[k].equals("true")?"plan_closed":"plan_not_closed"%>"><a href='javascript:;' onmouseover="showmenu(event, &quot;<%=mstr%>&quot;, 0)"><%=sortTitle[k]%></a></div><br>
					<%}
				}%>
			</td>
			<%}
		}%>
		</tr>
	<%}
  %>
</table>
</form>
<div class=menuskin id=popmenu onmouseover="clearhidemenu();highlightmenu(event,'on')" 
      onmouseout="highlightmenu(event,'off');dynamichide(event)" style="Z-index:100"></div>
<iframe width=0 height=0 src="" id="hiddenframe" style="display:none"></iframe>
</BODY>
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
</HTML>
