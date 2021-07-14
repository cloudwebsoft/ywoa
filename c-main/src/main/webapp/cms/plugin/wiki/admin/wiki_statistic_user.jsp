<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.module.pvg.*" %>
<%@ page import="cn.js.fan.module.cms.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="cn.js.fan.module.cms.plugin.wiki.*" %>
<%@ page import="java.lang.*"%>
<%@ page import="java.util.*"%>
<%
String dir_code = ParamUtil.get(request, "dir_code");
if (dir_code.equals(""))
	dir_code = com.redmoon.oa.fileark.Leaf.CODE_WIKI;
Directory dir = new Directory();
Leaf leaf = dir.getLeaf(dir_code);
%>
<html>
<head>
<title>发布文章统计</title>
<link href="../../../../common.css" rel="stylesheet" type="text/css">
<link href="../../../default.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script src="../../../../inc/common.js"></script>
<style type="text/css">
<!--
.style4 {
	color: #FFFFFF;
	font-weight: bold;
}
-->
</style>
</head>
<body bgcolor="#FFFFFF" text="#000000">
<jsp:useBean id="usermgr" scope="page" class="cn.js.fan.module.pvg.UserMgr"/>
<jsp:useBean id="privilege" scope="page" class="cn.js.fan.module.pvg.Privilege"/>
<%
	if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN))	{
		out.println(StrUtil.makeErrMsg(privilege.MSG_INVALID,"red","green"));
		return;
	}
	String userName = ParamUtil.get(request,"userName");
	if (userName.equals(""))
	{
		out.println("用户名不能为空!");
		return;
	}
	User ud = new User();
	ud = ud.getUser(userName);
	String realName = userName;
	
	com.redmoon.forum.person.UserDb fu = new com.redmoon.forum.person.UserDb();
	fu = fu.getUser(userName);
	if (fu.isLoaded()) {
		userName = fu.getName();
		ud = ud.getUser(fu.getNick());
		realName = ud.getRealName();
	}
%>
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
%>
<DIV id="tabBar">
  <div class="tabs">
    <ul>
      <li id="menu1"><a href="wiki_update_list.jsp?dir_code=<%=StrUtil.UrlEncode(leaf.getCode())%>">待审</a></li>
      <li id="menu2"><a href="wiki_list.jsp?dir_code=<%=StrUtil.UrlEncode(leaf.getCode())%>">词条</a></li>
      <li id="menu3"><a href="wiki_statistic_user_list.jsp">发布统计</a></li>
      <li id="menu4"><a href="wiki_doc_edit_rank.jsp">编辑排行</a></li>	  
      <li id="menu5"><a href="manager.jsp?dir_code=<%=StrUtil.UrlEncode(leaf.getCode())%>">配置</a></li>
    </ul>
  </div>
</DIV>
<script>
$("menu3").className="active"; 
</script>
<%          
	int countByDay = 0 ;
	String countByMonth = "",countByYear = "";
	String sqlCountByDay = "",sqlCountByMonth = "",sqlCountByYear = ""; 	
%>
<br>
<table width="98%"  border="0" align="center" cellpadding="0" cellspacing="0" class="p9">
    <tr>
      <td align="center">
	    <%=realName%>&nbsp;<%=showyear%>年<%=showmonth%>月发布编辑统计
	    <select name="showyear" onChange="var y = this.options[this.selectedIndex].value; window.location.href='?userName=<%=StrUtil.UrlEncode(userName)%>&showyear=' + y;">
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
						out.print("<a href='?userName="+StrUtil.UrlEncode(userName)+"&showyear="+showyear+"&showmonth="+i+"'><font color=red>"+i+"月</font></a>&nbsp;");
					else
						out.print("<a href='?userName="+StrUtil.UrlEncode(userName)+"&showyear="+showyear+"&showmonth="+i+"'>"+i+"月</a>&nbsp;");
				
				}
		  %> 
      </td>
    </tr>
</table>
<br>
<table style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" cellSpacing="0" cellPadding="3" width="61%" align="center">
  <tbody>
    <tr>
      <td class="thead" style="PADDING-LEFT: 10px" noWrap width="20%">星期</td>
      <td width="22%" noWrap class="thead" style="PADDING-LEFT: 10px"><img src="../../../images/tl.gif" align="absMiddle" width="10" height="15">日期</td>
      <td width="29%" noWrap class="thead" style="PADDING-LEFT: 10px"><img src="../../../images/tl.gif" align="absMiddle" width="10" height="15">发布文章</td>
      <td width="29%" noWrap class="thead" style="PADDING-LEFT: 10px"><img src="../../../images/tl.gif" align="absMiddle" width="10" height="15">编辑文章</td>
    </tr>
