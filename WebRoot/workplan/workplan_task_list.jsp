<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.workplan.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String userName = ParamUtil.get(request, "userName");
    if (userName.equals("")) {
        userName = privilege.getUser(request);
    }
    if (!userName.equals(privilege.getUser(request))) {
        if (!(privilege.canAdminUser(request, userName))) {
            out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }
    }

    String op = ParamUtil.get(request, "op");
    String action = ParamUtil.get(request, "action");
    String kind = ParamUtil.get(request, "kind");
    String what = ParamUtil.get(request, "what");
    String beginDate = ParamUtil.get(request, "beginDate");
    String endDate = ParamUtil.get(request, "endDate");
    int progressFlag = ParamUtil.getInt(request, "progressFlag", -1);

    try {
        com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "what", what, getClass().getName());
        com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "userName", userName, getClass().getName());

        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "action", action, getClass().getName());
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "userName", userName, getClass().getName());
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "what", what, getClass().getName());
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "kind", kind, getClass().getName());
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "beginDate", beginDate, getClass().getName());
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "endDate", endDate, getClass().getName());
    } catch (ErrMsgException e) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
        return;
    }

    String orderBy = ParamUtil.get(request, "orderBy");
    if (orderBy.equals(""))
        orderBy = "start_date";
    String sort = ParamUtil.get(request, "sort");
    if (sort.equals(""))
        sort = "desc";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>工作计划列表</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
    <script type="text/javascript" src="../inc/common.js"></script>
    <script type="text/javascript" src="../js/jquery.js"></script>
    <script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <script type="text/javascript" src="../js/flexigrid.js"></script>
</head>
<body>
<%@ include file="workplan_inc_menu_top.jsp" %>
<script>
    o("menu5").className = "current";
</script>
<table id="searchTable" width="100%" border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td>
            <form id="formSearch" name="formSearch" action="workplan_task_list.jsp" method="get">
                <input name="action" value="search" type="hidden"/>&nbsp;
                进度
                <select id="progressFlag" name="progressFlag">
                    <option value="-1" <%=progressFlag == -1 ? "selected" : ""%>>不限</option>
                    <option value="0" <%=progressFlag == 0 ? "selected" : ""%>>未完成</option>
                    <option value="1" <%=progressFlag == 1 ? "selected" : ""%>>已完成</option>
                </select>
                开始日期
                <input id="beginDate" name="beginDate" value="<%=beginDate%>" size=10>
                结束日期
                <input id="endDate" name="endDate" value="<%=endDate%>" size=10>
                标题
                <input name=what size=20 value="<%=what%>">
                &nbsp;
                <input class="tSearch" name="submit" type=submit value="搜索">
                <input name="op" type="hidden" value="<%=op%>">
                <input name="userName" type="hidden" value="<%=userName%>">
            </form>
        </td>
    </tr>
</table>
<%
    String sql;
    String myname = privilege.getUser(request);
    String querystr = "";
    sql = "select t.id from work_plan_task t, work_plan_task_user u where t.id=u.task_id and u.user_name=" + StrUtil.sqlstr(userName);
    if (action.equals("search")) {
        if (!what.equals(""))
            sql += " and name like " + StrUtil.sqlstr("%" + what + "%");
        if (!beginDate.equals(""))
            sql += " and start_date>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
        if (!endDate.equals(""))
            sql += " and end_date<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd");
        if (progressFlag != -1) {
            if (progressFlag == 0)
                sql += " and progress<100";
            else
                sql += " and progress=100";
        }
    }
    sql += " order by " + orderBy + " " + sort;

    // System.out.println(getClass() + " sql=" + sql);

    String urlStr = "op=" + op + "&userName=" + StrUtil.UrlEncode(userName) + "&action=" + action + "&what=" + StrUtil.UrlEncode(what) + "&beginDate=" + beginDate + "&endDate=" + endDate + "&progressFlag=" + progressFlag;

    querystr = urlStr + "&orderBy=" + orderBy + "&sort=" + sort;

    int pagesize = ParamUtil.getInt(request, "pageSize", 20);
    Paginator paginator = new Paginator(request);
    int curpage = paginator.getCurPage();

    WorkPlanTaskDb wptd = new WorkPlanTaskDb();

    ListResult lr = wptd.listResult(sql, curpage, pagesize);
    int total = lr.getTotal();
    Vector v = lr.getResult();
    Iterator ir = v.iterator();
    paginator.init(total, pagesize);
    // 设置当前页数和总页数
    int totalpages = paginator.getTotalPages();
    if (totalpages == 0) {
        curpage = 1;
        totalpages = 1;
    }
