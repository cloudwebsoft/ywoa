<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>统计信息</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script type="text/javascript" src="../js/flexigrid.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
</head>
<body>
<jsp:useBean id="cfg" scope="page" class="cn.js.fan.web.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, PrivDb.PRIV_BACKUP)) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    String rootpath = request.getContextPath();
    String bakpath = cfg.getProperty("Application.bak_path");
%>
<table cellSpacing="0" cellPadding="0" width="100%">
    <tbody>
    <tr>
        <td class="tdStyle_1">统计信息</td>
    </tr>
    </tbody>
</table>
<table id="grid">
    <thead>
    <tr>
        <th width="360">模块</th>
        <th width="360">数量</th>
        <th width="360">统计</th>
    </tr>
    </thead>
    <tr>
        <td>&nbsp;<a href="flow_predefine_frame.jsp" target="_blank">流程</a></td>
        <td>
            <%
                String sql = "select count(*) from flow where status=" + WorkflowDb.STATUS_NOT_STARTED;
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql);
                int countNot = 0;
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    countNot = rr.getInt(1);
                }
                out.print("<a href='flow_list.jsp?op=search&status=" + WorkflowDb.STATUS_NOT_STARTED + "' target=_blank>" + WorkflowDb.getStatusDesc(WorkflowDb.STATUS_NOT_STARTED) + "：" + countNot + "</a><BR>");

                sql = "select count(*) from flow where status=" + WorkflowDb.STATUS_STARTED;
                ri = jt.executeQuery(sql);
                int countStarted = 0;
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    countStarted = rr.getInt(1);
                }
                out.print("<a href='flow_list.jsp?op=search&status=" + WorkflowDb.STATUS_STARTED + "' target=_blank>" + WorkflowDb.getStatusDesc(WorkflowDb.STATUS_STARTED) + "：" + countStarted + "</a><BR>");

                sql = "select count(*) from flow where status=" + WorkflowDb.STATUS_FINISHED;
                ri = jt.executeQuery(sql);
                int countFinished = 0;
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    countFinished = rr.getInt(1);
                }
                out.print("<a href='flow_list.jsp?op=search&status=" + WorkflowDb.STATUS_FINISHED + "' target=_blank>" + WorkflowDb.getStatusDesc(WorkflowDb.STATUS_FINISHED) + "：" + countFinished + "</a><BR>");

                sql = "select count(*) from flow where status=" + WorkflowDb.STATUS_DISCARDED;
                ri = jt.executeQuery(sql);
                int countDiscarded = 0;
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    countDiscarded = rr.getInt(1);
                }
                out.print("<a href='flow_list.jsp?op=search&status=" + WorkflowDb.STATUS_DISCARDED + "' target=_blank>" + WorkflowDb.getStatusDesc(WorkflowDb.STATUS_DISCARDED) + "：" + countDiscarded + "</a><BR>");
            %><a href="flow_list.jsp" target=_blank>总数：<%=countNot + countStarted + countFinished + countDiscarded%>
        </a></td>
        <td><a href="flow_stat_month.jsp" target="_blank">月统计</a>&nbsp;&nbsp;&nbsp;<a href="flow_stat_year.jsp" target="_blank">年统计</a></td>
    </tr>
    <tr>
        <td>&nbsp;<a href="../notice/notice_list.jsp" target="_blank">通知</a></td>
        <td>
            <%
                sql = "select count(*) from oa_notice";
                ri = jt.executeQuery(sql);
                long c = 0;
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    c = rr.getLong(1);
                }
                out.print(c);
            %></td>
        <td><a href="../notice/notice_stat_year.jsp?isShowNav=0" target="_blank">年统计</a></td>
    </tr>
    <!--
          <tr>
            <td height="33">&nbsp;</td>
            <td height="22"><img src="images/arrow.gif" align="absmiddle">&nbsp;<a href="bak_do.jsp?op=db">备份数据库</a></td>
            <td>&nbsp;</td>
          </tr>
          -->
    <tr>
        <td>&nbsp;<a href="../fileark/fileark_frame.jsp" target="_blank">文件柜</a></td>
        <td>目录数：
            <%
                sql = "select count(*) from directory";
                ri = jt.executeQuery(sql);
                c = 0;
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    c = rr.getInt(1);
                }
                out.print(c);
            %>
            <br/>
            文章数：
            <%
                sql = "select count(*) from document";
                ri = jt.executeQuery(sql);
                c = 0;
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    c = rr.getInt(1);
                }
                out.print(c);
            %></td>
        <td><a href="../fileark/fileark_stat_year.jsp?isShowNav=0" target="_blank">年统计</a></td>
    </tr>
    <tr>
        <td>&nbsp;工作计划</td>
        <td>
            <%
                sql = "select count(*) from work_plan";
                ri = jt.executeQuery(sql);
                c = 0;
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    c = rr.getInt(1);
                }
                out.print(c);
            %></td>
        <td><a href="../workplan/workplan_stat_year.jsp?isShowNav=0" target="_blank">年统计</a></td>
    </tr>
    <tr>
        <td>&nbsp;工作报告</td>
        <td><%
            sql = "select count(*) from work_log";
            ri = jt.executeQuery(sql);
            c = 0;
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                c = rr.getInt(1);
            }
            out.print(c);
        %></td>
        <td><a href="../mywork/stat_year.jsp?isShowNav=0" target="_blank">年统计</a></td>
    </tr>
    <tr>
        <td>&nbsp;内部消息</td>
        <td><%
            sql = "select count(*) from oa_message";
            ri = jt.executeQuery(sql);
            c = 0;
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                c = rr.getInt(1);
            }
            out.print(c);
        %></td>
        <td><a href="../message_oa/message_stat_year.jsp?isShowNav=0" target="_blank">年统计</a></td>
    </tr>
    <tr>
        <td>&nbsp;网络硬盘</td>
        <td><%
            sql = "select count(*) from netdisk_document_attach";
            ri = jt.executeQuery(sql);
            c = 0;
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                c = rr.getInt(1);
            }
            out.print("文件" + c + "个，&nbsp;占用空间");
            sql = "select sum(diskSpaceUsed) from users";
            ri = jt.executeQuery(sql);
            c = 0;
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                c = rr.getLong(1);
            }
            out.print(NumberUtil.round((double) c / 1024000, 3) + "&nbsp;M");
        %>
        </td>
        <td><a href="../netdisk/netdisk_stat_year.jsp?isShowNav=0" target="_blank">年统计</a></td>
    </tr>
</table>
<script>
    $(document).ready(function () {
        flex = $("#grid").flexigrid
        (
            {
                url: false,
                usepager: true,
                checkbox: false,
                useRp: false,

                // title: "通知",
                singleSelect: true,
                resizable: false,
                showTableToggleBtn: true,
                showToggleBtn: true,

                onReload: onReload,
                /*
                onRowDblclick: rowDbClick,
                onColSwitch: colSwitch,
                onColResize: colResize,
                onToggleCol: toggleCol,
                */
                autoHeight: true,
                width: document.documentElement.clientWidth,
                height: document.documentElement.clientHeight - 84
            }
        );
    });

    function onReload() {
        window.location.reload();
    }
</script>
</body>
</html>