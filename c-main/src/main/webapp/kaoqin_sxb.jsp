<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import = "java.util.*"%>
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
<%@ page import = "java.text.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.Config"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/><%
	if (!privilege.isUserPrivValid(request, "read")) {
		out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");	
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	
	java.util.Date kqDate;
	String kqDateStr = ParamUtil.get(request, "kqDate");
	if (!kqDateStr.equals("")) {
		kqDate = DateUtil.parse(kqDateStr, "yyyy-MM-dd");
	}
	else {
		kqDate = new java.util.Date();
	}
	
	if (DateUtil.compare(new java.util.Date(), kqDate)==2) {
		out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");	
		out.print(SkinUtil.makeErrMsg(request, "时间还未到，不能考勤！", true));
		return;
	}
	
	String op = ParamUtil.get(request, "op");
	if(op.equals("check")) {
		boolean re = false;
		try {
			KaoqinMgr kq = new KaoqinMgr();
			re = kq.create(request);
		} catch (ErrMsgException e) {
			JSONObject json = new JSONObject();
			json.put("ret", "1");
			json.put("msg", e.getMessage());
			out.print(json);
			return;
		}
	
		if (re) {
			// out.print("{\"ret\":\"1\", \"msg\":\"操作成功！\"}");
			JSONObject json = new JSONObject();
			json.put("ret", "1");
			json.put("msg", "考勤成功！");
			out.print(json);
		}
		else {
			JSONObject json = new JSONObject();
			json.put("ret", "0");
			json.put("msg", "考勤失败！");
			out.print(json);
		}
		
		return;
	}	
	
	String userName = ParamUtil.get(request, "userName");
	if (userName.equals(""))
		userName = privilege.getUser(request);

	KaoqinPrivilege kpvg = new KaoqinPrivilege();

	if (!userName.equals(privilege.getUser(request))) {
		if (!(kpvg.canAdminKaoqin(request))) {
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
	}
	
	KaoqinDb kq = new KaoqinDb();
	
	cfgparser.parse("config.xml");
	java.util.Properties props = cfgparser.getProps();
	int latevalue = StrUtil.toInt(props.getProperty("latevalue"), 10);//上班时间后多少分钟后算迟到
	int minsAheadOfWorkBegin = StrUtil.toInt(props.getProperty("mins_ahead_of_workbegin"), 15);//上班时间前多少分钟可以开始上班考勤
	int minsAfterWorkBegin = StrUtil.toInt(props.getProperty("mins_after_workbegin"), 15);//上班时间后多少分钟内可以上班考勤
	int minsAheadOfWorkEnd = StrUtil.toInt(props.getProperty("mins_ahead_of_workend"), 15);//下班时间前多少分钟内可以下班考勤
	int minsAfterWorkEnd = StrUtil.toInt(props.getProperty("mins_after_workend"), 15);//下班时间后多少分钟内可以下班考勤
	//boolean isKaoqinNight = "true".equals(props.getProperty("isKaoqinNight"));
	
	Config cg = new Config();
	boolean isKaoqinNight = cg.getBooleanProperty("isKaoqinNight");
	
	OACalendarDb oaCalendarDb = new OACalendarDb();
	oaCalendarDb = (OACalendarDb)oaCalendarDb.getQObjectDb(DateUtil.parse(DateUtil.format(kqDate, "yyyy-MM-dd"), "yyyy-MM-dd"));
	if (oaCalendarDb==null) {
		out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");	
		out.print(SkinUtil.makeErrMsg(request, "工作日历未初始化！"));
		return;
	}
	String checkBeginA = StrUtil.getNullStr(oaCalendarDb.getString("work_time_begin_a"));
	String checkEndA = StrUtil.getNullStr(oaCalendarDb.getString("work_time_end_a"));
	String checkBeginB = StrUtil.getNullStr(oaCalendarDb.getString("work_time_begin_b"));
	String checkEndB = StrUtil.getNullStr(oaCalendarDb.getString("work_time_end_b"));
	String checkBeginC = StrUtil.getNullStr(oaCalendarDb.getString("work_time_begin_c"));
	String checkEndC = StrUtil.getNullStr(oaCalendarDb.getString("work_time_end_c"));
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>考勤</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/common/common.css" />
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-migrate-1.2.1.min.js"></script>
<script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<style>
#checkAttendanceTitle {
	width:100%;
	height:42px;
	border-bottom:3px solid #FF9704;
	float:left;
	background-repeat:no-repeat;
}
#checkArea {
	padding-top:10px;
	width:320px;
	height:250px;
	float:left;
	text-align:left;
	background-image:url(images/push_clock.png);
	background-repeat:no-repeat;
	background-position:center;
	background-color:#FFFFFF;
}
#checkTimeShow {
	width:780px;
	float:left;
	margin-top:10px;
	padding-left:10px;	
}
#checkTimeShow ul {
	list-style:none;
}
.checkAttendanceLeft {
	width:320px;
	float:left;
	padding-left:10px;
}
</style>
<script src="inc/common.js"></script>
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="js/datepicker/jquery.datetimepicker.css"/>
<script src="js/datepicker/jquery.datetimepicker.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="js/jquery.form.js"></script>
</head>
<body>
<%@ include file="kaoqin_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<form name="form" action="kaoqin_sxb.jsp" method="post">
  <div id="checkAttendance">
    <div id="checkAttendanceTitle"><img src="images/check_attendance_title.png"/><a href="javascript:void(0)" onclick="setWorkCalendar()" style="color:blue;font-size:16px;font-weight:bold;<%=privilege.isUserPrivValid(request, "archive.user") ? "" : "display:none" %>">考勤时间设置</a></div>
    
	<div class="checkAttendanceLeft">
		<div style="height:30px"></div>
		<div id="checkArea" style="text-align:center">
		  <!-- <object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=7,0,19,0" width="301">
            <param name="movie" value="images/clock.swf" />
            <param name="quality" value="high" />
            <embed src="images/clock.swf" quality="high" pluginspage="http://www.macromedia.com/go/getflashplayer" type="application/x-shockwave-flash" width="301" height="350"></embed>
          </object> -->
          <embed src="flash/clock.swf" width="150" height="150" quality=high pluginspage="http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash" type="application/x-shockwave-flash" wmode="transparent"></embed>
		</br></br><span id="detail_datetime"></span>
		</div>
	</div>
    <div id="checkTimeShow">
    	 
      <ul>
	  	<li>
	上午：<%=oaCalendarDb.getString("work_time_begin_a")%>~
	<%=oaCalendarDb.getString("work_time_end_a")%>
	下午：<%=oaCalendarDb.getString("work_time_begin_b")%>~
	<%=oaCalendarDb.getString("work_time_end_b")%>
    <%if (isKaoqinNight) {%>
	晚上：<%=oaCalendarDb.getString("work_time_begin_c")%>~	<%=oaCalendarDb.getString("work_time_end_c")%>
    <%}%>
		</li>
        <li>上班前&nbsp;&nbsp;<font color="#FF0000"><%=minsAheadOfWorkBegin%></font>&nbsp;&nbsp;分钟&nbsp;&nbsp;至&nbsp;&nbsp;上班时间后&nbsp;&nbsp;<font color="#FF0000"><%=minsAfterWorkBegin%></font>&nbsp;&nbsp;分钟&nbsp;&nbsp;内可以考勤</li>
        <li>下班前&nbsp;&nbsp;<font color="#FF0000"><%=minsAheadOfWorkEnd%></font>&nbsp;&nbsp;分钟&nbsp;&nbsp;至&nbsp;&nbsp;下班时间后&nbsp;&nbsp;<font color="#FF0000"><%=minsAfterWorkEnd%></font>&nbsp;&nbsp;分钟&nbsp;&nbsp;内可以考勤</li>
        <li>上班后&nbsp;&nbsp;<font color="#FF0000"><%=latevalue%></font>&nbsp;&nbsp;分钟后考勤记为&nbsp;<font color="#FF0000">迟到</font>&nbsp;&nbsp;下班前&nbsp;<font color="#FF0000"><%=latevalue%></font>&nbsp;&nbsp;分钟前考勤记为&nbsp;<font color="#FF0000">早退</font></li>
	  </ul>
	  <br />
	  <table class="percent98"><tr><td>
	  <%if (kpvg.canAdminKaoqin(request)) {%>
	  <input id="kqDate" name="kqDate" value="<%=DateUtil.format(kqDate, "yyyy-MM-dd")%>" size=10 onchange="window.location.href='kaoqin_sxb.jsp?kqDate=' + this.value" />
	  <%}else{%>
	  <input id="kqDate" name="kqDate" value="<%=DateUtil.format(kqDate, "yyyy-MM-dd")%>" size=10 type="hidden" />
	  <%}%>
	  <strong>上下班考勤</strong></td></tr></table>
      <table class="tabStyle_1 percent98">
          <tr>
            <td class="tabStyle_1_title" width="6%">序号</td>
            <td class="tabStyle_1_title" width="13%">类型</td>
            <td class="tabStyle_1_title" width="14%">规定时间</td>
            <td class="tabStyle_1_title" width="14%">登记时间</td>
            <td class="tabStyle_1_title" width="15%">迟到/早退<br />
            (分钟)</td>
            <td class="tabStyle_1_title" width="21%">原因</td>
            <td class="tabStyle_1_title" width="17%">操作</td>
          </tr>
        <tr>
          <td align="center">1</td>
          <td align="center"><span style="margin-top:12px">上午上班</span></td>
          <td align="center"><span style="margin-top:12px"><%=checkBeginA%></span></td>
          <td align="center">
		  <%
		  KaoqinDb kdb = kq.getKaoqinDb(userName, kqDate, 1);
		  if (kdb!=null) {
		  	out.print(DateUtil.format(kdb.getMyDate(), "HH:mm:ss"));
		  }
		  %>		  </td>
          <td align="center">
		  <%
		  if (kdb!=null) {
		  	if (kdb.getFlag()==1) {
				out.print("迟到" + kdb.getTimeMin());
			}
			else if (kdb.getFlag()==2) {
				out.print("早退" + kdb.getTimeMin());
			}
		  }
		  %>
		  </td>
          <td>
		  <%
		  if (kdb!=null) {
		  	out.print(kdb.getReason());
		  }
		  %>		  </td>
          <td align="center">
		  <%
		boolean canCheck = false;
		if (!checkBeginA.equals("")) {
			try {canCheck = KaoqinMgr.canCheck(request, userName, 1, kqDate);} catch (ErrMsgException e) {}
			if (canCheck) {
				if (kpvg.canAdminKaoqin(request)) {%>
				<a onclick="kqdlg(1, '<%=(kdb!=null)?DateUtil.format(kdb.getMyDate(), "HH:mm:ss"):DateUtil.format(new java.util.Date(), "HH:mm:ss")%>')" href="javascript:;">考勤</a>
				<%}
				else if (privilege.getUser(request).equals(userName)) {
			%>
				<a onclick="kq('1')" href="javascript:;">考勤</a>
			<%	}
			}
		}%></td>
        </tr>
        <tr>
          <td align="center">2</td>
          <td align="center">上午下班</td>
          <td align="center"><%=checkEndA%></td>
          <td align="center"><%
		  kdb = kq.getKaoqinDb(userName, kqDate, 2);
		  if (kdb!=null) {
		  	out.print(DateUtil.format(kdb.getMyDate(), "HH:mm:ss"));
		  }
		  %></td>
          <td align="center"><%
		  if (kdb!=null) {
		  	if (kdb.getFlag()==1) {
				out.print("迟到" + kdb.getTimeMin());
			}
			else if (kdb.getFlag()==2) {
				out.print("早退" + kdb.getTimeMin());
			}
		  }
		  %></td>
          <td><%
		  if (kdb!=null) {
		  	out.print(kdb.getReason());
		  }
		  %></td>
          <td align="center">
		<%
		if (!checkEndA.equals("")) {
			canCheck = false;
			try {canCheck = KaoqinMgr.canCheck(request, userName, 2, kqDate);} catch (ErrMsgException e) {}
			if (canCheck) {
				if (kpvg.canAdminKaoqin(request)) {%>
				<a onclick="kqdlg(2, '<%=(kdb!=null)?DateUtil.format(kdb.getMyDate(), "HH:mm:ss"):DateUtil.format(new java.util.Date(), "HH:mm:ss")%>')" href="javascript:;">考勤</a>
				<%}
				else if (privilege.getUser(request).equals(userName)) {
			%>
				<a onclick="kq('2')" href="javascript:;">考勤</a>
			<%	}
			}
		}%></td>
        </tr>
        <tr>
          <td align="center">3</td>
          <td align="center"><span style="margin-top:12px">下午上班</span></td>
          <td align="center"><span style="margin-top:12px"><%=checkBeginB%></span></td>
          <td align="center"><%
		  kdb = kq.getKaoqinDb(userName, kqDate, 3);
		  if (kdb!=null) {
		  	out.print(DateUtil.format(kdb.getMyDate(), "HH:mm:ss"));
		  }
		  %></td>
          <td align="center"><%
		  if (kdb!=null) {
		  	if (kdb.getFlag()==1) {
				out.print("迟到" + kdb.getTimeMin());
			}
			else if (kdb.getFlag()==2) {
				out.print("早退" + kdb.getTimeMin());
			}
		  }
		  %></td>
          <td><%
		  if (kdb!=null) {
		  	out.print(kdb.getReason());
		  }
		  %></td>
          <td align="center">
		<%
		if (!checkBeginB.equals("")) {
			canCheck = false;		
			try {canCheck = KaoqinMgr.canCheck(request, userName, 3, kqDate);} catch (ErrMsgException e) {}
			if (canCheck) {
				if (kpvg.canAdminKaoqin(request)) {%>
				<a onclick="kqdlg(3, '<%=(kdb!=null)?DateUtil.format(kdb.getMyDate(), "HH:mm:ss"):DateUtil.format(new java.util.Date(), "HH:mm:ss")%>')" href="javascript:;">考勤</a>
				<%}
				else if (privilege.getUser(request).equals(userName)) {
			%>
				<a onclick="kq('3')" href="javascript:;">考勤</a>
			<%	}
			}
		}%>
		</td>
        </tr>
        <tr>
          <td align="center">4</td>
          <td align="center">下午下班</td>
          <td align="center"><%=checkEndB%></td>
          <td align="center"><%
		  kdb = kq.getKaoqinDb(userName, kqDate, 4);
		  if (kdb!=null) {
		  	out.print(DateUtil.format(kdb.getMyDate(), "HH:mm:ss"));
		  }
		  %></td>
          <td align="center"><%
		  if (kdb!=null) {
		  	if (kdb.getFlag()==1) {
				out.print("迟到" + kdb.getTimeMin());
			}
			else if (kdb.getFlag()==2) {
				out.print("早退" + kdb.getTimeMin());
			}
		  }
		  %></td>
          <td><%
		  if (kdb!=null) {
		  	out.print(kdb.getReason());
		  }
		  %></td>
          <td align="center">
		<%
		if (!checkEndB.equals("")) {
			canCheck = false;		
			try {canCheck = KaoqinMgr.canCheck(request, userName, 4, kqDate);} catch (ErrMsgException e) {}
			if (canCheck) {
				if (kpvg.canAdminKaoqin(request)) {%>
				<a onclick="kqdlg(4, '<%=(kdb!=null)?DateUtil.format(kdb.getMyDate(), "HH:mm:ss"):DateUtil.format(new java.util.Date(), "HH:mm:ss")%>')" href="javascript:;">考勤</a>
				<%}
				else if (privilege.getUser(request).equals(userName)) {
			%>
				<a  onclick="kq('4')" href="javascript:;">考勤</a>
		  <%	}			
			}
		}%></td>
        </tr>
        <%if (isKaoqinNight) {%>
        <tr>
          <td align="center">5</td>
          <td align="center"><span style="margin-top:12px">晚上上班</span></td>
          <td align="center"><span style="margin-top:12px"><%=checkBeginC%></span></td>
          <td align="center"><%
		  kdb = kq.getKaoqinDb(userName, kqDate, 5);
		  if (kdb!=null) {
		  	out.print(DateUtil.format(kdb.getMyDate(), "HH:mm:ss"));
		  }
		  %></td>
          <td align="center"><%
		  if (kdb!=null) {
		  	if (kdb.getFlag()==1) {
				out.print("迟到" + kdb.getTimeMin());
			}
			else if (kdb.getFlag()==2) {
				out.print("早退" + kdb.getTimeMin());
			}
		  }
		  %></td>
          <td><%
		  if (kdb!=null) {
		  	out.print(kdb.getReason());
		  }
		  %></td>
          <td align="center">
		<%
		if (!checkBeginC.equals("")) {
			canCheck = false;		
			try {canCheck = KaoqinMgr.canCheck(request, userName, 5, kqDate);} catch (ErrMsgException e) {}
			if (canCheck) {
				if (kpvg.canAdminKaoqin(request)) {%>
				<a onclick="kqdlg(5, '<%=(kdb!=null)?DateUtil.format(kdb.getMyDate(), "HH:mm:ss"):DateUtil.format(new java.util.Date(), "HH:mm:ss")%>')" href="javascript:;">考勤</a>
				<%}
				else if (privilege.getUser(request).equals(userName)) {
			%>
				<a onclick="kq('5')" href="javascript:;">考勤</a>
			<%	}
			}
		}%></td>
        </tr>
        <tr>
          <td align="center">6</td>
          <td align="center">晚上下班</td>
          <td align="center"><%=checkEndC%></td>
          <td align="center"><%
		  kdb = kq.getKaoqinDb(userName, kqDate, 6);
		  if (kdb!=null) {
		  	out.print(DateUtil.format(kdb.getMyDate(), "HH:mm:ss"));
		  }
		  %></td>
          <td align="center"><%
		  if (kdb!=null) {
		  	if (kdb.getFlag()==1) {
				out.print("迟到" + kdb.getTimeMin());
			}
			else if (kdb.getFlag()==2) {
				out.print("早退" + kdb.getTimeMin());
			}
		  }
		  %></td>
          <td><%
		  if (kdb!=null) {
		  	out.print(kdb.getReason());
		  }
		  %></td>
          <td align="center">
		<%
		if (!checkEndC.equals("") && kdb==null) {
			canCheck = false;						
			try {canCheck = KaoqinMgr.canCheck(request, userName, 6, kqDate);} catch (ErrMsgException e) {}
			if (canCheck) {
				if (kpvg.canAdminKaoqin(request)) {%>
				<a onclick="kqdlg(6, '<%=(kdb!=null)?DateUtil.format(kdb.getMyDate(), "HH:mm:ss"):DateUtil.format(new java.util.Date(), "HH:mm:ss")%>')" href="javascript:;">考勤</a>
				<%}
				else if (privilege.getUser(request).equals(userName)) {
			%>
				<a onclick="kq('6')" href="javascript:;">考勤</a>
			<%	}			
			}
		}%></td>
        </tr>
        <%}%>
      </table>
	  <!--
      <br />
	  <div><strong>其它考勤</strong></div>	  
		<table width="546" class="tabStyle_1 percent98">
		<thead>
		<tr>
		  <td width="31">次序</td>
		  <td width="85">类型</td>
		  <td width="77">时间</td>
		  <td width="123">去向</td>
		  <td width="206">原因</td>
		  </tr>
		</thead>
		<%
		String sql = "select id from kaoqin where kind=0 and name=" + StrUtil.sqlstr(userName) + " and mydate>=" + SQLFilter.getDateStr(DateUtil.format(kqDate, "yyyy-MM-dd 00:00:00"), "yyyy-MM-dd HH:mm:ss") + " and mydate<=" + SQLFilter.getDateStr(DateUtil.format(kqDate, "yyyy-MM-dd 23:59:59"), "yyyy-MM-dd HH:mm:ss") + " order by mydate desc";
		Vector v = kq.list(sql);
		
		// 今日记录
		BasicDataMgr bdm = new BasicDataMgr("kaoqin");	
		int size = v.size();
		int n = 0;
		for (int i=size-1; i>=0; i--) {
			kq = (KaoqinDb)v.elementAt(i);
			String direction = kq.getDirection();
			String type = kq.getType();
			String reason = kq.getReason();
			String directionDesc = bdm.getItemText("direction", direction);
			n++;
			%>
			<tr>
			  <td align="center"><%=n%></td>
			  <td>&nbsp;&nbsp;<%=type%></td>
			  <td align="center"><%=DateUtil.format(kq.getMyDate(), "HH:mm:ss")%></td>
			  <td><%=directionDesc%></td>
			  <td><%=reason%></td>
		    </tr>
			<%
		}
		%>
      </table>-->
    </div>
  </div>
