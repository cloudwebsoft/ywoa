<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.worklog.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "admin.flow";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String typeCode = ParamUtil.get(request, "typeCode");
    if (typeCode.equals("")) {
        if (!privilege.isUserPrivValid(request, "admin"))
            return;
    }

    int showyear;
    Calendar cal = Calendar.getInstance();
    int curyear = cal.get(Calendar.YEAR);
    String strshowyear = request.getParameter("showyear");
    if (strshowyear != null) {
        showyear = Integer.parseInt(strshowyear);
    } else {
        showyear = cal.get(Calendar.YEAR);
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>流程绩效-人员年统计</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <%@ include file="../inc/nocache.jsp" %>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script type="text/javascript" src="../js/highcharts/highcharts.js"></script>
    <script type="text/javascript" src="../js/highcharts/highcharts-3d.js"></script>
    <script type="text/javascript" src="../js/swfobject.js"></script>
    <script type="text/javascript">
        //swfobject.embedSWF(
        //  "../flash/open-flash-chart.swf", "lineChart",
        //  "1500", "380", "9.0.0", "expressInstall.swf",
        //  {"data-file":"flow_performance_data_person_year.jsp<%=StrUtil.UrlEncode("?typeCode=" + StrUtil.UrlEncode(typeCode) + "&showyear=" + showyear)%>"} );

        $(function () {
            var url = "flow_performance_data_person_year.jsp?typeCode=<%=StrUtil.UrlEncode(typeCode) %>&showyear=<%= showyear%>";
            $.ajax({
                type: "get",
                url: url,
                success: function (data, status) {
                    data = $.parseJSON(data);
                    $('#lineChart').highcharts({
                        chart: {
                            type: 'column',
                            margin: 45,
                            options3d: {
                                enabled: true,
                                alpha: 0,
                                beta: 0,
                                depth: 70
                            }
                        },
                        title: {
                            text: data.leafName + "<%=showyear%> 年统计   共计：" + data.total
                        },
                        plotOptions: {
                            column: {
                                depth: 25
                            }
                        },
                        legend: {
                            enabled: false
                        },
                        xAxis: {
                            categories: ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"]
                        },
                        yAxis: {
                            min: 0,
                            opposite: false
                        },
                        series: [{
                            data: data.datas
                        }]
                    });
                },
                error: function (XMLHttpRequest, textStatus) {
                    alert(XMLHttpRequest.responseText);
                }
            });

        });
    </script>
</head>
<body>
<%
    if (!privilege.isUserLogin(request)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));

        return;
    }
%>
<%@ include file="flow_performance_stat_menu_top.jsp" %>
<script>
    o("menu4").className = "current";
</script>
<br/>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
        <td height="30" align="center">
            <select name="showyear"
                    onchange="var y=this.options[this.selectedIndex].value; window.location.href='?typeCode=<%=StrUtil.UrlEncode(typeCode)%>&showyear=' + y;">
                <%for (int y = curyear; y >= curyear - 60; y--) {%>
                <option value="<%=y%>"><%=y%>
                </option>
                <%}%>
            </select>
            <script>
                o("showyear").value = "<%=showyear%>";
            </script>
    </tr>
    <tr>
        <td align="center">
            <div id="lineChart"></div>
        </td>
    </tr>
    <tr>
        <td height="30" align="center">&nbsp;</td>
    </tr>
</table>
</body>
</html>
