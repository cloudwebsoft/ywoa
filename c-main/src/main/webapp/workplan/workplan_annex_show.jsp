<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.kernel.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.WorkflowDb" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作计划进度信息查看</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />

<script src="../inc/upload.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
<script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>
<script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    long annexId = ParamUtil.getInt(request, "annexId", -1);
    if (annexId==-1) {
        out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "err_id")));
        return;
    }

    WorkPlanAnnexDb wpad = new WorkPlanAnnexDb();
    wpad = (WorkPlanAnnexDb) wpad.getQObjectDb(new Long(annexId));
    if (wpad==null) {
        out.print(SkinUtil.makeErrMsg(request, "汇报不存在！"));
        return;
    }

    int workplanId = wpad.getInt("workplan_id");
    WorkPlanDb wpd = new WorkPlanDb();
    wpd = wpd.getWorkPlanDb(workplanId);

    com.redmoon.oa.workplan.Privilege pvg = new com.redmoon.oa.workplan.Privilege();
    if (!pvg.canUserSeeWorkPlan(request, workplanId)) {
        out.print(SkinUtil.makeErrMsg(request, "权限非法！"));
        return;
    }
%>
<br>
<table width="600" border="0" align="center" cellPadding="2" cellSpacing="0" class="tabStyle_1 percent80">
    <tbody>
    <tr>
        <td colspan="2" align="center" noWrap class="tabStyle_1_title"><%=wpd.getTitle()%>&nbsp;-&nbsp;汇报</td>
    </tr>
    <tr>
        <td align="center" noWrap>用户</td>
        <td>
            <%
                UserDb user = new UserDb();
                user = user.getUserDb(wpad.getString("user_name"));
                out.print(user.getRealName());
            %>
        </td>
    </tr>
    <tr>
        <td width="12%" align="center" noWrap>任务</td>
        <td>
            <%
                WorkPlanTaskDb wptd = new WorkPlanTaskDb();
                wptd = (WorkPlanTaskDb) wptd.getQObjectDb(new Long(wpad.getLong("task_id")));
            %>
            <%=wptd.getString("name")%>，原进度为&nbsp;<%=wpad.getInt("old_progress")%>%
            ，现进度为&nbsp;<%=wpad.getInt("progress")%>%
        </td>
    </tr>
    <tr>
        <td align="center" noWrap>内容</td>
        <td>
            <%=wpad.getString("content")%>
            <%
                if (wpad.getInt("annex_type") == WorkPlanAnnexDb.TYPE_FLOW) {
                    WorkflowDb wf = wpad.getWorkflowDb();
                    out.print("<a href = \"javascript:;\" onclick = \"addTab('汇报流程', '" + request.getContextPath() + "/flow_modify.jsp?flowId=" + wf.getId() + "')\">" + wf.getTitle() + "&nbsp;(" + wf.getStatusDesc() + ")</a>");
                }
            %>
        </td>
    </tr>
    <%
        WorkPlanAnnexAttachment wpa = new WorkPlanAnnexAttachment();
        Vector attV = wpa.getAttachments(annexId);
        if (attV.size() > 0) {
    %>
    <tr>
        <td align="center" noWrap>附件</td>
        <td>
            <%
                java.util.Iterator attir = attV.iterator();
                while (attir.hasNext()) {
                    WorkPlanAnnexAttachment att = (WorkPlanAnnexAttachment) attir.next();
            %>
            <div>
                <img src="../netdisk/images/sort/<%=com.redmoon.oa.netdisk.Attachment.getIcon(StrUtil.getFileExt(att.getDiskName()))%>"
                    width="17" height="17">
                &nbsp;<a target="_blank" href="<%=att.getAttachmentUrl(request)%>"><%=att.getName()%></a>
            <%}%>
        </td>
    </tr>
    <%
        }
    %>
    </tbody>
</table>
</body>
</html>