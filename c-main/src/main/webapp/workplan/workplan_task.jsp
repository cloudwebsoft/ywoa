<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.workplan.*" %>
<%@ page import="com.redmoon.oa.oacalendar.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "read";
    int workplanId = ParamUtil.getInt(request, "id");
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
            out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "pvg_invalid"), "提示"));
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

    String orderBy = ParamUtil.get(request, "orderBy");
    if (orderBy.equals(""))
        orderBy = "orders";
    String sort = ParamUtil.get(request, "sort");
    if (sort.equals(""))
        sort = "asc";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>工作计划列表</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>

    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
    <script type="text/javascript" src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <script type="text/javascript" src="../js/flexigrid.js"></script>

    <link href="<%=SkinMgr.getSkinPath(request)%>/main.css" rel="stylesheet" type="text/css"/>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <%@ include file="../inc/nocache.jsp" %>
    <script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>
</head>
<body>
<%
    WorkPlanDb wpd = new WorkPlanDb();
    wpd = wpd.getWorkPlanDb(workplanId);
    if (!wpd.isLoaded()) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id")));
        return;
    }
    int dur = 0;
    OACalendarDb oacal = new OACalendarDb();
    try {
        dur = oacal.getWorkDayCount(wpd.getBeginDate(), wpd.getEndDate());
    } catch (ErrMsgException e) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
        return;
    }
%>
<%@ include file="workplan_show_inc_menu_top.jsp" %>
<script>
    o("menu4").className = "current";
</script>
<table id="searchTable" width="100%" border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td align="center">
            <form id="formSearch" name="formSearch" action="?id=<%=workplanId%>" method="post" style="width:800px;">
                <input name="action" value="search" type="hidden"/>
                &nbsp;状态
                <select id="progressFlag" name="progressFlag">
                    <option value="-1" <%=progressFlag == -1 ? "selected" : ""%>>不限</option>
                    <option value="0" <%=progressFlag == 0 ? "selected" : ""%>>未完成</option>
                    <option value="1" <%=progressFlag == 1 ? "selected" : ""%>>已完成</option>
                </select>
                开始日期
                <input id="beginDate" name="beginDate" value="<%=beginDate%>" size=10/>
                结束日期
                <input id="endDate" name="endDate" value="<%=endDate%>" size=10/>
                标题
                <input id='what' type='text' name='what' size=20 value="<%=what%>"/>
                &nbsp;
                <input class="tSearch" name="submit" type=submit value="搜索"/>
                <input name="op" type="hidden" value="<%=op%>"/>
                <input name="userName" type="hidden" value="<%=userName%>"/>
            </form>
        </td>
    </tr>
