<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.workplan.*"%>
<%@ page import="com.redmoon.oa.oacalendar.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%
int workplanId = ParamUtil.getInt(request, "id");

// 翻月
int showyear = ParamUtil.getInt(request, "showyear", -1);
int showmonth = ParamUtil.getInt(request, "showmonth", -1);
Calendar cal = Calendar.getInstance();
if (showyear==-1)
	showyear = cal.get(cal.YEAR);
if (showmonth==-1)
	showmonth = cal.get(cal.MONTH)+1;

int curyear = cal.get(cal.YEAR);

int viewMode = ParamUtil.getInt(request, "viewMode", 0);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作负荷</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>

<link href="../js/qTip2/jquery.qtip.css" rel="stylesheet" />
<script src="../js/qTip2/jquery.qtip.js"></script>

<style>
.holiday {
	background-color:#eeeeee;	
}
.warn {
	background-color:#FF0;
}
.normal {
	background-color:#AAF49F;
}
</style>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%@ include file="workplan_show_inc_menu_top.jsp"%>
<script>
o("menu5").className="current";
</script>
<div class="spacerH"></div>
<%
WorkPlanDb wpd = new WorkPlanDb();
wpd = wpd.getWorkPlanDb(workplanId);

UserMgr um = new UserMgr();
	
Vector vUser = new Vector();
String[] principalAry = wpd.getPrincipals();
int len = principalAry==null?0:principalAry.length;
for (int i=0; i<len; i++) {
  if (principalAry[i].equals(""))
      continue;
  UserDb user = um.getUserDb(principalAry[i]);
  vUser.addElement(user);
}

String[] userAry = wpd.getUsers();
len = userAry==null?0:userAry.length;
for (int i=0; i<len; i++) {
  if (userAry[i].equals(""))
      continue;
  // 过滤掉负责人
  boolean isFound = false;
  for (int j=0; j<principalAry.length; j++) {
      if (principalAry[j].equals(userAry[i])) {
          isFound = true;
          break;
      }
  }
  if (isFound)
      continue;
  UserDb user = um.getUserDb(userAry[i]);
  vUser.addElement(user);  
}   	
%>
<table class="percent98" width="98%" align="center">
  <tr><td align="center">
<%=wpd.getTitle()%>  
<select name="showyear" onChange="var y=this.options[this.selectedIndex].value; window.location.href='workplan_burthen.jsp?id=<%=workplanId%>&viewMode=<%=viewMode%>&showyear=' + y;">
  <%for (int y=curyear-60; y<=curyear; y++) {%>
  <option value="<%=y%>"><%=y%></option>
  <%}%>
</select>
<script>
o("showyear").value = "<%=showyear%>";
</script>
<%
for (int i=1; i<=12; i++) {
	if (showmonth==i)
		out.print("<a href='workplan_burthen.jsp?showyear="+showyear+"&showmonth="+i+"&id=" + workplanId + "&viewMode=" + viewMode + "'><font color=red>"+i+"月</font></a>&nbsp;");
	else
		out.print("<a href='workplan_burthen.jsp?showyear="+showyear+"&showmonth="+i+"&id=" + workplanId + "&viewMode=" + viewMode + "'>"+i+"月</a>&nbsp;");

}
%>
<input type="radio" id="viewMode" value="1" <%=(viewMode==0)?"checked":""%> onclick="window.location.href='workplan_burthen.jsp?id=<%=workplanId%>&showyear=<%=showyear%>&showmonth=<%=showmonth%>&viewMode=0'" />
只显示本计划
<input type="radio" id="viewMode" value="0" <%=(viewMode==1)?"checked":""%> onclick="window.location.href='workplan_burthen.jsp?id=<%=workplanId%>&showyear=<%=showyear%>&showmonth=<%=showmonth%>&viewMode=1'" />
显示所有计划&nbsp;&nbsp; <span class="warn">&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;色表示工作量超负荷
</td></tr></table>
<form action="" method="post" name="form1" id="form1">
  <table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
    <tr>
      <td class="tabStyle_1_title" width="70" height="24" align="center">姓名</td>
      <%
	  int dayCount = DateUtil.getDayCount(showyear, showmonth-1);
	  for (int i=1; i<=dayCount; i++) {
	  %>
      <td class="tabStyle_1_title" width="40" align="center"><%=i%></td>
      <%}%>
    </tr>
<%
	int row = 0;
	java.util.Date start = DateUtil.getDate(showyear, showmonth-1, 1);
	OACalendarDb oacal = new OACalendarDb();
	WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();
	Iterator ir = vUser.iterator();
	while (ir.hasNext()) {
		UserDb user = (UserDb)ir.next();
		row ++;
%>
    <tr>
      <td height="22" align="left"><%=user.getRealName()%></td>
      <%
	  double[] ary = null;
	  if (viewMode==0)
		  ary = wptud.getBurthenOfWorkPlan(user.getName(), workplanId, showyear, showmonth-1);
	  else
		  ary = wptud.getBurthen(user.getName(), showyear, showmonth-1);
	  
	  for (int i=0; i<=dayCount-1; i++) {
		  // 判断是否为非工作日
		  java.util.Date d = DateUtil.addDate(start, i);
		  oacal = (OACalendarDb) oacal.getQObjectDb(d);

		  String cls = "";
		  if (oacal!=null && oacal.getInt("date_type") != OACalendarDb.DATE_TYPE_WORK) {
			  cls = "class='holiday'";
		  }
		  else {
			  if (ary[i]>1)
				cls = "class='warn'";
			  else if (ary[i]>0)
				cls = "class='normal'";	
		  }		  
		  
		  if (oacal==null) {
		  	oacal = new OACalendarDb();
		  }
		  
	  %>
      <td id="td<%=row%>_<%=i%>" align="center" <%=cls%>>
      <%
	  // 根据参与的task时间段，取出所在天的工作量
	  out.print(NumberUtil.round(ary[i], 1));
	  %>
      <script>
	  $("#td<%=row%>_<%=i%>").qtip({
		position: {
			my: 'bottom center',
			at: 'top center'
		},
		content: {
			text: "加载中...",
			ajax: {
			  url: "<%=request.getContextPath()%>/workplan/workplan_burthen_tip.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>&date=<%=DateUtil.format(d, "yyyy-MM-dd")%>"
			},
			title: {
				text: '<%=i+1%>号',
				button: "关闭"
			}
		},
		show: 'click',
		style: {
				classes: 'qtip-jtools'  
			},

		hide: 'click' // false
	  });
	  </script>      
      </td>
      <%}%>      
      
    </tr>
    <%
    }
%>
  </table>
</form>
</body>
</html>
