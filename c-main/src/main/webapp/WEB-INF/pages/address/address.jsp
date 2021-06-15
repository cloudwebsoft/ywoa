<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>通讯录</title>
    <link type="text/css" rel="stylesheet" href="${skinPath}/css.css"/>
    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
    <link type="text/css" rel="stylesheet" href="${skinPath}/flexigrid/flexigrid.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <script type="text/javascript" src="../js/flexigrid.js"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script src="../js/jquery.toaster.js"></script>
    <%@ include file="../../../inc/nocache.jsp" %>
    <style>
        .search-form input, select {
            vertical-align: middle;
        }
    </style>
</head>
<body>
<table id="searchTable" width="98%" align="center">
    <tr>
        <td colspan="2" align="center" nowrap="nowrap">
            <form action="list.do" class="search-form" method="get" name="form1" id="form1">
                分组
                <input name="op" value="search" type="hidden"/>
                <select name="dir_code" id="dir_code">
                        ${dirOpts}
                </select>
                <script>
                    $(function () {
                        o("dir_code").value = "${typeId}";
                    })
                </script>
                <input name="type" value="${type}" type="hidden"/>
                姓名&nbsp;
                <input type="text" name="person" size="10" value="${person}"/>
                手机&nbsp;
                <input type="text" name="mobile" size="10" value="${mobile}"/>
                单位&nbsp;
                <input type="text" name="company" size="10" value="${company}"/>
                <input type="hidden" name="mode" value="${mode}"/>
                &nbsp;<input class="tSearch" type="submit" value="搜索"/>
            </form>
        </td>
    </tr>
</table>
<form name="form1" action="../message_oa/sms_send.jsp" method="post">
    <table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
        <thead>
        <tr>
            <th width="72" style="cursor:pointer">姓名</th>
            <th width="72" style="cursor:pointer">职务</th>
            <th width="72" style="cursor:pointer">单位</th>
            <th width="100" style="cursor:pointer">手机</th>
            <th width="100" style="cursor:pointer">电话</th>
            <th width="72" style="cursor:pointer">短号</th>
            <th width="150" style="cursor:pointer">邮箱</th>
            <th width="125" style="cursor:pointer">操作</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${result}" var="addr">
            <tr id="row${addr.id}">
                <td align="left">
                    <a href="javascript:;" onclick="addTab('${addr.person}', 'address/show.do?id=${addr.id}&mode=show')">${addr.person}</a>
                </td>
                <td>${addr.job}</td>
                <td>${addr.company}</td>
                <td>${addr.mobile}</td>
                <td>${addr.tel}</td>
                <td>${addr.MSN}</td>
                <td>${addr.email}</td>
                <td>
                    <a href="javascript:;" onclick="addTab('${addr.person}', 'address/show.do?id=${addr.id}&mode=show')">查看</a>
                    <c:if test="${isUseSMS}">
                        &nbsp;&nbsp;<a href="../message_oa/sms_send.jsp?mobile=${addr.mobile}">短信</a>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</form>
