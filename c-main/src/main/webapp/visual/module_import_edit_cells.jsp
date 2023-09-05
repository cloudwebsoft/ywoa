<%@ page contentType="text/html;charset=utf-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="org.json.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String code = ParamUtil.get(request, "code");
    String formCode = ParamUtil.get(request, "formCode");
    long id = ParamUtil.getLong(request, "id", -1);

    boolean isAfterEdit = false;
    if (id == -1) {
        code = (String) request.getAttribute("code");
        formCode = (String) request.getAttribute("formCode");
        id = StrUtil.toLong((String) request.getAttribute("id"), -1);
        isAfterEdit = true;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>智能模块设计 - 导入设置（单个）</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link href="../js/select2/select2.css" rel="stylesheet" />
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
    <style>
        .setted {
            background-color: #00CC00;
        }

        .ui-dialog .ui-dialog-buttonpane button {
            width: 100px;
        }
        .ui-button-text-only .ui-button-text {
            padding:0;
        }
        .ui-dialog .ui-dialog-buttonpane button {
            margin-right: 30px;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../inc/livevalidation_standalone.js"></script>
    <script src="../js/jquery.toaster.js"></script>
    <script src="../js/select2/select2.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <%
        FormDb fd = new FormDb(formCode);
        if (!fd.isLoaded()) {
            out.print(StrUtil.jAlert_Back("该表单不存在！", "提示"));
            return;
        }

        if (id == -1) {
            out.print(StrUtil.jAlert_Back("标识非法！", "提示"));
            return;
        }

        if (isAfterEdit) {
            out.print(StrUtil.jAlert("操作成功！", "提示"));
        }
    %>
</head>
<body>
<%
    if (!privilege.isUserPrivValid(request, "admin.flow")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    StringBuffer opts = new StringBuffer();
    Iterator ir = fd.getFields().iterator();
    while (ir.hasNext()) {
        FormField ff = (FormField) ir.next();
        opts.append("<option value='" + ff.getName() + "'>" + ff.getTitle() + "</option>");
    }

    ModuleImportTemplateDb mid = new ModuleImportTemplateDb();
    mid = mid.getModuleImportTemplateDb(id);
    String name = mid.getString("name");
    String rules = mid.getString("rules");
    JSONArray arr = null;
    try {
        arr = new JSONArray(rules);
    } catch (JSONException e) {
        e.printStackTrace();
    }

    String cells = mid.getString("cells");
%>
<%@ include file="module_setup_inc_menu_top.jsp" %>
<script>
    o("menu6").className = "current";
    <%
    if (arr==null) {
        out.print(StrUtil.jAlert_Back("规则为空或非法！","提示"));
        return;
    }
    %>
</script>
<div class="spacerH"></div>
<form id="form1" method="post" action="module_import_edit.do">
    <div style="text-align:center; margin:10px auto">
        设置名称：<input id="name" name="name" value="<%=name%>"/>
    </div>

	<%=cells%>
    <table class="percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
        <tr>
            <td>
                <b>主表基础数据清洗</b></td>
        </tr>
    </table>
    <%
        String strJson = StrUtil.getNullStr(mid.getString("cleans"));
        JSONArray ary = null;
        if (!"".equals(strJson)) {
            ary = new JSONArray(strJson);
        }
        SelectMgr sm = new SelectMgr();
        MacroCtlMgr mm = new MacroCtlMgr();
        ir = fd.getFields().iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                if (mu != null && mu.getCode().equals("macro_flow_select")) {
                    SelectDb sd = sm.getSelect(ff.getDefaultValueRaw());
                    boolean isClean = false;
                    JSONObject json = null;
                    if (ary != null) {
                        for (int i = 0; i < ary.length(); i++) {
                            json = ary.getJSONObject(i);
                            if (ff.getName().equals(json.get("fieldName"))) {
                                isClean = true;
                                break;
                            }
                        }
                    }
    %>
    <table class="percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
        <tr>
            <td>
                <input type="checkbox" name="is_clean_<%=ff.getName()%>"
                       value="1" <%=isClean ? "checked" : "" %> /><%=ff.getTitle()%>
                （勾选后才能清洗数据）
            </td>
        </tr>
    </table>
    <table class="tabStyle_1 percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
        <tr>
            <td width="32%" class="tabStyle_1_title">名称</td>
            <td width="30%" class="tabStyle_1_title">值</td>
            <td width="38%" class="tabStyle_1_title">对应的名称</td>
        </tr>
        <%
            Vector v = sd.getOptions(new JdbcTemplate());
            Iterator irBasic = v.iterator();
            while (irBasic.hasNext()) {
                SelectOptionDb sod = (SelectOptionDb) irBasic.next();
                if (!sod.isOpen())
                    continue;
                String val = sod.getValue();
        %>
        <tr>
            <td><%=sod.getName()%>
            </td>
            <td><%=sod.getValue()%>
            </td>
            <td>
                <%
                    String otherVal = "";
                    if (isClean && json != null) {
                        Iterator keys = json.keys();
                        while (keys.hasNext()) {
                            String key = (String) keys.next();
                            String toVal = json.getString(key);
                            if (val.equals(toVal)) {
                                otherVal = key;
                                break;
                            }
                        }
                    }
                %>
                <input name="<%=ff.getName()%>_<%=StrUtil.escape(sod.getValue())%>" value="<%=otherVal%>"
                       onfocus="this.select()"/>
            </td>
        </tr>
        <%
            }
        %>
    </table>
    <%
                }
            }
        }
    %>
    <input id="code" name="code" type="hidden" value="<%=code %>"/>
    <input id="formCode" name="formCode" type="hidden" value="<%=formCode %>"/>
    <input id="id" name="id" type="hidden" value="<%=id%>"/>
    <input id="rules" name="rules" type="hidden"/>
    <input id="cells" name="cells" type="hidden"/>
    <input id="xlsTmpPath" name="xlsTmpPath" type="hidden" value="${xlsTmpPath}" />
    <div style="text-align:center; margin-top: 10px">
        <input class="btn btn-default" type="button" value="编辑表格" onclick="openWin('module_import_edit_cells_ueditor.jsp', 800, 600)"/>
        &nbsp;&nbsp;
        <input class="btn btn-default" type="button" value="确定" onclick="submitForm()"/>
        &nbsp;&nbsp;
        <input class="btn btn-default" type="button" value="返回" onclick="window.history.back()"/>
    </div>
</form>
<div id="dlg" style="display: none">
    表单域：
    <select id="field" name="field" style="width:200px">
        <option value=""></option>
        <%=opts%>
    </select>
</div>
<br/>
</body>
<script language="javascript">
    function getCells() {
        return $('#mainTable').prop('outerHTML');
    }

    function setCells(cells) {
        $('#mainTable').prop('outerHTML', cells);
    }

    var templName = new LiveValidation('name');
    templName.add(Validate.Presence);
    templName.add(Validate.Length, {minimum: 1, maximum: 45});

    var lv_formCode = new LiveValidation('formCode');

    function submitForm() {
        if (!LiveValidation.massValidate(lv_formCode.formObj.fields)) {
            jAlert("请检查表单中的内容填写是否正常！", "提示");
            return;
        }

        var jsonArr = [];
        $('#mainTable tr').each(function (i) {
            $(this).children('td').each(function (j) {
                var field = $(this).attr('field');
                if (field != null && field != '') {
                    var json = {};
                    json.r = i;
                    json.c = j;
                    json.field = field;
                    jsonArr.push(json);
                }
            });
        });
        var jsonStr = JSON.stringify(jsonArr);
        $('#rules').val(jsonStr);
        $('#cells').val($('#mainTable').prop('outerHTML'));

        $.ajax({
            type: "post",
            url: "modifyModuleImportCells.do",
            // contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
            data: $("#form1").serialize(),
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                jAlert(data.msg, "提示");
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


    $(function() {
        $('#field').select2();
        // 解决在dialog中无法输入问题
        $('#field').removeAttr('tabindex');

        $('.cell').dblclick(function() {
            var row = $(this).attr('row');
            var col = $(this).attr('col');
            var field = $(this).attr('field');
            if (field!=null) {
                $('#field').select2('val', [field]);
            }
            var self = this;
            jQuery("#dlg").dialog({
                title: "设置",
                modal: true,
                // bgiframe:true,
                buttons: {
                    "取消": function() {
                        jQuery(this).dialog("close");
                    },
                    "恢复": function() {
                        $(self).html($(self).attr('val'));
                        $(self).removeClass('setted');
                        $(self).removeAttr('field');
                        jQuery(this).dialog("close");
                    },
                    "确定": function() {
                        var field = $('#field').val();
                        if (field == '') {
                            $.toaster({
                                "priority" : "info",
                                "message" : "请选择表单域"
                            });
                            return;
                        }

                        var text = $("#field option:selected").text();
                        $(self).html(text);
                        $(self).addClass('setted');
                        $(self).attr('field', field);
                        jQuery(this).dialog("close");
                    }
                },
                closeOnEscape: true,
                draggable: true,
                resizable:true,
                width:300,
                height:100
            });
        });
    });
</script>
</html>