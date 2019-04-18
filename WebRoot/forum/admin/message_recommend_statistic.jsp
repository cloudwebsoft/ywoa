<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="java.util.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
	if (!privilege.isMasterLogin(request)) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	
	String beginDate = "";
	String endDate = "";
	String op = ParamUtil.get(request, "op");
	if(op.equals("doSta")) {
		beginDate = ParamUtil.get(request, "beginDate");
		endDate = ParamUtil.get(request, "endDate");
	} else {
		Calendar c = Calendar.getInstance();
		endDate = StrUtil.FormatDate(c.getTime(), "yyyy-MM-dd HH:mm:ss");
		c.add(Calendar.MONTH, -1);
		beginDate = StrUtil.FormatDate(c.getTime(), "yyyy-MM-dd HH:mm:ss");
	}
	
	String sql = "select count(*) as s, msg_id from sq_message_recommend";
	String query = "";
	if (!beginDate.equals("")) {
		query += " where submit_date>=?";
	}
	if(query.equals("")) {
		query += " where submit_date<=?";
	} else {
		query += " and submit_date<=?";
	}
	sql += query;
	sql += " group by msg_id order by s desc";
	//System.out.println(sql + '\n');
	
	Paginator paginator = new Paginator(request);
	int curpage = paginator.getCurPage();
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = null;
	long total = 0;
	int pageSize = 20;
	Calendar c = Calendar.getInstance();	
	if (beginDate.equals("") && endDate.equals("")) {
		ri = jt.executeQuery(sql, new Object[] {c.getTime()});
		total = ri.size();
		ri = jt.executeQuery(sql, new Object[] {c.getTime()}, total, curpage, pageSize);
	} else if (beginDate.equals("") && !endDate.equals("")) {
		java.util.Date d = DateUtil.parse(endDate, "yyyy-MM-dd HH:mm:ss");
		ri = jt.executeQuery(sql, new Object[] {d});
		total = ri.size();
		ri = jt.executeQuery(sql, new Object[] {d}, total, curpage, pageSize);
	} else if (!beginDate.equals("") && endDate.equals("")) {
		java.util.Date d = DateUtil.parse(beginDate, "yyyy-MM-dd HH:mm:ss");
		ri = jt.executeQuery(sql, new Object[] {beginDate, c.getTime()});
		total = ri.size();
		ri = jt.executeQuery(sql, new Object[] {beginDate, c.getTime()}, total, curpage, pageSize);
	} else {
		java.util.Date d = DateUtil.parse(beginDate, "yyyy-MM-dd HH:mm:ss");
		java.util.Date d2 = DateUtil.parse(endDate, "yyyy-MM-dd HH:mm:ss");
		ri = jt.executeQuery(sql, new Object[] {d, d2});
		total = ri.size();
		ri = jt.executeQuery(sql, new Object[] {d, d2}, total, curpage, pageSize);
	}
	paginator.init(total, pageSize);
	int totalpages = paginator.getTotalPages();
	if (totalpages == 0) {
		curpage = 1;
		totalpages = 1;
	}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="x-ua-compatible" content="ie=7" />
<title><lt:Label res="res.label.forum.admin.message_recommend_statistic" key="message_recommend_statistic"/></title>
<LINK href="../../cms/default.css" type=text/css rel=stylesheet>
<script src="../../inc/common.js"></script>
<script type="text/javascript" src="../../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../../util/jscalendar/calendar-win2k-2.css");</style>
</head>
<body>
<%@ include file="message_recommend_nav.jsp"%>
<script>
$("menu1").className="active";
</script>
<br />
<form action="message_recommend_statistic.jsp?op=doSta" method="post">
<table width="550" border='0' cellpadding='0' cellspacing='0' align="center">
  <tr>
    <td width="70" align="right"><lt:Label res="res.label.forum.admin.message_recommend_statistic" key="begin_date"/></td>
    <td width="180" align="left"><input type="text" id="beginDate" name="beginDate" value="<%=beginDate%>"></td>
    <td width="70" align="right"><lt:Label res="res.label.forum.admin.message_recommend_statistic" key="end_date"/></td>
    <td width="180" align="left"><input type="text" id="endDate" name="endDate" value="<%=endDate%>"></td>
    <td><input type="submit" value='<lt:Label res="res.label.forum.admin.message_recommend_statistic" key="statistic"/>' style="height:24px;font-size:12px" /></td>
  </tr>
</table>
<script type="text/javascript">
    Calendar.setup({
        inputField     :    "beginDate",
        ifFormat       :    "%Y-%m-%d %H:%M:00",
        showsTime      :    true,
        singleClick    :    false,
        align          :    "Tl",
        step           :    1
    });

    Calendar.setup({
        inputField     :    "endDate",
        ifFormat       :    "%Y-%m-%d %H:%M:00",
        showsTime      :    true,
        singleClick    :    false,
        align          :    "Tl",
        step           :    1
    });
</script>
<br>
</form>

<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0'>
  <tr>
    <td align="right"><%=paginator.getPageStatics(request)%></td>
  </tr>
</table>
<table width="98%" align="center" border='0' cellpadding='0' cellspacing='0' class="frame_gray">
	<tr>
   	  <td class="thead"><lt:Label res="res.label.forum.admin.message_recommend_statistic" key="id"/></td>
        <td class="thead"><lt:Label res="res.label.forum.admin.message_recommend_statistic" key="title"/></td>
        <td class="thead"><lt:Label res="res.label.forum.admin.message_recommend_statistic" key="recommend_times"/></td>
	</tr>
<%
	MsgDb md = null;
	MsgMgr mm = new MsgMgr();
	while (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		md = mm.getMsgDb(rr.getLong(2));
		String title = md.getTitle();
%>
    <tr>
    	<td width="120" height="22" align="center"><%=rr.getLong(2)%></td>
        <td><%=title%></td>
        <td align="center" width="120"><%=rr.getInt(1)%></td>
	</tr>
<%
	}
%>
</table>
<table width="98%" align="center" border='0' cellpadding='0' cellspacing='0'>
	<tr>
    	<td align="right">
<%
	String querystr = "";
	out.print(paginator.getCurPageBlock("message_recommend_statistic.jsp?" + querystr));
%>
        </td>
    </tr>
</table>
</body>
</html>