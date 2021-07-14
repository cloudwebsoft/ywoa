<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.worklog.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "java.text.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>工作报告 - 周报</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
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

<style type="text/css">
.divsty {  margin-top: 3px;
  padding-top: 3px;
  padding-right: 3px;
  padding-bottom: 3px;
  padding-left: 3px;
  background-color:#C8E1FF;
  text-align:left;
  width:50%;
}

.workplan_appraise {
	color:blue;
}
</style>
<script>
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
function monthChange(obj,isChange){
	m = form1.month.value;
	if(isChange == true){
		if (m==1){
			m=12;
			form1.year.value = Number($("#year").val()) - 1;
		}else{
			m--;
		}
		form1.month.value = m ;
	}else{
		if(m == 12){
			m=1;
			var y =  Number($("#year").val());
			$("#year").val(y+1);
		}else{
				m++;
		}
		form1.month.value = m ;
	}
	form1.submit();
}

function onYearChange(obj){
     var y = obj.options[obj.options.selectedIndex].value;	
	 form1.year.value = y;
	 o("form1").submit();
}

function onMonthChange(obj){
     var m = obj.options[obj.options.selectedIndex].value;	
	 document.form1.month.value = m;
	 form1.submit();
}
</script>
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request, priv)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
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
%>
<%@ include file="mywork_nav.jsp"%>
<script>
o("menu5").className="current";
</script>
<div class="spacerH"></div>
<%
int y = ParamUtil.getInt(request, "year", -1);
int m = ParamUtil.getInt(request, "month", -1);
String sj="";
Calendar c1 = Calendar.getInstance();
int year = c1.get(Calendar.YEAR);
if(y==-1){
  y=year;
}
if(m==-1){
  m=c1.get(Calendar.MONTH)+1;
}

String op = ParamUtil.get(request, "op");

WorkLogDb workLogDb = new WorkLogDb();

UserMgr um = new UserMgr();

com.redmoon.oa.worklog.Config cfg = com.redmoon.oa.worklog.Config.getInstance();
int weekLimit = cfg.getIntProperty("weekLimit");
%>
<table width="98%" align="center" border="0" cellpadding="0" cellspacing="0" class="percent80" height="40">
  <tr>
    <td align="center">
    <form id="form1" name="form1" action="mywork_list_week.jsp" method="get">
      &nbsp;&nbsp;<a href="#" onclick="yearChange(<%=y%>,true)"><img title="上一年" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="../plan/images/1.gif" /></a> &nbsp; <a href="#" onclick="monthChange(<%=m%>,true)"><img title="上一月" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="../plan/images/4.gif" /></a> &nbsp;
      <input type=hidden id=userName name=userName value='<%=userName %>' />
      <select id="year" name="year" onChange="onYearChange(this)" >
      <%
	   c1=Calendar.getInstance();
	   year += 1;
	   for(int i=0;i<30;i++){
	     if(y==year){
		 %>
        <option value="<%=year%>" selected="selected" ><%=year%>年</option>
        <%}else{%>
        <option value="<%=year%>"><%=year%>年</option>
        <%
	   	}
	   	year--;
	   }
	%>
      </select>
      <select id="month" name="month" onchange="onMonthChange(this)">
        <%
	for(int i=1;i<=12;i++){
	  if(m==i){
	%>
        <option value="<%=i%>" selected="selected"><%=i%>月</option>
        <%}else{%>
        <option value="<%=i%>"><%=i%>月</option>
        <%}
	}%>
      </select>
		&nbsp; <a href="#" onclick="monthChange(<%=m%>,false)"><img title="下一月" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="../plan/images/3.gif" /></a> &nbsp; <a href="#" onclick="yearChange(<%=y%>,false)"><img title="下一年" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="../plan/images/2.gif" /></a>
        &nbsp;（超出<%=weekLimit%>天后不可汇报）
    </form>
	</td>
  </tr>
