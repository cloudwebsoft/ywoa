<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="com.redmoon.oa.flow.Leaf" %>
<%@ page import="com.redmoon.oa.flow.LeafPriv" %>
<%@ page import="com.redmoon.oa.flow.WorkflowPredefineDb" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
    String op = ParamUtil.get(request, "op");

    WorkflowPredefineDb wpd = new WorkflowPredefineDb();
    wpd = wpd.getDefaultPredefineFlow(flowTypeCode);
    com.redmoon.clouddisk.Config cfgNd = com.redmoon.clouddisk.Config.getInstance();
    boolean isUsed = cfgNd.getBooleanProperty("isUsed"); //判断网盘是否启用
    if (wpd != null) {
        op = "edit";
    }

    com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="renderer" content="ie-stand"/>
    <meta http-equiv="pragma" content="no-cache"/>
    <meta http-equiv="Cache-Control" content="no-cache,must-revalidate"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>流程设计器</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <%@ include file="../inc/nocache.jsp" %>
    <script src="../inc/common.js"></script>
    <%--<script src="../js/jquery-1.9.1.min.js"></script>--%>
    <%--<script src="../js/jquery-migrate-1.2.1.min.js"></script>--%>

    <script type="text/javascript" src="../js/jquery.js"></script>
    <%--<link type="text/css" href="../js/flow/lib/jquery-ui-1.8.4.custom/css/smoothness/jquery-ui-1.8.4.custom.css" rel="stylesheet"/>--%>
    <%--<script type="text/javascript" src="../js/flow/lib/jquery-ui-1.8.4.custom/js/jquery-ui.min.js"></script>--%>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
    <script type="text/javascript" src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <link rel="stylesheet" type="text/css" href="../js/flow/myflow.css">
    <script type="text/javascript" src="../js/flow/lib/raphael-min.js"></script>
    <script type="text/javascript" src="../js/jquery.toaster.js"></script>
    <script type="text/javascript" src="../js/flow/myflow.min.js"></script>
    <script type="text/javascript" src="../js/flow/myflow.jpdl.js"></script>
    <script type="text/javascript" src="../js/flow/myflow.editors.js"></script>
    <script type="text/javascript" src="../js/crypto-js.min.js"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script type="text/javascript" src="../js/jquery-alerts/jquery.alerts.js"></script>
    <script type="text/javascript" src="../js/jquery-alerts/cws.alerts.js"></script>
    <script language=javascript>
        <!--
        var actionSelected = false;
        var curAction = null;
        var curLink = null;

        var modifyWin;

        function openWin(url, width, height) {
            modifyWin = window.open(url, "designer", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width=" + width + ",height=" + height);
            modifyWin.focus();
        }

        var curInternalName = "";

        function ModifyActionNotSubmit(user, title, clrindex, userRealName, jobCode, jobName, proxyJobCode, proxyJobName, proxyUserName, proxyUserRealName, fieldWrite, checkState, dept, flag, nodeMode, strategy, isEndNode, item2, isMsg) {
            var myflow = $.myflow;
            var _r = myflow.getStates()[curInternalName];
            if (_r == null) {
                return;
            }

            // 清除属性
            _r.clearProps();

            _r.setText(jobName + "：" + title);
            _r.setProp("ActionUser", user);
            _r.setProp("ActionTitle", title);

            _r.setProp("ActionUserRealName", userRealName);
            _r.setProp("ActionJobCode", jobCode);
            _r.setProp("ActionJobName", jobName);
            _r.setProp("ActionProxyJobCode", proxyJobCode);
            _r.setProp("ActionProxyJobName", proxyJobName);
            _r.setProp("ActionProxyUserName", proxyUserName);
            _r.setProp("ActionProxyUserRealName", proxyUserRealName);
            _r.setProp("ActionColorIndex", clrindex);
            _r.setProp("ActionFieldWrite", fieldWrite);
            _r.setProp("ActionCheckState", checkState);

            _r.setProp("ActionDept", dept);
            _r.setProp("ActionFlag", flag);
            _r.setProp("ActionDeptMode", Number(nodeMode));
            _r.setProp("ActionStrategy", strategy);
            _r.setProp("ActionItem1", isEndNode);
            _r.setProp("ActionItem2", item2);
            _r.setProp("ActionIsMsg", isMsg);

            // console.log(_r.toJson());
            if (isEndNode == 1) {
                _r.setType("end");
            }

            return;
        }

        function ModifyAction(user, title, clrindex, userRealName, jobCode, jobName, proxyJobCode, proxyJobName, proxyUserName, proxyUserRealName, fieldWrite, checkState, dept, flag, nodeMode, strategy, isEndNode, item2, isMsg) {
            ModifyActionNotSubmit(user, title, clrindex, userRealName, jobCode, jobName, proxyJobCode, proxyJobName, proxyUserName, proxyUserRealName, fieldWrite, checkState, dept, flag, nodeMode, strategy, isEndNode, item2, isMsg);
            submitDesigner();
        }

        function getActionUser() {
            return curAction.getPropVal("ActionUser");
        }

        function getActionTitle() {
            return curAction.getPropVal("ActionTitle");
        }

        function getActionColorIndex() {
            return curAction.getPropVal("ActionColorIndex");
        }

        function getActionUserRealName() {
            return curAction.getPropVal("ActionUserRealName");
        }

        function getActionCheckState() {
            return curAction.getPropVal("ActionCheckState");
        }

        function getActionJobCode() {
            return curAction.getPropVal("ActionJobCode");
        }

        function getActionJobName() {
            return curAction.getPropVal("ActionJobName");
        }

        function getActionProxyJobCode() {
            return curAction.getPropVal("ActionProxyJobCode");
        }

        function getActionProxyJobName() {
            return curAction.getPropVal("ActionProxyJobName");
        }

        function getActionProxyUserName() {
            return curAction.getPropVal("ActionProxyUserName");
        }

        function getActionProxyUserRealName() {
            return curAction.getPropVal("ActionProxyUserRealName");
        }

        function getActionFieldWrite() {
            return curAction.getPropVal("ActionFieldWrite");
        }

        function getActionDept() {
            return curAction.getPropVal("ActionDept");
        }

        function getActionFlag() {
            return curAction.getPropVal("ActionFlag");
        }

        function getActionNodeMode() {
            return curAction.getPropVal("ActionDeptMode");
        }

        function getActionType() {
            return curAction.getPropVal("ActionType");
        }

        function getActionStrategy() {
            return curAction.getPropVal("ActionStrategy");
        }

        function getActionItem1() {
            return curAction.getPropVal("ActionItem1");
        }

        function getActionItem2() {
            return curAction.getPropVal("ActionItem2");
        }

        function getActionIsMsg() {
            return curAction.getPropVal("ActionIsMsg");
        }

        function GetActionProperty(actionName, prop) {
            console.log("GetActionProperty: actionName=" + actionName + " prop=" + prop);
            var myflow = $.myflow;
            var _r = myflow.getStates()[curAction.getId()];
            if (prop == "inDegree") {
                console.log("inDegree:" + _r.getInDegree());
                return _r.getInDegree();
            }
            else {
                return _r.getPropVal(prop);
            }
        }

        function getAction(internalName) {
            var myflow = $.myflow;
            return myflow.getStates()[internalName];
        }

        function getLink(linkId) {
            var myflow = $.myflow;
            return myflow.getPaths()[linkId];
        }

        function isActionSelected() {
            return actionSelected;
        }

        function OpenModifyWin(internalName) {
            actionSelected = true;
            curAction = getAction(internalName);
            console.log(curAction);
            console.log(curAction.getId());

            curInternalName = internalName;

            $('#tabs').tabs('option', 'active', 1);
            // o('actionPropIframe').src = "flow_designer_action_prop.jsp?flowTypeCode=<%=flowTypeCode%>" + "&hidFieldWrite=" + getActionFieldWrite() + "&internalName=" + internalName;
            //可写字段内容过多导致url过长，使flow_designer_action_prop.jsp页面为空
            o("flowTypeCode").value = "<%=flowTypeCode%>";
            o("hidFieldWrite").value = getActionFieldWrite();
            o("internalName").value = internalName;
            document.getElementById('myform').submit();

            <%if (license.isSrc()) {%>
            o('actionScriptIframe').src = "flow_designer_script_view.jsp?flowTypeCode=<%=flowTypeCode%>" + "&internalName=" + internalName;
            <%}%>
            <%if (license.isPlatformSrc()) {%>
            o('writeBackIframe').src = "flow_designer_write_back.jsp?flowTypeCode=<%=flowTypeCode%>" + "&internalName=" + internalName;
            <%}%>
        }

        function Operate() {
            OpenModifyWin();
        }

        function clearSelectedLinkProperty() {
            var myflow = $.myflow;
            // console.log(curLink);
            var _o = myflow.getPaths()[curLink.getId()];
            _o.clearProps();
        }

        function SetSelectedLinkProperty(propItem, propValue) {
            var myflow = $.myflow;
            var _o = myflow.getPaths()[curLink.getId()];
            _o.setProp(propItem, propValue);

            var desc = _o.getPropVal("desc");
            var expireHour = _o.getPropVal("expireHour");
            var text = "";
            if (expireHour!="") {
                _o.setText(desc + "    " + expireHour + " " + myflow.config.expireUnit);
            }
            else {
                _o.setText(desc);
            }

            // console.log(_o);
        }

        function GetSelectedLinkProperty(propItem) {
            var propVal = null;
            if (curLink!=null) {
                if (propItem == "from") {
                    propVal = curLink.from().getId();
                }
                else if (propItem == "to") {
                    propVal = curLink.to().getId();
                }
                else {
                    propVal = curLink.getPropVal(propItem);
                }
            }
            else {
                console.log("当前未选中连接线");
            }
            if (propVal == null) {
                propVal = "";
            }
            return propVal;
        }

        function OpenLinkPropertyWin(linkId) {
            curLink = getLink(linkId);

            var t = GetSelectedLinkProperty("title");
            var conditionType = GetSelectedLinkProperty("conditionType");
            t = encodeURI(t);

            $('#tabs').tabs('option', 'active', 1);
            // title在flow_designer_link_prop.jsp中传过去时用不到，并且可能因为title中含有脚本，导致url太长，而使得flow_designer_link_prop.jsp页面显示为空
            // o('actionPropIframe').src = "flow_designer_link_prop.jsp?linkProp="+$('#linkProp').val()+"&flowTypeCode=<%=flowTypeCode%>&conditionType=" + conditionType; // + "&title=" + t;
            var url = "flow_designer_link_prop.jsp";
            var tempForm = document.createElement("form");
            tempForm.id = "tempForm1";
            tempForm.method = "post";
            tempForm.action = url;

            var hideInput = document.createElement("input");
            hideInput.type = "hidden";
            hideInput.name = "linkProp"
            hideInput.value = $('#linkProp').html();
            tempForm.appendChild(hideInput);

            hideInput = document.createElement("input");
            hideInput.type = "hidden";
            hideInput.name = "flowTypeCode"
            hideInput.value = "<%=flowTypeCode%>";
            tempForm.appendChild(hideInput);

            hideInput = document.createElement("input");
            hideInput.type = "hidden";
            hideInput.name = "conditionType"
            hideInput.value = conditionType;
            tempForm.appendChild(hideInput);

            hideInput = document.createElement("input");
            hideInput.type = "hidden";
            hideInput.name = "title"
            hideInput.value = t;
            tempForm.appendChild(hideInput);

            document.body.appendChild(tempForm);
            tempForm.target = "actionPropIframe";
            tempForm.submit();
            document.body.removeChild(tempForm);

            // openWin("flow_predefine_link_modify.jsp?flowTypeCode=<%=flowTypeCode%>&conditionType=" + conditionType + "&title=" + t, 620, 320);
        }

        function OpenLinkPropertyNormalWin(linkId) {
            curLink = getLink(linkId);

            var t = GetSelectedLinkProperty("title");
            t = encodeURI(t);

            $('#tabs').tabs('option', 'active', 1);
            o('actionPropIframe').src = "flow_designer_link_normal_prop_myflow.jsp?title=" + t;
        }

        var isLeftMenuShow = true;

        function closeLeftMenu() {
            if (isLeftMenuShow) {
                window.parent.setCols("0,*");
                isLeftMenuShow = false;
                btnName.innerHTML = "打开菜单";
            } else {
                window.parent.setCols("200,*");
                isLeftMenuShow = true;
                btnName.innerHTML = "关闭菜单";
            }
        }

        //-->
    </script>
</head>
<body style="margin:0px;padding:0px">
<%
    LeafPriv lp = new LeafPriv(flowTypeCode);
    if (!(lp.canUserExamine(privilege.getUser(request)))) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    UserDb ud = new UserDb();
    ud = ud.getUserDb(privilege.getUser(request));

    String mode = "define";
    String flowString = "";
    String title = "";
    int id = 0;
    String returnBackChecked = "checked";
    String dirCode = "";
    int examine = 0;
    int returnMode = WorkflowPredefineDb.RETURN_MODE_NORMAL;
    int returnStyle = WorkflowPredefineDb.RETURN_STYLE_FREE;
    int roleRankMode = WorkflowPredefineDb.ROLE_RANK_MODE_NONE;
    if (op.equals("edit")) {
        // mode = "user";
        flowString = wpd.getFlowString();
        title = wpd.getTitle();
        id = wpd.getId();
        dirCode = wpd.getDirCode();
        if (!wpd.isReturnBack()) {
            returnBackChecked = "";
        }
        examine = wpd.getExamine();
        returnMode = wpd.getReturnMode();
        returnStyle = wpd.getReturnStyle();
        roleRankMode = wpd.getRoleRankMode();
    } else {
        Leaf lf = new Leaf();
        lf = lf.getLeaf(flowTypeCode);
        if (lf != null) {
            title = lf.getName();
        }
    }

    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    String flowExpireUnit = cfg.get("flowExpireUnit");
    if (flowExpireUnit.equals("day")) {
        flowExpireUnit = "天";
    } else {
        flowExpireUnit = "小时";
    }
%>
<div id="myflow_tools" class="ui-widget-content">
    <div id="myflow_tools_handle" style="text-align: center;" class="ui-widget-header">工具条
    </div>
    <div class="node" id="myflow_redraw"><img src="../js/flow/img/create.png"/>&nbsp;&nbsp;新建</div>
    <div class="node" id="myflow_save" style="display: none"><img src="../js/flow/img/save.png"/>&nbsp;&nbsp;保存</div>
    <div class="node" id="myflow_revoke"><img src="../js/flow/img/undo.png"/>&nbsp;&nbsp;撤销</div>
    <div class="node" onclick="window.location.reload()"><img src="../js/flow/img/refresh.png"/>&nbsp;&nbsp;刷新</div>
    <div class="node" id="myflow_del"><img src="../js/flow/img/del.png"/>&nbsp;&nbsp;删除</div>
    <div>
        <hr/>
    </div>
    <div class="node selectable selected" id="pointer"><img src="../js/flow/img/select.png"/>&nbsp;&nbsp;选择
    </div>
    <div class="node selectable" id="path"><img src="../js/flow/img/path.png"/>&nbsp;&nbsp;连线
    </div>
    <div class="node selectable" id="pathReturn"><img src="../js/flow/img/return.png"/>&nbsp;&nbsp;返回
    </div>
    <div>
        <hr/>
    </div>
    <div class="node state" id="task" type="task"><img src="../js/flow/img/task.png"/>&nbsp;&nbsp;任务
    </div>
</div>

<div id="myflow_props" class="ui-widget-content" style="display: none;">
    <div id="myflow_props_handle" class="ui-widget-header">属性</div>
    <div>&nbsp;</div>
</div>

<table id="designerTable" border="0" align="left" cellpadding="0" cellspacing="0" style="margin:0px; padding:0px; width:100%; table-layout: fixed">
    <tr>
        <td align="left" width="75%" style="width:75%; overflow: auto; vertical-align: top">
            <div id="myflow"></div>
        </td>
        <td align="left" valign="top">
            <div id="tabs">
                <ul>
                    <li><a href="#tabs-1">流程</a></li>
                    <li><a href="#tabs-2">属性</a></li>
                    <%
                        if (license.isPlatformSrc()) {
                    %>
                    <li><a href="#tabs-5">回写</a></li>
                    <%
                        }
                    %>
                    <%if (license.isSrc()) {%>
                    <li><a href="#tabs-4">事件</a></li>
                    <%}%>
                </ul>
                <div id="tabs-1" class="tabDiv">
                    <form style="margin:0px; padding:0px" action="" method="post" id="form1" name="form1">
                        <div style="text-align:left;padding:0px;margin:0px;padding-top:3px">
                            <table width="100%" border="0" cellpadding="0" cellspacing="0" class="tabStyle_1" style="width:100%; padding:0px; margin:0px">
                                <tr>
                                    <td style="border-right:0px" colspan="2" align="center"><input class="btn" type="button" onclick="submitDesigner()" value=" 保存 "/></td>
                                </tr>
                                <tr>
                                    <td width="25%" style="border-left:0px">名称</td>
                                    <td width="75%" style="border-right:0px">
                                        <%=title%>
                                        <input id="title" name="title" value="<%=title%>" type="hidden"/>
                                        <input name="op" value="<%=op%>" type="hidden"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td title="自动存档时保存于文件柜的目录，需在节点属性上配置为“自动存档”">存档目录</td>
                                    <td title="自动存档时保存于文件柜的目录，需在节点属性上配置为“自动存档”" style="border-right:0px">
                                        <select id="dirCode" name="dirCode" style="width:100%" onchange="if(this.options[this.selectedIndex].value=='not'){alert(this.options[this.selectedIndex].text+' 不能被选择！'); return false;}">
                                            <option value="" selected="selected">无</option>
                                            <%
                                                com.redmoon.oa.fileark.Directory dir = new com.redmoon.oa.fileark.Directory();
                                                com.redmoon.oa.fileark.Leaf lf = dir.getLeaf("root");
                                                com.redmoon.oa.fileark.DirectoryView dv = new com.redmoon.oa.fileark.DirectoryView(request, lf);
                                                dv.ShowDirectoryAsOptions(out, lf, lf.getLayer());
                                            %>
                                        </select></td>
                                </tr>
                                <tr>
                                    <td title="自动存档时保存状态">保存状态</td>
                                    <td title="自动存档时保存状态" style="border-right:0px"><select id="examine" name="examine">
                                        <option value="<%=com.redmoon.oa.fileark.Document.EXAMINE_NOT%>">未审核</option>
                                        <option value="<%=com.redmoon.oa.fileark.Document.EXAMINE_PASS%>">已通过</option>
                                    </select>
                                        <input id="id" name="id" type="hidden" value="<%=id%>"/>
                                        <input type="hidden" id="typeCode" name="typeCode" value="<%=flowTypeCode%>"/>
                                        <input type="hidden" id="returnBack" name="returnBack" value="true" <%=returnBackChecked%> />
                                        <script>
                                            form1.dirCode.value = "<%=dirCode%>";
                                            form1.examine.value = "<%=examine%>";
                                        </script>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="border-left:0px">能否变更</td>
                                    <td style="border-right:0px">
                                        <input type="checkbox" title="能否在流程已转交下一步后再次变更" id="isReactive" name="isReactive" value="1" <%=wpd != null && wpd.isReactive() ? "checked" : ""%> /></td>
                                </tr>
                                <tr>
                                    <td>能否撤回</td>
                                    <td style="border-right:0px"><input type="checkbox" title="流程在转交后能否撤回" id="isRecall" name="isRecall" value="1" <%=wpd != null && wpd.isRecall() ? "checked" : ""%> /></td>
                                </tr>
                                <tr>
                                    <td>
                                        <%
                                            String disBtnName = "流程分发";
                                            String disBtnDesc = "在每个节点上都可以将流程表单分发给相关人员";
                                            String kind = com.redmoon.oa.kernel.License.getInstance().getKind();
                                            if (kind.equalsIgnoreCase(com.redmoon.oa.kernel.License.KIND_COM)) {
                                                disBtnName = "流程知会";
                                                disBtnDesc = "在每个节点上都可以将流程表单知会给相关人员";
                                            }
                                        %>
                                        <%=disBtnName %>
                                    </td>
                                    <td style="border-right:0px">
                                        <input type="checkbox" title="<%=disBtnDesc %>" id="isDistribute" name="isDistribute" value="1" <%=wpd != null && wpd.isDistribute() ? "checked" : ""%> />
                                    </td>
                                </tr>
                                <tr>
                                    <td><span style="border-left:0px">能否加签</span></td>
                                    <td style="border-right:0px">
                                        <input type="checkbox" title="流程中能否加签" id="isPlus" name="isPlus" value="1" <%=wpd != null && wpd.isPlus() ? "checked" : ""%> />
                                    </td>
                                </tr>
                                <tr>
                                    <td>能否指派</td>
                                    <td style="border-right:0px">
                                        <input type="checkbox" title="流程中能否指派" id="isTransfer" name="isTransfer" value="1" <%=wpd != null && wpd.isTransfer() ? "checked" : ""%> />
                                    </td>
                                </tr>
                                <tr>
                                    <td>能否回复</td>
                                    <td style="border-right:0px"><input type="checkbox" title="流程中能否回复" id="isReply" name="isReply" value="1" <%=wpd != null && wpd.isReply() ? "checked" : ""%> /></td>
                                </tr>
                                <tr>
                                    <td>最大下载</td>
                                    <td style="border-right:0px"><input title="每个人可下载每个附件的最大次数" id="downloadCount" name="downloadCount" value="<%=wpd != null ? wpd.getDownloadCount() : -1%>" style="width:50px"/>&nbsp;(-1表示不限)</td>
                                </tr>
                                <tr>
                                    <td>退回时可删除</td>
                                    <td style="border-right:0px"><input type="checkbox" title="当节点上设置了“删除流程”标志位时，被退回时能否删除" id="canDelOnReturn" name="canDelOnReturn" value="1" <%=wpd != null && wpd.isCanDelOnReturn() ? "checked" : ""%> /></td>
                                </tr>
                                <tr>
                                    <td>退回方式</td>
                                    <td style="border-right:0px">
                                        <select id="returnMode" name="returnMode">
                                            <option value="<%=WorkflowPredefineDb.RETURN_MODE_NORMAL%>">退回后按流程图流转</option>
                                            <option value="<%=WorkflowPredefineDb.RETURN_MODE_TO_RETURNER%>">退回后可直送给返回者</option>
                                        </select>
                                        <script>
                                            o("returnMode").value = "<%=returnMode%>";
                                        </script>
                                    </td>
                                </tr>
                                <tr>
                                    <td>退回人员</td>
                                    <td style="border-right:0px">
                                        <select id="returnStyle" name="returnStyle">
                                            <option value="<%=WorkflowPredefineDb.RETURN_STYLE_NORMAL%>">按流程图退回至设定的人员</option>
                                            <option value="<%=WorkflowPredefineDb.RETURN_STYLE_FREE%>">可退回至任一已处理过的人员</option>
                                        </select>
                                        <script>
                                            o("returnStyle").value = "<%=returnStyle%>";
                                        </script>
                                    </td>
                                </tr>
                                <tr>
                                    <td>角色比较</td>
                                    <td style="border-right:0px">
                                        <select id="roleRankMode" name="roleRankMode" title="特定角色节点不会被跳过">
                                            <option value="<%=WorkflowPredefineDb.ROLE_RANK_MODE_NONE%>">无</option>
                                            <option value="<%=WorkflowPredefineDb.ROLE_RANK_NEXT_LOWER_JUMP%>">跳过比当前角色小的节点</option>
                                        </select>
                                        <script>
                                            o("roleRankMode").value = "<%=roleRankMode%>";
                                        </script>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </form>
                    <br/>
                    &nbsp;注：<br/>
                    &nbsp;修改属性后请点击“保存”按钮<br/>
                    &nbsp;选中节点或连接线可以编辑属性
                </div>
                <div id="tabs-2" class="tabDiv">
                    <iframe id="actionPropIframe" name="actionPropIframe" src="flow_designer_action_prop_myflow.jsp" frameborder="0" style="width:100%;"></iframe>
                    <FORM id="myform" METHOD=POST ACTION="flow_designer_action_prop_myflow.jsp" TARGET="actionPropIframe">
                        <INPUT TYPE="hidden" NAME="flowTypeCode" value="">
                        <INPUT TYPE="hidden" NAME="hidFieldWrite" value="">
                        <INPUT TYPE="hidden" NAME="internalName" value="">
                    </FORM>
                </div>
                <%
                    if (license.isPlatformSrc()) {
                        if (license.isSrc()) {%>
                <div id="tabs-4" class="tabDiv">
                    <iframe id="actionScriptIframe" src="flow_designer_script_view.jsp?flowTypeCode=<%=flowTypeCode%>" frameborder="0" style="width:100%;"></iframe>
                </div>
                <%
                        }
                %>
                <div id="tabs-5" class="tabDiv">
                    <iframe id="writeBackIframe" src="flow_designer_write_back.jsp?flowTypeCode=<%=flowTypeCode%>" frameborder="0" style="width:100%;"></iframe>
                </div>
                <%
                    }
                %>
            </div>
            <script>
                $(function () {
                    //创建tabs
                    $('#tabs').tabs();
                });
            </script>
        </td>
    </tr>
</table>
<div id="result"></div>
<textarea id="props" name="props" style="display:none"><%=wpd != null ? wpd.getProps() : ""%></textarea>
<textarea id="views" name="views" style="display:none"><%=wpd != null ? wpd.getViews() : ""%></textarea>
<textarea style="display:none" name="hiddenCondition" id="hiddenCondition"></textarea>
<textarea style="display:none" name="hiddenMsgProp" id="hiddenMsgProp"><%=wpd != null ? wpd.getMsgProp() : ""%></textarea>
<xmp name="linkProp" id="linkProp" style="display:none"><%=wpd != null ? wpd.getLinkProp() : "" %></xmp>
<textarea id="flowData" style="width: 80%; display: none;"><%=wpd != null ? wpd.getFlowJson() : ""%></textarea>
</body>
<script>
    function setCondition(str) {
        $("#hiddenCondition").val(str);
    }

    function setMsgProp(str) {
        $("#hiddenMsgProp").val(str);
    }

    function getMsgProp() {
        return $("#hiddenMsgProp").val();
    }

    var errFunc = function (response) {
        alert('Error ' + response.status + ' - ' + response.statusText);
        alert(response.responseText);
    }

    function doGetRolesUserNamesDone(response) {
        var items = response.responseXML.getElementsByTagName("item");
        spanUserName.innerHTML = "";
        var str = "";
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            var userName = item.getElementsByTagName("userName")[0].firstChild.data;
            var userRealName = item.getElementsByTagName("userRealName")[0].firstChild.data;

            if (str == "") {
                str += "<a href=\"javascript:setPerson('', '', '" + userName + "','" + userRealName + "')\">" + userRealName + "</a>";
            } else {
                str += "，<a href=\"javascript:setPerson('', '', '" + userName + "','" + userRealName + "')\">" + userRealName + "</a>";
            }

        }
        spanUserName.innerHTML = str;
    }

    function getRoleUserName(roles) {
        var str = "op=getRoleUserName&roles=" + roles;
        var myAjax = new cwAjax.Request(
            "flow_predefine_init_tester.jsp",
            {
                method: "post",
                parameters: str,
                onComplete: doGetRolesUserNamesDone,
                onError: errFunc
            }
        );
    }

    function getFlowString() {
        var myflow = $.myflow;
        // console.log("getFlowString:" + myflow.getWorkflow());
        printCallStack();
        return myflow.getWorkflow();
    }

    function submitDesigner() {
        if (o("title").value == "") {
            alert("请填写名称！");
            return;
        }
        var myflow = $.myflow;
        var flowJson = myflow.getJson();
        var flowString = myflow.getWorkflow();
        // console.log("flowJson=" + flowJson);
        // console.log("myflow.getWorkflow()=" + flowString);
        var op = o("op").value;
        var url = op == "edit" ? "modifyFlowPredefined" : "createFlowPredefined";
        $.ajax({
            type: "post",
            url: url,
            data: {
                op: o("op").value,
                title: o("title").value,
                dirCode: o("dirCode").value,
                examine: o("examine").value,
                id: o("id").value,
                flowString: flowString,
                flowJson: flowJson,
                typeCode: o("typeCode").value,
                returnBack: o("returnBack").value,
                isReactive: o("isReactive").checked ? o("isReactive").value : "0",
                isRecall: o("isRecall").checked ? o("isRecall").value : "0",
                returnMode: o("returnMode").value,
                returnStyle: o("returnStyle").value,
                roleRankMode: o("roleRankMode").value,
                props: o("props").value,
                views: o("views").value,
                isDistribute: o("isDistribute").checked ? o("isDistribute").value : "0",
                toghterCondition: o("hiddenCondition").value,
                msgProp: o("hiddenMsgProp").value,
                isPlus: o("isPlus").checked ? o("isPlus").value : "0",
                isTransfer: o("isTransfer").checked ? o("isTransfer").value : "0",
                isReply: o("isReply").checked ? o("isReply").value : "0",
                downloadCount: o("downloadCount").value,
                canDelOnReturn: o("canDelOnReturn").checked?"1":"0"
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                //ShowLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                // alert(data.msg);
                $.toaster({
                    "priority": "info",
                    "message": data.msg
                });
                if (data.newId != null && data.newId != "-1") {
                    o("id").value = data.newId;
                    o("op").value = "edit";
                }
                // window.location.reload();
                if (window.opener) {
                    window.opener.location.reload();
                }
            },
            complete: function (XMLHttpRequest, status) {
                //HideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    function getProps() {
        var xml = $("#props").val();
        if (xml == "") {
            xml = "<?xml version='1.0' encoding='utf-8'?><actions></actions>";
        }
        return xml;
    }

    function setProps(xml) {
        $("#props").val(xml);
    }

    function getViews() {
        var xml = $("#views").val();
        if (xml == "") {
            xml = "<?xml version='1.0' encoding='utf-8'?><actions></actions>";
        }
        return xml;
    }

    function setViews(xml) {
        $("#views").val(xml);
    }

    window.onload = function () {
        document.getElementById("tabs-1").style.height = ($(document).height() - $('#toolbar').height() - 35) + "px";
        document.getElementById("tabs-2").style.height = ($(document).height() - $('#toolbar').height() - 35) + "px";
        <%if (license.isPlatformSrc()) {%>
        <%if (license.isSrc()) {%>
        document.getElementById("tabs-4").style.height = ($(document).height() - $('#toolbar').height() - 35) + "px";
        <%}%>
        document.getElementById("tabs-5").style.height = ($(document).height() - $('#toolbar').height() - 35) + "px";
        <%}%>
        document.getElementById("actionPropIframe").style.height = ($(document).height() - $('#toolbar').height() - 35) + "px";
        <%if (license.isSrc()) {%>
        document.getElementById("actionScriptIframe").style.height = ($(document).height() - $('#toolbar').height() - 35) + "px";
        <%}%>
        <%if (license.isPlatformSrc()) {%>
        document.getElementById("writeBackIframe").style.height = ($(document).height() - $('#toolbar').height() - 35) + "px";
        <%}%>
    };

    <%
        String cloudUrl = cfg.get("cloudUrl");
    %>

    $(function () {
        // 如果直接按下行赋值，则其中的\comma中的反斜杠\会丢失
        // var flowData = "< %=wpd!=null?wpd.getFlowJson():""% >";
        // 通过元素的值赋予反斜杠不会丢失
        var flowData = o("flowData").value;

        // eval这种方式转json不安全，eval会执行json串中的表达式，反斜杠会丢失，所以要先将\转为\\。
        flowData = flowData.replaceAll("\\\\", "\\\\");
        // 因为flowData书写不规范，键上面没有带引号，所以JSON.parse及$.parseJSON解析时都会报错
        if (flowData == "") {
            flowData = "{}";
        }
        flowData = eval("(" + flowData + ")");
        // console.log(JSON.stringify(flowData));

        $('#myflow').myflow({
            // basePath: "",
            allowStateMultiLine: false,
            editable: true,
            restore: flowData,
            expireUnit: "<%=flowExpireUnit%>",
            licenseKey: "<%=license.getKey()%>",
            cloudUrl: "<%=cloudUrl%>",
            activeRects: {},
            finishRects: {},
            rootPath: "<%=request.getContextPath()%>",
            tools: {
                save: function (data) {
                    // console.log("保存", data);
                    submitDesigner();
                    // window.localStorage.setItem("data", data)
                },
                /*publish: function (data) {
                    console.log("发布", eval("(" + data + ")"));
                },*/
                addPath: function (id, data) {
                    console.log("添加路径", id, eval("(" + data + ")"));
                },
                addRect: function (id, data) {
                    console.log("添加状态", id, eval("(" + data + ")"));
                },
                clickPath: function (id, data) {
                    console.log("点击线", id, eval("(" + data + ")"));
                    // 取得其from节点，判断是否为条件分支
                    var myflow = $.myflow;
                    var path = myflow.getPaths()[id];

                    // console.log(path.from());
                    var flag = path.from().getPropVal("ActionFlag");
                    var isFlagXorRadiate = false;
                    if (flag.length >= 7) {
                        if (flag.substr(6, 1) == "1") {
                            isFlagXorRadiate = true;
                        }
                    }

                    if (isFlagXorRadiate) {
                        OpenLinkPropertyWin(id);
                    }
                    else {
                        OpenLinkPropertyNormalWin(id);
                    }
                },
                clickRect: function (id, data) {
                    // console.log(data);
                    console.log("点击状态", id, eval("(" + data + ")"));
                    // OpenModifyWin(eval("(" + data + ")"));
                    if (curInternalName != id) {
                        OpenModifyWin(id);
                    }
                },
                deletePath: function (id) {
                    console.log("删除线", id);
                },
                deleteRect: function (id, data) {
                    console.log("删除状态", id, eval("(" + data + ")"));
                },
                revoke: function (id) {
                    console.log("撤销", id);
                }
            }
        });
    });
</script>
</html>
