<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    int id = ParamUtil.getInt(request, "id");
    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    String calendarCode = cfg.get("calendarCode");
%>
<div id="drag_<%=id%>" class="portlet drag_div bor ibox" style="padding:0px;">
    <div id="drag_<%=id%>_h" style="height:3px;padding:0px;margin:0px; font-size:1px"></div>
    <div class="portlet_content ibox-content" style="height:141px;padding:0px;margin:0px">
        <div style="text-align:center"><%=calendarCode%>
        </div>
    </div>
</div>