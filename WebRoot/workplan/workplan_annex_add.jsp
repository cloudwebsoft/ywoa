<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.workplan.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%
    long taskId = ParamUtil.getLong(request, "taskId");
    WorkPlanTaskDb wptd = new WorkPlanTaskDb();
    wptd = (WorkPlanTaskDb) wptd.getQObjectDb(taskId);

    int workplanId = wptd.getInt("work_plan_id");
    WorkPlanDb wpd = new WorkPlanDb();
    wpd = wpd.getWorkPlanDb(workplanId);

    String strAddDate = ParamUtil.get(request, "addDate");
    Date addDate = DateUtil.parse(strAddDate, "yyyy-MM-dd");
    if (addDate==null) {
        addDate = new Date();

        WorkPlanAnnexDb wpad = new WorkPlanAnnexDb();
        wpad = wpad.getWorkPlanTaskAnnexDb(workplanId, taskId, addDate);
        if (wpad != null) {
            response.sendRedirect("workplan_annex_edit.jsp?annexId=" + wpad.getLong("id") + "&id=" + workplanId);
            return;
        }
    }

    String privurl = ParamUtil.get(request, "privurl");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>工作计划 - 汇报</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script type="text/javascript" src="../js/jquery.js"></script>
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.min.css"/>
    <script src="../inc/upload.js"></script>
    <script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
    <script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
    <script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
    <style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
    <script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>
    <script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>
    <%@ include file="../inc/nocache.jsp" %>
    <script>
        function addform1_submit() {
            if (o("progress").value > 100) {
                jAlert("进度请填写小于或等于100的数值！", "提示");
                return false;
            }
            o("submitBtn").disabled = true;
        }
    </script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    if (addDate.before(wpd.getEndDate()) && addDate.after(wpd.getBeginDate())) {
        Date now = new java.util.Date();
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        int workplan_annex_day_add_limit = cfg.getInt("workplan_annex_day_add_limit");
        int expireDays = DateUtil.diff(now, addDate);
        // 在规定的超期范围内
        if (expireDays < 0) {
            out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, "不能提前汇报！", true));
            return;
        }
        if (expireDays > workplan_annex_day_add_limit) {
            out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, "已超期，不能汇报！", true));
            return;
        }
    }

    int annexYear = ParamUtil.getInt(request, "annexYear", -1);
    int annexType = ParamUtil.getInt(request, "annexType", WorkPlanAnnexDb.TYPE_NORMAL);
    int annexItem = ParamUtil.getInt(request, "annexItem", -1);
%>
<div class="spacerH"></div>
<form action="workplan_show.jsp?op=addAnnex&id=<%=workplanId%>" onsubmit="return addform1_submit()" method="post" enctype="multipart/form-data" id="addform1">
    <table width="60%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent80">
        <tr>
            <td height="24" colspan="3" align="center" class="tabStyle_1_title">
                <input type="hidden" name="annex_type" value="<%=annexType%>"/>
                <input type="hidden" name="annex_item" value="<%=annexItem%>"/>
                <input type="hidden" name="annex_year" value="<%=annexYear%>"/>
                <%=wpd.getTitle()%>&nbsp;
                <%
                    if (annexYear != -1) {
                        if (annexType == WorkPlanAnnexDb.TYPE_WEEK) {
                %>
                第<%=annexItem%>周&nbsp;&nbsp;周报
                <%
                } else {
                %>
                <%=annexItem%>月&nbsp;&nbsp;月报
                <%
                    }
                } else {
                %>
                -&nbsp;汇报
                <%}%>
            </td>
        </tr>
        <tr>
            <td height="24" align="center">任务</td>
            <td colspan="2">
                <%=wptd.getString("name")%>
            </td>
        </tr>
        <tr>
            <td height="24" align="center">进度</td>
            <td width="43%">
                <div style="float:left; margin-top:5px">原进度&nbsp;</div>
                <div style="float:left">
                    <div class="progressBar" style="padding:0px; margin:0px; height:20px; width:100px">
                        <div class="progressBarFore" style="width:<%=wptd.getInt("progress")%>%;">
                        </div>
                        <div class="progressText">
                            <%=wptd.getInt("progress")%>%
                        </div>
                    </div>
                </div>
                &nbsp;
                ，现进度&nbsp;<input id="progress" name="progress" size="3" title="如果进度不变，则表示仅汇报信息" value="<%=wptd.getInt("progress")%>"/>&nbsp;%
                <input name="old_progress" value="<%=wptd.getInt("progress")%>" type="hidden"/>
                <script>
                    var progress = new LiveValidation('progress');
                    progress.add(Validate.Presence);
                    progress.add(Validate.Numericality, {minimum: 0, maximum: 100});
                </script>
            </td>
            <td width="44%" style="padding-left:10px">
                <div id="slider" style="width:50%"></div>
                <script>
                    $("#progress").change(function () {
                        $("#slider").slider("value", $("#progress").val());
                    });
                </script>
            </td>
        </tr>
        <tr>
            <td width="13%" height="24" align="center">内容</td>
            <td colspan="2">
                <textarea id="content" name="content" style="display:none"></textarea>
                <script>
                    CKEDITOR.replace('content',
                        {
                            // skin : 'kama',
                            toolbar: 'Middle'
                        });
                </script>
            </td>
        </tr>
        <tr>
            <td height="24" align="center">附件</td>
            <td colspan="2">
                <script>initUpload()</script>
                <input name="workplan_id" value="<%=workplanId%>" type="hidden"/>
                <input name="user_name" value="<%=privilege.getUser(request)%>" type="hidden"/>
                <input name="task_id" value="<%=taskId%>" type="hidden"/>
                <%
                    com.redmoon.oa.workplan.Privilege pvg = new com.redmoon.oa.workplan.Privilege();
                    int checkStatus = 0;
                    if (pvg.canUserManageWorkPlan(request, workplanId)) {
                        checkStatus = 1;
                    }
                %>
                <input name="check_status" value="<%=checkStatus%>" type="hidden"/>
                <input name="add_date" value="<%=DateUtil.format(addDate, "yyyy-MM-dd")%>" type="hidden"/>
            </td>
        </tr>
        <tr>
            <td height="30" colspan="3" align="center">
                <input name="privurl" value="<%=privurl%>" type="hidden"/>
                <input class="btn" id="submitBtn" name="submitBtn" type="submit" value="确定"/>
                &nbsp;&nbsp;&nbsp;&nbsp;
                <input class="btn" id="backBtn" type="button" value="返回" onclick="window.history.back()"/>
            </td>
        </tr>
    </table>
</form>
</body>
<script>
    $(function () {
        $("#slider").slider({
            value:<%=wptd.getInt("progress")%>,
            min: 0,
            max: 100,
            step: 5,
            slide: function (event, ui) {
                $("#progress").val(ui.value);
            }
        });
    });
</script>
</html>