</table>
<%
    WorkPlanTaskDb wptd = new WorkPlanTaskDb();

    String jsonStr = wpd.getGantt();
    if (jsonStr.equals("")) {
        // 创建根任务
        String code = RandomSecquenceCreator.getId(20);
        String name = wpd.getTitle();
        int level = 0;
        String resource = wpd.getPrincipals()[0];
        int status = WorkPlanTaskDb.STATUS_UNDEFINED;
        String reportFlowType = "";
        wptd.create(new JdbcTemplate(), new Object[]{
                name, code,
                new Integer(level), new Integer(status), wpd.getBeginDate(), wpd.getEndDate(),
                new Integer(dur),
                new Integer(0),
                new Integer(0), resource,
                new Integer(WorkPlanTaskDb.RESOURCE_TYPE_USER),
                new Long(workplanId), new Integer(wpd.getProgress()),
                new Long(-1), new Integer(0), new Integer(0), StrUtil.sqlstr(reportFlowType),
                ""
        });

        // 取得刚创建的task
        wptd = wptd.getTaskByOrders(workplanId, 0);

        // 创建任务用户
        WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();
        wptud.create(new JdbcTemplate(), new Object[]{new Long(wptd.getLong("id")), resource, new java.util.Date(), new Integer(100), new Integer(dur), new Integer(0)});
        // 刷新gantt图
        WorkPlanTaskMgr.refreshGantt(workplanId);
    }

    String sql;
    String querystr = "";
    sql = "select id from work_plan_task where work_plan_id=" + workplanId;
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

    String urlStr = "op=" + op + "&userName=" + StrUtil.UrlEncode(userName) + "&action=" + action + "&what=" + StrUtil.UrlEncode(what) + "&beginDate=" + beginDate + "&endDate=" + endDate + "&progressFlag=" + progressFlag + "&id=" + ParamUtil.getInt(request, "id");

    querystr = urlStr + "&orderBy=" + orderBy + "&sort=" + sort;

    int pagesize = ParamUtil.getInt(request, "pageSize", 100);
    Paginator paginator = new Paginator(request);
    int curpage = paginator.getCurPage();

    //out.println(sql);
    ListResult lr = wptd.listResult(sql, curpage, pagesize);
    long total = lr.getTotal();
    Vector v = lr.getResult();

    //若是按照orders排序，则允许折叠操作
    //循环list，把每个任务对应的class放入到map中
    Map<Long, String> map = new HashMap<Long, String>();
    //储存包含子任务的任务ID
    Map<Long, String> hasChildMap = new HashMap<Long, String>();
    //是否根据orders排序，若是则允许折叠，否则不允许
    if (orderBy.equals("") || "orders".equals(orderBy)) {
        List<WorkPlanTaskDb> list = new ArrayList<WorkPlanTaskDb>();
        //查询结果复制至list中
        list.addAll(v);
        //list按照order排序
        Comparator comp = new Comparator<WorkPlanTaskDb>() {
            public int compare(WorkPlanTaskDb o1, WorkPlanTaskDb o2) {
                return o1.getInt("orders") - o2.getInt("orders");
            }
        };
        Collections.sort(list, comp);

        for (int i = 0; i < list.size(); i++) {
            WorkPlanTaskDb nowWd = list.get(i);
            if (i > 0) {
                WorkPlanTaskDb beforeWd = list.get(i - 1);
                if (nowWd.getInt("task_level") > beforeWd.getInt("task_level"))//为上一任务的子任务
                {
                    hasChildMap.put(beforeWd.getLong("id"), "1");//包含子任务
                    map.put(nowWd.getLong("id"), (map.get(beforeWd.getLong("id")) + " " + beforeWd.getLong("id")).trim());
                } else if (nowWd.getInt("task_level") == beforeWd.getInt("task_level"))//和上一任务同级
                {
                    map.put(nowWd.getLong("id"), map.get(beforeWd.getLong("id")).trim());
                } else//比上一任务高级
                {
                    int gap = beforeWd.getInt("task_level") - nowWd.getInt("task_level");
                    String[] classes = map.get(beforeWd.getLong("id")).split(" ");
                    String thisClass = "";
                    for (int j = 0; j < classes.length - gap; j++) {
                        thisClass = thisClass + " " + classes[j];
                    }
                    map.put(nowWd.getLong("id"), thisClass.trim());

                }

            } else//根任务
            {
                map.put(nowWd.getLong("id"), "");
            }
        }

        v = new Vector();
        v.addAll(list);
    }

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
        <th width="20" style="cursor:pointer"></th>
        <th width="36" style="cursor:pointer" abbr="ID">ID</th>
        <th width="237" style="cursor:pointer" abbr="name">标题</th>
        <th width="82" style="cursor:pointer" abbr="progress">进度</th>
        <th width="0" style="display:none"></th>
        <th width="50" style="cursor:pointer" abbr="task_resource">责任人</th>
        <th width="78" style="cursor:pointer" abbr="start_date">开始日期</th>
        <th width="78" style="cursor:pointer" abbr="end_date">结束日期</th>
        <th width="78" style="cursor:pointer" abbr="end_date">实际结束</th>
        <th width="65" style="cursor:pointer" abbr="duration">工作日</th>
        <th width="60" style="cursor:pointer">天数</th>
        <th width="35" style="cursor:pointer" abbr="assess">评价</th>
        <th width="70" style="cursor:pointer">关联计划</th>
        <th width="255" style="cursor:pointer">操作</th>
    </tr>
    </thead>
    <tbody>
    <%
        int i = 0;
        SelectOptionDb sod = new SelectOptionDb();
        WorkPlanAnnexDb wpad = new WorkPlanAnnexDb();
        UserMgr um = new UserMgr();
        com.redmoon.oa.workplan.Privilege workplanPvg = new com.redmoon.oa.workplan.Privilege();
        while (ir != null && ir.hasNext()) {
            wptd = (WorkPlanTaskDb) ir.next();
            i++;
            String sbeginDate = DateUtil.format(wptd.getDate("start_date"), "yyyy-MM-dd");
            String sendDate = DateUtil.format(wptd.getDate("end_date"), "yyyy-MM-dd");
    %>
    <tr class="<%=map.get(wptd.getLong("id"))%>" id="<%=wptd.getLong("id")%>Tr">
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
        <td style="align:center">
            <%
                String clr = "#ffffff";
                int status = wptd.getInt("status");
                if (status == WorkPlanTaskDb.STATUS_ACTIVE)
                    clr = "#6acce6";
                else if (status == WorkPlanTaskDb.STATUS_DONE)
                    clr = "#a9db23";
                else if (status == WorkPlanTaskDb.STATUS_FAILED)
                    clr = "#7f7f7f";
                else if (status == WorkPlanTaskDb.STATUS_SUSPENDED)
                    clr = "#ff6753";
                else if (status == WorkPlanTaskDb.STATUS_UNDEFINED)
                    clr = "#d1d1d1";
            %>
            <div title="<%=WorkPlanTaskDb.getStatusDesc(request, status)%>" style="width:6px; height:6px; background-color:<%=clr%>; border:1px solid #a0a0a0;"></div>
        </td>
        <td align="center"><%=wptd.getLong("id")%>
        </td>
        <td>
            <%
                for (int k = 0; k < wptd.getInt("task_level"); k++) {
                    out.print("&nbsp;&nbsp;&nbsp;&nbsp;");
                }

                if (orderBy.equals("") || orderBy.equals("orders")) {
                    if (hasChildMap.get(wptd.getLong("id")) != null) {
            %>
            <script>
                //获取该任务的cookies中折叠状态
                var style = unescape(get_cookie(<%=wptd.getLong("id")%>));
                //折叠
                if (style == "1") {
                    $("#<%=wptd.getLong("id")%>").attr("src", "../images/openico.jpg");
                    $("#<%=wptd.getLong("id")%>").attr("onclick", "showChild('<%=wptd.getLong("id")%>')");
                    $(".<%=wptd.getLong("id")%>").hide();
                }
                //展开
                else {
                    $("#<%=wptd.getLong("id")%>").attr("src", "../images/putawayico.jpg");
                    $("#<%=wptd.getLong("id")%>").attr("onclick", "hideChild('<%=wptd.getLong("id")%>')");
                    //若是该任务对应记录为显示，则所有子任务显示
                    if ($("#<%=wptd.getLong("id")%>" + "Tr").css("display") != "none") {
                        $(".<%=wptd.getLong("id")%>").show();
                    }

                }
            </script>
            <img src="" align="absmiddle" id='<%=wptd.getLong("id")%>' onclick="" style="cursor:pointer"/>
            <%
            } else {
            %>
            <span class="new_f-1">■</span>
            <%
                }
            } else {
                if (wptd.getInt("task_level") != 0) {
            %>

            <img src='../images/i_plus-2-3.gif' align='absmiddle'/>
            <%
                    }
                }
            %>
            <span id="realTaskName<%=wptd.getLong("id") %>"><%=wptd.getString("name")%></span></td>
        <td align="center">
            <div class="progressBar" style="padding:0px; margin:0px; height:20px">
                <div class="progressBarFore" style="width:<%=wptd.getInt("progress")%>%;padding:0px">
                </div>
                <div class="progressText">
                    <%=wptd.getInt("progress")%>%
                </div>
            </div>
        </td>
        <td style="display:none">
            <%
                if (!StrUtil.getNullStr(wptd.getString("task_resource")).equals("")) {
            %>
            <%=wptd.getString("task_resource") %>
            <%
                }
            %>
        </td>
        <td>
            <%
                if (!StrUtil.getNullStr(wptd.getString("task_resource")).equals("")) {
                    UserDb user = um.getUserDb(wptd.getString("task_resource"));
            %>
            <a href="javascript:;"
               onclick="addTab('消息', 'message_oa/message_frame.jsp?op=send&receiver=<%=StrUtil.UrlEncode(user.getName())%>')"><%=user.getRealName()%>
            </a>
            <%
                }
            %>
        </td>
        <td align="center"><%=sbeginDate%>&nbsp;<%=wptd.getInt("startIsMilestone") == 1 ? "<img title='里程碑' align='absmiddle' src='../images/workplan/milestone.png'>" : ""%>
        </td>
        <td align="center"><%=sendDate%>&nbsp;<%=wptd.getInt("endIsMilestone") == 1 ? "<img title='里程碑' align='absmiddle' src='../images/workplan/milestone.png'>" : ""%>
        </td>
        <td align="center"><%=DateUtil.format(wpad.getRealCompleteDate(wptd.getLong("id")), "yyyy-MM-dd")%>
        </td>
        <td align="center">
            <%=wptd.getInt("duration")%>
        </td>
        <td align="center">
            <%
                if (wptd.getInt("progress") < 100) {
                    if (nowDays < 0) {
            %>
            <font color="red">过期<%=-nowDays%>天</font>
            <%} else {%>
            剩余<%=nowDays%>天
            <%
                    }
                }
            %>
        </td>
        <td>
            <span id="assess<%=wptd.getLong("id")%>"><%=sod.getOptionName("workplan_assess", wptd.getString("assess"))%></span>
        </td>
        <td align="center">
            <%
                if (wptd.getLong("workplan_related") != -1) {
                    WorkPlanDb wprel = wpd.getWorkPlanDb(wptd.getInt("workplan_related"));
                    if (wprel.isLoaded()) {
            %>
            <a href="javascript:;" onclick="addTab('<%=wprel.getTitle()%>', 'workplan/workplan_show.jsp?id=<%=wprel.getId()%>')"><%=wprel.getTitle()%>
            </a>
            <% }
            } else {%>
            无
            <%}%>
        </td>
        <td>
            <a href="javascript:;" onclick="move(<%=wptd.getLong("id")%>, true)" title="上移">↑</a>
            &nbsp;&nbsp;<a href="javascript:;" onclick="move(<%=wptd.getLong("id")%>, false)" title="下移">↓</a>
            &nbsp;&nbsp;<a href="workplan_task_user.jsp?taskId=<%=wptd.getLong("id")%>&workplanId=<%=workplanId%>">参与者</a>
            &nbsp;&nbsp;<a href="javascript:;" onclick="addTab('任务日报', 'workplan/workplan_task_annex_day.jsp?taskId=<%=wptd.getLong("id")%>&id=<%=workplanId%>')">日报</a>
        </td>
    </tr>
    <%
        }
    %>
    </tbody>
