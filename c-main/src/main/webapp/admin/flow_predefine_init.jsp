<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.kernel.License" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="com.redmoon.oa.Config" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
    String op = ParamUtil.get(request, "op");

    WorkflowPredefineDb wpd = new WorkflowPredefineDb();
    wpd = wpd.getDefaultPredefineFlow(flowTypeCode);

    License license = License.getInstance();
    if (wpd==null) {
        if (license.getFlowDesigner().equals(License.FLOW_DESIGNER_X)) {
            response.sendRedirect("flow_predefine_init_myflow.jsp?flowTypeCode=" + flowTypeCode);
            return;
        }
    }

    if (wpd != null) {
        String flowDesignerType = Config.getInstance().get("flowDesignerType");
        // 如果指定用X版，且许可证允许用X版
        if (License.FLOW_DESIGNER_X.equals(flowDesignerType)) {
            if (License.FLOW_DESIGNER_X.equals(license.getFlowDesigner())) {
                // if (!StringUtils.isEmpty(wpd.getFlowJson())) {
                    response.sendRedirect("flow_predefine_init_myflow.jsp?flowTypeCode=" + flowTypeCode);
                    return;
                // }
            }
        }
        op = "edit";
    }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>预定义流程</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <%@ include file="../inc/nocache.jsp" %>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script type="text/javascript" src="../js/activebar2.js"></script>
    <script language="JavaScript" type="text/JavaScript">
        <!--
        function MM_preloadImages() { //v3.0
            var d = document;
            if (d.images) {
                if (!d.MM_p) d.MM_p = new Array();
                var i, j = d.MM_p.length, a = MM_preloadImages.arguments;
                for (i = 0; i < a.length; i++)
                    if (a[i].indexOf("#") != 0) {
                        d.MM_p[j] = new Image;
                        d.MM_p[j++].src = a[i];
                    }
            }
        }

        //-->
    </script>
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

        function hideDesigner() {
            $("#designerDiv").hide();
        }

        //-->
    </script>
</head>
<body onunload="hideDesigner()">
<%
    LeafPriv lp = new LeafPriv(flowTypeCode);