</form>
<div id="dlg" style="display:none">
<form id="kqForm" name="kqForm" action="kaoqin_sxb.jsp" method="post">
<table width="100%" style="border:0px">
  <tr id="timeTr">
    <td>
时间
  <input id="time" name="time" size="20" />
<input name="op" value="check" type="hidden" />
</td>
  </tr>
  <tr>
    <td>原因
	<input id="reason" name="reason" />
	(迟到或早退时才需要填写)</td>
  </tr>
  <tr>
    <td>
	<input id="kind" name="kind" type="hidden" />
	<input id="kqDate" name="kqDate" value="<%=DateUtil.format(kqDate, "yyyy-MM-dd")%>" type="hidden" />	</td>
  </tr>
</table>
</form>
</div>
<div id="result"></div>
</body>
<script>
/**function SelectDateTime(objName) {
	var dt = showModalDialog("util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:185px;status:no;help:no;");
	if (dt!=null)
		o(objName).value = dt;
}*/
function SelectDateTime(objName) {
    var dt = openWin("util/calendar/time.htm?divId" + objName,"266px","185px");//showModalDialog("../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:185px;status:no;help:no;");
}
function sel(dt, objName) {
    if (dt!=null && objName != "")
        o(objName).value = dt;
}
function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}

var flag = false;
$(document).ready(function() { 
    var options = { 
        //target:        '#output2',   // target element(s) to be updated with server response 
        //beforeSubmit:  function() {alert('d');},  // pre-submit callback 
        success:       showResponse,  // post-submit callback 
 
        // other available options: 
        //url:       url         // override for form's 'action' attribute 
        //type:      type        // 'get' or 'post', override for form's 'method' attribute 
        dataType:  'json'        // 'xml', 'script', or 'json' (expected server response type) 
        //clearForm: true        // clear all form fields after successful submit 
        //resetForm: true        // reset the form after successful submit 
 
        // $.ajax options can be used here too, for example: 
        //timeout:   3000 
    }; 

    // bind to the form's submit event 
    $('#kqForm').submit(function() {
        $(this).ajaxSubmit(options); 
        return false; 
    }); 
    
    $('#kqDate').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d'
    });

    $('#kqDate').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d'
    });

    $('#time').datetimepicker({
      	lang:'ch',
      	datepicker:false,
      	format:'H:i:00',
      	step:1,
      	onShow:function() {
      		if (!flag) {
      			flag = true;
      			return false;
      		}
      	}
     });

    showMyCurrentTime();
    setInterval("showMyCurrentTime()", 1000); //设定函数自动执行时间为 1000 ms(1 s)
});

