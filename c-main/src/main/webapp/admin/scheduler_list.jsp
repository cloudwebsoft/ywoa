<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.*" %>
<%@ page import="com.redmoon.oa.kernel.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>调度列表</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery.resizableColumns.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
    <script language=javascript>
        <!--
        function openWin(url, width, height) {
            var newwin = window.open(url, "_blank", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width=" + width + ",height=" + height);
        }
        //-->
    </script>
</head>
<body>
<%
    if (!privilege.isUserPrivValid(request, "admin")) {
        out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String op = ParamUtil.get(request, "op");
    SchedulerManager sm = SchedulerManager.getInstance();

    if (op.equals("start")) {
        if (Global.getInstance().isSchedule()) {
            sm.startWhenIsShutdown();
            out.print(StrUtil.jAlert_Redirect("调度启动！", "scheduler_list.jsp", "提示"));
        }
        else {
            out.print(StrUtil.jAlert_Back("调度未启用，请先在系统变量中启用调度！", "提示"));
        }
        return;
    } else if (op.equals("stop")) {
        sm.shutdown();
        out.print(StrUtil.jAlert_Redirect("调度停止！", "scheduler_list.jsp", "提示"));
        return;
    }

    JobUnitDb jud = new JobUnitDb();
    if (op.equals("del")) {
        int delid = ParamUtil.getInt(request, "id");
        JobUnitDb ldb = (JobUnitDb) jud.getQObjectDb(new Integer(delid));
        if (ldb.del()) {
            out.print(StrUtil.Alert_Redirect("操作成功", "scheduler_list.jsp"));
        } else {
            out.print(StrUtil.Alert_Back("操作失败"));
        }
        return;
    }

    String sql = "select ID from " + jud.getTable().getName() + " order by id desc";
    int pagesize = 10;
    Paginator paginator = new Paginator(request);
    int curpage = paginator.getCurPage();

    ListResult lr = jud.listResult(new JdbcTemplate(), sql, curpage, pagesize);
    long total = lr.getTotal();
    Vector v = lr.getResult();
    Iterator ir = null;
    if (v != null) {
        ir = v.iterator();
    }
    paginator.init(total, pagesize);
    // 设置当前页数和总页数
    int totalpages = paginator.getTotalPages();
    if (totalpages == 0) {
        curpage = 1;
        totalpages = 1;
    }
%>
<%@ include file="scheduler_inc_menu_top.jsp" %>
<script>
    o("menu1").className = "current";
</script>
<div class="spacerH"></div>
<div style="text-align:center">状态：<%=sm.isStarted() ? "已启动" : "未启动"%>&nbsp;&nbsp;
    <%if (!sm.isStarted()) {%>
    <a href="scheduler_list.jsp?op=start">开始调度</a>
    <%} else {%>
    <a href="scheduler_list.jsp?op=stop">停止调度</a>
    <%}%>
</div>
<table class="percent98" width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
        <td align="right"> 找到符合条件的记录 <b><%=paginator.getTotal() %>
        </b> 条　每页显示 <b><%=paginator.getPageSize() %>
        </b> 条　页次 <b><%=curpage %>/<%=totalpages %>
        </b></td>
    </tr>
</table>
<table id="mainTable" width="98%" border="0" align="center" cellpadding="2" cellspacing="0" class="tabStyle_1 percent98">
    <thead>
    <tr>
        <th data-resizable-column-id="first_name" width="5%" align="center" class="tabStyle_1_title">ID</th>
        <th width="32%" align="left" class="tabStyle_1_title">名称</th>
        <th width="19%" align="left" class="tabStyle_1_title">类名称</th>
        <th width="18%" align="left" class="tabStyle_1_title">调 度</th>
        <th width="12%" align="left" class="tabStyle_1_title">用户</th>
        <th width="14%" align="left" class="tabStyle_1_title">操作</th>
    </tr>
    </thead>
    <%
        while (ir.hasNext()) {
            jud = (JobUnitDb) ir.next();
            UserDb ud = new UserDb(jud.getString("user_name"));
    %>
    <tr>
        <td width="5%" align="center"><%=jud.getInt("id")%>
        </td>
        <td width="32%" height="22" align="left"><%=jud.get("job_name")%>
        </td>
        <td align="left"><%=jud.get("job_class")%>
        </td>
        <td align="left">&nbsp;&nbsp;<%=jud.getString("cron")%>
        </td>
        <td align="left">&nbsp;&nbsp;<%=ud.getRealName()%>
        </td>
        <td width="14%" align="center">
            <%
                if (jud.getString("job_class").equals("com.redmoon.oa.job.SynThirdPartyDataJob")) {%>
            <a href="javascript:;" onclick="addTab('<%=jud.get("job_name")%>', '<%=request.getContextPath()%>/admin/scheduler_edit_syn_data.jsp?id=<%=jud.getInt("id")%>')">编辑</a>
            <%} else if (jud.getString("job_class").equals("com.redmoon.oa.job.BeanShellScriptJob")) {%>
            <a href="javascript:;" onclick="addTab('<%=jud.get("job_name")%>', '<%=request.getContextPath()%>/admin/scheduler_edit_script.jsp?id=<%=jud.getInt("id")%>')">编辑</a>
            <%}%>
            &nbsp;&nbsp;<a href="scheduler_list.jsp?op=del&amp;id=<%=jud.get("id")%>" onClick="if (!confirm('您确定要删除吗？')) return false">删除</a></td>
    </tr>
    <%}%>
</table>
<table class="percent98" width="100%" border="0" cellspacing="1" cellpadding="3" align="center">
    <tr>
        <td height="23" align="right">&nbsp;
            <%
                String querystr = "";
                out.print(paginator.getCurPageBlock("?" + querystr));
            %>
        </td>
    </tr>
</table>
<br/>
</body>
<script>
    $(document).ready(function () {
        $("#mainTable td").mouseout(function () {
            if ($(this).parent().parent().get(0).tagName != "THEAD")
                $(this).parent().find("td").each(function (i) {
                    $(this).removeClass("tdOver");
                });
        });

        $("#mainTable td").mouseover(function () {
            if ($(this).parent().parent().get(0).tagName != "THEAD")
                $(this).parent().find("td").each(function (i) {
                    $(this).addClass("tdOver");
                });
        });
    });

    $(function () {
        $("table").ReSizeTablecolumn({});
    });
</script>

</html>
