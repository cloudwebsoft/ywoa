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
// 取出参与的任务
int workplanId = ParamUtil.getInt(request, "workplanId", -1);
java.util.Date dt = DateUtil.parse(ParamUtil.get(request, "date"), "yyyy-MM-dd");
String userName = ParamUtil.get(request, "userName");
String sql;
if (workplanId!=-1) {
	sql = "select t.id from work_plan_task t, work_plan_task_user u where t.work_plan_id=? and u.user_name=? and t.id=u.task_id and (t.start_date<=? and t.end_date>=?)";
}
else {
	sql = "select t.id from work_plan_task t, work_plan_task_user u where u.user_name=? and t.id=u.task_id and (t.start_date<=? and t.end_date>=?)";
}
WorkPlanTaskDb wptd = new WorkPlanTaskDb();
Iterator ir;
int count = 0;
if (workplanId!=-1) {
	Vector v = wptd.list(sql, new Object[]{new Integer(workplanId), userName, dt, dt});
	count = v.size();
	ir = v.iterator();
}
else {
	Vector v = wptd.list(sql, new Object[]{userName, dt, dt});
	count = v.size();
	ir = v.iterator();
}
%>
<div style="line-height:1.5">
<%
while (ir.hasNext()) {
	wptd = (WorkPlanTaskDb)ir.next();
	%>
	<%=wptd.getString("name")%><br />
	<%
}
if (count==0) {
%>
	无任务
<%
}
%>
</div>
