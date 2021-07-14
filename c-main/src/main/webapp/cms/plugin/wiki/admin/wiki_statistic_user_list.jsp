<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="java.lang.*"%>
<%@ page import="cn.js.fan.module.cms.plugin.wiki.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%
String dir_code = ParamUtil.get(request, "dir_code");
if (dir_code.equals(""))
	dir_code = Leaf.CODE_WIKI;
Directory dir = new Directory();
Leaf leaf = dir.getLeaf(dir_code);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>后台用户发布和编辑维客文章统计</title>
<link href="../../../../common.css" rel="stylesheet" type="text/css">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../../../../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../../../../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../../../../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../../../../util/jscalendar/calendar-win2k-2.css"); </style>
<style type="text/css">
<!--
.style4 {
	color: #FFFFFF;
	font-weight: bold;
}
-->
</style>
<script src="../../../../inc/common.js"></script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
LeafPriv lp = new LeafPriv(Leaf.CODE_WIKI);
if (!lp.canUserExamine(privilege.getUser(request))) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%@ include file="wiki_inc_menu_top.jsp"%>
<script>
o("menu3").className="current";
</script>
<div class="spacerH"></div>
<%
	String sql = "select name,realname from users order by name";      
	JdbcTemplate rmconn = new JdbcTemplate();
	ResultIterator ri = rmconn.executeQuery(sql);
	ResultRecord rr = null;
	String name = "",realname = "",strdate = "";
	int CountByYear = 0,CountByMonth = 0,CountByToday = 0,CountByYesterday = 0,CountByBYesterday = 0;
	int editByYear = 0,editByMonth = 0,editByToday = 0,editByYesterday = 0,editByBYesterday = 0;
	String sqlCountByYear = "",sqlCountByMonth = "",sqlCountByToday = "",sqlCountByYesterday = "",sqlCountByBYesterday = ""; 
	
	String sDate = ParamUtil.get(request, "date");
	java.util.Date date = DateUtil.parse(sDate, "yyyy-MM-dd");
	if (date==null)
		date = new java.util.Date();
	
	String strToday = DateUtil.format(date,"yyyy-MM-dd");
	java.util.Date today = DateUtil.parse(strToday,"yyyy-MM-dd");
	
	String strMonth = DateUtil.format(date,"yyyy-MM");
	java.util.Date month = DateUtil.parse(strMonth,"yyyy-MM");
	
	String strYear = DateUtil.format(date,"yyyy");
	java.util.Date year = DateUtil.parse(strYear,"yyyy");
	
	int y = StrUtil.toInt(strYear);
	Calendar cal = Calendar.getInstance();
	cal.setTime(month);
	int m = cal.get(Calendar.MONTH)+1;
	cal.setTime(today);
	int d = cal.get(Calendar.DAY_OF_MONTH);	
%>
<table width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td>选择日期：
    <input type="text" id="date" name="date" size="10" value="<%=DateUtil.format(date, "yyyy-MM-dd")%>" onChange="window.location.href='wiki_statistic_user_list.jsp?date='+this.value">
<script type="text/javascript">
    Calendar.setup({
        inputField     :    "date", 
        ifFormat       :    "%Y-%m-%d",
        showsTime      :    false,
        singleClick    :    false,
        align          :    "Tl",
        step           :    1
    });
</script>	
	</td>
  </tr>
</table>
<br>
<table class="tabStyle_1 percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
  <thead>
    <tr>
      <td noWrap width="8%">用户名</td>
      <td width="9%" noWrap>当年发表</td>
      <td width="8%" noWrap>当年编辑</td>
      <td width="9%" noWrap>当月发表</td>
      <td width="10%" noWrap>当月编辑</td>
      <td width="10%" noWrap>前两天发表</td>
      <td width="10%" noWrap>前两天编辑</td>
      <td width="10%" noWrap>前一天发表</td>
      <td width="9%" noWrap>前一天编辑</td>
      <td width="8%" noWrap>当天发表</td>
      <td width="9%" noWrap>当天编辑</td>
    </tr>
  </thead>
  <tbody>
