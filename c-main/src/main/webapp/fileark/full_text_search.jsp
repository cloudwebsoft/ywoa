<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="org.apache.lucene.search.*,org.apache.lucene.document.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>全文检索管理</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>

    <script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
    <script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
    <script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
    <script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>
    <style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>

    <script src="../inc/common.js"></script>
        <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
</head>
<body>
<jsp:useBean id="cfg" scope="page" class="cn.js.fan.web.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "admin")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String op = ParamUtil.get(request, "op");
    if (op.trim().equals("create")) {
        String sFromDate = ParamUtil.get(request, "fromDate");
        String sToDate = ParamUtil.get(request, "toDate");
        sFromDate = sFromDate.replace("/", "-") + ":00";
        sToDate = sToDate.replace("/", "-") + ":00";
        com.redmoon.oa.fileark.Document document = new com.redmoon.oa.fileark.Document();
        Indexer indexer = new Indexer();
        java.util.Date beginDate = null;
        java.util.Date endDate = null;
        beginDate = DateUtil.parse(sFromDate, "yyyy-MM-dd HH:mm:ss");
        endDate = DateUtil.parse(sToDate, "yyyy-MM-dd HH:mm:ss");

        if (document.index(indexer, beginDate, endDate, true)) {
            out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "提示", "full_text_search.jsp"));
        } else {
            out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_fail"), "提示", "full_text_search.jsp"));
        }
        return;
    }
    if (op.equals("regenerate")) {
        String sFromDate = ParamUtil.get(request, "fromDate");
        String sToDate = ParamUtil.get(request, "toDate");
        //String sFromTime = ParamUtil.get(request, "fromTime");
        //String sToTime = ParamUtil.get(request, "toTime");
        sFromDate = sFromDate.replace("/", "-") + ":00";
        sToDate = sToDate.replace("/", "-") + ":00";
        com.redmoon.oa.fileark.Document document = new com.redmoon.oa.fileark.Document();
        Indexer indexer = new Indexer();
        java.util.Date beginDate = null;
        java.util.Date endDate = null;

        beginDate = DateUtil.parse(sFromDate, "yyyy-MM-dd HH:mm:ss");
        endDate = DateUtil.parse(sToDate, "yyyy-MM-dd HH:mm:ss");

        if (document.index(indexer, beginDate, endDate, false)) {
            out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "提示", "full_text_search.jsp"));
        } else {
            out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_fail"), "提示", "full_text_search.jsp"));
        }
    }
%>
<table cellSpacing="0" cellPadding="0" width="100%">
    <tbody>
    <tr>
        <td class="tdStyle_1">全文检索索引</td>
    </tr>
    </tbody>
</table>
<br>
<form name="form1" method="post" action="?op=create">
    <TABLE class="tabStyle_1 percent60" cellSpacing=0 cellPadding=3 width="95%" align=center>
        <thead>
        <TR>
            <TD class="tabStyle_1_title" colspan="2" align="center" noWrap>全文检索</TD>
        </TR>
        </thead>
        <TBODY>
        <TR>
            <TD width="32%" align="right">开始时间</TD>
            <TD width="68%">
                <input id="fromDate" name="fromDate" size="18" readonly="">
            </TD>
        </TR>
        <TR>
            <TD align="right">结束时间</TD>
            <TD><input id="toDate" name="toDate" size="18" readonly="">
            </TD>
        </TR>
        <TR>
            <TD colspan="2" align="center"><input class="btn" name="submit" type=submit value="增量生成">
                &nbsp;&nbsp;&nbsp;
                <input name="submit2" class="btn" type=button value="全部重新生成" onClick="allReset();"></TD>
        </TR>
        <TR>
            <TD colspan="2" align="left"><p>1、选择开始时间和结束时间，根据时间段生成全文检索的索引<br>
                2、点击全部重新生成，将会生成所有的索引<br>
                3、系统在生成索引时，会形成“<a href="javascript:;" onclick="addTab('配置管理', 'admin/config.do')">全文检索的时间戳</a>”，该时间戳在配置中可以管理</p></TD>
        </TR>
        </TBODY>
    </TABLE>
</form>
</body>
<script>
    $(function () {
        $('#fromDate').datetimepicker({value: '', step: 10});
        $('#toDate').datetimepicker({value: '', step: 10});
    });

    function allReset() {
        var fromDate = document.getElementById("fromDate").value;
        var toDate = document.getElementById("toDate").value;
        window.location.href = '?op=regenerate&fromDate=' + fromDate + '&toDate=' + toDate;
    }
</script>
</html>