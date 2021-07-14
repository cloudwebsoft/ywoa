<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.message.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.idiofileark.*" %>
<%@ page import="com.alibaba.fastjson.JSONArray" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<jsp:useBean id="Msg" scope="page" class="com.redmoon.oa.message.MessageMgr"/>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserLogin(request)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    String name = privilege.getUser(request);

    String op = ParamUtil.get(request, "op");
    String action = ParamUtil.get(request, "action");
    String orderBy = ParamUtil.get(request, "orderBy");
    String sort = ParamUtil.get(request, "sort");
    if ("".equals(sort)) {
        sort = "desc";
    }
    String kind = ParamUtil.get(request, "kind");
    if (kind.equals(""))
        kind = "title";
    String actionType = ParamUtil.get(request, "actionType");
    String what = ParamUtil.get(request, "what");

    MessageDb md = new MessageDb();
    int pagesize = ParamUtil.getInt(request, "pagesize", 20);

    Paginator paginator = new Paginator(request);
    int curpage = paginator.getCurPage();

    int isRecycle = ParamUtil.getInt(request, "isRecycle", 0);
    String sql = md.getSqlOfSystem(name, isRecycle, action, what, kind, orderBy, sort, actionType);

    // out.println(sql);
    int total = md.getObjectCount(sql);

    paginator.init(total, pagesize);
    //设置当前页数和总页数
    int totalpages = paginator.getTotalPages();
    if (totalpages == 0) {
        curpage = 1;
        totalpages = 1;
    }

    int id, type;
    String title = "", sender = "", receiver = "", rq = "";
    boolean isreaded = true;
    int i = 0;
    Iterator ir = md.list(sql, (curpage - 1) * pagesize, curpage * pagesize - 1).iterator();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>消息中心</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <style>
        .un-readed {
            font-weight: bold;
        }

        .search-form input, select {
            vertical-align: middle;
        }

        .search-form input:not([type="radio"]):not([type="button"]) {
            width: 80px;
            line-height: 20px; /*否则输入框的文字会偏下*/
        }
    </style>
    <script src="../../inc/common.js"></script>
    <script src="../../js/jquery-1.9.1.min.js"></script>
    <script src="../../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>

    <link href="../../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script type="text/javascript" src="../../js/jquery.toaster.js"></script>

    <script type="text/javascript" src="../../js/flexigrid.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
    <script>
        function selAllCheckBox(checkboxname) {
            var checkboxboxs = document.getElementsByName(checkboxname);
            if (checkboxboxs != null) {
                // 如果只有一个元素
                if (checkboxboxs.length == null) {
                    checkboxboxs.checked = true;
                }
                for (i = 0; i < checkboxboxs.length; i++) {
                    checkboxboxs[i].checked = true;
                }
            }
        }

        function deSelAllCheckBox(checkboxname) {
            var checkboxboxs = document.getElementsByName(checkboxname);
            if (checkboxboxs != null) {
                if (checkboxboxs.length == null) {
                    checkboxboxs.checked = false;
                }
                for (i = 0; i < checkboxboxs.length; i++) {
                    checkboxboxs[i].checked = false;
                }
            }
        }
    </script>
    <style>
        .loading {
            display: none;
            position: fixed;
            z-index: 1801;
            top: 45%;
            left: 45%;
            width: 100%;
            margin: auto;
            height: 100%;
        }

        .SD_overlayBG2 {
            background: #FFFFFF;
            filter: alpha(opacity=20);
            -moz-opacity: 0.20;
            opacity: 0.20;
            z-index: 1500;
        }

        .treeBackground {
            display: none;
            position: absolute;
            top: -2%;
            left: 0%;
            width: 100%;
            margin: auto;
            height: 200%;
            background-color: #EEEEEE;
            z-index: 1800;
            -moz-opacity: 0.8;
            opacity: .80;
            filter: alpha(opacity=80);
        }
    </style>
</head>
<body>
<%@ include file="sys_inc_menu_top.jsp" %>
<script>
    <%
    if (isRecycle==0) {
    %>
    o("menu1").className = "current";
    <%}else{%>
    o("menu2").className = "current";
    <%}%>
