<%@ page contentType="text/html;charset=utf-8" language="java"
         errorPage="" %>
<%@ page import="com.redmoon.oa.notice.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.hr.*" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="cn.js.fan.db.ResultRecord" %>
<%@ page import="com.cloudwebsoft.framework.util.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "kaoqin.admin")) {
        out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(
                request, "pvg_invalid")));
        return;
    }
    String username = privilege.getUser(request);
    String skincode = UserSet.getSkin(request);
    if (skincode == null || skincode.equals(""))
        skincode = UserSet.defaultSkin;
    SkinMgr skm = new SkinMgr();
    Skin skin = skm.getSkin(skincode);
    String skinPath = skin.getPath();
    String user = privilege.getUser(request);

    String beginDate = ParamUtil.get(request, "begin_date");
    String endDate = ParamUtil.get(request, "end_date");
    String kq_date = ParamUtil.get(request, "kq_date");
    SignMgr kq = new SignMgr();
    String[] preDate = kq.getPreMonDate();
    String begindate = preDate[0];                     //获取上一个月的开始日期
    String enddate = preDate[1];                      //获取上一个月的结束日期
    if ("".equals(beginDate))
        beginDate = begindate;
    if ("".equals(endDate))
        endDate = enddate;
    String departCode = ParamUtil.get(request, "depart");
    String name = ParamUtil.get(request, "name");
    String sql = "select d.name,kq.number,kq.name,sum(kq.txsc),sum(kq.earlycount),sum(kq.latecount),sum(kq.abscount),sum(kq.nocount),sum(kq.days),sum(kq.tripday),sum(kq.sickday),sum(kq.thingday),sum(kq.yearday),sum(kq.marryday),sum(kq.maternityday),sum(kq.otherday),sum(kq.workday),sum(kq.wcday),sum(supplement_count) from kaoqin_arrange kq,users u,dept_user du,department d where kq.number = u.person_no and u.name = du.USER_NAME and du.DEPT_CODE = d.`code` and kq.date >= "
            + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd")
            + " and kq.date <= "
            + SQLFilter.getDateStr(endDate, "yyyy-MM-dd") + " group by d.name,kq.number,kq.name ";
    String op = ParamUtil.get(request, "op");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>考勤汇总</title>
    <%@ include file="../../inc/nocache.jsp" %>
    <link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css"/>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
    <script type="text/javascript" src="../../inc/common.js"></script>
    <script src="../../js/jquery-1.9.1.min.js"></script>
    <script src="../../js/jquery-migrate-1.2.1.min.js"></script>
    <link rel="stylesheet" type="text/css" href="../../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../../js/datepicker/jquery.datetimepicker.js"></script>
    <script type="text/javascript" src="../../js/flexigrid.js"></script>
    <script type="text/javascript" src="../../inc/livevalidation_standalone.js"></script>
    <script src="../../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
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
    <!-- 解决谷歌火狐样式问题 -->
    <style>
        .flexigrid div.tDiv2 {
            float: none;
        }
    </style>
