<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.flow.strategy.*" %>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>流程动作设定</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
    <script src="../js/bootstrap/js/bootstrap.min.js"></script>
    <%
        String op = ParamUtil.get(request, "op");
        String fieldWrite = ParamUtil.get(request, "hidFieldWrite");
        String fieldHide = ParamUtil.get(request, "fieldHide");
        String flowTypeCode = ParamUtil.get(request, "flowTypeCode");

        String dept = ParamUtil.get(request, "hidDept");
        String nodeMode = ParamUtil.get(request, "hidNodeMode");
        if (nodeMode.equals("")) {
            nodeMode = "" + WorkflowActionDb.NODE_MODE_ROLE;
        }
        if (op.equals("load")) {
            nodeMode = "" + WorkflowActionDb.NODE_MODE_ROLE;
        }

        // 节点在控件中的内部名称
        String internalName = ParamUtil.get(request, "internalName");
        if (internalName.equals("")) {
            out.print(SkinUtil.makeInfo(request, "请选择节点！"));
            return;
        }
    %>
    <jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
    <jsp:useBean id="cfg" scope="page" class="com.redmoon.oa.Config"/>
    <jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
    <%
        String priv = "read";
        if (!privilege.isUserPrivValid(request, priv)) {
            out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }

        String userName = "";
        String userRealName = "";
    %>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../js/jquery.xmlext.js"></script>
    <script language="JavaScript">
        function openWin(url, width, height) {
            var newwin = window.open(url, "fieldWin", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width=" + width + ",height=" + height);
            return newwin;
        }

        var curFields, curFieldsShow

        function OpenFormFieldSelWin(fields, fieldsShow) {
            curFields = fields;
            curFieldsShow = fieldsShow;
            var formView = o("formView").value;
            if (formView == null || formView == '') {
                jAlert('表单视图不存在', '提示');
                return;
            }
            //可写字段内容过多导致url过长，使flow_predefine_form_field_nest_sel取值内容不全，修改为post提交代替window.open
            openPostWindow("flow_predefine_form_field_nest_sel.jsp");
        }

        function openPostWindow(url) {
            openWin("flow_predefine_form_field_nest_sel.jsp", 600, 480);
            var tempForm = document.createElement("form");
            tempForm.id = "tempForm1";
            tempForm.method = "post";
            tempForm.action = url;

            var hideInput = document.createElement("input");
            hideInput.type = "hidden";
            hideInput.name = "formView"
            hideInput.value = o("formView").value;
            tempForm.appendChild(hideInput);

            hideInput = document.createElement("input");
            hideInput.type = "hidden";
            hideInput.name = "flowTypeCode"
            hideInput.value = "<%=flowTypeCode%>";
            tempForm.appendChild(hideInput);

            hideInput = document.createElement("input");
            hideInput.type = "hidden";
            hideInput.name = "fields"
            hideInput.value = o(curFields).value;
            tempForm.appendChild(hideInput);

            document.body.appendChild(tempForm);
            tempForm.target = "fieldWin";
            tempForm.submit();
            document.body.removeChild(tempForm);
        }

        function setFieldValue(v) {
            o(curFields).value = v;
            ModifyAction(false);
        }

        function setFieldText(v) {
            o(curFieldsShow).value = v;
        }

        function setDeptName(v) {
            o("deptName").value = v;
        }

        var flag = "";

        function ModifyAction(isSubmit) {
            if (!window.parent.Designer.isActionSelected) {
                jAlert("请选择节点且只能选择一个！", "提示");
                return;
            }

            if (isSubmit && o("title").value == "") {
                jAlert("请填写标题！", "提示");
                o("title").focus();
                return;
            }

            if (isSubmit && o("kind").value == "<%=WorkflowActionDb.KIND_READ%>") {
                if (o("fieldWriteText").value != "") {
                    jAlert("审阅时只能查看，不能填写可写字段！", "提示");
                    return;
                }
            }

            if (isSubmit) {
                if (o('userName').value == '<%=WorkflowActionDb.PRE_TYPE_USER_SELECT%>') {
                    var $opt = $("#strategy").find("option:selected");
                    if ($opt.attr("isSelectable") == "false") {
                        jAlert("当策略为“" + $opt.text() + "”时<br/>不能自选用户", "提示");
                        return;
                    }
                }
            }

            if (o("flagModify").checked)
                flag = "1";
            else
                flag = "0";
            if (o("flagSel").checked)
                flag += "1";
            else
                flag += "0";
            if (o("flagDiscardFlow").checked)
                flag += "1";
            else
                flag += "0";
            if (o("flagDelFlow").checked)
                flag += "1";
            else
                flag += "0";
            flag += o("flagSaveArchive").value;
            if (o("flagDelAttach").checked)
                flag += "1";
            else
                flag += "0";
            if (o("flagXorRadiate").checked)
                flag += "1";
            else
                flag += "0";
            if (o("flagXorAggregate").checked)
                flag += "1";
            else
                flag += "0";
            if (o("flagFinishFlow").checked)
                flag += "1";
            else
                flag += "0";
            if (o("flagEditAttach").checked)
                flag += "1";
            else
                flag += "0";
            if (o("flagReceiveRevise").checked)
                flag += "1";
            else
                flag += "0";
            if (o("flagAgreeAndFinish").checked) {
                flag += "1";
            } else {
                flag += "0";
            }
            if (o("flagXorFinish").checked) {
                flag += "1";
            }
            else {
                flag += "0";
            }
            if (o("flagXorReturn").checked) {
                flag += "1";
            }
            else {
                flag += "0";
            }

            var rankName = o("rank").options[o("rank").selectedIndex].text;
            var rel = "0";
            if (o("relateRoleToOrganization").checked)
                rel = "1";

            var isMsg = "0";
            if (o("isMsg").checked) {
                isMsg = "1";
            }

            // 检查fieldWrite与fieldHide不能有重叠
            if (o("fieldHide").value.trim() != "" && o("fieldWrite").value.trim() != "") {
                var hides = o("fieldHide").value.trim().split(",");
                var writes = o("fieldWrite").value.trim().split(",");
                var writesText = o("fieldWriteText").value.trim().split(",");
                for (var i = 0; i < hides.length; i++) {
                    for (var j = 0; j < writes.length; j++) {
                        if (hides[i] == writes[j]) {
                            jAlert("出现相同字段：" + writesText[j] + "，注意可填写字段与隐藏字段不能有重叠！", "提示");
                            return;
                        }
                    }
                }
            }

            var stra = $("#strategy").val();
            if (stra == null) {
                stra = "";
            }
            if (stra.indexOf("x") == 0) {
                if ($('#x_span').is(':hidden')) {
                    $("#x_span").show();
                } else {
                    var xnum = $("#x_num").val();
                    var type = /^[0-9]*[1-9][0-9]*$/;
                    var re = new RegExp(type);
                    if (xnum.match(re) == null) {
                        jAlert("x必须为正整数！", "提示");
                        return;
                    }
                    var xtext = $("#strategy option[value='" + stra + "']").text();
                    $("#strategy option[value='" + stra + "']").remove();
                    $("#strategy").append("<option value='x_" + xnum + "'>" + xtext + "</option>");
                    $("#strategy option[value='x_" + xnum + "']").attr("selected", true);
                }
            } else {
                if (!$('#x_span').is(':hidden')) {
                    var xnum = $("#x_num").val();
                    var xtext = $("#strategy option[value='x_" + xnum + "']").text();
                    $("#strategy option[value='x_" + xnum + "']").remove();
                    $("#strategy").append("<option value='x'>" + xtext + "</option>");
                    $("#x_num").val('');
                    $("#x_span").hide();
                }
            }

            var isDelayed = o("isDelayed").checked ? "1" : "0";
            var timeDelayedValue = o("timeDelayedValue").value;
            var timeDelayedUnit = o("timeDelayedUnit").value;
            var canPrivUserModifyDelayDate = o("canPrivUserModifyDelayDate").checked ? "1" : "0";

            var item2 = "{" + o("relateToAction").value + "," + o("ignoreType").value + "," + o("kind").value + "," + o("fieldHide").value.replaceAll(",", "|") + "," + isDelayed + "," + timeDelayedValue + "," + timeDelayedUnit + "," + canPrivUserModifyDelayDate + "," + o("formView").value + "," + (o("relateDeptManager").checked?1:0) + "}";

            makeSubFlowProp();
            if (isSubmit && (o("kind").value == "<%=WorkflowActionDb.KIND_SUB_FLOW%>")) {
                if ($("#p2s").html() == "" || $("#s2p").html() == "") {
                    jAlert("请设置父子字段映射！", "提示");
                    return false;
                }
            }
            window.parent.setMsgProp(o("msgProp").value);
            if (isSubmit)
                window.parent.ModifyAction("", o("title").value, o("OfficeColorIndex").value, "", o("userName").value, o("userRealName").value, o("direction").value, o("rank").value, rankName, rel, o("fieldWrite").value, o("checkState").value, o("dept").value, flag, o("nodeMode").value, o("strategy").value, o("item1").value, item2, isMsg);
            else
                window.parent.ModifyActionNotSubmit("", title.value, OfficeColorIndex.value, "", userName.value, userRealName.value, o("direction").value, rank.value, rankName, rel, fieldWrite.value, checkState.value, dept.value, flag, nodeMode.value, strategy.value, item1.value, item2, isMsg);

            if (userRealName.value == "脚本")
                o("nodeScriptTr").style.display = "";
            else
                o("nodeScriptTr").style.display = "none";

            window.close();
        }

        function getFlowString() {
            return window.parent.getFlowString();
        }

        function getInternalName() {
            return "<%=internalName%>";
        }

        var tmpId = 0;

        function onload_win() {
            if (!window.parent.Designer.isActionSelected) {
                mainTable.style.display = "none";
                return;
            } else {
                mainTable.style.display = "";
            }

            var STATE_NOTDO = <%=WorkflowActionDb.STATE_NOTDO%>;
            var STATE_IGNORED = <%=WorkflowActionDb.STATE_IGNORED%>;
            var STATE_DOING = <%=WorkflowActionDb.STATE_DOING%>;
            var STATE_RETURN = <%=WorkflowActionDb.STATE_RETURN%>;
            var STATE_FINISHED = <%=WorkflowActionDb.STATE_FINISHED%>;

            var chkState = window.parent.getActionCheckState();
            if (chkState == STATE_FINISHED || chkState == STATE_DOING) {
                jAlert("动作已完成或者正在处理中时，不能被编辑！", "提示");
                window.close();
                return;
            }

            o("userName").value = window.parent.getActionJobCode();
            if (o("userName").value == "<%=WorkflowActionDb.PRE_TYPE_NODE_SCRIPT%>") {
                o("nodeScriptTr").style.display = "";
            }

            o("title").value = window.parent.getActionTitle();
            o("OfficeColorIndex").value = window.parent.getActionColorIndex();
            o("userRealName").value = window.parent.getActionJobName();
            o("direction").value = window.parent.getActionProxyJobCode();
            o("rank").value = window.parent.getActionProxyJobName();
            var rel = window.parent.getActionProxyUserRealName();
            o("relateRoleToOrganization").checked = rel == "1";
            o("fieldWrite").value = window.parent.getActionFieldWrite();
            o("checkState").value = window.parent.getActionCheckState();

            o("nodeMode").value = window.parent.getActionNodeMode();

            if (o("nodeMode").value == "<%=WorkflowActionDb.NODE_MODE_ROLE%>") {
                o("spanMode").innerHTML = "角色";
            }
            else {
                o("spanMode").innerHTML = "用户";
            }

            <%if (dept.equals("")) {%>
            o("dept").value = window.parent.getActionDept();
            <%}%>
            flag = window.parent.getActionFlag();
            if (flag.length >= 1) {
                if (flag.substr(0, 1) != "1")
                    o("flagModify").checked = false;
                else
                    o("flagModify").checked = true;
            }
            if (flag.length >= 2) {
                if (flag.substr(1, 1) != "1")
                    o("flagSel").checked = false;
            }

            // flag.length长度为2时，是给新创建的节点设置属性

            if (flag.length >= 3) {
                if (flag.substr(2, 1) != "1")
                    o("flagDiscardFlow").checked = false;
            } else
                o("flagDiscardFlow").checked = false;

            if (flag.length >= 4) {
                if (flag.substr(3, 1) != "1") {
                    o("flagDelFlow").checked = false;
                }
            } else
                o("flagDelFlow").checked = false;

            if (flag.length >= 5) {
                o("flagSaveArchive").value = flag.substr(4, 1);
            } else {
                o("flagSaveArchive").value = "0";
            }

            if (flag.length >= 6) {
                if (flag.substr(5, 1) != "1") {
                    o("flagDelAttach").checked = false;
                }
            } else
                o("flagDelAttach").checked = false;

            if (flag.length >= 7) {
                if (flag.substr(6, 1) != "1") {
                    o("flagXorRadiate").checked = false;
                }
            } else {
                o("flagXorRadiate").checked = false;
            }

            if (flag.length >= 8) {
                if (flag.substr(7, 1) != "1") {
                    o("flagXorAggregate").checked = false;
                }
            } else {
                o("flagXorAggregate").checked = false;
            }

            if (flag.length >= 9) {
                if (flag.substr(8, 1) != "1") {
                    o("flagFinishFlow").checked = false;
                }
            } else {
                o("flagFinishFlow").checked = false;
            }

            if (flag.length >= 10) {
                if (flag.substr(9, 1) != "1") {
                    o("flagEditAttach").checked = false;
                }
            } else {
                o("flagEditAttach").checked = false;
            }

            if (flag.length >= 11) {
                if (flag.substr(10, 1) != "1") {
                    o("flagReceiveRevise").checked = false;
                }
            } else {
                o("flagReceiveRevise").checked = false;
            }

            if (flag.length >= 12) {
                if (flag.substr(11, 1) != "1") {
                    o("flagAgreeAndFinish").checked = false;
                }
            } else {
                o("flagAgreeAndFinish").checked = false;
            }

            if (flag.length >= 13) {
                if (flag.substr(12, 1)!="1") {
                    o("flagXorFinish").checked = false;
                }
                else {
                    o("flagXorFinish").checked = true;
                }
            } else {
                o("flagXorFinish").checked = false;
            }

            if (flag.length >= 14) {
                if (flag.substr(13, 1)!="1") {
                    o("flagXorReturn").checked = false;
                }
                else {
                    o("flagXorReturn").checked = true;
                }
            }
            else {
                o("flagXorReturn").checked = false;
            }

            var stra = window.parent.getActionStrategy();
            if (stra.indexOf("x") == 0) {
                var xtext = $("#strategy option[value='x']").text();
                $("#strategy option[value='x']").remove();
                $("#strategy").append("<option value='" + stra + "'>" + xtext + "</option>");
                $("#strategy option[value='" + stra + "']").attr("selected", true);
                $("#x_num").val(stra.substring(2));
                $("#x_span").show();
            } else {
                o("strategy").value = stra;
            }

            o("item1").value = window.parent.getActionItem1();
            if (o("item1").value == "") {
                o("item1").value = "0";
            }

            var item2 = window.parent.getActionItem2();
            // 解析item2，格式{ relateToAction , ignoreType , kind , fieldHide , isDelayed , timeDelayedValue , timeDelayedUnit , canPrivUserModifyDelayDate, formView, relateDeptManager };
            if (item2.length >= 2) {
                var items = item2.substring(1, item2.length - 1);
                var itemAry = items.split(",");
                if (itemAry.length >= 1) {
                    if (itemAry[0].length > 0) {
                        o("relateToAction").value = itemAry[0];
                    }
                    if (itemAry.length >= 2) {
                        o("ignoreType").value = itemAry[1];
                    }
                    if (itemAry.length >= 3) {
                        o("kind").value = itemAry[2];
                        switchKind(itemAry[2]);
                    } else {
                        switchKind("<%=WorkflowActionDb.KIND_ACCESS%>");
                    }
                    if (itemAry.length >= 4) {
                        o("fieldHide").value = itemAry[3].replaceAll("\\|", ",");
                    }

                    if (itemAry.length >= 5) {
                        if (itemAry[4] == "1")
                            o("isDelayed").checked = true;
                    }
                    if (itemAry.length >= 6) {
                        o("timeDelayedValue").value = itemAry[5];
                    }
                    if (itemAry.length >= 7) {
                        o("timeDelayedUnit").value = itemAry[6];
                    }
                    if (itemAry.length >= 8) {
                        if (itemAry[7] == 1)
                            o("canPrivUserModifyDelayDate").checked = true;
                    }
                    if (itemAry.length >= 9) {
                        o("formView").value = itemAry[8];
                    }
                    if (itemAry.length >= 10) {
                        if (itemAry[9]==1) {
                            o("relateDeptManager").checked = true;
                        }
                    }
                } else {
                    switchKind("<%=WorkflowActionDb.KIND_ACCESS%>");
                }
            } else {
                switchKind("<%=WorkflowActionDb.KIND_ACCESS%>");
            }

            var isMsg = window.parent.getActionIsMsg();
            // 当为空时表示新创建了节点，默认勾选上是否提醒
            if (isMsg == "1" || isMsg == "") {
                o("isMsg").checked = true;
            }
            else {
                o("isMsg").checked = false;
            }

            var inDegree = window.parent.GetActionProperty("curSelected", "inDegree");
            // 原始起点,20201116加入判断inDegree，以免开始节点被放在了非开始的位置
            if (window.parent.getActionType() == "workflow_start" && inDegree == "0") {
                span_direction.style.display = "none";
                span_starter.style.display = "none";
                o("kind").disabled = true;

                if (flag.length < 4) {
                    // 如果长度小于4，则说明是第一次创建流程图，则置为允许删除
                    o("flagDelFlow").checked = true;

                    o("flagDiscardFlow").checked = false;
                    o("flagFinishFlow").checked = false;

                    o("flagDiscardFlow").disabled = true;
                    o("flagFinishFlow").disabled = true;

                    o("isDelayTr").style.display = "none";
                }
            } else if (inDegree == "0") { // 入度为0，多起点
                span_direction.style.display = "none";
                span_starter.style.display = "none";
                o("kind").disabled = true;
            } else if (window.parent.getActionType() == "workflow_action") {
                span_self.style.display = "none";
                o("kind").disabled = false;
            }

            // document.frames["hiddenframe"].location.replace("flow_predefine_action_modify_getfieldtitle.jsp?flowTypeCode=<%=flowTypeCode%>&fieldWrite=" + o("fieldWrite").value + "&dept=" + o("dept").value + "&nodeMode=" + o("nodeMode").value); // 获取可写表单域的名称
            if (o("fieldHide").value.trim() != "") {
                $.get(
                    "flow_predefine_action_modify_getfieldtitle_ajax.jsp",
                    {
                        fields: o("fieldHide").value,
                        flowTypeCode: "<%=flowTypeCode%>"
                    },
                    function (data) {
                        o("fieldHideText").value = data.trim();
                    }
                );
            }

            if (o("fieldWrite").value.trim() != "") {
                $.get(
                    "flow_predefine_action_modify_getfieldtitle_ajax.jsp",
                    {
                        fields: o("fieldWrite").value,
                        flowTypeCode: "<%=flowTypeCode%>"
                    },
                    function (data) {
                        o("fieldWriteText").value = data.trim();
                    }
                );
            }

            if (o("dept").value.trim() != "") {
                $.get(
                    "flow_predefine_action_modify_get_dept_ajax.jsp",
                    {
                        dept: o("dept").value
                    },
                    function (data) {
                        o("deptName").value = data.trim();
                    }
                );
            }

            o("msgProp").value = window.parent.getMsgProp();
            if (o("msgProp").value.indexOf("<actions>") == 0 && o("msgProp").value.indexOf("internalName=\"<%=internalName%>\"")!=-1) {
                $("#imgComb").show();
            }

            var props = window.parent.getProps();
            var xml = $.parseXML(props);
            $xml = $(xml);
            $xml.find("actions").children().each(function (i) {
                if ($(this).attr("internalName") == "<%=internalName%>") {
                    var prop = $.parseJSON($(this).find("property").text());
                    if (prop) {
                        o("subFlowTypeCode").value = prop.subFlowTypeCode;

                        getSubFields();

                        var parentToSubMap = prop.parentToSubMap;
                        $.each(parentToSubMap, function (i, it) {
                            makeP2S(tmpId, this.parentField, this.parentTitle, this.subField, this.subTitle);
                            tmpId++;
                        });

                        var subToParentMap = prop.subToParentMap;
                        $.each(subToParentMap, function (i, it) {
                            makeS2P(tmpId, this.parentField, this.parentTitle, this.subField, this.subTitle);
                            tmpId++;
                        });
                    }

                    var redirectUrl = $(this).find("redirectUrl").text();
                    if (redirectUrl) {
                        o("redirectUrl").value = redirectUrl;
                    }

                    var nodeScript = $(this).find("nodeScript").text();
                    if (nodeScript) {
                        o("nodeScript").value = nodeScript;
                    }

                    var isModuleFilter = $(this).find("isModuleFilter").text();
                    if (isModuleFilter == "1") {
                        o("isModuleFilter").checked = true;
                    }

                    var branchMode = $(this).find("branchMode").text();
                    if (branchMode) {
                        o("branchMode").value = branchMode;
                    }

                    var btnAgreeName = $(this).find("btnAgreeName").text();
                    if (btnAgreeName) {
                        o("btnAgreeName").value = btnAgreeName;
                    }

                    var btnRefuseName = $(this).find("btnRefuseName").text();
                    if (btnRefuseName) {
                        o("btnRefuseName").value = btnRefuseName;
                    }
                    
                    var isShowNextUsers = $(this).find("isShowNextUsers").text();
                    // 判断是否为空，是为了向下兼容
                    if (isShowNextUsers=="" || isShowNextUsers=="1") {
                        o("isShowNextUsers").checked = true;
                    }
                    else {
                        o("isShowNextUsers").checked = false;
                    }

                    var deptField = $(this).find("deptField").text();
                    if (deptField) {
                        $('#deptField').val(deptField);
                    }
                    
                    // 被退回再提交时能否重新选择用户
                    var canSelUserWhenReturned = $(this).find("canSelUserWhenReturned").text();
                    if (canSelUserWhenReturned) {
                        $('#canSelUserWhenReturned').attr("checked", true);
                    }

                    return false;
                }
            });
        }

        function window_onload() {
            onload_win();
        }

        function setDepts(ret) {
            o("dept").value = "";
            o("deptName").value = "";
            for (var i = 0; i < ret.length; i++) {
                if (o("dept").value == "") {
                    o("dept").value += ret[i][0];
                    o("deptName").value += ret[i][1];
                } else {
                    o("dept").value += "," + ret[i][0];
                    o("deptName").value += "," + ret[i][1];
                }
            }

            ModifyAction(false);
        }

        function openWinDepts() {
            openWin('../dept_multi_sel.jsp', 480, 320);
        }

        function getSelUserNames() {
            if (o("nodeMode").value == "<%=WorkflowActionDb.NODE_MODE_USER%>") {
                if (o("userName").value == "$self" || o("userName").value == "$starter" || o("userName").value == "$userSelect")
                    return "";
                else
                    return o("userName").value;
            } else
                return "";
        }

        function getSelUserRealNames() {
            if (o("nodeMode").value == "<%=WorkflowActionDb.NODE_MODE_USER%>") {
                if (o("userName").value == "$self" || o("userName").value == "$starter" || o("userName").value == "$userSelect")
                    return "";
                else
                    return o("userRealName").value;
            } else
                return "";
        }

        function getUserRoles() {
            if (o("nodeMode").value == "<%=WorkflowActionDb.NODE_MODE_ROLE%>") {
                return o("userName").value;
            } else
                return "";
        }

        function getDepts() {
            return o("dept").value;
        }

        function setUsers(users, userRealNames) {
            o("userName").value = users;
            o("userRealName").value = userRealNames;
            o("nodeMode").value = "<%=WorkflowActionDb.NODE_MODE_USER%>";

            spanMode.innerHTML = "用户";

            ModifyAction(false);
        }

        function setRoles(roleCodes, roleDescs) {
            o("userName").value = roleCodes;
            o("userRealName").value = roleDescs;
            o("nodeMode").value = "<%=WorkflowActionDb.NODE_MODE_ROLE%>";
            spanMode.innerHTML = "角色";
            if (o("relateRoleToOrganization").checked) {
                if (roleCodes.indexOf(",") != -1) {
                    o("relateRoleToOrganization").checked = false;
                    jAlert("多个角色被选择，角色关联已经被取消！", "提示");
                }
            }

            ModifyAction(false);
        }

        function openWinUserRoles() {
            var roleCodes = "";
            if (nodeMode.value == "<%=WorkflowActionDb.NODE_MODE_ROLE%>") {
                roleCodes = o("userName").value
            }
            openWin('../role_multi_sel.jsp?unitCode=<%=StrUtil.UrlEncode(privilege.getUserUnitCode(request))%>&roleCodes=' + roleCodes, 526, 435);
            return;
        }

        function checkRelation() {
            if (o("relateRoleToOrganization").checked) {
                // if (userName.value.indexOf(",")!=-1) {
                //	alert("当只有一个角色时，才能被关联！");
                //	relateRoleToOrganization.checked = false;
                // }
            }
        }

        function showNodeScript() {
            o("nodeScriptTr").style.display = "";

            userName.value = '<%=WorkflowActionDb.PRE_TYPE_NODE_SCRIPT%>';
            userRealName.value = "脚本";
            nodeMode.value = '1';
            spanMode.innerHTML = iText;
            ModifyAction(false);
        }
    </script>
</HEAD>
<BODY onLoad="window_onload()">
<table align="center" cellpadding="2" cellspacing="0" class="tabStyle_1" id="mainTable" style="padding:0px; margin:0px; margin-top:3px; width:100%">
    <tr>
        <td height="22" colspan="2" align="center"><input name="okbtn" type="button" class="btn btn-default" title="节点<%=internalName%>" onclick="ModifyAction(true)" value=" 保存 "/></td>
    </tr>
    <tr style="display: none">
        <td>
            节点标识
        </td>
        <td>
            <%=internalName%>
        </td>
    </tr>
    <tr>
        <td width="82" height="22">处理人员</td>
        <td height="22" align="left" style="line-height:1.5">
            <span id="span_self"> <a title="本人" href="#" onclick="userName.value='$self';userRealName.value='本人';nodeMode.value='1';spanMode.innerHTML='用户';ModifyAction(false)">本人</a>&nbsp;&nbsp; </span> <a href="javascript:;" onclick="openWinUserRoles()">选择角色</a>&nbsp;&nbsp;&nbsp;<a href="#"
                                                                                                                                                                                                                                                                                                   onclick="javascript:openWin('../user_multi_sel.jsp','900','730')">选择用户</a>
            <!--<a href="#" onClick="userName.value='$deptLeader';userRealName.value='部门领导';jobCode.value='';jobName.value='';proxyJobCode.value='';proxyJobName.value=''">部门领导</a>-->
            <!--&nbsp;&nbsp;<a href="#" onClick="userName.value='<%=WorkflowActionDb.PRE_TYPE_USER_SELECT%>';userRealName.value='用户自选';jobCode.value='';jobName.value='';proxyJobCode.value='';proxyJobName.value=''">用户自选</a>-->
            <br/>
            <span id="span_starter">
    <a title="自动转换为发起人" href="javascript:;" onclick="userName.value='$starter';userRealName.value='发起人';nodeMode.value='1';spanMode.innerHTML='发起人';ModifyAction(false)">发起人</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a title="由前一用户自行指定" href="javascript:;"
                                                                                                                                                                                                                            onclick="userName.value='<%=WorkflowActionDb.PRE_TYPE_USER_SELECT%>';userRealName.value='自选用户';nodeMode.value='1';spanMode.innerHTML='自选用户';ModifyAction(false)">自选用户</a>
    <%if (cfg.getBooleanProperty("isMyLeaderUsed") && com.redmoon.oa.kernel.License.getInstance().isPlatformSrc()) {%>
    <br/>
    <a title="我的领导" href="javascript:;" onclick="userName.value='$myleader';userRealName.value='我的领导';nodeMode.value='1';spanMode.innerHTML='我的领导';ModifyAction(false)">我的领导</a>&nbsp;&nbsp;
    <a title="我的下属" href="javascript:;" onclick="userName.value='$mysubordinate';userRealName.value='我的下属';nodeMode.value='1';spanMode.innerHTML='我的下属';ModifyAction(false)">我的下属</a>
    <%} %>
    <div id="afBox" class="af_box">
      <span><a id="afBtn" href="javascript:;" title="高级选项"><img id="afBtnImg" src="<%=SkinMgr.getSkinPath(request)%>/images/af_arrow_down.png" width="27" height="14"/></a></span>
      <div class="af_line"></div>
    </div>
    <div id="afChildren" style="display:none">
	<!--<a title="自动转换为本节点往前两个节点处理人员" href="javascript:;" onclick="userName.value='<%=WorkflowActionDb.PRE_TYPE_FORE_ACTION%>';userRealName.value='自动转换为本节点往前两个节点处理人员';nodeMode.value='1';spanMode.innerHTML='往前两个节点人员';ModifyAction(false)">往前两个节点人员</a>&nbsp;&nbsp;&nbsp;&nbsp;-->
	<a title="由前一用户自行指定（但需在所管理的部门范围内，同时限定部门无效）" href="javascript:;" onclick="userName.value='<%=WorkflowActionDb.PRE_TYPE_USER_SELECT_IN_ADMIN_DEPT%>';userRealName.value='自选用户(管理)';nodeMode.value='1';spanMode.innerHTML='自选用户(管理)';ModifyAction(false)">自选用户(管理)</a>&nbsp;&nbsp;&nbsp;&nbsp;    
	<a title="部门管理员(拥有管理所在关联节点人员所在部门的权限)" href="javascript:;" onclick="userName.value='<%=WorkflowActionDb.PRE_TYPE_DEPT_MGR%>';userRealName.value='部门管理员';nodeMode.value='1';spanMode.innerHTML='部门管理员';ModifyAction(false)">部门管理员</a>
	<BR/>
    <a title="流程中某个节点的处理人员" href="javascript:;" onclick="selAction()">所选节点上的人员</a><br/>
    <a title="表单中指定的人员" href="javascript:;" onclick="selField()">表单中指定的人员</a><br/>
    <a title="通过脚本选择人员" href="javascript:;" onclick="showNodeScript()">通过脚本选人</a><br/>
	<span title="表单中需存在项目选择宏控件" style="display: none">
    项目角色<select id="projectRole" name="projectRole" onchange="setProjectRole()">
    <option value="">请选择</option>
	<%
        SelectMgr selm = new SelectMgr();
        SelectDb sd = selm.getSelect("project_role");
        Vector vsd = sd.getOptions();
        Iterator irsd = vsd.iterator();
        while (irsd.hasNext()) {
            SelectOptionDb sod = (SelectOptionDb) irsd.next();
    %>
		<option value="<%=sod.getValue()%>"><%=sod.getName()%></option>
		<%
            }
        %>
    </select>
    </span>
    </div>
    </span>
            <script>
                function selAction() {
                    openWin('flow_designer_action_sel.jsp', 200, 100);
                }

                function selField() {
                    openWin('flow_designer_field_sel.jsp?flowTypeCode=<%=flowTypeCode%>', 300, 220);
                }

                function setAction(iName, iText) {
                    userName.value = '<%=WorkflowActionDb.PRE_TYPE_ACTION_USER%>_' + iName;
                    userRealName.value = iText;
                    nodeMode.value = '1';
                    spanMode.innerHTML = iText;
                    ModifyAction(false);
                }

                function setField(userNames, userRealNames) {
                    userName.value = '<%=WorkflowActionDb.PRE_TYPE_FIELD_USER%>_' + userNames;
                    userRealName.value = userRealNames;
                    nodeMode.value = '1';
                    spanMode.innerHTML = userRealNames;
                    ModifyAction(false);
                }

                function setProjectRole() {
                    if ($("#projectRole").val() == "") {
                        userName.value = "";
                        userRealName.value = "";
                        return;
                    }
                    var txt = $("#projectRole").find("option:selected").text();
                    userName.value = "<%=WorkflowActionDb.PRE_TYPE_PROJECT_ROLE%>_" + $("#projectRole").val();
                    userRealName.value = txt;
                    nodeMode.value = '1';
                    spanMode.innerHTML = txt;
                    ModifyAction(false);
                }
            </script>

            <script>
                $(function () {
                    $('#afBtn').click(function () {
                        if ($(this).html().indexOf("down") == -1) {
                            $('#afBtnImg')[0].src = "<%=SkinMgr.getSkinPath(request)%>/images/af_arrow_down.png";
                            $('#afBox').next().hide();
                        } else {
                            $('#afBtnImg')[0].src = "<%=SkinMgr.getSkinPath(request)%>/images/af_arrow_up.png";
                            $('#afBox').next().show();
                        }
                    });
                });
            </script>

        </td>
    </tr>
    <tr id="nodeScriptTr" style="display:none">
        <td height="22" align="left">脚本选人</td>
        <td height="22">
            <%
                Leaf flowlf = new Leaf();
                flowlf = flowlf.getLeaf(flowTypeCode);
            %>
            <textarea id="nodeScript" name="nodeScript" style="width:100%; height:200px"></textarea>
            <input type="button" style="margin-top:3px" value="设计器" class="btn" onclick="openIdeWin()"/>
        </td>
    </tr>
    <tr>
        <td height="22" align="left">标题</td>
        <td height="22"><input id="title" type="text" name="title" size="30" onchange="ModifyAction(false)"></td>
    </tr>
    <tr style="display:none">
        <td height="22" align="left">内部编码</td>
        <td height="22"><%=internalName%>
        </td>
    </tr>
    <tr>
        <td height="22" align="left" bgcolor="#F9FAD3" title="角色与组织机构(行文方向)、职级、部门相关联">角色关联</td>
        <td height="22" bgcolor="#F9FAD3"><input onchange="ModifyAction(false)" type=checkbox id="relateRoleToOrganization" name="relateRoleToOrganization" value="1" checked title="设为关联后，如果角色中存在有多个用户，系统将自动根据组织机构图及行文方向就近匹配" onclick="checkRelation()"/></td>
    </tr>
    <tr>
        <td height="22" align="left" bgcolor="#F9FAD3" title="上一节点至本节点的行文方向">行文方向</td>
        <td height="22" bgcolor="#F9FAD3">
            <textarea id="userName" name="userName" rows="3" readonly style="display:none;background-color:#eeeeee"><%=userName%></textarea>
            <input id="nodeMode" name="nodeMode" type="hidden" size="5" readonly value="<%=nodeMode%>">
            <font color="red" style="display:none">当前为：<span id="spanMode" name="spanMode"></span></font>
            <span id="span_direction">
      <select onchange="ModifyAction(false)" id="direction" name="direction">
      <option value="2" selected>上级</option>
      <option value="<%=WorkflowActionDb.DIRECTION_MYDEPT_UP%>">先本部门后上级</option>      
      <option value="0">下级</option>
      <option value="1">先本部门后平级</option>
      <option value="<%=WorkflowActionDb.DIRECTION_PARALLEL_MYDEPT_UP%>">本部门、平级及上级部门</option>
      <option value="3">本部门</option>
        <%
            if (cfg.getBooleanProperty("isMyLeaderUsed")) {
        %>
      <option value="<%=WorkflowActionDb.DIRECTION_MY_LEADER%>">在本部门或上级部门找我的领导</option>
          <%
              }
          %>
      </select>
      </span>
        </td>
    </tr>
    <tr>
        <td height="22" align="left" bgcolor="#F9FAD3" title="上一节点至本节点的行文方向">关联节点</td>
        <td height="22" bgcolor="#F9FAD3">
            <select id="relateToAction" name="relateToAction" title="开始节点至下一节点不支持关联到表单中的部门字段">
                <option value="<%=WorkflowActionDb.RELATE_TO_ACTION_DEFAULT%>">关联上一节点</option>
                <option value="<%=WorkflowActionDb.RELATE_TO_ACTION_STARTER%>">关联开始节点</option>
                <option value="<%=WorkflowActionDb.RELATE_TO_ACTION_DEPT%>">关联表单中的部门字段</option>
            </select>
        </td>
    </tr>
    <tr>
        <td height="22" align="left" bgcolor="#F9FAD3" title="用户是否能管理关联节点上的人员">部门管理</td>
        <td height="22" bgcolor="#F9FAD3">
            <input id="relateDeptManager" name="relateDeptManager" value="1" type="checkbox" title="用户是否能管理关联节点上的人员"/>
        </td>
    </tr>
    <tr>
        <td height="22" align="left">角色/用户</td>
        <td height="22"><textarea id="userRealName" name="userRealName" readonly rows="3" style="width:180px;background-color:#eeeeee"><%=userRealName%></textarea></td>
    </tr>
    <tr style="display:none">
        <td height="22" align="left">用户职级</td>
        <td height="22">
            <select id="rank" name="rank" onchange="ModifyAction(false)">
                <option value="">不限定</option>
            </select>
            <input id="checkState" name="checkState" value="<%=WorkflowActionDb.STATE_NOTDO%>" type="hidden"></td>
    </tr>
    <tr style="display:none">
        <td height="22" align="left">审批颜色</td>
        <td height="22"><select onchange="ModifyAction(false)" id="OfficeColorIndex" name="OfficeColorIndex" style="width:80px" title="Office文件中批注审批所用的颜色(需安装签名批注插件)">
            <option selected style="BACKGROUND: red" value="6"></option>
            <option style="BACKGROUND: Turquoise" value="3"></option>
            <option style="BACKGROUND: #00ff00" value="4"></option>
            <option style="BACKGROUND: Pink" value="5"></option>
            <option style="BACKGROUND: yellow" value="7"></option>
            <option style="BACKGROUND: black" value="1"></option>
            <option style="BACKGROUND: blue" value="2"></option>
            <option style="BACKGROUND: white" value="8"></option>
            <option style="BACKGROUND: DarkBlue" value="9"></option>
            <option style="BACKGROUND: Teal" value="10"></option>
            <option style="BACKGROUND: green" value="11"></option>
            <option style="BACKGROUND: Violet" value="12"></option>
            <option style="BACKGROUND: DarkRed" value="13"></option>
            <option style="BACKGROUND: #FFCC67" value="14"></option>
            <option style="BACKGROUND: #808080" value="15"></option>
            <option style="BACKGROUND: #C0C0C0" value="16"></option>
        </select></td>
    </tr>
    <tr id="trFieldWritable">
        <td height="22" align="left">可写字段</td>
        <td height="22"><textarea title="可以填写的字段" name="fieldWriteText" rows="3" readonly="" id="fieldWriteText" style="width: 180px;background-color:#eeeeee"></textarea>
            <a href="javascript:OpenFormFieldSelWin('fieldWrite', 'fieldWriteText');">选择 </a>
            <input name="fieldWrite" type="hidden" id="fieldWrite" value="<%=fieldWrite%>"/>
        </td>
    </tr>
    <tr id="trFieldHided">
        <td height="22" align="left">隐藏字段</td>
        <td height="22"><textarea title="字段被隐藏后，在处理流程时将不可见" name="fieldHideText" rows="3" readonly="readonly" id="fieldHideText" style="width: 180px;background-color:#eeeeee"></textarea>
            <a href="javascript:OpenFormFieldSelWin('fieldHide', 'fieldHideText');">选择 </a>
            <input name="fieldHide" type="hidden" id="fieldHide" value="<%=fieldHide%>"/>
        </td>
    </tr>
    <tr>
        <td height="22" align="left">结束节点</td>
        <td height="22"><select onchange="ModifyAction(false)" id="item1" name="item1" title="如果是结束节点，则该节点处理后流程变为结束状态">
            <option value="1">是</option>
            <option value="0">否</option>
        </select></td>
    </tr>
    <tr id="trFlag">
        <td height="22" align="left">标志位</td>
        <td height="22"><input type=checkbox id="flagSel" name="flagSel" value="0" checked title="允许选择下一节点上的用户"/>
            选择用户
            <input onchange="ModifyAction(false)" type="checkbox" id="flagDiscardFlow" name="flagDiscardFlow" value="1" checked/>
            放弃流程
            <br/>
            <input onchange="ModifyAction(false)" type="checkbox" id="flagDelFlow" name="flagDelFlow" value="1" checked/>
            删除流程
            <input onchange="ModifyAction(false)" type="checkbox" id="flagEditAttach" name="flagEditAttach" value="1" checked/>
            编辑附件
            <br/>
            <input onchange="ModifyAction(false)" type="checkbox" id="flagDelAttach" name="flagDelAttach" value="1" checked/>
            删除附件
            <input onchange="ModifyAction(false)" type="checkbox" id="flagXorRadiate" name="flagXorRadiate" value="1" checked title="根据条件判断走相应的分支线"/>
            条件分支
            <br/>
            <input onchange="ModifyAction(false)" type="checkbox" id="flagXorAggregate" name="flagXorAggregate" value="1" checked title="节点有多条路径汇合，如果置为异步，则只要有其中的一条到达，节点就会被激活，否则，只有当所有路径都到达后才会继续"/>
            异步聚合
            <input onchange="ModifyAction(false)" type="checkbox" id="flagFinishFlow" name="flagFinishFlow" value="1" checked title="流程处理者可以拒绝流程，同时流程结束"/>
            拒绝流程
            <br/>
            <input title="模板套红" onchange="ModifyAction(false)" type="checkbox" id="flagReceiveRevise" name="flagReceiveRevise" value="1" checked/>
            模板套红
            <%
                String disBtnName = "流程抄送";
                String disBtnDesc = "将流程表单分发给相关人员";
                String kind = com.redmoon.oa.kernel.License.getInstance().getKind();
                if (kind.equalsIgnoreCase(com.redmoon.oa.kernel.License.KIND_COM)) {
                    disBtnName = "流程知会";
                    disBtnDesc = "将流程表单知会给相关人员";
                }
            %>
            <input title="<%=disBtnDesc %>" onchange="ModifyAction(false)" type="checkbox" id="flagModify" name="flagModify" value="1" checked/>
            <%=disBtnName %>
            <br/>
            <input title="同意并结束流程，可用于非开始节点" onchange="ModifyAction(false)" type="checkbox" id="flagAgreeAndFinish" name="flagAgreeAndFinish" value="1" checked/>
            结束流程
            <input title="同一节点中有多人处理时，每个人都可以立即往下提交，并且不能更改下一节点上之前被选择的用户" onchange="ModifyAction(false)" type="checkbox" id="flagXorFinish" name="flagXorFinish" value="1"/>
            异步提交
            <br/>
            <input title="同一节点中有多人处理时，退回时不会忽略本节点其他人及其他节点上的待办记录，并且在处理完毕再次提交时，不能更改之前选择的用户" onchange="ModifyAction(false)" type="checkbox" id="flagXorReturn" name="flagXorReturn" value="1"/>
            异步退回
            <iframe id=hiddenframe name=hiddenframe src="flow_predefine_action_modify_getfieldtitle.jsp" style="display:none" width=0 height=0></iframe>
        </td>
    </tr>
    <tr>
        <td height="22" colspan="2" align="left" bgcolor="#cff4fa">高级选项</td>
    </tr>
    <tr>
        <td height="22" align="left">分配策略</td>
        <td height="22">
            <select onchange="ModifyAction(false)" id="strategy" name="strategy" title="上一节点的用户选择本节点用户时应用的策略">
                <option value="">处理者指定</option>
                <!--<option value="">全部人员</option>-->
                <%
                    StrategyMgr sm = new StrategyMgr();
                    Vector smv = sm.getAllStrategy();
                    String smopts = "";
                    if (smv != null) {
                        Iterator smir = smv.iterator();
                        while (smir.hasNext()) {
                            StrategyUnit su = (StrategyUnit) smir.next();
                            smopts += "<option value='" + su.getCode() + "' isSelectable='" + su.getIStrategy().isSelectable() + "'>" + su.getName() + "</option>";
                        }
                    }
                    out.print(smopts);
                %>
            </select>
            </br>
            <span id="x_span" style='display:none'>x=<input onchange="ModifyAction(false)" id='x_num' name='x_num' type='text'/></span></td>
    </tr>
    <tr id="trIgnoreType" title="当下一节点仅有一个用户时，用户之前处理过才可以跳过">
        <td height="22" align="left">跳过方式</td>
        <td height="22">
            <select id="ignoreType" name="ignoreType" title="如设为无用户时跳过，则未选择用户时，也将被跳过">
                <option value="<%=WorkflowActionDb.IGNORE_TYPE_DEFAULT%>">无用户时跳过</option>
                <option value="<%=WorkflowActionDb.IGNORE_TYPE_NOT%>" selected>无用户时不允许跳过</option>
                <option value="<%=WorkflowActionDb.IGNORE_TYPE_USER_ACCESSED_BEFORE%>">无用户或用户之前处理过则跳过</option>
                <option value="<%=WorkflowActionDb.IGNORE_TYPE_ROLE_COMPARE_NOT%>">角色比较大小时不允许跳过</option>
            </select>
        </td>
    </tr>
    <tr>
        <td height="22" align="left">动作</td>
        <td height="22">
            <select id="kind" name="kind" onchange="onKindChange()">
                <option value="<%=WorkflowActionDb.KIND_ACCESS%>">处理</option>
                <option value="<%=WorkflowActionDb.KIND_READ%>">审阅</option>
                <option value="<%=WorkflowActionDb.KIND_SUB_FLOW%>">子流程</option>
            </select>
        </td>
    </tr>
    <tr>
        <td height="22" align="left">分支模式</td>
        <td height="22">
            <select id="branchMode" name="branchMode" title="当分支线上无条件时，分支模式才生效">
                <option value="<%=WorkflowActionDb.BRANCH_MODE_MULTI%>">多选</option>
                <option value="<%=WorkflowActionDb.BRANCH_MODE_SINGLE%>">单选</option>
            </select>
        </td>
    </tr>
    <tr id="isDelayTr">
        <td height="22" align="left">延迟接收</td>
        <td height="22">
            <input type="checkbox" onclick="ModifyAction(false)" id="isDelayed" name="isDelayed" value="1">
            延迟
            <input id="timeDelayedValue" name="timeDelayedValue" size="3"/>
            <select id="timeDelayedUnit" name="timeDelayedUnit">
                <option value="<%=WorkflowActionDb.TIME_UNIT_DAY%>">天</option>
                <option value="<%=WorkflowActionDb.TIME_UNIT_HOUR%>">小时</option>
                <option value="<%=WorkflowActionDb.TIME_UNIT_WORKDAY%>">工作日</option>
                <option value="<%=WorkflowActionDb.TIME_UNIT_WORKHOUR%>">工作小时</option>
            </select>
            <span title="当只有一个前置节点即入度为1时才有效">
    <br/>
    <input id="canPrivUserModifyDelayDate" name="canPrivUserModifyDelayDate" type="checkbox"/>
    前一用户可修改延迟时间
	</span>
        </td>
    </tr>
    <tr>
        <td height="22" align="left">限定部门</td>
        <td height="22"><textarea name="deptName" rows="3" readonly id="deptName" title="只有限定部门内的人员才能处理" style="width: 180px;background-color:#eeeeee"></textarea>
            <a href="javascript:openWinDepts();">选择</a>
            <input name="dept" type="hidden" id="dept" value="<%=dept%>"/>
        </td>
    </tr>
    <tr>
        <td height="22" align="left">限定部门表单域</td>
        <td height="22">
            <select id="deptField" name="deptField" title="只有限定部门控件内的人员才能处理，当角色关联行文方向时无效">
                <option value=""></option>
                <%
                    Leaf lf = new Leaf();
                    lf = lf.getLeaf(flowTypeCode);
                    FormDb fd = new FormDb();
                    fd = fd.getFormDb(lf.getFormCode());
                    Iterator irField = fd.getFields().iterator();
                    while (irField.hasNext()) {
                        FormField ff = (FormField) irField.next();
                        if (FormField.TYPE_MACRO.equals(ff.getType())) {
                            if (ff.getMacroType().equals("macro_dept_select") || ff.getMacroType().equals("macro_my_dept_select")
                                    || ff.getMacroType().equals("macro_dept_sel_win")
                                    || ff.getMacroType().equals("macro_moredeptctl")
                            ) {
                %>
                            <option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
                <%
                            }
                        }
                    }
                %>
            </select>
        </td>
    </tr>
    <tr>
        <td height="22" align="left">被退回可重选用户</td>
        <td height="22">
            <input title="被退回时是否可重选用户，不勾选表示只能提交给退回者" type="checkbox" id="canSelUserWhenReturned" name="canSelUserWhenReturned" value="1" />
        </td>
    </tr>
    <tr id="trArchive">
        <td height="22" align="left">归档</td>
        <td height="22"><select onchange="ModifyAction(false)" id="flagSaveArchive" name="flagSaveArchive">
            <option value="0" selected>不存档</option>
            <option value="1">手工存档</option>
            <option value="2">自动存档</option>
            <!--<option value="3">公文存档</option>-->
        </select></td>
    </tr>
    <tr>
        <td height="22" align="left">表单视图</td>
        <td height="22">
            <select id="formView" name="formView">
                <option value="<%=WorkflowActionDb.VIEW_DEFAULT%>">默认</option>
                <%
                    FormViewDb fvd = new FormViewDb();
                    Iterator irv = fvd.getViews(flowlf.getFormCode()).iterator();
                    while (irv.hasNext()) {
                        fvd = (FormViewDb) irv.next();
                %>
                <option value="<%=fvd.getInt("id")%>"><%=fvd.getString("name")%>
                </option>
                <%
                    }
                %>
            </select>
        </td>
    </tr>
    <tr>
        <td height="22" align="left">是否提醒</td>
        <td height="22">
            <input title="在本节点提交时是否发送消息提醒下一步处理人员" type="checkbox" id="isMsg" name="isMsg" value="1" checked />
        </td>
    </tr>
    <tr>
        <td height="22" align="left">提醒人员</td>
        <td height="22">
            <div>
                <img src="images/combination.png" style="margin-bottom:-5px;margin-top:5px;"/>&nbsp;<a href="javascript:;" title="如果不设置，默认仅提醒下一步处理人员" onclick="openMsgPropDlg()">配置</a>&nbsp;
                <img src="images/gou.png" style="margin-bottom:-5px;width:20px;height:20px;display:none" id="imgComb"/>
            </div>
            <script>
                function openMsgPropDlg() {
                    openWin("flow_designer_msg_prop.jsp?flowTypeCode=<%=flowTypeCode%>&internalName=<%=internalName%>", 700, 600);
                }
            </script>
        </td>
    </tr>
    <%
        boolean isPlatformSrc = com.redmoon.oa.kernel.License.getInstance().isPlatformSrc();
        String dis = isPlatformSrc ? "" : "display:none";
    %>
    <tr style="<%=dis%>">
        <td height="22" align="left">模块过滤</td>
        <td height="22"><input id="isModuleFilter" name="isModuleFilter" value="1" type="checkbox" title="如果存在用嵌套表格2，是否启用其模块中配置的过滤条件"/></td>
    </tr>
    <tr style="<%=dis%>">
        <td height="22" align="left">提交按钮</td>
        <td height="22">
            <input id="btnAgreeName" name="btnAgreeName" title="提交按钮的名称，空则默认为同意"/>
        </td>
    </tr>
    <tr style="<%=dis%>">
        <td height="22" align="left">拒绝按钮</td>
        <td height="22">
            <input id="btnRefuseName" name="btnRefuseName" title="拒绝按钮的名称，空则默认为拒绝"/>
        </td>
    </tr>
    <tr>
        <td height="22" align="left">审核人</td>
        <td height="22">
            <input id="isShowNextUsers" name="isShowNextUsers" title="是否显示下一节点上的审核人" value="1" type="checkbox" checked/>显示下一节点上的审核人
        </td>
    </tr>
    <tr style="<%=dis%>">
        <td height="22" align="left">流转页面</td>
        <td height="22">
            <input id="redirectUrl" name="redirectUrl" title="交办至下一节点后重定向的页面"/></td>
    </tr>
    <tr>
        <td height="22" align="left">内部名称</td>
        <td height="22">
            <input id="internalName" value="<%=internalName%>" style="width:98%" readonly onfocus="this.select()" title="节点的内部名称，用于二次开发"/>
        </td>
    </tr>
    <tr id="trProp">
        <td height="22" align="left">子流程类型</td>
        <td height="22">
            <select id="subFlowTypeCode" name="subFlowTypeCode" onchange="getSubFields()">
                <option value="">请选择</option>
                <%
                    lf = lf.getLeaf(Leaf.CODE_ROOT);
                    Iterator ir = lf.getChildren().iterator();
                    while (ir.hasNext()) {
                        lf = (Leaf) ir.next();
                        LeafPriv lp = new LeafPriv(lf.getCode());
                        if (!lp.canUserExamine(privilege.getUser(request)))
                            continue;

                        if (lf.getType() != 0) {
                %>
                <option value="<%=lf.getCode()%>"><%=lf.getName()%>
                </option>
                <%
                } else {
                    Iterator ir2 = lf.getAllChild(new Vector(), lf).iterator();
                    while (ir2.hasNext()) {
                        lf = (Leaf) ir2.next();
                        if (lf.getType() != 0) {
                %>
                <option value="<%=lf.getCode()%>"><%=lf.getName()%>
                </option>
                <%
                                }
                            }
                        }
                    }
                %>
            </select>
        </td>
    </tr>
    <tr id="trMapField">
        <td height="22" align="left">字段</td>
        <td height="22">
            <table border="0" cellspacing="0" style="border:0px">
                <tr>
                    <td style="width:100px;border:0px">父流程</td>
                    <td style="border:0px">子流程</td>
                </tr>
            </table>
            <select id="parentFields" name="parentFields" size="10" style="width:90px;height:150px;">
                <%
                    lf = lf.getLeaf(flowTypeCode);
                    ir = fd.getFields().iterator();
                    while (ir.hasNext()) {
                        FormField ff = (FormField) ir.next();
                %>
                <option value="<%=ff.getName()%>"><%=ff.getTitle()%>
                </option>
                <%
                    }
                %>
            </select>

            <select id="subFields" name="subFields" size="10" style="width:90px;height:150px;">
            </select>
            <br/>
            <input class="btn" type="button" onclick="addMap(true)" value="父→子"/>
            &nbsp;&nbsp;
            <input class="btn" type="button" onclick="addMap(false)" value="子→父"/>
        </td>
    </tr>
    <tr id="trP2S">
        <td height="22" align="left">父&nbsp;→&nbsp;子</td>
        <td height="22">
            <div id="p2s"></div>
        </td>
    </tr>
    <tr id="trS2P">
        <td height="22" align="left">子&nbsp;→&nbsp;父</td>
        <td height="22">
            <div id="s2P"></div>
        </td>
    </tr>
</table>
<textarea id="msgProp" style="display:none"></textarea>
</BODY>
<style>
    .mapItem {
    }

    .delSpan {
        cursor: pointer;
        color: red;
        font-size: 16px
    }
</style>
<script>
    function addMap(isParentToSub) {
        var pVal = $("#parentFields").val();
        var pText = $("#parentFields").find("option:selected").text();
        var sVal = $("#subFields").val();
        var sText = $("#subFields").find("option:selected").text();
        if (pVal == "" || sVal == "" || pVal == null || sVal == null) {
            jAlert("请选择字段！", "提示");
            return;
        }

        if (isMapExist(isParentToSub, pVal, sVal)) {
            jAlert("存在重复映射！", "提示");
            return;
        }

        if (isParentToSub)
            makeP2S(tmpId, pVal, pText, sVal, sText);
        else
            makeS2P(tmpId, pVal, pText, sVal, sText);

        tmpId++;
    }

    function makeP2S(tmpId, pVal, pText, sVal, sText) {
        $("#p2s").html($("#p2s").html() + "<div id='pMapDiv" + tmpId + "' class='mapItem' parentField='" + pVal + "' subField='" + sVal + "' parentTitle='" + pText + "' subTitle='" + sText + "'>" + pText + "→" + sText + "<span class='delSpan' onclick=\"$('#pMapDiv" + tmpId + "').remove()\">×</span></div>");
    }

    function makeS2P(tmpId, pVal, pText, sVal, sText) {
        $("#s2P").html($("#s2P").html() + "<div id='sMapDiv" + tmpId + "' class='mapItem' parentField='" + pVal + "' subField='" + sVal + "' parentTitle='" + pText + "' subTitle='" + sText + "'>" + sText + "→" + pText + "<span class='delSpan' onclick=\"$('#sMapDiv" + tmpId + "').remove()\">×</span></div>");
    }

    function makeSubFlowProp() {
        // {subFlowTypeCode:...,parentToSubMap:{parentField:subField,...},subToParentMap:{subField:parentField}}
        var str = "";
        if (o("kind").value == "<%=WorkflowActionDb.KIND_SUB_FLOW%>") {
            str = "\"subFlowTypeCode\":\"" + $("#subFlowTypeCode").val() + "\"";
            var parentToSubMap = "";
            $("div[id^='pMapDiv']").each(function () {
                if (parentToSubMap == "") {
                    parentToSubMap = "{\"parentField\":\"" + $(this).attr("parentField") + "\", \"subField\":\"" + $(this).attr("subField") + "\", \"parentTitle\":\"" + $(this).attr("parentTitle").trim() + "\", \"subTitle\":\"" + $(this).attr("subTitle") + "\"}";
                } else {
                    parentToSubMap += ",{\"parentField\":\"" + $(this).attr("parentField") + "\", \"subField\":\"" + $(this).attr("subField") + "\", \"parentTitle\":\"" + $(this).attr("parentTitle").trim() + "\", \"subTitle\":\"" + $(this).attr("subTitle") + "\"}";
                }
            });
            var subToParentMap = "";
            $("div[id^='sMapDiv']").each(function () {
                if (subToParentMap == "") {
                    subToParentMap = "{\"parentField\":\"" + $(this).attr("parentField") + "\", \"subField\":\"" + $(this).attr("subField") + "\", \"parentTitle\":\"" + $(this).attr("parentTitle").trim() + "\", \"subTitle\":\"" + $(this).attr("subTitle") + "\"}";
                } else {
                    subToParentMap += ",{\"parentField\":\"" + $(this).attr("parentField") + "\", \"subField\":\"" + $(this).attr("subField") + "\", \"parentTitle\":\"" + $(this).attr("parentTitle").trim() + "\", \"subTitle\":\"" + $(this).attr("subTitle") + "\"}";
                }
            });
            str = "{" + str + ",\"parentToSubMap\":[" + parentToSubMap + "], \"subToParentMap\":[" + subToParentMap + "]}";
        }

        // 查找internalName对应的项，如果没有，则添加
        var xml = $.parseXML(window.parent.getProps());
        $xml = $(xml);

        $xml.find("actions").children().each(function (i) {
            if ($(this).attr("internalName") == "<%=internalName%>") {
                // 删除原节点
                $(this).remove();
                return false;

                /*
                // 更新xml中对应action节点的内容
                $(this).find("property").text(str);
                $(this).find("redirectUrl").text(o("redirectUrl").value);
                isFound = true;
                return false;
                */
            }
        });

        // 新增节点
        var $elem = $($.parseXML("<action internalName='<%=internalName%>'><property>" + str + "</property>"
            + "<btnAgreeName>" + o("btnAgreeName").value + "</btnAgreeName>"
            + "<btnRefuseName>" + o("btnRefuseName").value + "</btnRefuseName>"
            + "<isShowNextUsers>" + (o("isShowNextUsers").checked?o("isShowNextUsers").value:0) +
            "</isShowNextUsers><redirectUrl>" + o("redirectUrl").value + "</redirectUrl><nodeScript>" +
            o("nodeScript").value + "</nodeScript><isModuleFilter>" + (o("isModuleFilter").checked ? o("isModuleFilter").value : "") + "</isModuleFilter><branchMode>" +
            o("branchMode").value + "</branchMode><deptField>" + o("deptField").value + "</deptField><canSelUserWhenReturned>" + (o("canSelUserWhenReturned").checked ? o("canSelUserWhenReturned").value : "") + "</canSelUserWhenReturned></action>"));
        var newNode = null;
        if (typeof document.importNode == 'function') {
            newNode = document.importNode($elem.find('action').get(0), true);
        } else {
            newNode = $elem.find('action').get(0);
        }
        $xml.find("actions").get(0).appendChild(newNode);

        window.parent.setProps($xml.xml());
    }

    function isMapExist(isParentToSub, pVal, sVal) {
        var mapDiv;
        if (isParentToSub) {
            mapDiv = "pMapDiv";
        } else {
            mapDiv = "sMapDiv";
        }
        var isFound = false;
        $("div[id^='" + mapDiv + "']").each(function () {
            if (pVal == $(this).attr("parentField") && sVal == $(this).attr("subField")) {
                isFound = true;
            }
        });
        return isFound;
    }

    function switchKind(kind) {
        if (kind == "<%=WorkflowActionDb.KIND_SUB_FLOW%>") {
            $("#trFieldWritable").hide();
            $("#trFieldHided").hide();
            $("#trIgnoreType").hide();
            $("#trArchive").hide();
            $("#trFlag").hide();

            $("#trProp").show();
            $("#trMapField").show();
            $("#trP2S").show();
            $("#trS2P").show();
        } else {
            $("#trFieldWritable").show();
            $("#trFieldHided").show();
            $("#trIgnoreType").show();
            $("#trArchive").show();
            $("#trFlag").show();

            $("#trProp").hide();
            $("#trMapField").hide();
            $("#trP2S").hide();
            $("#trS2P").hide();
        }
    }

    function onKindChange() {
        switchKind($("#kind").val());
        ModifyAction(false);
    }

    function getSubFields() {
        $.get(
            "getFieldsAsOptions.do",
            {
                flowTypeCode: $("#subFlowTypeCode").val()
            },
            function (data) {
                data = $.parseJSON(data);
                if (data.ret == 1) {
                    $("#subFields").html(data.options);
                }
                else {
                    consoleLog(data.msg);
                }
            }
        );
    }

    function getScript() {
        return o("nodeScript").value;
    }

    function setScript(script) {
        o("nodeScript").value = script;
    }

    function getFlowString() {
        return window.parent.getFlowString();
    }

    function getMsgProp() {
        return o("msgProp").value;
    }

    function setMsgProp(msgProp) {
        o("msgProp").value = msgProp;
    }

    <%
    com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
    com.redmoon.oa.SpConfig spCfg = new com.redmoon.oa.SpConfig();
    String version = StrUtil.getNullStr(oaCfg.get("version"));
    String spVersion = StrUtil.getNullStr(spCfg.get("version"));
	%>
    var ideUrl = "script_frame.jsp?formCode=<%=lf.getFormCode()%>";
    var ideWin;
    var cwsToken = "";

    function openIdeWin() {
        ideWin = openWinMax(ideUrl);
    }

    var onMessage = function (e) {
        var d = e.data;
        var data = d.data;
        var type = d.type;
        if (type == "setScript") {
            setScript(data);
            if (d.cwsToken!=null) {
                cwsToken = d.cwsToken;
                ideUrl = "script_frame.jsp?formCode=<%=lf.getFormCode()%>&cwsToken=" + cwsToken;
            }
        } else if (type == "getScript") {
            var data = {
                "type": "openerScript",
                "version": "<%=version%>",
                "spVersion": "<%=spVersion%>",
                "scene": "flow.node",
                "data": getScript()
            }
            ideWin.leftFrame.postMessage(data, '*');
        } else if (type == "setCwsToken") {
            cwsToken = d.cwsToken;
            ideUrl = "script_frame.jsp?formCode=<%=lf.getFormCode()%>&cwsToken=" + cwsToken;
        }
    }

    $(function () {
        if (window.addEventListener) { // all browsers except IE before version 9
            window.addEventListener("message", onMessage, false);
        } else {
            if (window.attachEvent) { // IE before version 9
                window.attachEvent("onmessage", onMessage);
            }
        }
    });
</script>
</HTML>