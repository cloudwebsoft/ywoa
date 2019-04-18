<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
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
// formCode = "contract";
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
        if (privurl.equals(""))
            modUrlList = request.getContextPath() + "/" + "visual/module_list.jsp?code=" + code + "&formCode=" + StrUtil.UrlEncode(formCode);
        else
            modUrlList = privurl;
    } else {
        modUrlList = request.getContextPath() + "/" + modUrlList;
    }

// 置嵌套表需要用到的pageType
    request.setAttribute("pageType", "add");
// 置NestSheetCtl需要用到的formCode
    request.setAttribute("formCode", formCode);

    if (fd == null || !fd.isLoaded()) {
        out.println(StrUtil.jAlert("表单不存在！", "提示"));
        return;
    }

    if (op.equals("saveformvalue")) {
        JSONObject json = new JSONObject();
        boolean re = false;
        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
        try {
            re = fdm.create(application, request, msd);
        } catch (ErrMsgException e) {
            json.put("ret", "-1");
            json.put("msg", e.getMessage());
            out.print(json);
            e.printStackTrace();
            return;
        }
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        out.print(json);
        return;
    }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>智能模块设计-添加内容</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css" />
    <link href="../flowstyle.css" rel="stylesheet" type="text/css"/>
    <script src="../inc/common.js"></script>
    <script src="../inc/map.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../js/jquery.raty.min.js"></script>
    <script src="../inc/flow_dispose_js.jsp"></script>
    <script src="../inc/flow_js.jsp"></script>
    <script src="<%=request.getContextPath()%>/inc/ajax_getpage.jsp"></script>
    <script src="../inc/livevalidation_standalone.js"></script>
    <script src="../inc/upload.js"></script>
    <script src="../js/jquery.bgiframe.js"></script>
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.min.css"/>
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
    <script src="<%=request.getContextPath()%>/flow/form_js/form_js_<%=formCode%>.jsp?pageType=add&code=<%=code%>"></script>
    <script>
        function setradio(myitem, v) {
            var radioboxs = document.all.item(myitem);
            if (radioboxs != null) {
                for (i = 0; i < radioboxs.length; i++) {
                    if (radioboxs[i].type == "radio") {
                        if (radioboxs[i].value == v)
                            radioboxs[i].checked = true;
                    }
                }
            }
        }

        // 控件完成上传后，调用Operate()
        function Operate() {
            // alert(redmoonoffice.ReturnMessage);
        }
    </script>
</head>
<body>
<%@ include file="module_inc_menu_top.jsp" %>
<script>
    o("menu1").className = "current";
</script>
<div class="spacerH"></div>
<%@ include file="../inc/tip_phrase.jsp" %>
<form action="module_add.jsp?op=saveformvalue&code=<%=code%>&formCode=<%=StrUtil.UrlEncode(formCode)%>&privurl=<%=StrUtil.UrlEncode(ParamUtil.get(request, "privurl"))%>" method="post" enctype="multipart/form-data" name="visualForm" id="visualForm">
    <table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
            <td align="left">
                <%
                    com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, fd);
                    out.print(rd.rendForAdd(msd));
                %>
            </td>
        </tr>
        <%if (fd.isHasAttachment()) {%>
        <tr>
            <td style="padding-top:3px">
                <script>initUpload()</script>
            </td>
        </tr>
        <%}%>
        <tr>
            <td height="30" align="center" style="padding-top: 10px">
                <input id="btnAdd" class="btn" type="button" value=" 确定 "/>
                &nbsp;&nbsp;
                <input class="btn" type="button" value=" 返回 " onclick="window.history.back()"/>
                <input id="helper" value="1" type="hidden"/>
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
        var name = "<%=com.redmoon.oa.visual.FormDAO.NAME_TEMP_CWS_IDS%>_" + formCode;
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

    $(function () {
        SetNewDate();
        var f_helper = new LiveValidation('helper');

        $('#btnAdd').click(function () {
            try {
                ctlOnBeforeSerialize();
            } catch (e) {
            }

            if (!LiveValidation.massValidate(f_helper.formObj.fields)) {
                jAlert("请检查表单中的内容填写是否正常！", "提示");
                return;
            }
            $('#btnAdd').attr("disabled", true);
            $('#visualForm').submit();

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
            jAlert(data.msg, "提示");
            $('#btnAdd').attr("disabled", false);
        } else {
            jAlert_Redirect(data.msg, "提示", "<%=modUrlList%>");
        }
    }
</script>
</html>
