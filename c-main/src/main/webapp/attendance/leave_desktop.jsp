<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ include file="../inc/nocache.jsp" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    int id = ParamUtil.getInt(request, "id");
    UserDesktopSetupDb udsd = new UserDesktopSetupDb();
    udsd = udsd.getUserDesktopSetupDb(id);
    String kind = ParamUtil.get(request, "kind"); // 是否来自于lte界面
%>
<div id="drag_<%=id%>" dragTitle="<%=udsd.getTitle()%>" count="<%=udsd.getCount()%>"
     wordCount="<%=udsd.getWordCount()%>" class="portlet drag_div bor ibox">
    <div id="drag_<%=id%>_h" class="box ibox-title">
        <!-- <span class="titletxt"><img src="<%=SkinMgr.getSkinPath(request)%>/images/titletype.png" width="8" height="12" /> <a href="attendance/leave_list.jsp"><%=udsd.getTitle()%></a></span>  -->
        <!-- <div class="opbut-1"> <img onclick="mini('<%=udsd.getId()%>')" title="最小化" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/minimization.png" align="absmiddle" width="19" height="19"/></div>  -->
        <!-- <div class="opbut-2"><img onclick="mod('<%=udsd.getId()%>')" title="修改显示方式" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/configure.png" align="absmiddle" width="19" height="19"/></div>  -->
        <!-- <div class="opbut-3"><img onclick="clo('<%=udsd.getId()%>')" title="关闭" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/close.png" align="absmiddle" width="19" height="19"/></div>  -->
        <%
            if ("lte".equals(kind)) {
        %>
        <h5>
            <i class="fa <%=udsd.getIcon()%>"></i>&nbsp;&nbsp;<a href="<%=request.getContextPath()%>/attendance/leave_list.jsp"><%=udsd.getTitle()%></a>
        </h5>
        <%
        }
        else {
        %>
        <div class="titleimg">
            <!--<img src="images/desktop/notepaper.png" width="40" height="40" />-->
            <i class="fa <%=udsd.getIcon()%>"></i>
            &nbsp;&nbsp;</div>
        <div class="titletxt">&nbsp;&nbsp;<a href="<%=request.getContextPath()%>/attendance/leave_list.jsp"><%=udsd.getTitle()%></a></div>
        <%
            }
        %>
    </div>
    <div id="drag_<%=udsd.getId()%>_c" class="portlet_content ibox-content">
        <%
            int count = udsd.getCount();
            // STATUS_FINISHED说明已经销假，流程已完毕
            String now = SQLFilter.getDateStr(DateUtil.format(new java.util.Date(), "yyyy-MM-dd"), "yyyy-MM-dd");
            String sql = "select f.flowId from form_table_qjsqd f, flow fl where f.flowId=fl.id and " + now + ">=qjkssj" + " and " + now + "<=qjjssj and f.flowTypeCode='qj' and f.result='1' and fl.status=" + WorkflowDb.STATUS_STARTED + " order by fl.mydate desc";
            // out.print(sql);
            
            FormDb fd = new FormDb();
            fd = fd.getFormDb("qjsqd");

            WorkflowDb wf = new WorkflowDb();
            ListResult lr = wf.listResult(sql, 1, count);
            Vector v = lr.getResult();
            Iterator ir = null;
            if (v != null) {
                ir = v.iterator();
            }
            
            UserMgr um = new UserMgr();
            FormDAO fdao = new FormDAO();
            if (ir.hasNext()) {
        %>
        <ul>
            <%
                while (ir.hasNext()) {
                    WorkflowDb wfd = (WorkflowDb) ir.next();
                    fdao = fdao.getFormDAO(wfd.getId(), fd);
                    String strBeginDate = fdao.getFieldValue("qjkssj");
                    String strEndDate = fdao.getFieldValue("qjjssj");
            %>
            <li><%=um.getUserDb(wfd.getUserName()).getRealName()%>&nbsp;&nbsp;&nbsp;请假时间：<%=strBeginDate%>&nbsp;-&nbsp;<%=strEndDate%>
            </li>
            <%}%>
        </ul>
        <%} else {%>
        <div class='no_content'><img title='暂无请假人员' src='images/desktop/no_content.jpg'></div>
        <%}%>
    </div>
</div>