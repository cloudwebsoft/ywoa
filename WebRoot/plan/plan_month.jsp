<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.io.File"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "java.text.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="plan" scope="page" class="com.redmoon.oa.person.PlanMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
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
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}
boolean isShared = ParamUtil.getBoolean(request, "isShared", false);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE>日程安排</TITLE>
<meta http-equiv=Content-Type content="text/html; charset=utf-8" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="main.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script type="text/javascript" src="../js/jquery.toaster.js"></script>
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
width:40px;
}
.divsty {
  margin-top: 3px;
  padding-top: 3px;
  padding-right: 3px;
  padding-bottom: 3px;
  padding-left: 3px;
  background-color:#C8E1FF;
  text-align:left;
  width:50%;
}
</style>
</HEAD>
<BODY>
<%@ include file="plan_inc_menu_top.jsp"%>
<script>
$("menu1").className="current";
</script>
<div class="spacerH"></div>
<script language="javascript" type="text/javascript">
var y;
var m;
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
	m = document.form1.month.value;
	if(isChange == true){
		m--;
		document.form1.month.value = m ;
		form1.submit();
	}else{
		m++;
		document.form1.month.value = m ;
		form1.submit();
	}
}
</script>
<form name="form1" action="plan_month.jsp">
    <input name="isShared" value="<%=isShared%>" type="hidden" />
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
	  %>
	<input type="hidden" name="y" value="<%=y%>">
	<input type="hidden" name="m" value="<%=m%>">
<table width="98%" align="center" class="tabStyle_1 percent98" >
  <tr>
    <td width="17%" class="tabStyle_1_title"><input class="btn" type="button" value="今天" onclick="window.location.href='plan_day.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>'"/></td>
    <td class="tabStyle_1_title">
	<a href="#" onclick="yearChange(<%=y%>,true)"><img title="上一年" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="images/1.gif" /></a>
	&nbsp;
	<a href="#" onclick="monthChange(<%=m%>,true)"><img title="上一月" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="images/4.gif" /></a>
	&nbsp;
	<select name="year" onChange="onTypeCodeChange(this)" >	
	<%
	   c1=Calendar.getInstance();
	   for(int i=0;i<30;i++){
	     if(y==year){
		 %>
		 <option value="<%=year%>" selected="selected" ><%=year%>年</option>
	<%}else{%>
		 <option value="<%=year%>"><%=year%>年</option>
	<%
	   }year--;
	   }
	%>
    </select>  
	<select name="month" onchange="onTypeCodeChange1(this)">
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
	&nbsp;
	<a href="#" onclick="monthChange(<%=m%>,false)"><img title="下一月" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="images/3.gif" /></a>
	&nbsp;
	<a href="#" onclick="yearChange(<%=y%>,false)"><img title="下一年" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="images/2.gif" /></a>
	<input name="userName" value="<%=userName%>" type="hidden" />
	</td>
    <td width="17%" class="tabStyle_1_title"><a style="color:#666; font-weight: normal" href="plan_day.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>"><img src="<%=SkinMgr.getSkinPath(request)%>/icons/OAimg28.gif" align="absmiddle" />&nbsp;日</a>
	&nbsp;<a style="color:#666; font-weight: normal" href="plan.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>"><img src="<%=SkinMgr.getSkinPath(request)%>/icons/OAimg28.gif" align="absmiddle" />&nbsp;周</a>
	&nbsp;<a style="color:#666; font-weight: normal" href="plan_month.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>"><img src="<%=SkinMgr.getSkinPath(request)%>/icons/OAimg28.gif" align="absmiddle" />&nbsp;<b style="color:#4e96f0">月</b></a>	</td>
	</td>
  </tr>
