<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.kaoqin.*"%>
<%@ page import = "cn.js.fan.util.StrUtil"%>
<%@ page import = "cn.js.fan.util.ParamUtil"%>
<%@ page import = "cn.js.fan.util.ErrMsgException"%>
<%@ page import = "cn.js.fan.util.DateUtil"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import="java.text.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>考勤</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%@ include file="../inc/nocache.jsp"%>
<script src="../inc/common.js"></script>
<script>
function window_onload() {
	$("lateCount").innerHTML = $("lateCountSpan").innerHTML;
	$("beforeCount").innerHTML = $("beforeCountSpan").innerHTML;
}
</script>
</head>
<body onload="window_onload()">
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
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
String priv="read";
if (!privilege.isUserLogin(request))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = ParamUtil.get(request, "userName");
if (userName.equals("")) {
	out.print(StrUtil.Alert_Back("用户名不能为空！"));
	return;
}

if (!privilege.canAdminUser(request, userName)) {
	out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

UserDb ud = new UserDb();
ud = ud.getUserDb(userName);

// 翻月
int showyear,showmonth;
Calendar cal = Calendar.getInstance();
int curday = cal.get(cal.DAY_OF_MONTH);
int curhour = cal.get(cal.HOUR_OF_DAY);
int curminute = cal.get(cal.MINUTE);
int curmonth = cal.get(cal.MONTH);
int curyear = cal.get(cal.YEAR);

String strshowyear = request.getParameter("showyear");
String strshowmonth = request.getParameter("showmonth");
if (strshowyear!=null)
	showyear = Integer.parseInt(strshowyear);
else
	showyear = cal.get(cal.YEAR);
if (strshowmonth!=null)
	showmonth = Integer.parseInt(strshowmonth);
else
	showmonth = cal.get(cal.MONTH)+1;
		
cfgparser.parse("config_oa.xml");
Properties props = cfgparser.getProps();
String mbeginp = props.getProperty("morningbegin");
String mendp = props.getProperty("morningend");
String abeginp = props.getProperty("afternoonbegin");
String aendp = props.getProperty("afternoonend");
String[] strmbegin = mbeginp.split(":");
String[] strmend = mendp.split(":");
String[] strabegin = abeginp.split(":");
String[] straend = aendp.split(":");

int latevalue = Integer.parseInt(props.getProperty("latevalue"));
int[] mbegin = new int[2]; // 上午上班开始时间，0为小时，1为分钟
int[] mend = new int[2];
int[] abegin = new int[2];
int[] aend = new int[2];
for (int k=0; k<2; k++) {
	mbegin[k] = Integer.parseInt(strmbegin[k]);
	mend[k] = Integer.parseInt(strmend[k]);
	abegin[k] = Integer.parseInt(strabegin[k]);
	aend[k] = Integer.parseInt(straend[k]);
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1"><span class="right-title"><%=ud.getRealName()%>&nbsp;&nbsp;<%=showmonth%> 月 考勤表 &nbsp;&nbsp;上午<%=mbeginp%>-<%=mendp%> 下午<%=abeginp%>-<%=aendp%> 迟到或早退为相差 <%=latevalue%>分钟</span></td>
    </tr>
  </tbody>
</table>
<table width="98%" border="0" align="center">
  <tr>
    <td align="center">&nbsp;</td>
  </tr>
  <tr>
    <td align="center"><select name="showyear" onchange="var y = this.options[this.selectedIndex].value; window.location.href='?showyear=' + y + '&amp;userName=<%=StrUtil.UrlEncode(userName)%>';">
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
%>
    </td>
  </tr>
  <tr>
    <td align="center">
	<a href="kaoqin.jsp?userName=<%=StrUtil.UrlEncode(userName)%>&amp;showyear=<%=showyear%>&amp;showmonth=<%=showmonth%>">全部</a>
	<%
	BasicDataMgr bdm = new BasicDataMgr("kaoqin");
	String[][] r = bdm.getOptions("type");
	int len = r.length;
	for (int i=0; i<len; i++) {%>
		<a href="kaoqin.jsp?userName=<%=StrUtil.UrlEncode(userName)%>&showtype=<%=StrUtil.UrlEncode(r[i][1])%>&showyear=<%=showyear%>&showmonth=<%=showmonth%>"><%=r[i][0]%></a>
	<%}%>
	&nbsp;&nbsp;迟到次数：<span id="lateCount"></span>&nbsp;&nbsp;&nbsp;早退次数：<span id="beforeCount"></span></td>
  </tr>
</table>
<%
String sql;
// 获取本月考勤
String showtype = ParamUtil.get(request, "showtype");
if (showtype.equals(""))
	sql = "select id from kaoqin where name="+fchar.sqlstr(userName)+" and MONTH(myDate)="+showmonth+" and YEAR(myDate)="+showyear+" order by mydate asc";
else
	sql = "select id from kaoqin where name="+fchar.sqlstr(userName)+" and MONTH(myDate)="+showmonth+" and YEAR(myDate)="+showyear+" and type="+fchar.sqlstr(showtype)+" order by mydate asc";
int i = 1;

String direction="",type="",reason="",mydate="",strweekday="";
int id = -1;
int weekday=0;
Date dt = null;
int monthday = -1;
int monthdaycount = getDays(showmonth-1,showyear);//当前显示月份的天数
String[] wday = {"","日","一","二","三","四","五","六"};

boolean coloralt = true;//背景颜色交替
String backcolor = "#ffffff";
int myhour = 0;	//用于计算迟到时间
int myminute = 0;
int latecount = 0;//迟到次数
int beforecount = 0; //早退次数
int latehour = 0;
int lateminute = 0;
Calendar cld = Calendar.getInstance();

KaoqinDb kd = new KaoqinDb();
Iterator ir = kd.list(sql).iterator();
%>
<br />
<table class="tabStyle_1 percent80" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
  <tr align="center">
    <td class="tabStyle_1_title" width="8%">星期</td>
    <td class="tabStyle_1_title" width="13%">日期</td>
    <td class="tabStyle_1_title" width="13%">时间</td>
    <td class="tabStyle_1_title" width="19%">去向</td>
    <td class="tabStyle_1_title" width="18%">类型</td>
    <td class="tabStyle_1_title" width="29%">事由</td>
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
	  
		while (i<=monthdaycount)
		{
			if (monthday==i)
			{
				coloralt = !coloralt;
				if (coloralt)
					backcolor = "#eeeeee";
				else
					backcolor = "#ECFFDF";
				String oldbackcolor = backcolor;
				
				while (monthday==i) {
					backcolor = oldbackcolor;
					id = kd.getId();
					direction = kd.getDirection();
					type = kd.getType();
					reason = kd.getReason();
					
					mydate = DateUtil.format(kd.getMyDate(), "yyyy-MM-dd HH:mm:ss");
					// dt = rs.getDate("mydate");//这样取的话会丢失小时和分钟信息
					dt = formatter.parse(mydate);
					cld.setTime(dt);
					
					mydate = mydate.substring(11,19);
					weekday = cld.get(cld.DAY_OF_WEEK);
					strweekday = wday[weekday];
					// 计算是否迟到
					myhour = cld.get(cld.HOUR_OF_DAY);
					myminute = cld.get(cld.MINUTE);
					if (type.equals("考勤"))
					{
						int hbeginf,mbeginf,hendf,mendf;
						if (myhour<13) //上午
						{
							hbeginf = mbegin[0];
							mbeginf = mbegin[1];
							hendf = mend[0];
							mendf = mend[1];
						}
						else
						{
							hbeginf = abegin[0];
							mbeginf = abegin[1];
							hendf = aend[0];
							mendf = aend[1];
						}
							
						if (direction.equals("c"))
						{
							latehour = myhour-hbeginf;
							lateminute = myminute-mbeginf;
							if (lateminute>0) //计算本次迟到的分钟数
								lateminute = latehour*60+lateminute;
							else
								lateminute = latehour*60+lateminute;
							//System.out.println("hbeginf="+hbeginf);
							//System.out.println("myhour="+myhour);
							//System.out.println("myminute="+myminute);
							//System.out.println("mbeginf="+mbeginf);
							//System.out.println("lateminute="+lateminute);
							if (lateminute>latevalue)//如果大于阀值则认为是迟到
							{
								backcolor = "#FFCECA";
								latecount++;
							}
						}
						else
						{
							latehour = hendf-myhour;
							lateminute = mendf-myminute;
							if (lateminute>0) //计算本次迟到的分钟数
								lateminute = latehour*60+lateminute;
							else
								lateminute = latehour*60+lateminute;
							if (lateminute>latevalue)//如果大于阀值则认为是早退
							{
								backcolor = "#ffff00";
								beforecount++;
							}
						}
					}

					if (direction.equals("c"))
						direction = "到达单位";
					else
						direction = "离开单位";
				%>
  <tr align="center" bgcolor="<%=backcolor%>">
    <td width="8%" bgcolor="<%=backcolor%>"><%=strweekday%> </td>
    <td width="13%" bgcolor="<%=backcolor%>"><%=i%></td>
    <td width="13%" bgcolor="<%=backcolor%>"><%=mydate%></td>
    <td width="19%" bgcolor="<%=backcolor%>"><%=direction%></td>
    <td width="18%" bgcolor="<%=backcolor%>"><%=type%></td>
    <td width="29%" bgcolor="<%=backcolor%>"><%=reason%></td>
  </tr>
  <%
						if (ir.hasNext()) {
							kd = (KaoqinDb)ir.next();
							dt = kd.getMyDate();
							cld.setTime(dt);
							monthday = cld.get(cld.DAY_OF_MONTH);
						}
						else
							break;
				 }
			}
			else
			{
				cld.set(showyear,showmonth-1,i);
				weekday = cld.get(cld.DAY_OF_WEEK);
				strweekday = wday[weekday];
				if (weekday==1 || weekday==7)
					strweekday = "<font color=red>"+strweekday+"</font>";				
				%>
    <tr align="center">
      <td width="8%"><%=strweekday%></td>
      <td width="13%"><%=i%></td>
      <td width="13%">&nbsp;</td>
      <td width="19%">&nbsp;</td>
      <td width="18%">&nbsp;</td>
      <td width="29%">&nbsp;</td>
    </tr>
  <%
			}
			i++;
		}%>
</table>
<br />
<table width="98%" border="0" align="center">
  <tr>
    <td align="center" style="display:none">迟到次数：<span id="lateCountSpan"><%=latecount%></span>&nbsp;&nbsp;&nbsp;早退次数：<span id="beforeCountSpan"><%=beforecount%></span></td>
  </tr>
</table>
</body>
</html>