</script>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../../images/loading.gif'></div>
<table id="searchTable" class="percent98" width="98%" border="0" cellpadding="0" cellspacing="0" align="center" style="margin-top:20px;">
    <tr>
        <td align="left">
            <form name="formSearch" action="sys_message.jsp" class="search-form" method="get">
                类型
                <select id="actionType" name="actionType">
                    <option value=""></option>
                    <%
                        JSONArray arr = MessageMgr.getActionTypes();
                        int len = arr.size();
                        for (int k = 0; k < len; k++) {
                    %>
                    <option value="<%=arr.getJSONObject(k).getString("type")%>"><%=arr.getJSONObject(k).getString("name")%>
                    </option>
                    <%
                        }
                    %>
                </select>
                按
                <select id="kind" name="kind">
                    <option value="title">标题</option>
                    <option value="content">内容</option>
                    <option value="notreaded">未读消息</option>
                </select>
                <script>
                    $(function () {
                        o("kind").value = "<%=kind%>";
                        o("actionType").value = "<%=actionType%>";
                    })
                </script>
                &nbsp;
                <input type="text" name=what size=20 value="<%=what%>"/>
                <input name="button" class="tSearch" type="submit" value="搜索"/>
                <input name="isRecycle" type="hidden" value="<%=isRecycle %>"/>
                <input name="action" value="search" type="hidden"/>
            </form>
        </td>
    </tr>
</table>
<div style="height:5px"></div>
<%
    String orderOp = "";
    if (sort.equals("") || sort.equals("desc")) {
        orderOp = "asc";
    } else {
        orderOp = "desc";
    }
    String imgSrc = "";
    if (orderOp.equals("desc")) {
        imgSrc = "../../netdisk/images/arrow_up.gif";
    } else {
        imgSrc = "../../netdisk/images/arrow_down.gif";
    }
%>
    <table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
        <thead>
        <tr>
            <th width="40" style="cursor:pointer"><input id="checkbox" name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')"/></th>
            <th width="500" style="cursor:pointer">标题</th>
            <th width="120" style="cursor:pointer" abbr="bySender">发送者</th>
            <th width="120" style="cursor:pointer" abbr="action_type">类型</th>
            <th width="150" style="cursor:pointer" abbr="byDate">日期</th>
        </tr>
        </thead>
        <%
            while (ir.hasNext()) {
                md = (MessageDb) ir.next();
                i++;
                id = md.getId();
                title = md.getTitle();
                sender = md.getSender();
                receiver = md.getReceiver();
                rq = md.getRq();
                type = md.getType();
                isreaded = md.isReaded();
                int msgLevel = md.getMsgLevel();
                int receipt = md.getReCeiptState();
        %>
        <tr id="tr<%=id%>">
            <td align="center" width="4%"><input type="checkbox" name="ids" value="<%=id%>"/></td>
            <td>
                <a id="title<%=id %>" class="<%=isreaded?"":"un-readed" %>" href="javascript:;" onclick="addTab('消息', '<%=request.getContextPath()%>/message_oa/message_ext/sys_showmsg.jsp?id=<%=id%>')" title="<%=title%>">
                    <%=title%>
                </a>
            </td>
            <td align="center">
                <%if (sender.equals(MessageDb.SENDER_SYSTEM)) {%>
                <%=sender%>
                <%} else {%>
                <a target="_blank" href="../../user_info.jsp?userName=<%=StrUtil.UrlEncode(md.getSender())%>"><%=md.getSenderRealName()%>
                </a>
                <%}%>
            </td>
            <td align="center">
                <%=MessageMgr.getActionName(md.getActionType())%>
            </td>
            <td align="center">
                <%=md.getSendTime()%>
            </td>
        </tr>
        <%}%>
    </table>
    <%
        String querystr = "action=" + action + "&op=" + op + "&kind=" + kind + "&what=" + StrUtil.UrlEncode(what) + "&orderBy=" + orderBy + "&sort=" + sort;
        String querystrWithoutSort = "action=" + action + "&op=" + op + "&kind=" + kind + "&what=" + StrUtil.UrlEncode(what);
    %>