</table>
<%
	String modify = "修改";
	String see = "查看";
	String mstr = "";
	GregorianCalendar   cal   =   new   GregorianCalendar();
	Vector v = new Vector();

	boolean b ;
	
	int dd = DateUtil.getDayCount(y, m-1);
	//得到每月的第一天和最后一天是一年的第几周
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); 
    Calendar c = Calendar.getInstance();   
    c.setTime(df.parse(y+"-"+m+"-"+"1"));
    int e = c.get(Calendar.DAY_OF_WEEK)-1;//每月的第一天是星期几
	if(e == 0){
		e=7;
	}
	int ww[] = new int[2];
	ww[0] = c.get(Calendar.WEEK_OF_YEAR);
	c.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(y+"-"+m+"-"+dd));
	c.setMinimalDaysInFirstWeek(7);
	int week1 = c.get(Calendar.WEEK_OF_YEAR);
	ww[1] = week1+1;
	int k = 1;
	int temp1  = dd;
	UserDb ud = new UserDb();		
	Vector content = new Vector();
	Vector hour_of_day = new Vector();
	Vector vid = new Vector();
	Vector date = new Vector();
	Vector vclosed = new Vector();
	v = new Vector();
	Vector overId = new Vector(); // 跨天
	String title = "";
	JdbcTemplate rmconn = new JdbcTemplate();
	 //String sql="select title,mydate,enddate from user_plan";
	String sql = "select id,title,mydate,enddate,is_closed,userName from user_plan where ((" + SQLFilter.year("mydate") + "=" + StrUtil.sqlstr(String.valueOf(y)) +" and "+ SQLFilter.month("mydate") + "=" + StrUtil.sqlstr(String.valueOf(m))+") or (" + SQLFilter.year("enddate") + "=" + StrUtil.sqlstr(String.valueOf(y)) + " and " +SQLFilter.month("enddate") + "=" + StrUtil.sqlstr(String.valueOf(m)) +"))";
	if (isShared) {
		sql += " and is_shared=1";
	}
	else {
 		sql += " and userName=" + StrUtil.sqlstr(userName);	
	}
	sql += " order by mydate,enddate";
	ResultIterator ri = rmconn.executeQuery(sql,0,0);
    ResultRecord rr = null;
	while (ri.hasNext()) {
		rr = (ResultRecord)ri.next();
		int id = rr.getInt(1);
		title = rr.getString(2);
		String startDate = DateUtil.format(rr.getDate(3), "yyyy-MM-dd HH:mm:ss");
		String endDate = DateUtil.format(rr.getDate(4), "yyyy-MM-dd HH:mm:ss");
		if (endDate.equals(""))
			endDate = startDate;
		String startDay = startDate.substring(8,10);
		String endDay = endDate.substring(8,10);
		String startHour = startDate.substring(11,13);
		String uName = rr.getString(6);
		if(startDay.equals(endDay)){
			title = startDate.substring(10,16) + " ~ " + endDate.substring(10,16) + "<br/>" +title + "<br/>";
		   	if (isShared) {
			  ud = ud.getUserDb(uName);
			  title = ud.getRealName() + "：" + title;
		   	}			
			content.add(title);
			hour_of_day.add(startHour);
			vid.add(String.valueOf(id));
			date.add(startDay);
			vclosed.add("" + (rr.getInt(5)==1));
		}else{
			title = startDate.substring(0,16) + " ~ " + endDate.substring(0,16) + "&nbsp;&nbsp;" +title + "<br/>";
		   	if (isShared) {
			  ud = ud.getUserDb(uName);
			  title = ud.getRealName() + "：" + title;
		   	}			
			overId.add(String.valueOf(id));
			v.add(title);
		}
	}
  %>
