<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.cloudwebsoft.framework.console.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="java.util.Calendar"%>
<%@ page import="java.util.Date"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>QuickFramework数据库连接监控</title>
<script src="<%=request.getContextPath()%>/inc/common.js"></script>
<script>
function loadStackTrace(t_id){
	var targetImg2=$("stackImg" + t_id);
	var targetTR2=$("stack" + t_id);
	if ("object"==typeof(targetImg2)){
		if (targetTR2.style.display!="")
		{
			targetTR2.style.display="";
			targetImg2.src="<%=request.getContextPath()%>/forum/images/minus.gif";
		}else{
			targetTR2.style.display="none";
			targetImg2.src="<%=request.getContextPath()%>/forum/images/plus.gif";
		}
	}
}

function loadStackTraceTooLong(t_id){
	var targetImg2=$("stackTooLongImg" + t_id);
	var targetTR2=$("stackTooLong" + t_id);
	if ("object"==typeof(targetImg2)){
		if (targetTR2.style.display!="")
		{
			targetTR2.style.display="";
			targetImg2.src="<%=request.getContextPath()%>/forum/images/minus.gif";
		}else{
			targetTR2.style.display="none";
			targetImg2.src="<%=request.getContextPath()%>/forum/images/plus.gif";
		}
	}
}
</script>
</head>
<body>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("remove")) {
	String connHashCode = ParamUtil.get(request, "connHashCode");
	ConnMonitor.removeFromActiveConnections(connHashCode);
	out.print(StrUtil.Alert_Redirect("操作成功！", "conn_monitor.jsp"));
	return;
}
else if (op.equals("removeTooLong")) {
	String connHashCode = ParamUtil.get(request, "connHashCode");
	ConnMonitor.removeFromTooLongQueries(connHashCode);
	out.print(StrUtil.Alert_Redirect("操作成功！", "conn_monitor.jsp"));
	return;
}
else if (op.equals("clearActiveConn")) {
	ConnMonitor.clearActiveConnections();
	out.print(StrUtil.Alert_Redirect("操作成功！", "conn_monitor.jsp"));
	return;	
}
else if (op.equals("clearTooLongQueries")) {
	ConnMonitor.clearTooLongQueries();
	out.print(StrUtil.Alert_Redirect("操作成功！", "conn_monitor.jsp"));
	return;	
}
/*
// 测试ConnMonitor
Conn conn = new Conn(Global.getDefaultDB());
String sql = "select * from users";
conn.executeQuery(sql);
// if (false)
	conn.close();
for (int i=0; i<30; i++) {	
Connection con = new Connection(Global.getDefaultDB());
con.executeQuery(sql);
if (true)
	con.close();
}
*/
%>
<div>未关闭的连接&nbsp;&nbsp;(<a onclick="return confirm('您确定要清空么？')" href="conn_monitor.jsp?op=clearActiveConn">清空</a>)</div>
<%
Map conns = ConnMonitor.getActiveConnections();
Iterator ir = conns.keySet().iterator();
while (ir.hasNext()) {
	Object c = ir.next();
	ConnInfo cd = (ConnInfo)conns.get(c);
	// 检测是否大于阀值
	if (System.currentTimeMillis() - cd.getTime() > ConsoleConfig.connElapseTimeMax) {
	%>
		<div id="conn<%=cd.getThreadName()%>">
		<div><img id="stackImg<%=cd.getThreadName()%>" style="cursor:pointer" src="<%=request.getContextPath()%>/forum/images/plus.gif" onclick="loadStackTrace('<%=cd.getThreadName()%>')">&nbsp;&nbsp;<%=cd.getThreadName()%>&nbsp;&nbsp;<%=DateUtil.format(DateUtil.parse(cd.getTime() + ""), "yyyy-MM-dd HH:mm:ss")%>&nbsp;&nbsp;已连接：<%=(System.currentTimeMillis() - cd.getTime())/1000%>秒&nbsp;&nbsp;<a href="conn_monitor.jsp?op=remove&connHashCode=<%=c%>">删除</a></div>
		<div id="stack<%=cd.getThreadName()%>" style="display:none">
		<%=StrUtil.toHtml(cd.getStackTraceString())%><br>
		</div>
		</div>
	<%
	}
}

// ConnMonitor.clearActiveConnections();
%>
<div>查询超长的连接 (<a onclick="return confirm('您确定要清空么？')" href="conn_monitor.jsp?op=clearTooLongQueries">清空</a>)</div>
<%
ConnMonitor.MAX_ELAPSE_TIME = 50; // 0.5秒

conns = ConnMonitor.getTooLongQueries();
ir = conns.keySet().iterator();
while (ir.hasNext()) {
	Object c = ir.next();
	ConnInfo cd = (ConnInfo)conns.get(c);
	// 检测是否大于阀值
	if (System.currentTimeMillis() - cd.getTime() >= 20) { // ConsoleConfig.connElapseTimeMax) {
	%>
		<div id="connTooLong<%=cd.getThreadName()%>">
		<div>
		  <div><img id="stackTooLongImg<%=cd.getThreadName()%>" style="cursor:pointer" src="<%=request.getContextPath()%>/forum/images/plus.gif" onclick="loadStackTraceTooLong('<%=cd.getThreadName()%>')">&nbsp;&nbsp;<%=cd.getThreadName()%>&nbsp;&nbsp;<%=DateUtil.format(DateUtil.parse(cd.getTime() + ""), "yyyy-MM-dd HH:mm:ss")%>&nbsp;&nbsp;已连接：<%=(System.currentTimeMillis() - cd.getTime())/1000%>秒&nbsp;&nbsp;<a href="conn_monitor.jsp?op=removeTooLong&connHashCode=<%=c%>">删除</a></div>
			<div><%=cd.getSql()%>&nbsp;&nbsp;<%=(double)cd.getQueryTimeElapse()/1000%>秒</div>
		</div>
		<div id="stackTooLong<%=cd.getThreadName()%>" style="display:none">
		<%=StrUtil.toHtml(cd.getStackTraceString())%><br>
		</div>
		</div>
	<%
	}
}
%>
</body>
</html>