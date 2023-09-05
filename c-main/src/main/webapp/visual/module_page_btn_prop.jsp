<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb" %>
<%@ page import="com.redmoon.oa.visual.ModuleUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String moduleCode = ParamUtil.get(request, "moduleCode");
    ModuleSetupDb msd = new ModuleSetupDb();
    msd = msd.getModuleSetupDb(moduleCode);

    if (!msd.isLoaded()) {
        out.print(StrUtil.jAlert_Back("该模块不存在！", "提示"));
        return;
    }

    String formCode = msd.getString("form_code");
    String pageType = ParamUtil.get(request, "pageType");
    boolean isAddPage = ConstUtil.PAGE_TYPE_ADD.equals(pageType);
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>智能模块设计 - 页面设置</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link href="../js/bootstrap/css/bootstrap.min.css" rel="stylesheet"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script type="text/javascript" src="../js/jquery.toaster.js"></script>
</head>
<body>
<%
    if (!privilege.isUserPrivValid(request, "admin")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    com.redmoon.oa.flow.Leaf rootlf = new com.redmoon.oa.flow.Leaf();
    rootlf = rootlf.getLeaf(com.redmoon.oa.flow.Leaf.CODE_ROOT);
    com.redmoon.oa.flow.DirectoryView flowdv = new com.redmoon.oa.flow.DirectoryView(rootlf);

    String btnId = ParamUtil.get(request, "btnId");
    boolean isSysBtn = false;
    String btnDefaultName = ModuleUtil.getBtnDefaultName(btnId);
    if (!"".equals(btnDefaultName)) {
        isSysBtn = true;
    } else if (btnId.equals(ConstUtil.BTN_PRINT)) {
        isSysBtn = true;
    } else if (btnId.equals(ConstUtil.BTN_OK)) {
        isSysBtn = true;
    } else if (btnId.equals(ConstUtil.BTN_CLOSE)) {
        isSysBtn = true;
    } else if (btnId.equals(ConstUtil.BTN_BACK)) {
        isSysBtn = true;
    }
    String strDisNone = isSysBtn?" style='display:none' ":"";
%>
<form id="form1">
    <table width="98%" align="center" class="tabStyle_1 percent98">
        <tr>
            <td colspan="2" class="tabStyle_1_title">按钮属性</td>
        </tr>
        <tr>
            <td align="center" width="20%">ID</td>
            <td align="left">
                <%
                    if (isSysBtn) {
                        out.print("系统按钮：" + btnDefaultName);
                %>
                <input id="btnId" name="btnId" size="20" type="hidden" value="<%=btnId%>"/>
                <%
                    } else {
                %>
                <input id="btnId" name="btnId" size="20" value="<%=btnId%>"/>
                <%
                    }
                %>
            </td>
        </tr>
        <tr>
            <td align="center">名称</td>
            <td align="left"><input id="linkName" name="linkName" size="20" value="<%=btnDefaultName%>"/>
            </td>
        </tr>
        <tr>
            <td align="center">提示</td>
            <td align="left"><input id="title" name="title" size="20"/>
            </td>
        </tr>
        <tr>
            <td align="center">启用</td>
            <td align="left"><input id="enabled" name="enabled" type="checkbox" checked/>
            </td>
        </tr>
        <tr style="display:<%=isAddPage?"none":""%>">
            <td align="center">条件
            </td>
            <td align="left"><img src="../admin/images/combination.png" style="margin-bottom:-5px;"/>
                <a href="javascript:;" onclick="openCondition(o('linkCond'), o('imgConds'))" title="当满足条件时，显示链接">配置条件</a>
                <span style="margin:10px">
                <img src="../admin/images/gou.png" style="margin-bottom:-5px;width:20px;height:20px;display:none" id="imgConds"/>
                </span>
                <textarea id="linkCond" name="linkCond" style="display:none"></textarea></td>
        </tr>
        <tr <%=strDisNone%>>
            <td align="center">事件
            </td>
            <td align="left"><select id="linkEvent" name="linkEvent">
                <option value="link">链接</option>
                <option value="click">点击</option>
                <option value="flow">发起流程</option>
            </select>
                <input id="linkHref" name="linkHref"/><BR/>(注：点击事件方法中如有双引号将会被自动替换为单引号)
                <div id="divFlow" style="display:none">
                    <select id="flowTypeCode" name="flowTypeCode">
                        <%
                            flowdv.ShowDirectoryAsOptions(request, out, rootlf, rootlf.getLayer());
                        %>
                    </select>
                    <input id="params" name="params" type="hidden"/>
                    <a href="javascript:" id="btnFlowMap"><i class="fa fa-cog" style="margin-right:5px"></i>映射字段</a>
                    <script>
                        $(function () {
                            $('#linkEvent').change(function () {
                                if ($(this).val() == 'flow') {
                                    $('#divFlow').show();
                                    $('#linkHref').hide();
                                } else {
                                    $('#divFlow').hide();
                                    $('#linkHref').show();
                                }
                            });

                            $('#btnFlowMap').click(function () {
                                if ($('#flowTypeCode').val() == 'not') {
                                    jAlert('请选择流程！', '提示');
                                    return;
                                }
                                curParamId = "params";
                                openWin('../flow/form_data_map.jsp?formCode=<%=formCode%>&flowTypeCode=' + $('#flowTypeCode').val(), 800, 600);
                            })
                        });

                        function setSequence(mapJson) {
                            $('#' + curParamId).val(mapJson);
                        }
                    </script>
                </div>
            </td>
        </tr>
        <tr style="display:<%=isAddPage?"none":""%>">
            <td align="center">角色
            </td>
            <td align="left"><textarea title="为空则表示角色不限，均可以看见此按钮" id="roleDescs" name="roleDescs" style="width:100%; height:80px" readonly></textarea>
                <input id="roleCodes" name="roleCodes" type="hidden"/>
                <a href="javascript:;" onclick="selRoles()">选择角色</a></td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <button id="btnOk" class="btn btn-default">确定</button>
                <input name="moduleCode" value="<%=moduleCode%>" type="hidden"/></td>
        </tr>
    </table>
</form>
<br>
</body>
<script>
    function openWin(url, width, height) {
        var newwin = window.open(url, "fieldWin", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width=" + width + ",height=" + height);
        return newwin;
    }

    var curCondsObj, curImgObj;

    function openCondition(condsObj, imgObj) {
        curCondsObj = condsObj;
        curImgObj = imgObj

        openWin("", 1024, 568);

        var url = "module_combination_condition.jsp";
        var tempForm = document.createElement("form");
        tempForm.id = "tempForm1";
        tempForm.method = "post";
        tempForm.action = url;

        var hideInput = document.createElement("input");
        hideInput.type = "hidden";
        hideInput.name = "condition";
        hideInput.value = curCondsObj.value;
        tempForm.appendChild(hideInput);

        hideInput = document.createElement("input");
        hideInput.type = "hidden";
        hideInput.name = "fromValue";
        hideInput.value = "";
        tempForm.appendChild(hideInput);

        hideInput = document.createElement("input");
        hideInput.type = "hidden";
        hideInput.name = "toValue";
        hideInput.value = ""
        tempForm.appendChild(hideInput);

        hideInput = document.createElement("input");
        hideInput.type = "hidden";
        hideInput.name = "moduleCode";
        hideInput.value = "<%=moduleCode %>";
        tempForm.appendChild(hideInput);

        hideInput = document.createElement("input");
        hideInput.type = "hidden";
        hideInput.name = "operate";
        hideInput.value = "";
        tempForm.appendChild(hideInput);

        document.body.appendChild(tempForm);
        tempForm.target = "fieldWin";
        tempForm.submit();
        document.body.removeChild(tempForm);
    }

    function setCondition(val) {
        curCondsObj.value = val;
        if (val == "") {
            $(curImgObj).hide();
        } else {
            $(curImgObj).show();
        }
    }

    var objCode, objDesc;

    function selRoles() {
        objCode = o('roleCodes');
        objDesc = o('roleDescs');
        openWin('../role_multi_sel.jsp?roleCodes=' + objCode.value + '&unitCode=<%=StrUtil.UrlEncode(privilege.getUserUnitCode(request))%>', 526, 435);
    }

    function setRoles(roles, descs) {
        objCode.value = roles;
        objDesc.value = descs;
    }

    function makeBtnJson() {
        if ($('#btnId').val() == '') {
            jAlert('请填写ID', '提示');
            $('#btnId').focus();
            return false;
        }
        if ($('#linkName').val() == '') {
            jAlert('请填写名称', '提示');
            $('#linkName').focus();
            return false;
        }

        var linkHref = $('#linkHref').val();
        var linkName = $("#linkName").val();
        var linkCond = $("#linkCond").val();
        var linkValue = $("#linkValue").val();
        var linkEvent = $("#linkEvent").val();
        var linkRole = $("#roleCodes").val();

        if (linkEvent == "flow") {
            var flowTypeCode = $('#flowTypeCode').val();
            if (flowTypeCode == '' || "not" == flowTypeCode) {
                jAlert("请选择流程！", "提示");
                return;
            }
            var json = {};

            json.flowTypeCode = flowTypeCode;
            json.params = $('#params').val();

            linkHref = encodeJSON(JSON.stringify(json));
        } else {
            // 替换操作列链接点击事件中的双引号为单引号，以免生成如href="javascript:fun(arg1, "arg2")"，致出错
            linkHref = linkHref.replaceAll("\"", "'");
            linkHref = encodeJSON(linkHref); // 替换掉javascript方法中用以间隔参数的逗号
        }

        var r = {};
        r.href = linkHref;
        r.name = linkName;
        r.cond = linkCond;
        r.value = linkValue;
        r.event = linkEvent;
        r.role = linkRole;
        r.id = $('#btnId').val();
        r.title = $('#title').val();
        r.enabled = $('#enabled').prop('checked');
        // console.log(r);
        return JSON.stringify(r);
    }

    // 对字符串中的引号进行编码，以免引起json解析问题
    function encodeJSON(jsonString) {
        jsonString = jsonString.replace(/\"/gi, "%dq");
        return jsonString.replace(/'/gi, "%sq");
    }

    function decodeJSON(jsonString) {
        jsonString = jsonString.replace(/%dq/gi, '"');
        jsonString = jsonString.replace(/%sq/gi, "'");
        return jsonString;
    }

    function getMaps() {
        return $('#params').val();
    }

    function setSequence(mapJson) {
        $('#params').val(mapJson);
    }

    function loadProp() {
        var prop = window.opener.getBtnProp();
        if (prop == '') {
            return;
        }
        prop = $.parseJSON(prop);
        // 系统按钮的prop.name可能为空
        if (prop.name && prop.name!='') {
            $('#linkName').val(prop.name);
        }
        $('#linkHref').val(decodeJSON(prop.href));
        $("#linkCond").val(prop.cond);
        $("#linkValue").val(prop.value);
        $("#linkEvent").val(prop.event);
        $("#roleCodes").val(prop.role);
        $('#btnId').val(prop.id);
        $('#title').val(prop.title);
        $('#enabled').prop('checked', prop.enabled);

        if (prop.event == "flow") {
            $('#divFlow').show();
            $('#linkHref').hide();
            var json = $.parseJSON(decodeJSON(prop.href));
            $('#flowTypeCode').val(json.flowTypeCode);
            $('#params').val(json.params);
        }
        else {
            $('#divFlow').hide();
            $('#linkHref').show();
        }

        if (prop.cond != '') {
            $('#imgConds').show();
        }

        $.get("../admin/getRoleDescs.do", {
                roleCodes: prop.role
            },
            function (data) {
                o("roleDescs").value = data.trim();
            }
        );
    }

    $(function() {
        $('#btnOk').click(function (e) {
            e.preventDefault();
            var r = makeBtnJson();
            if (!r) {
                return;
            }
            window.opener.setBtnProp(r);
            window.close();
        });

        loadProp();
    });

</script>
</html>