function showResponse(data)  {
	if (data.ret==1) {
		$("#result").html(data.msg);
	}
	else {
		$("#result").html(data.msg);
	}
	$("#result").dialog({title:"提示", modal: true, buttons: { "确定": function() { $(this).dialog("close"); window.location.reload()}}, closeOnEscape: true, draggable: true, resizable:true });
}

function kq(kind) {
	$("#kind").val(kind);
	$("#timeTr").css("display","none"); 
	$("#time").val(new Date().toLocaleTimeString());
	$("#dlg").dialog({title:"考勤", modal: true, 
						buttons: {
							"取消":function() {
								$(this).dialog("close");
							},
							"确定": function() {
								$(this).dialog("close");
								$.ajax({
									type: "get",
									url: "kaoqin_sxb.jsp",
									data: {
										op: "check",
										kind: kind,
										reason: $("#reason").val(),
										kqDate: $("#kqDate").val()
									},
									dataType: "html",
									beforeSend: function(XMLHttpRequest){
										// $('#bodyBox').showLoading();
									},
									success: function(data, status){
										data = $.parseJSON(data);
										if (data.ret==1) {
											$("#result").html(data.msg);
										}
										else {
											$("#result").html(data.msg);
										}
										$("#result").dialog({title: "提示", modal: true, buttons: { "确定": function() { $(this).dialog("close"); window.location.reload()}}, closeOnEscape: true, draggable: true, resizable:true });
									},
									complete: function(XMLHttpRequest, status){
										// $('#bodyBox').hideLoading();			
									},
									error: function(XMLHttpRequest, textStatus){
										// 请求出错处理
										alert(XMLHttpRequest.responseText);
									}
								});										
							}
						}, 
						closeOnEscape: true, 
						draggable: true, 
						resizable:true,
						width:350
					});
}