</body>
<script>
    function openExcel() {
        var sql = "${sql3des}";
        window.open("address_excel.jsp?sql=" + sql);
    }

    function openWin(url, width, height) {
        var newwin = window.open(url, "_blank", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width=" + width + ",height=" + height);
    }

    function importExcel() {
        var url = "import_excel.jsp?type=${type}&group=${typeId}";
        openWin(url, 360, 50);
    }

    function add() {
        window.location.href = "add.do?type=${type}&typeId=${typeId}";
    }

    function sendSms() {
        var ids = "";
        $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function (i) {
            var id = $(this).val();
            if (ids == "")
                ids = id;
            else
                ids += "," + id;
        });
        if (ids == "") {
            jAlert("请选择人员！", "提示");
            return;
        }
        var mobiles = "";
        var ary = ids.split(",");
        for (i in ary) {
            var id = ary[i];
            if ($('#' + id).attr('mobile') != '') {
                if (mobiles == "") {
                    mobiles = $('#' + id).attr('mobile');
                } else {
                    mobiles += "," + $('#' + id).attr('mobile');
                }
            }
        }
        if (mobiles == "") {
            jAlert("请选择有手机号的人员！", "提示");
            return;
        }
        window.location.href = "../message_oa/sms_send.jsp?mobile=" + mobiles;
    }

    $(function () {
        flex = $("#grid").flexigrid({
                buttons: [
                    <c:if test="${canManage}">
                    {name: '添加', bclass: 'add', onpress: actions},
                    {name: '修改', bclass: 'edit', onpress: actions},
                    {name: '删除', bclass: 'delete', onpress: actions},
                    {name: '导入', bclass: 'import1', onpress: actions},
                    {name: '导出', bclass: 'export', onpress: actions},
                    </c:if>
                    <c:if test="${isUseSMS}">
                    {name: '短信', bclass: 'sms', onpress: actions},
                    </c:if>
                    {separator: true},
                    {name: '条件', bclass: '', type: 'include', id: 'searchTable'}
                ],
                width: 'auto',
                sortname: "${orderBy}",
                sortorder: "${sort}",
                url: false,
                usepager: true,
                checkbox: true,
                page: ${curPage},
                total: ${total},
                useRp: true,
                rp: ${pageSize},

                // title: "通知",
                singleSelect: true,
                resizable: false,
                showTableToggleBtn: true,
                showToggleBtn: false,
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
                autoHeight: true
                // width: document.documentElement.clientWidth,
                // height: document.documentElement.clientHeight - 84
            }
        );
    });

    function changeSort(sortname, sortorder) {
        window.location.href = "list.do?pageSize=" + flex.getOptions().rp + "&dir_code=${typeId}&mode=${mode}&orderBy=" + sortname + "&sort=" + sortorder + "&${searchStr}";
    }

    function changePage(newp) {
        if (newp) {
            window.location.href = "list.do?CPages=" + newp + "&type=${type}&dir_code=${typeId}&mode=${mode}&pageSize=" + flex.getOptions().rp + "&${searchStr}";
        }
    }

    function rpChange(pageSize) {
        window.location.href = "list.do?CPages=${curPage}&type=${type}&dir_code=${typeId}&mode=${mode}&pageSize=" + pageSize + "&${searchStr}";
    }

    function onReload() {
        // window.location.reload();
    }

    function actions(com, grid) {
        if (com == '短信') {
            sendSms();
        } else if (com == '修改') {
            selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
            if (selectedCount == 0) {
                jAlert('请选择记录!', '提示');
                return;
            } else if (selectedCount > 1) {
                jAlert('请选择一条记录!', '提示');
                return;
            }

            var tabId = getActiveTabId();

            var id = "";
            // value!='on' 过滤掉复选框按钮
            $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function (i) {
                id = $(this).val().substring(3);
            });
            addTab('通讯录-修改', 'address/edit.do?type=${type}&id=' + id + '&tabIdOpener=' + tabId);
        } else if (com == '导入') {
            importExcel();
        } else if (com == '导出') {
            openExcel();
        } else if (com == '删除') {
            var ids = "";
            // value!='on' 过滤掉复选框按钮
            $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function (i) {
                if (ids == "")
                    ids = $(this).val().substring(3);
                else
                    ids += "," + $(this).val().substring(3);
            });
            if (ids == "") {
                jAlert('请选择记录!', '提示');
                return;
            }
            jConfirm("您确定要删除么？", "提示", function (r) {
                if (!r) {
                    return;
                } else {
                    $.ajax({
                        type: "post",
                        url: "delBatch.do",
                        contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                        data: {
                            ids: ids
                        },
                        dataType: "html",
                        beforeSend: function (XMLHttpRequest) {
                            $('body').showLoading();
                        },
                        success: function (data, status) {
                            data = $.parseJSON(data);
                            if (data.ret == "1") {
                                $.toaster({priority: 'info', message: data.msg});
                                var ary = ids.split(",");
                                for (var i = 0; i < ary.length; i++) {
                                    $('#row' + ary[i]).remove();
                                }
                            } else {
                                $.toaster({priority: 'info', message: data.msg});
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
                }
            });
        } else if (com == '添加') {
            add();
        }
    }

    $(function () {
        // $(".bDiv").css({"height": "448px"});
    })
</script>
</html>