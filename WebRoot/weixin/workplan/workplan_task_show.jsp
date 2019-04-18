<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@ page import="com.redmoon.oa.android.Privilege" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.redmoon.oa.dept.DeptDb" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="com.redmoon.oa.person.UserMgr" %>
<%@ page import="com.redmoon.oa.workplan.*" %>
<%@ page import="com.redmoon.oa.basic.SelectOptionDb" %>
<%
    int id = ParamUtil.getInt(request, "id", -1);
    if (id == -1) {
        out.print(SkinUtil.LoadString(request, "err_id"));
        return;
    }

    Privilege pvg = new Privilege();
    if (!pvg.auth(request)) {
        out.print(StrUtil.p_center("请登录"));
        return;
    }
    String skey = pvg.getSkey();
    String userName = pvg.getUserName();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>任务详情</title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta content="telephone=no" name="format-detection"/>
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <link rel="stylesheet" href="../css/mui.css">
    <link rel="stylesheet" href="../css/my_dialog.css"/>
    <link rel="stylesheet" href="../css/photoswipe.css">
    <link rel="stylesheet" href="../css/photoswipe-default-skin/default-skin.css">
    <link href="../../lte/css/font-awesome.min.css?v=4.4.0" rel="stylesheet"/>
    <script type="text/javascript" src="../js/photoswipe.js"></script>
    <script type="text/javascript" src="../js/photoswipe-ui-default.js"></script>
    <script type="text/javascript" src="../js/photoswipe-init.js"></script>
    <style type="text/css">
        h5 {
            font-weight: bold;
            text-align: center;
            font-size: 16px;
            color: #000;
        }
        .createdate {
            font-size: 12px;
            color: #8f8f94;
        }
    </style>
    <script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="../css/mui.css"></script>
    <script type="text/javascript" src="../js/mui.min.js"></script>
    <script type="text/javascript" src="../js/mui.pullToRefresh.js"></script>
    <script type="text/javascript" src="../js/mui.pullToRefresh.material.js"></script>
    <script src="../js/jq_mydialog.js"></script>
</head>
<body>
<%
    WorkPlanTaskDb wptd = new WorkPlanTaskDb();
    wptd = (WorkPlanTaskDb)wptd.getQObjectDb(id);
    UserMgr um = new UserMgr();
    SelectOptionDb sod = new SelectOptionDb();

    String strStartDate = DateUtil.format(wptd.getDate("start_date"), "yyyy-MM-dd");
    String strEndDate = DateUtil.format(wptd.getDate("end_date"), "yyyy-MM-dd");
%>
<div class="mui-content">
    <div style="padding: 10px 10px;">
        <div id="segmentedControl" class="mui-segmented-control">
            <a class="mui-control-item mui-active task-detail">
                任务详情
            </a>
            <a class="mui-control-item annex-day">
                任务日报
            </a>
        </div>
    </div>
    <script>
        $(function() {
            mui('#segmentedControl').on('tap', '.annex-day', function () {
                mui.openWindow({
                    "url": "workplan_task_annex_day.jsp?taskId=<%=id%>"
                })
            });
        })
    </script>
    <ul class="mui-table-view">
        <li class="mui-table-view-cell" style="text-align:center"><b><%=wptd.getString("name") %>
        </b><br></li>
        <li class="mui-table-view-cell">
            <div class="mui-row">
                <div class="mui-col-sm-2" style="width:30%; text-align:left;">进度</div>
                <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=wptd.getInt("progress") %>
                </div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-row">
                <div class="mui-col-sm-2" style="width:30%; text-align:left;">进度</div>
                <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=wptd.getLong("id") %>
                </div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-row">
                <div class="mui-col-sm-2" style="width:30%; text-align:left;">责任人</div>
                <div class="mui-col-sm-2" style="width:70%; text-align:left;">
                    <%
                        if (!StrUtil.getNullStr(wptd.getString("task_resource")).equals("")) {
                            UserDb user = um.getUserDb(wptd.getString("task_resource"));
                    %>
                    <%=user.getRealName()%>
                    <%
                        }
                    %>
                </div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-row">
                <div class="mui-col-sm-2" style="width:30%; text-align:left;">剩余天数</div>
                <div class="mui-col-sm-2" style="width:70%; text-align:left;">
                    <%
                        int nowDays = DateUtil.datediff(wptd.getDate("end_date"), new Date());
                        if (wptd.getInt("progress") < 100) {
                            if (nowDays < 0) {
                    %>
                    <font color="red">过期<%=-nowDays%>天</font>
                    <%
                    } else {
                    %>
                    剩余<%=nowDays%>天
                    <%
                            }
                        }
                        WorkPlanAnnexDb wpad = new WorkPlanAnnexDb();
                    %>
                </div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-row">
                <div class="mui-col-sm-2" style="width:30%; text-align:left;">开始日期</div>
                <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=strStartDate %>
                </div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-row">
                <div class="mui-col-sm-2" style="width:30%; text-align:left;">结束日期</div>
                <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=strEndDate%>
                </div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-row">
                <div class="mui-col-sm-2" style="width:30%; text-align:left;">工作日</div>
                <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=wptd.getInt("duration")%>
                </div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-row">
                <div class="mui-col-sm-2" style="width:30%; text-align:left;">实际结束</div>
                <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=DateUtil.format(wpad.getRealCompleteDate(wptd.getLong("id")), "yyyy-MM-dd")%>
                </div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-row">
                <div class="mui-col-sm-2" style="width:30%; text-align:left;">评价</div>
                <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=sod.getOptionName("workplan_assess", wptd.getString("assess"))%>
                </div>
            </div>
        </li>
    </ul>
</div>
<script>
    function callJS() {
        return {"btnAddShow": 0, "btnAddUrl": ""};
    }
    var iosCallJS = '{ "btnAddShow":0, "btnAddUrl":"" }';
</script>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=pvg.getSkey()%>"/>
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>
