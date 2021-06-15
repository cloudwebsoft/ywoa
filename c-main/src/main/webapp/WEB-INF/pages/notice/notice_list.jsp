<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>通知列表</title>
    <%@ include file="../../../inc/nocache.jsp" %>
    <link type="text/css" rel="stylesheet" href="${skinPath}/css.css"/>
    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
    <link type="text/css" rel="stylesheet" href="${skinPath}/flexigrid/flexigrid.css"/>
    <style>
        .search-form input, select {
            vertical-align: middle;
        }
    </style>
    <script type="text/javascript" src="../inc/common.js"></script>
        <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script type="text/javascript" src="../js/flexigrid.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>

    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
</head>
<body>
<table id="searchTable" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
        <td width="48%" height="30" align="left">
            <form class="search-form" action="list.do" method="get">
                <input id="op" name="op" value="search" type="hidden"/>
                <select id="cond" name="cond">
                    <option value="title">标题</option>
                    <option value="content">内容</option>
                </select>
                <input id="what" name="what" size="15" value="${what}"/>
                &nbsp;从
                <input size="8" id="fromDate" name="fromDate" value="${fromDate}"/>
                至
                <input size="8" id="toDate" name="toDate" value="${toDate}"/>
                <input class="tSearch" value="搜索" type="submit"/>
            </form>
        </td>
    </tr>
</table>
<table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
    <thead>
    <tr>
        <th width="371" style="cursor:pointer">标题</th>
        <th width="136" align="center" style="cursor:pointer">发布者</th>
        <th width="102" align="center" style="cursor:pointer">类别</th>
        <th width="169" align="center" style="cursor:pointer">有效期</th>
        <th width="171" align="center" style="cursor:pointer">发布日期</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${result}" var="notice">
    <tr id="${notice.id}">
        <td>
            <c:if test="${notice.noticeLevel>0}">
                <img src="../images/important_r.png" align="absmiddle" title="重要通知"/>
            </c:if>
            <a href="show.do?id=${notice.id}&isShow=${notice.isShow}&flowId=${notice.flowId}">
                <jsp:useBean id="now" class="java.util.Date"/>
                <fmt:formatDate value="${now}" type="both" dateStyle="long" pattern="yyyy-MM-dd" var="curDay"/>
                <c:choose>
                    <c:when test="${notice.endDate < curDay}">
                    <span style="color:#cccccc">${notice.title}</span>
                    </c:when>
                    <c:when test="${notice.beginDate > curDay}">
                    <span style="color:#ffcccc">${notice.title}</span>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${!notice.readed}">
                            <b>
                        </c:if>
                        <span style="color:${notice.color}">${notice.title}</span>
                        <c:if test="${notice.isBold==1}">
                            </b>
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </a>
            <c:if test="${notice.fresh}">
            <img src="../images/icon_new.gif"/>
            </c:if>
        </td>
        <td>${notice.user.realName}
        </td>
        <td>
            <c:choose>
                <c:when test="${notice.isDeptNotice}">
                    部门通知
                </c:when>
                <c:otherwise>
                    公共通知
                </c:otherwise>
            </c:choose>
        </td>
        <td>
            ${notice.beginDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}
            <c:if test="${!empty notice.endDate}">
               &nbsp;~&nbsp; ${notice.endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}
            </c:if>
        </td>
        <td align="center">
            ${notice.createDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}
        </td>
    </tr>
    </c:forEach>
    </tbody>
</table>
<script>
    var flex;

    function changeSort(sortname, sortorder) {
        window.location.href = "list.do?${queryStr}&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
    }

    function changePage(newp) {
        if (newp)
            window.location.href = "list.do?${queryStr}&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
    }

    function rpChange(pageSize) {
        window.location.href = "list.do?${queryStr}&pageNum=${pageNum}&pageSize=" + pageSize;
    }

    function onReload() {
        window.location.reload();
    }

    $(document).ready(function () {
        flex = $("#grid").flexigrid
        (
            {
                buttons: [
                    <c:if test="${isNoticeMgr || isNoticeAll || isNoticeAdd}">
                    {name: '添加', bclass: 'add', onpress: action},
                    {name: '修改', bclass: 'edit', onpress: action},
                    {name: '删除', bclass: 'delete', onpress: action},
                    </c:if>
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
                <c:if test="${isNoticeMgr || isNoticeAll || isNoticeAdd}">
                checkbox: true,
                </c:if>
                page: ${pageNum},
                total: ${total},
                useRp: true,
                rp: ${pageSize},

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
                autoHeight: true,
                width: document.documentElement.clientWidth,
                height: document.documentElement.clientHeight - 84
            }
        );

        <c:if test="${op=='search'}">
        o("cond").value = "${cond}";
        </c:if>

        $('#fromDate').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d'
        });
        $('#toDate').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d'
        });
    });

    function action(com, grid) {
        if (com == '添加') {
            window.location.href = "add.do";
        } else if (com == '修改') {
            selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
            if (selectedCount == 0) {
                jAlert('请选择一条记录!', '提示');
                return;
            }
            if (selectedCount > 1) {
                jAlert('只能选择一条记录!', '提示');
                return;
            }

            var id = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).val();
            // window.location.href = "edit.do?id=" + id;
            addTab("通知修改", "notice/edit.do?id=" + id);
        } else if (com == '删除') {
            selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;

            if (selectedCount == 0) {
                jAlert('请选择记录!', '提示');
                return;
            }
            jConfirm("您确定要删除么？", "提示", function (r) {
                if (!r) {
                    return;
                } else {
                    var ids = "";
                    $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function (i) {
                        if (ids == "")
                            ids = $(this).val();
                        else
                            ids += "," + $(this).val();
                    });

                    $.ajax({
                        type: "post",
                        url: "del.do",
                        data: {
                            ids: ids
                        },
                        dataType: "html",
                        beforeSend: function(XMLHttpRequest){
                            $("body").showLoading();
                        },
                        success: function(data, status){
                            data = $.parseJSON(data);
                            jAlert(data.msg, "提示");
                            if (data.ret=="1") {
                                var ary = ids.split(",");
                                for (var i=0; i<ary.length; i++) {
                                    $('#' + ary[i]).remove();
                                }
                            }
                        },
                        complete: function(XMLHttpRequest, status){
                            $("body").hideLoading();
                        },
                        error: function(XMLHttpRequest, textStatus){
                            // 请求出错处理
                            alert(XMLHttpRequest.responseText);
                        }
                    });
                }
            })
        }
    }
</script>
</body>
</html>