</body>
<script>
    flex = $("#grid").flexigrid
    (
        {
            buttons: [
                <%if (isRecycle==0) {%>
                {name: '已读', bclass: 'readed', onpress: action},
                {name: '未读', bclass: 'unreaded', onpress: action},
                {name: '删除', bclass: 'delete', onpress: action},
                <%}else{%>
                {name: '删除', bclass: 'delete', onpress: action},
                {name: '恢复', bclass: 'resetCol', onpress: action},
                <%}%>
                {separator: true},
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
        });

    function action(com, grid) {
        if (com == '已读') {
            setReaded('ids');
        } else if (com == '未读') {
            setUnReaded('ids');
        } else if (com == "恢复") {
            var ids = getCheckboxValue('ids');
            if (ids == '') {
                jAlert('请选择消息！', '提示');
                return;
            }
            jConfirm('您确定要恢复么？', '提示', function (r) {
                if (!r) {
                    return;
                } else {
                    $.ajax({
                        type: "post",
                        url: "../../public/message/restore.do",
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
                            var ary = ids.split(",");
                            for (i = 0; i < ary.length; i++) {
                                $('#tr' + ary[i]).remove();
                            }
                            $.toaster({
                                "priority": "info",
                                "message": data.msg
                            });
                        },
                        complete: function (XMLHttpRequest, status) {
                            $('body').hideLoading();
                        },
                        error: function (XMLHttpRequest, textStatus) {
                            // 请求出错处理
                            jAlert(XMLHttpRequest.responseText, "提示");
                        }
                    });
                }
            });
        } else if (com == "删除") {
            if (getCheckboxValue('ids') == '') {
                jAlert('请选择消息！', '提示');
                return;
            }
            <%if (isRecycle==0) {%>
            jConfirm('您确定要删除么？', '提示', function (r) {
                if (!r) {
                    return;
                } else {
                    delToDustbin();
                }
            });
            <%}else{%>
            jConfirm('您确定要彻底删除么？', '提示', function (r) {
                if (!r) {
                    return;
                } else {
                    del();
                }
            });
            <%}%>
        }
    }

    function changeSort(sortname, sortorder) {
        window.location.href = "sys_message.jsp?<%=querystrWithoutSort%>&pagesize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
    }

    function changePage(newp) {
        if (newp)
            window.location.href = "sys_message.jsp?<%=querystr%>&CPages=" + newp + "&pagesize=" + flex.getOptions().rp;
    }

    function rpChange(pageSize) {
        window.location.href = "sys_message.jsp?<%=querystr%>&CPages=<%=curpage%>&pagesize=" + pageSize;
    }

    function onReload() {
        window.location.reload();
    }

    function delToDustbin() {
        var ids = getCheckboxValue("ids");
        if (ids == "") {
            jAlert('请选择记录!', '提示');
            return;
        }
        $.ajax({
            type: "post",
            url: "../../public/message/delToDustbin.do",
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
                var ary = ids.split(",");
                for (i = 0; i < ary.length; i++) {
                    $('#tr' + ary[i]).remove();
                }
                $.toaster({
                    "priority": "info",
                    "message": data.msg
                });
            },
            complete: function (XMLHttpRequest, status) {
                $('body').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText, "提示");
            }
        });
    }

    function del() {
        var ids = getCheckboxValue("ids");
        if (ids == "") {
            jAlert('请选择记录!', '提示');
            return;
        }
        $.ajax({
            type: "post",
            url: "../../public/message/del.do",
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
                var ary = ids.split(",");
                for (i = 0; i < ary.length; i++) {
                    $('#tr' + ary[i]).remove();
                }
                $.toaster({
                    "priority": "info",
                    "message": data.msg
                });
            },
            complete: function (XMLHttpRequest, status) {
                $('body').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText, "提示");
            }
        });
    }

    function setReaded() {
        var ids = getCheckboxValue("ids");
        if (ids == "") {
            jAlert('请选择记录!', '提示');
            return;
        }
        $.ajax({
            type: "post",
            url: "../../public/message/setReaded.do",
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
                var ary = ids.split(",");
                for (i = 0; i < ary.length; i++) {
                    $('#title' + ary[i]).removeClass('un-readed');
                }
                $.toaster({
                    "priority": "info",
                    "message": data.msg
                });
            },
            complete: function (XMLHttpRequest, status) {
                $('body').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText, "提示");
            }
        });
    }

    function setUnReaded() {
        var ids = getCheckboxValue("ids");
        if (ids == "") {
            jAlert('请选择记录!', '提示');
            return;
        }
        $.ajax({
            type: "post",
            url: "../../public/message/setUnReaded.do",
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
                var ary = ids.split(",");
                for (i = 0; i < ary.length; i++) {
                    $('#title' + ary[i]).addClass('un-readed');
                }
                $.toaster({
                    "priority": "info",
                    "message": data.msg
                });
            },
            complete: function (XMLHttpRequest, status) {
                $('body').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText, "提示");
            }
        });
    }

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
</script>
</html>