</head>
<body>
<%
    if ("search".equals(op)) {
        if (!"".equals(beginDate) && "".equals(endDate)) {
            out.print(StrUtil.jAlert("请选择结束日期！", "提示"));
        } else if ("".equals(beginDate) && !"".equals(endDate)) {
            out.print(StrUtil.jAlert("请选择开始日期！", "提示"));
        } else {
            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            boolean isUseKqj = cfg.getBooleanProperty("isUseKqj");
            String condition;
            if (isUseKqj) {
                condition = " where kq.number = u.person_no and u.name = du.USER_NAME and du.DEPT_CODE = d.`code`";
            } else {
                condition = " where kq.name = u.name and u.name = du.USER_NAME and du.DEPT_CODE = d.`code`";
            }

            if (!"0".equals(departCode) && !"".equals(departCode) && !DeptDb.ROOTCODE.equals(departCode)) {
                condition += " and d.code = " + SQLFilter.sqlstr(departCode);
            }
            if ("previous".equals(kq_date)) {
                condition += " and kq.date >= " + SQLFilter.getDateStr(begindate, "yyyy-MM-dd") + " and kq.date <= " + SQLFilter.getDateStr(enddate, "yyyy-MM-dd");
            } else {
                condition += " and kq.date >= " + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + " and kq.date <= " + SQLFilter.getDateStr(endDate, "yyyy-MM-dd");
            }
            if (!"".equals(name)) {
                condition += " and u.realname like " + SQLFilter.sqlstr("%" + name + "%");
            }
            sql = "select d.name,kq.number,kq.name,sum(kq.txsc),sum(kq.earlycount),sum(kq.latecount),sum(kq.abscount),sum(kq.nocount),sum(kq.days),sum(kq.tripday),sum(kq.sickday),sum(kq.thingday),sum(kq.yearday),sum(kq.marryday),sum(kq.maternityday),sum(kq.otherday),sum(kq.workday),sum(kq.wcday),sum(supplement_count) from kaoqin_arrange kq,users u,dept_user du,department d" + condition + " group by d.name,kq.number,kq.name";
        }
    }
    String querystr = "op=" + op + "&begin_date=" + beginDate + "&end_date=" + endDate + "&kq_date=" + kq_date + "&depart=" + departCode + "&name=" + name;
    JdbcTemplate jt = new JdbcTemplate();
    ResultIterator ri = null;                                                    //   考勤汇总迭代
    Paginator paginator = new Paginator(request);
    int curpage = paginator.getCurPage();
    int pagesize = ParamUtil.getInt(request, "pageSize", 10);
    long total = 0;
    try {
        ri = jt.executeQuery(sql, curpage, pagesize);
        total = ri.getTotal();
    } catch (Exception e) {
        LogUtil.getLog(getClass()).error("考勤数据展示异常：" + StrUtil.trace(e));
    } finally {
        jt.close();
    }
    paginator.init(total, pagesize);
    // 设置当前页数和总页数
    int totalpages = paginator.getTotalPages();
    if (totalpages == 0) {
        curpage = 1;
        totalpages = 1;
    }
%>
<table id="searchTable" width="100%" border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td width="48%" height="30" align="left">
            <form name="formSearch" class="search-form" action="kq_arrange_list.jsp?op=search"
                  method="get" onsubmit="return submitValidate();">
                部门
                <select id="depart" name="depart">
                    <%
                        DeptDb lf = new DeptDb(DeptDb.ROOTCODE);
                        DeptView dv = new DeptView(lf);
                        dv.ShowDeptAsOptions(out, lf, lf.getLayer());
                    %>
                </select>
                <script>
                    $("#depart").find("option[value='<%=departCode%>']").attr("selected", true);
                </script>
                &nbsp;&nbsp;姓名
                <input id="name" name="name" type="text" size="10" value="<%=name %>"/>
                &nbsp;&nbsp;查询时间
                <select name="kq_date" id="kq_date" onclick="changeDateQuery()">
                    <option value="previous">上一月</option>
                    <option value="custom">自定义</option>
                </select>
                <span id="changeDateQuery" style="display:none;">
					&nbsp;&nbsp;开始时间
					<input id="begin_date" name="begin_date" type="text" value="<%=beginDate%>" size="10"/>
					&nbsp;&nbsp;结束时间
					<input id="end_date" name="end_date" value="<%=endDate%>" type="text" size="10"/>
					</span>&nbsp;&nbsp;
                <input class="tSearch" type="submit" value="搜索"/>
            </form>
        </td>
    </tr>
