<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %>
<%@ include file="../inc/nocache.jsp" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "archive.user")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String orderBy = ParamUtil.get(request, "orderBy");
    if (orderBy.equals(""))
        orderBy = "fl.mydate";
    String sort = ParamUtil.get(request, "sort");
    if (sort.equals(""))
        sort = "desc";

    String op = ParamUtil.get(request, "op");
    String what = ParamUtil.get(request, "what");

    String beginDate = ParamUtil.get(request, "beginDate");
    String endDate = ParamUtil.get(request, "endDate");
    String dept = ParamUtil.get(request, "dept_code");
    String jbtype = ParamUtil.get(request, "jb_type");

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>加班</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
    <style>
        .search-form input, select {
            vertical-align: middle;
        }
        input {
            line-height: normal; /*防止查询条件置于工具条时，input受其它样式影响紧贴于上方*/
        }
        .search-form input:not([type="radio"],[type="button"]) {
            width: 80px;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery.js"></script>
    <script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <script type="text/javascript" src="../js/flexigrid.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
    <script>
        var curOrderBy = "<%=orderBy%>";
        var sort = "<%=sort%>";

        function doSort(orderBy) {
            if (orderBy == curOrderBy)
                if (sort == "asc")
                    sort = "desc";
                else
                    sort = "asc";

            window.location.href = "jb_list_m.jsp?orderBy=" + orderBy + "&sort=" + sort + "&op=<%=op%>&what=<%=StrUtil.UrlEncode(what)%>&beginDate=<%=beginDate%>&endDate=<%=endDate%>";
        }
    </script>
</head>
<body>
<table id="searchTable" width="100%" border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td align="center">
            <form class="search-form" action="jb_list_m.jsp" method="get">
                用户
                <input type="text" name="what" size="10" value="<%=what%>"/>
                &nbsp;&nbsp;部门
                <select id="dept_code" name="dept_code" style="height:24px">
                    <%
                        DeptDb lf = new DeptDb(DeptDb.ROOTCODE);
                        DeptView dv = new DeptView(lf);
                        dv.ShowDeptAsOptions(out, lf, lf.getLayer());
                    %>
                </select>&nbsp;&nbsp;
                <script>
                    $("#dept_code").find("option[value='<%=dept%>']").attr("selected", true);
                </script>
                加班类别
                <select id="jb_type" name="jb_type">
                    <option value="">不限</option>
                    <option value="工作日">工作日</option>
                    <option value="休息日">休息日</option>
                    <option value="节假日">节假日</option>
                </select>
                <script>
                    $("#jb_type").find("option[value='<%=jbtype%>']").attr("selected", true);
                </script>
                &nbsp;&nbsp;
                从
                <input type="text" id="beginDate" name="beginDate" size="10" value="<%=beginDate%>"/>
                至
                <input type="text" id="endDate" name="endDate" size="10" value="<%=endDate%>"/>
                &nbsp;
                <input class="tSearch" name="submit" type=submit value="搜索">
                <input name="op" value="search" type="hidden"/>
            </form>
        </td>
    </tr>
</table>
<%
    // STATUS_FINISHED说明已经销假，流程已完毕
    String now = SQLFilter.getDateStr(DateUtil.format(new java.util.Date(), "yyyy-MM-dd"), "yyyy-MM-dd");
    String sql = "select f.flowId from form_table_jbsqd f, flow fl,users u where f.flowId=fl.id  and (fl.status=" + WorkflowDb.STATUS_STARTED + " or fl.status=" + WorkflowDb.STATUS_FINISHED + ")" + " and u.name = f.applier ";
    if (op.equals("search")) {
        if (!what.equals("")) {
            sql += " and u.realName like " + StrUtil.sqlstr("%" + what + "%");
        }
        if (!jbtype.equals("")) {
            sql += " and f.jblb=" + StrUtil.sqlstr(jbtype);
        }
        if (!beginDate.equals("") && !endDate.equals("")) {
            java.util.Date d = DateUtil.parse(endDate, "yyyy-MM-dd");
            sql += " and ((f.kssj>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + " and f.kssj<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd") + ") or (f.jssj<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd") + " and f.jssj>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + "))";
        } else if (!beginDate.equals("")) {
            sql += " and f.kssj>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
        } else if (beginDate.equals("") && !endDate.equals("")) {
            sql += " and f.kssj<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd");
        }
    }
