<%@page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.cloudwebsoft.framework.db.*" %>
<%@page import="com.redmoon.oa.hr.*" %>
<%@page import="com.redmoon.oa.visual.*" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="cn.js.fan.util.*" %>
<%@page import="cn.js.fan.db.*" %>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%@ page import="com.raq.expression.function.math.Sign" %>
<%@ page import="com.redmoon.oa.oacalendar.OACalendarDb" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="com.redmoon.oa.workplan.WorkPlanAnnexAttachment" %>
<%@ page import="com.redmoon.oa.workplan.WorkPlanAnnexDb" %>
<%@ page import="com.redmoon.oa.person.UserMgr" %>
<%@ page import="com.redmoon.oa.workplan.WorkPlanDb" %>
<%
    Privilege pvg = new Privilege();
    if (!pvg.auth(request)) {
        out.print(StrUtil.p_center("请登录"));
        return;
    }
    String skey = pvg.getSkey();
    int id = ParamUtil.getInt(request, "id", -1);
    WorkPlanDb wpd = new WorkPlanDb();
    wpd = wpd.getWorkPlanDb(id);
    if (!wpd.isLoaded()) {
        out.print(StrUtil.p_center("计划不存在！"));
        return;
    }

    com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    int workplan_annex_month_add_limit = cfg.getInt("workplan_annex_month_add_limit");
    int workplan_annex_month_edit_limit = cfg.getInt("workplan_annex_month_edit_limit");
%>
<!DOCTYPE html>
<html>
<head>
    <title>计划月报</title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection"/>
    <link rel="stylesheet" href="../css/mui.css"/>
    <link rel="stylesheet" href="../css/mui.picker.min.css"/>
    <link rel="stylesheet" href="../css/at_flow.css"/>
    <link rel="stylesheet" href="../css/my_dialog.css"/>
    <link href="../../lte/css/bootstrap.min.css?v=3.3.6" rel="stylesheet">
    <link href="../../lte/css/font-awesome.css?v=4.4.0" rel="stylesheet">
    <link href="../../lte/css/animate.css" rel="stylesheet">
    <link href="../../lte/css/style.css?v=4.1.0" rel="stylesheet">
    <style type="text/css">
        body {
            font-size: 17px;
            background-color: #efeff4;
        }
    </style>
    <script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="../js/mui.min.js"></script>
</head>
<body>
<div style="padding: 10px 10px;">
    <div id="segmentedControl" class="mui-segmented-control mui-segmented-control-inverted">
        <a class="mui-control-item workplan-task">
            任务
        </a>
        <a class="mui-control-item workplan-detail">
            详情
        </a>
        <a class="mui-control-item annex-day">
            日报
        </a>
        <a class="mui-control-item annex-week">
            周报
        </a>
        <a class="mui-control-item mui-active annex-month">
            月报
        </a>
        <script>
            $(function() {
                mui('#segmentedControl').on('tap', '.workplan-task', function () {
                    mui.openWindow({
                        "url": "workplan_show.jsp?id=<%=id%>"
                    })
                });
                mui('#segmentedControl').on('tap', '.workplan-detail', function () {
                    mui.openWindow({
                        "url": "workplan_show.jsp?id=<%=id%>&action=detail"
                    })
                });
                mui('#segmentedControl').on('tap', '.annex-day', function () {
                    mui.openWindow({
                        "url": "workplan_annex_day.jsp?id=<%=id%>"
                    })
                });
                mui('#segmentedControl').on('tap', '.annex-week', function () {
                    mui.openWindow({
                        "url": "workplan_annex_list_week.jsp?id=<%=id%>"
                    })
                });
            })
        </script>
    </div>
</div>
<%
    Date dt = new Date();
    int curYear = DateUtil.getYear(dt);
    int curMonth = DateUtil.getMonth(dt) + 1;

    int year = ParamUtil.getInt(request, "year", curYear);
    int month = ParamUtil.getInt(request, "month", curMonth);

    WorkPlanAnnexDb wad = new WorkPlanAnnexDb();
    UserMgr um = new UserMgr();
    com.redmoon.oa.workplan.Privilege pvgWorkplan = new com.redmoon.oa.workplan.Privilege();
