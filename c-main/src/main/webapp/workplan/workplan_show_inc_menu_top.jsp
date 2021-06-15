<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%
    int idTop = ParamUtil.getInt(request, "workplanId", -1);
    if (idTop == -1)
        idTop = ParamUtil.getInt(request, "id", -1);
    String taskTop = ParamUtil.get(request, "task");
    if (taskTop.equals(""))
        taskTop = "msg";
%>
<div class="tabs1Box">
    <div id="tabs1">
        <ul>
            <li id="menu1"><a href="workplan_show.jsp?id=<%=idTop%>"><span>查看计划</span></a></li>
            <li id="menu2"><a href="workplan_gantt.jsp?id=<%=idTop%>"><span>甘特图</span></a></li>
            <li id="menu4"><a href="workplan_task.jsp?id=<%=idTop%>"><span>任务项</span></a></li>
            <li id="menu10"><a href="workplan_task_milestone.jsp?id=<%=idTop%>"><span>里程碑</span></a></li>
            <li id="menu6"><a href="workplan_user.jsp?id=<%=idTop%>"><span>参与者</span></a></li>
            <li id="menu5"><a href="workplan_burthen.jsp?id=<%=idTop%>"><span>工作负荷</span></a></li>
            <li id="menu7"><a href="workplan_his_list.jsp?id=<%=idTop%>"><span>计划历史</span></a></li>
            <li id="menu3"><a href="workplan_annex_day.jsp?id=<%=idTop%>"><span>日报</span></a></li>
            <li id="menu8"><a href="workplan_annex_list_week.jsp?id=<%=idTop%>"><span>周报</span></a></li>
            <li id="menu9"><a href="workplan_annex_list_month.jsp?id=<%=idTop%>"><span>月报</span></a></li>
        </ul>
    </div>
</div>