/*
if (!(lp.canUserSee(privilege.getUser(request)))) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
*/
    if (!(lp.canUserExamine(privilege.getUser(request)))) {
        if (lp.canUserQuery(privilege.getUser(request))) {
            response.sendRedirect("flow_list.jsp?typeCode=" + StrUtil.UrlEncode(flowTypeCode));
            return;
        } else {
            out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }
    }

    UserDb ud = new UserDb();
    ud = ud.getUserDb(privilege.getUser(request));

    String mode = "view"; // "define";
    String flowString = "";
    String title = "";
    int id = 0;
    String returnBackChecked = "checked";
    String dirCode = "";
    int examine = 0;
    if (op.equals("edit")) {
        // mode = "user";
        flowString = wpd.getFlowString();
        Leaf lf = new Leaf();
        lf = lf.getLeaf(flowTypeCode);
        if (lf != null)
            title = lf.getName();
        // title = wpd.getTitle();
        id = wpd.getId();
        dirCode = wpd.getDirCode();
        if (!wpd.isReturnBack())
            returnBackChecked = "";
        examine = wpd.getExamine();
    } else {
        Leaf lf = new Leaf();
        lf = lf.getLeaf(flowTypeCode);
        if (lf != null)
            title = lf.getName();
    }

    String action = ParamUtil.get(request, "action");
    if (action.equals("apply")) {
        String templateCode = ParamUtil.get(request, "templateCode");
        // System.out.println(getClass() + " " + templateCode);
        boolean re = false;
        // 如果还没有预定义流程
        if (wpd == null) {
            wpd = new WorkflowPredefineDb();
            WorkflowPredefineDb twpd = wpd.getDefaultPredefineFlow(templateCode);
            if (twpd == null || !twpd.isLoaded()) {
                out.print(StrUtil.jAlert_Back("流程图不存在！", "提示"));
                return;
            }
            wpd.setTypeCode(flowTypeCode);
            wpd.setFlowString(twpd.getFlowString());
            wpd.setTitle(title);
            wpd.setReturnBack(twpd.isReturnBack());
            wpd.setReactive(twpd.isReactive());
            wpd.setRecall(twpd.isRecall());
            wpd.setReturnMode(twpd.getReturnMode());
            wpd.setReturnStyle(twpd.getReturnStyle());
            wpd.setRoleRankMode(twpd.getRoleRankMode());
            wpd.setProps(twpd.getProps());
            wpd.setViews(twpd.getViews());
            wpd.setScripts(twpd.getScripts());
            wpd.setLinkProp(twpd.getLinkProp());

            re = wpd.create();
        } else {
            WorkflowPredefineDb twpd = wpd.getDefaultPredefineFlow(templateCode);
            if (twpd == null || !twpd.isLoaded()) {
                out.print(StrUtil.jAlert_Back("流程图不存在！", "提示"));
                return;
            }
            wpd.setFlowString(twpd.getFlowString());

            wpd.setReturnBack(twpd.isReturnBack());
            wpd.setReactive(twpd.isReactive());
            wpd.setRecall(twpd.isRecall());
            wpd.setReturnMode(twpd.getReturnMode());
            wpd.setReturnStyle(twpd.getReturnStyle());
            wpd.setRoleRankMode(twpd.getRoleRankMode());
            wpd.setProps(twpd.getProps());
            wpd.setViews(twpd.getViews());
            wpd.setScripts(twpd.getScripts());
            wpd.setLinkProp(twpd.getLinkProp());

            re = wpd.save();
        }
        if (re)
            out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "flow_predefine_init.jsp?flowTypeCode=" + StrUtil.UrlEncode(flowTypeCode)));
        else
            out.print(StrUtil.jAlert_Back("操作失败！", "提示"));
        return;
    }

    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    String flowExpireUnit = cfg.get("flowExpireUnit");
    if (flowExpireUnit.equals("day")) {
        flowExpireUnit = "天";
    } else {
        flowExpireUnit = "小时";
    }

    com.redmoon.clouddisk.Config cfgNd = com.redmoon.clouddisk.Config
            .getInstance();
    boolean isUsed = cfgNd.getBooleanProperty("isUsed"); //判断网盘是否启用
%>
<%@ include file="flow_inc_menu_top.jsp" %>
<script>
    o("menu2").className = "current";
</script>
<div class="spacerH"></div>
<table width="100%" align="center" class="percent98">
    <tr>
        <td>
            <span id="infoSpan" style="color:red"></span>
        </td>
        <td align="left" width="75%">
            <select id="flowTemplate" name="flowTemplate">
                <%
                    Leaf rootlf = new Leaf();
                    rootlf = rootlf.getLeaf(Leaf.CODE_ROOT);
                    DirectoryView flowdv = new DirectoryView(rootlf);
                    flowdv.ShowDirectoryAsOptions(request, out, rootlf, rootlf.getLayer());
                %>
            </select>
            <input type="button" class="btn" value="套用流程图" onclick="applyFlow()"/>
            <%
                if (lp.canUserExamine(privilege.getUser(request))) {
            %>
            &nbsp;&nbsp;<input type="button" class="btn" onclick="openWin('flow_designer.jsp?flowTypeCode=<%=flowTypeCode%>', 1024, 521)" value="编辑流程图"/>
            <%}%>
        </td>
    </tr>
