<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="cn.js.fan.base.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.score.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<link rel="stylesheet" href="../../common.css">
<LINK href="default.css" type=text/css rel=stylesheet>
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Score Statistic</title>
<script type="text/javascript" src="../../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../../util/jscalendar/calendar-win2k-2.css"); </style>
</head>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head">
      <lt:Label res="res.label.forum.admin.score" key="score_statistic"/>
    </td>
  </tr>
</table>
<br>
<%
	int pagesize = 30;
	Paginator paginator = new Paginator(request);
	int curpage = paginator.getCurPage();	
		
	ScoreLogDb sld = new ScoreLogDb();
	String moneyCode = ParamUtil.get(request, "moneyCode");
	if (moneyCode.equals("")) {
		// moneyCode = Gold.code;
	}
	String beginDate = ParamUtil.get(request, "beginDate");
	String endDate = ParamUtil.get(request, "endDate");
	
	if (op.equals("")) {
		if (beginDate.equals("")) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_MONTH, 1);
			
			beginDate = DateUtil.format(cal, "yyyy-MM-dd 00:00:00");
		}
		if (endDate.equals("")) {
			endDate = DateUtil.format(new java.util.Date(), "yyyy-MM-dd 00:00:00");
		}
	}
	
	String sql = "select user_name, sum(score_value) as s from " + sld.getTable().getName();
	if (!moneyCode.equals(""))
		sql += " where score_code=" + StrUtil.sqlstr(moneyCode);
	sql += " group by user_name order by s desc";
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = jt.executeQuery(sql, curpage, pagesize);
	
	paginator.init(ri.size(), pagesize);
	// 设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if (totalpages==0) {
		curpage = 1;
		totalpages = 1;
	}
%>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead"><lt:Label res="res.label.forum.admin.score" key="score_rank"/></td>
  </tr>
  <tr> 
    <td valign="top"><br>
      <TABLE width="92%" border=0 align=center cellPadding=0 cellSpacing=1>
      <TBODY>
      <form name="fmFilter" action="score_statistic.jsp?op=search" method="post">
        <TR>
          <TD height=23 colspan="7" align="center">&nbsp;
            <lt:Label res="res.label.forum.score_transfer" key="moneyCode"/>
            <select name="moneyCode">
                <option value="">
                  <lt:Label key="all"/>
                </option>
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
%>
              </select>
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
            <input name="submit" type="submit" value="<lt:Label key="ok"/>"></TD>
        </TR>
      </form>
  </TBODY>
    </TABLE>
      <br>
      <table width="95%" border="0" align="center">
      <tr>
        <td align="right"><%=paginator.getPageStatics(request)%></td>
      </tr>
    </table>
      <table width="82%"  border="0" align="center" cellpadding="3" cellspacing="1" bgcolor="#CCCCCC">
      <tr align="center" bgcolor="#F8F7F9">
        <td width="38%" height="24" bgcolor="#EFEBDE">
          <lt:Label res="res.label.forum.admin.score" key="nick"/>
        </td>
        <td width="33%" bgcolor="#EFEBDE">
          <lt:Label res="res.label.forum.admin.score" key="score"/>
        </td>
      </tr>
	<%
	UserMgr um = new UserMgr();
	while (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
	%>
      <tr align="center">
        <td height="24" align="left" bgcolor="#FFF7FF">
		<a href="../score_record_list.jsp?userName=<%=rr.getString(1)%>" target=_blank><%=um.getUser(rr.getString(1)).getNick()%></a>
		</td>
        <td align="left" bgcolor="#FFF7FF"><%=rr.getInt(2)%></td>
      </tr>
	<%}%>
    </table>
      <table width="82%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
        <tr>
          <td height="23" align="right">
    <%
	  String querystr = "op=" + op + "&moneyCode=" + moneyCode + "&beginDate=" + beginDate + "&endDate=" + endDate;
 	  out.print(paginator.getCurPageBlock("score_statistic.jsp?"+querystr));
	%>          </td>
        </tr>
      </table>
    <br></td>
  </tr>
</table>
</td> </tr>             
      </table>                                        
       </td>                                        
     </tr>                                        
 </table>                                        
                               
</body>                                        
</html>                            
  