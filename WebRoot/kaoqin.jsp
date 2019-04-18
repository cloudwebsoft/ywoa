<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.Calendar"%>
<%@ page import = "java.util.Date"%>
<%@ page import = "com.redmoon.oa.*"%>
<%@ page import = "com.redmoon.oa.oacalendar.*"%>
<%@ page import = "com.redmoon.oa.kaoqin.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.StrUtil"%>
<%@ page import = "cn.js.fan.util.ParamUtil"%>
<%@ page import = "cn.js.fan.util.ErrMsgException"%>
<%@ page import = "cn.js.fan.util.DateUtil"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ include file="inc/inc.jsp"%>
<%@ page import="java.text.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
    out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

Calendar cal = Calendar.getInstance();
int curmonth = cal.get(Calendar.MONTH);
int curyear = cal.get(Calendar.YEAR);

String COLOR_LATE = "#ffeeee";
String COLOR_BEFORE = "#ffff00";

int showyear,showmonth;
String strshowyear = request.getParameter("showyear");
String strshowmonth = request.getParameter("showmonth");
if (strshowyear!=null)
	showyear = Integer.parseInt(strshowyear);
else
	showyear = cal.get(Calendar.YEAR);
if (strshowmonth!=null)
	showmonth = Integer.parseInt(strshowmonth);
else
	showmonth = cal.get(cal.MONTH)+1;

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
String op = ParamUtil.get(request, "op");

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>考勤</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="inc/common.js"></script>
<script type="text/javascript" src="js/jquery1.7.2.min.js"></script>
<script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script language="JavaScript" type="text/JavaScript">
<!--
function MM_preloadImages() { //v3.0
  var d=document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
    var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
    if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
}

function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}

function window_onload() {
	$("lateCount").innerHTML = $("lateCountSpan").innerHTML;
	$("beforeCount").innerHTML = $("beforeCountSpan").innerHTML;
	// $("overtimeCount").innerHTML = $("overtimeCountSpan").innerHTML;
}
//-->
</script>
<style>
	.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
	-moz-opacity: 0.20;
	opacity: 0.20;
	z-index: 1500;
	}
	.treeBackground {
	display: none;
	position: absolute;
	top: -2%;
	left: 0%;
	width: 100%;
	margin: auto;
	height: 200%;
	background-color: #EEEEEE;
	z-index: 1800;
	-moz-opacity: 0.8;
	opacity: .80;
	filter: alpha(opacity = 80);
	}
</style>
</head>
<body onload="window_onload()">
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='images/loading.gif'></div>
<%
if (op.equals("add")) {
	boolean re = false;
	try {
		KaoqinMgr km = new KaoqinMgr();
		%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		re = km.create(request);
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	if (re)
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "kaoqin.jsp?showyear=" + showyear + "&showmonth=" + showmonth));
	return;
}
else if (op.equals("del")) {
	boolean re = false;
	try {
		KaoqinMgr km = new KaoqinMgr();
		%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		re = km.del(request);
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	if (re)
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "kaoqin.jsp?showyear=" + showyear + "&showmonth=" + showmonth));
	return;
}
if (!privilege.isUserLogin(request))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%@ include file="kaoqin_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<%!
  int daysInMonth[] = {
      31, 28, 31, 30, 31, 30, 31, 31,
      30, 31, 30, 31};

  public int getDays(int month, int year) {
    //测试选择的年份是否是润年？
    if (1 == month)
      return ( (0 == year % 4) && (0 != (year % 100))) ||
          (0 == year % 400) ? 29 : 28;
        else
      return daysInMonth[month];
  }
%>
<%	
cfgparser.parse("config_oa.xml");
java.util.Properties props = cfgparser.getProps();

int latevalue = Integer.parseInt(props.getProperty("latevalue"));
boolean isKaoqinNight = "true".equals(props.getProperty("isKaoqinNight"));

OACalendarDb oaCalendarDb = new OACalendarDb();
oaCalendarDb = (OACalendarDb)oaCalendarDb.getQObjectDb(DateUtil.parse(DateUtil.format(new java.util.Date(), "yyyy-MM-dd"), "yyyy-MM-dd"));

com.redmoon.oa.person.UserDb user = new com.redmoon.oa.person.UserDb();
user = user.getUserDb(userName);
%>
<div class="spacerH"></div>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td align="center"><strong><%=user.getRealName()%>&nbsp;&nbsp;<%=showmonth%>月考勤表 &nbsp;&nbsp;迟到或早退为相差<%=latevalue%>分钟</strong></td>
    </tr>
  </tbody>
