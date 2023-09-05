<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="cn.js.fan.web.Global" %>
<%@ page import="com.cloudweb.oa.utils.JarFileUtil" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>图像宏控件属性</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <style>
        .icon-input {
            width: 100px;
        }
        select[name="icon"] {
            width: 200px;
        }
        .img-flag {
            vertical-align: middle;
        }
    </style>
    <script src="../../js/jquery-1.9.1.min.js"></script>
    <script src="../../js/jquery-migrate-1.2.1.min.js"></script>
    <link href="../../js/select2/select2.css" rel="stylesheet"/>
    <script src="../../js/select2/select2.js"></script>
    <script src="../../inc/map.js"></script>
    <script src="../../js/jquery.toaster.js"></script>
    <script>
        $(function() {
            var win = window.opener;
            var desc = win.document.getElementById('description').value;
            if (desc=="") {
                return;
            }
            if (desc.indexOf('{')==0) {
                // console.log(desc);
                var json = $.parseJSON(desc);
                var options = json.options;
                for (var i=0; i<options.length; i++) {
                    var opt = options[i];
                    var $tr = addRow();
                    var rCount = $tr.data('rowCount');
                    $('#icon' + rCount).select2("val", [opt.icon]);
                    $tr.find("[name='value']").val(opt.value);
                    $tr.find("[name='name']").val(opt.name);
                    if (opt.selected) {
                        $tr.find("[name='selected']").attr("checked", true);
                    }
                }

                if (json.isOnlyIcon) {
                    $('#isOnlyIcon').attr('checked', true);
                }
            }
        })

        var rowCount = 0;
    </script>
</head>
<body>
<table id="template" width="100%" style="position: absolute; top:-1000px">
    <tr>
        <td width="8%" height="42" align="center">图标</td>
        <td width="23%" align="left">
            <%
                JarFileUtil jarFileUtil = SpringUtil.getBean(JarFileUtil.class);
                List<String> list = new ArrayList<>();
                jarFileUtil.loadFiles("static/images/symbol", "", list);
            %>
            <select id="icon" name="icon" class="js-example-templating js-states form-control">
                <option value="">无</option>
                <%
                    for (String fileName : list) {
                %>
                <option value="<%=fileName%>" style="background-image: url('<%=request.getContextPath()%>/static/images/symbol/<%=fileName%>');"><%=fileName %></option>
                <%
                    }
                %>
            </select>
        </td>
        <td width="8%" align="center">值</td>
        <td width="15%" align="left"><input class="icon-input" name="value"/></td>
        <td width="8%" align="center">名称</td>
        <td width="15%" align="left"><input class="icon-input" name="name"/></td>
        <td width="12%" align="left">
            <input name="selected" type="radio"/>
            选中
        </td>
        <td align="left">
            <a class="btn-del" href='javascript:' title='删除'>×</a>
            &nbsp;&nbsp;
            <a class="btn-move-up" href='javascript:' title='上移'>↑</a>
            &nbsp;&nbsp;
            <a class="btn-move-down" href='javascript:' title='下移'>↓</a>
        </td>
    </tr>
</table>
<table id="iconTable" width="100%" height="114" cellPadding="0" cellSpacing="0">
    <tbody>
    <tr>
        <td height="28" colspan="8" class="tabStyle_1_title">图标宏控件</td>
    </tr>
    <tr id="rowOperate">
        <td height="42" colspan="7" align="center">
            <input type="button" class="btn" value="增加" onclick="addRow()"/>
            &nbsp;&nbsp;
            <input type="button" class="btn" value="确定" onclick="ok()"/>
            &nbsp;&nbsp;
            <input type="button" class="btn" value="取消" onclick="window.close()"/>
        </td>
    </tr>
    </tbody>
</table>
<div style="margin-left: 20px">
    <input type="checkbox" id="isOnlyIcon" />
    只显示图标，不显示名称
</div>
</body>
<script>
    function addRow() {
        $('#iconTable tr:last').before($('#template tr:last').prop('outerHTML'));
        var $trs = $("#iconTable").find("tr");
        rowCount ++;
        var $tr = $($trs[$trs.length - 2]);
        var rowId = 'row' + rowCount;
        $tr.attr('id', rowId);
        $tr.find('#icon').attr('id', 'icon' + rowCount);
        $tr.find('.btn-del').click(function() {
            $('#' + rowId).remove();
        });
        $tr.find('.btn-move-up').click(function() {
            moveUp(rowId);
        });
        $tr.find('.btn-move-down').click(function() {
            moveDown(rowId);
        });

        $("#icon" + rowCount).select2({
            templateResult: formatStatePrompt,
            templateSelection: formatStatePrompt
        });

        $tr.data('rowCount', rowCount);
        return $tr;
    }

    function moveUp(trId) {
        if ($('#' + trId).prev().index() == 0) {
            $.toaster({priority: 'info', message: '已到顶部'});
            return;
        }
        $('#' + trId).prev().before($('#' + trId));
    }

    function moveDown(trId) {
        if ($('#' + trId).next().attr('id') == 'rowOperate') {
            $.toaster({priority: 'info', message: '已到底部'});
            return;
        }
        $('#' + trId).next().after($('#' + trId));
    }

    var mapPrompt = new Map();
    <%
    for (String fileName : list) {
    %>
    mapPrompt.put('<%=fileName%>', '<%=fileName%>');
    <%
    }
    %>

    function formatStatePrompt(state) {
        if (!state.id) { return state.text; }
        var name = state.text;
        var p = name.lastIndexOf("/");
        if (p!=-1) {
            name = name.substring(p+1);
        }
        var $state = $(
            '<span><img src="../../showImgInJar.do?path=' + mapPrompt.get(state.text).value + '" class="img-flag" /> ' + name + '</span>'
        );
        return $state;
    };

    function ok() {
        var $trs = $("#iconTable").find("tr");

        var options = [];
        var len = $trs.length;
        for (var i=1; i<=len - 2; i++) {
            $tr = $($trs[i]);
            var opt = {};
            opt.icon = $tr.find("[name='icon']").val();
            opt.name = $tr.find("[name='name']").val();
            opt.value = $tr.find("[name='value']").val();
            opt.selected = $tr.find("[name='selected']").prop('checked');
            options.push(opt);
        }

        var json = {};
        json.isOnlyIcon = $('#isOnlyIcon').prop("checked");
        json.options = options;
        window.opener.setSequence(JSON.stringify(json), "");
        window.close();
    }
</script>
</html>