<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>嵌套表属性</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexbox/flexbox.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery.flexbox.js"></script>
    <script src="../inc/map.js"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../js/jquery-alerts/jquery.alerts.js"></script>
    <script src="../js/jquery-alerts/cws.alerts.js"></script>
</head>
<body>
<%
    String editFlag = ParamUtil.get(request, "editFlag");
    String nestType = ParamUtil.get(request, "nestType");
    if (editFlag.equals("")) {
%>
<%@ include file="module_field_sel_inc_menu_top.jsp" %>
<script>
    o("menu1").className = "current";
</script>
<%
    }
%>
<div class="spacerH"></div>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    String params = ParamUtil.get(request, "params");
    String openerFormCode = ParamUtil.get(request, "openerFormCode");
    String op = ParamUtil.get(request, "op");
    String oldRelateCode = ParamUtil.get(request, "oldRelateCode");

    int isTab = ParamUtil.getInt(request, "isTab", 0);
    int isPage = 0, pageSize = 20, isSearchable = 0;
    String jsonStr = ParamUtil.get(request, "jsonStr");
    String canAdd = "", canEdit = "", canImport = "", canDel = "", canSel = "", canExport = "";
    int formViewId = -1;
    String propStat = "";
    JSONObject jsonObject = null;
    if (!"".equals(jsonStr)) {
        jsonObject = new JSONObject(jsonStr);
        try {
            canAdd = jsonObject.getString("canAdd");
            canEdit = jsonObject.getString("canEdit");
            canImport = jsonObject.getString("canImport");
            canDel = jsonObject.getString("canDel");
            canSel = jsonObject.getString("canSel");
            canExport = jsonObject.getString("canExport");
            if (jsonObject.has("formViewId")) {
                formViewId = StrUtil.toInt(jsonObject.getString("formViewId"), -1);
            }
            if (jsonObject.has("propStat")) {
                propStat = jsonObject.getString("propStat");
            }
            if (jsonObject.has("isTab")) {
                isTab = jsonObject.getInt("isTab");
            }
            if (jsonObject.has("isPage")) {
                isPage = jsonObject.getInt("isPage");
                pageSize = jsonObject.getInt("pageSize");
            }
            if (jsonObject.has("isSearchable")) {
                isSearchable = jsonObject.getInt("isSearchable");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    ModuleRelateDb mrd = new ModuleRelateDb();
    String selectedValue = "";
    if ("add".equals(op)) {
        mrd = mrd.getModuleRelateDb(openerFormCode, params);
        if (mrd != null) {
            return;
        }
        mrd = new ModuleRelateDb();
        //获取所有关联模块
        Vector tempV = mrd.getModulesRelated(openerFormCode);
        try {
            mrd.create(new JdbcTemplate(), new Object[]{
                    openerFormCode, params, "id",
                    ModuleRelateDb.TYPE_MULTI,
                    (double) (tempV.size() + 1), com.redmoon.oa.flow.FormDAO.STATUS_DONE, ""});
        } catch (ResKeyException e) {
            e.printStackTrace();
        }
        return;
    } else if ("edit".equals(op)) {
        //删除原关联
        mrd = mrd.getModuleRelateDb(openerFormCode, oldRelateCode);
        if (mrd != null) {
            mrd.del();
        }

        //添加新关联
        mrd = new ModuleRelateDb();
        //获取所有关联模块
        Vector v = mrd.getModulesRelated(openerFormCode);

        try {
            mrd.create(new JdbcTemplate(), new Object[]{
                    openerFormCode, params, "id",
                    ModuleRelateDb.TYPE_MULTI,
                    (double) (v.size() + 1), com.redmoon.oa.flow.FormDAO.STATUS_DONE, ""});
        } catch (ResKeyException e) {
            e.printStackTrace();
        }
        return;
    }
%>
<table width="100%" align="center" cellPadding="0" cellSpacing="0"
       class="tabStyle_1" id="mapTable" style="padding: 0px; margin: 0px;">
    <tbody>
    <tr>
        <td height="28" colspan="5" class="tabStyle_1_title">
            嵌套表
        </td>
    </tr>
    <tr>
        <%
            ModuleSetupDb msd = new ModuleSetupDb();
            Vector v = msd.listUsed();
            Iterator ir = v.iterator();
            String json = "";
            while (ir.hasNext()) {
                ModuleSetupDb moduleSetupDb = (ModuleSetupDb) ir.next();
                if (params != null && params.equals(moduleSetupDb.getString("code"))) {
                    selectedValue = moduleSetupDb.getString("name");
                }
                if ("".equals(json)) {
                    json = "{\"id\":\"" + moduleSetupDb.getString("code") + "\", \"name\":\""
                            + moduleSetupDb.getString("name") + "\"}";
                } else {
                    json += ",{\"id\":\"" + moduleSetupDb.getString("code") + "\", \"name\":\""
                            + moduleSetupDb.getString("name") + "\"}";
                }
            }
        %>
        <td width="14%" colspan="-1" align="center">
            表单
        </td>
        <td align="center">
            <div id="destForm" style="float: left"></div>
            <span><a id="btnDestMoudle" href="javascript:" title="维护模块">维护</a></span>
            <script>
                var dests = [];
                var sourceCode = "";
                var destForm = $('#destForm').flexbox({
                    "results": [<%=json%>],
                    "total":<%=v.size()%>
                }, {
                    initialValue: '<%=selectedValue%>',
                    watermark: '请选择表单',
                    paging: false,
                    maxVisibleRows: 10,
                    onSelect: function () {
                        sourceCode = $("input[name=destForm]").val();
                        $("#code").val(sourceCode);
                        $("#formCode").val(sourceCode);
                        $("#params").val(sourceCode);
                        $("#subForm").submit();
                        //openWin("module_field_sel_nest1.jsp?nestType=nest_table&openerFormCode=<%=StrUtil.UrlEncode(openerFormCode)%>&params=" + sourceCode,800,600)
                    }
                });
            </script>
        </td>
        <td align="left">
            <input type="checkbox" id="isTab" name="isTab" value="1" <%=isTab == 1 ? "checked" : "" %> <%=!ConstUtil.NEST_TABLE.equals(nestType) ? "" : "style='display:none'"%> />
            <%
                if (!ConstUtil.NEST_TABLE.equals(nestType)) {
            %>
            显示为选项卡
            <%
                }
            %>
        </td>
    </tr>
    <%
        String dis = "";
        if (ConstUtil.NEST_TABLE.equals(nestType)) {
            dis = "display:none";
        }
    %>
    <tr style="<%=dis%>">
        <td height="42" align="center">分页</td>
        <td height="42" align="left">
            <input id="isPage" name="isPage" value="1" type="checkbox" <%=isPage==1?"checked":"" %> title="是否分页" />
        </td>
        <td height="42" align="left">
            每页条数
            <input id="pageSize" name="pageSize" value="<%=pageSize%>"/>
        </td>
    </tr>
    <tr style="<%=dis%>">
        <td height="42" align="center">搜索</td>
        <td height="42" colspan="2" align="left">
            <input id="isSearchable" name="isSearchable" value="1" type="checkbox" <%=isSearchable==1?"checked":"" %> title="是否能搜索" />
        </td>
    </tr>
    <tr>
        <td colspan="3" align="left" class="tabStyle_1_title">权限</td>
    </tr>
    <tr>
        <td colspan="3" align="left">
            <span class="tabStyle_1_title">
            <input id="canAdd" name="canAdd" type="checkbox" value="true"/>
            &nbsp;增加
                &nbsp;&nbsp;
                <input id="canEdit" name="canEdit" type="checkbox" value="true"/>
            &nbsp;修改
                &nbsp;&nbsp;
                <input id="canImport" name="canImport" type="checkbox" value="true"/>
            &nbsp;导入
                &nbsp;&nbsp;
                <input id="canExport" name="canExport" type="checkbox" value="true"/>
            &nbsp;导出
                &nbsp;&nbsp;
                <input id="canDel" name="canDel" type="checkbox" value="true"/>
            &nbsp;删除
              <span style="display: none">
                &nbsp;&nbsp;
                <input id="canSel" name="canSel" type="checkbox" value="true"/>
            &nbsp;选择
              </span>
            </span>
        </td>
    </tr>
    </tbody>
</table>
<table align="center" cellPadding="0" cellSpacing="0" class="tabStyle_1" id="mapTable" style="padding: 0px; margin: 0px; width: 100%">
    <tr>
        <td>
            <div id="moduleFieldPreviewBox">
                <jsp:include page="module_field_inc_preview.jsp">
                    <jsp:param name="code" value="<%=params%>"/>
                    <jsp:param name="formCode" value="<%=params%>"/>
                    <jsp:param name="resource" value="nest"/>
                </jsp:include>
            </div>
            <%
                if ("nest_table".equals(nestType)) {
            %>
            视图：
            <%
                if (!"".equals(params)) {
            %>
            <select id="formView" name="formView" title="视图">
                <option value="-1">请选择</option>
                <%
                    FormViewDb formViewDb = new FormViewDb();
                    Vector vView = formViewDb.getViews(params);
                    Iterator irView = vView.iterator();
                    while (irView.hasNext()) {
                        formViewDb = (FormViewDb) irView.next();
                %>
                <option value="<%=formViewDb.getLong("id")%>"><%=formViewDb.getString("name")%>
                </option>
                <%
                    }
                %>
            </select>
            <%
                }
            %>
            <script>
                $('#formView').change(function () {
                    if ($(this).val() != -1) {
                        $('#moduleFieldPreviewBox').hide();
                    } else {
                        $('#moduleFieldPreviewBox').show();
                    }
                });
            </script>
            <%
                }
            %>
        </td>
    </tr>
    <tr>
        <td class="tabStyle_1_title">
            合计字段
        </td>
    </tr>
    <tr>
        <td>
            <%
                if (StringUtils.isNotEmpty(params)) {
            %>
            <a href="javascript:" onclick="addCalcuField()">添加字段</a>
            <div id="divCalcuField" style="text-align:left; margin-top:3px;">
                <%
                    StringBuffer optsFieldsNum = new StringBuffer();
                    FormDb fd = new FormDb();
                    fd = fd.getFormDb(params);
                    Iterator irField = fd.getFields().iterator();
                    while (irField.hasNext()) {
                        FormField ff = (FormField) irField.next();
                        int fieldType = ff.getFieldType();
                        if (fieldType == FormField.FIELD_TYPE_INT
                                || fieldType == FormField.FIELD_TYPE_FLOAT
                                || fieldType == FormField.FIELD_TYPE_DOUBLE
                                || fieldType == FormField.FIELD_TYPE_PRICE
                                || fieldType == FormField.FIELD_TYPE_LONG
                        ) {
                            optsFieldsNum.append("<option value='" + ff.getName() + "'>" + ff.getTitle() + "</option>");
                        }
                    }

                    int curCalcuFieldCount = 0;
                    if (propStat.equals("")) {
                        propStat = "{}";
                    }
                    JSONObject jsonPropStat = new JSONObject(propStat);
                    Iterator irPropStat = jsonPropStat.keys();
                    while (irPropStat.hasNext()) {
                        String key = (String) irPropStat.next();
                %>
                <div id="divCalcuField<%=curCalcuFieldCount%>" style="float:left">
                    <select id="calcFieldCode<%=curCalcuFieldCount%>" name="calcFieldCode">
                        <option value="">无</option>
                        <%=optsFieldsNum.toString()%>
                    </select>
                    <select id="calcFunc<%=curCalcuFieldCount%>" name="calcFunc">
                        <option value="0">求和</option>
                        <%--<option value="1">求平均值</option>--%>
                    </select>
                    <a href='javascript:;' onclick="var pNode=this.parentNode; pNode.parentNode.removeChild(pNode);">×</a>
                    &nbsp;
                </div>
                <script>
                    $("#calcFieldCode<%=curCalcuFieldCount%>").val("<%=key%>");
                    $("#calcFunc<%=curCalcuFieldCount%>").val("<%=jsonPropStat.get(key)%>");
                </script>
                <%
                        curCalcuFieldCount++;
                    }
                %>
            </div>
            <%
                }
            %>
        </td>
    </tr>
</table>
<form id="subForm">
    <input name="code" id="code" type="hidden" value="<%=params%>"/>
    <input name="formCode" id="formCode" type="hidden" value="<%=params%>"/>
    <input name="openerFormCode" id="openerFormCode" type="hidden" value="<%=openerFormCode%>"/>
    <input name="params" id="params" type="hidden" value="<%=params%>"/>
    <input name="editFlag" id="editFlag" type="hidden" value="<%=editFlag%>"/>
    <input name="nestType" id="nestType" type="hidden" value="<%=nestType%>"/>
    <input name="oldRelateCode" id="oldRelateCode" type="hidden" value="<%=oldRelateCode%>"/>
</form>
<div style="text-align: center; margin-top: 5px;">
    <input type="button" class="btn" value="确定" onclick="makeMap()"/>
</div>
</body>
<script>
    $(function () {
        <%
        if ("true".equals(canAdd)) {
        %>
        setCheckboxChecked("canAdd", "true");
        <%
        }
        %>
        <%
        if ("true".equals(canEdit)) {
        %>
        setCheckboxChecked("canEdit", "true");
        <%
        }
        %>
        <%
        if ("true".equals(canImport)) {
        %>
        setCheckboxChecked("canImport", "true");
        <%
        }
        %>
        <%
        if ("true".equals(canExport)) {
        %>
        setCheckboxChecked("canExport", "true");
        <%
        }
        %>
        <%
        if ("true".equals(canDel)) {
        %>
        setCheckboxChecked("canDel", "true");
        <%
        }
        %>
        <%
        if ("true".equals(canSel)) {
        %>
        setCheckboxChecked("canSel", "true");
        <%
        }
        %>

        $('#formView').val('<%=formViewId%>');
    });

    function makeMap() {
        if (destForm.getValue() == "") {
            jAlert("请选择嵌套表单！", "提示");
            return;
        }

        // 组合成json字符串{maps:[{sourceField:..., destField:..., editable:true, appendable:true},...{...}]}
        var str = "{\"sourceForm\":\"\", \"destForm\":\"" + destForm.getValue() + "\", \"filter\":\"\", \"maps\":[]}";
        if (destForm.getValue() == '<%=selectedValue%>') {
            var isTab = 0;
            if (o("isTab").checked) {
                isTab = 1;
            }

            var canAdd = getCheckboxValue("canAdd");
            var canEdit = getCheckboxValue("canEdit");
            var canImport = getCheckboxValue("canImport");
            var canExport = getCheckboxValue("canExport");
            var canDel = getCheckboxValue("canDel");
            var canSel = getCheckboxValue("canSel");
            var canStr = "\"canAdd\":\"" + canAdd + "\", \"canEdit\":\"" + canEdit + "\", \"canImport\":\"" + canImport + "\", \"canDel\":\"" + canDel + "\", \"canSel\":\"" + canSel + "\", \"canExport\":\"" + canExport + "\"";

            // 字段合计描述字符串处理
            var calcCodesStr = "";
            var calcFuncs = $("select[name='calcFunc']");
            var map = new Map();
            var isFound = false;
            $("select[name='calcFieldCode']").each(function(i) {
                if ($(this).val()!="") {
                    if (!map.containsKey($(this).val()))
                        map.put($(this).val(), $(this).val());
                    else {
                        isFound = true;
                        jAlert($(this).find("option:selected").text() + "存在重复！","提示");
                        return false;
                    }

                    if (calcCodesStr=="")
                        calcCodesStr = "\"" + $(this).val() + "\":\"" + calcFuncs.eq(i).val() + "\"";
                    else
                        calcCodesStr += "," + "\"" + $(this).val() + "\":\"" + calcFuncs.eq(i).val() + "\"";
                }
            })
            if (isFound)
                return;
            calcCodesStr = "{" + calcCodesStr + "}";

            var isPage = 0;
            if (o("isPage").checked) {
                isPage = 1;
            }
            var pageSize = o("pageSize").value;
            if (pageSize == "") {
                pageSize = 20;
            }

            var isSearchable = 0;
            if (o("isSearchable").checked) {
                isSearchable = 1;
            }

            str = "{\"sourceForm\":\"\", \"destForm\":\"" + $("#params").val() + "\", \"filter\":\"\", \"isTab\":" + isTab + ", \"isSearchable\":" + isSearchable + ", \"isPage\":" + isPage + ", \"pageSize\":" + pageSize + ", " + canStr + ", \"maps\":[], \"propStat\":" + calcCodesStr + ", \"formViewId\":\"" + $('#formView').val() + "\"}";
        }

        str = encodeJSON(str);
        window.opener.setSequence(str, destForm.getText());
        var editFlag = $("#editFlag").val();
        var opFlag = "";
        if (editFlag == "") {
            opFlag = "add";
        } else {
            opFlag = "edit";
        }

        // 置关联模块
        $.ajax({
            type: "post",
            async: false, // 设为同步，以免窗口关闭致调用不成功
            url: "module_field_sel_nest1.jsp",
            data: {
                openerFormCode: "<%=openerFormCode%>",
                params: "<%=params%>",
                oldRelateCode: "<%=oldRelateCode%>",
                op: opFlag
            },
            dataType: "json",
            beforeSend: function (XMLHttpRequest) {
                //ShowLoading();
            },
            success: function (data, status) {

            },
            complete: function (XMLHttpRequest, status) {
                //HideLoading();
            },
            error: function () {
                //请求出错处理
            }
        });
        window.close();
    }

    // 对字符串中的引号进行编码，以免引起json解析问题
    function encodeJSON(jsonString) {
        jsonString = jsonString.replace(/\"/gi, "%dq");
        return jsonString.replace(/'/gi, "%sq");
    }

    $.fn.outerHTML = function () {
        return $("<p></p>").append(this.clone()).html();
    };

    function addCalcuField() {
        if (o("divCalcuField0")) {
            $("#divCalcuField").append($("#divCalcuField0").outerHTML());
        }
        else {
            initDivCalcuField();
        }
    }

    function initDivCalcuField() {
        $.ajax({
            type: "POST",
            url: "module_field_calcu_field_ajax.jsp",
            data : {
                formCode: "<%=params%>"
            },
            success: function(html) {
                $("#divCalcuField").html(html);
            },
            error: function(XMLHttpRequest, textStatus){
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText,"提示");
            }
        });
    }

    $(function() {
        $('#btnDestMoudle').click(function() {
            var destFormVal = $("#params").val();
            if (destFormVal == '') {
                jAlert('请选择嵌套表单', '提示');
                return;
            }
            openWin('module_field_list.jsp?code=' + destFormVal, 800, 600);
        });
    });
</script>
</html>