<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.base.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.hr.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "read")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    Date dt = new Date();
    int curYear = DateUtil.getYear(dt);
    int curMonth = DateUtil.getMonth(dt) + 1;

    int bookId = ParamUtil.getInt(request, "bookId", -1);
    int year = ParamUtil.getInt(request, "year", curYear);
    int month = ParamUtil.getInt(request, "month", curMonth);

    String orderBy = ParamUtil.get(request, "orderBy");
    String sort = ParamUtil.get(request, "sort");

    if (orderBy.equals("")) {
        orderBy = "id";
    }

    if (sort.equals(""))
        sort = "desc";

    int pagesize = ParamUtil.getInt(request, "pageSize", 20);
%>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>工资表</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
    <link href="../../lte/css/font-awesome.min.css?v=4.4.0" rel="stylesheet">

    <script src="../../inc/common.js"></script>
    <script src="../../js/jquery-1.9.1.min.js"></script>
    <script src="../../js/jquery-migrate-1.2.1.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css"/>
    <script src="../../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script type="text/javascript" src="../../js/flexigrid.js"></script>

    <script src="../../js/jquery.bgiframe.js"></script>

    <script src="../../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>

    <link rel="stylesheet" type="text/css" href="../../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../../js/datepicker/jquery.datetimepicker.js"></script>

    <link href="../../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../../js/jquery-showLoading/jquery.showLoading.js"></script>

    <script src="../../js/jquery.raty.min.js"></script>
    <script src="../../js/BootstrapMenu.min.js"></script>

    <script type="text/javascript" src="../../js/jquery.editinplace.js"></script>
    <script type="text/javascript" src="../../js/jquery.toaster.js"></script>

    <style>
        input[type='text'] {
            line-height: normal; /*防止查询条件置于工具条时，input受其它样式影响紧贴于上方*/
            width: 100px;
        }

        /*
        .search-form input {
            width: 80px;
        }
        */
        .search-form input:not([type="radio"],[type="button"]) {
            width: 80px;
        }
    </style>
</head>
<body>
<table id="searchTable" width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
    <tr>
        <td height="23" align="left" style="padding-top:5px">
            <form id="searchForm" class="search-form" action="payroll_list.jsp" onsubmit="return searchFormOnSubmit()">
                &nbsp;
                姓名
                <input id="realName" name="realName" type="text"/>
                年份
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
                月份
                <select id="month" name="month">
                    <%
                        for (int i = 1; i <= 12; i++) {
                    %>
                    <option value="<%=i%>" <%=(i == curMonth) ? "selected" : ""%>><%=i %>
                    </option>
                    <%
                        }
                    %>
                </select>
                <input type="hidden" name="op" value="search"/>
                <input class="tSearch condBtnSearch" name="submit" type="button" onclick="doQuery()" value=" 搜索 "/>
            </form>
        </td>
    </tr>