</table>
<br />
<table width="98%" border="0" align="center">
  <tr>
    <td align="center">
	今日上午：<%=oaCalendarDb.getString("work_time_begin_a")%>~
	<%=oaCalendarDb.getString("work_time_end_a")%>
	下午：<%=oaCalendarDb.getString("work_time_begin_b")%>~
	<%=oaCalendarDb.getString("work_time_end_b")%>
    <%if (isKaoqinNight) {%>
	晚上：<%=oaCalendarDb.getString("work_time_begin_c")%>~	<%=oaCalendarDb.getString("work_time_end_c")%>
    <%}%>
	迟到：<span style="background-color:<%=COLOR_LATE%>">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;&nbsp;
	早退：<span style="background-color:<%=COLOR_BEFORE%>">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
	&nbsp;&nbsp;<a href="oa_calendar.jsp" target="_blank">工作日历</a></td>
  </tr>
  <tr>
    <td align="center">
      <select name="showyear" onchange="var y = this.options[this.selectedIndex].value; window.location.href='?userName=<%=StrUtil.UrlEncode(userName)%>&showyear=' + y;">
        <%for (int y=curyear-60; y<=curyear; y++) {%>
        <option value="<%=y%>"><%=y%></option>
        <%}%>
      </select>
      <script>
		  showyear.value = "<%=showyear%>";
		  </script>
      <%
for (int i=1; i<=12; i++) {
	if (showmonth==i)
		out.print("<a href='kaoqin.jsp?userName=" + StrUtil.UrlEncode(userName) + "&showyear="+showyear+"&showmonth="+i+"'><font color=red>"+i+"月</font></a>&nbsp;");
	else
		out.print("<a href='kaoqin.jsp?userName=" + StrUtil.UrlEncode(userName) + "&showyear="+showyear+"&showmonth="+i+"'>"+i+"月</a>&nbsp;");
}
%></td>
  </tr>
  <tr>
    <td align="center">
	<a href="kaoqin.jsp?userName=<%=StrUtil.UrlEncode(userName)%>&showyear=<%=showyear%>&amp;showmonth=<%=showmonth%>">全部</a>
	<%
	BasicDataMgr bdm = new BasicDataMgr("kaoqin");
	String[][] r = bdm.getOptions("type");
	int len = r.length;
	for (int i=0; i<len; i++) {%>
		<a href="kaoqin.jsp?userName=<%=StrUtil.UrlEncode(userName)%>&showtype=<%=StrUtil.UrlEncode(r[i][1])%>&amp;showyear=<%=showyear%>&amp;showmonth=<%=showmonth%>"><%=r[i][0]%></a>
	<%}%>  	&nbsp;&nbsp;迟到次数：<span id="lateCount"></span>&nbsp;&nbsp;&nbsp;早退次数：<span id="beforeCount"></span><!--&nbsp;&nbsp;&nbsp;加班次数：<span id="overtimeCount"></span>--></td>
  </tr>
</table>
<%
String sql;
// 获取本月考勤
String showtype = ParamUtil.get(request, "showtype");

cal.set(showyear,showmonth-1,1,0,0,0);
java.util.Date d1 = cal.getTime();
cal.set(showyear,showmonth-1,getDays(showmonth-1, showyear),23,59,59);
java.util.Date d2 = cal.getTime();