<table width="98%" align="center" class="tabStyle_1 percent98">
  <tr>
    <td class="tabStyle_1_title" width="7%">周数</td>
    <td class="tabStyle_1_title" width="13%">星期一</td>
    <td class="tabStyle_1_title" width="13%">星期二</td>
    <td class="tabStyle_1_title" width="13%">星期三</td>
    <td class="tabStyle_1_title" width="13%">星期四</td>
    <td class="tabStyle_1_title" width="13%">星期五</td>
    <td class="tabStyle_1_title" width="13%">星期六</td>
    <td class="tabStyle_1_title" width="13%">星期日</td>
  </tr>
  <tr>
    <td>跨天</td>
    <td colspan="7"><%
		int startMonth = 0;
		int endMonth = 0;
		String tempMonth = "";
		for(int i = 0;i<v.size();i++){
			tempMonth = v.get(i).toString();
			startMonth = StrUtil.toInt(tempMonth.substring(5,7));
			endMonth = StrUtil.toInt(tempMonth.substring(24,26));
			
			mstr = "<a href='javascript:;' onclick='show(" + overId.get(i) + ")'>"+ see +"</a>";
			mstr += "<a href='javascript:;' onclick='edit(" + overId.get(i) + ")'>"+ modify +"</a>";						
			mstr += "<a onclick='del(" + vid.get(i) + ")' style='cursor:pointer;color:#666'>删除</a>";						
			
			if(startMonth == m && endMonth == m){%>
	        	<div id="plan<%=overId.get(i) %>" class="divsty"><a href='javascript:;' onmouseover="showmenu(event, &quot;<%=mstr%>&quot;, 0)"><font color="#0000FF"><%=v.get(i).toString()%></font></a></div>
		    <%}else if(startMonth < m && endMonth == m){%>
		        <div id="plan<%=overId.get(i) %>" class="divsty"> <a href="plan_month.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>&year=<%=y%>&month=<%=m-1%>"> <img src="images/4.gif" style="float:left;onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" title="上一月" /></a> <a href='javascript:;' onmouseover="showmenu(event, &quot;<%=mstr%>&quot;, 0)"><font color="#0000FF"><%=v.get(i).toString()%></font> </a></div>
		    <%}else if(endMonth > m && startMonth == m){%>
		       <div id="plan<%=overId.get(i) %>" class="divsty" style="float:left"><a style="float:left" href='javascript:;' onmouseover="showmenu(event, &quot;<%=mstr%>&quot;, 0)"><font color="#0000FF"> <%=v.get(i).toString()%></font></a> <a href="plan_month.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>&year=<%=y%>&month=<%=m+1%>" style="float:right; " > <img src="images/3.gif" style="float:right;onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" title="下一月" /></a> </div>
		    <%}else{%>
		        <div id="plan<%=overId.get(i) %>" class="divsty" style="float:left"> <a href="plan_month.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>&year=<%=y%>&month=<%=m-1%>"> <img src="images/4.gif" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" title="上一月" /></a> <a href='javascript:;' onmouseover="showmenu(event, &quot;<%=mstr%>&quot;, 0)"><font color="#3300FF"><%=v.get(i).toString()%></font> </a> <a href="plan_month.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>&year=<%=y%>&month=<%=m+1%>" style="float:left;" > <img src="images/3.gif" style="float:left;onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" title="下一月" /></a> </div>
		    <%}
			}
		%>
    </td>
  </tr>
  <%
   //得到每月的第一天是星期几
  // Date date = new Date() ; 
   //date.setYear(Integer.parseInt(y)) ; 
   //date.setMonth(Integer.parseInt(m)) ; 
   // date.setDate(1) ;
	Calendar current=Calendar.getInstance();
	int currentYear = current.get(Calendar.YEAR);
	int currentMonth = current.get(Calendar.MONTH)+1;   
	int currentDay = current.get(Calendar.DATE);
	int count = 1;
	int num = 0;
	b=false;
	for(int i=ww[0];i<=ww[1];i++){%>
  <tr>
    <td style="height:100px ">第<%=i%>周</td>
    <%for(int j=0;j<7;j++){ 
				if(k<e){
					out.print("<td></td>");
				}else if(k>=temp1 && k<=((ww[1]-ww[0]+1)*7) && count > temp1){
					out.print("<td></td>");
				}else if(k>((ww[1]-ww[0]+1)*7)){
					break;
				}else if(k >= e || count <= temp1){%>
    <td valign="top" align="right"><div style="background-color:#C8E1FF"> <a href="plan_day.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>&year=<%=y%>&month=<%=m%>&day=<%=count%>"><font color="#0000FF"><b><%=count%></b></font></a></div>
        <%if(count == temp1 && j==6){
						b = true;
						break;
					}	
					for(int t = num;t<content.size();t++){
						String tempDay = date.get(t).toString();
						if(StrUtil.toInt(tempDay) == count){
							if(currentYear == y && currentMonth == m && currentDay == count){
								mstr = "<a href='javascript:;' onclick='show(" + vid.get(t) + ")'>"+ see +"</a>";
								mstr += "<a href='javascript:;' onclick='edit(" + vid.get(t) + ")'>"+ modify +"</a>";						
								mstr += "<a onclick='del(" + vid.get(t) + ")' style='cursor:pointer;color:#666'>删除</a>";						
		 					%>
					        	<div id="plan<%=vid.get(t) %>" style="text-align:left" class="<%=vclosed.get(t).equals("true")?"plan_closed":"plan_not_closed"%>"><a class="nav" href='javascript:;' onmouseover="showmenu(event, &quot;<%=mstr%>&quot;, 0)"><%=content.get(t).toString()%></a></div>
					      		<br />
					        <%}else{
								mstr = "<a href='javascript:;' onclick='show(" + vid.get(t) + ")'>"+ see +"</a>";
								mstr += "<a href='javascript:;' onclick='edit(" + vid.get(t) + ")'>"+ modify +"</a>";						
								mstr += "<a onclick='del(" + vid.get(t) + ")' style='cursor:pointer;color:#666'>删除</a>";						
					 		%>
					        	<div id="plan<%=vid.get(t) %>" style="text-align:left" class="<%=vclosed.get(t).equals("true")?"plan_closed":"plan_not_closed"%>"><a class="nav" href='javascript:;' onmouseover="showmenu(event, &quot;<%=mstr%>&quot;, 0)"><%=content.get(t).toString()%></a></div>
					      		<br />
					        <%}
						}else{
							break;
						}
					num++;
					}
					count++;
					%>
    </td>
    <%}else{
					 break;
				}
					 k++;   
			}
			if(b == true){
				break;
			}
		%>
  </tr>
  <%}
	 if(count <= temp1){%>
  <tr>
    <td style="height:100px">第<%=ww[1]+1%>周</td>
    <%	for(int i=0;i<7;i++){
	 		if(count>temp1){%>
    <td></td>
    <%}else{%>
    <td valign="top" align="right"><div style="background-color:#C8E1FF"> <a href="plan_day.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>&year=<%=y%>&month=<%=m%>&day=<%=count%>"><font color="#0000FF"><b><%=count%></b></font></a> </div>
        <%
					String d = "";
				%>
    </td>
    <%}
			count++;
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
