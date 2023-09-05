<%@ page contentType="text/html;charset=utf-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.cloudweb.oa.service.MacroCtlService" %>
<%@ page import="com.cloudweb.oa.api.IBasicSelectCtl" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String op = ParamUtil.get(request, "op");
    String code = (String) request.getAttribute("code");
    String formCode = (String) request.getAttribute("formCode");
    FormDb fd = new FormDb(formCode);
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>智能模块设计 - 导入设置</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css" />
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
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
    <script src="../inc/livevalidation_standalone.js"></script>
    <script src="../js/jquery.toaster.js"></script>
    <script src="../js/select2/select2.js"></script>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <%
        if (!fd.isLoaded()) {
            out.print(StrUtil.jAlert_Back("该表单不存在！", "提示"));
            return;
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
%>
<%@ include file="../../../visual/module_setup_inc_menu_top.jsp"%>
<script>
    o("menu6").className = "current";
</script>
<div class="spacerH"></div>
<form id="form1" method="post" action="setModuleImportCols.do">
    <div style="text-align:center; margin:10px auto">
        设置名称：<input id="name" name="name"/>
    </div>
    <table id="mainTable" class="tabStyle_1 percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
        <c:forEach var="items" items="${cells}" varStatus="status">
            <tr>
                <c:forEach var="item" items="${items}" varStatus="stats">
                <td class="cell" row="${status.index}" col="${stats.index}" val="${item}">
                    <c:out value="${item}" />
                </td>
                </c:forEach>
            </tr>
        </c:forEach>
    </table>
    <table class="percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
        <tr>
            <td>
                <b>主表基础数据清洗</b></td>
        </tr>
    </table>
    <%
        SelectMgr sm = new SelectMgr();
        MacroCtlMgr mm = new MacroCtlMgr();
        MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
        IBasicSelectCtl basicSelectCtl = macroCtlService.getBasicSelectCtl();
        ir = fd.getFields().iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField)ir.next();
            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                if (mu!=null && "macro_flow_select".equals(mu.getCode())) {
                    String basicCode = basicSelectCtl.getCode(ff);
                    SelectDb sd = sm.getSelect(basicCode);
                    if (sd.getType() == SelectDb.TYPE_TREE) {
                        continue;
                    }
    %>
    <table class="percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
        <tr>
            <td>
                <input type="checkbox" name="is_clean_<%=ff.getName()%>" value="1" /><%=ff.getTitle()%>
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
            Vector<SelectOptionDb> v = sd.getOptions(new JdbcTemplate());
            for (SelectOptionDb sod : v) {
                if (!sod.isOpen()) {
                    continue;
                }
        %>
        <tr>
            <td><%=sod.getName()%>
            </td>
            <td><%=sod.getValue()%>
            </td>
            <td><input name="<%=ff.getName()%>_<%=StrUtil.escape(sod.getValue())%>" value="<%=sod.getName()%>"
                       onfocus="this.select()"/></td>
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
    <div style="text-align:center; margin-top: 10px">
        <input type="button" value="确定" class="btn btn-default" onclick="submitForm()"/>
    </div>
    <input id="code" name="code" type="hidden" value="${code}"/>
    <input id="formCode" name="formCode" type="hidden" value="${formCode}"/>
    <input id="rules" name="rules" type="hidden"/>
    <input id="cells" name="cells" type="hidden"/>
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
                    // console.log("第" + (i + 1) + "行，第" + (j + 1) + "个td的值：" + $(this).text());
                    var json = {};
                    json.r = i;
                    json.c = j;
                    json.field = field;
                    jsonArr.push(json);
                }
            });
        });
        var jsonStr =JSON.stringify(jsonArr);
        $('#rules').val(jsonStr);
        $('#cells').val($('#mainTable').prop('outerHTML'));

        $.ajax({
            type: "post",
            url: "setModuleImportCells.do",
            // contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
            data: $("#form1").serialize(),
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                // $('#container').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == "1") {
                    jAlert_Redirect("操作成功！", "提示", "../visual/module_import_list.do?code=" + $('#code').val() + "&formCode=" + $('#formCode').val());
                } else {
                    jAlert(data.msg, "提示");
                }
            },
            complete: function (XMLHttpRequest, status) {
                // $('#container').hideLoading();
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
                bgiframe:true,
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

            /*$('.ui-button').width(60);
            $('.ui-button').css('padding', '5px');*/
        });
    });

    $(function() {
        $('input, select, textarea').each(function() {
            if (!$('body').hasClass('form-inline')) {
                $('body').addClass('form-inline');
            }
            // ffb-input 为flexbox的样式
            if (!$(this).hasClass('ueditor') && !$(this).hasClass('btnSearch') && !$(this).hasClass('tSearch') &&
                $(this).attr('type') != 'hidden' && $(this).attr('type') != 'file' && !$(this).hasClass('ffb-input')) {
                $(this).addClass('form-control');
                $(this).attr('autocomplete', 'off');
            }
        });
    })
</script>
</html>