function kqdlg(kind, t) {
	$("#kind").val(kind);
	$("#timeTr").css("display",""); 
	$("#time").val(new Date().toLocaleTimeString());
	
	$("#dlg").dialog({title:"考勤", modal: true, 
						buttons: {
							"取消":function() {
								$(this).dialog("close");
							},
							"确定": function() {
								// $('#kqForm').submit();
								$(this).dialog("close");
								$.ajax({
									type: "get",
									url: "kaoqin_sxb.jsp",
									data: {
										op: "check",
										kind: kind,
										reason: $("#reason").val(),
										time: t,
										kqDate: $("#kqDate").val()
									},
									dataType: "html",
									beforeSend: function(XMLHttpRequest){
										// $('#bodyBox').showLoading();
									},
									success: function(data, status){
										data = $.parseJSON(data);
										if (data.ret==1) {
											$("#result").html(data.msg);
										}
										else {
											$("#result").html(data.msg);
										}
										jAlert(data.msg,"提示");
										window.location.reload();
										//$("#result").dialog({title: "提示", modal: true, buttons: {"取消":function(){$(this).dialog("close"); window.location.reload()}, "确定": function() { $(this).dialog("close"); window.location.reload()}}, closeOnEscape: true, draggable: true, resizable:true });
									},
									complete: function(XMLHttpRequest, status){
										// $('#bodyBox').hideLoading();			
									},
									error: function(XMLHttpRequest, textStatus){
										// 请求出错处理
										alert(XMLHttpRequest.responseText);
									}
								});										
							}
						}, 
						closeOnEscape: true, 
						draggable: true, 
						resizable:true,
						width:350
					});
}

function setWorkCalendar() {
	addTab('工作日历', '<%=request.getContextPath()%>/admin/oa_calendar.jsp?type=1')
}

function showMyCurrentTime() {
	var now=new Date();
	var yy=now.getYear();
	if (!isIE() || isIE9 || isIE10 || isIE11)
		yy = 1900 + yy;
	var MM=now.getMonth()+1;
	var dd=now.getDate();
	var DD=now.getDay();
	var x = new Array("星期日","星期一","星期二","星期三","星期四","星期五","星期六");
	var hh=now.getHours();
	var mm=now.getMinutes();
	var ss=now.getSeconds();
	//var ss=now.getTime()%60000;
	//ss=(ss-(ss%1000))/1000;
	if(MM<10)MM="0"+MM;
	if(dd<10)dd="0"+dd;
	if(hh<10)hh="0"+hh;
	if(mm<10)mm="0"+mm;
	if(ss<10)ss="0"+ss;
	//if(ss<10)ss="0"+ss;
	var date = x[DD]+"  "+yy+"-"+MM+"-"+dd+"  "+hh+":"+mm+":"+ss;
	document.getElementById('detail_datetime').innerHTML = date; //显示时间
}
</script>
</html>
