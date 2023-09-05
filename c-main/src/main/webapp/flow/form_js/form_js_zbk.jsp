<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="com.redmoon.oa.pvg.Privilege" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ page import="com.cloudweb.oa.api.IBasicSelectCtl" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="com.cloudweb.oa.utils.ResponseUtil" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page import="com.redmoon.oa.flow.FormParser" %>
<%@ page import="com.cloudweb.oa.service.MacroCtlService" %>
<%
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");

    String pageType = ParamUtil.get(request, "pageType");
    if (ConstUtil.PAGE_TYPE_LIST.equals(pageType)) {
        response.setContentType("text/javascript;charset=utf-8");
        return;
    }

    String op = ParamUtil.get(request, "op");
    if ("getOpts".equals(op)) {
        ResponseUtil responseUtil = SpringUtil.getBean(ResponseUtil.class);
        String moduleCode = ParamUtil.get(request, "moduleCode");
        String fieldName = ParamUtil.get(request, "fieldName");
        int itemIndex = ParamUtil.getInt(request, "itemIndex", 0);

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(moduleCode);
        if (!msd.isLoaded()) {
            out.print(responseUtil.getResJson(ConstUtil.RES_FAIL, "模块 " + moduleCode + " 不存在").toString());
            return;
        }
        FormDb fd = new FormDb();
        fd = fd.getFormDb(msd.getFormCode());
        FormField ff = fd.getFormField(fieldName);
        // 根据ff的类型
        if (ff.getType().equals(FormField.TYPE_SELECT)) {
            String opts = FormParser.getOptionsOfSelect(fd, ff);
            JSONObject json = responseUtil.getResJson(true);
            json.put("type", "select");
            json.put("result", opts);
            out.print(json.toString());
            return;
        }
        else if (ff.getType().equals(FormField.TYPE_MACRO)) {
            if ("macro_flow_select".equals(ff.getMacroType())) {
                MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                IBasicSelectCtl basicSelectCtl = macroCtlService.getBasicSelectCtl();
                String code = basicSelectCtl.getCode(ff);
                String result = basicSelectCtl.convertToHtmlCtl(request, "item" + itemIndex, code);
                JSONObject json = responseUtil.getResJson(true);
                json.put("type", "BasicSelectCtl");
                json.put("result", result);
                out.print(json.toString());
            }
            else {
                out.print(responseUtil.getResultJson(false, "表单域不是下拉框或基础数据宏控件"));
            }
        }
        return;
    }

    response.setContentType("text/javascript;charset=utf-8");
%>

$(function() {
     var oldValue = o("module").value;
     setInterval(function() {
         if (o("module")!="" && oldValue != o("module").value) {
            oldValue = o("module").value;
            // onChangeModule();
         }
     },500);

     var oldValueField1 = o("field1").value;
     setInterval(function() {
         if (o("field1")!="" && oldValueField1 != o("field1").value) {
            oldValueField1 = o("field1").value;
            onChangeField("field1");
         }
     },500);
     var oldValueField2 = o("field2").value;
     setInterval(function() {
         if (o("field2")!="" && oldValueField2 != o("field2").value) {
            oldValueField2 = o("field2").value;
            onChangeField("field2");
         }
     },500);
     var oldValueField3 = o("field3").value;
     setInterval(function() {
         if (o("field3")!="" && oldValueField3 != o("field3").value) {
            oldValueField3 = o("field3").value;
            onChangeField("field3");
         }
     },500);
     var oldValueField4 = o("field4").value;
     setInterval(function() {
         if (o("field4")!="" && oldValueField4 != o("field4").value) {
            oldValueField4 = o("field4").value;
            onChangeField("field4");
         }
     },500);
});

function onChangeModule() {
}

function onChangeField(fieldName) {
    if (o('module').value == '' || o(fieldName).value == '') {
        return;
    }
    var k = fieldName.substring(fieldName.length - 1);
    $.ajax({
        type: "post",
        url: "../flow/form_js/form_js_zbk.jsp?op=getOpts",
        contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
        data: {
            moduleCode: o('module').value,
            fieldName: o(fieldName).value,
            itemIndex: k
        },
        dataType: "html",
        beforeSend: function (XMLHttpRequest) {
            $('body').showLoading();
        },
        success: function (data, status) {
            data = $.parseJSON(data);
            if (data.res == 0) {
                // console.log(fieldName + k);
                if (data.type == 'select') {
                    $('[name=item' + k + ']').prop('outerHTML', '<select id="item' + k + '" name="item' + k + '">' + data.result + '</select>');
                }
                else {
                    $('[name=item' + k + ']').prop('outerHTML', data.result);
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