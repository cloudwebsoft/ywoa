<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.*"%>
<%@ page import="java.text.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
/*
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));

	return;
}
*/

int id = ParamUtil.getInt(request, "id");
UserDesktopSetupDb udsd = new UserDesktopSetupDb();
udsd = udsd.getUserDesktopSetupDb(id);
int count = udsd.getCount();

UserSetupDb usd = new UserSetupDb();
usd = usd.getUserSetupDb(privilege.getUser(request));
String clockCode = usd.getClockCode();
if (clockCode.equals("")) {
	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	clockCode = cfg.get("clockCode");
}
Date date=new Date();
DateFormat format=new SimpleDateFormat("EEEE yyyy-MM-dd HH:mm");
String time=format.format(date);
%>
<div id="drag_<%=id%>" class="portlet drag_div bor ibox" style="padding:0px;" >
  <div id="drag_<%=id%>_h" style="height:3px;padding:0px;margin:0px; font-size:1px"></div>
  <div class="portlet_content ibox-content" style="padding:0px;margin:0px; padding-bottom:5px; overflow:visible;">
		<div style="text-align:center"><%=clockCode%></div>
		<div style="text-align:center;margin-top:10px;" id="drag_<%=id%>_time"><%=time %></div>
  </div>
</div>
<script>
function showMyCurrentTime(){
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
	//var ss=now.getTime()%60000;
	//ss=(ss-(ss%1000))/1000;
	if(MM<10)MM="0"+MM;
	if(dd<10)dd="0"+dd;
	if(hh<10)hh="0"+hh;
	if(mm<10)mm="0"+mm;
	//if(ss<10)ss="0"+ss;
	var date = x[DD]+"  "+yy+"-"+MM+"-"+dd+"  "+hh+":"+mm;
	document.getElementById('drag_<%=id%>_time').innerHTML = date; //显示时间
}
setInterval("showMyCurrentTime()", 1000); //设定函数自动执行时间为 1000 ms(1 s)

</script>