</table>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent98" style="margin:0px">
    <tr>
        <td align="center" class="tabStyle_1_title">
            <a href="javascript:closeLeftMenu()" style="float:left"><span id="btnName" style="display:none">关闭菜单</span></a>
            <%=title%> 流程图
        </td>
    </tr>
    <tr>
        <td align="center" style="background-color: #fff">
            <div id="designerDiv">
                <%
                    boolean isOem = License.getInstance().isOem();
                    String codeBase = "";
                    if (!isOem) {
                        codeBase = "codebase=\"../activex/cloudym.CAB#version=1,3,0,0\"";
                    }
                %>
                <object id="Designer" classid="CLSID:ADF8C3A0-8709-4EC6-A783-DD7BDFC299D7" <%=codeBase%> width="100%" height="100%">
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
    </tr>
    <tr style="display:none">
        <td align="center">
            <form action="modifyFlowPredefine.do" method="post" id="form1" name="form1" onsubmit="form1_onsubmit()">
                <div style="text-align:left">
                    &nbsp;名称&nbsp;<input name="id" type="hidden" value="<%=id%>"/>
                    <input name="title" value="<%=title%>"/>
                    <input name="op" value="<%=op%>"/>
                    <input type="hidden" name="flowString" value=""/>
                    <input type="hidden" name="typeCode" value="<%=flowTypeCode%>"/>
                    <input type="hidden" name="returnBack" value="true" <%=returnBackChecked%> />
                    <!--允许打回-->
                    自动存至文件柜
                    <select name="dirCode" onchange="if(this.options[this.selectedIndex].value=='not'){jAlert(this.options[this.selectedIndex].text+' 不能被选择！','提示'); return false;}">
                        <option value="" selected="selected">无</option>
                        <%
                            com.redmoon.oa.fileark.Directory dir = new com.redmoon.oa.fileark.Directory();
                            com.redmoon.oa.fileark.Leaf lf = dir.getLeaf("root");
                            com.redmoon.oa.fileark.DirectoryView dv = new com.redmoon.oa.fileark.DirectoryView(request, lf);
                            dv.ShowDirectoryAsOptions(out, lf, lf.getLayer());
                        %>
                    </select>
                    &nbsp;保存状态
                    <select name="examine">
                        <option value="<%=com.redmoon.oa.fileark.Document.EXAMINE_NOT%>">未审核</option>
                        <option value="<%=com.redmoon.oa.fileark.Document.EXAMINE_PASS%>">已通过</option>
                    </select>
                    <input type="checkbox" title="能否在流程已转交下一步后重激活节点" id="isReactive" name="isReactive" value="1" <%=wpd != null && wpd.isReactive() ? "checked" : ""%> />
                    重激活&nbsp;&nbsp;
                    <input type="checkbox" title="流程在转交后能否撤回" id="isRecall" name="isRecall" value="1" <%=wpd != null && wpd.isRecall() ? "checked" : ""%> />
                    能否撤回
                    <script>
                        form1.dirCode.value = "<%=dirCode%>";
                        form1.examine.value = "<%=examine%>";
                    </script>
                </div>
                <input class="btn" name="submit" type="submit" value=" 确 定 "/>
            </form>
            <div id="testDiv" style="margin-top:3px;padding-top:5px;text-align:left;border-top:1px dashed #cccccc">
                <strong>节点人员匹配测试</strong><br/>
                当前所选节点符合条件的用户：
                <span id="spanUserName">
                  &lt;请选择节点&gt;
                </span>
                <br/>
                变换当前节点用户：
                <input name="curUserRealName" value="" readonly="readonly"/>
                <input name="curUserName" value="" type="hidden"/>
                &nbsp;&nbsp;<a href="javascript:;" onclick="if (!Designer.isActionSelected) {jAlert('请选择一个节点！','提示'); return;} showModalDialog('../user_sel.jsp',window.self,'dialogWidth:640px;dialogHeight:480px;status:no;help:no;')">选择用户</a><br/>
                <div id="divNextActions"></div>
            </div>
        </td>
    </tr>