if (showtype.equals("")) {
	// sql = "select id from kaoqin where name="+fchar.sqlstr(privilege.getUser(request))+" and MONTH(myDate)="+showmonth+" and YEAR(myDate)="+showyear+" order by mydate asc";
	sql = "select id from kaoqin where name="+fchar.sqlstr(userName)+" and myDate>=" + SQLFilter.getDateStr(DateUtil.format(d1, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss") + " and myDate<=" +SQLFilter.getDateStr(DateUtil.format(d2, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss") + " order by mydate asc";	
}
else {
	sql = "select id from kaoqin where name="+fchar.sqlstr(userName)+" and myDate>=" + SQLFilter.getDateStr(DateUtil.format(d1, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss") + " and myDate<=" +SQLFilter.getDateStr(DateUtil.format(d2, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss") +" and type="+fchar.sqlstr(showtype)+" order by mydate asc";
}

int i = 1;
String direction="",type="",reason="",mydate="",strweekday="";
int id;
int weekday=0;
Date dt = null;
int monthday = 0;
int monthdaycount = getDays(showmonth-1,showyear);//当前显示月份的天数
String[] wday = {"","日","一","二","三","四","五","六"};

String backcolor = "#ffffff";
int myhour = 0;			// 用于计算迟到时间
int myminute = 0;
int latecount = 0;		// 迟到次数
int beforecount = 0; 	// 早退次数
int overtimeCount = 0; 	// 加班次数
int latehour = 0;
int lateminute = 0;
Calendar cld = Calendar.getInstance();

KaoqinDb kd = new KaoqinDb();
Vector v = kd.list(sql);
Iterator ir = v.iterator();
%>
<br />
<table width="98%" align="center" class="tabStyle_1 percent98">
  <tr>
    <td width="6%" class="tabStyle_1_title">星期</td>
    <td width="7%" class="tabStyle_1_title">日期</td>
    <td width="9%" class="tabStyle_1_title">时间</td>
    <td width="12%" class="tabStyle_1_title">去向</td>
    <td width="13%" class="tabStyle_1_title">类型</td>
    <td width="13%" class="tabStyle_1_title">迟到早退(分钟)</td>
    <td width="28%" class="tabStyle_1_title">事由</td>
    <td width="12%" class="tabStyle_1_title">操作</td>
  </tr>
<%
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");				
 		if (ir.hasNext()) {
			kd = (KaoqinDb)ir.next();
			mydate = DateUtil.format(kd.getMyDate(), "yyyy-MM-dd HH:mm:ss");
			//dt = rs.getDate("mydate");//这样取的话会丢失小时和分钟信息
			dt = kd.getMyDate();
			cld.setTime(dt);
			monthday = cld.get(cld.DAY_OF_MONTH);
		}
		
		OACalendarDb oad = new OACalendarDb();
		
		while (i<=monthdaycount)
		{
			if (monthday==i)
			{
				while (monthday==i) {
					backcolor = "";
					id = kd.getId();
					direction = kd.getDirection();
					type = kd.getType();
					reason = kd.getReason();
					
					mydate = DateUtil.format(kd.getMyDate(), "yyyy-MM-dd HH:mm:ss");
					String strTempDate = DateUtil.format(kd.getMyDate(), "yyyy-MM-dd 00:00:00");
					dt = formatter.parse(strTempDate);
					cld.setTime(dt);
					
					mydate = mydate.substring(11,19);
					weekday = cld.get(cld.DAY_OF_WEEK);
					strweekday = wday[weekday];
					// 计算是否迟到
					myhour = cld.get(cld.HOUR_OF_DAY);
					myminute = cld.get(cld.MINUTE);
					if (type.equals("考勤"))	{
						OACalendarDb oad2 = (OACalendarDb)oad.getQObjectDb(dt);
						if (oad2.getInt("date_type") == OACalendarDb.DATE_TYPE_WORK) {
							if (kd.getFlag()==1) {
								latecount ++;
								backcolor = COLOR_LATE;								
							}
							else if (kd.getFlag()==2) {
								beforecount ++;
								backcolor = COLOR_BEFORE;								
							}
						}
					}
					else if (type.equals("加班")) {
						if (direction.equals("c"))					
							overtimeCount++;
					}					
					String directionDesc = bdm.getItemText("direction", direction);
				%>
  <tr class="highlight">
    <td align="center" bgcolor="<%=backcolor%>"><%=strweekday%></td>
    <td align="center" bgcolor="<%=backcolor%>"><%=i%></td>
    <td align="center" bgcolor="<%=backcolor%>"><%=mydate%></td>
    <td align="center" bgcolor="<%=backcolor%>"><%=directionDesc%></td>
    <td align="center" bgcolor="<%=backcolor%>"><%=type%></td>
    <td align="center" bgcolor="<%=backcolor%>">
	<%
		if (kd.getFlag()==1) {
			out.print("迟到" + kd.getTimeMin());
		}
		else if (kd.getFlag()==2) {
			out.print("早退" + kd.getTimeMin());
		}	
	%>
	</td>
    <td bgcolor="<%=backcolor%>"><%=reason%></td>
    <td align="center" bgcolor="<%=backcolor%>">
<%
if (kpvgTop.canAdminKaoqin(request)) {
%>
	<a href="javascript:;" onclick="addTab('考勤','<%=Global.getFullRootPath(request) %>/kaoqin_modify.jsp?id=<%=id%>')">修改</a>&nbsp;&nbsp;<a onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{window.location.href='kaoqin.jsp?op=del&id=<%=id%>&showyear=<%=showyear%>&showmonth=<%=showmonth%>'}})" style="cursor:pointer">删除</a>
<%
}
%>	</td>
  </tr>
<%
					if (ir.hasNext()) {
						kd = (KaoqinDb)ir.next();
						dt = kd.getMyDate();
						cld.setTime(dt);
						monthday = cld.get(cld.DAY_OF_MONTH);
					}
					else {
						break;
					}
				 }
			}
			else {
				cld.set(showyear,showmonth-1,i);
				weekday = cld.get(cld.DAY_OF_WEEK);
				strweekday = wday[weekday];
				if (weekday==1 || weekday==7)
					strweekday = "<font color=red>"+strweekday+"</font>";				
				%>
  <tr class="highlight">
    <td align="center"><%=strweekday%></td>
    <td align="center"><%=i%></td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
<%
			}
			i++;
		}%>
  <tr>
    <td class="tabStyle_1_title" colspan="8" align="center" style="display:none">迟到次数：<span id="lateCountSpan"><%=latecount%></span>&nbsp;&nbsp;&nbsp;早退次数：<span id="beforeCountSpan"><%=beforecount%></span><!--&nbsp;&nbsp;&nbsp;加班次数：<span id="overtimeCountSpan"><%=overtimeCount%></span>--></td>
  </tr>
</table>
<br />
</body>
</html>
