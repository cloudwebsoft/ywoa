<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="org.json.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.oacalendar.*"%>
<%@ page import="com.redmoon.oa.visual.FormUtil"%>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb"%>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="com.cloudweb.oa.service.IUserService" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudweb.oa.cache.UserCache" %>
<%@ page import="com.cloudweb.oa.entity.User" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>流程处理</title>
    <meta name="renderer" content="ie-stand"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="js/layui/css/layui.css" media="all">
    <style>
        .main-content {
            margin: 5px 30px;
        }

        .spanNextUser {
            font-size: 12px;
        }

        .checkerUser {
            width: 120px;
            display: block;
            float: left;
        }

        input,textarea {
            outline:none;
        }

        input[readonly] {
            background-color: #ddd;
        }

        select[readonly] {
            background-color: #ddd;
        }

        textarea[readonly] {
            background-color: #ddd;
        }
    </style>
    <%@ include file="inc/nocache.jsp" %>
    <script src="inc/common.js"></script>
    <script src="js/jquery-1.9.1.min.js"></script>
    <script src="js/jquery-migrate-1.2.1.min.js"></script>

    <link rel="stylesheet" href="js/poshytip/tip-yellowsimple/tip-yellowsimple.css" type="text/css" />
    <script type="text/javascript" src="js/poshytip/jquery.poshytip.js"></script>

    <script src="inc/livevalidation_standalone.js"></script>
    <script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link href="js/select2/select2.css" rel="stylesheet"/>
    <script src="js/select2/select2.js"></script>
    <script src="js/select2/i18n/zh-CN.js"></script>
    <link rel="stylesheet" type="text/css" href="js/MyPaging/MyPaging.css">
    <script src="js/MyPaging/MyPaging.js"></script>
    <link rel="stylesheet" href="js/bootstrap/css/bootstrap.min.css"/>
    <script src="js/BootstrapMenu.min.js"></script>
    <script src="js/layui/layui.js" charset="utf-8"></script>

    <jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
    <%
        String priv = "read";
        if (!privilege.isUserPrivValid(request, priv)) {
            out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }
        com.redmoon.clouddisk.Config cfgNd = com.redmoon.clouddisk.Config.getInstance();
        boolean isUsed = cfgNd.getBooleanProperty("isUsed"); //判断网盘是否启用

        String myname = privilege.getUser(request);
        long myActionId = ParamUtil.getLong(request, "myActionId");
        if (myActionId == -1) {
            String prompt = LocalUtil.LoadString(request, "res.flow.Flow", "prompt");
            String str = LocalUtil.LoadString(request, "res.flow.Flow", "errorFlow");
            out.print(SkinUtil.makeErrMsg(request, str));
            return;
        }

        MyActionDb mad = new MyActionDb();
        mad = mad.getMyActionDb((long) myActionId);
        if (!mad.isLoaded()) {
            out.print(SkinUtil.makeErrMsg(request, LocalUtil.LoadString(request, "res.flow.Flow", "myActionNotExist")));
            return;
        }

        // 如果存在子流程，则处理子流程
        if (mad.getSubMyActionId() != MyActionDb.SUB_MYACTION_ID_NONE) {
            response.sendRedirect("flow_dispose.jsp?myActionId=" + mad.getSubMyActionId());
            return;
        }

        UserMgr um = new UserMgr();
        UserDb myUser = um.getUserDb(myname);

        String myRealName = myUser.getRealName();

        int flowId = (int) mad.getFlowId();
        WorkflowMgr wfm = new WorkflowMgr();
        WorkflowDb wf = wfm.getWorkflowDb(flowId);

        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());

        boolean isFlowManager = false;

        LeafPriv lp = new LeafPriv(wf.getTypeCode());
        if (privilege.isUserPrivValid(request, "admin.flow")) {
            if (lp.canUserExamine(privilege.getUser(request))) {
                isFlowManager = true;
            }
        }

        WorkflowPredefineDb wfp = new WorkflowPredefineDb();
        wfp = wfp.getPredefineFlowOfFree(wf.getTypeCode());
        boolean isRecall = wfp.isRecall(); // 是否能撤回

        WorkflowActionDb wa = new WorkflowActionDb();
        int actionId = (int) mad.getActionId();
        wa = wa.getWorkflowActionDb(actionId);

        // 判断能否提交
        try {
            WorkflowMgr.canSubmit(request, wf, wa, mad, myname, wfp);
        } catch (ErrMsgException e) {
            out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
            return;
        }

        // 锁定流程
        if (wa.getKind() != WorkflowActionDb.KIND_READ) {
            wfm.lock(wf, myname);
        }

        // 如果是未读状态
        if (!mad.isReaded()) {
            mad.setReaded(true);
            mad.setReadDate(new java.util.Date());
            mad.save();
        }

        String flag = wa.getFlag();

        String op = ParamUtil.get(request, "op");
        Leaf lf = new Leaf();
        lf = lf.getLeaf(wf.getTypeCode());

        // 置嵌套表需要用到的cwsId
        request.setAttribute("cwsId", "" + flowId);
        // 置嵌套表需要用到的pageType
        request.setAttribute("pageType", ConstUtil.PAGE_TYPE_FLOW);
        // 置NestFromCtl及NestSheetCtl需要用到的workflowActionId
        request.setAttribute("workflowActionId", "" + wa.getId());
        // 置macro_js_ntko.jsp中需要用到的myActionId
        request.setAttribute("myActionId", "" + myActionId);
        // 置NestSheetCtl需要用到的formCode
        request.setAttribute("formCode", lf.getFormCode());

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        String canUserSeeDesignerWhenDispose = cfg.get("canUserSeeDesignerWhenDispose");
        String canUserModifyFlow = cfg.get("canUserModifyFlow");
        boolean canUserSeeFlowChart = cfg.getBooleanProperty("canUserSeeFlowChart");

        String flowExpireUnit = cfg.get("flowExpireUnit");
        boolean isHour = !flowExpireUnit.equals("day");
        if (flowExpireUnit.equals("day")) {
            String str = LocalUtil.LoadString(request, "res.flow.Flow", "day");
            flowExpireUnit = str;
        } else {
            String str = LocalUtil.LoadString(request, "res.flow.Flow", "hour");
            flowExpireUnit = str;
        }

        if (op.equals("discard")) {
            String prompt = LocalUtil.LoadString(request, "res.flow.Flow", "prompt");
            boolean re = false;
            try {
                re = wfm.discard(request, myname, flowId);
            } catch (ErrMsgException e) {
                out.print(StrUtil.jAlert(e.getMessage(), prompt));
            }
            if (re) {
                String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
                out.print(StrUtil.jAlert_Redirect(str, prompt, "flow/flow_list.jsp?displayMode=1"));
                return;
            }
        }

        String action = ParamUtil.get(request, "action");

        WorkflowRuler wr = new WorkflowRuler();
        com.redmoon.oa.flow.FlowConfig conf = new com.redmoon.oa.flow.FlowConfig();             //用于判断流程toolbar按钮是否显示

        FormDb fd = new FormDb();
        fd = fd.getFormDb(lf.getFormCode());
    %>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css"/>
    <script src="js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <link rel="stylesheet" type="text/css" href="js/datepicker/jquery.datetimepicker.css"/>
    <script src="js/datepicker/jquery.datetimepicker.js"></script>
    <script src="js/jquery.bgiframe.js"></script>
    <script src="js/fixheadertable/jquery.fixheadertable.js"></script>
    <link rel="stylesheet" media="screen" href="js/fixheadertable/base.css"/>
    <script src="js/jquery.form.js"></script>
    <link href="js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="js/jquery-showLoading/jquery.showLoading.js"></script>
    <script type="text/javascript" src="js/activebar2.js"></script>
    <script src="inc/flow_dispose.jsp"></script>
    <script src="inc/flow_js.jsp"></script>
    <script src="inc/upload.js"></script>
    <script src="inc/map.js"></script>
    <script src="inc/ajax_getpage.jsp"></script>
    <script src="flow/form_js/form_js_<%=lf.getFormCode()%>.jsp?pageType=<%=ConstUtil.PAGE_TYPE_FLOW%>&flowId=<%=flowId%>&myActionId=<%=myActionId%>&userName=<%=StrUtil.UrlEncode(privilege.getUser(request))%>&time=<%=Math.random()%>"></script>
    <script src="js/tabpanel/Toolbar.js" type="text/javascript"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar_flow.css"/>
    <script type="text/javascript" src="js/goToTop/goToTop.js"></script>
    <link type="text/css" rel="stylesheet" href="js/goToTop/goToTop.css"/>
    <script type="text/javascript" src="js/jquery.toaster.flow.js"></script>
    <script type="text/javascript" src="js/appendGrid/jquery.appendGrid-1.5.1.js"></script>
    <link type="text/css" rel="stylesheet" href="js/appendGrid/jquery.appendGrid-1.5.1.css"/>
    <%
        String userName1 = privilege.getUser(request);
        UserSetupDb userSetupDb = new UserSetupDb();
        userSetupDb = userSetupDb.getUserSetupDb(userName1);
        String str = userSetupDb.getLocal();
        if (str.equals("en-US")) {
    %>
    <script id='uploadJs' src="inc/upload.js" local="en"></script>
    <%} %>
    <link type="text/css" rel="stylesheet" href="skin/common/macro_detaillist_ctl.css"/>
    <link href="flowstyle.css" rel="stylesheet" type="text/css"/>
    <link href="js/simplesidebar/simplesidebar.css" rel="stylesheet" type="text/css"/>
    <script type="text/javascript" src="js/simplesidebar/jquery.simplesidebar.js"></script>
    <script language="javascript">
        <!--
        window.document.onkeydown = function () {
            if (event.keyCode == 13 && event.srcElement.type != 'button' && event.srcElement.type != 'submit' && event.srcElement.type != 'reset' && event.srcElement.type != 'textarea' && event.srcElement.type != '')
                event.keyCode = 9;
        }
        -->
    </script>
    <script language="javascript">
        // 设置选项卡标题
        $(document).ready(function () {
            setActiveTabTitle("<%=wf.getTitle().replaceAll("\r\n", "").trim().length() >= 8 ? wf.getTitle().replaceAll("\r\n", "").trim().substring(0, 8) : wf.getTitle().replaceAll("\r\n", "").trim()%>");
            // 初始化datetimepicker
            SetNewDate();
        });

        var toolbar;

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

        var isXorRadiate = false;
        var isCondSatisfied = true;
        var hasCond = false;

        var action = "<%=action%>";

        function SubmitResult(isAfterSaveformvalueBeforeXorCondSelect) {
            hideDesigner();

            if (hasCond && !isAfterSaveformvalueBeforeXorCondSelect) {
                // 先ajax保存表单，然后再ajax弹出对话框选择用户，然后才交办
                flowForm.op.value = "saveformvalueBeforeXorCondSelect";

                if (o('flowForm').onsubmit) {
                    if (o('flowForm').onsubmit()) {
                        $('#flowForm').submit();
                    } else {
                        toolbar.setDisabled(1, false);
                        $('#bodyBox').hideLoading();
                    }
                } else
                    $('#flowForm').submit();

                return;
            }

            // 如果本节点是异或节点，且是条件鉴别节点，如果未满足条件，则在此提醒用户先保存结果，然后继续往下进行
            if (isXorRadiate && (hasCond && !isCondSatisfied)) {
                // 2011-11-13 改为当不满足条件时，可排除掉不满足的分支，如果有条件为空的分支（且空的分支线大于2条，如果只有一条则表示为默认分支线），则使用户可以选择分支线
                // 因而此处不再提醒，如果一个条件也不满足，则在setXorRadiateNextBranch中会检测后提示，请选择后继节点
                // alert("当前处理不满足往下进行的条件（具体条件请见流程图），请先点击保存，待满足条件后继续！");
                // return;
            }
            // 如果是自动存档节点，则先保存表单，然后回到此页面，在onload的时候再FinishActoin
            <%if (flag.length()>=5 && flag.substring(4, 5).equals("2")) {%>
            flowForm.op.value = "AutoSaveArchiveNodeCommit";
            <%}else{%>
            flowForm.op.value = 'finish';
            <%}%>

            <%if (flag.length()>=5 && flag.substring(4, 5).equals("2")) {%>
            // flowForm.formReportContent.value = hidFrame.getFormReportContent();
            <%}%>

            getXorSelect();

            var re = true;
            try {
                // 在嵌套表格页面中，定义了onsubmit方法，20121124该方法已改用jquery
                re = flowForm.onsubmit();
            } catch (e) {
            }

            if (re) {
                if (isAfterSaveformvalueBeforeXorCondSelect)
                    o("isAfterSaveformvalueBeforeXorCondSelect").value = "" + isAfterSaveformvalueBeforeXorCondSelect;

                $("#flowForm").submit();
            } else {
                toolbar.setDisabled(1, false);
                $('#bodyBox').hideLoading();
            }
        }

        function saveDraft() {
            hideDesigner();
            /*
            if (!LiveValidation.massValidate(lv_cwsWorkflowResult.formObj.fields)) {
                jAlert("请检查表单中的内容是否已正常填写！", "提示");
                return;
            }
            */

            var fields = lv_cwsWorkflowResult.formObj.fields;
            // 取消验证
            LiveValidation.cancelValidate(lv_cwsWorkflowResult.formObj.fields);

            if (o('flowForm').fireEvent) {
                o('op').value = "saveformvalue";
                if (o('flowForm').onsubmit) {
                    // if (o('flowForm').onsubmit()) {
                    toolbar.setDisabled(0, true);
                    $('#bodyBox').showLoading();

                    $('#flowForm').submit();
                    // }
                } else
                    $('#flowForm').submit();
            } else if (document.createEvent) {
                o('op').value = "saveformvalue";
                if (o('flowForm').onsubmit()) {
                    toolbar.setDisabled(0, true);
                    $('#bodyBox').showLoading();
                    $('#flowForm').submit();
                }
                /*
                // livevalidation只支持onsubmit方式
                // DOM2标准注册方式以及IE的注册方式attachEvent中，onsubmit方法是不存在的
                var ev = document.createEvent('HTMLEvents');
                ev.initEvent('submit', false, true);
                o('flowForm').dispatchEvent(ev);
                */
            }

            // 恢复验证
            LiveValidation.restoreValidate(fields);

        }

        // 退回
        function returnFlow() {
            <%if (cfg.getBooleanProperty("isFlowReturnWithRemark")) {%>
            if (o("cwsWorkflowResult").value == "") {
                jAlert('<lt:Label res="res.flow.Flow" key="note"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>');
                o("cwsWorkflowResult").focus();
                return;
            }
            <%}%>

            // 退回时验证数据合法性
            try {
                // 在form_js_formCode.jsp中写此方法
                var r = checkOnReturnBack();
                if (r != "") {
                    jAlert(r, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                    return;
                }
            } catch (e) {
            }

            <%if (wfp.getReturnStyle()==WorkflowPredefineDb.RETURN_STYLE_FREE) {%>
            $.ajax({
                type: "post",
                url: "flow_dispose_ajax_return.jsp",
                data: {
                    myActionId: "<%=myActionId%>",
                    actionId: "<%=actionId%>",
                    flowId: "<%=flowId%>"
                },
                dataType: "html",
                beforeSend: function (XMLHttpRequest) {
                    $('#bodyBox').showLoading();
                },
                success: function (data, status) {
                    o("spanLoad").innerHTML = "";
                    $("#dlg").html(data);
                    var $radios = $("#dlg").find("input[type='radio']");
                    if ($radios.length == 1) {
                        $radios[0].checked = true;
                    }
                    hideDesigner();
                    $("#dlg").dialog({
                        title: '<lt:Label res="res.flow.Flow" key="returnUser"/>', modal: true,
                        buttons: {
                            '<lt:Label res="res.flow.Flow" key="cancel"/>': function () {
                                $(this).dialog("close");
                            },
                            '<lt:Label res="res.flow.Flow" key="sure"/>': function () {
                                // 必须要用clone，否则checked属性在IE9、chrome中会丢失
                                // o("dlgReturn").innerHTML = $("#dlg").html();

                                jConfirm('<lt:Label res="res.flow.Flow" key="isReturn"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>', function (r) {
                                    if (!r) {
                                        return;
                                    } else {
                                        // 因为radio是成组的，所以不能直接用$("#dlgReturn").html($("#dlg").clone())
                                        // 某一组radio只会有一个被选中，所以这里可能会导致第一次选中返回时报请选择用户，而刷新以后，再选择用户返回就可以了
                                        var tmp = $("#dlg").clone().html();
                                        $("#dlgReturn").html(tmp);
                                        // IE11中在clone时，会丢失checked属性，在此重新赋予
                                        if (true || isIE11) {
                                            $("#dlg").find("input").each(function () {
                                                var obj = $(this);
                                                $("input:radio", o("dlgReturn")).each(function () {
                                                    if (obj.attr("value") == this.value) {
                                                        if (obj.attr("checked") == "checked") {
                                                            this.setAttribute("checked", "checked");
                                                        }
                                                    }
                                                });
                                            });
                                        }
                                    }

                                    $("#dlg").html('');

                                    flowForm.op.value = 'return';

                                    var fields = lv_cwsWorkflowResult.formObj.fields;
                                    // 取消验证
                                    LiveValidation.cancelValidate(lv_cwsWorkflowResult.formObj.fields);

                                    if (o('flowForm').onsubmit) {
                                        if (o('flowForm').onsubmit()) {
                                            $('#flowForm').submit();
                                        } else {
                                            jAlert('<lt:Label res="res.flow.Flow" key="formNotPass"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>');
                                        }
                                    } else
                                        $('#flowForm').submit();

                                    // 恢复验证
                                    LiveValidation.restoreValidate(fields);

                                });
                                $(this).dialog("close");
                                // $('#bodyBox').showLoading();
                            }
                        },
                        closeOnEscape: true,
                        draggable: true,
                        resizable: true,
                        width: 500
                    });
                },
                complete: function (XMLHttpRequest, status) {
                    $('#bodyBox').hideLoading();
                },
                error: function (XMLHttpRequest, textStatus) {
                    // 请求出错处理
                    jAlert(XMLHttpRequest.responseText, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                }
            });
            <%}else{%>
            $("#dlg").html(o("dlgReturn").innerHTML);
            hideDesigner();
            $("#dlg").dialog({
                title: '<lt:Label res="res.flow.Flow" key="returnUser"/>', modal: true,
                buttons: {
                    '<lt:Label res="res.flow.Flow" key="cancel"/>': function () {
                        $(this).dialog("close");
                    },
                    '<lt:Label res="res.flow.Flow" key="sure"/>': function () {
                        $("#dlgReturn").html($("#dlg").clone());

                        // IE11中在clone时，会丢失checked属性，在此重新赋予
                        if (isIE11) {
                            $("#dlg").find("input").each(function () {
                                var obj = $(this);
                                $("input:radio", o("dlgReturn")).each(function () {
                                    if (obj.attr("value") == this.value) {
                                        if (obj.attr("checked") == "checked") {
                                            this.setAttribute("checked", "checked");
                                        }
                                    }
                                });
                            });
                        }
                        jConfirm('<lt:Label res="res.flow.Flow" key="isReturn"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>', function (r) {
                            if (!r) {
                                return;
                            } else {
                                flowForm.op.value = 'return';

                                $('#bodyBox').showLoading();

                                var fields = lv_cwsWorkflowResult.formObj.fields;
                                // 取消验证
                                LiveValidation.cancelValidate(lv_cwsWorkflowResult.formObj.fields);

                                if (o('flowForm').onsubmit) {
                                    if (o('flowForm').onsubmit()) {
                                        $('#flowForm').submit();
                                    } else {
                                        jAlert('<lt:Label res="res.flow.Flow" key="formNotPass"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>');
                                    }
                                } else
                                    $('#flowForm').submit();

                                // 恢复验证
                                LiveValidation.restoreValidate(fields);
                            }
                        })
                        $(this).dialog("close");
                    }
                },
                closeOnEscape: true,
                draggable: true,
                resizable: true,
                width: 500
            });
            <%}%>
        }

        function read(isAfterSaveformvalueBeforeXorCondSelect) {
            flowForm.op.value = "read";

            if (o('flowForm').onsubmit) {
                if (o('flowForm').onsubmit()) {
                    $('#flowForm').submit();
                } else {
                    toolbar.setDisabled(0, false);
                    $('#bodyBox').hideLoading();
                }
            } else
                $('#flowForm').submit();
        }

        function manualFinish() {
            hideDesigner();
            $('body').showLoading();

            // 如果是自动存档节点，则先保存表单，然后回到此页面，在onload的时候再FinishActoin
            <%if (flag.length()>=5 && flag.substring(4, 5).equals("2")) {%>
            flowForm.op.value = "AutoSaveArchiveNodeManualFinish";
            <%}else{%>
            if (o("cwsWorkflowResult").value == "") {
                jAlert('<lt:Label res="res.flow.Flow" key="note"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>');
                return;
            }
            flowForm.op.value = "manualFinish";
            <%}%>
            var re = true;
            try {
                re = flowForm.onsubmit();
            } catch (e) {
            }
            if (re)
                $("#flowForm").submit();
        }

        function manualFinishAgree() {
            hideDesigner();

            if (o("cwsWorkflowResult").value == "") {
                // jAlert('<lt:Label res="res.flow.Flow" key="note"/>','<lt:Label res="res.flow.Flow" key="prompt"/>');
                // return;
            }
            flowForm.op.value = "manualFinishAgree";

            var re = true;
            try {
                re = flowForm.onsubmit();
            } catch (e) {
            }
            if (re)
                $("#flowForm").submit();
        }

        function SubmitNotDelive() {
            // 如果本节点是异或聚合，办理完毕，但不转交
            $.ajax({
                type: "post",
                url: "flow/setFinishAndNotDelive.do",
                data: {
                    myActionId: "<%=myActionId%>",
                    actionId: "<%=actionId%>"
                },
                dataType: "html",
                beforeSend: function (XMLHttpRequest) {
                    $('body').showLoading();
                },
                success: function (data, status) {
                    data = $.parseJSON(data);
                    if (data.ret == "0") {
                        jAlert(data.msg, "提示");
                    } else {
                        jAlert_Redirect(data.msg, "提示", "flow/flow_list.jsp?displayMode=1");
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

        // 审批文件，并作痕迹保留
        function ReviseByUserColor(user, colorindex, doc_id, file_id) {
            <%if (wa.isStart==0) {%>
            openWin("flow/flow_ntko_edit.jsp?file_id=" + file_id + "&flowId=<%=flowId%>&actionId=<%=actionId%>&doc_id=" + doc_id + "&isRevise=1", 1024, 768);
            <%}else{%>
            openWin("flow/flow_ntko_edit.jsp?file_id=" + file_id + "&flowId=<%=flowId%>&actionId=<%=actionId%>&doc_id=" + doc_id + "&isRevise=0", 1024, 768);
            <%}%>
        }

        function applyTemplate(user, doc_id, file_id) {
            // openWin("flow/flow_ntko_edit.jsp?file_id=" + file_id + "&flowId=<%=flowId%>&actionId=<%=actionId%>&doc_id=" + doc_id + "&isRevise=0&isApply=true", 800, 600);
        }

        function openWin(url, width, height) {
            if (width > window.screen.width)
                width = window.screen.width;
            if (height > window.screen.height)
                height = window.screen.height;
            var l = (window.screen.width - width) / 2;
            var t = (window.screen.height - height) / 2;
            var newwin = window.open(url, "_blank", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=" + t + ",left=" + l + ",width=" + width + ",height=" + height);
        }

        function saveArchive(flowId, actionId) {
            openWin("flow_doc_archive_save.jsp?op=saveFromFlow&flowId=" + flowId + "&actionId=" + actionId, 800, 600);
        }

        function saveArchiveGov(flowId, actionId) {
            openWin("visual/module_add.jsp?formCode=archive_files&flowId=" + flowId + "&actionId=" + actionId, 800, 600);
        }

        var disDepts = "";
        var disNames = "";

        function getDepts() {
            return disDepts;
        }

        function distributeDoc(flowId) {
            openWin("paper/paper_distribute.jsp?flowId=" + flowId + "&actionId=<%=actionId%>&myActionId=<%=myActionId%>", 800, 600);
            return;
        }

        var curUserSelectActionId;

        function OpenModifyWin(internalName, actionId, isXor, curActionId) {
            curUserSelectActionId = actionId;
            if (!curActionId) {
                curActionId = -1;
            }
            // showModalDialog('flow_action_modify.jsp?actionId=' + actionId + '&isXor=' + isXor, window.self,'dialogWidth:800px;dialogHeight:600px;status:no;help:no;')
            openWin('flow_action_modify.jsp?actionId=' + actionId + '&isXor=' + isXor + '&curActionId=' + curActionId, 800, 600)
        }

        function getXorSelect() {
            o("XorNextActionInternalNames").value = getCheckboxValue("XorActionSelected");
        }

        isCondSatisfied = false;

        function checkDesignerInstalled() {
            if (!isIE())
                return true;

            var bCtlLoaded = false;
            try {
                if (typeof (o("Designer").ModifyAction) == "undefined")
                    bCtlLoaded = false;
                if (typeof (o("Designer").ModifyAction) == "unknown") {
                    bCtlLoaded = true;
                }
            } catch (ex) {
            }
            return bCtlLoaded;
        }

        function switchProcessList() {
            if (o("imgSwitchProcess") == null) {
                return;
            }
            if (o("imgSwitchProcess").src.indexOf("show.png") != -1) {
                $("#processListTab").show();
                o("imgSwitchProcess").src = "images/hide.png";
                $("#spanSwitchProcess").html('&nbsp;&nbsp;<lt:Label res="res.flow.Flow" key="collapse"/>');
                o("imgSwitchProcess").alt = '<lt:Label res="res.flow.Flow" key="flowProcess"/>';
                o("imgSwitchProcess").title = '<lt:Label res="res.flow.Flow" key="flowProcess"/>';
            } else {
                $("#processListTab").hide();
                o("imgSwitchProcess").src = "images/show.png";
                $("#spanSwitchProcess").html('&nbsp;&nbsp;<lt:Label res="res.flow.Flow" key="expansion"/>');
                o("imgSwitchProcess").alt = '<lt:Label res="res.flow.Flow" key="displayProcess"/>';
                o("imgSwitchProcess").title = '<lt:Label res="res.flow.Flow" key="displayProcess"/>';
            }
        }

        function window_onload() {
            var re = false;
            <%if (canUserSeeDesignerWhenDispose.equals("true")) {%>
            re = checkDesignerInstalled();
            <%}%>
        }

        $(function () {
            switchProcessList();

            $(window).goToTop({
                showHeight: 1,//设置滚动高度时显示
                speed: 500 //返回顶部的速度以毫秒为单位
            });
        });
    </script>
</head>
<body onLoad="window_onload()">
<style>
    #loading {
        position: fixed;
        z-index: 400;
        width: 100%;
        height: 100%;
        top: 0;
        left: 0%;
        text-align: center;
        font-size: 0.9rem;
        color: #595758;
        background-color: #ffffff;
/*
        filter: alpha(Opacity=60);
        -moz-opacity: 0.6;
        opacity: 0.6;
*/
    }
</style>
<div id="loading">
    <img src="images/loading.gif" alt="loading.." style="margin-top:50px"/>
</div>
<div id="bodyBox">
    <div id="toolbar" class="toolbar-box"></div>
<%@ include file="inc/tip_phrase.jsp"%>
<%
    String mode = "user";
    if (canUserModifyFlow.equals("true")) {
        mode = "user";
    } else {
        mode = "view";
    }

    int doc_id = wf.getDocId();
    DocumentMgr dm = new DocumentMgr();
    Document doc = dm.getDocument(doc_id);
    Render rd = new Render(request, wf, doc);
    String content = rd.rend(wa);

    String spanNextUserDis = "";
    String strIsShowNextUsers = WorkflowActionDb.getActionProperty(wpd, wa.getInternalName(), "isShowNextUsers");
    boolean isNotShowNextUsers = strIsShowNextUsers != null && strIsShowNextUsers.equals("0");
    if (isNotShowNextUsers) {
        spanNextUserDis = "display:none";
    }
%>
    <form id="flowForm" name="flowForm" action="flow/finishAction.do" method="post" enctype="multipart/form-data">
        <div>
            <table width="100%" border="0" align="center" cellpadding="0" cellspacing="0" class="main flow-user">
                <tr>
                    <td align="left">
                        <span id="spanNextUser" style="<%=spanNextUserDis%>">
                        <jsp:include page="flow_dispose_ajax.jsp">
                            <jsp:param name="myActionId" value="<%=myActionId%>"/>
                            <jsp:param name="actionId" value="<%=actionId%>"/>
                        </jsp:include>
                        </span>
                        <!--提醒是否为加签-->
                        <%
                            if (mad.getActionStatus() == WorkflowActionDb.STATE_PLUS) {
                                int plusType = -1;
                                try {
                                    if (!"".equals(wa.getPlus())) {
                                        JSONObject plusJson = new JSONObject(wa.getPlus());
                                        try {
                                            plusType = plusJson.getInt("type");
                                        } catch (JSONException ex1) {
                                            throw new ErrMsgException(ex1.getMessage());
                                        }
                                    }
                                } catch (JSONException ex) {
                                    ex.printStackTrace();
                                }
                        %>
                        <div style="margin-top:10px">
                            <img src="images/alert.gif" align="absmiddle"/>&nbsp;
                            <lt:Label res="res.flow.Flow" key="plusing"/>
                            <%if (plusType == WorkflowActionDb.PLUS_TYPE_BEFORE) {%>
                            ，<lt:Label res="res.flow.Flow" key="completedPlus"/>
                            <%}%>
                        </div>
                        <%
                            }
                        %>
                    </td>
                    <td width="30%" align="right" style="font-size:12px; font-weight:normal">
                        <span id="projectName">
                        <%
                            if (wf.getProjectId() != -1) {
                                com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
                                FormDb prjFd = new FormDb();
                                prjFd = prjFd.getFormDb("project");
                                fdao = fdao.getFormDAO((int) wf.getProjectId(), prjFd);
                        %>
                            <lt:Label res="res.flow.Flow" key="project"/>：<a href="javascript:;" onclick="addTab('<%=fdao.getFieldValue("name")%>', 'project/project_show.jsp?projectId=<%=wf.getProjectId()%>&formCode=project')"><%=fdao.getFieldValue("name")%></a>&nbsp;&nbsp;<a title="取消关联" href="javascript:;" onclick="unlinkProject()"
                                                                                                                                                                                                                                                                                     style='font-size:16px; font-color:red'>×</a>
                            <%
                                }
                            %>
                        </span>
                        <%if (mad.getExpireDate() != null) {%>
                            <img src="images/clock.png" align="absmiddle"/>&nbsp;&nbsp;<lt:Label res="res.flow.Flow" key="expirationDate"/>：<%=DateUtil.format(mad.getExpireDate(), "yyyy-MM-dd HH:mm")%>
                        <%}%>
                        <span style="display: <%=wa.isMsg()?"":"none"%>">
                            <%
                                if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
                                    boolean flowAutoSMSRemind = cfg.getBooleanProperty("flowAutoSMSRemind");
                                    String chk = "checked=\"checked\"";
                                    if (!flowAutoSMSRemind) {
                                        chk = "";
                                    }
                            %>
                            <input id="isToMobile" name="isToMobile" value="true" type="checkbox" <%=chk%> />
                            <lt:Label res="res.flow.Flow" key="sms"/>
                            <%
                                }
                                boolean flowAutoMsgRemind = cfg.getBooleanProperty("flowAutoMsgRemind");
                                String chk = "checked=\"checked\"";
                                if (!flowAutoMsgRemind) {
                                    chk = "";
                                }

                                Leaf lfParent = new Leaf();
                                lfParent = lfParent.getLeaf(lf.getParentCode());
                            %>
                            <input id="isUseMsg" title="<%=lfParent.getName()%>：<%=lf.getName()%>" name="isUseMsg" value="true" type="checkbox" <%=chk%> />
                            <lt:Label res="res.flow.Flow" key="message"/>&nbsp;&nbsp; <span id="spanLoad"></span>
                        </span>
                    </td>
                </tr>
            </table>
        </div>
        <input name="flowAction" type="hidden"/>
        <div style="text-align:left; padding:0px 5px; margin-bottom:10px; color:#888888;margin-top:5px;">
                        <%if (cfg.getBooleanProperty("isFlowLevelDisplay") && !wf.isStarted()) {%>
                        <input name="cwsWorkflowLevel" type="radio" value="<%=WorkflowDb.LEVEL_NORMAL%>" checked/><img style="margin-left: 5px" src="images/general.png" align="absmiddle"/>&nbsp;<lt:Label res="res.flow.Flow" key="ordi"/>
                        <input name="cwsWorkflowLevel" type="radio" value="<%=WorkflowDb.LEVEL_IMPORTANT%>"/><img style="margin-left: 5px" src="images/important.png" align="absmiddle"/>&nbsp;<lt:Label res="res.flow.Flow" key="impor"/>
                        <input name="cwsWorkflowLevel" type="radio" value="<%=WorkflowDb.LEVEL_URGENT%>"/><img style="margin-left: 5px" src="images/urgent.png" align="absmiddle"/>&nbsp;<lt:Label res="res.flow.Flow" key="emergent"/>
                        <script>
                            setRadioValue("cwsWorkflowLevel", "<%=wf.getLevel()%>");
                        </script>
                        <%} else {%>
                        <%=WorkflowMgr.getLevelImg(request, wf)%>
                        <%}%>
                        <lable style="">&nbsp; ID：<%=wf.getId()%>&nbsp;</lable>
                        &nbsp;<lt:Label res="res.flow.Flow" key="tit"/>：
                        <%
                            String flowTitle = "";
                            if (wf.getTitle().startsWith("#")) {
                                flowTitle = LocalUtil.LoadString(request, "res.ui.menu", wf.getTypeCode());
                            } else {
                                flowTitle = wf.getTitle();
                            }
                            // 当设置了默认标题时，不允许修改流程标题
                            if (wf.isStarted()) { // || !lf.getDescription().equals("")) {%>
                        <%=flowTitle%><input type="hidden" id="cwsWorkflowTitle" name="cwsWorkflowTitle" value="<%=StrUtil.HtmlEncode(flowTitle)%>" style="width:200px;"/>&nbsp;&nbsp;
                        <%} else {%>
                        <input id="cwsWorkflowTitle" name="cwsWorkflowTitle" value="<%=StrUtil.HtmlEncode(flowTitle)%>" style="border:1px solid #cccccc; color:#888888;width:200px;" size="40"/>&nbsp;
                        <%
                            }
                        %>
                        <lt:Label res="res.flow.Flow" key="organ"/>：
                        <%
                            String starterName = wf.getUserName();
                            String starterRealName = "";
                            if (starterName != null) {
                                UserDb starter = um.getUserDb(wf.getUserName());
                                starterRealName = starter.getRealName();
                            }
                            out.print(starterRealName);
                            if (wf.isStarted()) {%>
                            &nbsp;<lt:Label res="res.flow.Flow" key="organDate"/>： <%=DateUtil.format(wf.getBeginDate(), "MM-dd HH:mm")%>
                            <%
                            }
                            if (wf.getStatus() == WorkflowDb.STATUS_FINISHED) {
                                if (wpd.isReactive()) {
                                %>
                                &nbsp;&nbsp;<span style="color: red;" title="<lt:Label res="res.flow.Flow" key="alteringTitle"/>">
                                <lt:Label res="res.flow.Flow" key="altering"/></span>
                                <%
                                }
                            }
                            else {
                                if (wf.isAlter()) {
                                    UserCache userCache = SpringUtil.getBean(UserCache.class);
                                    String alterUserRealName = "";
                                    User alterUser = userCache.getUser(wf.getAlterUser());
                                    if (alterUser!=null) {
                                        alterUserRealName = alterUser.getRealName();
                                    }
                                %>
                                &nbsp;&nbsp;
                                <span style="color: red;" title="<lt:Label res="res.flow.Flow" key="alteringTitle"/>">
                                    <lt:Label res="res.flow.Flow" key="altering"/>
                                    <lt:Label res="res.flow.Flow" key="alterUser"/><%=alterUserRealName%>&nbsp;&nbsp;<%=DateUtil.format(wf.getAlterTime(), "MM-dd HH:mm")%>
                                </span>
                                <%
                                }
                            }

                            String remarkDis = "";
                            boolean flowIsRemarkShow = cfg.getBooleanProperty("flowIsRemarkShow");
                            if (!flowIsRemarkShow) {
                                remarkDis = "style='display:none'";
                            }
                        %>
                        <span <%=remarkDis%>>
                        &nbsp;&nbsp;<lt:Label res="res.flow.Flow" key="rem"/>：
                        <input id="cwsWorkflowResult" name="cwsWorkflowResult" size="30" style="border:1px solid #cccccc; color:#888888;width:250px;" value="<%=StrUtil.HtmlEncode(mad.getResult())%>"/>
                        </span>
                    </div>
					<div class="main-content">
                    <table id="designerTable" width="100%" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                            <td align="center">
                                <%
                                    boolean canUserSeeFlowImage = cfg.getBooleanProperty("canUserSeeFlowImage");
                                    if (canUserSeeFlowImage) { //  && !"".equals(wf.getImgVisualPath())) {
                                %>
                                <div id="Designer" class="flow-image-box" style="width:0px; height:0px; overflow-x:scroll; overflow-y: scroll;">
                                </div>
                                <%
                                } else if (!canUserSeeFlowChart && "true".equals(canUserSeeDesignerWhenDispose)) {
                                    if ("day".equals(flowExpireUnit)) {
                                        String str1 = LocalUtil.LoadString(request, "res.flow.Flow", "day");
                                        flowExpireUnit = str1;
                                    } else {
                                        String str1 = LocalUtil.LoadString(request, "res.flow.Flow", "hour");
                                        flowExpireUnit = str1;
                                    }

                                    boolean isOem = License.getInstance().isOem();
                                    String codeBase = "";
                                    if (!isOem) {
                                        codeBase = "codebase=\"activex/cloudym.CAB#version=1,3,0,0\"";
                                    }
                                %>
                                <object id="Designer" classid="CLSID:ADF8C3A0-8709-4EC6-A783-DD7BDFC299D7" <%=codeBase%> style="width:0px; height:0px;">
                                    <param name="Workflow" value="<%=wf.getFlowString()%>"/>
                                    <param name="Mode" value="<%=mode%>"/>
                                    <!--debug user initiate complete-->
                                    <param name="CurrentUser" value="<%=privilege.getUser(request)%>"/>
                                    <param name="ExpireUnit" value="<%=flowExpireUnit%>">
                                    <%
                                        com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();
                                    %>
                                    <param name="Organization" value="<%=license.getCompany()%>"/>
                                    <param name="Key" value="<%=license.getKey()%>"/>
                                    <param name="LicenseType" value="<%=license.getType()%>"/>
                                </object>
                                <%}%>
                            </td>
                        </tr>
                    </table>
                    <div style="text-align:center">
                        <span id="switchProcessBox" style="cursor:pointer" onclick="switchProcessList()">
                            <img id="imgSwitchProcess" src="images/hide.png" alt="<lt:Label res='res.flow.Flow' key='displayProcess'/>"/><span id="spanSwitchProcess" style="font-size: 12px;color:#606060;">&nbsp;&nbsp;<lt:Label res='res.flow.Flow' key='expansion'/></span>
                        </span>
                    </div>
                    <table id="processListTab" width="98%" class="tabStyle_1 percent98" style="display:none;margin-top:10px;background-color:#fff">
                        <thead>
                        <tr>
                            <td class="tabStyle_1_title" width="7%" align="center"><lt:Label res="res.flow.Flow" key="handler"/></td>
                            <td class="tabStyle_1_title" width="5%" align="center"><lt:Label res="res.flow.Flow" key="bearer"/></td>
                            <td class="tabStyle_1_title" width="8%" align="center"><lt:Label res="res.flow.Flow" key="task"/></td>
                            <td class="tabStyle_1_title" width="7%" align="center"><lt:Label res="res.flow.Flow" key="startTime"/></td>
                            <td class="tabStyle_1_title" width="7%" align="center"><lt:Label res="res.flow.Flow" key="handleTime"/></td>
                            <%
                                if (cfg.getBooleanProperty("flowPerformanceDisplay")) {
                            %>
                            <td class="tabStyle_1_title" width="7%" align="center"><lt:Label res="res.flow.Flow" key="remainTime"/></td>
                            <td class="tabStyle_1_title" width="5%" align="center"><lt:Label res="res.flow.Flow" key="timeSpent"/>
                                (<%=flowExpireUnit%>)
                            </td>
                            <td class="tabStyle_1_title" width="4%" align="center"><lt:Label res="res.flow.Flow" key="achievements"/></td>
                            <%
                                }
                                if (wpd.isReactive()) {
                            %>
                            <td class="tabStyle_1_title" width="6%" align="center"><lt:Label res="res.flow.Flow" key="alterTime"/></td>
                            <%
                                }
                            %>
                            <td class="tabStyle_1_title" width="6%" align="center"><lt:Label res="res.flow.Flow" key="handle"/></td>
                            <td class="tabStyle_1_title" width="12%" align="center"><lt:Label res="res.flow.Flow" key="rem"/></td>
                        </tr>
                        </thead>
                        <tbody>
                        <%
                            Vector vProcess = mad.getMyActionDbOfFlow((int)mad.getFlowId());
                            Iterator ir = vProcess.iterator();
                            DeptMgr deptMgr = new DeptMgr();
                            OACalendarDb oad = new OACalendarDb();
                            int m = 0;
                            while (ir.hasNext()) {
                                MyActionDb madPro = (MyActionDb) ir.next();
                                WorkflowDb wfd = new WorkflowDb();
                                wfd = wfd.getWorkflowDb((int) madPro.getFlowId());
                                String userName = wfd.getUserName();
                                String userRealName = "";
                                if (userName != null) {
                                    UserDb user = um.getUserDb(madPro.getUserName());
                                    userRealName = user.getRealName();
                                }
                                WorkflowActionDb wad = new WorkflowActionDb();
                                wad = wad.getWorkflowActionDb((int) madPro.getActionId());
                                m++;
                        %>
                        <tr class="highlight">
                            <td><%
                                String deptCodes = madPro.getDeptCodes();
                                String[] depts = StrUtil.split(deptCodes, ",");
                                if (depts != null) {
                                    String dts = "";
                                    int deptLen = depts.length;
                                    for (int n = 0; n < deptLen; n++) {
                                        DeptDb dd = deptMgr.getDeptDb(depts[n]);
                                        if (dd != null) {
                                            if (dts.equals("")) {
                                                dts = dd.getName();
                                            } else {
                                                dts += "," + dd.getName();
                                            }
                                        }
                                    }
                                    if (!dts.equals("")) {
                                        out.print(dts + "：");
                                    }
                                }

                                boolean isExpired = false;
                                java.util.Date chkDate = madPro.getCheckDate();
                                if (chkDate == null) {
                                    chkDate = new Date();
                                }
                                if (DateUtil.compare(chkDate, madPro.getExpireDate()) == 1) {
                                    isExpired = true;
                                }
                                if (isExpired) {%>
                                <img src="images/flow/expired.png" align="absmiddle" alt="<lt:Label res='res.flow.Flow' key='timeOut'/>"/>
                                <%}%>
                                <%=userRealName%>
                            </td>
                            <td><%
                                if (madPro.getPrivMyActionId() != -1) {
                                    MyActionDb mad2 = madPro.getMyActionDb(madPro.getPrivMyActionId());
                                    if (mad2.getUserName() != null) {
                                        out.print(um.getUserDb(mad2.getUserName()).getRealName());
                                    }
                                } else {
                                    out.print("&nbsp;");
                                }
                            %>
                            </td>
                            <td><%=wad.getTitle()%>
                            </td>
                            <td align="center"><%=DateUtil.format(madPro.getReceiveDate(), "yy-MM-dd HH:mm")%>
                            </td>
                            <td align="center"><%=DateUtil.format(madPro.getCheckDate(), "yy-MM-dd HH:mm")%>
                            </td>
                            <%
                                if (cfg.getBooleanProperty("flowPerformanceDisplay")) {
                            %>
                            <td align="center">
                                <%
                                    String remainDateStr = "";
                                    if (mad.getExpireDate() != null && DateUtil.compare(new java.util.Date(), mad.getExpireDate()) == 2) {
                                        int[] ary = DateUtil.dateDiffDHMS(mad.getExpireDate(), new java.util.Date());
                                        String str_day = LocalUtil.LoadString(request, "res.flow.Flow", "day");
                                        String str_hour = LocalUtil.LoadString(request, "res.flow.Flow", "h_hour");
                                        String str_minute = LocalUtil.LoadString(request, "res.flow.Flow", "minute");
                                        remainDateStr = ary[0] + " " + str_day + ary[1] + " " + str_hour + ary[2] + " " + str_minute;
                                        out.print(remainDateStr);
                                    }%>
                            </td>
                            <td align="center">
                                <%
                                if (isHour) {
                                    try {
                                        double d = oad.getWorkHourCount(madPro.getReceiveDate(), madPro.getCheckDate());
                                        out.print(NumberUtil.round(d, 1));
                                    } catch (ErrMsgException e) {
                                        out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
                                    }
                                } else {
                                    int d = oad.getWorkDayCountFromDb(madPro.getReceiveDate(), madPro.getCheckDate());
                                    out.print(d);
                                }
                                %>
                            </td>
                            <td align="center"><%=NumberUtil.round(madPro.getPerformance(), 2)%>
                            </td>
                            <%
                                }
                                if (wpd.isReactive()) {
                            %>
                            <td align="center"><%=DateUtil.format(madPro.getAlterTime(), "yy-MM-dd HH:mm")%>
                            <%
                                }
                            %>
                            <td align="center">
                                <%
                                    if (madPro.getChecker().equals(UserDb.SYSTEM)) {
                                        String str1 = LocalUtil.LoadString(request, "res.flow.Flow", "skipOverTime");
                                        out.print(str1);
                                    } else {
                                %>
                                <%=madPro.getCheckStatusName()%>
                                <%
                                    }
                                    if (madPro.getCheckStatus() != 0 && madPro.getCheckStatus() != MyActionDb.CHECK_STATUS_TRANSFER && madPro.getCheckStatus() != MyActionDb.CHECK_STATUS_SUSPEND) {
                                        if (madPro.getResultValue() != WorkflowActionDb.RESULT_VALUE_RETURN) {
                                            out.print("(" + WorkflowActionDb.getResultValueDesc(madPro.getResultValue()) + ")");
                                        }
                                    }
                                %>
                            </td>
                            <td align="center"><%=madPro.getResult()%>
                            </td>
                        </tr>
                        <%}%>
                        </tbody>
                    </table>
                    <%
                        Vector returnv = wa.getLinkReturnActions();
                        if (returnv.size() > 0 || wfp.getReturnStyle() == WorkflowPredefineDb.RETURN_STYLE_FREE) {
                    %>
                    <div id="dlgReturn" style="display:none"><lt:Label res="res.flow.Flow" key="backTo"/>：
                        <%
                            Iterator returnir = returnv.iterator();
                            while (returnir.hasNext()) {
                                WorkflowActionDb returnwa = (WorkflowActionDb) returnir.next();
                                if (returnwa.getStatus() != WorkflowActionDb.STATE_IGNORED) {
                        %>
                        <input type="checkbox" name="returnId" value="<%=returnwa.getId()%>" checked="checked"/>
                        <%=returnwa.getTitle()%>：<%=returnwa.getUserRealName()%>
                        <% }
                        }
                        %>
                    </div>
                    <%
                        }
                    %>

        <table id="tableBox" width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
            <tr>
                <td width="35%" align="center">
                    <table class="percent98" border="0" align="center" cellpadding="0" cellspacing="0">
                        <tr>
                            <td align="left" style="padding-top:5px;">
                                <%if (fd.isHasAttachment()) {%>
                                <script>initUpload();</script>
                                <%}%>
                                <input type="hidden" name="flowId" value="<%=flowId%>"/>
                                <input type="hidden" name="actionId" value="<%=actionId%>"/>
                                <input type="hidden" name="myActionId" value="<%=myActionId%>"/>
                                <input type="hidden" name="XorNextActionInternalNames"/>
                                <input type="hidden" name="op" value="saveformvalue"/>
                                <input type="hidden" name="isAfterSaveformvalueBeforeXorCondSelect"/>
                                <textarea name="formReportContent" style="display:none"></textarea>
                            </td>
                        </tr>
                    </table>
                    <div id="attBox"></div>
                    <%
                        if (lf.getQueryId() != Leaf.QUERY_NONE) {
                            // 判断权限，管理员能看见查询，其它人员根据角色进行判断
                            String[] roles = StrUtil.split(lf.getQueryRole(), ",");
                            boolean canSeeQuery = false;
                            if (!privilege.isUserPrivValid(request, "admin")) {
                                if (roles != null) {
                                    UserDb user = new UserDb();
                                    user = user.getUserDb(privilege.getUser(request));
                                    for (int i = 0; i < roles.length; i++) {
                                        if (user.isUserOfRole(roles[i])) {
                                            canSeeQuery = true;
                                            break;
                                        }
                                    }
                                } else {
                                    canSeeQuery = true;
                                }
                            } else {
                                canSeeQuery = true;
                            }
                            FormQueryDb aqd = new FormQueryDb();
                            aqd = aqd.getFormQueryDb((int) lf.getQueryId());
                            if (canSeeQuery && aqd.isLoaded()) {
                    %>
                    <div id="formQueryBox"></div>
                    <%
                        String colratio = "";
                        String colP = aqd.getColProps();
                        if (colP == null || colP.equals("")) {
                            colP = "[]";
                        }
                        int tableWidth = 0;
                        JSONArray jsonArray = new JSONArray(colP);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject json = jsonArray.getJSONObject(i);
                            if (((Boolean) json.get("hide")).booleanValue()) {
                                continue;
                            }
                            String name = (String) json.get("name");
                            if (name.equalsIgnoreCase("cws_op")) {
                                continue;
                            }
                            tableWidth += ((Integer) json.get("width")).intValue();
                            if (colratio.equals("")) {
                                colratio = "" + ((Integer) json.get("width")).intValue();
                            } else {
                                colratio += "," + ((Integer) json.get("width")).intValue();
                            }
                        }

                        // System.out.println(getClass() + " colratio=" + colratio);

                        String queryAjaxUrl;
                        if (aqd.isScript()) {
                            queryAjaxUrl = "flow/form_query_list_script_embed_ajax.jsp";
                        } else {
                            queryAjaxUrl = "flow/form_query_list_embed_ajax.jsp";
                        }

                        JSONObject json = new JSONObject(lf.getQueryCondMap());
                        Iterator irJson = json.keys();
                    %>
                    <style>
                        .pager {
                            margin: 3px 0px;
                        }
                    </style>
                    <script>
                        function onQueryRelateFieldChange() {
                            $.ajax({
                                type: "post",
                                contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                                url: "<%=queryAjaxUrl%>",
                                data: {
                                    id: "<%=lf.getQueryId()%>",
                                <%
                                while (irJson.hasNext()) {
                                    String qField = (String) irJson.next();
                                %>
                                <%=qField%> : o("<%=qField%>").value,
                                <%
                                }
                                %>
                                flowTypeCode : "<%=lf.getCode()%>"
                                },
                                dataType: "html",
                                    beforeSend:function (XMLHttpRequest) {
                                    $('#bodyBox').showLoading();
                                },
                                success: function (data, status) {
                                    // 如果存在queryBox（内置于表单中）
                                    if (o("queryBox")) {
                                        o("queryBox").innerHTML = data;
                                    } else {
                                        o("formQueryBox").innerHTML = data;
                                    }

                                    $('#formQueryTable').fixheadertable({
                                        caption: '<%=aqd.getQueryName()%>',
                                        colratio: [<%=colratio%>],
                                        // height      : 150,
                                        width: <%=tableWidth+2%>,
                                        zebra: true,
                                        // sortable    : true,
                                        sortedColId: 1,
                                        resizeCol: true,
                                        pager: true,
                                        rowsPerPage: 25, // 10 25 50 100
                                        // sortType    : ['integer', 'string', 'string', 'string', 'string', 'date'],
                                        dateFormat: 'm/d/Y'
                                    });
                                },
                                complete: function (XMLHttpRequest, status) {
                                    $('#bodyBox').hideLoading();
                                },
                                error: function (XMLHttpRequest, textStatus) {
                                    $('#bodyBox').hideLoading();
                                    // 请求出错处理
                                    // jAlert("查询错误！",'<lt:Label res="res.flow.Flow" key="prompt"/>');
                                    alert(XMLHttpRequest.responseText);
                                }
                            });
                        }

                        $(function () {
                            onQueryRelateFieldChange();
                        });

                        <%
                        irJson = json.keys();
                        while (irJson.hasNext()) {
                            // 主表单中的字段
                            String qField = (String) irJson.next();
                            %>
                        $(function () {
                            var oldValue_<%=qField%> = o("<%=qField%>").value;

                            setInterval(function () {
                                if (oldValue_<%=qField%> != o("<%=qField%>").value) {
                                    onQueryRelateFieldChange();
                                    oldValue_<%=qField%> = o("<%=qField%>").value;
                                }
                            }, 500);
                        });
                        <%
                    }
                    %>
                    </script>
                    <%}%>
                    <%}%>

                    <div id="netdiskFilesDiv" class="percent98" style="line-height:1.5; text-align:left"></div>

                    <div id="formContent" class="form-content">
                        <%
                            // 判断是否需按选项卡的方式显示
                            StringBuffer sbUl = new StringBuffer();
                            StringBuffer sbDiv = new StringBuffer();
                            FormDb formDbOther = new FormDb();
                            MacroCtlMgr mm = new MacroCtlMgr();
                            Iterator irField = fd.getFields().iterator();
                            while (irField.hasNext()) {
                                FormField macroField = (FormField) irField.next();
                                if (macroField.getType().equals(FormField.TYPE_MACRO)) {
                                    MacroCtlUnit mu = mm.getMacroCtlUnit(macroField.getMacroType());
                                    if (mu != null && mu.getNestType() == MacroCtlUnit.NEST_TYPE_NORMAIL) {
                                        String destForm = macroField.getDescription();
                                        try {
                                            String defaultVal = StrUtil.decodeJSON(destForm);
                                            JSONObject json = new JSONObject(defaultVal);
                                            int isTab = 0;
                                            if (json.has("isTab")) {
                                                isTab = json.getInt("isTab");
                                                if (isTab == 1) {
                                                    destForm = json.getString("destForm");
                                                    formDbOther = formDbOther.getFormDb(destForm);
                                                    sbUl.append("<li><a href='#tabs-" + destForm + "'>" + formDbOther.getName() + "</a></li>");
                                                    sbDiv.append("<div id='tabs-" + destForm + "' class='tabDiv'></div>");
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }

                            if (sbUl.length() > 0) {
                        %>
                        <style>
                            .tabDiv {
                                min-height: 100%;
                                _height: 100%;
                                height: 100%;
                                padding-top: 10px;
                            }
                        </style>
                        <%
                            String strUl = "<ul>" + "<li><a href='#tabs-" + fd.getCode() + "'>" + fd.getName() + "</a></li>" + sbUl.toString() + "</ul>";
                            String strDiv = "<div id='tabs-" + fd.getCode() + "' class='tabDiv'>" + content + "</div>" + sbDiv.toString();
                            out.print("<div id='tabs' style='height:100%'>" + strUl + strDiv + "</div>");
                        %>
                        <script>
                            $(function () {
                                $('#tabs').tabs();
                            });
                        </script>
                        <%
                            } else {
                                out.print(content);
                            }
                            String replyDis = wf.isStarted() ? "" : "display:none";
                            if (!wpd.isReply()) {
                                replyDis = "display:none";
                            }
                        %>
                    </div>
                    <br/>
                    <div id="divAnnexBox" style="width:98%;background-color:#efefef;padding-top:10px;padding-bottom:10px;margin-bottom:50px;<%=replyDis%>">
                        <div id="divAnnex" style="width:98%;background-color:white;padding-top:10px;">
                            <table class="" width="95%" border="0" cellspacing="0" cellpadding="0">
                                <tr>
                                    <td width="48%" align="left" style="text-align:left"><strong>&nbsp;附言：</strong>
                                        <%
                                            if (fd.isProgress()) {
                                                com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
                                                fdao = fdao.getFormDAO(flowId, fd);
                                        %>
                                        （总进度<span id="totalProgress"><%=fdao.getCwsProgress()%></span>%）
                                        <%}%>
                                    </td>
                                    <td width="52%" style="text-align:right">
                                        <input id="showDiv" style="display:none" class="mybtn2" type="button" value="展开" onclick="show()"/>
                                        <input id="notShowDiv" class="mybtn2" type="button" value="收起" onclick="notshow()"/>
                                        <input class="mybtn2" type="button" value="回复" onclick="addMyReply('<%=0%>')"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td align="left" colspan="2">
                                        <div id="myReplyTextarea<%=0%>" style="display:none; clear:both;position:relative;margin-bottom:40px">
                                            <textarea name="myReplyTextareaContent" id="get<%=0%>" class="myTextarea"></textarea>
                                            <span align="left" title="<lt:Label res='res.flow.Flow' key='othersHidden'/>" style="cursor:pointer;" onclick="chooseHideComment(this);"><img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png"/>&nbsp;<lt:Label res='res.flow.Flow'
                                                                                                                                                                                                                                                                                                key='needHidden'/>
                                    <input type="hidden" id="isSecret<%=0%>" name="isSecret<%=0%>" value="0"/>
                                    </span>
                                            <%if (fd.isProgress()) {%>
                                            &nbsp;&nbsp;进度&nbsp;<input id="cwsProgress" name="cwsProgress" style="width:30px; height:22px;" value="0" readonly/>
                                            <div id="slider" style="margin-left:10px;width:200px; display:inline-block; *display:inline;*zoom:1;"></div>
                                            <%}%>

                                            <script>
                                                $(function () {
                                                    $("#slider").slider({
                                                        value: 0,
                                                        min: 0,
                                                        max: 100,
                                                        step: 5,
                                                        slide: function (event, ui) {
                                                            $("#cwsProgress").val(ui.value);
                                                        }
                                                    });
                                                });
                                            </script>

                                            <input type="hidden" id="myActionId<%=0%>" name="myActionId<%=0%>" value="<%=myActionId%>"/>
                                            <input type="hidden" id="discussId<%=0%>" name="discussId<%=0%>" value="<%=0 %>"/>
                                            <input type="hidden" id="flow_id<%=0%>" name="flow_id<%=0%>" value="<%=flowId %>"/>
                                            <input type="hidden" id="action_id<%=0%>" name="action_id<%=0%>" value="<%=0 %>"/>
                                            <input type="hidden" id="user_name<%=0%>" name="user_name<%=0%>" value="<%=myname%>"/>
                                            <input type="hidden" id="userRealName<%=0%>" name="userRealName<%=0%>" value="<%=myRealName%>"/>
                                            <input type="hidden" id="reply_name<%=0%>" name="reply_name<%=0%>" value="<%=myname%>"/>
                                            <input type="hidden" id="parent_id<%=0%>" name="parent_id<%=0%>" value="<%=-1%>"/>
                                            <input class="mybtn" type="button" value="<lt:Label res='res.flow.Flow' key='sure'/>" onclick="submitPostscript('<%=0%>','<%=0%>')"/>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="2">
                                        <hr class="hrLine"/>
                                    </td>
                                </tr>
                            </table>
                            <div id="divShow" style="width:98%;">
                                <table id="tablehead" class="" width="95%" border="0" cellspacing="3" cellpadding="0">
                                </table>
                                <%
                                    int total = 0;
                                    int pagesize = 20;
                                    int curpage = ParamUtil.getInt(request, "CPages", 1);

                                    WorkflowAnnexDb wad = new WorkflowAnnexDb();
                                    Vector vec1 = wad.listRoot(flowId, myname);
                                    Iterator<WorkflowAnnexDb> ir1 = vec1.iterator();

                                    while (ir1.hasNext()) {
                                        wad = ir1.next();
                                        int id = (int) wad.getLong("id");
                                        int n = 1;
                                %>
                                <table id="replaytable<%=id%>" class="" width="95%" border="0" cellspacing="3" cellpadding="0">
                                    <tr id="trReply<%=id%>">
                                        <td width="50" style="text-align:left;" class="nameColor">
                                            <%=um.getUserDb(wad.getString("user_name")).getRealName()%>&nbsp;:
                                        </td>
                                        <td width="70%" style="text-align:left;word-break:break-all">
                                            <%
                                                if (fd.isProgress()) {
                                            %>
                                            <div>进度：<%=wad.getInt("progress")%>%</div>
                                            <%
                                                }
                                            %>
                                            <%=wad.getString("content")%>
                                            <%if (isFlowManager) {%>
                                            <a href="javascript:;" onclick="delAnnex('<%=wad.getLong("id")%>')">[<lt:Label res="res.flow.Flow" key="delete"/>]</a>
                                            <%}%>
                                        </td>
                                        <td width="" style="text-align:right;">
                                            <%=DateUtil.format(wad.getDate("add_date"), "yyyy-MM-dd HH:mm:ss")%> &nbsp;&nbsp;
                                            <a align="right" class="comment" href="javascript:;" onclick="addMyReply('<%=id%>')"><img title='<lt:Label res="res.flow.Flow" key="replyTo"/>' src="images/dateline/replyto.png"/></a>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td align="left" colspan="3">
                                            <div id="myReplyTextarea<%=id%>" style="display:none; clear:both;position:relative;margin-bottom:40px">
                                                <textarea name="myReplyTextareaContent" id="get<%=id%>" class="myTextarea"></textarea>
                                                <span align="left" title="<lt:Label res='res.flow.Flow' key='othersHidden'/>" style="cursor:pointer;" onclick="chooseHideComment(this);"><img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png"/>&nbsp;<lt:Label res='res.flow.Flow'
                                                                                                                                                                                                                                                                                                    key='needHidden'/><input
                                                        type="hidden" id="isSecret<%=id%>" name="isSecret<%=id%>" value="0"/></span>
                                                <input type="hidden" id="myActionId<%=id%>" name="myActionId<%=id%>" value=""/>
                                                <input type="hidden" id="discussId<%=id%>" name="discussId<%=id%>" value="<%=id %>"/>
                                                <input type="hidden" id="flow_id<%=id%>" name="flow_id<%=id%>" value="<%=wad.getString("flow_id") %>"/>
                                                <input type="hidden" id="action_id<%=id%>" name="action_id<%=id%>" value="<%=wad.getString("action_id") %>"/>
                                                <input type="hidden" id="user_name<%=id%>" name="user_name<%=id%>" value="<%=myname%>"/>
                                                <input type="hidden" id="userRealName<%=id%>" name="userRealName<%=id%>" value="<%=myRealName%>"/>
                                                <input type="hidden" id="reply_name<%=id%>" name="reply_name<%=id%>" value="<%=wad.getString("user_name")%>"/>
                                                <input type="hidden" id="parent_id<%=id%>" name="parent_id<%=id%>" value="<%=id%>"/>
                                                <input class="mybtn" type="button" value="<lt:Label res='res.flow.Flow' key='sure'/>" onclick="submitPostscript('<%=id%>','<%=id%>')"/>
                                            </div>
                                        </td>
                                    </tr>
                                    <%
                                        WorkflowAnnexDb wad2 = new WorkflowAnnexDb();
                                        Vector vec2 = wad2.listChildren(wad.getInt("id"), myname);
                                        Iterator<WorkflowAnnexDb> ir2 = vec2.iterator();

                                        while (ir2.hasNext()) {
                                            wad2 = ir2.next();
                                            int id2 = (int) wad2.getLong("id");
                                    %>
                                    <tr id="trReply<%=id2%>" pId="<%=id%>">
                                        <td width="180" style="text-align:left;" class="nameColor">
                                            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=um.getUserDb(wad2.getString("user_name")).getRealName()%>&nbsp;回复&nbsp;<%=um.getUserDb(wad2.getString("reply_name")).getRealName()%>&nbsp;:
                                        </td>
                                        <td style="text-align:left;">
                                            <%=wad2.getString("content")%>
                                            <%if (isFlowManager) {%>
                                            <a href="javascript:;" onclick="delAnnex('<%=wad2.getLong("id")%>')">[<lt:Label res="res.flow.Flow" key="delete"/>]</a>
                                            <%}%>
                                        </td>
                                        <td style="text-align:right;">
                                            <%=DateUtil.format(wad2.getDate("add_date"), "yyyy-MM-dd HH:mm:ss")%> &nbsp;&nbsp;
                                            <a align="right" class="comment" href="javascript:;" onclick="addMyReply('<%=id2%>')"><img title='<lt:Label res="res.flow.Flow" key="replyTo"/>' src="images/dateline/replyto.png"/></a>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td align="left" colspan="3">
                                            <div id="myReplyTextarea<%=id2%>" style="display:none; clear:both;position:relative;margin-bottom:40px">
                                                <textarea name="myReplyTextareaContent" id="get<%=id2%>" class="myTextarea"></textarea>
                                                <span align="left" title="<lt:Label res='res.flow.Flow' key='othersHidden'/>" style="cursor:pointer;" onclick="chooseHideComment(this);"><img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png"/>&nbsp;<lt:Label res='res.flow.Flow'
                                                                                                                                                                                                                                                                                                    key='needHidden'/><input
                                                        type="hidden" id="isSecret<%=id2%>" name="isSecret<%=id2%>" value="0"/></span>
                                                <input type="hidden" id="myActionId<%=id2%>" name="myActionId<%=id2%>" value=""/>
                                                <input type="hidden" id="discussId<%=id2%>" name="discussId<%=id2%>" value="<%=id2 %>"/>
                                                <input type="hidden" id="flow_id<%=id2%>" name="flow_id<%=id2%>" value="<%=wad2.getString("flow_id") %>"/>
                                                <input type="hidden" id="action_id<%=id2%>" name="action_id<%=id2%>" value="<%=wad2.getString("action_id") %>"/>
                                                <input type="hidden" id="user_name<%=id2%>" name="user_name<%=id2%>" value="<%=myname%>"/>
                                                <input type="hidden" id="userRealName<%=id2%>" name="userRealName<%=id2%>" value="<%=myRealName%>"/>
                                                <input type="hidden" id="reply_name<%=id2%>" name="reply_name<%=id2%>" value="<%=wad2.getString("user_name")%>"/>
                                                <input type="hidden" id="parent_id<%=id2%>" name="parent_id<%=id2%>" value="<%=id%>"/>
                                                <input class="mybtn" type="button" value="<lt:Label res='res.flow.Flow' key='sure'/>" onclick="submitPostscript('<%=id2%>','<%=id%>')"/>
                                            </div>
                                        </td>
                                    </tr>
                                    <%
                                        }
                                    %>
                                    <tr id="trline<%=id%>">
                                        <td colspan="3">
                                            <hr class="hrLine"/>
                                        </td>
                                    </tr>
                                </table>
                                <%
                                        n++;
                                    }
                                %>
                            </div>
                        </div>
                    </div>
                    <!--<textarea name="flowstring" style="display:none"><%=wf.getFlowString()%></textarea>-->
                    <input name="returnBack" value="<%=wf.isReturnBack()?"true":"false"%>" type=hidden>
                </td>
            </tr>
        </table>
        
   		 <div id="processBox"></div>
        
        </div>
    </form>
    <br/>
    <div id="dlg" style="display:none"></div>

    <div id="plusDlg" style="display:none">
        <table width="80%" class="tabStyle_1 percent80">
            <tr>
                <td width="23%"><strong>
                    <lt:Label res="res.flow.Flow" key="plusType"/></strong></td>
                <td width="77%">
                    <%if (wf.isStarted()) { %>
                    <input id="plusType" name="plusType" type="radio" checked value="<%=WorkflowActionDb.PLUS_TYPE_BEFORE%>"/>
                    <lt:Label res="res.flow.Flow" key="bplus"/>
                    <%} %>
                    <input id="plusType" name="plusType" type="radio" <%=!wf.isStarted() ? "checked" : "" %> value="<%=WorkflowActionDb.PLUS_TYPE_AFTER%>"/>
                    <lt:Label res="res.flow.Flow" key="aplus"/>
                    <input id="plusType" name="plusType" type="radio" value="<%=WorkflowActionDb.PLUS_TYPE_CONCURRENT%>"/>
                    <lt:Label res="res.flow.Flow" key="cplus"/></td>
            </tr>
            <tr id="plusModeTr">
                <td><strong><lt:Label res="res.flow.Flow" key="approvalType"/></strong></td>
                <td>
                    <input id="plusMode" name="plusMode" type="radio" value="<%=WorkflowActionDb.PLUS_MODE_ORDER%>"/>
                    <lt:Label res="res.flow.Flow" key="approvalOrder"/><br/>
                    <input id="plusMode" name="plusMode" type="radio" value="<%=WorkflowActionDb.PLUS_MODE_ONE%>"/>
                    <lt:Label res="res.flow.Flow" key="approvalDown"/><br/>
                    <input id="plusMode" name="plusMode" type="radio" value="<%=WorkflowActionDb.PLUS_MODE_ALL%>" checked/>
                    <lt:Label res="res.flow.Flow" key="approvalAll"/>
                </td>
            </tr>
            <tr>
                <td><strong><lt:Label res="res.flow.Flow" key="selectPeople"/></strong></td>
                <td>
                    <input name="plusUsers" id="plusUsers" type="hidden" value=""/>
                    <input name="plusUserRealNames" readonly wrap="yes" id="plusUserRealNames"/>
                    <input class="btn" title="<lt:Label res='res.flow.Flow' key='selectUser'/>" onclick="openWin('user_multi_sel.jsp', 900, 730)" type="button" value="<lt:Label res='res.flow.Flow' key='choose'/>"/>
                </td>
            </tr>
        </table>
    </div>
    <div id="toolbar2" style="height:25px; clear:both"></div>
</div>
<%if (lf.isDebug()) {%>
<div class="sidebar">
    <div class="subNav"><h6><strong>调试面板</strong></h6></div>
    <hr/>
    <div class="subNav"><h6>用户：<%=myUser.getRealName()%>&nbsp;&nbsp;
        <a target="_top" href="index.jsp">重新登录</a>
    </h6></div>
    <div class="subNav"><h6><input title="应用可写字段及隐藏字段" onclick="applyProps()" type="button" class="btn btn-default" value="确定"/></h6></div>
    <div class="subNav"><h6>可写字段</h6></div>
    <ul class="navContent">
        <%
            String fieldWrite = "," + StrUtil.getNullString(wa.getFieldWrite()).trim() + ",";
            String fieldHide = "," + StrUtil.getNullString(wa.getFieldHide()).trim() + ",";

            Vector vFields = fd.getFields();
            int formView = wa.getFormView();
            if (formView != WorkflowActionDb.VIEW_DEFAULT) {
                FormViewDb fvd = new FormViewDb();
                fvd = fvd.getFormViewDb(formView);
                String form = fvd.getString("content");
                String ieVersion = fvd.getString("ie_version");
                FormParser fp = new FormParser();
                vFields = fp.parseCtlFromView(form, ieVersion, fd);
            }
            Iterator irFields = vFields.iterator();
            while (irFields.hasNext()) {
                FormField ff = (FormField) irFields.next();
                boolean isWrite = fieldWrite.indexOf("," + ff.getName() + ",") != -1;
        %>
        <li><input name="fieldsWrite" type="checkbox" value="<%=ff.getName() %>" title="<%=ff.getTitle() %>" <%=isWrite ? "checked" : "" %> /><%=ff.getTitle() %>
        </li>
        <%
            }

            String[] fds = StrUtil.getNullString(wa.getFieldWrite()).trim().split(",");
            int len = fds.length;

            // 取出嵌套表
            irFields = vFields.iterator();
            while (irFields.hasNext()) {
                FormField ff = (FormField) irFields.next();
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    // System.out.println(getClass() + " ff.getMacroType()=" + ff.getMacroType());
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    // System.out.println(getClass() + " mu.getNestType()=" + mu.getNestType());
                    if (mu.getNestType() != MacroCtlUnit.NEST_TYPE_NONE) {
                        // if (ff.getMacroType().equals("nest_table") || ff.getMacroType().equals("nest_sheet")) {
                        // String nestFormCode = ff.getDefaultValue();

                        String defaultVal = "";
                        String nestFormCode = ff.getDefaultValue();
                        try {
                            if (mu.getNestType() == MacroCtlUnit.NEST_DETAIL_LIST) {
                                defaultVal = StrUtil.decodeJSON(ff.getDescription());
                            } else {
                                defaultVal = ff.getDescription();
                                if ("".equals(defaultVal)) {
                                    defaultVal = ff.getDefaultValueRaw();
                                }
                                defaultVal = StrUtil.decodeJSON(defaultVal); // ff.getDefaultValueRaw()
                            }
                            // 20131123 fgf 添加
                            JSONObject json = new JSONObject(defaultVal);
                            nestFormCode = json.getString("destForm");
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            System.out.println(getClass() + " title=" + ff.getTitle() + " defaultVal=" + defaultVal);
                            e.printStackTrace();
                            // LogUtil.getLog(getClass()).info(nestFormCode + " is old version before 20131123.");
                        }

                        FormDb nestfd = new FormDb();
                        nestfd = nestfd.getFormDb(nestFormCode);

                        ModuleSetupDb msd = new ModuleSetupDb();
                        msd = msd.getModuleSetupDbOrInit(nestFormCode);

                        // System.out.println(getClass() + " nestFormCode=" + nestFormCode);

                        /// String listField = "," + StrUtil.getNullStr(msd.getString("list_field")) + ",";
                        String[] fields = msd.getColAry(false, "list_field");
                        String listField = "," + StrUtil.getNullStr(StringUtils.join(fields, ",")) + ",";

                        Iterator ir2 = nestfd.getFields().iterator();
                        while (ir2.hasNext()) {
                            FormField ff2 = (FormField) ir2.next();
                            // 判断是否在模块中已设置为显示于列表中
                            if (true || listField.indexOf("," + ff2.getName() + ",") != -1) {
                                String txt = ff2.getTitle() + "(嵌套表-" + nestfd.getName() + ")";
                                // 判断是否已被选中
                                boolean isFinded = false;
                                for (int i = 0; i < len; i++) {
                                    if (("nest." + ff2.getName()).equals(fds[i])) {
                                        isFinded = true;
                                    }
                                }
        %>
        <li><input name="fieldsWrite" type="checkbox" value="nest.<%=ff2.getName() %>" title="<%=ff2.getTitle() %>" <%=isFinded ? "checked" : "" %> /><%=txt %>
        </li>
        <%
                            }
                        }
                        // break;
                    }
                }
            }
        %>
    </ul>
    <div class="subNav"><h6>隐藏字段</h6></div>
    <ul class="navContent">
        <%
            irFields = vFields.iterator();
            while (irFields.hasNext()) {
                FormField ff = (FormField) irFields.next();
                boolean isHide = fieldHide.indexOf("," + ff.getName() + ",") != -1;
        %>
        <li><input name="fieldsHide" type="checkbox" value="<%=ff.getName() %>" title="<%=ff.getTitle() %>" <%=isHide ? "checked" : "" %> /><%=ff.getTitle() %>
        </li>
        <%
            }

            fds = StrUtil.getNullString(wa.getFieldHide()).trim().split(",");
            len = fds.length;
            // 取出嵌套表
            irFields = vFields.iterator();
            while (irFields.hasNext()) {
                FormField ff = (FormField) irFields.next();
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu.getNestType() != MacroCtlUnit.NEST_TYPE_NONE) {
                        String nestFormCode = ff.getDefaultValue();
                        String defaultVal = "";
                        try {
                            if (mu.getNestType() == MacroCtlUnit.NEST_DETAIL_LIST) {
                                defaultVal = StrUtil.decodeJSON(ff.getDescription());
                            } else {
                                defaultVal = ff.getDescription();
                                if ("".equals(defaultVal)) {
                                    defaultVal = ff.getDefaultValueRaw();
                                }
                                defaultVal = StrUtil.decodeJSON(defaultVal); // ff.getDefaultValueRaw()
                            }
                            // 20131123 fgf 添加
                            JSONObject json = new JSONObject(defaultVal);
                            nestFormCode = json.getString("destForm");
                        } catch (JSONException e) {
                            System.out.println(getClass() + " title=" + ff.getTitle() + " " + defaultVal);
                            e.printStackTrace();
                            // LogUtil.getLog(getClass()).info(nestFormCode + " is old version before 20131123.");
                        }

                        FormDb nestfd = new FormDb();
                        nestfd = nestfd.getFormDb(nestFormCode);

                        ModuleSetupDb msd = new ModuleSetupDb();
                        msd = msd.getModuleSetupDbOrInit(nestFormCode);

                        // String listField = "," + StrUtil.getNullStr(msd.getString("list_field")) + ",";
                        String[] fields = msd.getColAry(false, "list_field");
                        String listField = "," + StrUtil.getNullStr(StringUtils.join(fields, ",")) + ",";

                        Iterator ir2 = nestfd.getFields().iterator();
                        while (ir2.hasNext()) {
                            FormField ff2 = (FormField) ir2.next();
                            // 判断是否在模块中已设置为显示于列表中
                            if (true || listField.indexOf("," + ff2.getName() + ",") != -1) {
                                String txt = ff2.getTitle() + "(嵌套表-" + nestfd.getName() + ")";
                                // 判断是否已被选中
                                boolean isFinded = false;
                                for (int i = 0; i < len; i++) {
                                    if (("nest." + ff2.getName()).equals(fds[i])) {
                                        isFinded = true;
                                    }
                                }
        %>
        <li><input name="fieldsHide" type="checkbox" value="nest.<%=ff2.getName() %>" title="<%=ff2.getTitle() %>" <%=isFinded ? "checked" : "" %> /><%=txt %>
        </li>
        <%
                            }
                        }
                        // break;
                    }
                }
            }
        %>
    </ul>
    <%if (License.getInstance().isSrc()) {%>
    <div class="subNav"><h6>验证脚本</h6></div>
    <ul class="navContent">
        <li><input type="button" class="btn btn-default" value="运行" onclick="runValidateScript()"/></li>
        <li>
            <div id="boxValidateResult" class="console">
            </div>
        </li>
    </ul>
    <div class="subNav"><h6>结束脚本</h6></div>
    <ul class="navContent">
        <li><input type="button" class="btn btn-default" value="运行" onclick="runFinishScript()"/></li>
        <li>
            <div id="boxFinishResult" class="console">

            </div>
        </li>
    </ul>
    <div class="subNav"><h6>流转脚本</h6></div>
    <ul class="navContent">
        <li><input type="button" class="btn btn-default" value="运行" onclick="runDeliverScript()"/></li>
        <li>
            <div id="boxDeliverResult" class="console">

            </div>
        </li>
    </ul>
    <%} %>
    </div>
</div>
<script>
    function runDeliverScript() {
        $.ajax({
            type: "post",
            url: "flow/runDeliverScript.do",
            data: {
                myActionId: <%=myActionId%>,
                flowId: <%=flowId%>
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('#bodyBox').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == "1") {
                    $("#boxDeliverResult").html(data.msg);
                } else {
                    $("#boxDeliverResult").html("<font style='color:red'>" + data.msg + "</font>");
                }
            },
            complete: function (XMLHttpRequest, status) {
                $('#bodyBox').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
                $('#bodyBox').hideLoading();
            }
        });
    }

    function runFinishScript() {
        $.ajax({
            type: "post",
            url: "flow/runFinishScript.do",
            data: {
                actionId: <%=actionId%>,
                flowId: <%=flowId%>
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('#bodyBox').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                data.msg += "<br/>事件运行结束";
                if (data.ret == "1") {
                    $("#boxFinishResult").html(data.msg);
                } else {
                    $("#boxFinishResult").html("<font style='color:red'>" + data.msg + "</font>");
                }
            },
            complete: function (XMLHttpRequest, status) {
                $('#bodyBox').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
                $('#bodyBox').hideLoading();
            }
        });
    }

    function runValidateScript() {
        $.ajax({
            type: "post",
            url: "flow/runValidateScript.do",
            data: {
                actionId: <%=actionId%>,
                flowId: <%=flowId%>
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('#bodyBox').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == "1") {
                    $("#boxValidateResult").html(data.msg);
                } else {
                    $("#boxValidateResult").html("<font style='color:red'>" + data.msg + "</font>");
                }
            },
            complete: function (XMLHttpRequest, status) {
                $('#bodyBox').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
                $('#bodyBox').hideLoading();
            }
        });
    }

    function applyProps() {
        var fieldWrite = getCheckboxValue("fieldsWrite");
        var fieldHide = getCheckboxValue("fieldsHide");

        var writes = fieldWrite.split(",");
        var hides = fieldHide.split(",");
        for (var i = 0; i < hides.length; i++) {
            for (var j = 0; j < writes.length; j++) {
                if (hides[i] == writes[j]) {
                    var title = $($("input[value='" + hides[i] + "']")[0]).attr("title");
                    jAlert("出现相同字段：" + title + "，注意可填写字段与隐藏字段不能有重叠！", "提示");
                    return;
                }
            }
        }

        jConfirm('您确定要应用么？', '提示', function (r) {
            if (!r) {
                return;
            } else {
                $.ajax({
                    type: "post",
                    url: "flow/applyProps.do",
                    data: {
                        fieldWrite: fieldWrite,
                        fieldHide: fieldHide,
                        actionId: <%=actionId%>,
                        flowId: <%=flowId%>
                    },
                    dataType: "html",
                    beforeSend: function (XMLHttpRequest) {
                        $('#bodyBox').showLoading();
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        if (data.ret==1) {
                            jAlert(data.msg, "提示", function() {
                                window.location.reload();
                            });
                        }
                        else {
                            jAlert(data.msg, "提示");
                        }
                    },
                    complete: function (XMLHttpRequest, status) {
                        $('#bodyBox').hideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        alert(XMLHttpRequest.responseText);
                        $('#bodyBox').hideLoading();
                    }
                });
            }
        });
    }
</script>
<%} %>

</body>
<link rel="stylesheet" href="js/jquery-contextmenu/jquery.contextMenu.min.css">
<script src="js/jquery-contextmenu/jquery.contextMenu.js"></script>
<script src="js/jquery-contextmenu/jquery.ui.position.min.js"></script>
<script>
    $(document).ready(function () {
        <%if (lf.isDebug()) {%>
        var opener = $("a[title='调试面板']")[0];

        if (opener) {
            opener.id = "mybtn";
        }
        else {
            console.error('开启了调试模式，但是未找到调试面板');
        }

        $('.sidebar').simpleSidebar({
            settings: {
                opener: '#mybtn',
                wrapper: '.wrapper',
                animation: {
                    duration: 0,
                    easing: 'easeOutQuint'
                }
            },
            sidebar: {
                align: 'right',
                width: 300,
                closingLinks: 'a',
            }
        });
        <%}%>
    });

    $(function () {
        $(".subNav").click(function () {
            // 修改数字控制速度， slideUp(500)控制卷起速度
            $(this).next(".navContent").slideToggle(500);
        })
    })


    function getSelUserNames() {
        return o("plusUsers").value;
    }

    function getSelUserRealNames() {
        return o("plusUserRealNames").value;
    }

    function setUsers(users, userRealNames) {
        o("plusUsers").value = users;
        o("plusUserRealNames").value = userRealNames;
    }

    function refreshAttachments() {
        ajaxpage("<%=Global.getFullRootPath(request)%>/flow_dispose_ajax_att.jsp?myActionId=<%=myActionId%>&flowId=<%=flowId%>", "attBox");
        o("netdiskFilesDiv").innerHTML = "";
    }

    var errFunc = function (response) {
        jAlert('Error ' + response.status + ' - ' + response.statusText, '<lt:Label res="res.flow.Flow" key="prompt"/>');
        jAlert(response.responseText, '<lt:Label res="res.flow.Flow" key="prompt"/>');
    }

    function renameAtt(attId) {
        if (o("spanAttName" + attId).innerHTML == o("attName" + attId).value) {
            o("spanRename" + attId).style.display = "";
            o("spanAttNameInput" + attId).style.display = "none";
            o("spanAttLink" + attId).style.display = "";
            return;
        }

        $.ajax({
            type: "post",
            url: "flow/renameAtt.do",
            data: {
                attId: attId,
                newName: o("attName" + attId).value
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == "0") {
                    jAlert(data.msg, "提示");
                } else {
                    o("spanRename" + attId).style.display = "";
                    o("spanAttNameInput" + attId).style.display = "none";
                    o("spanAttName" + attId).innerHTML = o("attName" + attId).value;
                    o("spanAttLink" + attId).style.display = "";
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

    function changeName(attId) {
        o("spanRename" + attId).style.display = "none";
        o("spanAttNameInput" + attId).style.display = "";
        o("spanAttLink" + attId).style.display = "none";
    }

    function writeDoc() {
        openWin("flow/flow_ntko_write_doc.jsp?flowId=<%=flowId%>", 800, 600);
    }

    // 用于自选用户flow_action_modify.jsp选人后置XorActionSelected为checked
    function setXORActionChecked(actionId) {
        var xorObj = document.getElementById("XOR" + actionId);
        if (xorObj == null)
            return;

        xorObj.checked = true;
        $("#dlg input[id=" + "XOR" + actionId + "]").attr("checked", true);
    }

    function checkXOR(chkObj, actionId) {
        var xorObj = document.getElementById("XOR" + actionId);
        if (xorObj == null)
            return;

        if (chkObj.checked) {
            xorObj.checked = true;
            // 下句在IE8（IE6 7未测）下无效
            // $("#XOR" + actionId).attr("checked", true);
            $("#dlg input[id=" + "XOR" + actionId + "]").attr("checked", true);
            return;
        }

        var isAllUnchecked = true;
        /*
        var ary = document.getElementsByName("WorkflowAction_" + actionId);
        for (var i=0;i<ary.length;i++) {
            if (ary[i].checked) {
                isAllUnchecked = false;
                break;
            }
        }
        */
        $("#dlg input[name='" + "WorkflowAction_" + actionId + "']").each(function () {
            // var chked = $(this).checked; // 取到的为undefined
            var chked = $(this)[0].checked;
            chked = "" + chked;
            if (chked == "true") {
                isAllUnchecked = false;
                return false;
            }
        });

        if (isAllUnchecked) {
            xorObj.checked = false;
            // IE11下面无效
            // $("#dlg input:checkbox[id=" + "XOR" + actionId + "]").attr("checked", false);
        }
        // alert(xorObj.id + " -- " + xorObj.outerHTML);
    }

    function hideDesigner() {
        if (o("Designer")) {
            o("Designer").style.width = "0px";
            o("Designer").style.height = "0px";
            o("Designer").style.marginTop = "0px";
        }
    }

    function detectZoom() {
        var ratio = 1,
            screen = window.screen;
        var os = getOS();
        if (os == 1) { // ie
            if (window.devicePixelRatio) {
                ratio = window.devicePixelRatio;
            } else if (screen.deviceXDPI && screen.logicalXDPI) {
                ratio = screen.deviceXDPI / screen.logicalXDPI;
            }
        } else if (os == 3) { // chrome
            ratio = window.top.outerWidth / window.top.innerWidth;
        } else if (window.outerWidth !== undefined && window.innerWidth !== undefined) { // firefox、opera
            if (window.devicePixelRatio) {
                ratio = window.devicePixelRatio;
            } else {
                ratio = window.outerWidth / window.innerWidth;
            }
        }
        ratio = Math.round(ratio * 100) / 100;
        return ratio;
    }

    function ShowDesigner() {
        <%
            if (canUserSeeFlowChart) {
        %>
            addTab('流程图', 'flow_modify_show_designer.jsp?flowId=<%=flowId%>&isShowNav=0');
            return;
        <%
            }
        %>
        /*
        // 如果这样处理，会使得officeedit控件崩溃
        if (o("designerTable").style.display == "") {
            o("designerTable").style.display = "none";
        }
        else {
            o("designerTable").style.display = "";
        }
        */
        if (!o("Designer")) {
            return;
        }

        if (o("Designer").style.width == "0px") {
            <%
            if (canUserSeeFlowImage) {
                // 如果点击速度过快的话，此时流程图还未生成
                String flowImagePath = cfg.get("flowImagePath");
                Date myDate = wf.getMydate();
                Calendar cal = Calendar.getInstance();
                cal.setTime(myDate);
                String year = String.valueOf(cal.get(Calendar.YEAR));
                String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
                String vpath = flowImagePath + "/" + year + "/" + month;
                // 加入rand，以使得每次点击按钮查看流程图时，从服务器再获取一次
            %>
            $('#Designer').html('<img id="flowImage" src="<%=vpath%>/<%=wf.getId()%>.jpg?rand=' + Math.random() + '" style="width:2593px; height:2161px;"/>');
            // 使图片保持原来的尺寸，不受浏览器的缩放影响
            var radio = detectZoom();
            // 如果是chrome且radio小于1
            if (getOS() == 3 && radio < 1) {
                $('#flowImage').width($('#flowImage').width() * radio);
                $('#flowImage').height($('#flowImage').height() * radio);
            } else if (radio > 1) {
                $('#flowImage').width($('#flowImage').width() / radio);
                $('#flowImage').height($('#flowImage').height() / radio);
            }

            o("Designer").style.width = "1000px";
            o("Designer").style.height = "490px";
            <%
            }
            else {
            %>
            o("Designer").style.width = "80%";
            o("Designer").style.height = "515px";
            <%
            }
            %>
            o("Designer").style.marginTop = "10px";
        } else {
            o("Designer").style.width = "0px";
            o("Designer").style.height = "0px";
            o("Designer").style.marginTop = "0px";
        }
    }

    function linkProject() {
        openWin("<%=request.getContextPath()%>/project/project_list_sel.jsp?action=linkProject", 800, 600);
    }

    function unlinkProject() {
        jConfirm('<lt:Label res="res.flow.Flow" key="cancelAssociation"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>', function (r) {
            if (!r)
                return false;
            else {
                $.ajax({
                    type: "post",
                    url: "flow/unlinkProject.do",
                    data: {
                        flowId: <%=wf.getId()%>
                    },
                    dataType: "html",
                    beforeSend: function (XMLHttpRequest) {
                        $('#bodyBox').showLoading();
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        if (data.ret == "1") {
                            jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                            o("projectName").innerHTML = "";
                        } else {
                            jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                        }
                    },
                    complete: function (XMLHttpRequest, status) {
                        $('#bodyBox').hideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        jAlert(XMLHttpRequest.responseText, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                    }
                });
            }
        });
    }

    function doLinkProject(prjId, prjName) {
        $.ajax({
            type: "post",
            url: "flow/linkProject.do",
            data: {
                projectId: prjId,
                myActionId: "<%=myActionId%>",
                flowId: <%=wf.getId()%>
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('#bodyBox').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == "1") {
                    jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                    o("projectName").innerHTML = "<lt:Label res='res.flow.Flow' key='project'/>：<a href=\"javascript:;\" onclick=\"addTab('" + prjName + "', 'project/project_show.jsp?projectId=" + prjId + "&formCode=project')\">" + prjName + "</a>&nbsp;&nbsp;<a title=\"取消关联\" href=\"javascript:;\" onclick=\"unlinkProject()\" style='font-size:16px; font-color:red'>×</a>";
                } else {
                    jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                }
            },
            complete: function (XMLHttpRequest, status) {
                $('#bodyBox').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText, '<lt:Label res="res.flow.Flow" key="prompt"/>');
            }
        });
    }

    function setPerson(deptCode, deptName, user, userRealName) {
        if (userRealName == null || userRealName == "") {
            jAlert('<lt:Label res="res.flow.Flow" key="setTransferPerson"/>');
            return false;
        }

        jConfirm('<lt:Label res="res.flow.Flow" key="assign1"/>' + userRealName + '？', '<lt:Label res="res.flow.Flow" key="prompt"/>', function (r) {
            if (r) {
                o("spanLoad").innerHTML = "<img src='<%=request.getContextPath()%>/inc/ajaxtabs/loading.gif' />";

                $.ajax({
                    type: "post",
                    url: "flow/flow_dispose_ajax.jsp",
                    data: {
                        op: "transfer",
                        toUserName: user,
                        isUseMsg: o("isUseMsg").checked,
                        isToMobile: o("isToMobile") ? o("isToMobile").checked : "false",
                        myActionId: "<%=myActionId%>",
                        cwsWorkflowResult: $("#cwsWorkflowResult").val()
                    },
                    dataType: "html",
                    beforeSend: function (XMLHttpRequest) {
                        $('#bodyBox').showLoading();
                    },
                    success: function (data, status) {
                        o("spanLoad").innerHTML = "";
                        data = $.parseJSON(data);
                        if (data.ret == "0") {
                            jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                        } else {
                            done(data.msg);
                            // jAlert_Redirect(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', "flow/flow_list.jsp?displayMode=1");
                        }
                    },
                    complete: function (XMLHttpRequest, status) {
                        $('#bodyBox').hideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        jAlert(XMLHttpRequest.responseText, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                    }
                });
            }
        });
    }

    function transfer() {
        openWin('user_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>', 800, 600);
    }

    function suspend() {
        jConfirm('<lt:Label res="res.flow.Flow" key="hang"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>', function (r) {
            if (r) {
                o("spanLoad").innerHTML = "<img src='<%=request.getContextPath()%>/inc/ajaxtabs/loading.gif' />";

                $.ajax({
                    type: "post",
                    url: "flow/flow_dispose_ajax.jsp",
                    data: {
                        op: "suspend",
                        myActionId: "<%=myActionId%>"
                    },
                    dataType: "html",
                    beforeSend: function (XMLHttpRequest) {
                        //ShowLoading();
                    },
                    success: function (data, status) {
                        o("spanLoad").innerHTML = "";
                        data = $.parseJSON(data);
                        if (data.ret == "0") {
                            jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                        } else {
                            done(data.msg, true);
                            // jAlert_Redirect(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', "flow/flow_list.jsp?displayMode=1");
                        }
                    },
                    complete: function (XMLHttpRequest, status) {
                        //HideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        jAlert(XMLHttpRequest.responseText, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                    }
                });
            }
        });
    }

    function resume() {
        jConfirm('<lt:Label res="res.flow.Flow" key="restore"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>', function (r) {
            if (r) {
                o("spanLoad").innerHTML = "<img src='<%=request.getContextPath()%>/inc/ajaxtabs/loading.gif' />";

                $.ajax({
                    type: "post",
                    url: "flow/flow_dispose_ajax.jsp",
                    data: {
                        op: "resume",
                        myActionId: "<%=myActionId%>"
                    },
                    dataType: "html",
                    beforeSend: function (XMLHttpRequest) {
                        //ShowLoading();
                    },
                    success: function (data, status) {
                        o("spanLoad").innerHTML = "";
                        data = $.parseJSON(data);
                        if (data.ret == "0") {
                            jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                        } else {
                            jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                            window.location.href = "flow_dispose.jsp?myActionId=" + data.myActionId;
                        }
                    },
                    complete: function (XMLHttpRequest, status) {
                        //HideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        jAlert(XMLHttpRequest.responseText, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                    }
                });
            }
        });
    }

    var tabIdOpener = '<%=ParamUtil.get(request, "tabIdOpener")%>';

    function showResponse(data) {
        // 过滤掉其它字符，只保留JSON字符串
        var m = data.match(/\{.*?\}/gi);
        if (m != null) {
            if (m.length == 1) {
                data = m[0];
            }
        }

        $('#bodyBox').hideLoading();

        try {
            data = jQuery.parseJSON(data);
        } catch (e) {
            jAlert(data, '<lt:Label res="res.flow.Flow" key="prompt"/>');
            // if (op=="finish") {
            toolbar.setDisabled(1, false);
            // }
            return;
        }

        if (data == null)
            return;
        if (data.msg != null)
            data.msg = data.msg.replace(/\\r/ig, "<BR>");

        if (data.ret == "0") {
            // 下面的注释是因为在有条件分支时，可能为op=saveformvalueBeforeXorCondSelect
            // if (data.op=="finish") {
            toolbar.setDisabled(1, false);
            // }
            toolbar.setDisabled(0, false);
            jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
            return;
        }

        if (window.top.mainFrame) {
            // var pos = window.top.mainFrame.getActiveTab().id;
            // window.top.mainFrame.closeTab("待办流程");
            window.top.mainFrame.reloadTab("桌面");
            // window.top.mainFrame.showTab(pos, false);
        } else {
            if (window.top.o("content-main")) {
                reloadTab("0");
            }
        }
        if (tabIdOpener != "") {
            reloadTab(tabIdOpener);
        }

        var op = data.op;
        if (op == "read") {
            <%if (lf.isDebug() && com.redmoon.oa.kernel.License.getInstance().isPlatformSrc()) {%>
            jAlert_Redirect(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', "flow/flow_list_debugger.jsp?myActionId=<%=myActionId%>");
            <%}else{%>
            done(data.msg, true);
            // jAlert_Redirect(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', "flow/flow_list.jsp?displayMode=1");
            <%}%>
            return;
        } else if (op == "saveformvalue") {
            toolbar.setDisabled(0, false);
            jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
            refreshAttachments();
            delAllUploadFile();
            return;
        } else if (op == "AutoSaveArchiveNodeManualFinish") {
            // jAlert_Redirect(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', "flow_dispose.jsp?action=afterAutoSaveArchiveNodeManualFinish&myActionId=<%=myActionId%>");
            // 20200428自动存档已经不需要再回到flow_dispose.jsp页面处理了
            done(data.msg, true);
            return;
        } else if (op == "manualFinish" || op == "manualFinishAgree") {
            done(data.msg, true);
            // jAlert_Redirect(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', "flow/flow_list.jsp?displayMode=1");
        } else if (op == "finish") {
            <%
            String redirectUrl = WorkflowActionDb.getActionProperty(wpd, wa.getInternalName(), "redirectUrl");
            if (redirectUrl!=null && !"".equals(redirectUrl)) {
                if (redirectUrl.indexOf("?")==-1) {
                    redirectUrl += "?";
                }
                else {
                    redirectUrl += "&";
                }
                if (redirectUrl.startsWith("http:")) {

                %>
            jAlert_Redirect(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', "<%=redirectUrl%>myActionId=<%=myActionId%>");
            <%
            }
            else {
            %>
            jAlert_Redirect(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', "<%=request.getContextPath()%>/<%=redirectUrl%>myActionId=<%=myActionId%>");
            <%
            }
        }
        else {
        %>
            var nextMyActionId = data.nextMyActionId;
            if (nextMyActionId != "") {
                jAlert_Redirect(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', "flow_dispose.jsp?myActionId=" + nextMyActionId);
            } else {
                <%if (lf.isDebug() && com.redmoon.oa.kernel.License.getInstance().isPlatformSrc()) {%>
                jAlert_Redirect(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', "flow/flow_list_debugger.jsp?myActionId=<%=myActionId%>");
                <%} else {%>
                // jAlert_Redirect(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', "flow/flow_list.jsp?displayMode=1");
                done(data.msg, false, op);
                <%}%>
            }
            <%}%>
        } else if (op == "return") {
            <%if (lf.isDebug() && com.redmoon.oa.kernel.License.getInstance().isPlatformSrc()) {%>
            jAlert_Redirect(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', "flow/flow_list_debugger.jsp?myActionId=<%=myActionId%>");
            <%} else {%>
            done(data.msg);
            // jAlert_Redirect(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', "flow/flow_list.jsp?displayMode=1");
            <%}%>
        } else if (op == "saveformvalueBeforeXorCondSelect") {
            $("#dlg").html(data.msg);
            // ajax匹配
            $.ajax({
                type: "post",
                url: "flow_dispose_ajax.jsp",
                data: {
                    op: "matchNextBranch",
                    actionId: "<%=mad.getActionId()%>",
                    deptOfUserWithMultiDept: deptOfUserWithMultiDept, // 当前所选的部门
                    myActionId: "<%=mad.getId()%>",
                    askType: 1
                },
                dataType: "html",
                beforeSend: function (XMLHttpRequest) {
                    $('#bodyBox').showLoading();
                },
                success: function (data, status) {
                    hideDesigner();
                    $('#bodyBox').hideLoading();
                    $("#spanNextUser").hide();
                    o("spanNextUser").innerHTML = data;
                    // 判断有没有匹配到人员
                    var hasUserCheckbox = false;
                    $("input:checkbox", o("spanNextUser")).each(function () {
                        if (this.name.indexOf('WorkflowAction_') == 0) {
                            hasUserCheckbox = true;
                            return;
                        }
                    });
                    var isMatchUserException = data.indexOf("isMatchUserException") != -1;
                    // 如果没有匹配到人员且没有“选择用户”按钮
                    if (!hasUserCheckbox && data.indexOf("<%=LocalUtil.LoadString(request,"res.flow.Flow","selectUser")%>") == -1 && !isMatchUserException) {
                        data += "<br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;无需选择用户，请点击确定按钮！";
                    }
                    <%
                    if (isNotShowNextUsers) {
                    %>
                    if (!isMatchUserException) {
                        data = "请点击确定按钮<span style='display:none'>" + data + "</span>";
                    }
                    <%
                    }
                    %>
                    $("#dlg").html(data);
                    $("#dlg").dialog({
                        title: '<lt:Label res="res.flow.Flow" key="conditions"/>',
                        modal: true,
                        // bgiframe:true,
                        buttons: {
                            '<lt:Label res="res.flow.Flow" key="cancel"/>': function () {
                                $(this).dialog("close");
                                toolbar.setDisabled(1, false);
                                $("#spanNextUser").show();
                            },
                            '<lt:Label res="res.flow.Flow" key="sure"/>': function () {
                                // o("spanNextUser").innerHTML = $("#dlg").html();
                                $("#spanNextUser").html($("#dlg").clone().html());
                                // 置选中状态，因为clone不会复制状态
                                $("#dlg").find("input").each(function () {
                                    var obj = $(this);
                                    $("input:checkbox", o("spanNextUser")).each(function () {
                                        if (obj.attr("value") == this.value) {
                                            if (obj.attr("checked") == "checked") {
                                                this.setAttribute("checked", "checked");
                                            }
                                        }
                                    });
                                });

                                var hasUser = false;
                                $("input:checkbox", o("spanNextUser")).each(function () {
                                    if (this.name.indexOf('WorkflowAction_') == 0 && $(this).attr("checked") == "checked") {
                                        hasUser = true;
                                    }
                                });

                                // console.log($("#spanNextUser").html());

                                // $("#dlg").clone()中的DIV含有jquery-ui中的class，界面效果比较难看，所以隐藏掉

                                if (!hasUser) {
                                    // 20161116 fgf 如果下一节点未匹配到人，则可能需跳过，所以不必再次确认
                                    // 未选择用户，确定要提交么？
                                    if (hasUserCheckbox && !confirm('<lt:Label res="res.flow.Flow" key="noUserSelected"/>')) {
                                        return;
                                    }
                                }

                                // 置为空，否则当取XorNextActionInternalNames时，通过getCheckboxValue("XorActionSelected")会出现重复
                                $("#dlg").html('');
                                var isAfterSaveformvalueBeforeXorCondSelect = true;
                                SubmitResult(isAfterSaveformvalueBeforeXorCondSelect);
                                $(this).dialog("close");

                                $('#bodyBox').showLoading();
                            }
                        },
                        closeOnEscape: true,
                        draggable: true,
                        resizable: true,
                        width: 500
                    });
                },
                complete: function (XMLHttpRequest, status) {
                    $('#bodyBox').hideLoading();
                },
                error: function (XMLHttpRequest, textStatus) {
                    // 请求出错处理
                    jAlert(XMLHttpRequest.responseText, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                }
            });
        }

        return;
    }

    function showError(pRequest, pStatus, pErrorText) {
        jAlert('pStatus=' + pStatus + '\r\n\r\n' + 'pErrorText=' + pErrorText + '\r\n\r\npRequest=' + pRequest, '<lt:Label res="res.flow.Flow" key="prompt"/>');
    }

    <%
    boolean isReadOnly = false;
    if (wa.getKind()==WorkflowActionDb.KIND_READ) {
        isReadOnly = true;
    }
    %>

    var isDocDistributed = false;

    <%
    // 检查文件是否已分发
    if (!isReadOnly && wf.isStarted() && wa.isDistribute()) {
        if (mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {
            PaperDistributeDb pdd = new PaperDistributeDb();
            int paperCount = pdd.getCountOfWorkflow(wf.getId());
            if (paperCount > 0) {
                %>
    isDocDistributed = true;
    <%
}
}
}
%>

    <%
    Vector fields = fd.getFields();
    String fieldWrite = StrUtil.getNullString(wa.getFieldWrite()).trim();
    String fieldHide = StrUtil.getNullString(wa.getFieldHide()).trim();

    String[] fds = fieldWrite.split(",");
    int len = fds.length;

    //将不可写的域筛选出
    Iterator fir = fields.iterator();
    while (fir.hasNext()) {
        FormField ff = (FormField) fir.next();

        boolean finded = false;
        for (int i = 0; i < len; i++) {
            if (ff.getName().equals(fds[i])) {
                finded = true;
                break;
            }
        }

        if (!finded) {
            ff.setEditable(false);
        }
    }
    String checkJs = FormUtil.doGetCheckJS(request, fields);
    String checkJsSub = checkJs.substring(9, checkJs.length() - 10);
    %>

    function toolbarSubmit() {
        hideDesigner();
        //var fields = lv_cwsWorkflowResult.formObj.fields;
        // 取消验证
        //LiveValidation.cancelValidate(lv_cwsWorkflowResult.formObj.fields);
        //lv_cwsWorkflowResult.formObj.fields.destory();

        try {
            ctlOnBeforeSerialize();
        } catch (e) {
        }

        // hw 20160616 要摧毁校验，包括他不允许为空的*以及错误提示都需要摧毁，再实例化lv_cwsWorkflowResult，再调用FormUtil.doGetCheckJS
        // FormUtil.doGetCheckJS也要注意，因为它是script标签的，所以一定得去除标签
        LiveValidation.destroyValidate(lv_cwsWorkflowResult.formObj.fields);
        $(".LV_presence").remove();
        lv_cwsWorkflowResult = new LiveValidation('cwsWorkflowResult');
        //$(".LV_validation_message").remove();
        <%out.print(checkJsSub);%>
        if (!LiveValidation.massValidate(lv_cwsWorkflowResult.formObj.fields)) {
            // jAlert('<lt:Label res="res.flow.Flow" key="checkForm"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>');
            jAlert(LiveValidation.liveErrMsg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
            return;
        }

        var isConfirmed = false;
        if (o("cwsWorkflowTitle") && o("cwsWorkflowTitle").value == "<%=lf.getName()%>") {
            /*
            if (!confirm("流程标题为默认标题，您确定不修改就提交么？")) {
                return;
            }
            else
              isConfirmed = true;
            */
        }

        <%
        if (wr.canUserStartFlow(request, wf)) {%>
        if (!isConfirmed) {
            jConfirm('<lt:Label res="res.flow.Flow" key="startFlow"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>', function (r) {
                if (!r) {
                    return;
                } else {
                    toolbar.setDisabled(1, true);
                    $('#bodyBox').showLoading();
                    SubmitResult();
                }
            })
        }
        <%}else{%>
        if (!isConfirmed) {
            jConfirm('<lt:Label res="res.flow.Flow" key="submitForm"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>', function (r) {
                if (!r) {
                    return;
                } else {
                    toolbar.setDisabled(1, true);
                    $('#bodyBox').showLoading();
                    SubmitResult();
                }
            })
        }
        <%}%>
    }

    var lv_title = new LiveValidation('cwsWorkflowTitle');
    lv_title.add(Validate.Presence, {failureMessage: '<lt:Label res="res.flow.Flow" key="writeTitle"/>'});

    // 用于massValidate检查表单内容
    var lv_cwsWorkflowResult = new LiveValidation('cwsWorkflowResult');

    function initToolbar(toolbarId) {
      // 不能在ready中初始化toolbar，因为有时onload时间可能比较长，会致toolbar要过很长时间才能显示
      toolbar = new Toolbar({
        renderTo : toolbarId,
        // border: 'top',
        items : [
        <%if (!isReadOnly) {%>
            <% if (conf.getIsDisplay("FLOW_BUTTON_SAVE")){%>
            {
              type : 'button',
              text : '<%=conf.getBtnName("FLOW_BUTTON_SAVE").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","saveDraft"):conf.getBtnName("FLOW_BUTTON_SAVE")%>',
              title: '<%=conf.getBtnTitle("FLOW_BUTTON_SAVE").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","noSubmit"):conf.getBtnTitle("FLOW_BUTTON_SAVE")%>',

              bodyStyle : 'save',
              useable : 'T',
              handler : function(){
                saveDraft();
                return false;
              }
            }
            <%}%>
            <%if (mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {%>
            ,'-', {
              type : 'button',
              <%
              // 取得节点上设置的同意按钮名称
              String btnAgreeName = WorkflowActionDb.getActionProperty(wpd, wa.getInternalName(), "btnAgreeName");
              if (wf.isStarted()) {%>
                <%if (conf.getIsDisplay("FLOW_BUTTON_AGREE")) {
                    String textName = "";
                    if (btnAgreeName!=null && !"".equals(btnAgreeName)) {
                        textName = btnAgreeName;
                    }
                    else {
                        textName = conf.getBtnName("FLOW_BUTTON_AGREE").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","agree"):conf.getBtnName("FLOW_BUTTON_AGREE");
                    }
                %>
                    text : '<%=textName%>',
                <%}
              }else{%>
                  <%if (conf.getIsDisplay("FLOW_BUTTON_COMMIT")) {
                    String textName = "";
                    if (btnAgreeName!=null && !"".equals(btnAgreeName)) {
                        textName = btnAgreeName;
                    }
                    else {
                        textName = conf.getBtnName("FLOW_BUTTON_COMMIT").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","submit"):conf.getBtnName("FLOW_BUTTON_COMMIT");
                    }
                  %>
                    text : '<%=textName%>',
                  <%}
              }%>
              title: '<%=conf.getBtnTitle("FLOW_BUTTON_AGREE").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","nextNode"):conf.getBtnTitle("FLOW_BUTTON_AGREE")%>',
              bodyStyle : 'commit',
              useable : 'T',
              handler : function() {
                  toolbarSubmit();
                  return false;
              }
            }
            <%}%>
        <%} else {%>
            <%if(conf.getIsDisplay("FLOW_BUTTON_CHECK")){%>
            {
              type : 'button',
              text : '<%=conf.getBtnName("FLOW_BUTTON_CHECK").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","review"):conf.getBtnName("FLOW_BUTTON_CHECK")%>',
              title: '<%=conf.getBtnTitle("FLOW_BUTTON_CHECK").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","review"):conf.getBtnTitle("FLOW_BUTTON_CHECK")%>',
              bodyStyle : 'commit',
              useable : 'T',
              handler : function(){
                  hideDesigner();
                  toolbar.setDisabled(0, true);
                  $('#bodyBox').showLoading();
                  read();
                  return false;
              }
            }
        <%
            }
        }

        if (conf.getIsDisplay("FLOW_BUTTON_PLUS") && wpd.isPlus()){
            if (!isReadOnly && wa.getPlus().equals("")) {
                if (mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {%>
                ,'-',{
                  type : 'button',
                  text : '<%=conf.getBtnName("FLOW_BUTTON_PLUS").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","plus"):conf.getBtnName("FLOW_BUTTON_PLUS")%>',
                  title: '<%=conf.getBtnTitle("FLOW_BUTTON_PLUS").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","plus"):conf.getBtnTitle("FLOW_BUTTON_PLUS")%>',
                  bodyStyle : 'plus',
                  useable : 'T',
                  handler : function() {
                      hideDesigner();
                      addPlus();
                  }
                }
                <%
                }
                %>
        <%
            }
        }

        if(conf.getIsDisplay("FLOW_BUTTON_RETURN")){
            if (!isReadOnly && wa.isStart!=1) {
                // 加签时不允许返回，否则可能流程可能会进行不下去 fgf 20161007
                if (mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND && mad.getActionStatus() != WorkflowActionDb.STATE_PLUS) {
                    if (returnv.size()>0 || wfp.getReturnStyle()==WorkflowPredefineDb.RETURN_STYLE_FREE) {%>
        ,'-',{
          type : 'button',
          text : '<%=conf.getBtnName("FLOW_BUTTON_RETURN").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","back"):conf.getBtnName("FLOW_BUTTON_RETURN")%>',
          title: '<%=conf.getBtnTitle("FLOW_BUTTON_RETURN").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","backNode"):conf.getBtnTitle("FLOW_BUTTON_RETURN")%>',
          bodyStyle : 'return',
          useable : 'T',
          handler : function(){
              hideDesigner();
              returnFlow();
          }
        }
        <%
                    }
                }
            }
        }

        // 如果该节点是异或节点，则如果其后续相邻节点中有已完成的节点，说明该节点曾被激活过，那么用户可以选择不往下继续
        if(conf.getIsDisplay("FLOW_BUTTON_NOCOMMIT")){
            if (!isReadOnly && wa.isXorAggregate()) {
                int accessedCount = wa.linkedFromActionsAccessedCount();
                if (accessedCount>=2) {
                    if (mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {%>
        ,'-',{
          type : 'button',
          text : '<%=conf.getBtnName("FLOW_BUTTON_NOCOMMIT").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","completed"):conf.getBtnName("FLOW_BUTTON_NOCOMMIT")%>',
          title: '<%=conf.getBtnTitle("FLOW_BUTTON_NOCOMMIT").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","noNextNode"):conf.getBtnTitle("FLOW_BUTTON_NOCOMMIT")%>',
          bodyStyle : 'role-setup',
          useable : 'T',
          handler : function(){
              hideDesigner();
              jConfirm('<lt:Label res="res.flow.Flow" key="isCompleted"/>','<lt:Label res="res.flow.Flow" key="prompt"/>',function(r){
                if(!r){return;}
                else{
                    SubmitNotDelive();
                }
              })
          }
        }
        <%
                    }
                }
            }
        }

        if(conf.getIsDisplay("FLOW_BUTTON_PROCESS")){
            if (wf.isStarted() && !lf.isDebug()) {%>
        ,'-',{
          type : 'button',
          text : '<%=conf.getBtnName("FLOW_BUTTON_PROCESS").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","process"):conf.getBtnName("FLOW_BUTTON_PROCESS")%>',
          title: '<%=conf.getBtnTitle("FLOW_BUTTON_PROCESS").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","showFlow"):conf.getBtnTitle("FLOW_BUTTON_PROCESS")%>',
          bodyStyle : 'process',
          useable : 'T',
          handler : function(){
            window.location.href='flow_modify.jsp?flowId=<%=wf.getId()%>';
          }
        }
        <%
            }
        }

        if(!"".equals(wfp.getDirCode()) && conf.getIsDisplay("FLOW_BUTTON_ALTER")){
            if (wf.isStarted() && !lf.isDebug()) {
        %>
        ,'-',{
            type : 'button',
            text : '<%=conf.getBtnName("FLOW_BUTTON_ALTER").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","alter"):conf.getBtnName("FLOW_BUTTON_ALTER")%>',
            title: '<%=conf.getBtnTitle("FLOW_BUTTON_ALTER").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","alterTitle"):conf.getBtnTitle("FLOW_BUTTON_ALTER")%>',
            bodyStyle : 'alter',
            useable : 'T',
            handler : function() {
                addTab('<%=LocalUtil.LoadString(request,"res.flow.Flow","alterTitle")%>', '<%=request.getContextPath()%>/flow/flow_doc_list.jsp?flowId=<%=wf.getId()%>');
            }
        }
        <%
            }
        }

        if (conf.getIsDisplay("FLOW_BUTTON_DOC")){
            if (!isReadOnly) {
                if (mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {
                %>
        ,'-',{
          type : 'button',
          text : '<%=conf.getBtnName("FLOW_BUTTON_DOC").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","proposedText"):conf.getBtnName("FLOW_BUTTON_DOC")%>',
          title: '<%=conf.getBtnTitle("FLOW_BUTTON_DOC").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","work"):conf.getBtnTitle("FLOW_BUTTON_DOC")%>',
          bodyStyle : 'doc',
          useable : 'T',
          handler: function(){
            writeDoc();
          }
        }
        <%
                }
            }
        }

        if(conf.getIsDisplay("FLOW_BUTTON_DISAGREE")){
            if (!isReadOnly && wf.isStarted() && flag.length()>=9 && flag.substring(8, 9).equals("1")) {
                if (mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {%>
                ,'-',{
                  type : 'button',
                <%
                String btnRefuseName = WorkflowActionDb.getActionProperty(wpd, wa.getInternalName(), "btnRefuseName");
                if (btnRefuseName!=null && !"".equals(btnRefuseName)) {
                %>
                    text : '<%=btnRefuseName%>',
                <%
                }
                else {
                %>
                    text : '<%=conf.getBtnName("FLOW_BUTTON_DISAGREE").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","refuse"):conf.getBtnName("FLOW_BUTTON_DISAGREE")%>',
                <%
                }
                %>
                  title: '<%=conf.getBtnTitle("FLOW_BUTTON_DISAGREE").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","refusedAndEndFlow"):conf.getBtnTitle("FLOW_BUTTON_DISAGREE")%>',
                  bodyStyle : 'disagree',
                  useable : 'T',
                  handler : function() {
                    jConfirm('<lt:Label res="res.flow.Flow" key="endFlow"/>','<lt:Label res="res.flow.Flow" key="prompt"/>',function(r){
                        if(!r){return;}
                        else{
                            // 退回时验证数据合法性
                            try {
                                // 在form_js_formCode.jsp中写此方法
                                var r = checkOnRefuse();
                                if (r!="") {
                                    jAlert(r, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                                    return;
                                }
                            }
                            catch (e) {}

                            hideDesigner();
                            manualFinish();
                        }
                    })
                  }
                }
                <%
                }
            }
        }

        if(conf.getIsDisplay("FLOW_BUTTON_FINISH")){
            if (!isReadOnly && wf.isStarted() && flag.length()>=12 && flag.substring(11, 12).equals("1")) {
                if (mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {%>
                ,'-',{
                  type : 'button',
                  text : '<%=conf.getBtnName("FLOW_BUTTON_FINISH").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","finish"):conf.getBtnName("FLOW_BUTTON_FINISH")%>',
                  title: '<%=conf.getBtnTitle("FLOW_BUTTON_FINISH").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","finishAndAgree"):conf.getBtnTitle("FLOW_BUTTON_FINISH")%>',
                  bodyStyle : 'finish',
                  useable : 'T',
                  handler : function() {
                    jConfirm('<lt:Label res="res.flow.Flow" key="endFlowAgree"/>','<lt:Label res="res.flow.Flow" key="prompt"/>',function(r){
                        if(!r){return;}
                        else{
                            hideDesigner();
                            manualFinishAgree();
                        }
                    })
                  }
                }
                <%
                }
            }
        }

        if (!isReadOnly && wf.isStarted() && !lf.isDebug()) {
            if (mad.getCheckStatus()==MyActionDb.CHECK_STATUS_SUSPEND) {
            if(conf.getIsDisplay("FLOW_BUTTON_RESUME")){
            %>
        ,'-',{
          type : 'button',
          text : '<%=conf.getBtnName("FLOW_BUTTON_RESUME").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","recovery"):conf.getBtnName("FLOW_BUTTON_RESUME")%>',
          title: '<%=conf.getBtnTitle("FLOW_BUTTON_RESUME").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","recoveryProcess"):conf.getBtnTitle("FLOW_BUTTON_RESUME")%>',
          bodyStyle : 'suspend',
          useable : 'T',
          handler : function(){
              hideDesigner();
              resume();
          }
        }
        <%
            }
        }else{
            if (conf.getIsDisplay("FLOW_BUTTON_SUSPEND")){
        %>
        ,'-',{
          type : 'button',
          text : '<%=conf.getBtnName("FLOW_BUTTON_SUSPEND").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","hangUp"):conf.getBtnName("FLOW_BUTTON_SUSPEND")%>',
          title: '<%=conf.getBtnTitle("FLOW_BUTTON_SUSPEND").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","hang"):conf.getBtnTitle("FLOW_BUTTON_SUSPEND")%>',
          bodyStyle : 'suspend',
          useable : 'T',
          handler : function(){
             hideDesigner();
             suspend();
          }
        }
        <%
            }
        }
        }

        if(conf.getIsDisplay("FLOW_BUTTON_TRANSFER") && wpd.isTransfer()){
            if (!isReadOnly && wf.isStarted() && !lf.isDebug()) {
                if (mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {%>
        ,'-',{
          type : 'button',
          text : '<%=conf.getBtnName("FLOW_BUTTON_TRANSFER").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","assign"):conf.getBtnName("FLOW_BUTTON_TRANSFER")%>',
          title: '<%=conf.getBtnTitle("FLOW_BUTTON_TRANSFER").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","assignUser"):conf.getBtnTitle("FLOW_BUTTON_TRANSFER")%>',
          bodyStyle : 'transfer',
          useable : 'T',
          handler : function(){
              hideDesigner();
              transfer();
          }
        }
        <%
                }
            }
        }

        if(conf.getIsDisplay("FLOW_BUTTON_DIRECT")){
            if (!isReadOnly && mad.getActionStatus()==WorkflowActionDb.STATE_RETURN && wfp.getReturnMode()==WorkflowPredefineDb.RETURN_MODE_TO_RETURNER) {
                if (mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {%>
        ,'-',{
          type : 'button',
          text : '<%=conf.getBtnName("FLOW_BUTTON_DIRECT").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","direct"):conf.getBtnName("FLOW_BUTTON_DIRECT")%>',
          title: '<%=conf.getBtnTitle("FLOW_BUTTON_DIRECT").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","directBack"):conf.getBtnTitle("FLOW_BUTTON_DIRECT")%>',
          bodyStyle : 'toRetuner',
          useable : 'T',
          handler : function(){
            jConfirm('<lt:Label res="res.flow.Flow" key="isDirectBack"/>','<lt:Label res="res.flow.Flow" key="prompt"/>',function(r){
                if(!r){return;}
                else{
                    o("flowAction").value = "toRetuner";
                    SubmitResult();
                }
            })
          }
        }
        <%
                }
            }
        }

        if (conf.getIsDisplay("FLOW_BUTTON_DISCARD")){
            if (!isReadOnly && flag.length()>=3 && flag.substring(2, 3).equals("1")) {
                if (mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {%>
        ,'-',{
          type : 'button',
          text : '<%=conf.getBtnName("FLOW_BUTTON_DISCARD").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","discard"):conf.getBtnName("FLOW_BUTTON_DISCARD")%>',
          title: '<%=conf.getBtnTitle("FLOW_BUTTON_DISCARD").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","discardFlow"):conf.getBtnTitle("FLOW_BUTTON_DISCARD")%>',
          bodyStyle : 'discard',
          useable : 'T',
          handler : function(){
            jConfirm('<lt:Label res="res.flow.Flow" key="isDiscard"/>','<lt:Label res="res.flow.Flow" key="prompt"/>',function(r){
                if(!r){return;}
                else{
                    window.location.href='flow_dispose.jsp?op=discard&myActionId=<%=myActionId%>';
                }
            })
          }
        }
        <%
                }
            }
        }

        if(conf.getIsDisplay("FLOW_BUTTON_DEL") && !isReadOnly) {
            if (WorkflowMgr.canDelFlowOnAction(request, wf, wa, mad)) {
                if (mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {%>
        ,'-',{
          type : 'button',
          text : '<%=conf.getBtnName("FLOW_BUTTON_DEL").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","delete"):conf.getBtnName("FLOW_BUTTON_DEL")%>',
          title: '<%=conf.getBtnTitle("FLOW_BUTTON_DEL").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","deleteFlow"):conf.getBtnTitle("FLOW_BUTTON_DEL")%>',
          bodyStyle : 'del',
          useable : 'T',
          handler : function(){
            jConfirm('<lt:Label res="res.flow.Flow" key="isDeleteFlow"/>','<lt:Label res="res.flow.Flow" key="prompt"/>',function(r){
                if(!r){return;}
                else{
                    window.location.href='flow_del.jsp?flow_id=<%=flowId%>';
                }
            })
          }
        }
        <%
                }
            }
        }

        if(conf.getIsDisplay("FLOW_BUTTON_ARCHIVE")){
            if (!isReadOnly && wf.isStarted() && flag.length()>=5) {
                if (mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {
                    if (wa.isArchiveManual()) {%>
        ,'-',{
          type : 'button',
          text : '<%=conf.getBtnName("FLOW_BUTTON_ARCHIVE").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","archive"):conf.getBtnName("FLOW_BUTTON_ARCHIVE")%>',
          title: '<%=conf.getBtnTitle("FLOW_BUTTON_ARCHIVE").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","archiveFile"):conf.getBtnTitle("FLOW_BUTTON_ARCHIVE")%>',
          bodyStyle : 'archive',
          useable : 'T',
          handler : function(){
            hideDesigner();

            saveArchive(<%=wf.getId()%>, <%=wa.getId()%>);
          }
        }
        <%
                }else if (flag.substring(4, 5).equals("3")){%>
        ,'-',{
          type : 'button',
          text : '<%=conf.getBtnName("FLOW_BUTTON_ARCHIVE").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","archive"):conf.getBtnName("FLOW_BUTTON_ARCHIVE")%>',
          title: '<lt:Label res="res.flow.Flow" key="archiveForm"/>',
          bodyStyle : 'archive',
          useable : 'T',
          handler : function(){
            hideDesigner();

            saveArchiveGov(<%=wf.getId()%>, <%=wa.getId()%>);
          }
        }
        <%
                    }
                }
            }
        }

        if(conf.getIsDisplay("FLOW_BUTTON_DISTRIBUTE")){
            if (!isReadOnly && (wfp.isDistribute() ||wa.isDistribute())) {
                if (mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {%>
        ,'-',{
          type : 'button',
            <%
            if (!conf.getBtnName("FLOW_BUTTON_DISTRIBUTE").startsWith("#")) {
            %>
              text : '<%=conf.getBtnName("FLOW_BUTTON_DISTRIBUTE")%>',
              title: '<%=conf.getBtnTitle("FLOW_BUTTON_DISTRIBUTE")%>',
            <%}
            else {
                String kind = License.getInstance().getKind();
                if (kind.equalsIgnoreCase(License.KIND_COM)) {
                    %>
                    text : '<%=LocalUtil.LoadString(request,"res.flow.Flow","notify")%>',
                    title: '<%=LocalUtil.LoadString(request,"res.flow.Flow","notify")%>',
                    <%
                }
                else {
                    %>
                    text : '<%=LocalUtil.LoadString(request,"res.flow.Flow","distribute")%>',
                    title: '<%=LocalUtil.LoadString(request,"res.flow.Flow","fileDistribute")%>',
                    <%
                }
            }
          %>
          bodyStyle : 'distribute',
          useable : 'T',
          handler : function() {
            hideDesigner();
            distributeDoc(<%=wf.getId()%>);
          }
        }
        <%
                }
            }
        }
        if(conf.getIsDisplay("FLOW_BUTTON_CHART")){
            if (canUserSeeFlowImage || canUserSeeDesignerWhenDispose.equals("true") || canUserSeeFlowChart) {%>
        ,'-',{
          type : 'button',
          text : '<%=conf.getBtnName("FLOW_BUTTON_CHART").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","flowChart"):conf.getBtnName("FLOW_BUTTON_CHART")%>',
          title: '<%=conf.getBtnTitle("FLOW_BUTTON_CHART").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","showFlowChart"):conf.getBtnTitle("FLOW_BUTTON_CHART")%>',
          bodyStyle : 'chart',
          useable : 'T',
          handler : function(){
              ShowDesigner();
          }
        }
        <%
            }
        }
        if (conf.getIsDisplay("FLOW_BUTTON_LINKPROJECT")){
            if (com.redmoon.oa.kernel.License.getInstance().isPlatform()) {%>
        ,'-',{
          type : 'button',
          text : '<%=conf.getBtnName("FLOW_BUTTON_LINKPROJECT").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","connProject"):conf.getBtnName("FLOW_BUTTON_LINKPROJECT")%>',
          title: '<%=conf.getBtnTitle("FLOW_BUTTON_LINKPROJECT").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","connProject"):conf.getBtnTitle("FLOW_BUTTON_LINKPROJECT")%>',
          bodyStyle : 'linkProject',
          useable : 'T',
          handler : function(){
              linkProject();
          }
        }
        <%
            }
        }

        if (conf.getIsDisplay("FLOW_BUTTON_NETDISKFILE") && !lf.isDebug()){%>
        ,'-',{
          type : 'button',
          text : '<%=conf.getBtnName("FLOW_BUTTON_NETDISKFILE").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","cloud"):conf.getBtnName("FLOW_BUTTON_NETDISKFILE")%>',
          title: '<%=conf.getBtnTitle("FLOW_BUTTON_NETDISKFILE").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","cloud"):conf.getBtnTitle("FLOW_BUTTON_NETDISKFILE")%>',
          bodyStyle : 'netdiskfile',
          useable : 'T',
          handler : function(){
              selectNetdiskFile();
          }
        }
        <%
        }
        if (!lf.isDebug() && conf.getIsDisplay("FLOW_BUTTON_ATTENTION")){%>
        ,'-',
            <%
            WorkflowFavoriteDb wffd = new WorkflowFavoriteDb();
            if (!wffd.isExist(myname, (long) flowId)){%>
            {
              type : 'button',
              text : '<%=conf.getBtnName("FLOW_BUTTON_ATTENTION").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","attention"):conf.getBtnName("FLOW_BUTTON_ATTENTION")%>',
              title: '<%=conf.getBtnTitle("FLOW_BUTTON_ATTENTION").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","attention"):conf.getBtnTitle("FLOW_BUTTON_ATTENTION")%>',
              bodyStyle : 'attention',
              useable : 'T',
              handler : function(){
                  setAttention(true);
              }
            }
            <%} else {%>
            {
              type : 'button',
              text : '<%=conf.getBtnName("FLOW_BUTTON_CANCEL_ATTENTION").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","cancelAttention"):conf.getBtnName("FLOW_BUTTON_CANCEL_ATTENTION")%>',
              title: '<%=conf.getBtnTitle("FLOW_BUTTON_CANCEL_ATTENTION").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","cancelAttention"):conf.getBtnTitle("FLOW_BUTTON_CANCEL_ATTENTION")%>',
              bodyStyle : 'cancelAttention',
              useable : 'T',
              handler : function(){
                  setAttention(false);
              }
            }
            <%
            }
        }

        String tester = (String)Privilege.getAttribute(request, Privilege.SESSION_OA_FLOW_TESTER); // 流程测试员
        if (tester==null && privilege.isUserPrivValid(request, "admin")) {
            %>
               ,'-',{
               type : 'button',
                text : '管理',
                title: '管理流程',
                bodyStyle : 'manage',
                useable : 'T',
                handler : function() {
                    var title = '<%=wpd.getTitle()%>';
                    addTab(title, '<%=request.getContextPath()%>/admin/flow_predefine_frame.jsp?flowTypeCode=<%=wpd.getTypeCode()%>');
                }
              }
            <%
            }

            if (lf.isDebug() && com.redmoon.oa.kernel.License.getInstance().isPlatformSrc()) {%>
               ,'-',{
               type : 'button',
                text : '调试',
                title: '调试面板',
                bodyStyle : 'debug',
                useable : 'T',
                handler : function() {

                }
              }
          <%}%>
        ]
      });
      toolbar.render();
    }

    // 放在$(function...)中时，在ie8下某些时候会出现工具条出不来的情况
    initToolbar('toolbar');
    $(function () {
        // 可以在底部再放一个工具条
        // initToolbar('toolbar2');
    });

    // ajaxForm序列化提交数据之前的回调函数
    function onBeforeSerialize() {
        try {
            ctlOnBeforeSerialize();
        } catch (e) {
        }
    }

    // ajaxSubmit存在bug，当提交后，遇到提示”请先套红“，再提交时表单会被提交两次，出现两个附件，独立使用$(this).ajaxSubmit(options)问题依然存在，且仅当提交文件时才存在
    // 经检测jquery.form.js升级到新版(3.48.0)也同样存在此问题，且每次带文件提交时，jquery.form.js中存储的数据是被清空了的，因而不是数据bug问题，而是事件问题
    var lastSubmitTime = new Date().getTime();
    $('#flowForm').submit(function () {
        // 通过判断时间，禁多次重复提交
        var curSubmitTime = new Date().getTime();
        // 在0.5秒内的点击视为连续提交两次，实际当出现重复提交时，测试时间差为0
        if (curSubmitTime - lastSubmitTime < 500) {
            lastSubmitTime = curSubmitTime;
            // alert(curSubmitTime - lastSubmitTime);
            $('#bodyBox').hideLoading();
            return false;
        } else {
            lastSubmitTime = curSubmitTime;
        }

        var options = {
            beforeSerialize: onBeforeSerialize,
            success: showResponse,  // post-submit callback
            error: showError,
            dataType: 'text'   // 'xml', 'script', or 'json' (expected server response type)  表单为multipart/form-data即上传文件时，json无法解析
        };
        $(this).ajaxSubmit(options);
        return false;
    });


    // 如果不注释掉，则当直接在桌面点击时，无法载入
    // $(document).ready(function() {
    refreshAttachments();

    // });

    function selectNetdiskFile() {
        openWin('netdisk/clouddisk_list.jsp?mode=select', 850, 600);
    }

    function setAttention(isAttention) {
        var page = isAttention ? "favorite.do" : "unfavorite.do";
        $.ajax({
            type: "post",
            url: "flow/" + page,
            data: {
                flowId: "<%=flowId%>"
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('#bodyBox').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == "0") {
                    jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                } else {
                    //jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                    $.toaster({priority: 'info', message: data.msg});
                    if (isAttention) {
                        $('.attention').html('<%=conf.getBtnName("FLOW_BUTTON_CANCEL_ATTENTION").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","cancelAttention"):conf.getBtnName("FLOW_BUTTON_CANCEL_ATTENTION")%>');
                        $('.attention').attr('title', '<%=conf.getBtnTitle("FLOW_BUTTON_CANCEL_ATTENTION").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","cancelAttention"):conf.getBtnTitle("FLOW_BUTTON_CANCEL_ATTENTION")%>');
                        $('.attention').attr('class', 'cancelAttention');
                        $('.cancelAttention').unbind().bind('click', function () {
                            setAttention(false);
                        });
                    } else {
                        $('.cancelAttention').html('<%=conf.getBtnName("FLOW_BUTTON_ATTENTION").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","attention"):conf.getBtnName("FLOW_BUTTON_ATTENTION")%>');
                        $('.cancelAttention').attr('title', '<%=conf.getBtnTitle("FLOW_BUTTON_ATTENTION").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","attention"):conf.getBtnTitle("FLOW_BUTTON_ATTENTION")%>');
                        $('.cancelAttention').attr('class', 'attention');
                        $('.attention').unbind().bind('click', function () {
                            setAttention(true);
                        });
                    }
                }
            },
            complete: function (XMLHttpRequest, status) {
                $('#bodyBox').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                jAlert(XMLHttpRequest.responseText, '<lt:Label res="res.flow.Flow" key="prompt"/>');
            }
        });
    }

    function delAtt(docId, attId, fieldName) {
        jConfirm('<lt:Label res="res.flow.Flow" key="isDelete"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>', function (r) {
            if (!r) {
                return;
            } else {
                $.getJSON('flow_dispose_ajax_att.jsp',
                    {
                        "op": "delAttach",
                        "myActionId":<%=myActionId%>,
                        "flowId":<%=flowId%>,
                        "doc_id": docId,
                        "attach_id": attId
                    },
                    function (data) {
                        if (data.re == "true") {
                            jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                            $('#trAtt' + attId).remove();
                            if (fieldName != null) {
                                $('#helper_' + fieldName).remove();
                            }
                        } else {
                            jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                        }

                    });
            }
        })
    }

    $("input[name='plusType']").click(function () {
        if ($(this).val() == "<%=WorkflowActionDb.PLUS_TYPE_CONCURRENT%>") {
            $("#plusModeTr").hide();
            /*
            $("input[name='plusMode']").each(function () {
                $(this).attr("disabled", true);
            });
            */
        } else {
            $("#plusModeTr").show();
            /*
            $("input[name='plusMode']").each(function () {
                $(this).removeAttr("disabled");
            });
            */
        }
    });

    function addPlus() {
        $("#plusDlg").dialog({
            title: '<lt:Label res="res.flow.Flow" key="plusPeople"/>',
            modal: true,
            width: 500,
            height: 260,
            // bgiframe:true,
            buttons: {
                '<lt:Label res="res.flow.Flow" key="cancel"/>': function () {
                    $(this).dialog("close");
                },
                '<lt:Label res="res.flow.Flow" key="sure"/>': function () {
                    if ($("#plusUsers").val() == "") {
                        jAlert('<lt:Label res="res.flow.Flow" key="plusPeople"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>');
                        return;
                    }
                    $.ajax({
                        type: "post",
                        url: "flow/flow_dispose_ajax.jsp",
                        data: {
                            op: "plus",
                            myActionId: "<%=myActionId%>",
                            users: o("plusUsers").value,
                            type: $("input[name='plusType']:checked").val(),
                            mode: $("input[name='plusMode']:checked").val(),
                            cwsWorkflowResult: o("cwsWorkflowResult").value,
                            actionId: "<%=actionId%>"
                        },
                        dataType: "html",
                        beforeSend: function (XMLHttpRequest) {
                            $('#bodyBox').showLoading();
                        },
                        success: function (data, status) {
                            data = $.parseJSON(data);
                            if (data.ret == "0") {
                                jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                            } else {
                                if (data.type == "<%=WorkflowActionDb.PLUS_TYPE_BEFORE%>") {
                                    done(data.msg, true);
                                    // jAlert_Redirect(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', "flow/flow_list.jsp?displayMode=1");
                                } else {
                                    jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                                }
                            }
                        },
                        complete: function (XMLHttpRequest, status) {
                            $('#bodyBox').hideLoading();
                        },
                        error: function (XMLHttpRequest, textStatus) {
                            // 请求出错处理
                            jAlert(XMLHttpRequest.responseText, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                        }
                    });

                    $(this).dialog("close");
                }
            },
            closeOnEscape: true,
            draggable: true,
            resizable: true
        });
    }

    // 记录当前所选的部门（当存在兼职的情况时），用于当存在条件分支时，还需再machNextBrach，此时还需要调用flow_dispose_ajax.jsp，
    // 还会调用matchActionUser，缺了此参数，会报“请选择您所在的部门”异常
    var deptOfUserWithMultiDept;

    // 当存在兼职的情况，选择部门时
    function onSelDept(deptCode) {
        // $("#dlg").html(data.msg);
        // ajax匹配
        $.ajax({
            type: "post",
            url: "flow_dispose_ajax.jsp",
            data: {
                op: "matchAfterSelDept",
                actionId: "<%=mad.getActionId()%>",
                myActionId: "<%=mad.getId()%>",
                deptOfUserWithMultiDept: deptCode
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('#bodyBox').showLoading();
            },
            success: function (data, status) {
                hideDesigner();
                o("spanNextUser").innerHTML = data;
                $("#dlg").html(data);
            },
            complete: function (XMLHttpRequest, status) {
                $('#bodyBox').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText, '<lt:Label res="res.flow.Flow" key="prompt"/>');
            }
        });
    }

    function setNetdiskFiles(ids) {
        getNetdiskFiles(ids);
    }

    function doGetNetdiskFiles(response) {
        var rsp = response.responseText.trim();
        o("netdiskFilesDiv").innerHTML += rsp;
    }

    function getNetdiskFiles(ids) {
        var str = "ids=" + ids;
        var myAjax = new cwAjax.Request(
            "<%=cn.js.fan.web.Global.getFullRootPath(request)%>/netdisk/ajax_getfile.jsp",
            {
                method: "post",
                parameters: str,
                onComplete: doGetNetdiskFiles,
                onError: errFunc
            }
        );
    }

    // 公文套红
    function selTemplate(doc_id, file_id) {
        // $("#dlg").html(data.msg);
        // ajax匹配
        $.ajax({
            type: "post",
            url: "flow/flow_get_templates.jsp",
            data: {
                op: "sel"
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('#bodyBox').showLoading();
            },
            success: function (data, status) {
                hideDesigner();
                $("#dlg").html(data);
                hideDesigner();
                $("#dlg").dialog({
                    title: '<lt:Label res="res.flow.Flow" key="template"/>', modal: true,
                    buttons: {
                        '<lt:Label res="res.flow.Flow" key="cancel"/>': function () {
                            $("#dlg").html("");
                            $(this).dialog("close");
                        },
                        '<lt:Label res="res.flow.Flow" key="sure"/>': function () {
                            jConfirm('<lt:Label res="res.flow.Flow" key="red"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>', function (r) {
                                if (!r) {
                                    $("#dlg").html("");
                                    return;
                                } else {
                                    openWin("flow/flow_ntko_edit.jsp?file_id=" + file_id + "&flowId=<%=flowId%>&actionId=<%=actionId%>&doc_id=" + doc_id + "&isRevise=0&isApply=true&templateId=" + o("template").value, 1024, 768);
                                    $("#dlg").html("");
                                }
                            })
                            $(this).dialog("close");
                        }
                    },
                    closeOnEscape: true,
                    draggable: true,
                    resizable: true,
                    width: 300,
                    height: 200
                });


            },
            complete: function (XMLHttpRequest, status) {
                $('#bodyBox').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText, '<lt:Label res="res.flow.Flow" key="prompt"/>');
            }
        });
    }

    function addMyReply(id) {
        if ($("#myReplyTextarea" + id).is(":hidden")) {
            $("#myReplyTextarea" + id).show();
            $("#get" + id).focus();
            autoTextarea($("#get" + id).get(0));
        } else {
            $("#myReplyTextarea" + id).hide();
        }
    }

    /**
     * 文本框根据输入内容自适应高度
     * @param                {HTMLElement}        输入框元素
     * @param                {Number}                设置光标与输入框保持的距离(默认0)
     * @param                {Number}                设置最大高度(可选)
     */
    var autoTextarea = function (elem, extra, maxHeight) {
        extra = extra || 0;
        var isFirefox = !!document.getBoxObjectFor || 'mozInnerScreenX' in window,
            isOpera = !!window.opera && !!window.opera.toString().indexOf('Opera'),
            addEvent = function (type, callback) {
                elem.addEventListener ?
                    elem.addEventListener(type, callback, false) :
                    elem.attachEvent('on' + type, callback);
            },
            getStyle = elem.currentStyle ? function (name) {
                var val = elem.currentStyle[name];

                if (name === 'height' && val.search(/px/i) !== 1) {
                    var rect = elem.getBoundingClientRect();
                    return rect.bottom - rect.top -
                        parseFloat(getStyle('paddingTop')) -
                        parseFloat(getStyle('paddingBottom')) + 'px';
                }
                ;

                return val;
            } : function (name) {
                return getComputedStyle(elem, null)[name];
            },
            minHeight = parseFloat(getStyle('height'));

        elem.style.resize = 'none';

        var change = function () {
            var scrollTop, height,
                padding = 0,
                style = elem.style;

            if (elem._length === elem.value.length) return;
            elem._length = elem.value.length;

            if (!isFirefox && !isOpera) {
                padding = parseInt(getStyle('paddingTop')) + parseInt(getStyle('paddingBottom'));
            }
            ;
            //scrollTop = document.body.scrollTop || document.documentElement.scrollTop;

            elem.style.height = minHeight + 'px';
            if (elem.scrollHeight > minHeight) {
                if (maxHeight && elem.scrollHeight > maxHeight) {
                    height = maxHeight - padding;
                    style.overflowY = 'auto';

                } else {
                    height = elem.scrollHeight - padding;
                    style.overflowY = 'hidden';

                }
                ;
                style.height = height + extra + 'px';
                //scrollTop += parseInt(style.height) - elem.currHeight;
                //document.body.scrollTop = scrollTop;
                //document.documentElement.scrollTop = scrollTop;
                elem.currHeight = parseInt(style.height);
            }
            ;
            //initTree();
        };

        addEvent('propertychange', change);
        addEvent('input', change);
        addEvent('focus', change);
        change();

    };

    function submitPostscript(textareaId, rootId) {
        var textareaContent = $("#get" + textareaId).val();//“评论”文本框的内容
        var flow_id = $("#flow_id" + textareaId).val();
        var action_id = $("#action_id" + textareaId).val();
        var myActionId = $("#myActionId" + textareaId).val();
        var discussId = $("#discussId" + textareaId).val();
        var userRealName = $("#userRealName" + textareaId).val();
        var user_name = $("#user_name" + textareaId).val();
        var reply_name = $("#reply_name" + textareaId).val();
        var flow_name = $("#flow_name" + textareaId).val();
        var parent_id = $("#parent_id" + textareaId).val();
        var is_secret = $("#isSecret" + textareaId).val();

        if (textareaContent == "") {
            jAlert('<lt:Label res="res.flow.Flow" key="reviewContent"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>');
        } else {
            $.ajax({
                type: "post",
                url: "flow/addReply.do",
                data: {
                    content: textareaContent,
                    flow_id: flow_id,
                    action_id: action_id,
                    myActionId: myActionId,
                    discussId: discussId,
                    userRealName: userRealName,
                    user_name: user_name,
                    reply_name: reply_name,
                    flow_name: flow_name,
                    parent_id: parent_id,
                    cwsProgress: $('#cwsProgress').val(),
                    isSecret: is_secret
                },
                dataType: "html",
                beforeSend: function (XMLHttpRequest) {
                    $('#bodyBox').showLoading();
                    //$('#loading-indicator-bodyBox-overlay').height($('#flowbodybg').height());
                    //$('#loading-indicator-bodyBox').css({'bottom':'350px','top':''});
                },
                complete: function (XMLHttpRequest, status) {
                    $('#bodyBox').hideLoading();
                    //$("#get"+textareaId).height("48px");
                    //$('#bodyBox').hideLoading();
                },
                success: function (data, status) {
                    data = data.substring(data.indexOf("{\"ret\""));
                    //alert(data);
                    var re = $.parseJSON(data);
                    if (re.ret == "1") {
                        if (rootId == 0) {
                            $("#tablehead").append(re.result);
                            <%if (fd.isProgress()) {%>
                            $("#tablehead").find("td:eq(1)").prepend("<div>进度：" + $('#cwsProgress').val() + "%</div>");
                            $("#totalProgress").html($('#cwsProgress').val());
                            <%}%>
                            $("#get" + textareaId).val("");
                            $("#myReplyTextarea" + textareaId).hide();
                            $("#divShow").show();
                        } else {
                            $("#trline" + rootId).before(re.result);
                            $("#get" + textareaId).val("");
                            $("#myReplyTextarea" + textareaId).hide();
                            $("#divShow").show();
                        }
                    }
                },
                error: function () {
                    jAlert('<lt:Label res="res.flow.Flow" key="replyWrong"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>');
                }
            });

        }
    }

    function show() {
        $("#notShowDiv").show();
        $("#showDiv").hide();
        $("#divShow").show();
    }

    function notshow() {
        $("#notShowDiv").hide();
        $("#showDiv").show();
        $("#divShow").hide();
    }

    function chooseHideComment(obj) {
        var myImg = $(obj).children("img");
        var myInput = $(obj).children("input");
        if (myImg.attr("src").indexOf("checkbox_sel") != -1) {//现在是“显示”状态
            myImg.attr("src", "<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png");
            myInput.val("0");
        } else {//现在是“隐藏”状态
            myImg.attr("src", "<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png");
            myInput.val("1");
        }
    }

    <%
    if (lf.isDebug()) { //  || tester!=null) {
    %>
    $.toaster({priority: 'info', message: '流程处于调试模式'});
    <%}%>

    function delAnnex(annexId) {
        jConfirm('<lt:Label res="res.flow.Flow" key="isDelete"/>', '提示', function (r) {
            if (!r) {
                return;
            } else {
                $.ajax({
                    type: "post",
                    url: "flow/delAnnex.do",
                    data: {
                        annexId: annexId
                    },
                    dataType: "html",
                    beforeSend: function (XMLHttpRequest) {
                        $('#bodyBox').showLoading();
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        jAlert(data.msg, "提示");
                    },
                    complete: function (XMLHttpRequest, status) {
                        $('#bodyBox').hideLoading();
                        $('#trReply' + annexId).hide();
                        $('#trline' + annexId).hide();
                        $("tr[pId='" + annexId + "']").hide();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        jAlert(XMLHttpRequest.responseText, "提示");
                    }
                });
            }
        });
    }

    <%
    // 如果下一节点上设置为“表单中的人员”，或者限定了部门表单域，则绑定change事件，以重新匹配人员
    Vector vto = wa.getLinkToActions();
    Iterator toir = vto.iterator();
    while (toir.hasNext()) {
        WorkflowActionDb towa = (WorkflowActionDb)toir.next();
        String jobCode = towa.getJobCode();
        if (jobCode.startsWith(WorkflowActionDb.PRE_TYPE_FIELD_USER)) {
            String fieldNames = jobCode.substring((WorkflowActionDb.PRE_TYPE_FIELD_USER + "_").length());
            if (!fieldNames.startsWith("nest.")) {
                String[] fieldAry = StrUtil.split(fieldNames, ",");
                if (fieldAry==null) {
                    continue;
                }
                for (int k=0; k<fieldAry.length; k++) {
                    String fieldName = fieldAry[k];
                    %>
                    if (o('<%=fieldName%>').tagName == 'input') {
                        $("input[name='<%=fieldName%>']").change(function () {
                            reMatchUser('<%=fieldName%>', o('<%=fieldName%>').value);
                        });
                    } else {
                        $("select[name='<%=fieldName%>']").change(function () {
                            reMatchUser('<%=fieldName%>', o('<%=fieldName%>').value);
                        });
                    }
                    // 在页面加载时，即reMatchUser，如：当部门宏控件默认为本人部门时
                    $(function () {
                        reMatchUser('<%=fieldName%>', o('<%=fieldName%>').value);
                    })
    <%
                }
            }
        }
        else {
            String deptField = WorkflowActionDb.getActionProperty(wpd, towa.getInternalName(), "deptField");
            if (deptField!=null) {
        %>
        if (o('<%=deptField%>')) {
            if (o('<%=deptField%>').tagName == 'input') {
                $("input[name='<%=deptField%>']").change(function () {
                    reMatchUser('<%=deptField%>', o('<%=deptField%>').value);
                });
            } else {
                $("select[name='<%=deptField%>']").change(function () {
                    reMatchUser('<%=deptField%>', o('<%=deptField%>').value);
                });
            }

            // 在页面加载时，即reMatchUser
            $(function () {
                reMatchUser('<%=deptField%>', o('<%=deptField%>').value);
            })
        }
        <%
            }
        }
    }
    %>
    // 当表单中的用户有变化时，重新匹配用户
    function reMatchUser(fieldName, fieldValue) {
        var data = {
            op: "reMatchUser",
            actionId: "<%=mad.getActionId()%>",
            myActionId: "<%=mad.getId()%>",
            fieldName: fieldName,
            fieldValue: fieldValue
        }

        $.ajax({
            type: "post",
            url: "flow_dispose_ajax.jsp",
            data: data,
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('#bodyBox').showLoading();
            },
            success: function (data, status) {
                hideDesigner();
                o("spanNextUser").innerHTML = data;
                $("#dlg").html(data);
            },
            complete: function (XMLHttpRequest, status) {
                $('#bodyBox').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText, '<lt:Label res="res.flow.Flow" key="prompt"/>');
            }
        });
    }

    // 页面载入时显示loading效果，以免当页面还没有完全加载，如产业类别这类级联的SQL控件还未完全载入时，即点击了同意，致数据丢失
    // 不能用jQuery的ready方法，因为ready方法注册的事件处理程序，只要在DOM完全就绪时，就可以调用了，比如一张图片只要<img>标签完成，不用等这个图片加载完成，就可以设置图片的宽高的属性或样式等
    // 用window.onload也不行，因为页面中有ajax
    // 注意用when的意思是参数中放ajax执行的函数，下行会导致页面多加载了两次
    /*$.when($.ajax(), $.ajax()).then(function(){
        // 所有 AJAX 请求已完成
        $("#loading").hide();
    })*/

    // 前提：所有ajax请求都是用jquery的$.ajax发起的，而非原生的XHR；
    var ajaxBack = $.ajax;
    var ajaxCount = 0;
    var allAjaxDone = function () {
        // 所有 AJAX 请求已完成
        $("#loading").hide();
    };
    // 由于get/post/getJSON等，最后还是调用到ajax，因此只要改ajax函数即可
    $.ajax = function (setting) {
        ajaxCount++;
        var cb = setting.complete;
        setting.complete = function () {
            if ($.isFunction(cb)) {
                cb.apply(setting.context, arguments);
            }
            ajaxCount--;
            if (ajaxCount == 0 && $.isFunction(allAjaxDone)) {
                allAjaxDone();
            }
        };
        ajaxBack(setting);
    };
    $(function () {
        // 如果没有ajax，则ajaxCount为0，应置loading为hide
        if (ajaxCount == 0) {
            $("#loading").hide();
        }
    })

    function done(msg, isClose, op) {
        if (tabIdOpener != "") {
            reloadTab(tabIdOpener);
        }
        if (isClose) {
            jAlert(msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', function () {
                // 关闭tab
                closeActiveTab();
            })
        } else {
            if ("finish" == op) {
                var isRecall = <%=isRecall%>;
                if (isRecall) {
                    msg += " <lt:Label res="res.flow.Flow" key="recallTip"/>";
                }
            }
            jAlert(msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', function () {
                if (isRecall) {
                    window.location.href = "flow_modify.jsp?flowId=<%=flowId%>";
                } else {
                    // 关闭tab
                    closeActiveTab();
                }
            })
        }
    }

    <%
        String actionMode = ParamUtil.get(request, "actionMode");
        if ("onlyReturn".equals(actionMode)) {
            String btnReturnName = conf.getBtnName("FLOW_BUTTON_RETURN").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","back"):conf.getBtnName("FLOW_BUTTON_RETURN");
    %>
    $(function () {
        // 隐藏掉除了退回按钮之外的其它按钮
        var $btnTables = $('#toolbar').find('.button_table');
        $btnTables.each(function () {
            if ($(this).text().indexOf("<%=btnReturnName%>") == -1) {
                $(this).hide();
            }
        })

        $('.spacer').parent().hide();
    })
    <%
        }

        // 如果处理过程显示于下方
        boolean flowProcessShowOnBottom = cfg.getBooleanProperty("flowProcessShowOnBottom");
        if (flowProcessShowOnBottom) {
    %>
    $(function() {
        $("#processListTab").appendTo($('#processBox'));
        $("#processListTab").show();
        $('#switchProcessBox').hide();
    });
    <%
        }
    %>

    $(function () {
        $('input[type=radio]').each(function (i) {
            var name = $(this).attr("name");
            if ($(this).attr("readonly") == null) {
                $(this).addClass('radio-menu');
            }
        });

    // 不能用BootstrapMenu，因为chrome上会导致radio无法点击
    $.contextMenu({
        selector: '.radio-menu',
        trigger: 'hover',
        delay: 1000,
        callback: function(key, options) {
            if (key == 'cancel') {
                var $obj = options.$trigger;
                var name = $obj.attr('name');
                $('input[type=radio][name="' + name + '"]:checked').attr("checked", false);
            }
        },
        items: {
            "cancel": {name: "取消选择", icon: function($element, key, item){ return 'context-menu-icon context-menu-icon-quit'; }}
        }
    });

    $('input').each(function() {
        if ($(this).attr('kind')=='DATE' || $(this).attr('kind')=='DATE_TIME') {
            $(this).attr('autocomplete', 'off');
        }
    });

    // 初始化tip提示
    // 不能通过$("#visualForm").serialize()来获取所有的元素，因为radio或checkbox未被选中，则不会被包含
    $('#flowForm input, #flowForm select, #flowForm textarea').each(function() {
        var tip = '';
        if ($(this).attr('type') == 'radio') {
            tip = $(this).parent().attr('tip');
        }
        else {
            tip = $(this).attr('tip');
        }
        if (null!=tip && ""!=tip) {
            $(this).poshytip({
                content: function(){return tip;},
                className: 'tip-yellowsimple',
                alignTo: 'target',
                alignX: 'center',
                offsetY: 5,
                allowTipHover: true
            });
        }
    });
});

<%
    ModuleSetupDb msd = new ModuleSetupDb();
    msd = msd.getModuleSetupDbOrInit(lf.getFormCode());
    if (msd.getPageStyle()==ConstUtil.PAGE_STYLE_LIGHT) {
%>
    // 不能放在$(function() 中，原来的tabStyle_8风格会闪现
    $('#flowForm').find('.tabStyle_8').addClass('layui-table').removeClass('tabStyle_8');
    $('#processListTab').addClass('layui-table').removeClass('tabStyle_1');
    $('#processListTab').find('.tabStyle_1_title').removeClass('tabStyle_1_title');
<%
    }
%>
</script>
</html>