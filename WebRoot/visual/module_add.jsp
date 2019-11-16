<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.visual.FormDAO" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String op = ParamUtil.get(request, "op");

    String code = ParamUtil.get(request, "code");
    if ("".equals(code)) {
        code = ParamUtil.get(request, "formCode");
    }

    ModuleSetupDb msd = new ModuleSetupDb();
    msd = msd.getModuleSetupDb(code);
    if (msd == null) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块不存在！"));
        return;
    }

    String formCode = msd.getString("form_code");
    if (formCode.equals("")) {
        out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    FormMgr fm = new FormMgr();
    FormDb fd = fm.getFormDb(formCode);

    ModulePrivDb mpd = new ModulePrivDb(code);
    if (!mpd.canUserAppend(privilege.getUser(request))) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String modUrlList = StrUtil.getNullStr(msd.getString("url_list"));
    if (modUrlList.equals("")) {
        String privurl = ParamUtil.get(request, "privurl");
        if (privurl.equals("")) {
            modUrlList = request.getContextPath() + "/" + "visual/module_list.jsp?code=" + code + "&formCode=" + StrUtil.UrlEncode(formCode);
        }
        else {
            modUrlList = privurl;
        }
    } else {
        modUrlList = request.getContextPath() + "/" + modUrlList;
    }

    request.setAttribute("modUrlList", modUrlList);

    // 置嵌套表需要用到的pageType
    request.setAttribute("pageType", "add");
    // 置NestSheetCtl需要用到的formCode
    request.setAttribute("formCode", formCode);

    if (fd == null || !fd.isLoaded()) {
        out.print(StrUtil.jAlert("表单不存在！", "提示"));
        return;
    }

    if (op.equals("saveformvalue")) {
        JSONObject json = new JSONObject();
        boolean re = false;
        String addToUrl = "";
        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
        try {
            re = fdm.create(application, request, msd);
            // 如果指定了添加跳转URL
            addToUrl = StrUtil.getNullStr(msd.getString("add_to_url"));
            if (!"".equals(addToUrl)) {
                addToUrl = ModuleUtil.parseUrl(request, addToUrl, fdm.getFormDAO());
                if (!addToUrl.startsWith("http")) {
                    addToUrl = request.getContextPath() + "/" + addToUrl;
                }
            }
        } catch (ErrMsgException e) {
            json.put("ret", "-1");
            json.put("msg", e.getMessage());
            out.print(json);
            // e.printStackTrace();
            return;
        }
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
            json.put("addToUrl", addToUrl);
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        out.print(json);
        return;
    }

    request.setAttribute("formCode", formCode);
    request.setAttribute("code", code);
    request.setAttribute("skinPath", SkinMgr.getSkinPath(request));
    request.setAttribute("privurl", StrUtil.UrlEncode(ParamUtil.get(request, "privurl")));
    request.setAttribute("nameTempCwsId", com.redmoon.oa.visual.FormDAO.NAME_TEMP_CWS_IDS);

    Map map = new HashMap();
    Enumeration reqParamNames = request.getParameterNames();
    while (reqParamNames.hasMoreElements()) {
        String paramName = (String) reqParamNames.nextElement();
        String[] paramValues = request.getParameterValues(paramName);
        if (paramValues.length == 1) {
            String paramValue = ParamUtil.getParam(request, paramName);
            // 过滤掉formCode等
            if (paramName.equals("code")
                    || paramName.equals("formCode")
                    || paramName.equals("moduleCode")
                    || paramName.equals("mainCode")
                    || paramName.equals("menuItem")
                    || paramName.equals("parentId")
                    || paramName.equals("moduleCodeRelated")
                    || paramName.equals("formCodeRelated")
                    || paramName.equals("mode") // 去掉mode及tagName，否则当存在mode=subTagRelated，关联模块中就会有问题
                    || paramName.equals("tagName")
                    || paramName.equals("id")
            ) {
                ;
            }
            else {
                map.put(paramName, paramValue);
            }
        }
    }
    request.setAttribute("map", map);
    request.setAttribute("isHasAttachment", fd.isHasAttachment());

    com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, fd);
    request.setAttribute("rend", rd.rendForAdd(msd));
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>添加内容</title>
    <link type="text/css" rel="stylesheet" href="${skinPath}/css.css"/>
    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css" />
    <link href="../flowstyle.css" rel="stylesheet" type="text/css"/>
    <style>
        input[readonly]{
            background-color: #ddd;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../inc/map.js"></script>
    <script src="../inc/livevalidation_standalone.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../js/jquery.raty.min.js"></script>
    <script src="../inc/flow_dispose_js.jsp"></script>
    <script src="../inc/flow_js.jsp"></script>
    <script src="../inc/ajax_getpage.jsp"></script>
    <script src="../inc/upload.js"></script>
    <script src="../js/jquery.bgiframe.js"></script>
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <link type="text/css" rel="stylesheet" href="${skinPath}/jquery-ui/jquery-ui-1.10.4.min.css"/>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <link href="../js/select2/select2.css" rel="stylesheet"/>
    <script src="../js/select2/select2.js"></script>
    <script src="../js/select2/i18n/zh-CN.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script src="../js/jquery.form.js"></script>
    <script type="text/javascript" src="../js/appendGrid/jquery.appendGrid-1.5.1.js"></script>
    <link type="text/css" rel="stylesheet" href="../js/appendGrid/jquery.appendGrid-1.5.1.css"/>
    <script src="../flow/form_js/form_js_${formCode}.jsp?pageType=add&code=${code}"></script>
</head>
<body>
<%@ include file="module_inc_menu_top.jsp" %>
<script>
    o("menu1").className = "current";
</script>
<div class="spacerH"></div>
<%@ include file="../inc/tip_phrase.jsp" %>
<script>
    function add() {
        try {
            ctlOnBeforeSerialize();
        } catch (e) {
        }

        var f_helper = new LiveValidation('cwsHelper');
        if (!LiveValidation.massValidate(f_helper.formObj.fields)) {
            if (LiveValidation.liveErrMsg.length < 100)
                jAlert(LiveValidation.liveErrMsg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
            else
                jAlert("请检查表单中的内容填写是否正常！","提示");
            return;
        }

        $('#btnAdd').attr("disabled", true);
        $('#visualForm').submit();
    }

    $(function () {
        SetNewDate();

        $('#btnAdd').click(function () {
            add();
        });
    });

    // ajaxForm序列化提交数据之前的回调函数
    function onBeforeSerialize() {
        try {
            ctlOnBeforeSerialize();
        } catch (e) {
        }
    }

    $(function () {
        var options = {
            beforeSerialize: onBeforeSerialize,
            //target:        '#output2',   // target element(s) to be updated with server response
            beforeSubmit: preSubmit,  // pre-submit callback
            success: showResponse  // post-submit callback

            // other available options:
            //url:       url         // override for form's 'action' attribute
            //type:      type        // 'get' or 'post', override for form's 'method' attribute
            //dataType:  null        // 'xml', 'script', or 'json' (expected server response type)
            //clearForm: true        // clear all form fields after successful submit
            //resetForm: true        // reset the form after successful submit

            // $.ajax options can be used here too, for example:
            //timeout:   3000
        };

        // bind to the form's submit event
        var lastSubmitTime = new Date().getTime();
        $('#visualForm').submit(function () {
            // 通过判断时间，禁多次重复提交
            var curSubmitTime = new Date().getTime();
            // 在0.5秒内的点击视为连续提交两次，实际当出现重复提交时，测试时间差为0
            if (curSubmitTime - lastSubmitTime < 500) {
                lastSubmitTime = curSubmitTime;
                $('#visualForm').hideLoading();
                return false;
            } else {
                lastSubmitTime = curSubmitTime;
            }

            $(this).ajaxSubmit(options);
            return false;
        });
    });

    function preSubmit() {
        $('#visualForm').showLoading();
    }

    function showResponse(responseText, statusText, xhr, $form) {
        $('#visualForm').hideLoading();
        var data = $.parseJSON($.trim(responseText));
        if (data.ret == "-1") {
            if (data.msg != null) {
                data.msg = data.msg.replace(/\\r/ig, "<BR>");
            }
            jAlert(data.msg, "提示");
            $('#btnAdd').attr("disabled", false);
        } else {
            var url = "${modUrlList}";
            if (data.addToUrl!="") {
                url = data.addToUrl;
            }

            if (url.indexOf("?")!=-1) {
                url += "&${reqParams}"; // reqParams在module_inc_menu_top.jsp中定义
            }
            else {
                url += "?${reqParams}";
            }

            jAlert_Redirect(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', url);
        }
    }
</script>
<form action="module_add.jsp?op=saveformvalue&code=${code}&formCode=${formCode}&privurl=${privurl}" method="post" enctype="multipart/form-data" name="visualForm" id="visualForm">
    <table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
            <td align="left">
                ${rend}
            </td>
        </tr>
        <c:if test="${isHasAttachment}">
        <tr>
            <td style="padding-top:3px">
                <script>initUpload()</script>
            </td>
        </tr>
        </c:if>
        <tr>
            <td height="30" align="center" style="padding-top: 10px">
                <input id="btnAdd" class="btn" type="button" value=" 确定 "/>
                &nbsp;&nbsp;
                <input class="btn" type="button" value=" 返回 " onclick="window.history.back()"/>
                <input id="cwsHelper" name="cwsHelper" value="1" type="hidden"/>
                <c:forEach items="${map}" var="mymap" >
                <input type='hidden' name='<c:out value="${mymap.key}" />' value='<c:out value="${mymap.value}" />'/>
                </c:forEach>
            </td>
        </tr>
    </table>
    <span id="spanTempCwsIds"></span>
</form>
<br/>
</body>
<script>
    // 记录添加的嵌套表格2记录的ID
    function addTempCwsId(formCode, cwsId) {
        var name = "${nameTempCwsId}_" + formCode;
        var inp;
        try {
            inp = document.createElement('<input type="hidden" name="' + name + '" />');
        } catch (e) {
            inp = document.createElement("input");
            inp.type = "hidden";
            inp.name = name;
        }
        inp.value = cwsId;

        spanTempCwsIds.appendChild(inp);
    }
</script>
</html>
