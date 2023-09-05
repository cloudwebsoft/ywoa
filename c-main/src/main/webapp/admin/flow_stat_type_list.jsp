<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@page import="com.cloudwebsoft.framework.util.LogUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String isFlow = ParamUtil.get(request, "isFlow");

    String curUnitCode = ParamUtil.get(request, "unitCode");
    if ("".equals(curUnitCode)) {
        curUnitCode = privilege.getUserUnitCode(request);
    }
    if (!privilege.canUserAdminUnit(request, curUnitCode)) {
        // 检查用户能否管理该单位
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>表单管理</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <%@ include file="../inc/nocache.jsp" %>
    <script src="../inc/common.js"></script>
    <script language="JavaScript" type="text/JavaScript">
        <!--
        function presskey(eventobject) {
            if (event.ctrlKey && window.event.keyCode == 13) {
                <%if (isFlow.equals("")) {%>
                window.location.href = "?isFlow=0";
                <%}else{%>
                window.location.href = "?";
                <%}%>
            }
        }

        document.onkeydown = presskey;
        //-->
    </script>
</head>
<body>
<%
    if (!privilege.isUserPrivValid(request, "admin.flow")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
    String flowTypeName = "";
    if (!"".equals(flowTypeCode)) {
        Leaf flf = new Leaf();
        flf = flf.getLeaf(flowTypeCode);
        flowTypeName = flf.getName();

        LeafPriv lp = new LeafPriv(flowTypeCode);
        if (!(lp.canUserExamine(privilege.getUser(request)))) {
            // out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
            out.println(cn.js.fan.web.SkinUtil.makeInfo(request, "请选择流程"));
            return;
        }
    } else {
        if (!privilege.isUserPrivValid(request, "admin.flow")) {
            out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }
    }

    String op = ParamUtil.get(request, "op");
    Calendar cal = Calendar.getInstance();
    int cury = cal.get(Calendar.YEAR);
    int y = ParamUtil.getInt(request, "showyear", cury);
%>
<table width="98%" class="percent98">
    <tr>
        <td align="center">
            <form name="formSearch" action="flow_type_performance_list.jsp" method="get">
                &nbsp;&nbsp;年度
                <select id="showyear" name="showyear"
                        onchange="var y=this.options[this.selectedIndex].value; window.location.href='flow_stat_type_list.jsp?showyear=' + y;">
                    <%for (int y2 = cury - 12; y2 <= cury; y2++) {%>
                    <option value="<%=y2%>"><%=y2%>
                    </option>
                    <%}%>
                </select>
                <script>
                    o("showyear").value = "<%=y%>";
                </script>
            </form>
        </td>
    </tr>
</table>
<form action="form_m.jsp" method=post>
    <table class="tabStyle_1 percent98" width="93%" align="center" cellpadding="3" cellspacing="0">
        <tr>
            <td class="tabStyle_1_title" width="12%" height="25" align="center">名称</td>
            <td class="tabStyle_1_title" width="9%" align="center">未开始</td>
            <td class="tabStyle_1_title" width="9%" align="center">处理中</td>
            <td class="tabStyle_1_title" width="9%" align="center">已结束</td>
            <td class="tabStyle_1_title" width="9%" align="center">已放弃</td>
            <td class="tabStyle_1_title" width="9%" align="center">合计</td>
            <td class="tabStyle_1_title" width="9%" align="center">每月平均</td>
            <td class="tabStyle_1_title" width="9%" align="center">每日平均</td>
            <td class="tabStyle_1_title" width="24%" height="25" align="center">统计</td>
        </tr>
        <%
            FormDb ftd = new FormDb();
            String sql = "";
            if ("0".equals(isFlow)) {
                sql = "select code from " + ftd.getTableName() + " where isFlow=0 and unit_code=" + StrUtil.sqlstr(curUnitCode) + " order by code asc";
            } else {
                if (!"".equals(flowTypeCode)) {
                    sql = "select code from " + ftd.getTableName() + " where flowTypeCode=" + StrUtil.sqlstr(flowTypeCode) + " and isFlow=1 order by code asc";
                } else {
                    sql = "select code from " + ftd.getTableName() + " where isFlow=1 and unit_code=" + StrUtil.sqlstr(curUnitCode) + " order by code asc";
                }
            }

            JdbcTemplate jt = new JdbcTemplate();
            jt.setAutoClose(false);
            try {
                for (Object o : ftd.list(sql)) {
                    ftd = (FormDb) o;
                    if (ftd == null || !ftd.isLoaded()) {
                        continue;
                    }
                    try {
                        sql = "select count(f.id) from flow f," + ftd.getTableNameByForm() + " m where f.id = m.flowId and " + SQLFilter.year("f.mydate") + " = " + StrUtil.sqlstr(y + "") + "  and f.status=" + WorkflowDb.STATUS_NOT_STARTED;

                        ResultIterator ri = jt.executeQuery(sql);
                        int countNot = 0;
                        if (ri.hasNext()) {
                            ResultRecord rr = (ResultRecord) ri.next();
                            countNot = rr.getInt(1);
                        }

                        // System.out.println(getClass() + " " + sql);

                        sql = "select count(f.id) from flow f," + ftd.getTableNameByForm() + " m where f.id = m.flowId and " + SQLFilter.year("f.mydate") + " = " + StrUtil.sqlstr(y + "") + "  and f.status=" + WorkflowDb.STATUS_STARTED;
                        ri = jt.executeQuery(sql);
                        int countStarted = 0;
                        if (ri.hasNext()) {
                            ResultRecord rr = (ResultRecord) ri.next();
                            countStarted = rr.getInt(1);
                        }
                        sql = "select count(f.id) from flow f," + ftd.getTableNameByForm() + " m where f.id = m.flowId and " + SQLFilter.year("f.mydate") + " = " + StrUtil.sqlstr(y + "") + "  and f.status=" + WorkflowDb.STATUS_FINISHED;
                        ri = jt.executeQuery(sql);
                        int countFinished = 0;
                        if (ri.hasNext()) {
                            ResultRecord rr = (ResultRecord) ri.next();
                            countFinished = rr.getInt(1);
                        }
                        sql = "select count(f.id) from flow f," + ftd.getTableNameByForm() + " m where f.id = m.flowId and " + SQLFilter.year("f.mydate") + " = " + StrUtil.sqlstr(y + "") + "  and f.status=" + WorkflowDb.STATUS_DISCARDED;
                        ri = jt.executeQuery(sql);
                        int countDiscarded = 0;
                        if (ri.hasNext()) {
                            ResultRecord rr = (ResultRecord) ri.next();
                            countDiscarded = rr.getInt(1);
                        }
                        int flow_sum = countNot + countStarted + countFinished + countDiscarded;
                        int average_month = (int) flow_sum / 12;
                        int average_day = (int) flow_sum / 365;

                        String code = ftd.getCode();
                        Leaf leaf = new Leaf();
                        leaf = leaf.getLeafByFormCode(code);
        %>
        <tr class="highlight">
            <td width="8%" height="24"><%=ftd.getName()%>
            </td>
            <td width="9%"><%=countNot%>
            </td>
            <td width="9%"><%=countStarted%>
            </td>
            <td width="9%"><%=countFinished%>
            </td>
            <td width="9%"><%=countDiscarded%>
            </td>
            <td width="9%"><%=flow_sum%>
            </td>
            <td width="9%"><%=average_month%>
            </td>
            <td width="9%"><%=average_day%>
            </td>
            <td width="24%" align="center">
                <%
                    if (leaf != null) {
                %>
                <a href="javascript:;"
                   onclick="addTab('<%=ftd.getName()%>月统计', '<%=request.getContextPath()%>/admin/flow_stat_month.jsp?typeCode=<%=leaf.getCode()%>')">月统计</a>&nbsp;&nbsp;&nbsp;<a
                    href="javascript:;"
                    onclick="addTab('<%=ftd.getName()%>年统计', '<%=request.getContextPath()%>/admin/flow_stat_year.jsp?typeCode=<%=leaf.getCode()%>')">年统计</a>
                <%
                    }
                %>
            </td>
        </tr>
        <%
                    } catch (Exception e) {
                        LogUtil.getLog(getClass()).equals(StrUtil.trace(e));
                    }
                }
            } finally {
                jt.close();
            }
        %>
    </table>
</form>
<br/>
</body>
</html>
