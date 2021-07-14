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
<%
    Privilege pvg = new Privilege();
    pvg.auth(request);
    String skey = pvg.getSkey();
    String userName = pvg.getUserName();
%>
<!DOCTYPE html>
<html>
<head>
    <title>考勤月报</title>
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
</head>
<body>
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
    <%
        String sjts = NumberUtil.round(SignMgr.getLeaveDays(userName, year, month, "事假"), 1);
        String bjts = NumberUtil.round(SignMgr.getLeaveDays(userName, year, month, "病假"), 1);
        String cjts = NumberUtil.round(SignMgr.getLeaveDays(userName, year, month, "产假"), 1);
        String njts = NumberUtil.round(SignMgr.getLeaveDays(userName, year, month, "年假"), 1);
        String hjts = NumberUtil.round(SignMgr.getLeaveDays(userName, year, month, "婚假"), 1);
        // double qtts = SignMgr.getLeaveDays(userName, year, month, "其它");

        double tripts = SignMgr.getTripDays(userName, year, month); // 出差

        // 应勤天数
        Date bd = DateUtil.getDate(year, month - 1, 1);
        int daysMonth = DateUtil.getDayCount(year, month - 1);
        Date ed = DateUtil.addDate(bd, daysMonth - 1);
        OACalendarDb oacal = new OACalendarDb();
        int yqdays = oacal.getWorkDayCount(DateUtil.addDate(bd, -1), ed);

        int[] kq = SignMgr.getAttendanceDays(userName, year, month);
        int days = kq[0]; // 出勤
        int nocount = kq[1]; // 旷工
        int abscount = kq[2]; // 缺勤

        int[] kqEarly = SignMgr.getAttendanceDays(userName, year, month, "earlycount"); // 早退
        int[] kqLate = SignMgr.getAttendanceDays(userName, year, month, "latecount"); // 迟到

        double wcts = SignMgr.getWcDays(userName, year, month);
    %>
    <ul class="mui-table-view">
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5"><b>项目</b></span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5"><b>时长</b></span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5"><b>数量</b></span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">应勤</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">-</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5"><%=yqdays%>天</span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">实际</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">-</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5"><%=days%>天</span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">迟到</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5"><%=kqLate[0]%>分</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5"><%=kqLate[0]%>次</span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">早退</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5"><%=kqEarly[0]%>分</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5"><%=kqEarly[0]%>次</span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">旷工</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">-</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5"><%=nocount%>次</span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">缺勤</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">-</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5"><%=abscount%>次</span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">出差</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">-</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5"><%=tripts%>天</span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">年假</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">-</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5"><%=njts%>天</span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">病假</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">-</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5"><%=bjts%>天</span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">事假</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">-</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5"><%=sjts%></span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">婚假</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">-</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5"><%=hjts%>天</span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">产假</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">-</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5"><%=cjts%>天</span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">外出</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5">-</span></div>
                <div class="mui-table-cell mui-col-xs-4"><span class="mui-h5"><%=wcts%>天</span></div>
            </div>
        </li>
    </ul>

</div>
<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="../js/mui.min.js"></script>
<script>
    $(function () {
        $('#year').val("<%=year%>");
        $('#month').val("<%=month%>");
        $('#year').change(function () {
            window.location.href = "kq_month_sheet.jsp?skey=<%=skey%>&year=" + $(this).val() + "&month=" + $('#month').val();
        })
        $('#month').change(function () {
            window.location.href = "kq_month_sheet.jsp?skey=<%=skey%>&year=" + $('#year').val() + "&month=" + $(this).val();
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