%>
<div class="mui-content">
    <ul class="mui-table-view">
        <li class="mui-table-view-cell">
            <div class="mui-table" style="text-align: center;">
                <div class="mui-table-cell mui-col-xs-1 mui-select">
                    <select id="year" name="year">
                        <%
                            for (int i = curYear; i > curYear - 20; i--) {
                        %>
                        <option value="<%=i%>"><%=i %>
                        </option>
                        <%
                            }
                        %>
                    </select>
                </div>
                <div class="mui-table-cell mui-col-xs-2">
                    年
                </div>
            </div>
        </li>
    </ul>
    <ul class="mui-table-view">
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-1"><span class="mui-h5"><b>月</b></span></div>
                <div class="mui-table-cell mui-col-xs-3"><span class="mui-h5"><b>汇报人</b></span></div>
                <div class="mui-table-cell mui-col-xs-2"><span class="mui-h5"><b>进度</b></span></div>
            </div>
        </li>
        <%
            for (int i = 1; i <= 12; i++) {
                WorkPlanAnnexDb wpa = wad.getWorkPlanAnnexDb(id, year, WorkPlanAnnexDb.TYPE_MONTH, i);
                int day = DateUtil.getDayCount(year, i-1);
                Date date = DateUtil.getDate(year, i-1, 1);
                Date date1 = DateUtil.getDate(year, i-1, day);
        %>
        <li class="mui-table-view-cell">
            <div class="mui-slider-right mui-disabled"><a class="mui-btn mui-btn-red att_del">删除</a></div>
            <div class="mui-slider-handle">
                <div class="mui-table" annexId="<%=wpa!=null ? wpa.getLong("id") : -1%>">
                    <div class="mui-table-cell mui-col-xs-1"><span class="mui-h5"><%=i%>月</span></div>
                    <div class="mui-table-cell mui-col-xs-3">
                        <span class="mui-h5">
                            <%
                                if (wpa != null) {
                            %>
                            <%=um.getUserDb(wpa.getString("user_name")).getRealName()%>
                            <%
                                }
                            %>
                        </span>
                    </div>
                    <div class="mui-table-cell mui-col-xs-2">
                        <span class="mui-h5">
                            <%
                                if (wpa!=null) {
                                    out.print(wpa.getInt("progress") + "%");
                                }
                            %>
                        </span>
                    </div>
                    <div class="mui-table-cell mui-col-xs-2">
                        <span class="mui-h5">
                <%
                    if (pvgWorkplan.canUserManageWorkPlan(request, id)) {
                        if (wpa == null) {
                            Date now = new java.util.Date();
                            if (now.before(wpd.getEndDate()) && now.after(wpd.getBeginDate())) {
                                int expireDays = DateUtil.datediff(now, date1);
                                // 处于一周的最后一天或在规定的超期范围内
                                if ((expireDays >= 0 && expireDays <= workplan_annex_month_add_limit) || (now.before(DateUtil.addDate(date1, 1)) && now.after(date))) {
                            %>
                            <input type="button" class="mui-btn add-btn" workplanId="<%=id%>" annexYear="<%=year%>" annexItem="<%=i%>" annexType="<%=WorkPlanAnnexDb.TYPE_MONTH%>" value="汇报"/>
                            <%
                                }
                            }
                        } else {
                            Date addDate = wpa.getDate("add_date");
                            if (DateUtil.datediff(new java.util.Date(), addDate) <= workplan_annex_month_edit_limit) {
                                %>
                            <input type="button" class="mui-btn edit-btn" annexId="<%=wpa.getLong("id")%>" workplanId="<%=id%>" annexType="<%=WorkPlanAnnexDb.TYPE_MONTH%>" annexYear="<%=year%>" annexItem="<%=i%>" value="修改"/>
                                <%
                            }
                        }
                    }
                %>
                        </span>
                    </div>
                </div>
            </div>
        </li>
        <%
            }
        %>
    </ul>
</div>
<script>
    $(function () {
        $('#year').val("<%=year%>");
        $('#month').val("<%=month%>");
        $('#year').change(function () {
            window.location.href = "workplan_annex_list_month.jsp?id=<%=id%>&skey=<%=skey%>&year=" + $(this).val();
        })

        $('.add-btn').click(function() {
            mui.openWindow({
                "url": "workplan_annex_add.jsp?id=<%=id%>&annexYear=" + $(this).attr('annexYear') + "&annexItem=" + $(this).attr('annexItem') + "&annexType=" + $(this).attr('annexType')
            })
        })

        $('.edit-btn').click(function() {
            mui.openWindow({
                "url": "workplan_annex_edit.jsp?id=<%=id%>&annexId=" + $(this).attr('annexId') + "&annexType=" + $(this).attr('annexType') + "&year=<%=year%>&month=<%=month%>"
            })
            return false;
        })
        
        $('.mui-table').click(function () {
            var annexId = $(this).attr('annexId');
            if (annexId && annexId!="-1") {
                mui.openWindow({
                    "url": "workplan_annex_show.jsp?annexId=" + annexId
                })
            }
        })
    })

    function callJS() {
        return {"btnAddShow": 0, "btnAddUrl": "", "btnBackUrl": ""};
    }

    var iosCallJS = '{ "btnAddShow":0, "btnAddUrl":"", "btnBackUrl":"" }';
</script>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=pvg.getSkey()%>" />
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>