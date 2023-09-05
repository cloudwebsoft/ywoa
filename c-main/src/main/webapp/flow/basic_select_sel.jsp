<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>基础数据选择</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link href="../js/select2/select2.css" rel="stylesheet"/>
    <script src="../js/select2/select2.js"></script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<table class="tabStyle_1" align="center" width="100%" cellPadding="0" cellSpacing="0">
    <tbody>
    <tr>
        <td height="28" class="tabStyle_1_title" colspan="2">基础数据选择</td>
    </tr>
    <tr>
        <td height="42" align="center">
            基础数据
        </td>
        <td>
            <%
                String macroType = ParamUtil.get(request, "macroType");
                boolean isTree = "macro_basic_tree_select_ctl".equals(macroType);
                SelectMgr sm = new SelectMgr();
                java.util.Iterator ir = sm.getAllSelect().iterator();
                String opts = "";
                while (ir.hasNext()) {
                    SelectDb sd = (SelectDb) ir.next();
                    if (isTree) {
                        if (sd.getType() == SelectDb.TYPE_TREE) {
                            opts += "<option value='" + sd.getCode() + "'>" + sd.getName() + "</option>";
                        }
                    }
                    else {
                        opts += "<option value='" + sd.getCode() + "'>" + sd.getName() + "</option>";
                    }
                }
            %>
            <select id="sel" name="sel" style="width:200px">
                <%=opts%>
            </select>
        </td>
    </tr>
    <tr>
        <td height="42" align="center">
            可选层级
        </td>
        <td>
            <select id="layer" name="layer" title="设为1级时，只显示第1层级，设为末级时，只能选择末级">
                <option value="">不限</option>
                <option value="1">1级</option>
                <option value="0">末级</option>
            </select>
            （仅对树形基础数据有效）
        </td>
    </tr>
    <tr>
        <td align="center">
            请求参数
        </td>
        <td>
            <input id="requestParam" name="requestParam" title="request请求参数名称"/>(默认为字段名)
        </td>
    </tr>
    <tr>
        <td align="center" colspan="2">
            <input type="button" class="btn btn-default" value="确定" onClick="doSel()">
        </td>
    </tr>
    </tbody>
</table>
</body>
<script language="javascript">
    <!--
    $(function () {
        $('#sel').select2();

        var win = window.opener;
        var desc = win.document.getElementById('description').value;
        if (desc == "") {
            desc = win.document.getElementById('orgvalue').value;
            if (desc == "") {
                return;
            }
        }
        if (desc.indexOf('{') == 0) {
            var json = $.parseJSON(desc);
            $("#sel").select2("val", [json.code]);
            $("#layer").val(json.layer);
            $("#requestParam").val(json.requestParam);
        } else {
            $("#sel").select2("val", [desc]);
        }
    })

    function doSel() {
        var desc = {};
        desc.code = sel.options[sel.selectedIndex].value;
        desc.layer = $('#layer').val();
        desc.requestParam = $('#requestParam').val();
        window.opener.setSequence(JSON.stringify(desc), sel.options[sel.selectedIndex].text);
        window.close();
    }

    //-->
</script>
</html>