</table>
<div id="taskDlg" style="display:none">
    <form id="formAdd">
        <table>
            <tr height='30px'>
                <td width="96">标题</td>
                <td width="300"><input type="text" name="name" id="name" class="formElements" style="width:200px"/></td>
            </tr>
            <tr height='30px'>
                <td>上级任务</td>
                <td><input id="parentTaskId" name="parentTaskId" type="hidden"/><span id="parentTaskName"></span>
                </td>
            </tr>
            <tr height='30px'>
                <td>进度</td>
                <td><input type="text" name="progress" id="progress" value="0" size="3" class="formElements"/>&nbsp;%
                </td>
            </tr>
            <tr height='30px' style="display: none">
                <td>前置任务</td>
                <td>
                    <input id="depends" name="depends"/>
                </td>
            </tr>
            <tr height='30px'>
                <td>开始日期</td>
                <td>
                    <input readonly type="text" id="start_date" name="start_date" size="15">
                    <input id="startIsMilestone" name="startIsMilestone" type="checkbox" value="1"/>
                    里程碑
                </td>
            </tr>
            <tr height='30px'>
                <td>结束日期</td>
                <td>
                    <input readonly type="text" id="end_date" name="end_date" size="15">
                    <input id="endIsMilestone" name="endIsMilestone" type="checkbox" value="1"/>
                    里程碑
                </td>
            </tr>
            <tr height='30px'>
                <td>关联计划</td>
                <td><input type="text" name="workplan_related" id="workplan_related" value="" size="3"
                           class="formElements"/></td>
            </tr>
            <tr height='34px'>
                <td>任务状态</td>
                <td>
                    <select id="status" name="status">
                        <option value="<%=WorkPlanTaskDb.STATUS_ACTIVE%>">活动</option>
                        <option value="<%=WorkPlanTaskDb.STATUS_DONE%>">完成</option>
                        <option value="<%=WorkPlanTaskDb.STATUS_FAILED%>">失败</option>
                        <option value="<%=WorkPlanTaskDb.STATUS_SUSPENDED%>">挂起</option>
                        <option value="<%=WorkPlanTaskDb.STATUS_UNDEFINED%>">未定义</option>
                    </select>
                </td>
            </tr>
            <tr height='34px'>
                <td>责任人</td>
                <td>
                    <select id="task_resource" name="task_resource">
                        <%
                            String[] principalAry = wpd.getPrincipals();
                            int len = principalAry == null ? 0 : principalAry.length;
                            for (i = 0; i < len; i++) {
                                if (principalAry[i].equals(""))
                                    continue;
                                UserDb user = um.getUserDb(principalAry[i]);
                        %>
                        <option value="<%=user.getName()%>"><%=user.getRealName()%>
                        </option>
                        <%
                            }

                            String[] userAry = wpd.getUsers();
                            len = userAry == null ? 0 : userAry.length;
                            for (i = 0; i < len; i++) {
                                if (userAry[i].equals(""))
                                    continue;
                                // 过滤掉负责人
                                boolean isFound = false;
                                for (int j = 0; j < principalAry.length; j++) {
                                    if (principalAry[j].equals(userAry[i])) {
                                        isFound = true;
                                        break;
                                    }
                                }
                                if (isFound)
                                    continue;
                                UserDb user = um.getUserDb(userAry[i]);
                        %>
                        <option value="<%=user.getName()%>"><%=user.getRealName()%>
                        </option>
                        <%
                            }
                        %>
                    </select>
                    <input id="work_plan_id" name="work_plan_id" value="<%=workplanId%>" type="hidden"/>
                </td>
            </tr>
            <tr height='34px'>
                <td>汇报流程</td>
                <td>
                    <select id="report_flow_type" name="report_flow_type">
                        <option value="">直接汇报</option>
                        <%
                            com.redmoon.oa.flow.Leaf rootlf = new com.redmoon.oa.flow.Leaf();
                            rootlf = rootlf.getLeaf(com.redmoon.oa.flow.Leaf.CODE_ROOT);
                            com.redmoon.oa.flow.DirectoryView flowdv = new com.redmoon.oa.flow.DirectoryView(rootlf);
                            flowdv.ShowDirectoryAsOptions(request, out, rootlf, rootlf.getLayer());
                        %>
                    </select>
                </td>
            </tr>
            <%if (wpd.getCheckStatus() == WorkPlanDb.CHECK_STATUS_PASSED) {%>
            <tr>
                <td>
                    修改原因
                </td>
                <td>
                    <textarea id="reason" name="reason" style="width:100%; height:50px"></textarea>
                </td>
            </tr>
            <%}%>
        </table>
    </form>