<%
      int CountByYearAll = 0;
	  int CountByMonthAll = 0;
	  int CountByBYesterdayAll = 0;
	  int CountByYesterdayAll = 0;
	  int CountByTodayAll = 0;
	  
      int editByYearAll = 0;
	  int editByMonthAll = 0;
	  int editByBYesterdayAll = 0;
	  int editByYesterdayAll = 0;
	  int editByTodayAll = 0;
	/*
	cms_wiki_doc_update中user_name字段为前台用户的用户名，注意不是nick
	document中的nick为前台用户的nick
	*/
	com.redmoon.forum.person.UserMgr um = new com.redmoon.forum.person.UserMgr();
	while (ri.hasNext()) {
		rr = (ResultRecord)ri.next();
		name = rr.getString(1);
		realname = rr.getString(2);
		
		com.redmoon.forum.person.UserDb fu = um.getUserDbByNick(name);
		if (fu!=null)
			name = fu.getName();
		
		int daysOfYear = DateUtil.getDaysOfYear(y);
		java.util.Date tempd = DateUtil.addDate(year, daysOfYear);
		tempd = DateUtil.addMinuteDate(tempd, -1);
		sqlCountByYear =
				" select count(*) from document d, cms_wiki_doc s where d.id=s.doc_id and examine=" + Document.EXAMINE_PASS + " and createDate>=? and createDate<=?" + " and nick = " + StrUtil.sqlstr(name);		
		ResultIterator ri_year = rmconn.executeQuery(sqlCountByYear, new Object[]{DateUtil.toLongString(year), DateUtil.toLongString(tempd)});
		ResultRecord rr_year = null;
		if (ri_year.hasNext()) {
			rr_year = (ResultRecord)ri_year.next();
			CountByYear = rr_year.getInt(1);
			CountByYearAll+=CountByYear;
		}
		
		sql =
				" select count(*) from cms_wiki_doc_update where check_status=" + WikiDocUpdateDb.CHECK_STATUS_PASSED + " and edit_date>=? and edit_date<=?" + " and user_name = " + StrUtil.sqlstr(name);		
		ri_year = rmconn.executeQuery(sqlCountByYear, new Object[]{year, tempd});
		if (ri_year.hasNext()) {
			rr_year = (ResultRecord)ri_year.next();
			editByYear = rr_year.getInt(1);
			editByYearAll+=editByYear;
		}		
		
		int daysOfMonth = DateUtil.getDayCount(y, m-1);
		tempd = DateUtil.addDate(month, daysOfMonth);
		tempd = DateUtil.addMinuteDate(tempd, -1);
		sqlCountByMonth =
				" select count(*) from document d, cms_wiki_doc s where d.id=s.doc_id and examine=" + Document.EXAMINE_PASS + " and createDate>=? and createDate<=?" + " and nick = " + StrUtil.sqlstr(name);
		ResultIterator ri_month = rmconn.executeQuery(sqlCountByMonth, new Object[] {DateUtil.toLongString(month), DateUtil.toLongString(tempd)});
		ResultRecord rr_month = null;
		if (ri_month.hasNext()) {
			rr_month = (ResultRecord)ri_month.next();
			CountByMonth = rr_month.getInt(1);
			CountByMonthAll+=CountByMonth;
		}
		ri_month = rmconn.executeQuery(sql, new Object[]{month, tempd});
		if (ri_month.hasNext()) {
			rr_month = (ResultRecord)ri_month.next();
			editByMonth = rr_month.getInt(1);
			editByMonthAll+=editByMonth;
		}
		
		tempd = DateUtil.addHourDate(today, 24);
		tempd = DateUtil.addMinuteDate(tempd, -1);
		sqlCountByToday =
				" select count(*) from document d, cms_wiki_doc s where d.id=s.doc_id and examine=" + Document.EXAMINE_PASS + " and createDate>=? and createDate<=?" + " and nick = " + StrUtil.sqlstr(name);
		ResultIterator ri_today = rmconn.executeQuery(sqlCountByToday, new Object[]{DateUtil.toLongString(today), DateUtil.toLongString(tempd)});
		ResultRecord rr_today = null;
		if (ri_today.hasNext()) {
			rr_today = (ResultRecord)ri_today.next();
			CountByToday = rr_today.getInt(1);
			CountByTodayAll+=CountByToday;
		}
		ri_today = rmconn.executeQuery(sql, new Object[]{today, tempd});
		if (ri_today.hasNext()) {
			rr_today = (ResultRecord)ri_today.next();
			editByToday = rr_today.getInt(1);
			editByTodayAll+=editByToday;
		}				
		
		sqlCountByYesterday= " select count(*) from document d, cms_wiki_doc s where d.id=s.doc_id and examine=" + Document.EXAMINE_PASS + " and createDate >= " + StrUtil.sqlstr(Long.toString(today.getTime()-24*60*60000)) + " and createDate < " + StrUtil.sqlstr(Long.toString(today.getTime())) + " and nick = " + StrUtil.sqlstr(name);
		ResultIterator ri_yesterday = rmconn.executeQuery(sqlCountByYesterday);
		ResultRecord rr_yesterday = null;
		if (ri_yesterday.hasNext()) {
			rr_yesterday = (ResultRecord)ri_yesterday.next();
			CountByYesterday = rr_yesterday.getInt(1);
			CountByYesterdayAll+=CountByYesterday;
		}
		ri_yesterday = rmconn.executeQuery(sql, new Object[]{new java.util.Date(today.getTime()-24*60*60000), tempd});
		if (ri_yesterday.hasNext()) {
			rr_yesterday = (ResultRecord)ri_yesterday.next();
			editByYesterday = rr_yesterday.getInt(1);
			editByYesterdayAll+=editByYesterday;
		}		
		
		sqlCountByBYesterday= " select count(*) from document d, cms_wiki_doc s where d.id=s.doc_id and examine=" + Document.EXAMINE_PASS + " and createDate >= " + StrUtil.sqlstr(Long.toString(today.getTime()-2*24*60*60000)) + " and createDate < " + StrUtil.sqlstr(Long.toString(today.getTime()-24*60*60000)) + " and nick = " + StrUtil.sqlstr(name);
		ResultIterator ri_byesterday = rmconn.executeQuery(sqlCountByBYesterday);
		ResultRecord rr_byesterday = null;
		if (ri_byesterday.hasNext()) {
			rr_byesterday = (ResultRecord)ri_byesterday.next();
			CountByBYesterday = rr_byesterday.getInt(1);
			CountByBYesterdayAll+=CountByBYesterday;
		}
		ri_byesterday = rmconn.executeQuery(sql, new Object[]{new java.util.Date(today.getTime()-2*24*60*60000), tempd});
		if (ri_byesterday.hasNext()) {
			rr_byesterday = (ResultRecord)ri_byesterday.next();
			editByBYesterday = rr_byesterday.getInt(1);
			editByBYesterdayAll+=editByBYesterday;
		}		
%>
    <tr onMouseOver="this.className='tbg1sel'" onMouseOut="this.className='tbg1'" class="tbg1">
      <td>&nbsp;<a href="wiki_statistic_user.jsp?userName=<%=StrUtil.UrlEncode(name)%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>"><%=realname%></a></td>
      <td><%=CountByYear%></td>
      <td><%=editByYear%></td>
      <td><%=CountByMonth%></td>
      <td><%=editByMonth%></td>
      <td><%=CountByBYesterday%></td>
      <td><%=editByBYesterday%></td>
      <td><%=CountByYesterday%></td>
      <td><%=editByYesterday%></td>
      <td><%=CountByToday%></td>
      <td><%=editByToday%></td>
    </tr>
<%}