</table>
<table width="100%" border="0" cellpadding="0" cellspacing="0" id="grid" align="center">
    <thead>
    <tr align="left">
        <th width="80">
            部门
        </th>
        <th width="80">
            编号
        </th>
        <th width="80">
            姓名
        </th>
        <th width="80">
            正常出勤天数
        </th>
        <th width="80">
            早退次数
        </th>
        <th width="80">
            迟到次数
        </th>
        <th width="80">
            旷工天数
        </th>
        <th width="80">
            缺勤次数
        </th>
        <th width="80">
            补签次数
        </th>
        <th width="80">
            出差天数
        </th>
        <th width="80">
            病假天数
        </th>
        <th width="80">
            事假天数
        </th>
        <th width="80">
            年假天数
        </th>
        <th width="80">
            婚假天数
        </th>
        <th width="80">
            产假天数
        </th>
        <th width="80">
            其它天数
        </th>
        <th width="80">
            外出天数
        </th>
        <th width="80">
            加班(小时)
        </th>
        <th width="80">
            调休(小时)
        </th>
    </tr>
    </thead>
    <tbody>
    <%
        UserDb ud = new UserDb();
        ResultRecord rd = null;
        while (ri != null && ri.hasNext()) {
            rd = (ResultRecord) ri.next();
            String deptName = rd.getString(1);
            String number = rd.getString(2);

            ud = ud.getUserDb(rd.getString(3));
            String realName = ud.getRealName();

            double txsc = rd.getDouble(4);
            int earlycount = rd.getInt(5);
            int latecount = rd.getInt(6);
            int abscount = rd.getInt(7);
            int nocount = rd.getInt(8);
            int days = rd.getInt(9);            // 正常出勤天数
            int tripDay = rd.getInt(10);
            int sickDay = rd.getInt(11);
            int thingDay = rd.getInt(12);
            int yearDay = rd.getInt(13);
            int marryDay = rd.getInt(14);
            int maternityDay = rd.getInt(15);
            int otherDay = rd.getInt(16);
            double workDay = rd.getDouble(17);   // 加班次数
            int wcDay = rd.getInt(18);
            int supplementCount = rd.getInt(19); // 补签
    %>
    <tr>
        <td width="80"><%=deptName%>
        </td>
        <td width="80"><%=number%>
        </td>
        <td width="80"><%=realName%>
        </td>
        <td width="80"><%=days%>
        </td>
        <td width="80"><%=earlycount%>
        </td>
        <td width="80"><%=latecount%>
        </td>
        <td width="80"><%=nocount%>
        </td>
        <td width="80"><%=abscount%>
        </td>
        <td width="80"><%=supplementCount%>
        </td>
        <td width="80"><%=tripDay%>
        </td>
        <td width="80"><%=sickDay%>
        </td>
        <td width="80"><%=thingDay%>
        </td>
        <td width="80"><%=yearDay%>
        </td>
        <td width="80"><%=marryDay%>
        </td>
        <td width="80"><%=maternityDay%>
        </td>
        <td width="80"><%=otherDay%>
        </td>
        <td width="80"><%=wcDay%>
        </td>
        <td width="80"><%=NumberUtil.round(workDay, 1)%>
        </td>
        <td width="80"><%=txsc%>
        </td>
        <%
            }
        %>
    </tr>
    </tbody>
</table>
</body>
<script>
    function initCalendar() {
        $('#begin_date').datetimepicker({
            lang: 'ch',
            datepicker: true,
            timepicker: false,
            format: 'Y-m-d'
        });

        $('#end_date').datetimepicker({
            lang: 'ch',
            datepicker: true,
            timepicker: false,
            format: 'Y-m-d'
        });
    }

    function doOnToolbarInited() {
        initCalendar();
    }

    var flex;

    function changeSort(sortname, sortorder) {
        window.location.href = "kq_arrange_list.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
    }

    function changePage(newp) {
        if (newp)
            window.location.href = "kq_arrange_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
    }

    function rpChange(pageSize) {
        window.location.href = "kq_arrange_list.jsp?<%=querystr%>&CPages=1&pageSize=" + pageSize;
    }

    function onReload() {
        window.location.reload();
    }

    $(document).ready(function () {
        flex = $("#grid").flexigrid
        (
            {
                buttons: [
                    {name: '导出', bclass: 'export', onpress: action},
                    {separator: true},
                    {name: '条件', bclass: '', type: 'include', id: 'searchTable'}
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

        function action(com, grid) {
            if (com == "导出") {
                window.open('kq_arrange_export.jsp?deptCode=<%=departCode%>&begin_date=<%=beginDate%>&end_date=<%=endDate%>&op=<%=op%>&name=<%=name%>');
            }
        }

        /**
         Calendar.setup({
					inputField     :    "begin_date",
					ifFormat       :    "%Y-%m-%d",
					showsTime      :    false,
					singleClick    :    false,
					align          :    "Tl",
					step           :    1
				});
         Calendar.setup({
					inputField     :    "end_date",
					ifFormat       :    "%Y-%m-%d",
					showsTime      :    false,
					singleClick    :    false,
					align          :    "Tl",
					step           :    1
				});*/
        var kq_date_type = "<%=kq_date%>";
        if (kq_date_type == "custom") {
            $("#kq_date").val("custom");
            $("#changeDateQuery").css("display", "inline");
        }
    });

    function submitValidate() {
        var beginDate = $("#begin_date").val();
        var endDate = $("#end_date").val();
        if (beginDate != "" && endDate == "") {
            jAlert("请选择结束日期!", "提示");
            return false;
        }
        if (beginDate == "" && endDate != "") {
            jAlert("请选择开始日期!", "提示");
            return false;
        }
        return true;
    }

    function changeDateQuery() {
        var kq_date = $("#kq_date").val();
        if (kq_date == "previous")
            $("#changeDateQuery").css("display", "none");
        else
            $("#changeDateQuery").css("display", "inline");
    }
</script>

</html>