</div>

<div id="dlg" style="display:none">
    <form id="form1">
        结果&nbsp;
        <select id="assess" name="assess">
            <%
                SelectMgr sm = new SelectMgr();
                SelectDb sd = sm.getSelect("workplan_assess");
                Vector vsd = sd.getOptions();
                Iterator irsd = vsd.iterator();
                while (irsd.hasNext()) {
                    sod = (SelectOptionDb) irsd.next();
            %>
            <option value="<%=sod.getValue()%>"><%=sod.getName()%>
            </option>
            <%
                }
            %>
        </select>
    </form>
</div>

</body>
<script type="text/javascript">
    function initCalendar() {
        $('#beginDate').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d'
        });
        $('#endDate').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d'
        });
    }

    function doOnToolbarInited() {
        initCalendar();
    }

    var flex;

    function changeSort(sortname, sortorder) {
        window.location.href = "workplan_task.jsp?<%=urlStr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
    }

    function changePage(newp) {
        if (newp)
            window.location.href = "workplan_task.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
    }

    function rpChange(pageSize) {
        window.location.href = "workplan_task.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
    }

    function onReload() {
        window.location.reload();
    }

    function action(com, grid) {
        if (com == '添加') {
            // 当甘特图中任务项不为空时
            if ($('tr', $('#grid')).length > 0) {
                if ($('.trSelected', $('#grid')).length == 0) {
                    jAlert("请选择上级任务！", "提示");
                    return;
                }

                if ($('.trSelected', $('#grid')).length > 1) {
                    jAlert("请只选择一个任务！", "提示");
                    return;
                }
            }

            var ptaskId = "";
            $('.trSelected td:nth-child(3) div', $('#grid')).each(function (i) {
                ptaskId = $(this).text();
            });

            if (ptaskId == "") {
                o('parentTaskId').value = "无";
                $('#parentTaskName').html('无');
            } else {
                o("parentTaskId").value = ptaskId;
                $('#parentTaskName').html($('#realTaskName' + ptaskId).html());
            }

            var ptaskResource = "";
            $('.trSelected td:nth-child(6) div', $('#grid')).each(function (i) {
                ptaskResource = $(this).text().trim();
            });

            o("name").value = "";
            o("progress").value = "";
            o("start_date").value = "";
            o("end_date").value = "";
            o("task_resource").value = ptaskResource;
            o("startIsMilestone").checked = false;
            o("endIsMilestone").checked = false;

            o("workplan_related").value = "";
            o("work_plan_id").value = "<%=workplanId%>";
            o("status").value = '<%=WorkPlanTaskDb.STATUS_UNDEFINED%>';
            o("report_flow_type").value = "";

            $("#taskDlg").dialog({
                title: "添加任务",
                modal: true,
                bgiframe: true,
                width: 500,
                height: 550,
                // bgiframe:true,
                buttons: {
                    "取消": function () {
                        $(this).dialog("close");
                    },
                    "确定": function () {
                        // f_name为doCheckJS自动生成的变量
                        if (!LiveValidation.massValidate(f_name.formObj.fields))
                            return false;

                        $.ajax({
                            type: "post",
                            url: "workplan_do.jsp?op=addTask&parentTaskId=" + ptaskId + "&" + $('#formAdd').serialize(),
                            dataType: "html",
                            beforeSend: function (XMLHttpRequest) {
                                $('#grid').showLoading();
                            },
                            success: function (data, status) {
                                data = $.parseJSON(data);
                                if (data.ret == "0") {
                                    jAlert(data.msg, "提示");
                                } else {
                                    jAlert(data.msg, "提示");
                                    window.location.reload();
                                }
                            },
                            complete: function (XMLHttpRequest, status) {
                                $('#grid').hideLoading();
                            },
                            error: function (XMLHttpRequest, textStatus) {
                                // 请求出错处理
                                alert(XMLHttpRequest.responseText);
                            }
                        });

                        $(this).dialog("close");
                    }
                },
                closeOnEscape: true,
                draggable: true,
                resizable: true
            });

        } else if (com == "删除") {
            if ($('tr', $('#grid')).length > 0) {
                if ($('.trSelected', $('#grid')).length == 0) {
                    jAlert("请选择任务！", "提示");
                    return;
                }
            }

            var taskId = "";
            $('.trSelected td:nth-child(3) div', $('#grid')).each(function (i) {
                taskId = $(this).text();
            });

            jConfirm('您确定要删除么？', '提示', function (r) {
                if (!r) {
                    return;
                } else {
                    $.ajax({
                        type: "post",
                        url: "workplan_do.jsp",
                        data: {
                            op: "delTask",
                            taskId: taskId
                        },
                        dataType: "html",
                        beforeSend: function (XMLHttpRequest) {
                            $('#grid').showLoading();
                        },
                        success: function (data, status) {
                            data = $.parseJSON(data);
                            if (data.ret == "0") {
                                jAlert(data.msg, "提示");
                            } else {
                                jAlert(data.msg, "提示");
                                window.location.reload();
                            }
                        },
                        complete: function (XMLHttpRequest, status) {
                            $('#grid').hideLoading();
                        },
                        error: function (XMLHttpRequest, textStatus) {
                            // 请求出错处理
                            alert(XMLHttpRequest.responseText);
                        }
                    });
                }
            });
        } else if (com == "编辑") {
            if ($('tr', $('#grid')).length > 0) {
                if ($('.trSelected', $('#grid')).length == 0) {
                    jAlert("请选择任务！", "提示");
                    return;
                }
            }

            var taskId = "";
            $('.trSelected td:nth-child(3) div', $('#grid')).each(function (i) {
                taskId = $(this).text();
            });

            $.ajax({
                type: "post",
                url: "workplan_do.jsp",
                data: {
                    op: "getTask",
                    taskId: taskId
                },
                dataType: "html",
                beforeSend: function (XMLHttpRequest) {
                    $('#grid').showLoading();
                },
                success: function (data, status) {
                    data = $.parseJSON(data);
                    if (data.ret == "0") {
                        jAlert(data.msg, "提示");
                    } else {
                        o("name").value = data.name;
                        o("progress").value = data.progress;
                        o("start_date").value = data.start;
                        o("end_date").value = data.end;
                        o("task_resource").value = data.resource;
                        if (data.startIsMilestone == 1)
                            o("startIsMilestone").checked = true;
                        else
                            o("startIsMilestone").checked = false;
                        if (data.endIsMilestone == 1)
                            o("endIsMilestone").checked = true;
                        else
                            o("endIsMilestone").checked = false;

                        o("workplan_related").value = data.workplan_related;
                        o("work_plan_id").value = "<%=workplanId%>";
                        o("status").value = data.status;
                        o("report_flow_type").value = data.reportFlowType;

                        o("parentTaskId").value = data.parentTaskId;
                        $('#parentTaskName').html(data.parentTaskName);
                        o("depends").value = data.depends;

                        $("#taskDlg").dialog({
                            title: "编辑任务",
                            modal: true,
                            bgiframe: true,
                            width: 500,
                            height: 550,
                            // bgiframe:true,
                            buttons: {
                                "取消": function () {
                                    $(this).dialog("close");
                                },
                                "确定": function () {
                                    // f_name为doCheckJS自动生成的变量
                                    if (!LiveValidation.massValidate(f_name.formObj.fields))
                                        return false;

                                    <%if (wpd.getCheckStatus()==WorkPlanDb.CHECK_STATUS_PASSED) {%>
                                    if ($('#reason').val() == "") {
                                        jAlert("计划审核已通过，请填写修改原因！", "提示");
                                        return false;
                                    }
                                    <%}%>
                                    var dataParam = $("#formAdd").serializeArray();
                                    dataParam.push({"name": "id", "value": taskId});
                                    $.ajax({
                                        type: "post",
                                        contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                                        url: "workplan_do.jsp?op=editTask",
                                        data: dataParam,
                                        dataType: "html",
                                        beforeSend: function (XMLHttpRequest) {
                                            $('body').showLoading();
                                        },
                                        success: function (data, status) {
                                            data = $.parseJSON(data);
                                            if (data.ret == "0") {
                                                jAlert(data.msg, "提示");
                                            } else {
                                                jAlert(data.msg, "提示", function () {
                                                    window.location.reload();
                                                });
                                            }
                                        },
                                        complete: function (XMLHttpRequest, status) {
                                            $('body').hideLoading();
                                        },
                                        error: function (XMLHttpRequest, textStatus) {
                                            // 请求出错处理
                                            alert(XMLHttpRequest.responseText);
                                        }
                                    });

                                    $(this).dialog("close");
                                }
                            },
                            closeOnEscape: true,
                            draggable: true,
                            resizable: true
                        });
                    }
                },
                complete: function (XMLHttpRequest, status) {
                    $('#grid').hideLoading();
                },
                error: function (XMLHttpRequest, textStatus) {
                    // 请求出错处理
                    alert(XMLHttpRequest.responseText);
                }
            });
        } else if (com == "导出") {
            window.open('workplan_gantt_export.jsp?workplanId=<%=workplanId%>');
        } else if (com == "提醒") {
            jConfirm('您确定要发送计划变更提醒么？', '提示', function (r) {
                if (r) {
                    $.ajax({
                        type: "post",
                        url: "workplan_do.jsp?op=remind&workplanId=<%=workplanId%>",
                        dataType: "html",
                        beforeSend: function (XMLHttpRequest) {
                            $('#grid').showLoading();
                        },
                        success: function (data, status) {
                            data = $.parseJSON(data);
                            if (data.ret == "0") {
                                jAlert(data.msg, "提示");
                            } else {
                                jAlert(data.msg, "提示", function () {
                                    // window.location.reload();
                                });
                            }
                        },
                        complete: function (XMLHttpRequest, status) {
                            $('#grid').hideLoading();
                        },
                        error: function (XMLHttpRequest, textStatus) {
                            // 请求出错处理
                            alert(XMLHttpRequest.responseText);
                        }
                    });
                }
            });
        }

    }

    function assessTask(taskId, assess) {
        o("assess").value = assess;
        $("#dlg").dialog({
            title: "评价",
            modal: true,
            bgiframe: true,
            width: 200,
            height: 100,
            // bgiframe:true,
            buttons: {
                "取消": function () {
                    $(this).dialog("close");
                },
                "确定": function () {
                    $.ajax({
                        type: "post",
                        url: "workplan_do.jsp",
                        data: {
                            op: "assessTask",
                            assess: o("assess").value,
                            taskId: taskId
                        },
                        dataType: "html",
                        beforeSend: function (XMLHttpRequest) {
                            $('#dlg').showLoading();
                        },
                        success: function (data, status) {
                            data = $.parseJSON(data);
                            if (data.ret == "0") {
                                jAlert(data.msg, "提示");
                            } else {
                                jAlert(data.msg, "提示");
                                o("assess" + taskId).innerHTML = $("#assess").find("option:selected").text();
                            }
                        },
                        complete: function (XMLHttpRequest, status) {
                            $('#dlg').hideLoading();
                        },
                        error: function (XMLHttpRequest, textStatus) {
                            // 请求出错处理
                            alert(XMLHttpRequest.responseText);
                        }
                    });

                    $(this).dialog("close");
                }
            },
            closeOnEscape: true,
            draggable: true,
            resizable: true
        });
    }

    function move(taskId, isUp) {
        $.ajax({
            type: "post",
            url: "workplan_do.jsp?workplanId=<%=workplanId%>",
            data: {
                op: "move",
                direction: isUp,
                id: taskId
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('#grid').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == "0") {
                    jAlert(data.msg, "提示");
                } else {
                    jAlert(data.msg, "提示", function () {
                        window.location.reload();
                    });
                }
            },
            complete: function (XMLHttpRequest, status) {
                $('#grid').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    //隐藏子任务
    function hideChild(id) {
        $("." + id).hide();
        //设置cookies，记录当前任务折叠状态
        var expdate = new Date();
        var expday = 60;
        expdate.setTime(expdate.getTime() + (24 * 60 * 60 * 1000 * expday));
        document.cookie = id + "=" + escape("1") + ";expires=" + expdate.toGMTString();
        //设置点击事件及图片
        $("#" + id).attr("src", "../images/openico.jpg");
        document.getElementById(id).onclick = function () {
            showChild(id);
        }

    }

    //显示子任务
    function showChild(id) {
        $("." + id).show();
        //设置cookies，记录当前任务折叠状态
        var expdate = new Date();
        var expday = 60;
        expdate.setTime(expdate.getTime() + (24 * 60 * 60 * 1000 * expday));
        document.cookie = id + "=" + escape("0") + ";expires=" + expdate.toGMTString();
        //设置点击事件及图片
        $("#" + id).attr("src", "../images/putawayico.jpg");
        document.getElementById(id).onclick = function () {
            hideChild(id);
        }
        //遍历所有子任务，若是子任务含有子任务且子任务为折叠状态，则展示该任务子任务时，子任务的子任务不予展示
        $("." + id).each(function (i) {
            var thisId = this.id.substr(0, this.id.length - 2);//根据trID获取任务id
            if (unescape(get_cookie(thisId)) != null && unescape(get_cookie(thisId)) == "1") {
                $("." + thisId).hide();
            }
        });
    }

    <%
        // WorkPlanTaskDb wptd = new WorkPlanTaskDb();

        ParamConfig pc = new ParamConfig(wptd.getTable().getFormValidatorFile()); // "form_rule.xml");
        ParamChecker pck = new ParamChecker(request);
        out.print(pck.doGetCheckJS(pc.getFormRule("workplan_task_create")));
    %>
    $(function () {
        $('#start_date').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d'
        });
        $('#end_date').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d'
        });
        flex = $("#grid").flexigrid
        (
            {
                buttons: [
                    {name: '添加', bclass: 'add', onpress: action},
                    {name: '编辑', bclass: 'edit', onpress: action},
                    {name: '删除', bclass: 'delete', onpress: action},
                    {name: '提醒', bclass: 'check', onpress: action},
                    {name: '导出', bclass: 'export', onpress: action},
                    {name: '条件', bclass: 'btnseparator', type: 'include', id: 'searchTable'}
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
                /*autoHeight: true,*/
                width: document.documentElement.clientWidth,
                height: document.documentElement.clientHeight - 158
            }
        );
    })
</script>
</html>