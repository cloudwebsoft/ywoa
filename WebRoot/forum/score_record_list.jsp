<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.base.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<title><lt:Label res="res.label.forum.score_transfer" key="score_record"/> - <%=Global.AppName%></title>
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
</head>
<body>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
  <table height="25" cellspacing="0" cellpadding="1" width="98%" align="center" border="1" class="tableCommon">
    <tbody>
      <tr>
        <td><img src="images/userinfo.gif" width="9" height="9" />&nbsp;
            <lt:Label res="res.label.forum.inc.position" key="cur_position"/>
            <a href="<%=request.getContextPath()%>/forum/index.jsp">
            <lt:Label res="res.label.forum.inc.position" key="forum_home"/>
            </a>&nbsp;<b>&raquo;</b>&nbsp;<a href="<%=request.getContextPath()%>/usercenter.jsp">
            <lt:Label res="res.label.forum.menu" key="user_center"/>
            </a></td>
      </tr>
    </tbody>
  </table>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<div id="newdiv" name="newdiv">
<%
	// 安全验证
	String querystring = StrUtil.getNullString(request.getQueryString());
	String privurl=request.getRequestURL()+"?"+StrUtil.UrlEncode(querystring,"utf-8");
	if (!privilege.isUserLogin(request)) {
		response.sendRedirect("info.jsp?op=login&info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "err_not_login")));
		return;
	}
	
	String userName = ParamUtil.get(request, "userName");
	if (userName.equals(""))
		userName = privilege.getUser(request);
	else {
		if (!privilege.isMasterLogin(request)) {
			response.sendRedirect("info.jsp?info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
	
	}
	String operate = ParamUtil.get(request, "operate");
	String moneyCode = ParamUtil.get(request, "moneyCode");
	String beginDate = ParamUtil.get(request, "beginDate");
	String endDate = ParamUtil.get(request, "endDate");
	if (SQLFilter.isValidSqlParam(moneyCode) && SQLFilter.isValidSqlParam(operate))
		;
	else {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "err_param")));
		return;
	}
	String sql = "";
	if (operate.equals(""))
		operate = ScoreRecordDb.OPERATION_PAY;
	if (operate.equals(ScoreRecordDb.OPERATION_PAY)) {
		sql = "select id from sq_score_record where (buyer="+StrUtil.sqlstr(userName) + " or seller=" + StrUtil.sqlstr(userName)  + ") and operation=" + StrUtil.sqlstr(ScoreRecordDb.OPERATION_PAY);
	}
	else {
		sql = "select id from sq_score_record where buyer="+StrUtil.sqlstr(userName) + " and operation=" + StrUtil.sqlstr(operate);
	}
	if (!moneyCode.equals(""))
		sql += " and from_score=" + StrUtil.sqlstr(moneyCode);
		
	if (!beginDate.equals("")) {
		java.util.Date bDate = DateUtil.parse(beginDate, "yyyy-MM-dd HH:mm:ss");
		String sbDate = DateUtil.toLongString(bDate);
		sql += " and lydate>=" + StrUtil.sqlstr(sbDate);
	}
	
	if (!beginDate.equals("")) {
		java.util.Date eDate = DateUtil.parse(endDate, "yyyy-MM-dd HH:mm:ss");
		String sDate = DateUtil.toLongString(eDate);
		sql += " and lydate<=" + StrUtil.sqlstr(sDate);
	}
		
	sql += " order by lydate desc";
	
	// out.print(sql);

	ScoreRecordDb srd = new ScoreRecordDb();
	int pagesize = 20;
	Paginator paginator = new Paginator(request);
	int curpage = paginator.getCurPage();
				
	ListResult lr = srd.listResult(sql, curpage, pagesize);
	int total = lr.getTotal();
	Vector v = lr.getResult();
	Iterator ir = null;
	if (v!=null)
		ir = v.iterator();
	paginator.init(total, pagesize);
	// 设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if (totalpages==0) {
		curpage = 1;
		totalpages = 1;
	}
%>
  <div class="tableTitle">
    <lt:Label res="res.label.forum.score_transfer" key="score_record"/>
  </div> 
  <TABLE class="tableCommon" align=center cellPadding=0 cellSpacing=1>
    <TBODY>
	  <form name="fmFilter" action="score_record_list.jsp" method="get">
      <TR>
        <TD height=23 colspan="7" align="left" bgcolor="#FFFFFF">
		&nbsp;
		<lt:Label res="res.label.forum.score_transfer" key="moneyCode"/>
		  <select name="moneyCode">
		  <option value=""><lt:Label key="wu"/></option>
<%	  
        ScoreMgr sm = new ScoreMgr();
        Iterator scoreIr = sm.getAllScore().iterator();
        while (scoreIr.hasNext()) {
            ScoreUnit su = (ScoreUnit) scoreIr.next();
            if (su.isExchange()) {
%>
			<option value="<%=su.getCode()%>"><%=su.getName()%></option>
<%	  
          }
      }
%>		</select>
		  <lt:Label res="res.label.forum.score_transfer" key="beginDate"/>
		  <input type="text" id="beginDate" name="beginDate" size="20">
		  <lt:Label res="res.label.forum.score_transfer" key="endDate"/>
		  &nbsp;
