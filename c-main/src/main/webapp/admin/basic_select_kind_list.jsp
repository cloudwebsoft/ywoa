<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>基础数据类型管理</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.css">
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
    <script src="../js/layui/layui.js" charset="utf-8"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
</head>
<body>
<%@ include file="basic_select_inc_menu_top.jsp" %>
<script>
    $("#menu3").addClass("current");
</script>
<div class="spacerH"></div>
<table class="tabStyle_1 percent60" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
        <td colspan="3" class="tabStyle_1_title">类型</td>
    </tr>
    <tr>
        <td colspan="3" align="center">
            <form id="form1" name="form1" method="post">
                序号
                <input name="orders" size="3"/>
                名称
                <input name="name" maxlength="20"/>
                &nbsp;
                <input class="btn btn-default" id="btnOk" type="button" value="添加"/>
                &nbsp;&nbsp;
            </form>
        </td>
    </tr>
    <%
        SelectKindDb wptd = new SelectKindDb();
        Iterator ir = wptd.list().iterator();
        while (ir.hasNext()) {
            wptd = (SelectKindDb) ir.next();
    %>
    <tr id="tr<%=wptd.getId()%>">
        <td width="7%" align="center">
            <%=wptd.getOrders()%>
        </td>
        <td width="70%"><a href="basic_select_list.jsp?kind=<%=wptd.getId()%>"><%=wptd.getName()%>
        </a></td>
        <td width="23%" align="center">
            <a href="basic_select_kind_edit.jsp?id=<%=wptd.getId()%>">编辑</a>
            &nbsp;&nbsp;
            <a href="javascript:;" onclick="del(<%=wptd.getId()%>)">删除</a>
            &nbsp;&nbsp;
            <a href="javascript:;" onclick="addTab('<%=wptd.getName()%>权限', '<%=request.getContextPath()%>/admin/basic_select_kind_priv_m.jsp?kindId=<%=wptd.getId()%>')">权限</a>
        </td>
    </tr>
    <%}%>
</table>
<script>
    $(function() {
        $('#btnOk').click(function(e) {
            e.preventDefault();
            add();
        })
    })

    function add() {
        $.ajax({
            type: "post",
            url: "../basicdata/createKind.do",
            data: $('#form1').serialize(),
            dataType: "json",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                if (data.res == 0) {
                    layer.alert(data.msg, {
                        yes: function() {
                            window.location.reload();
                        }
                    });
                } else {
                    layer.msg(data.msg);
                }
            },
            complete: function (XMLHttpRequest, status) {
                $('body').hideLoading();
            },
            error: function () {
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    function del(id) {
        layer.confirm('您确认要删除么?', {icon: 3, title: '提示'}, function (index) {
            $.ajax({
                type: "post",
                url: "../basicdata/delKind.do",
                data: {
                    id: id
                },
                dataType: "html",
                beforeSend: function (XMLHttpRequest) {
                    $('body').showLoading();
                },
                success: function (data, status) {
                    data = $.parseJSON(data);
                    layer.msg(data.msg, {
                        offset: '6px'
                    });
                    if (data.res == 0) {
                        $('#tr' + id).remove();
                    }
                },
                complete: function (XMLHttpRequest, status) {
                    $('body').hideLoading();
                },
                error: function (XMLHttpRequest, textStatus) {
                    alert(XMLHttpRequest.responseText);
                }
            });
        });
    }
</script>
</body>
</html>
