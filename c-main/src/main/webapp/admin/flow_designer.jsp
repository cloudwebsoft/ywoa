<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.kernel.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
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
    <title>流程设计 - <%=wpd.getTitle()%></title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <%@ include file="../inc/nocache.jsp" %>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../js/jquery.form.js"></script>
    <script type="text/javascript" src="../js/activebar2.js"></script>

    <script language=javascript>
        <!--
        function checkOfficeEditInstalled() {
            if (!isIE())
                return true;

            var bCtlLoaded = false;
            try {
                if (typeof (Designer.SetSelectedLinkProperty) == "undefined")
                    bCtlLoaded = false;
                if (typeof (Designer.SetSelectedLinkProperty) == "unknown") {
                    bCtlLoaded = true;
                }
            } catch (ex) {
            }
            return bCtlLoaded;
        }

        $(function () {
            re = checkOfficeEditInstalled();
            if (!re) {
                <%if(isUsed){%> //判断云盘是否启用
                if (isWow64()) {
                    $('<div></div>').html('您还没有安装流程设计控件，请点击确定此处下载安装！').activebar({
                        'icon': '../images/alert.gif',
                        'highlight': '#FBFBB3',
                        'url': '../activex/clouddisk_x64.exe',
                        'button': '../images/bar_close.gif'
                    });
                } else {
                    $('<div></div>').html('您还没有安装流程设计控件，请点击确定此处下载安装！').activebar({
                        'icon': '../images/alert.gif',
                        'highlight': '#FBFBB3',
                        'url': '../activex/clouddisk.exe',
                        'button': '../images/bar_close.gif'
                    });
                }
                <%}else{%>
                $('<div></div>').html('您还没有安装流程设计控件，请点击确定此处下载安装！').activebar({
                    'icon': '../images/alert.gif',
                    'highlight': '#FBFBB3',
                    'url': '../activex/oa_client.exe',
                    'button': '../images/bar_close.gif'
                });
                <%}%>
            }
        });

        var modifyWin;

        function openWin(url, width, height) {
            modifyWin = window.open(url, "designer", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width=" + width + ",height=" + height);
            modifyWin.focus();
        }

        function ModifyActionNotSubmit(user, title, clrindex, userRealName, jobCode, jobName, proxyJobCode, proxyJobName, proxyUserName, proxyUserRealName, fieldWrite, checkState, dept, flag, nodeMode, strategy, isEndNode, item2, isMsg) {
            /*
            // 不需要转换了，linux服务器上的编码是zh_CN.UTF-8就正常
            $.ajax({
                url: "flow_designer_do.jsp",
                async: false,
                data: {
                    op: "convert",
                    user: user,
                    title: title,
                    userRealName: userRealName,
                    jobCode: jobCode,
                    jobName: jobName,
                    proxyJobCode: proxyJobCode,
                    proxyJobName: proxyJobName,
                    proxyUserName: proxyUserName,
                    proxyUserRealName: proxyUserRealName
                },
                contentType: "application/x-www-form-urlencoded; charset=utf-8",
                dataType: "html",
                beforeSend: function(XMLHttpRequest){
                },
                success: function(data, status) {
                    data = $.parseJSON(data.trim());
                    if (data.ret == 1) {
                        Designer.ActionUser = data.user;
                        Designer.ActionTitle = data.title;
                        Designer.ActionUserRealName = data.userRealName;
                        Designer.ActionJobCode = data.jobCode;
                        Designer.ActionJobName = data.jobName;
                        Designer.ActionProxyJobCode = data.proxyJobCode;
                        Designer.ActionProxyJobName = data.proxyJobName;
                        Designer.ActionProxyUserName = data.proxyUserName;
                        Designer.ActionProxyUserRealName = data.proxyUserRealName;
                    } else {
                        Designer.ActionUser = user;
                        Designer.ActionTitle = title;
                        Designer.ActionUserRealName = userRealName;
                        Designer.ActionJobCode = jobCode;
                        Designer.ActionJobName = jobName;
                        Designer.ActionProxyJobCode = proxyJobCode;
                        Designer.ActionProxyJobName = proxyJobName;
                        Designer.ActionProxyUserName = proxyUserName;
                        Designer.ActionProxyUserRealName = proxyUserRealName;
                    }
                },
                complete: function(XMLHttpRequest, status){
                },
                error: function(XMLHttpRequest, textStatus){
                }
            });
            */

            Designer.ActionUser = user;
            Designer.ActionTitle = title;
            Designer.ActionUserRealName = userRealName;
            Designer.ActionJobCode = jobCode;
            Designer.ActionJobName = jobName;
            Designer.ActionProxyJobCode = proxyJobCode;
            Designer.ActionProxyJobName = proxyJobName;
            Designer.ActionProxyUserName = proxyUserName;
            Designer.ActionProxyUserRealName = proxyUserRealName;

            Designer.ActionColorIndex = clrindex;
            Designer.ActionFieldWrite = fieldWrite;
            Designer.ActionCheckState = checkState;
            Designer.ActionDept = dept;
            Designer.ActionFlag = flag;
            Designer.ActionDeptMode = Number(nodeMode);
            Designer.ActionStrategy = strategy;
            Designer.ActionItem1 = isEndNode;
            Designer.ActionItem2 = item2;
            Designer.ActionIsMsg = isMsg;
            Designer.ModifyAction();
        }

        function ModifyAction(user, title, clrindex, userRealName, jobCode, jobName, proxyJobCode, proxyJobName, proxyUserName, proxyUserRealName, fieldWrite, checkState, dept, flag, nodeMode, strategy, isEndNode, item2, isMsg) {
            ModifyActionNotSubmit(user, title, clrindex, userRealName, jobCode, jobName, proxyJobCode, proxyJobName, proxyUserName, proxyUserRealName, fieldWrite, checkState, dept, flag, nodeMode, strategy, isEndNode, item2, isMsg);
            submitDesigner();
        }

        function getActionUser() {
            return Designer.ActionUser;
        }

        function getActionTitle() {
            return Designer.ActionTitle;
        }

        function getActionColorIndex() {
            return Designer.ActionColorIndex;
        }

        function getActionUserRealName() {
            return Designer.ActionUserRealName;
        }

        function getActionCheckState() {
            return Designer.ActionCheckState;
        }

        function getActionJobCode() {
            return Designer.ActionJobCode;
        }

        function getActionJobName() {
            return Designer.ActionJobName;
        }

        function getActionProxyJobCode() {
            return Designer.ActionProxyJobCode;
        }

        function getActionProxyJobName() {
            return Designer.ActionProxyJobName;
        }

        function getActionProxyUserName() {
            return Designer.ActionProxyUserName;
        }

        function getActionProxyUserRealName() {
            return Designer.ActionProxyUserRealName;
        }

        function getActionFieldWrite() {
            return Designer.ActionFieldWrite;
        }

        function getActionDept() {
            return Designer.ActionDept;
        }

        function getActionFlag() {
            return Designer.ActionFlag;
        }

        function getActionNodeMode() {
            return Designer.ActionDeptMode;
        }

        function getActionType() {
            return Designer.ActionType;
        }

        function getActionStrategy() {
            return Designer.ActionStrategy;
        }

        function getActionItem1() {
            return Designer.ActionItem1;
        }

        function getActionItem2() {
            return Designer.ActionItem2;
        }

        function getActionIsMsg() {
            return Designer.ActionIsMsg;
        }

        function GetActionProperty(actionName, prop) {
            return Designer.GetActionProperty(actionName, prop);
        }

        function OpenModifyWin() {
            var isActionSelected = Designer.isActionSelected;
            if (isActionSelected) {
                var internalName = Designer.GetActionProperty("curSelected", "name");

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
            } else
                alert("请选择一个动作！");
        }

        function Operate() {
            OpenModifyWin();
        }

        function SetSelectedLinkProperty(propItem, propValue) {
            Designer.SetSelectedLinkProperty(propItem, propValue);
        }

        function GetSelectedLinkProperty(propItem) {
            return Designer.GetSelectedLinkProperty(propItem);
        }

        function OpenLinkPropertyWin() {
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

        function OpenLinkPropertyNormalWin() {
            var t = GetSelectedLinkProperty("title");
            t = encodeURI(t);

            $('#tabs').tabs('option', 'active', 1);
            o('actionPropIframe').src = "flow_designer_link_normal_prop.jsp?title=" + t;
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
<table id="designerTable" border="0" align="left" cellpadding="0" cellspacing="0" style="margin:0px; padding:0px; width:100%;">
    <tr>
        <td align="left" width="70%">
            <style>
                #toolbar {
                    position: relative;
                }

                #toolbar ul {
                    float: left;
                }

                #toolbar .imgOut {
                    margin: 3px;
                    height: 27px;
                    width: 27px;
                    float: left;
                    list-style: none;
                }

                #toolbar .imgOver {
                    margin: 3px;
                    height: 27px;
                    width: 27px;
                    float: left;
                    list-style: none;
                    cursor: pointer;
                    background-image: url(../images/designer/background.jpg);
                }

                #toolbar img {
                    border: 0px;
                    margin: 0px;
                    padding-left: 3px;
                    padding-top: 3px;
                    vertical-align: middle;
                }
            </style>
            <div id="toolbar">
                <ul>
                    <div id="infoSpan" style="display:none;color:red;margin-left:8px"></div>
                    <li class="imgOut" onmouseover="this.className='imgOver'" onmouseout="this.className='imgOut'"><img title="新建流程" src="../images/designer/new.png" onclick="Designer.New()"/></li>
                    <li class="imgOut" onmouseover="this.className='imgOver'" onmouseout="this.className='imgOut'"><img title="串签" src="../images/designer/serial.png" onclick="Designer.AddActionSerial()"/></li>
                    <li class="imgOut" onmouseover="this.className='imgOver'" onmouseout="this.className='imgOut'"><img title="会签" src="../images/designer/join.png" onclick="Designer.AddActionJoin()"/></li>
                    <li class="imgOut" onmouseover="this.className='imgOver'" onmouseout="this.className='imgOut'"><img title="分支" src="../images/designer/branch.png" onclick="Designer.AddActionBranch()"/></li>
                    <li class="imgOut" onmouseover="this.className='imgOver'" onmouseout="this.className='imgOut'"><img title="创建节点" src="../images/designer/node.png" onclick="Designer.AddAction()"/></li>
                    <li class="imgOut" onmouseover="this.className='imgOver'" onmouseout="this.className='imgOut'"><img title="删除节点" src="../images/designer/del.png" onclick="Designer.DeleteAllSelected()"/></li>
                    <li class="imgOut" onmouseover="this.className='imgOver'" onmouseout="this.className='imgOut'"><img title="连接节点" src="../images/designer/link.png" onclick="Designer.LinkAction()"/></li>
                    <li class="imgOut" onmouseover="this.className='imgOver'" onmouseout="this.className='imgOut'"><img title="放大" src="../images/designer/zoomin.png" onclick="Designer.ZoomIn()"/></li>
                    <li class="imgOut" onmouseover="this.className='imgOver'" onmouseout="this.className='imgOut'"><img title="缩小" src="../images/designer/zoomout.png" onclick="Designer.ZoomOut()"/></li>
                    <li class="imgOut" onmouseover="this.className='imgOver'" onmouseout="this.className='imgOut'"><img title="栅格线" src="../images/designer/snap.png" onclick="Designer.Snap()"/></li>
                </ul>
            </div>
            <div style="clear:both">
                <%
                    boolean isOem = License.getInstance().isOem();
                    String codeBase = "";
                    if (!isOem) {
                        codeBase = "codebase=\"../activex/cloudym.CAB#version=1,3,0,0\"";
                    }
                %>
                <object id="Designer" classid="CLSID:ADF8C3A0-8709-4EC6-A783-DD7BDFC299D7" <%=codeBase%>>
                    <param name="Workflow" value="<%=flowString%>"/>
                    <param name="Mode" value="<%=mode%>"/>
                    <!--debug user initiate complete-->
                    <param name="CurrentUser" value="<%=privilege.getUser(request)%>"/>
                    <param name="CurrentUserRealName" value="<%=ud.getRealName()%>"/>
                    <param name="CurrentJobCode" value=""/>
                    <param name="CurrentJobName" value=""/>
                    <param name="ExpireUnit" value="<%=flowExpireUnit%>"/>
                    <param name="Organization" value="<%=license.getCompany()%>"/>
                    <param name="Key" value="<%=license.getKey()%>"/>
                    <param name="Company" value="<%=license.getName()%>"/>
                    <param name="LicenseType" value="<%=license.getType()%>"/>
                </object>
            </div>
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
                    <table width="100%" border="0" cellpadding="0" cellspacing="0" class="tabStyle_1" style="margin:0px; padding:0px; width:100%;display:none">
                        <tr>
                            <td style="border-top:0px; border-left:0px; border-right:0px" colspan="2"><strong>节点处理人员预匹配</strong></td>
                        </tr>
                        <tr>
                            <td width="25%" style="border-left:0px">所选节点</td>
                            <td width="75%" style="border-right:0px">
                                <span id="spanUserName">
                                    &lt;请选择节点&gt;
                                </span>
                            </td>
                        </tr>
                        <tr>
                            <td title="变换当前所选节点用户" style="border-left:0px">变换用户</td>
                            <td style="border-right:0px"><input name="curUserRealName" value="" readonly="readonly"/>
                                <input name="curUserName" value="" type="hidden"/>
                                <a href="javascript:;" onclick="if (!Designer.isActionSelected) {alert('请选择一个节点！'); return;} showModalDialog('../user_sel.jsp',window.self,'dialogWidth:640px;dialogHeight:480px;status:no;help:no;')">选择</a></td>
                        </tr>
                        <tr>
                            <td style="border-left:0px">下一节点</td>
                            <td style="border-right:0px">
                                <div id="divNextActions"></div>
                            </td>
                        </tr>
                    </table>
                    <br/>
                    &nbsp;注：<br/>
                    &nbsp;修改属性后请点击“保存”按钮<br/>
                    &nbsp;选中节点或连接线可以编辑属性
                </div>
                <div id="tabs-2" class="tabDiv">
                    <iframe id="actionPropIframe" name="actionPropIframe" src="flow_designer_action_prop.jsp" frameborder="0" style="width:100%;"></iframe>
                    <FORM id="myform" METHOD=POST ACTION="flow_designer_action_prop.jsp" TARGET="actionPropIframe">
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

    function showProp() {
        Designer.ShowProp();
    }

    function testAction() {
        if (Designer.ActionDeptMode == "<%=WorkflowActionDb.NODE_MODE_ROLE%>") {
            // spanMode.innerHTML = "角色";
            getRoleUserName(Designer.ActionJobCode);
        } else {
            // spanMode.innerHTML = "用户";
            if (Designer.ActionJobCode == "$foreAction" || Designer.ActionJobCode == "$self" || Designer.ActionJobCode == "$starter" || Designer.ActionJobCode == "$userSelect") {
                spanUserName.innerHTML = Designer.ActionJobName;
            } else {
                var ary = Designer.ActionJobCode.split(",");
                var ary2 = Designer.ActionJobName.split(",");
                var str = "";
                for (var i = 0; i < ary.length; i++) {
                    if (str == "") {
                        str += "<a href=\"javascript:setPerson('', '', '" + ary[i] + "','" + ary2[i] + "')\">" + ary2[i] + "</a>";
                    } else {
                        str += "，<a href=\"javascript:setPerson('', '', '" + ary[i] + "','" + ary2[i] + "')\">" + ary2[i] + "</a>";
                    }
                }
                if (ary.length == 1) {
                    setPerson("", "", ary[0], ary2[0]);
                }
                spanUserName.innerHTML = str;
            }
        }
    }

    function setPerson(deptCode, deptName, user, userRealName) {
        curUserName.value = user;
        curUserRealName.value = userRealName;

        divNextActions.innerHTML = "";

        var toActionInternalNames = Designer.GetActionProperty("curSelected", "toActionInternalNames");
        var internalName1 = Designer.GetActionProperty("curSelected", "name");
        // 取得下一节点的用户
        var ary = toActionInternalNames.split(",");
        for (var i = 0; i < ary.length; i++) {
            // 如果尚未设置节点上的处理角色或用户
            if (Designer.GetActionProperty(ary[i], "jobCode") == "")
                continue;
            var str = "op=matchActionUsers";
            str += "&internalName2=" + ary[i];
            str += "&jobCode2=" + Designer.GetActionProperty(ary[i], "jobCode");
            str += "&jobName2=" + Designer.GetActionProperty(ary[i], "jobName");
            str += "&proxyJobCode2=" + Designer.GetActionProperty(ary[i], "proxyJobCode");
            str += "&proxyJobName2=" + Designer.GetActionProperty(ary[i], "proxyJobName");
            str += "&dept2=" + Designer.GetActionProperty(ary[i], "dept");
            str += "&deptMode2=" + Designer.GetActionProperty(ary[i], "deptMode");
            str += "&proxyUserRealName2=" + Designer.GetActionProperty(ary[i], "proxyUserRealName");

            str += "&internalName1=" + internalName1;
            str += "&jobCode1=" + Designer.GetActionProperty(internalName1, "jobCode");
            str += "&proxyJobCode1=" + Designer.GetActionProperty(internalName1, "proxyJobCode");
            str += "&proxyJobName1=" + Designer.GetActionProperty(internalName1, "proxyJobName");
            str += "&dept1=" + Designer.GetActionProperty(internalName1, "dept");
            str += "&deptMode1=" + Designer.GetActionProperty(internalName1, "deptMode");
            str += "&proxyUserRealName1=" + Designer.GetActionProperty(ary[i], "proxyUserRealName");
            str += "&curUserName=" + curUserName.value;
            str += "&curUserRealName=" + curUserRealName.value;

            var myAjax = new cwAjax.Request(
                "flow_predefine_init_tester.jsp",
                {
                    method: "post",
                    parameters: str,
                    onComplete: doMatchActionUsersDone,
                    onError: errFunc
                }
            );
        }
    }

    function doMatchActionUsersDone(response) {
        var ret = response.responseText.trim();
        if (ret.substring(0, 1) == "-") {
            alert(ret.substring(1));
            return;
        }
        var jobNames = response.responseXML.getElementsByTagName("jobName");
        var jobName = jobNames[0].firstChild.data;
        divNextActions.innerHTML += jobName + "：";
        var items = response.responseXML.getElementsByTagName("item");
        var str = "";
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            var userName = item.getElementsByTagName("userName")[0].firstChild.data;
            var userRealName = item.getElementsByTagName("userRealName")[0].firstChild.data;
            if (str == "")
                str += userRealName;
            else
                str += "，" + userRealName;
        }
        divNextActions.innerHTML += str + "<BR>";
    }

    function getFlowString() {
        return Designer.Workflow;
    }

    function submitDesigner() {
        if (o("title").value == "") {
            alert("请填写名称！");
            return;
        }
        var op = o("op").value;
        var url = op == "edit" ? "modifyFlowPredefined" : "createFlowPredefined";
        $.ajax({
            type: "post",
            url: url,
            data: {
                title: o("title").value,
                dirCode: o("dirCode").value,
                examine: o("examine").value,
                id: o("id").value,
                flowString: Designer.Workflow,
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
                alert(data.msg);
                if (data.newId != null && data.newId != "-1") {
                    o("id").value = data.newId;
                    o("op").value = "edit";
                }
                window.location.reload();
                if (window.opener)
                    window.opener.location.reload();
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
        if (!isIE()) {
            $('#infoSpan').html("设计器只能在IE内核浏览器使用!");
            $('#infoSpan').show();
        }
        Designer.height = $(document).height() - $('#toolbar').height() - 35;
        Designer.width = $('#designerTable').width() - 340;
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


    /**
     * window.onresize 事件 专用事件绑定器 v0.1 Alucelx
     * http://www.cnblogs.com/Alucelx/archive/2011/10/20/2219263.html
     * <description>
     * 用于解决 lte ie8 & chrome 及其他可能会出现的 原生 window.resize 事件多次执行的 BUG.
     * </description>
     * <methods>
     * add: 添加事件句柄
     * remove: 删除事件句柄
     * </methods>
     */
    var onWindowResize = function () {
        //事件队列
        var queue = [],
            indexOf = Array.prototype.indexOf || function () {
                var i = 0, length = this.length;
                for (; i < length; i++) {
                    if (this[i] === arguments[0]) {
                        return i;
                    }
                }
                return -1;
            };
        var isResizing = {}, //标记可视区域尺寸状态， 用于消除 lte ie8 / chrome 中 window.onresize 事件多次执行的 bug
            lazy = true, //懒执行标记
            listener = function (e) { //事件监听器
                var h = window.innerHeight || (document.documentElement && document.documentElement.clientHeight) || document.body.clientHeight,
                    w = window.innerWidth || (document.documentElement && document.documentElement.clientWidth) || document.body.clientWidth;
                if (h === isResizing.h && w === isResizing.w) {
                    return;
                } else {
                    e = e || window.event;
                    var i = 0, len = queue.length;
                    for (; i < len; i++) {
                        queue[i].call(this, e);
                    }
                    isResizing.h = h,
                        isResizing.w = w;
                }
            }
        return {
            add: function (fn) {
                if (typeof fn === 'function') {
                    if (lazy) { //懒执行
                        if (window.addEventListener) {
                            window.addEventListener('resize', listener, false);
                        } else {
                            window.attachEvent('onresize', listener);
                        }
                        lazy = false;
                    }
                    queue.push(fn);
                } else {
                }
                return this;
            },
            remove: function (fn) {
                if (typeof fn === 'undefined') {
                    queue = [];
                } else if (typeof fn === 'function') {
                    var i = indexOf.call(queue, fn);
                    if (i > -1) {
                        queue.splice(i, 1);
                    }
                }
                return this;
            }
        };
    }.call(this);

    var _fn = function () {
        Designer.height = $(document).height() - $('#toolbar').height() - 35;
        Designer.width = $('#designerTable').width() - 340;
        document.getElementById("tabs-1").style.height = ($(document).height() - $('#toolbar').height() - 35) + "px";
        document.getElementById("tabs-2").style.height = ($(document).height() - $('#toolbar').height() - 35) + "px";
        <%if (license.isPlatformSrc()) {%>
        document.getElementById("tabs-4").style.height = ($(document).height() - $('#toolbar').height() - 35) + "px";
        document.getElementById("tabs-5").style.height = ($(document).height() - $('#toolbar').height() - 35) + "px";
        <%}%>
        document.getElementById("actionPropIframe").style.height = ($(document).height() - $('#toolbar').height() - 35) + "px";
        <%if (license.isPlatformSrc()) {%>
        document.getElementById("actionScriptIframe").style.height = ($(document).height() - $('#toolbar').height() - 35) + "px";
        document.getElementById("writeBackIframe").style.height = ($(document).height() - $('#toolbar').height() - 35) + "px";
        <%}%>
    };
    onWindowResize.add(_fn);
</script>
<%
    if (!license.isBiz() && !license.isOem()) {
%>
<script src="../js/flow_logo.js" type="text/javascript"></script>
<%}%>
</html>
