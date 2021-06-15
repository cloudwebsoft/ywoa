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
    int workplan_annex_week_add_limit = cfg.getInt("workplan_annex_week_add_limit");
    int workplan_annex_week_edit_limit = cfg.getInt("workplan_annex_week_edit_limit");
%>
<!DOCTYPE html>
<html>
<head>
    <title>计划周报</title>
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
        <a class="mui-control-item mui-active annex-week">
            周报
        </a>
        <a class="mui-control-item annex-month">
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
                mui('#segmentedControl').on('tap', '.annex-month', function () {
                    mui.openWindow({
                        "url": "workplan_annex_list_month.jsp?id=<%=id%>"
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
%>
<div class="mui-content">
    <ul class="mui-table-view">
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-4 mui-select">
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
                    &nbsp;年
                </div>
                <div class="mui-table-cell mui-col-xs-4 mui-select">
                    <select id="month" name="month">
                        <%
                            for (int i = 1; i < 13; i++) {
                        %>
                        <option value="<%=i%>"><%=i%>
                        </option>
                        <%
                            }
                        %>
                    </select>
                </div>
                <div class="mui-table-cell mui-col-xs-2">
                    &nbsp;月
                </div>
            </div>
        </li>
    </ul>
    <ul class="mui-table-view">
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-1"><span class="mui-h5"><b>周</b></span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5"><b>时间</b></span></div>
                <div class="mui-table-cell mui-col-xs-3"><span class="mui-h5"><b>汇报人</b></span></div>
                <div class="mui-table-cell mui-col-xs-2"><span class="mui-h5"><b>进度</b></span></div>
            </div>
        </li>
        <%
            int dd = DateUtil.getDayCount(year, month - 1);
            //得到每月的第一天和最后一天是一年的第几周
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Calendar c = Calendar.getInstance();
            c.setFirstDayOfWeek(Calendar.MONDAY);

            // 当前周数
            int curw = c.get(Calendar.WEEK_OF_YEAR);

            c.setTime(df.parse(year + "-" + month + "-" + "1"));
            int e = c.get(Calendar.DAY_OF_WEEK) - 1;//每月的第一天是星期几
            if (e == 0) {
                e = 7;
            }
            int ww[] = new int[2];
            ww[0] = c.get(Calendar.WEEK_OF_YEAR);
            c.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(year + "-" + month + "-" + dd));
            // c.setMinimalDaysInFirstWeek(7);

            int week1 = c.get(Calendar.WEEK_OF_YEAR);
            ww[1] = week1 + 1;
            int k = 1;
            int temp1 = dd;

            Calendar current = Calendar.getInstance();
            int currentYear = current.get(Calendar.YEAR);
            int currentMonth = current.get(Calendar.MONTH) + 1;
            int currentDay = current.get(Calendar.DATE);
            int count = 1;
            int num = 0;
            boolean b = false;

            UserMgr um = new UserMgr();
            WorkPlanAnnexDb wad = new WorkPlanAnnexDb();
            WorkPlanAnnexAttachment wfaa = new WorkPlanAnnexAttachment();
            com.redmoon.oa.workplan.Privilege pvgWorkplan = new com.redmoon.oa.workplan.Privilege();

            if (ww[0] > ww[1]) {
                c.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(year + "-" + month + "-" + (dd - 7)));
                ww[1] = c.get(Calendar.WEEK_OF_YEAR);
            }

            for (int i = ww[0]; i <= ww[1]; i++) {
                WorkPlanAnnexDb wpa = wad.getWorkPlanAnnexDb(id, year, WorkPlanAnnexDb.TYPE_WEEK, i);
        %>
        <li class="mui-table-view-cell">
            <div class="mui-slider-right mui-disabled"><a class="mui-btn mui-btn-red att_del">删除</a></div>
            <div class="mui-slider-handle">
                <div class="mui-table" annexId="<%=wpa!=null ? wpa.getLong("id") : -1%>">
                    <div class="mui-table-cell mui-col-xs-1"><span class="mui-h5"><%=i%></span></div>
                    <div class="mui-table-cell mui-col-xs-4">
                        <span class="mui-h5">
                <%
                    Calendar calendar = Calendar.getInstance();
                    calendar.clear();
                    calendar.setFirstDayOfWeek(Calendar.MONDAY);
                    calendar.set(Calendar.WEEK_OF_YEAR, i);
                    calendar.set(Calendar.YEAR, year);

                    // Now get the first day of week.
                    Date date = calendar.getTime();
                    Date date1 = DateUtil.addDate(date, 6);
                %>
                    <%=DateUtil.format(date, "MM-dd")%>
                    至
                    <%=DateUtil.format(date1, "MM-dd")%>
                        </span>
                    </div>
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
                                if ((expireDays >= 0 && expireDays <= workplan_annex_week_add_limit) || (now.before(DateUtil.addDate(date1, 1)) && now.after(date))) {
                            %>
                            <input type="button" class="mui-btn add-btn" workplanId="<%=id%>" annexYear="<%=year%>" annexItem="<%=i%>" annexType="<%=WorkPlanAnnexDb.TYPE_WEEK%>" value="汇报"/>
                            <%
                                }
                            }
                        } else {
                            Date addDate = wpa.getDate("add_date");
                            if (DateUtil.datediff(new java.util.Date(), addDate) <= workplan_annex_week_edit_limit) {
                                %>
                            <input type="button" class="mui-btn edit-btn" annexId="<%=wpa.getLong("id")%>" workplanId="<%=id%>" annexType="<%=WorkPlanAnnexDb.TYPE_WEEK%>" annexYear="<%=year%>" annexItem="<%=i%>" value="修改"/>
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
            window.location.href = "workplan_annex_list_week.jsp?id=<%=id%>&skey=<%=skey%>&year=" + $(this).val() + "&month=" + $('#month').val();
        })

        $('#month').change(function () {
            window.location.href = "workplan_annex_list_week.jsp?id=<%=id%>&skey=<%=skey%>&year=" + $('#year').val() + "&month=" + $(this).val();
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