// and " + now + ">=kssj" + " and " + now + "<=jssj and xjrq is null
    int result = ParamUtil.getInt(request, "result", -1);
    if (result != -1)
        sql += " and f.result='" + result + "'";
    sql += " order by " + orderBy + " " + sort;

// out.print(sql);

    FormDb fd = new FormDb();
    fd = fd.getFormDb("jbsqd");

    String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
    if (strcurpage.equals(""))
        strcurpage = "1";
    if (!StrUtil.isNumeric(strcurpage)) {
        out.print(StrUtil.makeErrMsg("标识非法！"));
        return;
    }
    int pagesize = ParamUtil.getInt(request, "pageSize", 10);
    int curpage = Integer.parseInt(strcurpage);

// out.print(sql);

    WorkflowDb wf = new WorkflowDb();
    ListResult lr = null;
    Vector v = null;
    try {
        lr = wf.listResult(sql, curpage, pagesize);
    } catch (ErrMsgException e) {
        LogUtil.getLog("加班记录页面").error(e.getMessage());
    }
    int total = 0;
    if (lr != null) {
        total = lr.getTotal();
        v = lr.getResult();
    }

    Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
    int totalpages = paginator.getTotalPages();
    if (totalpages == 0) {
        curpage = 1;
        totalpages = 1;
    }
    Iterator ir = null;
    if (v != null)
        ir = v.iterator();
%>
<table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
    <tbody>
    <thead>
    <tr>
        <th width="80" style="cursor:pointer" abbr="applier">姓名</th>
        <th width="90" style="cursor:pointer" abbr="deptName">部门</th>
        <th width="70" style="cursor:pointer" abbr="jblb">加班类别</th>
        <th width="80" style="cursor:pointer" abbr="day_count">加班时长(H)</th>
        <th width="110" style="cursor:pointer" abbr="strBeginDate">开始时间</th>
        <th width="110" style="cursor:pointer" abbr="strEndDate">结束时间</th>
        <th width="110" style="cursor:pointer" abbr="Mydate">申请时间</th>
        <th width="80" style="cursor:pointer" abbr="result">审批结果</th>
        <th width="90" style="cursor:pointer" abbr="StatusDesc">流程状态</th>
        <th width="100" style="cursor:pointer" abbr="checker">审批者</th>
        <th width="70">操作</th>
    </tr>
    </thead>
    <%
        Leaf ft = new Leaf();
        UserMgr um = new UserMgr();
        FormDAO fdao = new FormDAO();
        DeptMgr dm = new DeptMgr();
        while (ir != null && ir.hasNext()) {
            WorkflowDb wfd = (WorkflowDb) ir.next();
            fdao = fdao.getFormDAO(wfd.getId(), fd);
            String strBeginDate = StrUtil.getNullStr(fdao.getFieldValue("kssj"));
            String strEndDate = StrUtil.getNullStr(fdao.getFieldValue("jssj"));
            String xjrq = StrUtil.getNullStr(fdao.getFieldValue("xjrq"));
            DeptDb dd = dm.getDeptDb(fdao.getFieldValue("dept"));
            String deptName = "";
            if (dd != null)
                deptName = dd.getName();
            String checker = fdao.getFieldValue("checker");
            if (checker != null && !checker.equals(""))
                checker = um.getUserDb(checker).getRealName();
            java.util.Date eDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
            java.util.Date xjDate = DateUtil.parse(xjrq, "yyyy-MM-dd");
            boolean isExpire = false;
            if (DateUtil.compare(xjDate, eDate) == 1) {
                isExpire = true;
            }
    %>
    <tr class="highlight">
        <td align="center"><%=um.getUserDb(fdao.getFieldValue("applier")).getRealName()%>
        </td>
        <td align="center"><%=deptName%>
        </td>
        <td align="center"><%=fdao.getFieldValue("jblb")%>
        </td>
        <td align="center"><%=fdao.getFieldValue("day_count")%>
        </td>
        <td align="center"><%=DateUtil.format(DateUtil.parse(strBeginDate, "yyyy-MM-dd HH:mm:ss"), "yy-MM-dd HH:mm")%>
        </td>
        <td align="center"><%=DateUtil.format(DateUtil.parse(strEndDate, "yyyy-MM-dd HH:mm:ss"), "yy-MM-dd HH:mm")%>
        </td>
        <td align="center"><%=DateUtil.format(wfd.getMydate(), "yy-MM-dd HH:mm")%>
        </td>
        <%
            if (fdao.getFieldValue("result") != null) {
        %>
        <td align="center"><%=fdao.getFieldValue("result").equals("1") ? "通过" : "不通过"%>
        </td>
        <%} else { %>
        <td align="center"></td>
        <%} %>
        <td align="center"><%=wfd.getStatusDesc()%>
        </td>
        <td align="center"><%=checker%>
        </td>
        <td align="center"><a target="_blank" href="../flow_form_edit.jsp?flowId=<%=wfd.getId()%>">编辑</a></td>
    </tr>
    <%}%>
    </tbody>
