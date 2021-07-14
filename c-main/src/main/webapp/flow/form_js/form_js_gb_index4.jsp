<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.redmoon.oa.sms.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.redmoon.oa.basic.TreeSelectDb" %>
<%
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
    // Privilege pvg = new Privilege();
    // int flowId = ParamUtil.getInt(request, "flowId");
    String userName = ParamUtil.get(request, "userName");
    String rootPath = request.getContextPath();

    long parentId = ParamUtil.getLong(request, "parentId", -1);
    FormDb fdParent = new FormDb();
    fdParent = fdParent.getFormDb("gb_index3");
    com.redmoon.oa.visual.FormDAO fdaoP = new com.redmoon.oa.visual.FormDAO();
    fdaoP = fdaoP.getFormDAO(parentId, fdParent);
    String parentCode = fdaoP.getFieldValue("code");
    // 当前正处理的模块编码
    String thisModuleCode = ParamUtil.get(request, "code");
    if ("".equals(thisModuleCode)) {
        thisModuleCode = ParamUtil.get(request, "moduleCodeRelated");
    }

    String op = ParamUtil.get(request, "op");
    if ("getNodeCodesToDel".equals(op)) {
        response.setContentType("text/html;charset=utf-8");
        JSONObject json = new JSONObject();
        String ids = ParamUtil.get(request, "ids");
        String[] ary = StrUtil.split(ids, ",");
        if (ary == null) {
            json.put("ret", 0);
            json.put("msg", "请选择记录");
            out.print(json.toString());
            return;
        }
        String formCode = "gb_index4";
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        StringBuffer sb = new StringBuffer();
        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
        for (String strId : ary) {
            long id = StrUtil.toLong(strId, -1);
            fdao = fdao.getFormDAO(id, fd);
            StrUtil.concat(sb, ",", fdao.getFieldValue("code"));
        }
        json.put("ret", 1);
        json.put("nodeCodes", sb.toString());
        out.print(json.toString());
        return;
    }
    if ("getCode".equals(op)) {
        response.setContentType("text/html;charset=utf-8");
        JSONObject json = new JSONObject();
        long id = ParamUtil.getLong(request, "id", -1);
        if (id==-1) {
            json.put("ret", 0);
            json.put("msg", SkinUtil.LoadString(request, SkinUtil.ERR_ID));
            out.print(json.toString());
            return;
        }
        String formCode = "gb_index4";
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
        fdao = fdao.getFormDAO(id, fd);
        json.put("ret", 1);
        json.put("code", fdao.getFieldValue("code"));
        out.print(json.toString());
        return;
    }

    response.setContentType("text/javascript;charset=utf-8");
%>
function onModuleAdd<%=thisModuleCode%>(id, isTabStyleHor) {
    // console.log('id=' + id);
    var code = '';
    $.ajax({
        url: "<%=rootPath%>/flow/form_js/form_js_gb_index4.jsp",
        type: "post",
        async: false,
        dataType: "json",
        data: {
            op: 'getCode',
            id: id
        },
        success: function (data, status) {
            console.log(data);
            if (data.ret == 1) {
                code = data.code;
            } else {
                jAlert(data.msg, "提示");
            }
        },
        error: function (XMLHttpRequest, textStatus) {
            jAlert(XMLHttpRequest.responseText, "提示");
        }
    });
    // window.parent.document.getElementById("leftModuleFrame").contentWindow.location.reload();
    window.parent.document.getElementById("leftModuleFrame").contentWindow.addNewNode('<%=parentCode%>', code, o('name').value);
}

var nodeCodesToDel = '';
function onBeforeModuleDel<%=thisModuleCode%>(ids) {
    $.ajax({
        url: "<%=rootPath%>/flow/form_js/form_js_gb_index4.jsp",
        type: "post",
        async: false,
        dataType: "json",
        data: {
            op: 'getNodeCodesToDel',
            ids: ids
        },
        success: function (data, status) {
            if (data.ret == 1) {
                nodeCodesToDel = data.nodeCodes;
            } else {
                jAlert(data.msg, "提示");
            }
        },
        error: function (XMLHttpRequest, textStatus) {
            jAlert(XMLHttpRequest.responseText, "提示");
        }
    });
}

function onModuleDel<%=thisModuleCode%>(ids) {
    // window.parent.document.getElementById("leftModuleFrame").contentWindow.location.reload();
    console.log('nodeCodesToDel=' + nodeCodesToDel);
    window.parent.document.getElementById("leftModuleFrame").contentWindow.delNode(nodeCodesToDel);
}

function onModuleEdit<%=thisModuleCode%>(id, tabIdOpener) {
    var code = o("code").value;
    var name = o("name").value;
    if (tabIdOpener != '') {
        getTabWindow(tabIdOpener).document.getElementById("leftModuleFrame").contentWindow.modifyTitle(code, name);
    }
    else {
        window.parent.document.getElementById("leftModuleFrame").contentWindow.modifyTitle(code, name);
    }
}