</table>
</body>
<script>
    var errFunc = function (response) {
        jAlert('Error ' + response.status + ' - ' + response.statusText, '提示');
        jAlert(response.responseText, "提示");
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
            jAlert(ret.substring(1), "提示");
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

    function applyFlow() {
        $(".tabStyle_1_title").parent().parent().parent().hide();
        if (o("flowTemplate").value == "not") {
            jAlert("请选择流程！", "提示");
            return;
        }

        jConfirm("您确定要套用流程图么？", "提示", function (r) {
            if (!r) {
                return;
            } else {
                window.location.href = "flow_predefine_init.jsp?action=apply&flowTypeCode=<%=StrUtil.UrlEncode(flowTypeCode)%>&templateCode=" + o("flowTemplate").value;
            }
        })
    }

    var modifyWin;

    function openWin(url, width, height) {
        modifyWin = window.open(url, "pre_action_modify", "toolbar=yes,location=yes,directories=yes,status=yes,menubar=yes,scrollbars=yes,resizable=yes,fullScreen=yes");
        modifyWin.focus();
    }

    function ModifyAction(user, title, clrindex, userRealName, jobCode, jobName, proxyJobCode, proxyJobName, proxyUserName, proxyUserRealName, fieldWrite, checkState, dept, flag, nodeMode, strategy, isEndNode) {
        Designer.ActionUser = user;
        Designer.ActionTitle = title;
        Designer.ActionColorIndex = clrindex;
        Designer.ActionUserRealName = userRealName;
        Designer.ActionJobCode = jobCode;
        Designer.ActionJobName = jobName;
        Designer.ActionProxyJobCode = proxyJobCode;
        Designer.ActionProxyJobName = proxyJobName;
        Designer.ActionProxyUserName = proxyUserName;
        Designer.ActionProxyUserRealName = proxyUserRealName;
        Designer.ActionFieldWrite = fieldWrite;
        Designer.ActionCheckState = checkState;
        Designer.ActionDept = dept;
        Designer.ActionFlag = flag;
        Designer.ActionDeptMode = Number(nodeMode);
        Designer.ActionStrategy = strategy;
        Designer.ActionItem1 = isEndNode;
        Designer.ModifyAction();
    }


    $(function () {
        re = checkOfficeEditInstalled();
        if (!re) {
            <%if(isUsed){%> //判断网盘是否启用
            if (isWow64()) {
                $('<div></div>').html('您还没有安装流程设计控件，请点击此处下载安装！').activebar({
                    'icon': '../images/alert.gif',
                    'highlight': '#FBFBB3',
                    'url': '../activex/clouddisk_x64.exe',
                    'button': '../images/bar_close.gif'
                });
            } else {
                $('<div></div>').html('您还没有安装流程设计控件，请点击此处下载安装！').activebar({
                    'icon': '../images/alert.gif',
                    'highlight': '#FBFBB3',
                    'url': '../activex/clouddisk.exe',
                    'button': '../images/bar_close.gif'
                });
            }
            <%}else{ %>
            $('<div></div>').html('您还没有安装流程设计控件，请点击此处下载安装！').activebar({
                'icon': '../images/alert.gif',
                'highlight': '#FBFBB3',
                'url': '../activex/oa_client.exe',
                'button': '../images/bar_close.gif'
            });
            <%}%>
        }
        if (!isIE()) {
            $('#infoSpan').html("设计器只能在IE内核浏览器使用!");
        }
    });

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

    function OpenModifyWin() {
        var isActionSelected = Designer.isActionSelected
        if (isActionSelected) {
            // showModalDialog('flow_predefine_action_modify.jsp',window.self,'dialogWidth:480px;dialogHeight:400px;status:no;help:no;')
            // alert(getActionFieldWrite());
            openWin("flow_predefine_action_modify.jsp?flowTypeCode=<%=flowTypeCode%>" + "&hidFieldWrite=" + getActionFieldWrite(), 620, 475);
        } else
            jAlert("请选择一个动作！", "提示");
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
        openWin("flow_predefine_link_modify.jsp?flowTypeCode=<%=flowTypeCode%>&conditionType=" + conditionType + "&title=" + t, 620, 320);
    }

    function OpenLinkPropertyNormalWin() {
        var t = GetSelectedLinkProperty("title");
        t = encodeURI(t);
        openWin("flow_predefine_link_normal_modify.jsp?title=" + t, 620, 260);
    }

    function form1_onsubmit() {
        if (form1.title.value == "") {
            jAlert("请填写名称！", "提示");
            return false;
        }
        form1.flowString.value = Designer.Workflow;
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

</script>
</html>