</table>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0" class="percent98">
    <tr>
        <td width="34%" align="left">

        </td>
        <td width="66%" align="right"><%
            String querystr = "op=" + op + "&orderBy=" + orderBy + "&sort=" + sort + "&op=" + op + "&what=" + StrUtil.UrlEncode(what) + "&beginDate=" + beginDate + "&endDate=" + endDate;
            //out.print(paginator.getCurPageBlock("?"+querystr));
        %></td>
    </tr>
</table>
</body>
<script>
    function initCalendar() {
        $('#beginDate').datetimepicker({
            lang: 'ch',
            datepicker: true,
            timepicker: false,
            format: 'Y-m-d'
        });

        $('#endDate').datetimepicker({
            lang: 'ch',
            datepicker: true,
            timepicker: false,
            format: 'Y-m-d'
        });
    }

    function doOnToolbarInited() {
        initCalendar();
    }

    $(document).ready(function () {
        flex = $("#grid").flexigrid
        (
            {
                buttons: [
                    {name: '导出', bclass: 'export', onpress: action},
                    //{separator: true},
                    {name: '条件', bclass: 'btnseparator', type: 'include', id: 'searchTable'}
                ],
                /*
                searchitems : [
                    {display: 'ISO', name : 'iso'},
                    {display: 'Name', name : 'name', isdefault: true}
                    ],
                sortname: "iso",
                sortorder: "asc",
                */
                url: false,
                usepager: true,
                checkbox: false,
                page: <%=curpage%>,
                total: <%=total%>,
                useRp: true,
                rp: <%=pagesize%>,

                // title: "通知",
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
    });

    function action(com, grid) {
        if (com == '导出') {

            window.open('jb_list_excel.jsp?orderBy=<%=orderBy%>&sort=<%=sort%>&op=<%=op%>&what=<%=StrUtil.UrlEncode(what)%>&beginDate=<%=beginDate%>&endDate=<%=endDate%>')
        }
    }

    function changeSort(sortname, sortorder) {
        window.location.href = "jb_list_m.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
    }

    function changePage(newp) {
        if (newp)
            window.location.href = "jb_list_m.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
    }

    function rpChange(pageSize) {
        window.location.href = "jb_list_m.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
    }

    function onReload() {
        window.location.reload();
    }
</script>
</html>