</table>
<table id="grid" style="display:none"></table>
</body>
<script>
    function initCalendar() {
        $('input[kind=date]').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d'
        });
    }

    function doOnToolbarInited() {
    }

    var flex;

    function changeSort(sortname, sortorder) {
        if (!sortorder)
            sortorder = "desc";

        var params = $("form").serialize();
        // console.log(params);
        var urlStr = "<%=request.getContextPath()%>/salary/listPayroll.do?" + params;
        urlStr += "&pageSize=" + $("#grid").getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
        $("#grid").flexOptions({url: urlStr});
        $("#grid").flexReload();
    }

    function onReload() {
        doQuery();
    }

    function editCol(celDiv, id, colName) {
        $(celDiv).click(function () {
            // 该插件会上传值：original_value、update_value
            $(celDiv).editInPlace({
                url: "../../salary/editSalary.do",
                params: "colName=" + colName + "&id=" + id,
                saving_text: "保存中...",
                saving_image: "../../images/loading.gif",
                error: function (obj) {
                    alert(JSON.stringify(obj));
                },
                success: function (data) {
                    data = $.parseJSON(data);
                    if (data.ret == -1) {
                        return;
                    } else {
                        $.toaster({
                            "priority": "info",
                            "message": data.msg
                        });
                        $("#grid").flexReload();
                    }
                }
            });
        });
    }

    <%
    // user_name,科目
    String colProps = "{display: '姓名', name : 'realName', width : 150, sortable : true, align: 'center', hide: false}";
    String sql = "select subject from salary_book_subject where book_id=? and year=? and month=? order by id asc";
    JdbcTemplate jt = new JdbcTemplate();
    ResultIterator ri = jt.executeQuery(sql, new Object[]{bookId, year, month});
    while (ri.hasNext()) {
        ResultRecord rr = (ResultRecord)ri.next();
        String subjectCode = rr.getString(1);
        com.redmoon.oa.visual.FormDAO fdaoSubject = SalaryMgr.getSubject(subjectCode);
        String kind = fdaoSubject.getFieldValue("kind");
        if ("1".equals(kind)) {
            colProps += ",{display: '" + fdaoSubject.getFieldValue("name") + "', name : '" + subjectCode + "', width : 120, sortable : true, align: 'center', hide: false, process:editCol}";
        }
        else {
            colProps += ",{display: '" + fdaoSubject.getFieldValue("name") + "', name : '" + subjectCode + "', width : 120, sortable : true, align: 'center', hide: false}";
        }
    }
    %>
    var requestParams = [];
    requestParams.push({name: 'bookId', value: '<%=bookId%>'});
    requestParams.push({name: 'year', value: '<%=year%>'});
    requestParams.push({name: 'month', value: '<%=month%>'});

    var colModel = [<%=colProps%>];
    $("#grid").flexigrid({
        url: '../../salary/listPayroll.do',
        params: requestParams,
        dataType: 'json',
        colModel: colModel,
        buttons: [
            {name: '删除', bclass: 'delete', onpress: action},
            {name: '重新生成', bclass: 'pass', onpress: action},
            {name: '导出', bclass: 'export', onpress: action},
            {separator: true},
            {name: '条件', bclass: '', type: 'include', id: 'searchTable'}
        ],
        sortname: "<%=orderBy%>",
        sortorder: "<%=sort%>",
        usepager: true,
        checkbox: true,
        useRp: true,
        rp: <%=pagesize%>,

        // title: "通知",
        singleSelect: true,
        resizable: false,
        showTableToggleBtn: true,
        showToggleBtn: true,

        onChangeSort: changeSort,

        // onChangePage: changePage,
        // onRpChange: rpChange,
        onLoad: onLoad,
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
    });

    function onLoad() {
        try {
            onFlexiGridLoaded();
        } catch (e) {
        }
    }

    function action(com, grid) {
        if (com == '删除') {
            var ids = "";
            // value!='on' 过滤掉复选框按钮
            $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function (i) {
                if (ids == "")
                    ids = $(this).val().substring(3);
                else
                    ids += "," + $(this).val().substring(3);
            });
            del(ids);
        } else if (com == "导出") {
            window.open("<%=request.getContextPath()%>/salary/exportPayroll.do?bookId=<%=bookId%>&year=<%=year%>&month=<%=month%>&realName=" + encodeURI(o('realName').value));
        } else if (com == "重新生成") {
            var ids = "";
            // value!='on' 过滤掉复选框按钮
            $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function (i) {
                if (ids == "")
                    ids = $(this).val().substring(3);
                else
                    ids += "," + $(this).val().substring(3);
            });
            regenerate(ids);
        }
    }

    // 用于工具条自定义按钮的调用
    function getIdsSelected(onlyOne) {
        var selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
        if (selectedCount == 0) {
            return "";
        }

        if (selectedCount > 1 && onlyOne) {
            return "";
        }

        var ids = "";
        // value!='on' 过滤掉复选框按钮
        $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function (i) {
            var id = $(this).val().substring(3); // 去掉前面的row
            if (ids == "")
                ids = id;
            else
                ids += "," + id;
        });
        return ids;
    }

    $(function () {
        initCalendar();
    });

    function regenerate(ids) {
        if (ids == "") {
            jAlert('请选择记录!', '提示');
            return;
        }

        jConfirm('您确定要重新生成么？', '提示', function (r) {
            if (!r) {
                return;
            }

            $.ajax({
                type: "post",
                url: "<%=request.getContextPath()%>/salary/regenerateForUsers.do",
                data: {
                    ids: ids,
                    bookId: <%=bookId%>,
                    year: <%=year%>,
                    month: <%=month%>
                },
                dataType: "html",
                beforeSend: function (XMLHttpRequest) {
                    $("body").eq(0).showLoading();
                },
                success: function (data, status) {
                    data = $.parseJSON(data);
                    jAlert(data.msg, "提示");
                    if (data.ret == "1") {
                        doQuery();
                    }
                },
                complete: function (XMLHttpRequest, status) {
                    $("body").eq(0).hideLoading();
                },
                error: function (XMLHttpRequest, textStatus) {
                    // 请求出错处理
                    alert(XMLHttpRequest.responseText);
                }
            });
        });
    }

    function del(ids) {
        if (ids == "") {
            jAlert('请选择记录!', '提示');
            return;
        }

        jConfirm('您确定要删除么？', '提示', function (r) {
            if (!r) {
                return;
            }

            $.ajax({
                type: "post",
                url: "<%=request.getContextPath()%>/salary/delSalaryForUsers.do",
                data: {
                    ids: ids
                },
                dataType: "html",
                beforeSend: function (XMLHttpRequest) {
                    $("body").eq(0).showLoading();
                },
                success: function (data, status) {
                    data = $.parseJSON(data);
                    jAlert(data.msg, "提示");
                    if (data.ret == "1") {
                        /*
                        var ary = ids.split(",");
                        for (var i=0; i<ary.length; i++) {
                            $('#row' + ary[i]).remove();
                        }
                        */
                        doQuery();
                        // 置全选checkbox为非选中状态
                        $(".hDiv input[type='checkbox']").removeAttr("checked");
                    }
                },
                complete: function (XMLHttpRequest, status) {
                    $("body").eq(0).hideLoading();
                },
                error: function (XMLHttpRequest, textStatus) {
                    // 请求出错处理
                    alert(XMLHttpRequest.responseText);
                }
            });
        });
    }

    function doQuery() {
        var params = $("form").serialize();
        var urlStr = "<%=request.getContextPath()%>/salary/listPayroll.do?" + params;
        $("#grid").flexOptions({url: urlStr});
        $("#grid").flexReload();
    }

    function searchFormOnSubmit() {
        doQuery();
        return false;
    }
</script>
</html>