<input type="text" id="endDate" name="endDate" size="20">		
<script type="text/javascript">
    function catcalc(cal) {
        var date = cal.date;
        var time = date.getTime()
        // use the _other_ field
        var field = document.getElementById("endDate");
        time += 31*Date.DAY; // add one week
        var date2 = new Date(time);
        field.value = date2.print("%Y-%m-%d %H:%M:00");
    }

    Calendar.setup({
        inputField     :    "beginDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d %H:%M:00",       // format of the input field
        showsTime      :    true,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1,                // show all years in drop-down boxes (instead of every other year as default)
		onUpdate       :    catcalc
    });

    Calendar.setup({
        inputField     :    "endDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d %H:%M:00",       // format of the input field
        showsTime      :    true,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
		
	fmFilter.moneyCode.value = "<%=moneyCode%>";
	fmFilter.beginDate.value = "<%=beginDate%>";
	fmFilter.endDate.value = "<%=endDate%>";
</script>
	<input type="submit" value="<lt:Label key="ok"/>">		</TD>
      </TR>
	  </form>
  </TBODY></TABLE>
  <TABLE class="tableCommon" width="92%" border=0 align=center cellPadding=0 cellSpacing=0>		
      <thead> 
      <TD width=84 height=23 align="center"><lt:Label res="res.label.forum.score_transfer" key="from_user_name"/></TD>
        <TD width=86 height=23 align="center"><lt:Label res="res.label.forum.score_transfer" key="to_use_name"/></TD>
        <TD width=118 height=23 align="center"><lt:Label res="res.label.forum.score_transfer" key="date"/></TD>
        <TD width=123 height="23" align="center"><lt:Label res="res.label.forum.score_transfer" key="value"/></TD>
        <TD width=157 align="center"><lt:Label res="res.label.forum.score_transfer" key="operation"/></TD>
        <TD width=173 align="center"><lt:Label res="res.label.forum.score_transfer" key="op_type"/></TD>
        <TD width=133 align="center"><lt:Label res="res.label.forum.score_transfer" key="msg"/></TD>
      </thead>
<%		
	String lydate="", buyer="", seller="", fromScore="", toScore="", operation="";
	double fromValue=0, toValue=0;
	UserMgr um = new UserMgr();
	UserDb fud = null;
	UserDb tud = null;
	String toNick = "", fromNick = "";
	while (ir.hasNext()) {
		srd = (ScoreRecordDb)ir.next();
		if (srd.getBuyer().equals(IPluginScore.SELLER_SYSTEM)) {
			fromNick = "System";
		}
		else {
			fud = um.getUser(srd.getBuyer());
			fromNick = fud.getNick();
		}
		
		if (srd.getSeller().equals(IPluginScore.SELLER_SYSTEM)) {
			toNick = "System";
		}
		else {
			tud = um.getUser(srd.getSeller());
			toNick = tud.getNick();
		}
		
		lydate = srd.getLydate();
		
		fromScore = srd.getFromScore();
		ScoreUnit pu = sm.getScoreUnit(fromScore);
		toValue = srd.getToValue();
%>
      <TR bgColor=#f8f8f8> 
        <TD width=84 height=23 align="center"><%=fromNick%></TD>
        <TD width=86 height=23 align="center"><%=toNick%></TD>
        <TD width=118 height=23 align="center"><%=ForumSkin.formatDateTime(request, DateUtil.parse(lydate))%></TD>
        <TD height="23" align="left">&nbsp;<%=pu.getName()%> <%=toValue%></TD>
        <TD width=157 height=23 align="center">
		<%if (srd.getOperation().equals(ScoreRecordDb.OPERATION_TRANSFER)) {%>
		<lt:Label res="res.label.forum.score_transfer" key="score_transfer"/>
		<%}else if (srd.getOperation().equals(ScoreRecordDb.OPERATION_EXCHANGE)) {%>
		<lt:Label res="res.label.forum.score_exchange" key="score_exchange"/>
		<%}else if (srd.getOperation().equals(ScoreRecordDb.OPERATION_PAY)) {%>
		<lt:Label res="res.label.forum.score_transfer" key="score_pay"/>
		<%}else{%>
		<%=srd.getOperation()%>		
		<%}%>		</TD>
        <TD width=173 align="center"><%=ScoreRecordDb.getSysOperateDesc(request, srd.getOpType())%></TD>
        <TD width=133 align="center">
		<%if (srd.getMsgId()!=ScoreRecordDb.MSG_ID_NONE) {%>
		<a target=_blank href="<%=ForumPage.getShowTopicPage(request, 1, 0, srd.getMsgId(), 1, "")%>"><%=srd.getMsgId()%></a>
		<%}%>		</TD>
      </TR>
<%
	}
%>
    </TBODY>
  </TABLE>
  <table width="90%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
    <tr>
      <td height="23"><div align="right">
          <%
	  String querystr = "operate=" + operate + "&moneyCode=" + moneyCode + "&beginDate=" + beginDate + "&endDate=" + endDate;
 	  out.print(paginator.getCurPageBlock("score_record_list.jsp?"+querystr));
	%>
      </div></td>
    </tr>
  </table>
</div>
<%@ include file="inc/footer.jsp"%>
</div>
</body>
</html>