%>
<table id="grid">
    <thead>
    <tr>
        <th width="23" style="cursor:pointer" abbr="title">&nbsp;</th>
        <th width="36" style="cursor:pointer" abbr="ID">ID</th>
        <th width="237" style="cursor:pointer" abbr="name">标题</th>
        <th width="171" style="cursor:pointer">属于计划</th>
        <th width="82" style="cursor:pointer" abbr="progress">进度</th>
        <th width="80" style="cursor:pointer" abbr="start_date">开始日期</th>
        <th width="80" style="cursor:pointer" abbr="end_date">结束日期</th>
        <th width="55" style="cursor:pointer" abbr="t.duration">计划工作日</th>
        <th width="80" style="cursor:pointer">天数</th>
        <th width="80" style="cursor:pointer">关联计划</th>
        <th width="80" style="cursor:pointer">操作</th>
    </tr>
    </thead>
    <tbody>
    <%
        int i = 0;
        WorkPlanDb wpd = new WorkPlanDb();
        SelectOptionDb sod = new SelectOptionDb();
        UserMgr um = new UserMgr();
        while (ir != null && ir.hasNext()) {
            wptd = (WorkPlanTaskDb) ir.next();
            i++;
            int id = wpd.getId();
            String sbeginDate = DateUtil.format(wptd.getDate("start_date"), "yyyy-MM-dd");
            String sendDate = DateUtil.format(wptd.getDate("end_date"), "yyyy-MM-dd");
            wpd = wpd.getWorkPlanDb(wptd.getInt("work_plan_id"));
    %>
    <tr>
        <td align="center">
            <%
                int nowDays = DateUtil.datediff(wptd.getDate("end_date"), new Date());
                if (nowDays < 0) {
                    // nowDays = 0;
                }
                int sumDays = DateUtil.datediff(wptd.getDate("end_date"), wptd.getDate("start_date"));
                float progress = (float) nowDays / sumDays;

                float r23 = (float) 2 / 3;
                if (progress > r23) {
            %>
            <img src="../images/green.jpg" width="16" height="18" border="0" title="时间大于2/3"/>
            <%} else if (progress < r23 && progress > ((float) 1 / 3)) {%>
            <img src="../images/yel.jpg" width="16" height="18" border="0" title="时间介于1/3与2/3之间"/>
            <%} else if (progress < ((float) 1 / 3) && progress >= 0) {%>
            <img src="../images/red.jpg" width="16" height="18" border="0" title="时间小于1/3"/>
            <%} else {%>
            <img src="../images/red_hot.jpg" width="16" height="18" border="0" title="时间超期"/>
            <%}%>
        </td>
        <td align="center"><%=wptd.getLong("id")%>
        </td>
        <td><%=wptd.getString("name")%>
        </td>
        <td align="center"><a href="javascript:;" onclick="addTab('<%=wptd.getString("name")%>', '<%=request.getContextPath()%>/workplan/workplan_show.jsp?id=<%=wptd.getLong("work_plan_id")%>')"><%=wpd.getTitle()%>
        </a></td>
        <td align="center">
            <div class="progressBar" style="padding:0px; margin:0px; height:20px">
                <div class="progressBarFore" style="width:<%=wpd.getProgress()%>%;">
                </div>
                <div class="progressText">
                    <%=wptd.getInt("progress")%>%
                </div>
            </div>
        </td>
        <td align="center"><%=sbeginDate%>
        </td>
        <td align="center"><%=sendDate%>
        </td>
        <td align="center">
            <%=wptd.getInt("duration")%>
        </td>
        <td align="center">
            <%if (progress < 100 && nowDays < 0) {%>
            <font color="red">过期<%=-nowDays%>天</font>
            <%} else {%>
            剩余<%=nowDays%>天
            <%}%>
        </td>
        <td align="center">
            <%
                if (wptd.getLong("workplan_related") != -1) {
                    wpd = wpd.getWorkPlanDb(wptd.getInt("workplan_related"));
            %>
            <a href="javascript:;" onclick="addTab('<%=wpd.getTitle()%>', 'workplan/workplan_show.jsp?id=<%=wpd.getId()%>')"><%=wpd.getTitle()%>
            </a>
            <%} else {%>
            无
            <%}%>
        </td>
        <td>
            <a href="javascript:;" onclick="addTab('任务日报', 'workplan/workplan_task_annex_day.jsp?taskId=<%=wptd.getLong("id")%>&id=<%=wptd.getInt("work_plan_id")%>')">日报</a>
        </td>
    </tr>
    <%
        }
    %>
    </tbody>
</table>
</body>
<script type="text/javascript">
    function initCalendar() {
        $('#beginDate').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d',
            formatDate: 'Y/m/d'
        });
        $('#endDate').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d',
            formatDate: 'Y/m/d'
        });
    }

    function doOnToolbarInited() {
        initCalendar();
    }

    var flex;

    function changeSort(sortname, sortorder) {
        window.location.href = "workplan_task_list.jsp?<%=urlStr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
    }

    function changePage(newp) {
        if (newp)
            window.location.href = "workplan_task_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
    }

    function rpChange(pageSize) {
        window.location.href = "workplan_task_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
    }

    function onReload() {
        window.location.reload();
    }

    $(function () {
        flex = $("#grid").flexigrid
        (
            {
                buttons: [
                    {name: '条件', bclass: '', type: 'include', id: 'searchTable'}
                ],
                /*
                searchitems : [
                    {display: 'ISO', name : 'iso'},
                    {display: 'Name', name : 'name', isdefault: true}
                    ],
                */
                sortname: "<%=orderBy%>",
                sortorder: "<%=sort%>",
                url: false,
                usepager: true,
                checkbox: false,
                page: <%=curpage%>,
                total: <%=total%>,
                useRp: true,
                rp: <%=pagesize%>,

                //title: "通知",
                singleSelect: true,
                resizable: false,
                showTableToggleBtn: true,
                showToggleBtn: true,

                onChangeSort: changeSort,

                onChangePage: changePage,
                onRpChange: rpChange,
                onReload: onReload,
                /*
                onRowDblclick: rowDbClick,
                onColSwitch: colSwitch,
                onColResize: colResize,
                onToggleCol: toggleCol,
                */
                onToolbarInited: doOnToolbarInited,
                autoHeight: true,
                width: document.documentElement.clientWidth,
                height: document.documentElement.clientHeight - 84
            }
        );
    })

</script>
</html>