WikiStatistic st = new WikiStatistic();
%>	
    <tr onMouseOver="this.className='tbg1sel'" onMouseOut="this.className='tbg1'" class="tbg1">
      <td height="22" bgcolor="#EFF1FE">合计：</td>
      <td bgcolor="#EFF1FE"><%=CountByYearAll%></td>
      <td bgcolor="#EFF1FE"><%=editByYearAll%></td>
      <td bgcolor="#EFF1FE"><%=CountByMonthAll%></td>
      <td bgcolor="#EFF1FE"><%=editByMonthAll%></td>
      <td bgcolor="#EFF1FE"><%=CountByBYesterdayAll%></td>
      <td bgcolor="#EFF1FE"><%=editByBYesterdayAll%></td>
      <td bgcolor="#EFF1FE"><%=CountByYesterdayAll%></td>
      <td bgcolor="#EFF1FE"><%=editByYesterdayAll%></td>
      <td bgcolor="#EFF1FE"><%=CountByTodayAll%></td>
      <td bgcolor="#EFF1FE"><%=editByTodayAll%></td>
    </tr>
    <tr onMouseOver="this.className='tbg1sel'" onMouseOut="this.className='tbg1'" class="tbg1">
      <td height="22" colspan="11"><strong>总计发表：</strong><%=st.getAllDocCount()%></td>
    </tr>
  </tbody>
</table>
</body>
</html>