<%
	int i = 1;
	String content="",mydate="",strweekday="";
	int id = -1;
	int weekday=0;
	Date dt = null;
	int monthday = -1;
	int monthdaycount = getDays(showmonth-1,showyear);//当前显示月份的天数
	String[] wday = {"","日","一","二","三","四","五","六"};
	boolean rsnotend = true;
	boolean coloralt = true;//背景颜色交替
	String backcolor = "#ffffff";
	Calendar cld = Calendar.getInstance();
	java.util.Date date = null;
	int editByDay = 0;
	JdbcTemplate rmconn = new JdbcTemplate();	
	while (i<=monthdaycount) {	
		cld.set(showyear,showmonth-1,i);
		weekday = cld.get(cld.DAY_OF_WEEK);
		strweekday = wday[weekday];
		if (weekday==1 || weekday==7) {
			strweekday = "<font color=red>"+strweekday+"</font>";	
		}

		date = new java.util.Date(showyear-1900,showmonth-1,i);
		String strDay = DateUtil.format(date,"yyyy-MM-dd");
		java.util.Date day = DateUtil.parse(strDay,"yyyy-MM-dd");
        sqlCountByDay= " select count(*) from document d, cms_wiki_doc s where d.id=s.doc_id and d.examine=" + Document.EXAMINE_PASS + " and createDate >= " + StrUtil.sqlstr(Long.toString(day.getTime())) + " and createDate < " + StrUtil.sqlstr(Long.toString(day.getTime()+24*60*60000)) + " and nick = " + StrUtil.sqlstr(userName);
		ResultIterator ri_day = rmconn.executeQuery(sqlCountByDay);
		ResultRecord rr_day = null;
		if (ri_day.hasNext()) {
			rr_day = (ResultRecord)ri_day.next();
			countByDay = rr_day.getInt(1);
		}
		
        sqlCountByDay = " select count(*) from cms_wiki_doc_update where check_status=" + WikiDocUpdateDb.CHECK_STATUS_PASSED + " and edit_date>=? and edit_date<? and user_name = " + StrUtil.sqlstr(userName);
		ri_day = rmconn.executeQuery(sqlCountByDay, new Object[]{day, new java.util.Date(day.getTime()+24*60*60000)});
		if (ri_day.hasNext()) {
			rr_day = (ResultRecord)ri_day.next();
			editByDay = rr_day.getInt(1);
		}		
%>
    <tr onMouseOver="this.className='tbg1sel'" onMouseOut="this.className='tbg1'" class="tbg1">
      <td style="PADDING-LEFT: 10px"><%=strweekday%></td>
      <td style="PADDING-LEFT: 10px"><%=i%></td>
      <td style="PADDING-LEFT: 10px"><%if(countByDay == 0){%><%=countByDay%><%}else{%><font color="#FF0000"><%=countByDay%></font><%}%></td>
      <td style="PADDING-LEFT: 10px"><%if(editByDay == 0){%><%=editByDay%><%}else{%><font color="#FF0000"><%=editByDay%></font><%}%></td>
    </tr>
<%
		i++;
	}	
	date = new java.util.Date(showyear-1900,showmonth-1,1);
	String strMonth = DateUtil.format(date,"yyyy-MM");
	java.util.Date month = DateUtil.parse(strMonth,"yyyy-MM");
	
	String strYear = DateUtil.format(date,"yyyy");
	java.util.Date year = DateUtil.parse(strYear,"yyyy");
	
	long k = monthdaycount;
	sqlCountByMonth = "select count(*) from document d, cms_wiki_doc s where d.id=s.doc_id and d.examine=" + Document.EXAMINE_PASS + " and createDate >= " + StrUtil.sqlstr(Long.toString(month.getTime())) + " and createDate < " + StrUtil.sqlstr(Long.toString(month.getTime() + k*24*60*60000)) + " and nick = " + StrUtil.sqlstr(userName);
	ResultIterator ri_month = rmconn.executeQuery(sqlCountByMonth);
	ResultRecord rr_month = null;
	if (ri_month.hasNext()) {
		rr_month = (ResultRecord)ri_month.next();
		countByMonth = rr_month.getString(1);
	}	
	
	monthdaycount = 0;
	for(int j = 0 ; j < 12 ; j++){
		monthdaycount += getDays(j,showyear);
	}
	k = monthdaycount;
	sqlCountByYear = "select count(*) from document d, cms_wiki_doc s where d.id=s.doc_id and d.examine=" + Document.EXAMINE_PASS + " and createDate >= " + StrUtil.sqlstr(Long.toString(year.getTime())) + " and createDate < " + StrUtil.sqlstr(Long.toString(year.getTime() + k*24*60*60000)) + " and nick = " + StrUtil.sqlstr(userName);
	ResultIterator ri_year = rmconn.executeQuery(sqlCountByYear);
	ResultRecord rr_year = null;
	if (ri_year.hasNext()) {
		rr_year = (ResultRecord)ri_year.next();
		countByYear = rr_year.getString(1);
	}	
%>
    <tr class="row" style="BACKGROUND-COLOR: #ffffff">
      <td style="PADDING-LEFT: 10px">&nbsp;</td>
      <td style="PADDING-LEFT: 10px">&nbsp;</td>
      <td style="PADDING-LEFT: 10px">月总计：<%=countByMonth%>&nbsp;&nbsp;年总计：<%=countByYear%></td>
      <td style="PADDING-LEFT: 10px">&nbsp;</td>
    </tr>
  </tbody>
</table>
</body>
</html>