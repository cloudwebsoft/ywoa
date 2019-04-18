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
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%
    Privilege pvg = new Privilege();
    pvg.auth(request);
    String skey = pvg.getSkey();
    String userName = pvg.getUserName();
%>
<!DOCTYPE html>
<html>
<head>
    <title>我的工资</title>
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

    int year = ParamUtil.getInt(request, "year", curYear);
%>
<div class="mui-content">
    <ul class="mui-table-view">
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-5"><span class="mui-h5">年份</span></div>
                <div class="mui-table-cell mui-col-xs-5 mui-select"> <span class="mui-h5">
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
                </span></div>
            </div>
        </li>
    </ul>
    <%
        FormDAO fdao = PersonMgr.getPerson(userName);
        if (fdao==null) {
            out.print(StrUtil.p_center("人员未建档！"));
            return;
        }
        long personId = fdao.getId();
        String sqlSubject = "select subject from salary_book_subject where book_id=? and year=? and month=? order by id asc";

        String sql = "select * from salary_payroll where person_id=? and year=? order by month asc";
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = jt.executeQuery(sql, new Object[]{personId, year});
        if (ri.size() == 0) {
            out.print("<div style='text-align:center; margin:10px'>无工资记录</div>");
        }
        while (ri.hasNext()) {
            ResultRecord rr = (ResultRecord) ri.next();

            int bookId = rr.getInt("book_id");
            int month = rr.getInt("month");
    %>
    <ul class="mui-table-view">
        <div style="text-align: center; font-weight: bold; line-height: 40px"><%=month%>月</div>
        <%
            ResultIterator riSubject = jt.executeQuery(sqlSubject, new Object[]{bookId, year, month});
            while (riSubject.hasNext()) {
                ResultRecord rrSbuject = (ResultRecord) riSubject.next();
                String subjectCode = rrSbuject.getString(1);
                // System.out.println(getClass() + " subjectCode=" + subjectCode);
                com.redmoon.oa.visual.FormDAO fdaoSubject = SalaryMgr.getSubject(subjectCode);
                String subjectName;
                String val = "";
                if (fdaoSubject==null) {
                    subjectName = subjectCode + "不存在";
                }
                else {
                    subjectName = fdaoSubject.getFieldValue("name");
                    int decimals = StrUtil.toInt(fdaoSubject.getFieldValue("decimals"), 2);
                    val = NumberUtil.round(rr.getDouble(subjectCode), decimals);
                }
        %>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-5"><span class="mui-h5"><%=subjectName%></span></div>
                <div class="mui-table-cell mui-col-xs-5"><span class="mui-h5"><%=val%></span></div>
            </div>
        </li>
        <%
            }
        %>
    </ul>
    <%
        }
    %>
</div>
<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="../js/mui.min.js"></script>
<script>
    $(function() {
        $('#year').val("<%=year%>");
        $('#year').change(function () {
            window.location.href = "payroll_my_list.jsp?skey=<%=skey%>&year=" + $(this).val();
        })
    })

    function callJS() {
        return {"btnAddShow": 0, "btnAddUrl": "", "btnBackUrl": ""};
    }

    var iosCallJS = '{ "btnAddShow":0, "btnAddUrl":"", "btnBackUrl":"" }';
</script>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=skey%>" />
    <jsp:param name="tabId" value="app" />
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>