</table>
<table id="mainTable" width="98%" align="center" class="tabStyle_1 percent98">
  <tr>
    <td class="tabStyle_1_title" width="7%">周数</td>
    <td class="tabStyle_1_title" width="8%">时间</td>
    <td class="tabStyle_1_title" width="9%">汇报人</td>
    <td class="tabStyle_1_title" width="52%">内容</td>
    <td class="tabStyle_1_title" width="11%">操作</td>
  </tr>
  <%
	int dd = DateUtil.getDayCount(y, m-1);
	//得到每月的第一天和最后一天是一年的第几周
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); 
    Calendar c = Calendar.getInstance();
	c.setFirstDayOfWeek(Calendar.MONDAY);	

	// 当前周数
	int curw = c.get(Calendar.WEEK_OF_YEAR);

    c.setTime(df.parse(y+"-"+m+"-"+"1"));
    int e = c.get(Calendar.DAY_OF_WEEK)-1;//每月的第一天是星期几
	if(e == 0){
		e=7;
	}
	int ww[] = new int[2];
	ww[0] = c.get(Calendar.WEEK_OF_YEAR);
	c.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(y+"-"+m+"-"+dd));
	// c.setMinimalDaysInFirstWeek(7);

	int week1 = c.get(Calendar.WEEK_OF_YEAR);
	ww[1] = week1+1;
	int k = 1;
	int temp1 = dd;

	Calendar current = Calendar.getInstance();
	int currentYear = current.get(Calendar.YEAR);
	int currentMonth = current.get(Calendar.MONTH)+1;
	int currentDay = current.get(Calendar.DATE);
	int count = 1;
	int num = 0;
	boolean b = false;
	boolean isNextYear = (currentYear + 1 == y && currentMonth == 12 && curw == 1);

	if (ww[0]>ww[1]) {
		c.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(y+"-"+m+"-"+ (dd-7)));	
		ww[1] = c.get(Calendar.WEEK_OF_YEAR);
	}
	
	for(int i=ww[0];i<=ww[1];i++){%>
  <tr>
    <td style="height:80px" align="center">第<%=i%>周
    </td>
    <td style="height:80px" align="center"><%
	Calendar calendar = Calendar.getInstance();
	calendar.clear();
	calendar.setFirstDayOfWeek(Calendar.MONDAY);	
	calendar.set(Calendar.WEEK_OF_YEAR, i);
	calendar.set(Calendar.YEAR, y);
	
	// Now get the first day of week.
	Date date = calendar.getTime();
	Date date1 = DateUtil.addDate(date, 6);
	%>
    <%=DateUtil.format(date, "yyyy-MM-dd")%>
    至
    <%=DateUtil.format(date1, "yyyy-MM-dd")%>
    </td>
    <td align="center">
    <%
	WorkLogDb wld = workLogDb.getWorkLogDb(userName, WorkLogDb.TYPE_WEEK, y, i);
	if (wld!=null) {
	%>
    <%=um.getUserDb(wld.getUserName()).getRealName()%>
    <%}%>
    </td>
    <td align="left"  style='word-break:break-all'>
      <%
	if (wld!=null) {
			out.print(wld.getContent());
			
			String appraise = wld.getAppraise();
			if (!appraise.equals("")) {
				%>
      <div class="worklog_appraise" style="border-top:1px dashed #cccccc; padding-top:5px">
        <div id="appraise<%=wld.getId()%>"><%=appraise%></div>
        </div>
      <%
			}
			%>		
      <%
	}
	%>
    </td>
    <td align="center">
    <%
		if (wld==null) {
			Calendar cal=Calendar.getInstance();
			cal.clear();
			cal.set(Calendar.YEAR, isNextYear ? currentYear + 1 : currentYear);
			cal.set(Calendar.WEEK_OF_YEAR, i);
			cal.setTimeInMillis(cal.getTimeInMillis()+8*24*60*60*1000);
			cal = DateUtil.add(cal.getTime(), weekLimit);
			if (((currentYear == y && (currentMonth == m || currentMonth == m + 1)) || (isNextYear && m == 1 && i == 1)) && (curw>=i || (currentMonth == 12 && curw == 1)) && DateUtil.compare(current, cal)==2) {
			%>
			<a href="<%=request.getContextPath()%>/mywork/mywork_add.jsp?logYear=<%=y%>&logType=<%=WorkPlanAnnexDb.TYPE_WEEK%>&logItem=<%=i%>">汇报</a>
			<%
			}
		}
		else {
			%>
			<a href="javascript:;" onclick="addTab('第<%=i%>周 周报', '<%=request.getContextPath()%>/mywork/mywork_edit.jsp?id=<%=wld.getId()%>&userName=<%=StrUtil.UrlEncode(userName)%>')">编辑</a>
			<%if (userName.equals(privilege.getUser(request)) || privilege.canAdminUser(request, userName)) {%>
            &nbsp;&nbsp;<a title="点评" href="mywork_appraise.jsp?id=<%=wld.getId()%>&userName=<%=StrUtil.UrlEncode(userName)%>">点评</a>
            <%}
		}
	%>
    </td>
  </tr>
  <%}
%>
</table>
</body>
<script>
$(document).ready( function() {
	$("#mainTable td").mouseout( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).removeClass("tdOver"); });
	});  
	
	$("#mainTable td").mouseover( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).addClass("tdOver"); });  
	});  
});
</script>
</html>
